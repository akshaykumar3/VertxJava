import io.vertx.core.*;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import utils.ApiHelper;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.LogManager;

/**
 * Created by akshay.kumar1 on 30/09/16.
 */
public class ApiCall extends AbstractVerticle {

    private HttpServer server = null;
    public static Logger logger;

    public static HttpClient httpClient;

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        super.start(startFuture);

        // Initialize the server
        server = vertx.createHttpServer();

        // Create a router object.
        Router router = Router.router(vertx);

        //Initialize the logger
        initLogger();

        HttpClientOptions clientOptions = new HttpClientOptions();
        clientOptions.setMaxPoolSize(500);
        clientOptions.setPipelining(true);
        clientOptions.setKeepAlive(true);
        httpClient = vertx.createHttpClient(clientOptions);


        /**
         * curl -X GET -H "Content-Type: application/json" -H "imei: 123" "http://127.0.0.1:3000/getUrl"
         * **/
        Route getRoute = router.route(HttpMethod.GET, config().getString("get_url"));
        getRoute.handler(routingContext -> {
            logger.info("Inside GET call");
            HttpServerRequest serverRequest = routingContext.request();

            Future<JsonObject> apiResponse = ApiHelper.getApiResponse(httpClient, logger);

            Future<JsonObject> usResponse = ApiHelper.getApiResponse(httpClient, logger);


            CompositeFuture.all(apiResponse, usResponse).setHandler(new Handler<AsyncResult<CompositeFuture>>() {
                @Override
                public void handle(AsyncResult<CompositeFuture> compositeFutureAsyncResult) {
                    if (compositeFutureAsyncResult.failed()) {
                        logger.error("Compite Future failed cause = "+compositeFutureAsyncResult.cause());
                        serverRequest.response().end(createResponse("FAILURE", null));
                        return;
                    }
                    CompositeFuture compositeFuture = compositeFutureAsyncResult.result();
                    JsonObject firstResp = compositeFuture.resultAt(0);
                    JsonObject secondResp = compositeFuture.resultAt(1);
                    logger.info("firstResp = "+firstResp.toString()+"\nsecondResp = "+secondResp.toString());
                    firstResp.mergeIn(secondResp);
                    serverRequest.response().end(createResponse("SUCCESS", firstResp));
                }
            });
            logger.info("Done with GET");
        });


        server.requestHandler(router::accept).listen(config().getInteger("server_port"));
    }

    private void initLogger(){
        try {
            final InputStream inputStream = WebServer.class.getResourceAsStream("/logging.properties");
            LogManager.getLogManager().readConfiguration(inputStream);
            logger = (Logger) LoggerFactory.getLogger(WebServer.class.getName());
            logger.info("LogFactory initialized");
        } catch(Exception ex) {

        }
    }

    private String createResponse(String input) {
        JsonObject json = new JsonObject();
        json.put("response", input);
        return (json.toString());
    }

    private String createResponse(String status, JsonObject response) {
        JsonObject json = new JsonObject();
        json.put("status", status);
        json.put("response", response);
        return (json.toString());
    }

    private boolean isNullOrEmpty(String input) {
        return (null == input || input.isEmpty());
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        // TODO Auto-generated method stub
        super.stop(stopFuture);
    }
}
