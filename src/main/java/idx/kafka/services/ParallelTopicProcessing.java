package idx.kafka.services;


import io.confluent.connect.jms.Value;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;

public class ParallelTopicProcessing implements Runnable{
    private Thread t;
    private ConsumerRecord<String, Value> record;

    public ParallelTopicProcessing(ConsumerRecord<String, Value> record) {
        this.record = record;
    }


    public void run() {
        try {
            TopicProcessing.run(this.record);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start () {
        if (t == null) {
            t = new Thread (this);
            t.start ();
        }
    }
}