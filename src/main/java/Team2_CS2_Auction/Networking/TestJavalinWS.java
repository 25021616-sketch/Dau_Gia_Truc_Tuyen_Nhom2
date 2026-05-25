package Team2_CS2_Auction.Networking;
import io.javalin.Javalin;
import java.time.Duration;
public class TestJavalinWS {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
        });
        app.ws("/test", ws -> {
            ws.onConnect(ctx -> {
                ctx.session.setIdleTimeout(Duration.ofHours(24));
                System.out.println("Timeout set successfully!");
            });
        });
        System.out.println("Compile success!");
    }
}
