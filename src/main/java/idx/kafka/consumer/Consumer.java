package idx.kafka.consumer;

import idx.kafka.services.ParallelTopicProcessing;
import io.confluent.connect.jms.Value;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;

import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.*;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import idx.kafka.config.Config;

public class Consumer {


    private static final String TOPIC = "test_openapi_push"; //topic name
    @SuppressWarnings("InfiniteLoopStatement")

    public static void main(final String[] args) throws IOException {
        if (args.length != 2){
            System.out.println("please use : java -jar app.jar file_config.config topic_for_consumer_name");
            System.exit(0);
        }

        String config_file = args[0];
        String topic = args[1];

        Config config = new Config();
        Properties props = config.Kafka(config_file);

        try (final KafkaConsumer<String, Value> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));
            //ADI is avsc file which contain Avro Schema Format, you can find it in "./src/main/resources/avro/io/confluent/examples/clients/basicavro/"
            while (true) {
                final ConsumerRecords<String, Value> records = consumer.poll(Duration.ofMillis(100)); //pooling time in ms
                for (final ConsumerRecord<String, Value> record : records) {
                    ParallelTopicProcessing parallelTopicProcessing = new ParallelTopicProcessing(record);
                    parallelTopicProcessing.start();

                }
                }
            }
        }
    }



