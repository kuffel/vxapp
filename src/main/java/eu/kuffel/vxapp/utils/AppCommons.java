package eu.kuffel.vxapp.utils;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.bson.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by adam on 22.12.16.
 */
public class AppCommons {


    /**
     * Helper Function to parse json request.
     * @param rc RoutingContext
     * @return JsonObject or null if decoding fails
     */
    public static JsonObject parseJsonRequest( RoutingContext rc ){
        return parseJsonRequest(rc,null);
    }

    /**
     * Helper Function to parse json request and validate the exsistence of some fieldnames.
     * This function cancels the current request and sends an error message.
     * If the request Json is invalid or the required fields are missing.
     * If everthing is okay this function returns the decoded JsonObject.
     * @param rc RoutingContext
     * @param requiredFields String array with required fieldnames.
     * @return JsonObject or null if decoding fails
     */
    public static JsonObject parseJsonRequest( RoutingContext rc, String[] requiredFields ){
        Objects.nonNull(rc);
        JsonObject data = null;
        JsonArray errors = new JsonArray();
        try {
            data = rc.getBodyAsJson();
            if(requiredFields != null){
                for(String r : requiredFields){
                    if(!data.containsKey(r)){
                        errors.add(new JsonObject().put(r, "required"));
                    }
                }
            }
            if(errors.size() > 0){
                AppJsonMessages.sendMessage(rc,AppJsonMessages.CLIENT_ERROR, new JsonObject().put("errors",errors));
                return null;
            }else{
                return data;
            }
        }catch(DecodeException decodeEx) {
            errors.add(new JsonObject().put("data", decodeEx.getMessage()));
            AppJsonMessages.sendMessage(rc, AppJsonMessages.INVALID_JSON, new JsonObject().put("errors", errors));
            return null;
        }
    }



    /**
     * Convert a simple vertx json object to an mongo db document.
     * @param jsonObject vertx json object
     * @return Document Mongo DB Document Object
     */
    public static Document convertJsonToBson(JsonObject jsonObject){
        if(jsonObject != null){
            return Document.parse(jsonObject.encode());
        }
        return null;
    }

    /**
     * Convert an mongo db document to a  vertx json object.
     * @param document Document Mongo DB Document Object
     * @return vertx json object
     */
    public static JsonObject convertBsonToJson( Document document ){
        if(document != null){
            return new JsonObject(document.toJson());
        }
        return null;
    }

    /**
     * Convert a java.util.Calendar object to an ISO8601 UTC string.
     * @param calendar Calendar object or null if we want to convert now.
     * @return ISO8601 UTC string
     */
    public static String convertCalendarToISO8601( Calendar calendar ){
        if(calendar == null){
            calendar = GregorianCalendar.getInstance();
        }
        Date date = calendar.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formatted = formatter.format(date);
        return formatted.substring(0, 22) + ":" + formatted.substring(22);
    }

    /**
     * Convert an ISO8601 UTC string to a Calendar object.
     * @param iso8601string ISO8601 UTC string
     * @return Calendar object
     */
    public static Calendar convertISO8610ToCalendar(String iso8601string ){
        Calendar calendar = GregorianCalendar.getInstance();
        String s = iso8601string.replace("Z", "+00:00");
        try {
            s = s.substring(0, 22) + s.substring(23);  // to get rid of the ":"
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = formatter.parse(s);
            calendar.setTime(date);
        }catch(IndexOutOfBoundsException e){
            //e.printStackTrace();
        } catch (ParseException e) {
            //e.printStackTrace();
        } finally {
            return calendar;
        }
    }


    /**
     * Convert an ISO8601 UTC string to an UNIX timestamp.
     * @param iso8601string ISO8601 UTC string
     * @return long UNIX timestamp
     */
    public static long convertISO8610ToTimestamp( String iso8601string ){
        if(iso8601string != null){
            Calendar calendar = convertISO8610ToCalendar(iso8601string);
            return calendar.getTimeInMillis()/1000;
        }
        return 0;
    }



}