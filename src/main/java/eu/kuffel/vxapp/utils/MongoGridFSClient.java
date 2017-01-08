package eu.kuffel.vxapp.utils;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.gridfs.*;
import com.mongodb.async.client.gridfs.helpers.AsyncStreamHelper;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import static io.vertx.ext.mongo.MongoClient.DEFAULT_POOL_NAME;
import io.vertx.ext.mongo.impl.MongoClientImpl;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;
import org.bson.Document;
import org.bson.types.ObjectId;


/**
 * Created by adam on 08.01.17.
 */
public class MongoGridFSClient extends MongoClientImpl {

    private final GridFSBucket bucket;


    public static MongoGridFSClient createNonShared(Vertx vertx, JsonObject config) {
        return new MongoGridFSClient(vertx, config, UUID.randomUUID().toString());
    }

    public static MongoGridFSClient createShared(Vertx vertx, JsonObject config, String dataSourceName) {
        return new MongoGridFSClient(vertx, config, dataSourceName);
    }

    public static MongoGridFSClient createShared(Vertx vertx, JsonObject config) {
        return new MongoGridFSClient(vertx, config, DEFAULT_POOL_NAME);
    }


    public MongoGridFSClient(Vertx vertx, JsonObject config, String dataSourceName) {
        super(vertx, config, dataSourceName);
        String db_name = config.getString("db_name","gridfs_db");
        String bucket_name = config.getString("bucket_name","files");
        bucket = GridFSBuckets.create(mongo.getDatabase(db_name), bucket_name);
    }


    /**
     * Upload a ByteBuffer with the given filename to gridfs. The callback will be called with true if everything is fine.
     * @param filename Filename
     * @param buffer Vertx ByteBuffer
     * @param metadata Optional Metadata
     * @param callback Vertx Handler, will be called with a boolean value.
     */
    public void uploadFile( String filename, Buffer buffer, JsonObject metadata, Handler<Boolean> callback ){
        Objects.nonNull(filename);
        Objects.nonNull(buffer);
        Objects.nonNull(callback);
        GridFSUploadOptions options = new GridFSUploadOptions();
        if(metadata != null){
            options.metadata(Document.parse(metadata.encode()));
        }
        GridFSUploadStream stream = bucket.openUploadStream(filename,options);
        stream.write(ByteBuffer.wrap(buffer.getBytes()),new SingleResultCallback<Integer>() {
            @Override
            public void onResult(Integer filesize, Throwable uploadError) {
                stream.close(new SingleResultCallback<Void>() {
                    @Override
                    public void onResult(Void result, Throwable closeError) {
                        callback.handle(uploadError == null);
                    }
                });
            }
        });
    }

    /**
     * Get a file from gridfs as vertx buffer.
     * @param objectId MongoDB ObjectID
     * @param callback Callback Funtion with a Buffer as parameter, null if id doesnt exists.
     */
    public void downloadFile( ObjectId objectId, Handler<Buffer> callback ){
        Objects.nonNull(objectId);
        Objects.nonNull(callback);
        GridFSDownloadStream downloadStream = bucket.openDownloadStream(objectId);
        downloadStream.getGridFSFile(new SingleResultCallback<GridFSFile>() {
            @Override
            public void onResult(GridFSFile file, Throwable t) {
                if(file != null){
                    ByteBuffer byteBuffer = ByteBuffer.allocate((int)file.getLength());
                    downloadStream.read(byteBuffer, new SingleResultCallback<Integer>() {
                        @Override
                        public void onResult(Integer result, Throwable t) {
                            if(result > 0){
                                Buffer data = Buffer.buffer(byteBuffer.array());
                                downloadStream.close(new SingleResultCallback<Void>() {
                                    @Override
                                    public void onResult(Void result, Throwable t) {
                                        callback.handle(data);
                                    }
                                });
                            }else{
                                callback.handle(null);
                            }
                        }
                    });
                }else{
                    callback.handle(null);
                }
            }
        });
    }

    /**
     * Get GridFSFile Object
     * @param objectId MongoDB ObjectID
     * @param callback GridFSFile or null
     */
    public void getFile( ObjectId objectId, Handler<GridFSFile> callback ){
        Objects.nonNull(objectId);
        Objects.nonNull(callback);
        GridFSDownloadStream downloadStream = bucket.openDownloadStream(objectId);
        downloadStream.getGridFSFile(new SingleResultCallback<GridFSFile>() {
            @Override
            public void onResult(GridFSFile file, Throwable getFileError) {
                downloadStream.close(new SingleResultCallback<Void>() {
                    @Override
                    public void onResult(Void result, Throwable closeError) {
                        callback.handle(file);
                    }
                });
            }
        });
    }

