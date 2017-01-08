package eu.kuffel.vxapp.handlers;

import eu.kuffel.vxapp.models.AppClient;
import eu.kuffel.vxapp.models.AppUser;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.time.Instant;

/**
 * This handler checks if the clients userid is set and loads the user object into the current RoutingContext.
 *
 * @author akuffel
 * @version 1.0.0
 */
public class AppUserHandler implements Handler<RoutingContext> {

    private static AppUserHandler instance;

    private AppUserHandler(){}

    public static AppUserHandler create(){
        if(instance == null){
            instance = new AppUserHandler();
        }
        return instance;
    }


    @Override
    public void handle(RoutingContext routingContext) {
        AppClient client = (AppClient) routingContext.data().get("client");
        if(client != null){
            AppUser.findById(client.getUserId(), clientUser -> {
                if(clientUser != null){
                    clientUser.setLastactive(Instant.now());
                    clientUser.save(userUpdated -> {
                        routingContext.data().put("user",clientUser);
                        routingContext.next();
                    });
                }else{
                    routingContext.data().remove("user");
                    routingContext.next();
                }
            });
        }else{
            routingContext.next();
        }
    }

}
