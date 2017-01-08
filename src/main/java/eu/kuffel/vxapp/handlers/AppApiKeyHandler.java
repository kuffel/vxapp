package eu.kuffel.vxapp.handlers;

import eu.kuffel.vxapp.utils.AppJsonMessages;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

/**
 * Checks if a valid api-key header is provided with requests.
 * Sends an error if no valid api-key is available.
 *
 * @author akuffel
 * @version 1.0.0
 */
public class AppApiKeyHandler implements Handler<RoutingContext> {

    private String apikey;
    private static final String HEADER_NAME = "x-api-key";

    private static AppApiKeyHandler instance;

    private AppApiKeyHandler(){}

    public static AppApiKeyHandler create( String apikey ){
        if(instance == null){
            instance = new AppApiKeyHandler();
        }
        instance.apikey = apikey;
        return instance;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        if(apikey == null){
            routingContext.next();
        }
        HttpServerRequest request = routingContext.request();
        String xApiKeyHeader = request.headers().get(HEADER_NAME);
        if( xApiKeyHeader != null && xApiKeyHeader.equals(apikey) ){
            routingContext.next();
        } else {
            AppJsonMessages.sendMessage(routingContext, AppJsonMessages.INVALID_API_KEY);
        }


    }
}