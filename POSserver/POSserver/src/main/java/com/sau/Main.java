package com.sau;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.LocalDateTime;
import java.util.*;

public class Main {
    public static Properties properties = new Properties();

    public static void main(String[] args) {
        System.out.printf("POS Server has started.");

        String bootstrapServers = "127.0.0.1:9092";
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Timer timer = new Timer();
        timer.schedule(new POSDevice(), 0, 1000);
    }

    static class POSDevice extends TimerTask {

        // User_id is coming from POS device in real life
        List<Integer> userids = Arrays.asList(7369, 7499, 7521, 7566, 7654, 7698, 7782, 7788, 7839, 7844, 7876, 7900, 7902, 7934);
        List<String> goods = Arrays.asList(
                "Macaroni, Food, 2, 35.45", "TV, Appliances, 1, 1280.45", "Cinema, Entertainment, 2, 80.00",
                "Tea, Food, 2, 310.90", "Fridge, Appliances, 2, 4480.00", "Theatre, Entertainment, 1, 250.00",
                "Banana, Food, 2, 180.00", "Biscuit, Food, 2, 90.00", "Chocolate, Food, 2, 26.90", "Wrench, Hardware, 3, 345.00"
                );
        public void run() {
            // create the producer
            KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
            Random r = new Random();
            int deptno = (r.nextInt(4)+1)*10;
            int i = r.nextInt(14);
            int j = r.nextInt(10);

            LocalDateTime currentDateTime = LocalDateTime.now();
            String record = userids.get(i) + ", " + currentDateTime + ", " + goods.get(j);

            System.out.println(record);

            // create a POS device record
            // <user_id, date_time, description, type, count, payment>

            ProducerRecord<String, String> producerRecord =
                    new ProducerRecord<>(Topic.getTopic(deptno), record);

            // send data - asynchronous
            producer.send(producerRecord);

            // flush data - synchronous
            producer.flush();
            // flush and close producer
            producer.close();


        }
    }
}