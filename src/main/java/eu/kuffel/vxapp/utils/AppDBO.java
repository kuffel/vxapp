package eu.kuffel.vxapp.utils;

import eu.kuffel.vxapp.Application;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

/**
 * Extend this class to add mongo database CRUD methods to your simple POJO.
 * This abstract class provides all methods to persist your objects as json to
 * a mongo database instance. Just extends it and implement all required methods.
 *
 * @author akuffel
 * @param <T> Simple POJO to save in a mongo db instance.
 * @version 0.9.0
 */
public abstract class AppDBO<T> {

    /**
     * Get the mongo database collection name as string.
     * This name should be unique and will be used to create the collection.
     * @return String collection name
     */
    public abstract String getCollectionName();

    /**
     * String represantion of the mongo db id.
     * Just add an id variable to your class and use this method as a getter.
     * @return String id or null if object is unsaved.
     */
    public abstract String getId();

    /**
     * Set the id
     * @param id String id
     * @return T Returns itself for to allow a fluent usage.
     */
    public abstract T setId( String id );

    /**
     * Get a json representation of this class.
     * @return JsonObject object as json object
     */
    public abstract JsonObject getJSON();

    /**
     * Get a json representation of this class and exclude the specified fields.
     * @param excludeFields Fields to exclude, non existing fields will be ignored.
     * @return JsonObject object as json object
     */
    public JsonObject getJSON( String ... excludeFields ){
        JsonObject full = this.getJSON();
        JsonObject filtered = new JsonObject(full.encode());
        Set<String> fieldNames = full.fieldNames();
        for(String i : fieldNames){
            for(String e : excludeFields){
                if(i.equalsIgnoreCase(e)){
                    filtered.remove(i);
                }
            }
        }
        return filtered;
    }

    /**
     * Set all parameters from json on this instance.
     * @param json JsonObject with values to set.
     * @param patch Change only field that are in json.
     * @return T Returns itself for to allow a fluent usage.
     */
    public abstract T setJSON( JsonObject json, boolean patch );

    /**
     * Create a new instance from json and return it.
     * @param json JsonObject to extract values from.
     * @return T Returns a new instance with the data from the given json.
     */
    public abstract T createFromJson( JsonObject json );


    /**
     * Override this method to apply custom validations.
     * To return errors add JsonObjects with the following format.
     * { fieldname : errormessage }
     *
     * @param callback
     */
    public void validate( Handler<JsonArray> callback ){
        JsonArray errors = new JsonArray();
        callback.handle(errors);
    }


    /**
     * Count saved objects by Query and call Handler with the number.
     * @param query JsonObject with Query, e.g. new JsonObject().put("size",169);
     * @param callback Callback handler
     */
    public void count( JsonObject query, Handler<Long> callback ){
        Objects.nonNull(query);
        Objects.nonNull(callback);
        Application.database.count(getCollectionName(), query, (AsyncResult<Long> countResult) -> {
            if(countResult.succeeded()){
                callback.handle(countResult.result());
            }else{
                callback.handle(0L);
            }
        });
    }

    /**
     * Delete this object without a callback.
     */
    public void delete(){
        this.delete(null);
    }

    /**
     * Delete this object and invoke the specified handler.
     * A successful delete operation returns true.
     * @param callback Simple Handler with a boolean result.
     */
    public void delete( Handler<Boolean> callback ){
        this.removeOne(this.getId(), (removeOneResult) -> {
            if(callback != null){
                callback.handle(removeOneResult);
            }
        });
    }


    /**
     * Save this object to database, if no id was set a new id will be assigned.
     */
    public void save(){
        this.save(null);
    }

