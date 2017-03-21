import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
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
import utils.MysqlHelper;

import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * Created by akshay.kumar1 on 07/08/16.
 */
public class JDBC extends AbstractVerticle {

    private HttpServer server = null;
    private Logger logger;
    public static JDBCClient jdbcClient;

    private static final String DB_INSERT = "INSERT INTO `CouponCode` (`code`, `imei`, `merchantId`, `notificationId`, `applied`) VALUES ('84001601', '1234567890123458', %d, 16, 0);";

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        super.start(startFuture);

        // Initialize the server
        server = vertx.createHttpServer();

        // Create a router object.
        Router router = Router.router(vertx);

        //Initialize the logger
        initLogger();

        jdbcClient = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", "jdbc:mysql://127.0.0.1/catalog_server?autoReconnect=true&failOverReadOnly=false&maxReconnects=10&useUnicode=true&characterEncoding=UTF-8")
                .put("user", "root")
                .put("password", "")
                .put("driver_class", "com.mysql.jdbc.Driver")
                .put("max_pool_size", 30));


        logger.info("Vertx Started");
        
        Route mysqlRoute = router.route(HttpMethod.GET, "/mysql");
        mysqlRoute.handler(routingContext -> {
        	HttpServerRequest request = routingContext.request();
        	String query1 = "select name from contents where id = 8";
        	String query2 = "select name from contents where id = 9";
        	Future<JsonObject> mySqlFuture1 = MysqlHelper.mysqlGet(jdbcClient, query1);
        	Future<JsonObject> mySqlFuture2 = MysqlHelper.mysqlGet(jdbcClient, query2);
        	
        	CompositeFuture.all(mySqlFuture1, mySqlFuture2).setHandler(res -> {
        		if (res.failed()) {
        			request.response().end(createResponse("FAILURE"));
                    return;
				}
        		
        		CompositeFuture future = res.result();
        		JsonObject result1 = future.resultAt(0);
        		JsonObject result2 = future.resultAt(0);
        		logger.info("result1 = "+result1.toString());
        		logger.info("result2 = "+result2.toString());
        		request.response().end(createResponse("SUCCESS"));
        	});
        	logger.info("Done");
        });
        
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

                    String insertQuery = "INSERT INTO `Merchants` (`emailId`, `password`, `approved`) VALUES ('akshay0007k@gmail.com', '1234', 1);";
                    logger.info("1 Mysql statement = " + insertQuery);

                    jdbcClient.getConnection( conn -> {
                        if(conn.failed()) {
                            logger.error("MySql connection failed");
                            request.response().end(createResponse("FAILURE"));
                            return;
                        }

                        SQLConnection connection = conn.result();

                        connection.setAutoCommit(false, res -> {

                            connection.update(insertQuery, insert -> {
                                if(insert.failed()) {
                                    logger.error("1 MySql Insert failed");
                                    request.response().end(createResponse("FAILURE"));
                                    return;
                                }

                                JsonObject response = insert.result().toJson();
                                logger.info("1 Response = "+response);
                                int key = response.getJsonArray("keys").getInteger(0);

                                // Album
                                String query = String.format(DB_INSERT, key);
                                logger.info("2 Mysql statement = " + query);

                                connection.update(query, resp -> {
                                    if (resp.failed()) {
                                        logger.error("2 MySql Insert failed");
                                        logger.error("2 cause = "+resp.cause());
                                        request.response().end(createResponse("FAILURE"));
                                        return;
                                    }

                                    logger.info("2 response = "+resp.result().toJson());


                                    // Artist



                                    connection.commit(outcome -> {
                                        if (outcome.succeeded()) {
                                            logger.info("SUCCESS");
                                            request.response().end(createResponse("SUCCESS"));
                                        } else {
                                            logger.error("3 MySql Insert failed");
                                            logger.error("3 cause = "+outcome.cause());
                                            request.response().end(createResponse("FAILURE"));
                                        }
                                    });
                                });


                                // Artist


                                // Close the connection
//                                conne2
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
            final InputStream inputStream = JDBC.class.getResourceAsStream("/logging.properties");
            LogManager.getLogManager().readConfiguration(inputStream);
            logger = (Logger) LoggerFactory.getLogger(JDBC.class.getName());
            logger.info("LogFactory initialized");
        } catch(Exception ex) {

        }
    }
}
