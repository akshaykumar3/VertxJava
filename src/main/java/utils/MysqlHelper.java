package utils;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

public final class MysqlHelper {
	
	private static final Logger logger = LogFactory.getLogger(MysqlHelper.class);

	public static Future<JsonObject> mysqlGet(JDBCClient jdbcClient, String query) {
		logger.info("query = "+query);
		Future<JsonObject> mysqlFuture = Future.future();
		jdbcClient.getConnection(conn -> {
			if (conn.failed()) {
				logger.error("MySql connection failed");
                mysqlFuture.fail("MySql connection failed for query = "+query);
			}
			
			SQLConnection connection = conn.result();
			
			connection.query(query, res -> {
				if(res.failed()) {
                    logger.error("MySql query failed");
                    mysqlFuture.fail("MySql query failed for query = "+query);
                }

                JsonObject response = res.result().toJson();
                logger.info("response = "+response.toString());
                JsonArray responseArray = response.getJsonArray("rows");
                JsonObject ans = responseArray.getJsonObject(0);

                // Close the connection
                connection.close(done -> {
                    if (done.failed()) {
                        throw new RuntimeException(done.cause());
                    }
                });
                
                mysqlFuture.complete(ans);
			});
		});
		return mysqlFuture;
	}

	public static void createMysqlEntry(String query, JsonArray params, JDBCClient jdbcClient, Future<Void> future) {

		jdbcClient.getConnection(conn -> {
			if (conn.failed()) {
				logger.error("MySql connection failed");
				future.fail("MySql connection failed for query = "+query);
			}

			SQLConnection connection = conn.result();

			connection.updateWithParams(query, params, res -> {

			});

			connection.query(query, res -> {
				if(res.failed()) {
					logger.error("MySql query failed");
					future.fail("MySql query failed for query = "+query);
				}

				JsonObject response = res.result().toJson();
				logger.info("response = "+response.toString());
				JsonArray responseArray = response.getJsonArray("rows");
				JsonObject ans = responseArray.getJsonObject(0);

				// Close the connection
				connection.close(done -> {
					if (done.failed()) {
						throw new RuntimeException(done.cause());
					}
				});

				future.complete();
			});
		});

	}
}
