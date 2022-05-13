package idx.kafka.services;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.confluent.connect.jms.Value;
import org.apache.avro.util.Utf8;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

class DataReturn {
    String Filename;
    String idxGroupId;
    Integer idxTotal;
    Integer idxNumber;
    DataReturn(String c,String m, Integer d, Integer a)
    {
        Filename = c;
        idxGroupId = m;
        idxTotal = d;
        idxNumber = a;
    }
}

public class TopicProcessing {

    public  static DataReturn saveToFileFromAvroRecord(Value record) throws IOException {

        Utf8 groupIDObj =  new Utf8("idxGroupID") ;
        Utf8 filenameObj =  new Utf8("idxFileName");
        Utf8 idxNumberObj = new Utf8("idxNumber");
        Utf8 idxTotalObj = new Utf8("idxTotal");

        if (record.getBytes() != null){
            String filename = String.valueOf(record.getProperties().get(filenameObj).getString());
            Integer idxTotal = record.getProperties().get(idxTotalObj).getInteger();
            Integer idxNumber = record.getProperties().get(idxNumberObj).getInteger();
            String idxGroupID = String.valueOf(record.getProperties().get(groupIDObj).getString());


            filename = filename.replace("/","_").replace(" ","_");
            System.out.println("process file "+filename+" ....");
            FileChannel fc = new FileOutputStream(filename).getChannel();
            fc.write(record.getBytes());
            fc.close();
            return  new DataReturn(filename,idxGroupID,idxTotal,idxNumber);
        }else{
            return null;
        }
    }

    public  static  String getFileType(String filename){
        return "pdf";
    }

    public static void sendFile(String filename, String url, String fileType,String idxtotal,String idxnumber,String idxgroup) throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        ((AbstractHttpClient) httpclient).setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
        File file = new File(filename);
        HttpPost post = new HttpPost(url);
        FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
        StringBody stringBody1 = new StringBody(idxgroup, ContentType.MULTIPART_FORM_DATA);
        StringBody stringBody2 = new StringBody(idxtotal, ContentType.MULTIPART_FORM_DATA);
        StringBody stringBody3 = new StringBody(idxnumber,ContentType.MULTIPART_FORM_DATA);
//
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", fileBody);
        builder.addPart("idxgroup", stringBody1);
        builder.addPart("idxtotal", stringBody2);
        builder.addPart("idxnumber",stringBody3);
        HttpEntity entity = builder.build();
//
        post.setEntity(entity);
        HttpResponse response = httpclient.execute(post);

        if (response.getStatusLine().getStatusCode() == 200) {
            try {
                Files.deleteIfExists(
                        Paths.get(filename));
            }
            catch (NoSuchFileException e) {
                System.out.println(
                        "No such file/directory exists");
            }
            catch (DirectoryNotEmptyException e) {
                System.out.println("Directory is not empty.");
            }
            catch (IOException e) {
                System.out.println("Invalid permissions.");
            }

            System.out.println("Deletion successful.");
        }else {
            System.out.println(response.getStatusLine());
        }




    }

    public static void run(final ConsumerRecord<String, Value> record, final String url) throws IOException {


            DataReturn dataReturn = saveToFileFromAvroRecord(record.value());

            if (dataReturn != null){
                String fileType = getFileType(dataReturn.Filename);
                sendFile(dataReturn.Filename,url,fileType,dataReturn.idxTotal.toString(),dataReturn.idxNumber.toString(),dataReturn.idxGroupId);
                // "http://localhost:8000/v1/idx/upload"
            }



    }
}
