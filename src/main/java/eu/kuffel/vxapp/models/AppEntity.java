package eu.kuffel.vxapp.models;

import eu.kuffel.vxapp.utils.AppDBO;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by adam on 08.01.17.
 */
public class AppEntity extends AppDBO<AppEntity> {

private String id;
private String type;
private String title;
private String body;
private List<String> tags;
private JsonObject nested;
private String author;
private byte[] data;
private int version;
private boolean validated;
private Instant created;
private Instant updated;
private Instant deleted;

public AppEntity() {
        tags = new ArrayList<>();
        }

@Override
public String getCollectionName() {
        return "entity";
        }

@Override
public String getId() {
        return this.id;
        }

@Override
public AppEntity setId(String id) {
        this.id = id;
        return this;
        }

@Override
public JsonObject getJSON() {
        JsonObject ret = new JsonObject();
        if (this.id != null) {
        ret.put("_id", this.id);
        }
        ret.put("type", this.type);
        ret.put("title", this.title);
        ret.put("body", this.body);
        JsonArray tagsJson = new JsonArray();
        for(String t : tags){
        tagsJson.add(t);
        }
        ret.put("tags", tagsJson);
        ret.put("nested",nested);
        ret.put("author",author);
        ret.put("data", this.data);
        ret.put("version", this.version);
        ret.put("validated", this.validated);
        ret.put("created", this.created);
        ret.put("updated", this.updated);
        ret.put("deleted", this.deleted);
        return ret;
        }

@Override
public AppEntity setJSON(JsonObject json, boolean patch) {
        // TODO: Patch JSON
        if (json != null) {

        this.id = json.getString("_id", null);

        if(patch){
        if(json.containsKey("type")){
        this.type = json.getString("type", null);
        }
        if(json.containsKey("title")){
        this.title = json.getString("title", null);
        }
        if(json.containsKey("body")){
        this.body = json.getString("body", null);
        }
        if(json.containsKey("tags")){
        JsonArray tagsJson = json.getJsonArray("tags",null);
        tags = new ArrayList<>();
        if(tagsJson != null){
        for(int i = 0; i < tagsJson.size(); i++){
        tags.add(tagsJson.getString(i));
        }
        }
        }
        if(json.containsKey("nested")){
        this.nested = json.getJsonObject("nested", null);
        }
        if(json.containsKey("data")){
        this.data = json.getBinary("data");
        }
        if(json.containsKey("version")){
        this.version = json.getInteger("version",0);
        }
        if(json.containsKey("validated")){
        this.validated = json.getBoolean("validated",false);
        }
        if(json.containsKey("created")){
        this.created = json.getInstant("created",null);
        }
        if(json.containsKey("updated")){
        this.updated = json.getInstant("updated",null);
        }
        if(json.containsKey("created")){
        this.created = json.getInstant("created",null);
        }
        }else{
        this.type = json.getString("type", null);
        this.title = json.getString("title", null);
        this.body = json.getString("body", null);
        JsonArray tagsJson = json.getJsonArray("tags",null);
        this.tags = new ArrayList<>();
        if(tagsJson != null){
        for(int i = 0; i < tagsJson.size(); i++){
        tags.add(tagsJson.getString(i));
        }
        }
        this.nested = json.getJsonObject("nested",null);
        this.author = json.getString("author", null);
        this.data = json.getBinary("data");
        this.version = json.getInteger("version", 0);
        this.validated = json.getBoolean("validated", false);
        this.created = json.getInstant("created", null);
        this.updated = json.getInstant("updated", null);
        this.deleted = json.getInstant("deleted", null);
        }
        }
        return this;
        }

@Override
public AppEntity createFromJson(JsonObject json) {
        AppEntity n = new AppEntity();
        n.setJSON(json,false);
        return n;
        }

@Override
public String toString() {
        return this.getJSON().encode();
        }


@Override
public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppEntity appEntity = (AppEntity) o;

        if (version != appEntity.version) return false;
        if (validated != appEntity.validated) return false;
        if (id != null ? !id.equals(appEntity.id) : appEntity.id != null) return false;
        if (type != null ? !type.equals(appEntity.type) : appEntity.type != null) return false;
        if (title != null ? !title.equals(appEntity.title) : appEntity.title != null) return false;
        if (body != null ? !body.equals(appEntity.body) : appEntity.body != null) return false;
        if (tags != null ? !tags.equals(appEntity.tags) : appEntity.tags != null) return false;
        if (nested != null ? !nested.equals(appEntity.nested) : appEntity.nested != null) return false;
        if (!Arrays.equals(data, appEntity.data)) return false;
        if (created != null ? !created.equals(appEntity.created) : appEntity.created != null) return false;
        if (updated != null ? !updated.equals(appEntity.updated) : appEntity.updated != null) return false;
        return deleted != null ? deleted.equals(appEntity.deleted) : appEntity.deleted == null;
        }

@Override
public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + (nested != null ? nested.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(data);
        result = 31 * result + version;
        result = 31 * result + (validated ? 1 : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        result = 31 * result + (updated != null ? updated.hashCode() : 0);
        result = 31 * result + (deleted != null ? deleted.hashCode() : 0);
        return result;
        }

public String getType() {
        return type;
        }

public void setType(String type) {
        this.type = type;
        }

public String getTitle() {
        return title;
        }

public void setTitle(String title) {
        this.title = title;
        }

public String getBody() {
        return body;
        }

public void setBody(String body) {
        this.body = body;
        }

public List<String> getTags() {
        return tags;
        }

public void setTags(List<String> tags) {
        this.tags = tags;
        }

public JsonObject getNested() {
        return nested;
        }

public void setNested(JsonObject nested) {
        this.nested = nested;
        }

public String getAuthor() {
        return author;
        }

public void setAuthor(String author) {
        this.author = author;
        }

public byte[] getData() {
        return data;
        }

public void setData(byte[] data) {
        this.data = data;
        }

public int getVersion() {
        return version;
        }

public void setVersion(int version) {
        this.version = version;
        }

public boolean isValidated() {
        return validated;
        }

public void setValidated(boolean validated) {
        this.validated = validated;
        }

public Instant getCreated() {
        return created;
        }

public void setCreated(Instant created) {
        this.created = created;
        }

public Instant getUpdated() {
        return updated;
        }

public void setUpdated(Instant updated) {
        this.updated = updated;
        }

public Instant getDeleted() {
        return deleted;
        }

public void setDeleted(Instant deleted) {
        this.deleted = deleted;
        }







        }
