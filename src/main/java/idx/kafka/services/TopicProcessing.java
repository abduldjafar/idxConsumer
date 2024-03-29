package idx.kafka.services;

import io.confluent.connect.jms.Value;
import org.apache.avro.util.Utf8;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.*;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.kafka.clients.consumer.ConsumerRecord;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

class DataReturn {
    String Filename;
    String idxGroupId;
    Integer idxTotal;
    Integer idxNumber;

    String idxMethod;
    DataReturn(String c, String m, Integer d, Integer a, String method) {
        Filename = c;
        idxGroupId = m;
        idxTotal = d;
        idxNumber = a;
        idxMethod = method;
    }
}

public class TopicProcessing {

    public static DataReturn saveToFileFromAvroRecord(Value record) throws IOException {

        Utf8 groupIDObj = new Utf8("idxGroupID");
        Utf8 filenameObj = new Utf8("idxFileName");
        Utf8 idxNumberObj = new Utf8("idxNumber");
        Utf8 idxTotalObj = new Utf8("idxTotal");
        Utf8 idxMethodObj = new Utf8("idxMethod");




            if (record.getBytes() != null) {
                String filename = String.valueOf(record.getProperties().get(filenameObj).getString());
                Integer idxTotal = record.getProperties().get(idxTotalObj).getInteger();
                Integer idxNumber = record.getProperties().get(idxNumberObj).getInteger();
                String idxGroupID = String.valueOf(record.getProperties().get(groupIDObj).getString());
                String idxMethod = String.valueOf(record.getProperties().get(idxMethodObj).getString());


                filename = filename.replace("/", "_").replace(" ", "_");
                System.out.println("process file " + filename + " with idxGroupID " + idxGroupID + "...");
                FileChannel fc = new FileOutputStream(filename).getChannel();
                fc.write(record.getBytes());
                fc.close();
                return new DataReturn(filename, idxGroupID, idxTotal, idxNumber,idxMethod);
            } else {

                return null;
            }
    }

    public static String getFileType(String filename) {
        String[] split = filename.split("[.]");
        String file_type = split[split.length-1];
        return file_type;
    }

    public static void sendFile(String filename, String url, String fileType, String idxtotal, String idxnumber, String idxgroup, String idxMethod) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                sslsf).setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)).build();

        //HttpClient httpclient = new DefaultHttpClient();
        File file = new File(filename);
        HttpPost post = new HttpPost(url);
        FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
        StringBody stringBody1 = new StringBody(idxgroup, ContentType.MULTIPART_FORM_DATA);
        StringBody stringBody2 = new StringBody(idxtotal, ContentType.MULTIPART_FORM_DATA);
        StringBody stringBody3 = new StringBody(idxnumber, ContentType.MULTIPART_FORM_DATA);
        StringBody stringBody4 = new StringBody(idxMethod, ContentType.MULTIPART_FORM_DATA);

        //
        MultipartEntityBuilder builderm = MultipartEntityBuilder.create();
        builderm.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builderm.addPart("file", fileBody);
        builderm.addPart("idxgroup", stringBody1);
        builderm.addPart("idxtotal", stringBody2);
        builderm.addPart("idxnumber", stringBody3);
        builderm.addPart("idx_method",stringBody4);
        HttpEntity entity = builderm.build();
        //
        post.setEntity(entity);
        HttpResponse response = httpclient.execute(post);

        if (response.getStatusLine().getStatusCode() == 200) {
            File myObj = new File(filename);
            if (myObj.exists() && !myObj.isDirectory()) {
                // do something
                if (myObj.delete()) {
                    System.out.println("success process " + filename + " with idxGroupID " + idxgroup + "...");
                    System.out.println("Deleted the file: " + myObj.getName());
                }
            }
        } else {
            System.out.println(response.getStatusLine());
        }
    }

    public static void run(final ConsumerRecord < String, Value > record, final String url) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {


        DataReturn dataReturn = saveToFileFromAvroRecord(record.value());
        if (dataReturn != null) {
            File myObj = new File(dataReturn.Filename);
            if (myObj.exists() && !myObj.isDirectory()) {
                String fileType = getFileType(dataReturn.Filename);

                System.out.println("file type :"+fileType);
                if (fileType.equals("pdf")  || fileType.equals("xlsx")){
                    sendFile(dataReturn.Filename, url, fileType, dataReturn.idxTotal.toString(), dataReturn.idxNumber.toString(), dataReturn.idxGroupId,dataReturn.idxMethod);
                }
                // "http://localhost:8000/v1/idx/upload"
            }

        }
    }
}