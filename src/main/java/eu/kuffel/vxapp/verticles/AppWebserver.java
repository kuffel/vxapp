package eu.kuffel.vxapp.verticles;

import com.mongodb.client.model.Filters;
import eu.kuffel.vxapp.handlers.*;
import eu.kuffel.vxapp.routers.AppClientRouter;
import eu.kuffel.vxapp.routers.AppEntityRouter;
import eu.kuffel.vxapp.routers.AppUserRouter;
import eu.kuffel.vxapp.utils.AppJsonMessages;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import org.bson.conversions.Bson;

import java.sql.Date;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by adam on 08.01.17.
 */
public class AppWebserver extends AbstractVerticle {

    private static final int DEFAULT_PORT = 8888;

    private HttpServer server;
    private HttpServerOptions options;
    private Router router;


    @Override
    public void start() throws Exception {


        // Read config and prepare HttpServerOptions
        JsonObject webserverConfig = config().getJsonObject("webserver", null);
        int port = DEFAULT_PORT;
        if(webserverConfig != null){
            port = webserverConfig.getInteger("port", DEFAULT_PORT);
        }
        options = new HttpServerOptions();
        options.setPort(port);

        // Add default http headers
        HashMap<String,String> httpHeadersConfig = new HashMap<>();
        if(webserverConfig != null){
            JsonObject httpHeaders = webserverConfig.getJsonObject("headers", new JsonObject());
            Set<String> fieldNames = httpHeaders.fieldNames();
            for(String f : fieldNames){
                httpHeadersConfig.put(f, httpHeaders.getString(f));
            }
        }

        server = vertx.createHttpServer(options);
        router = Router.router(vertx);

        // Configure and add default middleware handlers.
        int postlimit = webserverConfig.getInteger("postlimit",0);
        if(postlimit > 0){
            router.route().handler(BodyHandler.create().setBodyLimit(postlimit));
        }else{
            router.route().handler(BodyHandler.create());
        }
        router.route().handler(CookieHandler.create());
        int timeout = webserverConfig.getInteger("timeout",0);
        if(timeout > 0){
            router.route().handler(TimeoutHandler.create(timeout));
        }

        router.route().handler(ResponseTimeHandler.create());

        boolean logging = webserverConfig.getBoolean("logging",false);
        if(logging){
            router.route().handler(LoggerHandler.create());
        }


        // Add default HTTP headers
        router.route("/api/*").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.setChunked(true);
            response.headers().add(HttpHeaders.CONTENT_TYPE, "application/json");
            response.headers().addAll(httpHeadersConfig);
            routingContext.next();
        });

        // Allow jQuery CORS preflight requests
        router.route("/api/*").handler(AppCORSHandler.create());

        // Require x-api-key for all routers
        router.route("/api/*").handler(AppApiKeyHandler.create(webserverConfig.getString("apikey",null)));

        // Require x-api-client-key for all routers
        router.route("/api/*").handler(AppClientKeyHandler.create());

        // Loads the current user into the context (if client is authenticated)
        router.route("/api/*").handler(AppUserHandler.create());

        // Watches requests rates and limits them if a client exceeds them.
        router.route("/api/*").handler(AppRateLimitHandler.create());


        // Mount subrouters....

        // /api/client
        router.mountSubRouter("/api", AppClientRouter.router(vertx));

        // /api/user
        router.mountSubRouter("/api", AppUserRouter.router(vertx));

        // /api/entities
        router.mountSubRouter("/api", AppEntityRouter.router(vertx));




        router.route("/api/debug").handler(AppDevelopHandler.create());

        router.route("/api/develop").handler( rc -> {





            /*
            AppEntity e = new AppEntity();
            e.getTags().add("A");
            e.getTags().add("B");
            e.getTags().add("C");
            e.setNested( new JsonObject().put("key1","A").put("key2","B"));
            e.save(saved->{
                //rc.response().write(saved.getJSON().encode()).end();
                new AppEntity().findOne(saved.getId(), entityFound -> {
                    if(entityFound != null){
                        rc.response().write(entityFound.getJSON().encode()).end();
                    }else{
                        AppJsonMessages.sendMessage(rc,AppJsonMessages.NOT_FOUND);
                    }
                });
            });
            */


            //rc.response().write(new JsonObject().encode()).end();









            /*
            if(Application.mailer != null){
                MailMessage email = new MailMessage();
                email.setFrom("vertx@kuffel.eu");
                email.setTo("adam@kuffel.eu");
                email.setSubject("Testmail2 von vertx");
                email.setText("Hier ist ein Test");
                email.setHtml("<h1>Hier kommt ein Test mit HTML als Inhalt</h1>");
                Application.mailer.sendMail(email, result -> {
                    if (result.succeeded()) {
                        System.out.println(result.result().getMessageID());
                    } else {
                        System.out.println(result.cause().getMessage());
                    }
                });
            }
            */


            /*
            AppEntity e = new AppEntity();
            e.setType("demo");
            e.setVersion(200);
            e.setValidated(true);
            e.setCreated(Instant.now());
            e.setUpdated(Instant.now());
            e.setDeleted(Instant.now());
            e.save(new Handler<AppEntity>() {
                @Override
                public void handle(AppEntity entitySaved) {
                    response.write(e.getJSON().encode());
                    rc.response().end();
                }
            });
            */

            /*
            AppClient c = new AppClient();
            Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
            //Bson query = Filters.and(Filters.eq("calls",0), Filters.lt("lastactive",  oneHourAgo.toString() ));
            Bson query = Filters.lt("lastactive",  oneHourAgo.toString());
            c.remove(query, handler ->{
                System.out.println("Deleted old client keys...");
            });
            */

            /*
            c.find(query, list ->{
                for(AppClient i : list){
                    System.out.println(i.getJSON().encodePrettily());
                }
            });
            */





        });


        // Handler for known exceptions.
        router.route("/api/*").failureHandler(frc -> {
            if(frc.failed()){
                if(frc.failure().getClass().equals( ClassCastException.class )) {
                    AppJsonMessages.sendMessage(frc, AppJsonMessages.INVALID_JSON_TYPES);
                    return;
                }
                if(frc.failure().getClass().equals( DateTimeParseException.class ) ){
                    AppJsonMessages.sendMessage(frc, AppJsonMessages.INVALID_JSON_DATEFORMAT);
                    return;
                }




                //AppJsonMessages.sendMessage(frc, AppJsonMessages.NOT_FOUND);
                JsonObject exception = new JsonObject().put("exception", frc.failure().getClass().getCanonicalName());
                AppJsonMessages.sendMessage(frc, AppJsonMessages.SERVER_ERROR, exception);
            }
        });

        /*
        // Default JSON Message for not found errors.
        router.route("/api/*").failureHandler(frc -> {
            AppJsonMessages.sendMessage(frc, AppJsonMessages.NOT_FOUND);
        });
        */

        router.route("/*").handler(StaticHandler.create());
        server.requestHandler(router::accept).listen(options.getPort());
    }




}