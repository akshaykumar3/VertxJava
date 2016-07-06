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
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * Created by akshay.kumar1 on 06/07/16.
 */
public class Redis extends AbstractVerticle {

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

        // Create the redis client
        final RedisClient redisClient = RedisClient.create(vertx, new RedisOptions().setHost(config().getString("redis_host")));

        logger.info("Vertx Started");

        // This is a GET API. Call this API "/getRedis?key=abc"
        Route getRoute = router.route(HttpMethod.GET, config().getString("get_redis_url"));
        getRoute.handler(routingContext -> {
            HttpServerRequest request = routingContext.request();
            String key = request.params().get("key");

            redisClient.get(key, res -> {
                if(res.succeeded()) {
                    logger.info("Redis value = "+res.result());
                    request.response().end(createResponse(res.result()));
                    return;
                } else {
                    logger.error("Redis get failed");
                    request.response().end(createResponse("FAILURE"));
                }
            });
            logger.info("Done with GET Redis");
        });


        // This is a POST API. Call this API "/postRedis"
        Route postRoute = router.route(HttpMethod.POST, config().getString("post_redis_url"));
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
                    String key = jsonRequest.getString("key");
                    String value = jsonRequest.getString("value");
                    long ttl = jsonRequest.getLong("ttl");

                    redisClient.setex(key, ttl, value, res -> {
                        if(res.succeeded()) {
                            request.response().end(createResponse("SUCCESS"));
                        } else {
                            request.response().end(createResponse("FAILURE"));
                        }
                    });
                    logger.info("Done with POST Redis");
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
