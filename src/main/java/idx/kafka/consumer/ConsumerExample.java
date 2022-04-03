package idx.kafka.consumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.confluent.connect.jms.Value;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import idx.kafka.services.TopicProcessing;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;

import java.io.*;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConsumerExample {

    private static final String TOPIC = "test_openapi_push"; //topic name
    private static final Properties props = new Properties();
    private static String configFile;

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(final String[] args) throws IOException {

        //Refer to javaschema.config
        // Load properties from a local configuration file e.g javaschema.config
        // Create the configuration file (e.g. at '$HOME/.confluent/java.config') with configuration parameters
        // to connect to your Kafka cluster, which can be on your local host, Confluent Cloud, or any other cluster.
        // Documentation at https://docs.confluent.io/platform/current/tutorials/examples/clients/docs/java.html
        configFile ="/Users/user/IdeaProjects/IdxAttachmentGent/src/main/java/kafka.config";
        if (!Files.exists(Paths.get(configFile))) {
            throw new IOException(configFile + " not found.");
        } else {
            try (InputStream inputStream = new FileInputStream(configFile)) {
                props.load(inputStream);
            }
        }


        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "https://DevIMDSConfSchema01:8081");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "f-30545860-0-3553"); //consumer group
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true"); //auto commit setting true or false
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000"); //auto commit interval
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); //earliest or latest
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        try (final KafkaConsumer<String, Value> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(TOPIC));
            //ADI is avsc file which contain Avro Schema Format, you can find it in "./src/main/resources/avro/io/confluent/examples/clients/basicavro/"
            while (true) {
                final ConsumerRecords<String, Value> records = consumer.poll(Duration.ofMillis(100)); //pooling time in ms
                for (final ConsumerRecord<String, Value> record : records) {
                     TopicProcessing.run(record);
                    }
                }
            }
        }
    }