    /**
     * Save this object to database and callback the specified handler, if no id was set a new id will be assigned.
     * @param callback Callback with the updated/saved object, or null if something went very wrong.
     */
    @SuppressWarnings("unchecked")
    public void save( Handler<T> callback ){
        Application.database.save(getCollectionName(), this.getJSON(), (saveResult) -> {
            if(saveResult.succeeded()){
                if(this.getId() == null){
                    this.setId(saveResult.result());
                }
                if(callback != null){
                    callback.handle((T) this);
                }
            }else{
                if(callback != null){
                    System.out.println(saveResult.cause().getMessage());
                    callback.handle(null);
                }
            }
        });
    }

    /**
     * Find all documents that match the specified query.
     * You can construct the query with the standard mongo filter functions.
     * http://mongodb.github.io/mongo-java-driver/3.3/builders/filters/
     * e.g.: Bson b = Filters.and(Filters.gt("points", 30),Filters.lt("points", 60));
     * @param query Bson representation of your filter.
     * @param callback Handler with documents or an empty list if nothing matches.
     */
    public void find( Bson query, Handler<List<T>> callback ){
        Objects.nonNull(query);
        Objects.nonNull(callback);
        JsonObject queryJson = null;
        if(query != null){
            BsonDocument bsonDocument = query.toBsonDocument(BsonDocument.class, com.mongodb.MongoClient.getDefaultCodecRegistry());
            queryJson = new JsonObject(bsonDocument.toJson());
        }
        find(queryJson, callback);
    }

    /**
     * Find all documents that match the specified query.
     * @param query Json representation of your filter.
     * @param callback Handler with documents or an empty list if nothing matches.
     */
    public void find( JsonObject query, Handler<List<T>> callback ){
        Objects.nonNull(query);
        Objects.nonNull(callback);
        Application.database.find(getCollectionName(), query, (findResult) -> {
            if(findResult.succeeded()){
                List<T> retList = new ArrayList<>();
                List<JsonObject> results = findResult.result();
                for( JsonObject i : results){
                    retList.add(this.createFromJson(i));
                }
                callback.handle(retList);
            }else{
                List<T> retList = new ArrayList<>();
                callback.handle(retList);
            }
        });
    }

    /**
     * Find the document with the specified id.
     * @param id ObjectID as String
     * @param callback Handler with found document as callback, or null if id doesnt exist.
     */
    @SuppressWarnings("unchecked")
    public void findOne( String id, Handler<T> callback ){
        Objects.nonNull(id);
        Objects.nonNull(callback);
        JsonObject query = new JsonObject();
        query.put("_id", id);
        Application.database.findOne(getCollectionName(), query, null, (findOneResult) -> {
            if(findOneResult.succeeded()){
                if(findOneResult.result() != null){
                    this.setJSON(findOneResult.result(),false);
                    callback.handle((T) this);
                }else{
                    callback.handle(null);
                }
            }else{
                callback.handle(null);
            }
        });
    }

    /**
     * Find one document that has the specified value in the specified field.
     * @param field Fieldname
     * @param value Value
     * @param callback Handler with found document as callback, or null if id doesnt exist.
     */
    @SuppressWarnings("unchecked")
    public void findOneByFieldValue( String field, Object value, Handler<T> callback ){
        Objects.nonNull(field);
        Objects.nonNull(callback);
        JsonObject query = new JsonObject();
        query.put(field, value);
        Application.database.findOne(getCollectionName(), query, null, (findOneResult) -> {
            if(findOneResult.succeeded()){
                if(findOneResult.result() != null){
                    this.setJSON(findOneResult.result(),false);
                    callback.handle((T) this);
                }else{
                    callback.handle(null);
                }
            }else{
                // Error, log something.....
                callback.handle(null);
            }
        });
    }

    /**
     * Find multiple documents that have the specified value in the specified field.
     * @param field Fieldname
     * @param value Value
     * @param callback Handler with documents or an empty list if nothing matches.
     */
    public void findByFieldValue( String field, Object value, Handler<List<T>> callback ){
        Objects.nonNull(field);
        Objects.nonNull(callback);
        JsonObject query = new JsonObject();
        query.put(field, value);
        Application.database.find(getCollectionName(),query, findResults -> {
            if(findResults.succeeded()){
                List<T> retList = new ArrayList<>();
                List<JsonObject> results = findResults.result();
                for( JsonObject i : results){
                    retList.add(this.createFromJson(i));
                }
                callback.handle(retList);
            }else{
                List<T> retList = new ArrayList<>();
                callback.handle(retList);
            }
        });
    }

