import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * Created by akshay.kumar1 on 06/07/16.
 */
public class MQTTSubscriber extends AbstractVerticle implements MqttCallback {

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

        // This is a GET API. Call this API "/subscribe?topic=abc"
        Route getRoute = router.route(HttpMethod.GET, config().getString("subscribe_url"));
        getRoute.handler(routingContext -> {
            HttpServerRequest request = routingContext.request();
            String topic = request.params().get("topic");
            MemoryPersistence persistence = new MemoryPersistence();

            try {
                MqttClient client = new MqttClient(config().getString("mqtt_broker"), config().getString("mqtt_clientId"), persistence);
                client.connect();
                client.setCallback(this);
                client.subscribe(topic);
            } catch(MqttException e) {
                System.out.println("reason "+e.getReasonCode());
                System.out.println("msg "+e.getMessage());
                System.out.println("loc "+e.getLocalizedMessage());
                System.out.println("cause "+e.getCause());
                System.out.println("excep "+e);
                e.printStackTrace();
            }
            logger.info("Done with Subscribe");
        });

        server.requestHandler(router::accept).listen(config().getInteger("server_port"));

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        logger.info("MQTT message : "+message);
    }

    @Override
    public void connectionLost(Throwable cause) {
        logger.error("Connection lost");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.info("Delivery complete");
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        // TODO Auto-generated method stub
        super.stop(stopFuture);
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
