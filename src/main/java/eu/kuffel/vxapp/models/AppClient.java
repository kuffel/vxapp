package eu.kuffel.vxapp.models;

import eu.kuffel.vxapp.utils.AppDBO;
import eu.kuffel.vxapp.utils.AppRandom;
import io.vertx.core.json.JsonObject;
import java.time.Instant;
import java.util.Objects;

/**
 *
 * @author akuffel
 */
public class AppClient extends AppDBO<AppClient> {

    private static final int KEY_LENGTH = 64;
    private static final String KEY_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    public static final String HTTP_HEADER_NAME = "x-api-client-key";

    private String id;
    private String key;
    private String userid;
    private int calls;
    private Instant callsreset;
    private Instant created;
    private Instant lastactive;


    public AppClient(){
        this.key = AppRandom.getRandomString(KEY_LENGTH, KEY_CHARS.toCharArray());
        this.calls = 0;
        this.userid = null;
        this.callsreset = Instant.now();
        this.created = Instant.now();
        this.lastactive = Instant.now();
    }


    @Override
    public String getCollectionName() {
        return "client";
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public AppClient setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject ret = new JsonObject();
        if(this.id != null){
            ret.put("_id", this.id);
        }
        ret.put("key", this.key);
        ret.put("userid", userid);
        ret.put("calls", this.calls);
        ret.put("callsreset", this.callsreset);
        ret.put("created", this.created);
        ret.put("lastactive", this.lastactive);
        return ret;
    }

    @Override
    public AppClient setJSON(JsonObject json, boolean patch) {
        if(json != null){
            this.id = json.getString("_id", null);
            this.key = json.getString("key", null);
            this.userid = json.getString("userid",null);
            this.calls = json.getInteger("calls",0);
            this.callsreset = json.getInstant("callsreset");
            this.created = json.getInstant("created");
            this.lastactive = json.getInstant("lastactive");
        }
        return this;
    }

    @Override
    public AppClient createFromJson(JsonObject json) {
        AppClient n = new AppClient();
        n.setJSON(json,false);
        return n;
    }

    public String getKey() {
        return key;
    }

    public AppClient setKey(String key) {
        this.key = key;
        return this;
    }

    public String getUserId() {
        return userid;
    }

    public AppClient setUserId(String userid) {
        this.userid = userid;
        return this;
    }

    public int getCalls() {
        return calls;
    }

    public AppClient setCalls(int calls) {
        this.calls = calls;
        return this;
    }

    public Instant getCallsreset() {
        return callsreset;
    }

    public AppClient setCallsreset(Instant callsreset) {
        this.callsreset = callsreset;
        return this;
    }

    public Instant getCreated() {
        return created;
    }

    public AppClient setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getLastactive() {
        return lastactive;
    }

    public AppClient setLastactive(Instant lastactive) {
        this.lastactive = lastactive;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + Objects.hashCode(this.id);
        hash = 13 * hash + Objects.hashCode(this.key);
        hash = 13 * hash + Objects.hashCode(this.userid);
        hash = 13 * hash + this.calls;
        hash = 13 * hash + Objects.hashCode(this.callsreset);
        hash = 13 * hash + Objects.hashCode(this.created);
        hash = 13 * hash + Objects.hashCode(this.lastactive);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AppClient other = (AppClient) obj;
        if (this.calls != other.calls) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        if (!Objects.equals(this.userid, other.userid)) {
            return false;
        }
        if (!Objects.equals(this.callsreset, other.callsreset)) {
            return false;
        }
        if (!Objects.equals(this.created, other.created)) {
            return false;
        }
        if (!Objects.equals(this.lastactive, other.lastactive)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getJSON().encode();
    }

}