# Dockerfile - Không cần sửa pom.xml, dùng classpath trực tiếp

# --- Giai đoạn 1: Build với Maven ---
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml để cache dependencies trước
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

# Copy source và compile
COPY src ./src
RUN mvn compile -DskipTests -q

# Copy tất cả dependencies ra thư mục riêng
RUN mvn dependency:copy-dependencies -DoutputDirectory=target/libs -DskipTests -q

# --- Giai đoạn 2: Chạy server với JRE nhẹ ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy compiled classes
COPY --from=build /app/target/classes ./classes

# Copy tất cả dependencies (MySQL, Gson, v.v.)
COPY --from=build /app/target/libs ./libs

# Khai báo port TCP 8080
EXPOSE 8080

# Chạy ServerMain với classpath đầy đủ
CMD ["java", "-cp", "classes:libs/*", "Team2_CS2_Auction.Networking.ServerMain"]
