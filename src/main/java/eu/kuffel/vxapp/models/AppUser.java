package eu.kuffel.vxapp.models;

import eu.kuffel.vxapp.utils.AppDBO;
import eu.kuffel.vxapp.utils.AppHashing;
import eu.kuffel.vxapp.utils.AppRandom;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author akuffel
 */
public class AppUser extends AppDBO<AppUser> {

    private static final int PASSWORD_ITERATIONS = 1000;
    private static final int PASSWORD_KEY_LENGTH = 4096;


    private String id;
    private String username;
    private String emailaddress;
    private byte[] password;
    private String language;
    private String timezone;
    private String verificationcode;
    private String resetcode;
    private boolean verified;
    private boolean active;
    private Instant created;
    private Instant lastactive;

    public AppUser() {
        this.verificationcode = AppRandom.getRandomStringAlphanumeric(128);
        this.created = Instant.now();
    }


    @Override
    public String getCollectionName() {
        return "user";
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public AppUser setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject ret = new JsonObject();
        if(this.id != null){
            ret.put("_id", this.id);
        }
        ret.put("username", this.username);
        ret.put("emailaddress", this.emailaddress);
        ret.put("password", this.password);
        ret.put("language", this.language);
        ret.put("timezone", this.timezone);
        ret.put("verificationcode", this.verificationcode);
        ret.put("resetcode", this.resetcode);
        ret.put("verified", this.verified);
        ret.put("active", this.active);
        ret.put("created", this.created);
        ret.put("lastactive", this.lastactive);
        return ret;
    }


    @Override
    public AppUser setJSON(JsonObject json, boolean patch) {
        if(json != null){
            this.id = json.getString("_id", null);
            this.username = json.getString("username");
            this.emailaddress = json.getString("emailaddress");
            this.password = json.getBinary("password");
            this.language = json.getString("language");
            this.timezone = json.getString("timezone");
            this.verificationcode = json.getString("verificationcode");
            this.resetcode = json.getString("resetcode");
            this.verified = json.getBoolean("verified");
            this.active = json.getBoolean("active");
            this.created = json.getInstant("created");
            this.lastactive = json.getInstant("lastactive");
        }
        return this;
    }

    @Override
    public AppUser createFromJson(JsonObject json) {
        return new AppUser().setJSON(json,false);
    }

    public String getUsername() {
        return username;
    }

    public AppUser setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getEmailaddress() {
        return emailaddress;
    }

    public AppUser setEmailaddress(String emailaddress) {
        this.emailaddress = emailaddress;
        return this;
    }

    public AppUser setPassword( String password ){
        Objects.requireNonNull(this.created);
        Objects.requireNonNull(password);
        String salt = AppHashing.getHashSHA512(String.valueOf(this.created.getEpochSecond()));
        int iterations = PASSWORD_ITERATIONS;
        int keyLength = PASSWORD_KEY_LENGTH;
        this.password = AppHashing.getHashPassword(password.toCharArray(), salt.getBytes(), iterations, keyLength);
        /*
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream( );
            bos.write(AppHashing.getHashPassword(password.toCharArray(), salt.getBytes(), iterations, keyLength));
            bos.write(ByteBuffer.allocate(4).putInt(iterations).array());
            bos.write(ByteBuffer.allocate(4).putInt(keyLength).array());
            this.password = bos.toByteArray();
        } catch (IOException ex) {
            // This should never happen, we have to exit the application.....
            ex.printStackTrace();
            System.exit(1);
        }
        */
        return this;

    }

    public boolean checkPassword( String password ){
        if(password != null){
            /*
            int max = this.password.length -1;
            String salt = AppHashing.getHashSHA512(String.valueOf(this.created.getEpochSecond()));
            byte[] passwordPart = Arrays.copyOfRange(this.password, 0, max - 8);
            //byte[] iterationsPart = Arrays.copyOfRange(this.password, max - 8, max - 4 );
            //byte[] keyLengthPart = Arrays.copyOfRange(this.password, max - 4, max);
            int iterations = ByteBuffer.wrap( this.password, passwordPart.length, 4 ).getInt();
            int keyLength = ByteBuffer.wrap( this.password, passwordPart.length + 4, 4 ).getInt();
            System.out.println(this.password);
            System.out.println(passwordPart);
            System.out.println(iterations);
            System.out.println(keyLength);
            byte[] checkPassword = AppHashing.getHashPassword(password.toCharArray(), salt.getBytes(), iterations, keyLength);
            */
            String salt = AppHashing.getHashSHA512(String.valueOf(this.created.getEpochSecond()));
            int iterations = PASSWORD_ITERATIONS;
            int keyLength = PASSWORD_KEY_LENGTH;
            byte[] checkPassword = AppHashing.getHashPassword(password.toCharArray(), salt.getBytes(), iterations, keyLength);
            return Arrays.equals(this.password, checkPassword);
        }else{
            return false;
        }
    }

