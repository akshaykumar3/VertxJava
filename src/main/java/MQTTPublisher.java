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
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * Created by akshay.kumar1 on 06/07/16.
 */
public class MQTTPublisher extends AbstractVerticle {

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

        // This is a POST API. Call this API "/publish?topic=abc"
        Route postRoute = router.route(HttpMethod.POST, config().getString("publish_url"));
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
                    String inputMessage = jsonRequest.getString("message");
                    String topic = jsonRequest.getString("topic");
                    MemoryPersistence persistence = new MemoryPersistence();

                    try {
                        MqttClient client = new MqttClient(config().getString("mqtt_broker"), config().getString("mqtt_clientId"), persistence);
                        client.connect();
                        MqttMessage message = new MqttMessage();
                        message.setPayload(inputMessage.getBytes());
                        client.publish(topic, message);
                        request.response().end(createResponse("SUCCESS"));
                    } catch(MqttException e) {
                        System.out.println("reason "+e.getReasonCode());
                        System.out.println("msg "+e.getMessage());
                        System.out.println("loc "+e.getLocalizedMessage());
                        System.out.println("cause "+e.getCause());
                        System.out.println("excep "+e);
                        e.printStackTrace();
                    }
                }
            });
            logger.info("Done with Publish");
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
