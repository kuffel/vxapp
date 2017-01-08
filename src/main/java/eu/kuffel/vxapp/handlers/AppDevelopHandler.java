package eu.kuffel.vxapp.handlers;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by adam on 08.01.17.
 */
public class AppDevelopHandler implements Handler<RoutingContext> {

    private static AppDevelopHandler instance;

    private AppDevelopHandler(){}

    public static AppDevelopHandler create(){
        if(instance == null){
            instance = new AppDevelopHandler();
        }
        return instance;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();
        JsonObject answer = new JsonObject();
        answer.put("version", request.version());
        answer.put("method", request.method());
        answer.put("remoteAddress", request.remoteAddress().toString());
        answer.put("absoluteURI", request.absoluteURI());
        answer.put("uri", request.uri());
        answer.put("path", request.path());
        answer.put("query", request.query());


        JsonObject requestHeadersObj = new JsonObject();
        List<Map.Entry<String,String>> requestHeaders = request.headers().entries();
        for(Map.Entry<String, String> rq : requestHeaders){
            requestHeadersObj.put(rq.getKey(), rq.getValue());
        }
        answer.put("headers", requestHeadersObj);

        JsonObject cookiesJSON = new JsonObject();
        Set<Cookie> cookies = routingContext.cookies();
        for(Cookie c : cookies){
            //JsonObject cookieJSON = new JsonObject();
            cookiesJSON.put(c.getName(), c.getValue());
        }
        answer.put("cookies", cookiesJSON);


        JsonObject requestParamsObj = new JsonObject();
        List<Map.Entry<String,String>> requestParams = request.params().entries();
        for(Map.Entry<String, String> rp : requestParams){
            requestParamsObj.put(rp.getKey(), rp.getValue());
        }
        answer.put("params", requestParamsObj);



        JsonObject fileUploadsJSON = new JsonObject();
        Set<FileUpload> uploads = routingContext.fileUploads();
        for(FileUpload f : uploads){
            JsonObject file = new JsonObject();
            file.put("name", f.fileName());
            file.put("size", f.size());
            file.put("contentType", f.contentType());
            file.put("uploadedFileName", f.uploadedFileName());
            fileUploadsJSON.put(f.name(), file);
        }
        answer.put("uploads", fileUploadsJSON);


        Buffer bodyBuffer = routingContext.getBody();
        answer.put("bodyBuffer", bodyBuffer.toString("UTF-8"));

        try {
            JsonObject bodyJSON = routingContext.getBodyAsJson();
            answer.put("bodyJSON", bodyJSON);
        }catch(DecodeException decodeEx){
            answer.put("bodyJSON", "Invalid JSON-Body");
        }


        // http://vertx.io/docs/vertx-web/java/#_context_data
        JsonObject contextDataJSON = new JsonObject();
        Set<Map.Entry<String, Object>> contextData = routingContext.data().entrySet();
        for( Map.Entry<String, Object> i : contextData ){
            contextDataJSON.put(i.getKey(), String.valueOf(i.getValue()));
        }
        answer.put("contextData", contextDataJSON);

        //answer.put("config", config);

        /*
        ResourceBundle bundle = ResourceBundle.getBundle("webroot");
        System.out.println(bundle);
        */

        /*
        JsonObject systemPropertiesJSON = new JsonObject();
        Properties props = System.getProperties();
        Enumeration e = props.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            systemPropertiesJSON.put(key, props.getProperty(key));
        }
        answer.put("systemProperties", systemPropertiesJSON);
        */


        response.write(answer.encode());
        routingContext.response().end();
    }


}
