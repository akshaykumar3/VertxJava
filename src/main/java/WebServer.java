import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.VoidHandler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import rx.Observable;
import rx.Scheduler;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

/**
 * Created by akshay.kumar1 on 06/07/16.
 */
public class WebServer extends AbstractVerticle {

    private HttpServer server = null;
    private Logger logger;

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        super.start(startFuture);

        // Initialize the server
        server = vertx.createHttpServer();

        // Create a router object.
        Router router = Router.router(vertx);

        //Initialize the logger
        initLogger();

        logger.info("Vertx Started");

        // This is a GET API. Call this API "/getUrl?input=Hello"
        Route getRoute = router.route(HttpMethod.GET, config().getString("get_url"));
        getRoute.handler(routingContext -> {
            logger.info("Inside GET call");
            HttpServerRequest request = routingContext.request();
            String input = request.params().get("input");
            logger.info("GET call with para = "+input);
            if(isNullOrEmpty(input)) {
                input = "Empty Input";
            }
            request.response().end(createResponse(input));
            logger.info("Done with GET");
        });


        // This is a POST API. Call this API "/postUrl"
        Route postRoute = router.route(HttpMethod.POST, config().getString("post_url"));
        postRoute.handler(routingContext -> {
            HttpServerRequest request = routingContext.request();
            final Buffer body = Buffer.buffer();

            request.handler(new Handler<Buffer>() {
                public void handle(Buffer buffer) {
                    body.appendBuffer(buffer);
                }
            });

            request.endHandler(new VoidHandler() {
                public void handle() {
                    String requestBody = body.getString(0, body.length(), "UTF-8");
                    JsonObject jsonRequest = new JsonObject(requestBody);
                    String input = jsonRequest.getString("input");
                    logger.info("POST call with para = "+input);

                    if(isNullOrEmpty(input)) {
                        input = "Empty Input";
                    }
                    request.response().end(createResponse(input));
                    logger.info("Done with POST");
                }
            });
        });

        server.requestHandler(router::accept).listen(config().getInteger("server_port"));

    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        // TODO Auto-generated method stub
        super.stop(stopFuture);
    }

    private String createResponse(String input) {
        JsonObject json = new JsonObject();
        json.put("response", input);
        return (json.toString());
    }

    private boolean isNullOrEmpty(String input) {
        return (null == input || input.isEmpty());
    }

    private void initLogger(){
        try {
            final InputStream inputStream = WebServer.class.getResourceAsStream("/logging.properties");
            LogManager.getLogManager().readConfiguration(inputStream);
            logger = (Logger) LoggerFactory.getLogger(WebServer.class.getName());
            logger.info("Logger initialized");
        } catch(Exception ex) {

        }
    }
}