    public String getLanguage() {
        return language;
    }

    public AppUser setLanguage(String language) {
        this.language = language;
        return this;
    }

    public String getTimezone() {
        return timezone;
    }

    public AppUser setTimezone(String timezone) {
        this.timezone = timezone;
        return this;
    }

    public String getVerificationcode() {
        return verificationcode;
    }

    public AppUser setVerificationcode(String verificationcode) {
        this.verificationcode = verificationcode;
        return this;
    }

    public String getResetcode() {
        return resetcode;
    }

    public AppUser setResetcode(String resetcode) {
        this.resetcode = resetcode;
        return this;
    }

    public boolean isVerified() {
        return verified;
    }

    public AppUser setVerified(boolean verified) {
        this.verified = verified;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public AppUser setActive(boolean active) {
        this.active = active;
        return this;
    }

    public Instant getCreated() {
        return created;
    }

    public AppUser setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getLastactive() {
        return lastactive;
    }

    public AppUser setLastactive(Instant lastactive) {
        this.lastactive = lastactive;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.id);
        hash = 47 * hash + Objects.hashCode(this.username);
        hash = 47 * hash + Objects.hashCode(this.emailaddress);
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
        final AppUser other = (AppUser) obj;
        if (this.verified != other.verified) {
            return false;
        }
        if (this.active != other.active) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        if (!Objects.equals(this.emailaddress, other.emailaddress)) {
            return false;
        }
        if (!Objects.equals(this.language, other.language)) {
            return false;
        }
        if (!Objects.equals(this.timezone, other.timezone)) {
            return false;
        }
        if (!Objects.equals(this.verificationcode, other.verificationcode)) {
            return false;
        }
        if (!Objects.equals(this.resetcode, other.resetcode)) {
            return false;
        }
        if (!Arrays.equals(this.password, other.password)) {
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

    public static void findById(String id, Handler<AppUser> callback ){
        Objects.nonNull(callback);
        if(id != null){
            new AppUser().findOne(id,callback);
        }else{
            callback.handle(null);
        }
    }

    public static void findByUsername( String username, Handler<AppUser> callback ){
        Objects.nonNull(callback);
        if(username == null){
            callback.handle(null);
        }else{
            new AppUser().findOneByFieldValue("username", username.toLowerCase(), callback);
        }
    }

    public static void findByEmailaddress( String emailaddress, Handler<AppUser> callback ){
        Objects.nonNull(callback);
        if(emailaddress == null){
            callback.handle(null);
        }else{
            new AppUser().findOneByFieldValue("emailaddress", emailaddress.toLowerCase(), callback);
        }
    }

    public static void authenticate( JsonObject authJson, Handler<AppUser> callback ){
        Objects.nonNull(authJson);
        Objects.nonNull(callback);
        String username = authJson.getString("username",null);
        String emailaddress = authJson.getString("emailaddress",null);
        String password = authJson.getString("password",null);
        if(username == null && emailaddress == null){
            callback.handle(null);
        }
        if(password == null){
            callback.handle(null);
        }
        if(username != null && emailaddress == null){
            findByUsername(username, foundUser -> {
                if(foundUser != null && foundUser.checkPassword(password)){
                    callback.handle(foundUser);
                }else{
                    callback.handle(null);
                }
            });
        }
        if(username == null && emailaddress != null){
            findByEmailaddress(emailaddress, foundUser -> {
                if(foundUser != null && foundUser.checkPassword(password)){
                    callback.handle(foundUser);
                }else{
                    callback.handle(null);
                }
            });
        }
        if(username != null && emailaddress != null){
            findByEmailaddress(emailaddress, foundUserEmail -> {
                if(foundUserEmail != null){
                    findByUsername(username, foundUserName -> {
                        if(foundUserEmail.equals(foundUserName)){
                            if(foundUserName.checkPassword(password)){
                                callback.handle(foundUserName);
                            }else{
                                callback.handle(null);
                            }
                        }else{
                            callback.handle(null);
                        }
                    });
                }else{
                    callback.handle(null);
                }
            });
        }
    }



}