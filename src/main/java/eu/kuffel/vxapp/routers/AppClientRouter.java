package eu.kuffel.vxapp.routers;

import com.mongodb.client.model.Filters;
import eu.kuffel.vxapp.models.AppClient;
import eu.kuffel.vxapp.utils.AppJsonMessages;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.bson.conversions.Bson;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Created by adam on 08.01.17.
 */
public class AppClientRouter {

    public static final long CLIENTKEY_CLEANUP_INTERVAL_MS = 60000;
    public static final int CLIENTKEY_MAX_INACTIVITY_SECONDS = 7*24*3600; // 7 days


    private static Router router;

    public static Router router(Vertx vertx){
        router = Router.router(vertx);

        router.get("/client").handler( rc -> {
            HttpServerRequest request = rc.request();
            HttpServerResponse response = rc.response();
            String clientKey = request.getHeader(AppClient.HTTP_HEADER_NAME);
            if(clientKey == null){
                AppClient freshClient = new AppClient();
                freshClient.save((savedClient) -> {
                    if(savedClient != null){
                        response.setStatusCode(201).end(savedClient.getJSON("_id").encode());
                    }else{
                        AppJsonMessages.sendMessage(rc, AppJsonMessages.SERVER_ERROR);
                    }
                });
            }else{
                AppClient loadClient = new AppClient();
                loadClient.findOneByFieldValue("key", clientKey, (loadedClient) -> {
                    if(loadedClient != null){
                        response.setStatusCode(200).end(loadedClient.getJSON("_id").encode());
                    }else{
                        AppJsonMessages.sendMessage(rc, AppJsonMessages.INVALID_CLIENT_KEY);
                    }
                });
            }
        });


        /*
        router.patch("/client").handler(rc -> {
            AppJsonMessages.sendMessage(
                    rc.response(),
                    AppJsonMessages.NOT_IMPLEMENTED,
                    new JsonObject().put("router", "AppClientRouter")
            );

        });
        */

        router.delete("/client").handler(rc -> {
            HttpServerRequest request = rc.request();
            HttpServerResponse response = rc.response();
            String clientKey = request.getHeader(AppClient.HTTP_HEADER_NAME);
            if(clientKey != null){
                AppClient c = new AppClient();
                c.findOneByFieldValue("key", clientKey, (foundClient) -> {
                    if(foundClient != null){
                        foundClient.delete((Boolean deleteEvent) -> {
                            if(deleteEvent.booleanValue()){
                                AppJsonMessages.sendMessage(rc, AppJsonMessages.CLIENT_DELETED);
                            }else{
                                AppJsonMessages.sendMessage(rc, AppJsonMessages.INVALID_CLIENT_KEY);
                            }
                        });
                    }else{
                        AppJsonMessages.sendMessage(rc, AppJsonMessages.INVALID_CLIENT_KEY);
                    }
                });
            }else{
                AppJsonMessages.sendMessage(rc, AppJsonMessages.INVALID_CLIENT_KEY);
            }

        });



        // Cleanup old client keys in the specified interval.
        vertx.setPeriodic(CLIENTKEY_CLEANUP_INTERVAL_MS, handler ->{
            AppClient c = new AppClient();
            Instant oneHourAgo = Instant.now().minus(CLIENTKEY_MAX_INACTIVITY_SECONDS, ChronoUnit.SECONDS);
            //Bson query = Filters.and(Filters.eq("calls",0), Filters.lt("lastactive",  oneHourAgo.toString() ));
            Bson query = Filters.lt("lastactive",  oneHourAgo.toString());
            c.remove(query, cleanupDoneHandler ->{});
        });



        return router;
    }

}
