package eu.kuffel.vxapp;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import eu.kuffel.vxapp.utils.AppRandom;
import eu.kuffel.vxapp.utils.MongoGridFSClient;
import eu.kuffel.vxapp.verticles.AppWebserver;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.*;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.util.concurrent.TimeoutException;

/**
 * Main Application class,
 * parses parameters from cli or config files, and bootstraps verticles.
 *
 * @author akuffel
 * @version 0.0.1
 */
public class Application {

    /**
     * Version string for this application.
     */
    public static final String VERSION = "0.0.2";


    /**
     *  Used by cli parser to display usage information in console.
     */
    public static final String CMD_DEFAULT = "java -jar "+new java.io.File(Application.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();

    /**
     * Optional header to display some information in cli.
     */
    public static final String CMD_HEADER = "";

    /**
     * Optional header to display some information in cli.
     */
    public static final String CMD_FOOTER = "";

    /**
     * Reference to the global vertx object.
     */
    public static Vertx vertx;

    /**
     * Active config, parsed from CLI parameter or default config.
     */
    public static JsonObject config;

    /**
     * Reference to the an active mongo db connection.
     */
    public static MongoClient database;

    /**
     * Reference to the an active mongo db connection.
     */
    public static MongoGridFSClient gridfs;

    /**
     * Reference to an active redis cache connection.
     */
    public static RedisClient cache;


    /**
     * Reference to an active elasticsearch connection.
     */
    public static TransportClient elasticsearch;


    /**
     * Reference to an active rabbitmq connection.
     */
    public static Connection rabbitmq;

    /**
     * Reference to an active mailserver connection.
     */
    public static MailClient mailer;



    public static void main(String[] args) {
        CommandLine cmd = parseArgs(args);
        vertx = Vertx.vertx();


        if(config == null){
            /*
            System.out.println("Please provide a valid config file to start this application. See "+CMD_DEFAULT+" --help for more information.");
            System.exit(0);
            */
            config = getDefaultConfig();
        }
        //System.out.println(config.encodePrettily());


        // Create mongo db connection
        database = MongoClient.createShared(vertx, config.getJsonObject("mongodb", new JsonObject()));
        database.insert("startup_check_collection", new JsonObject(), (AsyncResult<String> event) -> {
            if(event.succeeded()){
                System.out.println("Connection to Mongo db server successful.");
                database.dropCollection("startup_check_collection",(dropEvent) -> {});
            }else{
                System.err.println(event.cause().getMessage());
                vertx.close();
            }
        });

        // Create mongo db grid fs connection
        gridfs = MongoGridFSClient.createShared(vertx, config.getJsonObject("gridfs",new JsonObject()));
        /*
        gridfs.uploadFile("test2.txt", Buffer.buffer("Klappt das würklüch?"), new JsonObject().put("ok","juhu"), new Handler<Boolean>() {
            @Override
            public void handle(Boolean event) {
                System.out.println(event);
            }
        });
        gridfs.downloadFile(new ObjectId("585d9435410aa981b828c0cc"), new Handler<Buffer>() {
            @Override
            public void handle(Buffer data) {
                System.out.println(data);
            }
        });
        gridfs.getFile(new ObjectId("585d9435410aa981b828c0cc"), new Handler<GridFSFile>() {
            @Override
            public void handle(GridFSFile file) {
                System.out.println(file);
            }
        });
        gridfs.renameFile(new ObjectId("585d9435410aa981b828c0cc"), "juhu.txt", new Handler<Void>() {
            @Override
            public void handle(Void event) {

            }
        });
        gridfs.deleteFile(new ObjectId("585d9435410aa981b828c0cc"),null);
        */


        // Create redis db connection
        cache = RedisClient.create(vertx, new RedisOptions(config.getJsonObject("cache", new JsonObject())));
        cache.echo("Connection to Redis cache server successful.", (AsyncResult<String> event) -> {
            if(event.succeeded()){
                System.out.println(event.result());
            }else{
                System.err.println(event.cause().getMessage());
                vertx.close();
            }
        });



        // Create elasticsearch connection
        JsonObject elasticsearchConfig = config.getJsonObject("elasticsearch",null);
        if(elasticsearchConfig != null){
            try{
                InetAddress address = InetAddress.getByName(elasticsearchConfig.getString("host"));
                InetSocketTransportAddress inetAddress= new InetSocketTransportAddress(address, elasticsearchConfig.getInteger("port"));
                Settings settings = Settings.builder().put("cluster.name", elasticsearchConfig.getString("cluster.name")).build();
                elasticsearch = new PreBuiltTransportClient(settings).addTransportAddress(inetAddress);
                if(elasticsearch.connectedNodes().size() > 0){
                    System.out.println("Connection to Elasticsearch successful.");
                }
            } catch(UnknownHostException ex){
                ex.printStackTrace();
                vertx.close();
            }
        }


        // Create rabbitmq connection
        JsonObject rabbitmqConfig = config.getJsonObject("rabbitmq",null);
        if(rabbitmqConfig != null){
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setUsername(rabbitmqConfig.getString("username"));
                factory.setPassword(rabbitmqConfig.getString("password"));
                factory.setHost(rabbitmqConfig.getString("host"));
                factory.setVirtualHost(rabbitmqConfig.getString("virtualHost"));
                factory.setPort(rabbitmqConfig.getInteger("port"));
                rabbitmq = factory.newConnection();
                if(rabbitmq.isOpen()){
                    System.out.println("Connection to RabbitMQ successful.");
                }
            }catch (IOException ex){
                ex.printStackTrace();
                vertx.close();
            }catch (TimeoutException ex){
                ex.printStackTrace();
                vertx.close();
            }
        }

        // Create mailserver connection
        JsonObject mailConfigJSON = Application.config.getJsonObject("mailserver", null);
        if(mailConfigJSON != null) {
            MailConfig config = new MailConfig();
            config.setHostname(mailConfigJSON.getString("hostname"));
            config.setPort(mailConfigJSON.getInteger("port"));
            if (mailConfigJSON.getString("startTLS").equalsIgnoreCase("REQUIRED")) {
                config.setStarttls(StartTLSOptions.REQUIRED);
            }
            if (mailConfigJSON.getString("startTLS").equalsIgnoreCase("OPTIONAL")) {
                config.setStarttls(StartTLSOptions.OPTIONAL);
            }
            if (mailConfigJSON.getString("startTLS").equalsIgnoreCase("DISABLED")) {
                config.setStarttls(StartTLSOptions.DISABLED);
            }
            if (mailConfigJSON.getString("login").equalsIgnoreCase("REQUIRED")) {
                config.setLogin(LoginOption.REQUIRED);
                config.setUsername(mailConfigJSON.getString("username"));
                config.setPassword(mailConfigJSON.getString("password"));
            } else {
                config.setLogin(LoginOption.NONE);
            }
            if (mailConfigJSON.getString("ssl").equalsIgnoreCase("yes")) {
                config.setSsl(true);
            } else {
                config.setSsl(false);
            }
            mailer = MailClient.createShared(vertx, config);
        }






        // Start webserver verticle.....
        DeploymentOptions webserverOptions = new DeploymentOptions();
        webserverOptions.setInstances(config.getJsonObject("webserver").getInteger("instances"));
        webserverOptions.setConfig(config);
        vertx.deployVerticle(AppWebserver.class.getCanonicalName(), webserverOptions, (AsyncResult<String> event) -> {
            if(event.succeeded()){
                System.out.println("Webserver deployed successfully and listening on "+config.getJsonObject("webserver").getInteger("port", null));
            }else{
                System.err.println(event.cause().getMessage());
                event.cause().printStackTrace();
                vertx.close();
            }
        });


        /*
        Vertx vertx = Vertx.vertx();
        vertx.setPeriodic(2000, event -> {
            System.out.println("ok");
        });
        */
    }


