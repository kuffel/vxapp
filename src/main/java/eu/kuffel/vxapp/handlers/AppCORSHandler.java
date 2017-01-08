package eu.kuffel.vxapp.handlers;


import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
* This handler allows jQuery CORS Preflight Requests.
* Implements io.vertx.core.Handler and can be used on any router.
*
* @author akuffel
* @version 1.0.0
*/
public class AppCORSHandler implements Handler<RoutingContext> {

    private static AppCORSHandler instance;


    private AppCORSHandler(){}

    public static AppCORSHandler create(){
        if(instance == null){
            instance = new AppCORSHandler();
        }
        return instance;
    }


    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();
        if(request.method().equals(HttpMethod.OPTIONS)){
            String allowedMethods = "GET,PUT,POST,PATCH,DELETE,HEAD,OPTIONS";
            response.headers().add("Content-Type", "application/json");
            response.headers().add("Allow", "GET,PUT,POST,PATCH,DELETE,HEAD,OPTIONS");
            JsonObject answer = new JsonObject();
            answer.put("Allow", allowedMethods);
            response.setStatusCode(200).end(answer.encode());
        }else{
            routingContext.next();
        }
    }




}