    /**
     * Find documents that match the specified query.
     * @param query BSON Query
     * @param options FindOptions
     * @param callback Handler with documents or an empty list if nothing matches.
     */
    public void findWithOptions( Bson query, FindOptions options, Handler<List<T>> callback ){
        Objects.nonNull(query);
        Objects.nonNull(callback);
        JsonObject queryJson = null;
        if(query != null){
            BsonDocument bsonDocument = query.toBsonDocument(BsonDocument.class, com.mongodb.MongoClient.getDefaultCodecRegistry());
            queryJson = new JsonObject(bsonDocument.toJson());
        }
        findWithOptions(queryJson,options, callback);
    }


    /**
     * Find documents that match the specified query.
     * @param query JSON Query
     * @param options FindOptions
     * @param callback Handler with documents or an empty list if nothing matches.
     */
    public void findWithOptions( JsonObject query, FindOptions options, Handler<List<T>> callback ){
        Objects.nonNull(query);
        Objects.nonNull(callback);
        Application.database.findWithOptions(getCollectionName(), query, options, (findWithOptionsResult) -> {
            if(findWithOptionsResult.succeeded()){
                List<T> retList = new ArrayList<>();
                List<JsonObject> results = findWithOptionsResult.result();
                for( JsonObject i : results){
                    retList.add(this.createFromJson(i));
                }
                callback.handle(retList);
            }else{
                List<T> retList = new ArrayList<>();
                callback.handle(retList);
            }
        });
    }


    /**
     * Delete documents that match the specified query.
     * @param query BSON query
     */
    public void remove( Bson query ){
        remove(query, null);
    }

    /**
     * Delete documents that match the specified query.
     * @param query JSON query
     */
    public void remove( JsonObject query ){
        remove(query, null);
    }


    /**
     * Delete documents that match the specified query.
     * @param query BSON query
     * @param callback Handler with count of removed items or null on errors.
     */
    public void remove( Bson query, Handler<Long> callback ){
        Objects.nonNull(query);
        JsonObject queryJson = null;
        if(query != null){
            BsonDocument bsonDocument = query.toBsonDocument(BsonDocument.class, com.mongodb.MongoClient.getDefaultCodecRegistry());
            queryJson = new JsonObject(bsonDocument.toJson());
        }
        remove(queryJson, callback);
    }

    /**
     * Delete documents that match the specified query.
     * @param query JSON query
     * @param callback Handler with count of removed items or null on errors.
     */
    public void remove( JsonObject query, Handler<Long> callback ){
        Objects.nonNull(query);
        Application.database.removeDocuments(getCollectionName(),query, deleteResult -> {
            if(callback != null){
                if(deleteResult.succeeded()){
                    callback.handle(deleteResult.result().getRemovedCount());
                }else{
                    callback.handle(null);
                }
            }
        });
    }

    /**
     * Delete the document with the specified id.
     * @param id ObjectID to delete
     */
    public void removeOne( String id ){
        removeOne(id,null);
    }

    /**
     * Delete the document with the specified id.
     * @param id ObjectID to delete
     * @param callback Handler with True if something was deleted.
     */
    public void removeOne( String id, Handler<Boolean> callback ){
        Objects.nonNull(id);
        JsonObject query = new JsonObject();
        query.put("_id", id);
        Application.database.removeDocument(getCollectionName(), query, (deleteResult) -> {
            if(callback != null) {
                if (deleteResult.succeeded()) {
                    callback.handle(deleteResult.result().getRemovedCount() > 0);
                } else {
                    callback.handle(false);
                }
            }
        });
    }



}