    /**
     * Renames the stored file with the specified id.
     * @param objectId the id of the file in the files collection to rename
     * @param newFilename the new filename for the file
     * @param callback the callback that is completed once the file has been renamed
     */
    public void renameFile( ObjectId objectId, String newFilename, Handler<Void> callback ){
        Objects.nonNull(objectId);
        Objects.nonNull(newFilename);
        bucket.rename(objectId, newFilename, new SingleResultCallback<Void>() {
            @Override
            public void onResult(Void result, Throwable t) {
                if(callback != null){
                    callback.handle(result);
                }
            }
        });
    }


    /**
     * Given a id, delete this stored file's files collection document and associated chunks from a GridFS bucket.
     * @param objectId the ObjectId of the file to be deleted
     * @param callback the callback that is completed once the file has been deleted
     */
    public void deleteFile( ObjectId objectId, Handler<Void> callback ){
        Objects.nonNull(objectId);
        bucket.delete(objectId, new SingleResultCallback<Void>() {
            @Override
            public void onResult(Void result, Throwable t) {
                if(callback != null){
                    callback.handle(result);
                }
            }
        });
    }





    public void something(){



        /*
        GridFSDownloadStream downloadStream = bucket.openDownloadStream(new ObjectId("585d9435410aa981b828c0cc"));
        downloadStream.getGridFSFile(new SingleResultCallback<GridFSFile>() {
            @Override
            public void onResult(GridFSFile file, Throwable t) {
                if(file != null){
                    ByteBuffer byteBuffer = ByteBuffer.allocate((int)file.getLength());
                    downloadStream.read(byteBuffer, new SingleResultCallback<Integer>() {
                        @Override
                        public void onResult(Integer result, Throwable t) {
                            System.out.println("result: "+result);
                            System.out.println("buffer: "+new String( byteBuffer.array() ));
                        }
                    });
                }
            }
        });
        */



        /*
         try {
                ObjectId fileId = new ObjectId("585d9435410aa981b828c0cc");
                Path outputPath = Paths.get("/Users/adam/Desktop/demo.txt");
                AsynchronousFileChannel streamToDownloadTo = AsynchronousFileChannel.open(outputPath,
                     StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, StandardOpenOption.DELETE_ON_CLOSE);
                //AsyncStreamHelper.toAsyncOutputStream()
                //bucket.downloadToStream();
         }catch (IOException ex){
             ex.printStackTrace();
         }
        */



        //com.mongodb.async.client.gridfs.helpers.AsyncStreamHelper.toAsyncOutputStream();
        /*
        bucket.downloadToStream(new ObjectId("585d10d08bb98e0eec1c7fed"), channelToOutputStream(streamToDownloadTo),
                new SingleResultCallback<Long>() {
                    @Override
                    public void onResult(final Long result, final Throwable t) {
                        System.out.println("downloaded file sized: " + result);
                    }
                });
        */

        /*
        Bson filter = Filters.eq("_id", new ObjectId("991fbc4509700bed261f6fec"));
        FileOutputStream streamToDownloadTo = new FileOutputStream("/Users/akuffel/Desktop/demo.txt");
        bucket.downloadToStream(new ObjectId("585d10d08bb98e0eec1c7fed"), streamToDownloadTo);
        streamToDownloadTo.close();
        */
    }


    /*
    public void uploadSomething(){
        GridFSUploadOptions options = new GridFSUploadOptions();
        Document doc = new Document();
        doc.put("hello","world");
        options.metadata(doc);
        GridFSUploadStream stream = bucket.openUploadStream("demo.txt",options);
        stream.write(ByteBuffer.wrap("Hello gridfs".getBytes()), new SingleResultCallback<Integer>() {
            @Override
            public void onResult(Integer result, Throwable t) {
                System.out.println("onResult(Size):"+result);
                stream.close(new SingleResultCallback<Void>() {
                    @Override
                    public void onResult(Void result, Throwable t) {
                        System.out.println("onResult(Void)");
                    }
                });
            }
        });
    }
    */

}
