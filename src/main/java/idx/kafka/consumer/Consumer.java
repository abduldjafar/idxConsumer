package idx.kafka.consumer;

import idx.kafka.services.ParallelTopicProcessing;
import idx.kafka.services.TopicProcessing;
import io.confluent.connect.jms.Value;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import idx.kafka.config.Config;

public class Consumer {


    @SuppressWarnings("InfiniteLoopStatement")

    public static void main(final String[] args) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        if (args.length != 3){
            System.out.println("please use : java -jar app.jar file_config.config topic_for_consumer_name url_fileserver");
            System.exit(0);
        }

        String config_file = args[0];
        String topic = args[1];
        String url = args[2];

        Config config = new Config();
        Properties props = config.Kafka(config_file);

        try (final KafkaConsumer<String, Value> consumer = new KafkaConsumer<>(props)) {

            consumer.subscribe(Collections.singletonList(topic));
            while (true) {
                final ConsumerRecords<String, Value> records = consumer.poll(Duration.ofMillis(100)); //pooling time in ms
                for (final ConsumerRecord<String, Value> record : records) {
                    ParallelTopicProcessing parallelTopicProcessing = new ParallelTopicProcessing(record,url);
                    parallelTopicProcessing.start();
                    //TopicProcessing.run(record,url);


                }
                }
            }
        }
    }