    private static JsonObject getDefaultConfig(){
        JsonObject defaultConfig = new JsonObject();

        JsonObject application = new JsonObject();
        application.put("name", "vxApp");
        application.put("description", "Simple template microservice configuration file.");
        application.put("package", Application.class.getPackage().getName());
        application.put("mode", "development");
        application.put("secret", AppRandom.getRandomStringAlphanumeric(64));
        application.put("version", VERSION);

        JsonObject filesystem = new JsonObject();

        JsonObject webserver = new JsonObject();
        webserver.put("instances", 4);
        webserver.put("port", 8888);
        webserver.put("apikey", "DEMOKEY");
        webserver.put("timeout", 5000);
        webserver.put("logging", false);
        webserver.put("postlimit", 1024*1024*10); // 10 MB


        JsonObject customHeaders = new JsonObject();
        customHeaders.put("X-Powered-By", "vxApp");
        webserver.put("headers", customHeaders);

        JsonObject mailserver = new JsonObject();
        mailserver.put("hostname" , "mail.mailserver.com");
        mailserver.put("port" , 587);
        mailserver.put("startTLS" , "OPTIONAL");
        mailserver.put("login" , "REQUIRED");
        mailserver.put("username" , "user@mailserver.com");
        mailserver.put("password" , "password");
        mailserver.put("ssl" , "no");

        // http://vertx.io/docs/vertx-mongo-client/java/#_configuring_the_client
        JsonObject mongoConfig = new JsonObject();
        mongoConfig.put("db_name", "vxapp_db");
        mongoConfig.put("host", "127.0.0.1");
        mongoConfig.put("port", 27017);


        JsonObject gridfsConfig = new JsonObject();
        gridfsConfig.put("db_name", "vxapp_fs_db");
        gridfsConfig.put("bucket_name", "files");
        gridfsConfig.put("host", "127.0.0.1");
        gridfsConfig.put("port", 27017);


        JsonObject cacheConfig = new JsonObject();
        cacheConfig.put("host", "localhost");
        cacheConfig.put("port", 6379);
        cacheConfig.put("encoding", "UTF-8");
        cacheConfig.put("tcpKeepAlive", true);
        cacheConfig.put("tcpNoDelay", true);


        JsonObject elasticsearchConfig = new JsonObject();
        elasticsearchConfig.put("hostname","localhost");
        elasticsearchConfig.put("port",9300);
        elasticsearchConfig.put("cluster.name","myclustername");


        JsonObject rabbitmqConfig = new JsonObject();
        rabbitmqConfig.put("username","myadmin");
        rabbitmqConfig.put("password","password");
        rabbitmqConfig.put("host","localhost");
        rabbitmqConfig.put("port",5672);
        rabbitmqConfig.put("virtualHost","/");


        defaultConfig.put("application", application);
        defaultConfig.put("filesystem", filesystem);
        defaultConfig.put("webserver", webserver);
        defaultConfig.put("mailserver", mailserver);
        defaultConfig.put("mongodb", mongoConfig);
        defaultConfig.put("gridfs", gridfsConfig);
        defaultConfig.put("cache", cacheConfig);
        defaultConfig.put("elasticsearch", elasticsearchConfig);
        defaultConfig.put("rabbitmq", rabbitmqConfig);
        return defaultConfig;
    }




