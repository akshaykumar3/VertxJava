import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * Created by akshay.kumar1 on 08/09/16.
 */
public class UI extends AbstractVerticle {

    private static HttpServer server = null;
    private static Logger logger;

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        super.start(startFuture);

        // Create a router object.
        Router router = Router.router(vertx);

        //Initialize the logger
        initLogger();

        // Enable multipart form data parsing
        router.route().handler(BodyHandler.create());

        router.route("/").handler(routingContext -> {
            routingContext.response().putHeader("content-type", "text/html").end(
                    "<form action=\"/form\" method=\"post\">\n" +
                            "    <div>\n" +
                            "        <label for=\"name\">Enter your name:</label>\n" +
                            "        <input type=\"text\" id=\"name\" name=\"name\" />\n" +
                            "    </div>\n" +
                            "    <div class=\"button\">\n" +
                            "        <button type=\"submit\">Send</button>\n" +
                            "    </div>" +
                            "</form>"
            );
        });

        // handle the form
        router.post("/form").handler(ctx -> {
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
            // note the form attribute matches the html form element name.
            ctx.response().end("Hello " + ctx.request().getParam("name") + "!");
        });

        // Initialize the server
        server = vertx.createHttpServer();

        server.requestHandler(httpServerRequest -> {
            router.accept(httpServerRequest);
        }).listen(config().getInteger("server_port"));

    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        // TODO Auto-generated method stub
        super.stop(stopFuture);
    }

    private void initLogger(){
        try {
            final InputStream inputStream = UI.class.getResourceAsStream("/logging.properties");
            LogManager.getLogManager().readConfiguration(inputStream);
            logger = (Logger) LoggerFactory.getLogger(UI.class.getName());
            logger.info("Logger initialized");
        } catch(Exception ex) {

        }
    }
}
