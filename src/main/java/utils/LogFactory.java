package utils;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * Created by akshay.kumar1 on 17/10/16.
 */
public final class LogFactory {

    public static Logger getLogger(Class clazz) {
        Logger logger = null;
        try {
            final InputStream inputStream = clazz.getResourceAsStream("/logging.properties");
            LogManager.getLogManager().readConfiguration(inputStream);
            logger = (Logger) LoggerFactory.getLogger(clazz.getName());
            logger.info("LogFactory initialized");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return logger;
    }
}
