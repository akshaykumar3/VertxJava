package utils;


import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;


/**
 * Making Http call
 */
public final class ApiHelper {
    private ApiHelper() {}

    public static Future<JsonObject> getApiResponse(HttpClient httpClient, Logger logger) {
        Future<JsonObject> futureObj= Future.future();

        logger.info("1 Time = "+System.currentTimeMillis());

        final Buffer buffer = Buffer.buffer();

        HttpClientRequest request = httpClient.get(8080, "127.0.0.1", "/health_check", response -> {
            response.handler(new Handler<Buffer>() {
                @Override
                public void handle(Buffer event) {
                    buffer.appendBuffer(event);
                }
            });

            response.endHandler(handler -> {
                JsonObject apiResponse = new JsonObject(buffer.toString());
                futureObj.complete(apiResponse);
            });
        });
        request.end();

        logger.info("2 Time = "+System.currentTimeMillis());

        return futureObj;
    }
}
