package com.thinkbiganalytics.nifi.kafka_stream.api;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class BasicConsumerExample {

    public static void main(String[] args) {
            Properties props = new Properties();
            props.put("auto.offset.reset", "earliest");
            props.put("bootstrap.servers", "kylo-demo:2181");
            props.put("group.id", "test");
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "1000");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            Map<String, List<PartitionInfo>> topicList = consumer.listTopics();
            List<PartitionInfo> partitionInfo = consumer.partitionsFor("test-topic-matt");
            Vector<TopicPartition> partitions = new Vector<>();
            for (PartitionInfo info : partitionInfo) {
                System.out.println(info.topic());
                partitions.add(new TopicPartition(info.topic(), info.partition()));
            }
            consumer.assign(partitions);

            //consumer.subscribe(Arrays.asList("test-topic-matt"));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records)
                    System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
            }
        }

}