package idx.kafka.services;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.confluent.connect.jms.Value;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TopicProcessing {

    public  static String saveTofile(JsonObject obj) throws IOException {
        byte[] bytesFileStr = obj.get("bytes") != JsonNull.INSTANCE ? obj.get("bytes").getAsString().getBytes() : null;

        if (bytesFileStr != null) {
            JsonObject properties = obj.get("properties").getAsJsonObject();
            JsonObject filenameObj = properties.get("idxFileName") != JsonNull.INSTANCE? properties.get("idxFileName").getAsJsonObject():null;
            String filename = filenameObj.get("string") != JsonNull.INSTANCE? filenameObj.get("string").getAsString(): null;

            if (filename != null){
                filename = filename.replace("/","_").replace(" ","_");
                Files.write(Paths.get(filename), bytesFileStr);
            }

            return  filename;
        }else{
            return null;
        }

    }

    public  static  String getFileType(String filename){
        return "pdf";
    }

    public static void sendFile(String filename, String url, String fileType) throws IOException {

        AsyncHttpClient client = new DefaultAsyncHttpClient();
        client.prepare("POST", url)
                .setHeader("Content-Type", "multipart/form-data; boundary=---011000010111000001101001")
                .setBody("-----011000010111000001101001\r\nContent-Disposition: form-data; name=\"file\"; filename=\""+filename+"\"\r\nContent-Type: application/pdf\r\n\r\n\r\n-----011000010111000001101001--\r\n")
                .execute()
                .toCompletableFuture()
                .thenAccept(System.out::println)
                .join();

        client.close();

        File myObj = new File(filename);
        if (myObj.delete()) {
            System.out.println("Deleted the file: " + myObj.getName());
        } else {
            System.out.println("Failed to delete the file: " + myObj.getName());
        }
    }

    public static void run(final ConsumerRecord<String, Value> record) throws IOException {
        final String key = record.key(); //Prepare consumed key
        final Value value = record.value(); //Prepare consumed value

        JsonElement e = new JsonParser().parse(value.toString());
        if (e.isJsonObject()) {

            JsonObject obj = e.getAsJsonObject();
            String filename = saveTofile(obj);

            if (filename != null){
                String fileType = getFileType(filename);
                sendFile(filename,"http://localhost:8000/v1/idx/upload",fileType);
            }


        }
    }
}
