package utils;


import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;


/**
 * Created by akshay.kumar1 on 05/10/16.
 */
public final class ApiHelper {
    private ApiHelper() {}

    public static Future<JsonObject> getApiResponse(HttpClient httpClient, Logger logger) {
        Future<JsonObject> futureObj= Future.future();

        return futureObj;
    }
}
