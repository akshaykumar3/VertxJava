import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import rx.Observable;
import rx.Scheduler;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

/**
 * Created by akshay.kumar1 on 09/08/16.
 */
public class RxScheduler extends AbstractVerticle {

    private Logger logger;

    @Override
    public void start() throws Exception {

        initLogger();

        //
        Scheduler scheduler = io.vertx.rxjava.core.RxHelper.scheduler(vertx);

        // Create a periodic event stream using Vertx scheduler
        Observable<Long> o = Observable.timer(0, 1000, TimeUnit.MILLISECONDS, scheduler);

        o.subscribe(item -> {
            logger.info("Got item " + item);
        });
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
