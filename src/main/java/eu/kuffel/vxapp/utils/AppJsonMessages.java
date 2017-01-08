package eu.kuffel.vxapp.utils;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;

/**
 * Created by akuffel on 22.12.16.
 */
public class AppJsonMessages {

    public static final int NOT_IMPLEMENTED = 5010;

    public static final int SERVER_ERROR = 5000;

    public static final int RATE_LIMIT_EXCEEDED = 4290;

    public static final int NOT_FOUND = 4040;

    public static final int INVALID_CREDENTIALS = 4033;
    public static final int INVALID_CLIENT_KEY = 4032;
    public static final int INVALID_API_KEY = 4031;
    public static final int ACCESS_DENIED = 4030;

    public static final int CLIENT_ERROR = 4000;
    public static final int INVALID_JSON = 4001;
    public static final int INVALID_JSON_TYPES = 4002;
    public static final int INVALID_JSON_DATEFORMAT = 4003;
    public static final int PATCH_FAILED = 4004;

    public static final int USER_CREATED = 2011;


    public static final int OK = 2000;
    public static final int USER_LOGGED_IN = 2001;
    public static final int USER_LOGGED_OUT = 2002;
    public static final int USER_DELETED = 2003;
    public static final int CLIENT_DELETED = 2004;
    public static final int RESSOURCE_DELETED = 2005;



    public static JsonObject getMessage(int messageCode, JsonObject info ){
        JsonObject messageJson;
        switch(messageCode){
            case NOT_IMPLEMENTED:
                messageJson = createMessageJson(501, "Not implemented", "This endpoint is not implemented yet", info);
                break;
            case SERVER_ERROR:
                messageJson = createMessageJson(500, "Internal server error", "Something went terribly wrong, try again later.", info );
                break;
            case RATE_LIMIT_EXCEEDED:
                messageJson = createMessageJson(429, "Rate limit exceeded", "Please try again in a few seconds.", info);
                break;
            case NOT_FOUND:
                messageJson = createMessageJson(404, "Not found", "The specified ressource is not existing.", info);
                break;
            case INVALID_CLIENT_KEY:
                messageJson = createMessageJson(403, "Invalid client key", "Your http x-api-client-key header is missing or invalid.", info);
                break;
            case INVALID_API_KEY:
                messageJson = createMessageJson(403, "Invalid api key", "Your http x-api-key header is missing or invalid.", info);
                break;
            case INVALID_CREDENTIALS:
                messageJson = createMessageJson(403, "Invalid credentials", "Login failed, your credentials are invalid.", info);
                break;
            case ACCESS_DENIED:
                messageJson = createMessageJson(403, "Access denied", "You are not allowed to access this resource.", info);
                break;
            case INVALID_JSON:
                messageJson = createMessageJson(400, "Invalid json", "Your request contains invalid json.", info);
                break;
            case INVALID_JSON_TYPES:
                messageJson = createMessageJson(400, "Invalid json type", "One or more parameters have the wrong datatype.", info);
                break;
            case INVALID_JSON_DATEFORMAT:
                messageJson = createMessageJson(400, "Invalid json dateformat", "Could not parse date, please use ISO8601 dateformat.", info);
                break;
            case PATCH_FAILED:
                messageJson = createMessageJson(400, "Patch failed", "See errors for more information.", info);
                break;
            case CLIENT_ERROR:
                messageJson = createMessageJson(400, "Invalid request", "Your request is invalid, see errors for more information.", info);
                break;
            case USER_CREATED:
                messageJson = createMessageJson(201, "Signup successful", "Your account has been created successfully.", info);
                break;
            case USER_LOGGED_IN:
                messageJson = createMessageJson(200, "User logged in", "User logged in successfully", info);
                break;
            case USER_LOGGED_OUT:
                messageJson = createMessageJson(200, "User logged out", "User logged out successfully", info);
                break;
            case USER_DELETED:
                messageJson = createMessageJson(200, "User deleted", "User logged out and deleted successfully", info);
                break;
            case CLIENT_DELETED:
                messageJson = createMessageJson(200, "Client deleted", "Your client was removed and is invalid. Obtain a new client key for further requests.", info);
                break;
            case RESSOURCE_DELETED:
                messageJson = createMessageJson(200, "Ressource deleted", "Ressource deleted successfully.", info);
                break;
            case OK:
                messageJson = createMessageJson(200, "OK", "Everything is ok, dont worry.", info);
                break;
            default:
                messageJson = createMessageJson( 501, "Error not implemented", "This is an unknown error message.", info );
                break;
        }
        return messageJson;
    }

    public static JsonObject getMessage( int messageCode ){
        return getMessage(messageCode, null);
    }

    public static JsonObject createMessageJson( int httpStatus, String message, String description, JsonObject info ){
        JsonObject errorJson = new JsonObject();
        errorJson.put("status", httpStatus);
        errorJson.put("message", message);
        errorJson.put("description", description);
        if(info != null){
            errorJson.mergeIn(info);
        }
        return errorJson;
    }

    public static void sendMessage(RoutingContext rc, int errorCode ){
        sendMessage(rc, errorCode, null);
    }

    public static void sendMessage( RoutingContext rc, int messageCode, JsonObject info ){
        Objects.requireNonNull(rc);
        JsonObject errorData = getMessage(messageCode, info);
        rc.response().setStatusCode(errorData.getInteger("status", 501)).end(errorData.encode());
    }

}