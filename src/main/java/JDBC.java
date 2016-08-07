import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.VoidHandler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * Created by akshay.kumar1 on 07/08/16.
 */
public class JDBC extends AbstractVerticle {

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

        final JDBCClient jdbcClient = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", "jdbc:mysql://127.0.0.1/MyDB?autoReconnect=true&failOverReadOnly=false&maxReconnects=10&useUnicode=true&characterEncoding=UTF-8")
                .put("user", "root")
                .put("password", "")
                .put("driver_class", "com.mysql.jdbc.Driver")
                .put("max_pool_size", 30));


        logger.info("Vertx Started");

        // This is a GET API. Call this API "/getMysql?id=1"
        Route getRoute = router.route(HttpMethod.GET, config().getString("get_mysql"));
        getRoute.handler(routingContext -> {
            logger.info("Inside GET call");
            HttpServerRequest request = routingContext.request();
            String input = request.params().get("id");
            int id = Integer.parseInt(input);


            String selectQuery = String.format("select text from VertxTest where id = %d;", id);
            logger.info("Mysql statement = " + selectQuery);

            jdbcClient.getConnection( conn -> {
                if(conn.failed()) {
                    logger.error("MySql connection failed");
                    request.response().end(createResponse("FAILURE"));
                    return;
                }

                SQLConnection connection = conn.result();

                connection.query(selectQuery, res -> {
                    if(res.failed()) {
                        logger.error("MySql query failed");
                        request.response().end(createResponse("FAILURE"));
                        return;
                    }

                    JsonObject response = res.result().toJson();
                    logger.info("response = "+response.toString());
                    JsonArray responseArray = response.getJsonArray("rows");
                    JsonObject ans = responseArray.getJsonObject(0);
                    String value = ans.getString("text");
                    request.response().end(createResponse(value));

                    // Close the connection
                    connection.close(done -> {
                        if (done.failed()) {
                            throw new RuntimeException(done.cause());
                        }
                    });
                });
            });

            logger.info("Done with GET");
        });


        // This is a POST API. Call this API "/postMysql"
        Route postRoute = router.route(HttpMethod.POST, config().getString("post_mysql"));
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
                    String text = jsonRequest.getString("text");
                    logger.info("POST call with para = "+text);

                    if(isNullOrEmpty(text)) {
                        text = "Empty Input";
                    }

                    String insertQuery = String.format("INSERT INTO `VertxTest` (`text`) VALUES (\"%s\");", text);
                    logger.info("Mysql statement = " + insertQuery);

                    jdbcClient.getConnection( conn -> {
                        if(conn.failed()) {
                            logger.error("MySql connection failed");
                            request.response().end(createResponse("FAILURE"));
                            return;
                        }

                        SQLConnection connection = conn.result();

                        connection.execute(insertQuery, insert -> {
                            if(insert.failed()) {
                                logger.error("MySql Insert failed");
                                request.response().end(createResponse("FAILURE"));
                                return;
                            }

                            request.response().end(createResponse("SUCCESS"));

                            // Close the connection
                            connection.close(done -> {
                                if (done.failed()) {
                                    throw new RuntimeException(done.cause());
                                }
                            });
                        });
                    });

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