    /**
     * Parse commandline arguments with Apache Commons CLI.
     * @param args Arguments from main method.
     * @return CommandLine object with parsed arguments
     */
    public static CommandLine parseArgs( String[] args ){
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        Options options = new Options();
        try{

            options.addOption(Option.builder("c").longOpt("config").desc("Path to config.json")
                    .hasArg(true).argName("config.json").required(false).build());
            options.addOption(Option.builder("i").longOpt("init").desc("Create empty template config.json").build());
            options.addOption("v", "version", false, "Display current application version");
            options.addOption("h", "help", false, "Show usage and help information");

            cmd = parser.parse(options, args);

            if(cmd.hasOption("c")){
                Path p = Paths.get(cmd.getOptionValue("c").trim());
                Path fullPath = p.toAbsolutePath().normalize();
                if(Files.exists(fullPath, LinkOption.NOFOLLOW_LINKS)){
                    byte[] configData = Files.readAllBytes(fullPath);
                    try{
                        JsonObject configJSON = new JsonObject(new String(configData));
                        config = configJSON;
                    }catch(DecodeException ex){
                        System.out.println("Error: "+fullPath+" is not a valid json file.");
                        System.out.println(ex.getMessage());
                        System.exit(0);
                    }
                }else{
                    System.out.println("Error: "+fullPath+" not found.");
                    System.exit(0);
                }
            }
            if(cmd.hasOption("i")){
                Path appDir = Paths.get(".").toAbsolutePath().normalize();
                Path configFilePath = Paths.get(appDir.toString(),"config.json");
                try{
                    if(Files.exists(configFilePath, LinkOption.NOFOLLOW_LINKS)){
                        System.out.println("Error: "+configFilePath+" already exists. Cowardly refusing to overwrite it.");
                    }else{
                        Files.write(configFilePath, getDefaultConfig().encodePrettily().getBytes(), StandardOpenOption.CREATE_NEW);
                        System.out.println("Success: "+configFilePath+" successfully created. Modify it to create your own settings.");
                    }
                }catch(IOException ex){
                    ex.printStackTrace();
                    System.exit(1);
                }
                System.exit(0);
            }
            if(cmd.hasOption("h")){
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(CMD_DEFAULT, CMD_HEADER, options, CMD_FOOTER, true);
                System.exit(0);
            }
            if(cmd.hasOption("v")){
                System.out.println(VERSION);
                System.exit(0);
            }

        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(CMD_DEFAULT, CMD_HEADER, options, CMD_FOOTER, true);
            System.exit(0);
        } finally {
            return cmd;
        }
    }


}