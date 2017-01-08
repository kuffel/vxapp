package eu.kuffel.vxapp.handlers;

import eu.kuffel.vxapp.models.AppClient;
import eu.kuffel.vxapp.utils.AppJsonMessages;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.time.Instant;

/**
 * This handler checks if a valid x-api-client-key header exists.
 * It loads the client from the mongo database and increments the call counter.
 * If the provided client-key is invalid it send an error message to the client.
 *
 * @author akuffel
 * @version 1.0.0
 */
public class AppClientKeyHandler implements Handler<RoutingContext> {

    private static AppClientKeyHandler instance;

    private AppClientKeyHandler(){}

    public static AppClientKeyHandler create(){
        if(instance == null){
            instance = new AppClientKeyHandler();
        }
        return instance;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();
        String clientKey = request.getHeader(AppClient.HTTP_HEADER_NAME);
        if(request.method().equals(HttpMethod.GET) && request.path().equalsIgnoreCase("/api/client") && clientKey == null){
            // The URL to obtain an API Client key, this is ok, proceed.
            routingContext.next();
        }else{
            // Check clientkey
            if(clientKey != null){
                AppClient c = new AppClient();
                c.findOneByFieldValue("key", clientKey, loadedClient -> {
                    if(loadedClient != null){
                        //response.end(loadedClient.getJSON().encode());
                        loadedClient.setCalls( loadedClient.getCalls() + 1 );
                        loadedClient.setLastactive(Instant.now());
                        loadedClient.save((updatedClient) -> {
                            routingContext.put("client", updatedClient);
                            routingContext.next();
                        });
                    }else{
                        AppJsonMessages.sendMessage(routingContext, AppJsonMessages.INVALID_CLIENT_KEY);
                    }
                });
            }else{
                AppJsonMessages.sendMessage(routingContext, AppJsonMessages.INVALID_CLIENT_KEY);
            }
        }
    }

}