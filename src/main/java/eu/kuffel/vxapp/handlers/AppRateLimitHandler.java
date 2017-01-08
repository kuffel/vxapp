package eu.kuffel.vxapp.handlers;

import eu.kuffel.vxapp.models.AppClient;
import eu.kuffel.vxapp.utils.AppJsonMessages;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 * This handler limits the request rates of known clients.
 * If a client exceeds the limits we send a http 429 error and end the request.
 * Implements io.vertx.core.Handler and can be used on any router.
 *
 * @author akuffel
 * @version 1.0.0
 */
public class AppRateLimitHandler implements Handler<RoutingContext> {

    private static final String RATE_LIMIT_HEADER = "X-Rate-Limit";
    private static final String RATE_LIMIT_REMAINING_HEADER = "X-Rate-Limit-Remaining";
    private static final String RATE_LIMIT_RESET_HEADER = "X-Rate-Limit-Reset";


    private static final int RATE_LIMIT = 120; // Max Requests per period
    private static final int RATE_LIMIT_RESET_PERIOD = 60; // Seconds


    private static AppRateLimitHandler instance;

    private AppRateLimitHandler(){}

    public static AppRateLimitHandler create(){
        if(instance == null){
            instance = new AppRateLimitHandler();
        }
        return instance;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        AppClient client = (AppClient)routingContext.data().get("client");
        if(routingContext.request().method().equals(HttpMethod.GET) && routingContext.request().path().equalsIgnoreCase("/api/client") && client == null){
            routingContext.next();
        }
        if(client != null){

            int lastReset = (int)Duration.between(client.getCallsreset(), Instant.now()).get(ChronoUnit.SECONDS);
            int remainingCalls = RATE_LIMIT-client.getCalls();
            if(remainingCalls < 0){
                remainingCalls = 0;
            }
            int nextReset = RATE_LIMIT_RESET_PERIOD - lastReset;
            if(nextReset < 0){
                nextReset = 0;
            }
            routingContext.response().headers().add(RATE_LIMIT_HEADER, String.valueOf(RATE_LIMIT));
            routingContext.response().headers().add(RATE_LIMIT_REMAINING_HEADER, String.valueOf(remainingCalls));
            routingContext.response().headers().add(RATE_LIMIT_RESET_HEADER, String.valueOf(nextReset));

            if(lastReset > RATE_LIMIT_RESET_PERIOD){
                client.setCalls(0);
                client.setCallsreset(Instant.now());
                client.save(clientReset->{
                    routingContext.next();
                });
            } else{
                if(client.getCalls() > RATE_LIMIT){
                    AppJsonMessages.sendMessage(routingContext, AppJsonMessages.RATE_LIMIT_EXCEEDED);
                }else{
                    routingContext.next();
                }
            }

        }
    }

}