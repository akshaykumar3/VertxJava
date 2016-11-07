import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * Created by akshay.kumar1 on 08/09/16.
 */
public class UI extends AbstractVerticle {

    private static HttpServer server = null;
    private static Logger logger;

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        super.start(startFuture);

        // Create a router object.
        Router router = Router.router(vertx);

        //Initialize the logger
        initLogger();

        // Enable multipart form data parsing
        router.route().handler(BodyHandler.create());

        router.route("/").handler(routingContext -> {
            routingContext.response().putHeader("content-type", "text/html").end("<html>\n" +
                    "<head>\n" +
                    "<title>Merchant Login Form </title>\n" +
                    "<!-- Include CSS File Here -->\n" +
                    "<link rel=\"stylesheet\" href=\"style.css\"/>\n" +
                    "<script src=\"http://ajax.aspnetcdn.com/ajax/jquery/jquery-1.9.0.js\"></script>\n" +
                    "<!-- Include JS File Here -->\n" +
                    "<script src=\"login.js\"></script>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<div class=\"container\">\n" +
                    "<div class=\"main\">\n" +
                    "<h2>Merchant Login Form </h2>\n" +
                    "<form id=\"form_id\" method=\"post\" name=\"myform\">\n" +
                    "<label>User Name :</label>\n" +
                    "<input type=\"text\" name=\"username\" id=\"username\"/>\n" +
                    "<label>Password :</label>\n" +
                    "<input type=\"password\" name=\"password\" id=\"password\"/>\n" +
                    "<input type=\"button\" id=\"loginButton\" value=\"Login\" onclick=\"validate();\" />\n" +
                    "</form>\n" +
                    "</div>\n" +
                    "</div>\n" +
                    "</body>\n" +
                    "</html>");
        });

        // handle the form
        router.post("/form").handler(ctx -> {
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
            // note the form attribute matches the html form element name.
            ctx.response().end("Hello " + ctx.request().getParam("name") + "!");
        });

        // Initialize the server
        server = vertx.createHttpServer();

        server.requestHandler(httpServerRequest -> {
            router.accept(httpServerRequest);
        }).listen(config().getInteger("server_port"));

    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        // TODO Auto-generated method stub
        super.stop(stopFuture);
    }

    private void initLogger(){
        try {
            final InputStream inputStream = UI.class.getResourceAsStream("/logging.properties");
            LogManager.getLogManager().readConfiguration(inputStream);
            logger = (Logger) LoggerFactory.getLogger(UI.class.getName());
            logger.info("LogFactory initialized");
        } catch(Exception ex) {

        }
    }
}
