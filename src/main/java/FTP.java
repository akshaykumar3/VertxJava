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
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import org.apache.commons.net.ftp.FTPClient;
import utils.LogFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by akshay.kumar1 on 17/10/16.
 */
public class FTP extends AbstractVerticle {

    private static final Logger logger = LogFactory.getLogger(FTP.class);
    private HttpServer server = null;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);

        // Initialize the server
        server = vertx.createHttpServer();

        // Create a router object.
        Router router = Router.router(vertx);

        logger.info("Vertx Started");

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

                    String host = "127.0.0.1";
                    FTPClient client = new FTPClient();
                    try {
                        client.connect(host);
                        String userName = "";
                        String password = "";
                        client.login(userName, password);
                        String removeLocationPath = "";
                        OutputStream stream = new FileOutputStream("localPath");
                        client.retrieveFile(removeLocationPath, stream);
                    } catch (IOException e) {
                        logger.error("IOException = "+e.getMessage());
                        logger.error("IOException Cause = "+e.getCause());
                    }

                    logger.info("Done with POST Redis");
                }
            });
        });

        server.requestHandler(httpServerRequest -> {
            router.accept(httpServerRequest);
        }).listen(config().getInteger("server_port"));
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
    }
}
