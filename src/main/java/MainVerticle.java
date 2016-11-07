import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * Created by akshay.kumar1 on 16/08/16.
 */
public class MainVerticle extends AbstractVerticle {

    private static Logger logger;

    public static void main(String[] args) {

        initLogger();

        final Vertx vertx = Vertx.vertx();
//        logger.info("config = "+config().getString("get_redis_url"));
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setInstances(2);
        vertx.deployVerticle("Redis");
    }


    private static void initLogger(){
        try {
            final InputStream inputStream = MainVerticle.class.getResourceAsStream("/logging.properties");
            LogManager.getLogManager().readConfiguration(inputStream);
            logger = (Logger) LoggerFactory.getLogger(MainVerticle.class.getName());
            logger.info("LogFactory initialized");
        } catch(Exception ex) {

        }
    }
}
