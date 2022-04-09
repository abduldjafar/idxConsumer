package idx.kafka.config;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {

    private static final Properties props = new Properties();


    public static Properties Kafka(String configFile) throws IOException {
        //Refer to javaschema.config
        // Load properties from a local configuration file e.g javaschema.config
        // Create the configuration file (e.g. at '$HOME/.confluent/java.config') with configuration parameters
        // to connect to your Kafka cluster, which can be on your local host, Confluent Cloud, or any other cluster.
        // Documentation at https://docs.confluent.io/platform/current/tutorials/examples/clients/docs/java.html
        // configFile ="/Users/user/IdeaProjects/IdxAttachmentGent/src/main/java/kafka.config";
        if (!Files.exists(Paths.get(configFile))) {
            throw new IOException(configFile + " not found.");
        } else {
            try (InputStream inputStream = new FileInputStream(configFile)) {
                props.load(inputStream);
            }
        }

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        return props;

    }
}
