package eu.kuffel.vxapp.routers;

import com.mongodb.client.model.Filters;
import eu.kuffel.vxapp.models.AppEntity;
import eu.kuffel.vxapp.utils.AppCommons;
import eu.kuffel.vxapp.utils.AppJsonMessages;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.web.Router;
import org.bson.conversions.Bson;

import java.util.ArrayList;

/**
 * Created by adam on 08.01.17.
 */
public class AppEntityRouter {

    private static final int DEFAULT_LIMIT = 100;

    private static Router router;

    public static Router router(Vertx vertx) {
        router = Router.router(vertx);


        router.get("/entities").handler( rc -> {

            AppEntity q = new AppEntity();

            FindOptions findOptions = new FindOptions();
            int limit = DEFAULT_LIMIT;
            int page = 0;
            if(rc.request().params().get("limit") != null){
                try {
                    limit = Integer.parseInt(rc.request().params().get("limit"));
                    if(limit <= 0){
                        limit = DEFAULT_LIMIT;
                    }
                }catch(NumberFormatException ex){}
            }
            if(rc.request().params().get("page") != null) {
                try {
                    page = Integer.parseInt(rc.request().params().get("page"));
                    if (page <= 0) {
                        page = 0;
                    }
                } catch (NumberFormatException ex) {}
            }
            findOptions.setLimit(limit);
            findOptions.setSkip(limit * page);

            String sort = rc.request().params().get("sort");
            if(sort != null){
                JsonObject sortOptions = new JsonObject();
                String[] fields = sort.split(",");
                for(String f : fields){
                    String fieldName = f;
                    int fieldDirection = 1;
                    if(f.contains("+")){
                        fieldName = f.substring(f.indexOf("+")+1);
                        fieldDirection = 1;
                    }
                    if(f.contains("-")){
                        fieldName = f.substring(f.indexOf("-")+1);
                        fieldDirection = -1;
                    }
                    sortOptions.put(fieldName.trim(),fieldDirection);
                }
                findOptions.setSort(sortOptions);
            }

            JsonObject query = new JsonObject();
            q.findWithOptions(query, findOptions, list ->{
                q.count(query, countResult -> {
                    JsonObject results = new JsonObject();
                    results.put("total", countResult);
                    if(findOptions.getLimit() <= 0){
                        results.put("page", 0);
                    }else{
                        results.put("page", (int)(findOptions.getSkip() / findOptions.getLimit()));
                    }
                    results.put("limit", findOptions.getLimit());
                    JsonArray data = new JsonArray();
                    for( AppEntity e : list ){
                        data.add(e.getJSON());
                    }
                    results.put("data", data);
                    rc.response().write(results.encode()).end();
                });
            });
        }); // END GET /entities



        router.get("/entities/:id").handler(rc -> {
            AppEntity q = new AppEntity();
            String id = rc.request().params().get("id");
            q.findOne(id, found -> {
                if(found != null){
                    rc.response().write(found.getJSON().encode()).end();
                }else{
                    AppJsonMessages.sendMessage(rc, AppJsonMessages.NOT_FOUND);
                }
            });
        }); // END GET /entities/:id



        router.delete("/entities").handler(rc -> {
            AppEntity q = new AppEntity();
            JsonObject data = AppCommons.parseJsonRequest(rc, new String[]{ "id" });
            if(data != null){
                JsonArray ids = data.getJsonArray("id");
                if(ids != null && ids.size() > 0){
                    ArrayList<Bson> idFilters = new ArrayList<>();
                    for(int i = 0; i < ids.size(); i++){
                        idFilters.add( Filters.eq("_id", ids.getString(i)));
                    }
                    Bson deleteFilter = Filters.or(idFilters);
                    q.remove(deleteFilter, deletedItems -> {
                        //System.out.println(deletedItems);
                        AppJsonMessages.sendMessage(rc,AppJsonMessages.RESSOURCE_DELETED);
                    });
                }
            }
        }); // END DELETE /entities



        router.delete("/entities/:id").handler(rc -> {
            AppEntity q = new AppEntity();
            String id = rc.request().params().get("id");
            q.findOne(id, found -> {
                if(found != null){
                    found.delete(deleted -> {
                        AppJsonMessages.sendMessage(rc,AppJsonMessages.RESSOURCE_DELETED);
                    });
                }else{
                    AppJsonMessages.sendMessage(rc, AppJsonMessages.NOT_FOUND);
                }
            });
        }); // END DELETE /entities/:id


        router.post("/entities").handler(rc -> {
            JsonObject data = AppCommons.parseJsonRequest(rc);
            if(data != null){
                AppEntity e = new AppEntity();
                e.setJSON(data,false);
                e.validate( errors -> {
                    if(errors.size() == 0){
                        e.save( saved -> {
                            rc.response().write(saved.getJSON().encode()).end();
                        });
                    }else{
                        rc.response().write(errors.encode()).end();
                    }
                });
            }
        }); // END POST /entities



        router.patch("/entities/:id").handler(rc -> {
            AppEntity q = new AppEntity();
            String id = rc.request().params().get("id");
            JsonObject data = AppCommons.parseJsonRequest(rc);
            q.findOne(id, found -> {
                if(found != null){
                    found.setJSON(data,true);
                    found.setId(id);
                    found.validate( errors -> {
                        if(errors.size() == 0){
                            found.save( saved -> {
                                rc.response().write(saved.getJSON().encode()).end();
                            });
                        }else{
                            rc.response().write(errors.encode()).end();
                        }
                    });
                }else{
                    AppJsonMessages.sendMessage(rc, AppJsonMessages.NOT_FOUND);
                }
            });
        });




        return router;
    }


}
