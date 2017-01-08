package eu.kuffel.vxapp.routers;

import eu.kuffel.vxapp.models.AppClient;
import eu.kuffel.vxapp.models.AppUser;
import eu.kuffel.vxapp.utils.AppCommons;
import eu.kuffel.vxapp.utils.AppJsonMessages;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.Instant;

/**
 * Created by adam on 08.01.17.
 */
public class AppUserRouter {

    private static final int USERNAME_MIN_LENGTH = 4;
    private static final int PASSWORD_MIN_LENGTH = 4;

    private static Router router;

    public static Router router(Vertx vertx) {
        router = Router.router(vertx);



        router.get("/user").handler( rc -> {
            AppUser user = (AppUser) rc.data().get("user");
            if(user != null){
                JsonObject userJSON = user.getJSON("_id","password","verificationcode","resetcode");
                rc.response().setStatusCode(200).end(userJSON.encode());
            }else{
                AppJsonMessages.sendMessage(rc,AppJsonMessages.ACCESS_DENIED);
            }
        }); // END GET - /user



        router.patch("/user").handler( rc -> {
            JsonArray errors = new JsonArray();
            JsonObject data = AppCommons.parseJsonRequest(rc);
            AppUser user = (AppUser) rc.data().get("user");
            if(data != null){
                if(user != null){

                    String username = null;
                    boolean changeUsername = false;
                    String emailaddress = null;
                    boolean changeEmailaddress = false;

                    if(data.containsKey("username")){
                        username = data.getString("username",null);
                        if(username != null && username.length() > USERNAME_MIN_LENGTH ){
                            changeUsername = true;
                        }else{
                            errors.add(new JsonObject().put("username", "Must be at least "+USERNAME_MIN_LENGTH+" characters long."));
                        }
                    }

                    if(data.containsKey("emailaddress")){
                        emailaddress = data.getString("emailaddress",null);
                        if(EmailValidator.getInstance().isValid(emailaddress)){
                            changeEmailaddress = true;
                        }else{
                            errors.add(new JsonObject().put("emailaddress","Invalid emailaddress"));
                        }
                    }

                    if(data.containsKey("password")){
                        String changePassword = data.getString("password",null);
                        if(changePassword.length() < PASSWORD_MIN_LENGTH){
                            errors.add(new JsonObject().put("password", "Must be at least "+PASSWORD_MIN_LENGTH+" characters long."));
                        }else{
                            user.setPassword(changePassword);
                        }
                    }
                    if(data.containsKey("language")){
                        String changeLanguage = data.getString("language",null);
                        user.setLanguage(changeLanguage);
                    }
                    if(data.containsKey("timezone")){
                        String changeTimezone = data.getString("timezone",null);
                        user.setTimezone(changeTimezone);
                    }



                    Future<AppUser> checkUsernameFuture = Future.future();
                    Future<AppUser> checkEmailFuture = Future.future();
                    AppUser.findByUsername(username, userByName -> {
                        checkUsernameFuture.complete(userByName);
                    });
                    AppUser.findByEmailaddress(emailaddress, userByMail -> {
                        checkEmailFuture.complete(userByMail);
                    });
                    CompositeFuture.all(checkUsernameFuture,checkEmailFuture).setHandler(ar ->{

                        boolean usernameTaken = checkUsernameFuture.result() != null;
                        boolean emailaddressTaken = checkEmailFuture.result() != null;

                        if(data.containsKey("username")){
                            if(usernameTaken){
                                errors.add(new JsonObject().put("username", "Username already in use."));
                            }else{
                                user.setUsername(data.getString("username",null));
                            }
                        }

                        if(data.containsKey("emailaddress")){
                            if(emailaddressTaken){
                                errors.add(new JsonObject().put("emailaddress", "Emailaddress already in use."));
                            }else{
                                user.setEmailaddress(data.getString("emailaddress",null));
                            }
                        }
                        if(errors.size() == 0){
                            user.save(doneHandler ->{
                                JsonObject userJSON = user.getJSON("_id","password","verificationcode","resetcode");
                                rc.response().setStatusCode(200).end(userJSON.encode());
                            });
                        }else{
                            AppJsonMessages.sendMessage(rc, AppJsonMessages.PATCH_FAILED, new JsonObject().put("errors",errors));
                        }
                    });
                }else{
                    AppJsonMessages.sendMessage(rc,AppJsonMessages.ACCESS_DENIED);
                }
            }
        }); // END PATCH - /user


        router.delete("/user").handler( rc -> {
            JsonArray errors = new JsonArray();
            String[] required = new String[]{ "emailaddress", "password" };
            AppClient client = (AppClient) rc.data().get("client");
            AppUser user = (AppUser) rc.data().get("user");
            JsonObject data = AppCommons.parseJsonRequest(rc, required);
            if(data != null && user != null){
                if( user.getEmailaddress().equalsIgnoreCase(data.getString("emailaddress",null)) ){
                    if(user.checkPassword(data.getString("password",null))){
                        client.setUserId(null);
                        client.save(clientSaved->{
                            // TODO : Delete associated data in other collections.
                            user.delete(userDeleted ->{
                                AppJsonMessages.sendMessage(rc,AppJsonMessages.USER_DELETED);
                            });
                        });
                    }else{
                        AppJsonMessages.sendMessage(rc,AppJsonMessages.ACCESS_DENIED);
                    }
                }else{
                    AppJsonMessages.sendMessage(rc,AppJsonMessages.ACCESS_DENIED);
                }
            }
        }); // END DELETE - /user




        router.post("/user/signup").handler( rc -> {
            JsonArray errors = new JsonArray();
            String[] required = new String[]{ "emailaddress", "password" };
            JsonObject data = AppCommons.parseJsonRequest(rc, required);
            if(data != null){
                String username = data.getString("username",null);
                String emailaddress = data.getString("emailaddress",null);
                String password = data.getString("password",null);
                if(username != null){
                    if(username.length() < USERNAME_MIN_LENGTH){
                        errors.add(new JsonObject().put("username", "Must be at least "+USERNAME_MIN_LENGTH+" characters long."));
                    }
                }
                if(!EmailValidator.getInstance().isValid(emailaddress)){
                    errors.add(new JsonObject().put("emailaddress", "Invalid emailaddress"));
                }
                if(password != null){
                    if(password.length() < PASSWORD_MIN_LENGTH){
                        errors.add(new JsonObject().put("password", "Must be at least "+PASSWORD_MIN_LENGTH+" characters long."));
                    }
                }
                if(errors.size() == 0){
                    AppUser.findByEmailaddress(emailaddress, emailaddressTaken -> {
                        if(emailaddressTaken != null) {
                            errors.add(new JsonObject().put("emailaddress", "Emailaddress already in use."));
                        }
                        AppUser.findByUsername(username, usernameTaken -> {
                            if(usernameTaken != null){
                                errors.add(new JsonObject().put("username", "Username already in use."));
                            }
                            if(errors.size() == 0){
                                AppUser signupUser = new AppUser();
                                if(username == null){
                                    signupUser.setUsername(emailaddress.toLowerCase());
                                }else{
                                    signupUser.setUsername(username.toLowerCase());
                                }
                                signupUser.setEmailaddress(emailaddress.toLowerCase());
                                signupUser.setPassword(password);
                                signupUser.setActive(true);
                                // TODO: signupUser.setLanguage()
                                // TODO: signupUser.setTimezone()
                                signupUser.save(savedUser ->{
                                    if( savedUser!= null ){
                                        AppJsonMessages.sendMessage(rc,AppJsonMessages.USER_CREATED);
                                    }else{
                                        AppJsonMessages.sendMessage(rc, AppJsonMessages.SERVER_ERROR);
                                    }
                                });
                            }else{
                                AppJsonMessages.sendMessage(rc,AppJsonMessages.CLIENT_ERROR, new JsonObject().put("errors",errors));
                            }
                        });
                    });
                }else{
                    AppJsonMessages.sendMessage(rc,AppJsonMessages.CLIENT_ERROR, new JsonObject().put("errors",errors));
                }
            }
        }); // END /user/signup


        router.post("/user/login").handler( rc -> {
            JsonArray errors = new JsonArray();
            String[] required = new String[]{ "password" };
            JsonObject data = AppCommons.parseJsonRequest(rc, required);
            if(data != null){
                AppUser.authenticate(data,authenticatedUser -> {
                    if(authenticatedUser != null){
                        AppClient client = (AppClient)rc.data().get("client");
                        client.setUserId(authenticatedUser.getId());
                        client.save( doneHandler -> {
                            rc.data().put("user",authenticatedUser);
                            authenticatedUser.setLastactive(Instant.now());
                            authenticatedUser.save( userUpdated -> {
                                AppJsonMessages.sendMessage( rc,AppJsonMessages.USER_LOGGED_IN);
                            });
                        });
                    }else{
                        AppJsonMessages.sendMessage(rc, AppJsonMessages.INVALID_CREDENTIALS);
                    }
                });
            }
        }); // END /user/login

        router.post("/user/logout").handler( rc -> {
            AppClient client = (AppClient) rc.data().get("client");
            AppUser user = (AppUser) rc.data().get("user");
            if(client != null && user != null){
                client.setUserId(null);
                client.save(clientSaved ->{
                    AppJsonMessages.sendMessage( rc,AppJsonMessages.USER_LOGGED_OUT);
                });
            }else{
                AppJsonMessages.sendMessage(rc,AppJsonMessages.ACCESS_DENIED);
            }
        }); // END /user/logout




        /*
        TODO: POST /user/verify
        TODO: POST /user/verify/reset
        TODO: POST /user/forgot
        TODO: POST /user/forgot/reset
        */


        return router;
    }


}
