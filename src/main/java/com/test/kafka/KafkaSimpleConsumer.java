package com.test.kafka;

import com.test.constant.KafkaConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 * @date 2020/7/26 16:28:00
 * @description 消费者
 */
@Slf4j
@Component
public class KafkaSimpleConsumer {

    // 简单消费者
    /*@KafkaListener(topics = KafkaConstants.TOPIC_SIMPLE)
    public void consumer1(String msgData) {
        log.info("consumer, topic= {}, content = {}", KafkaConstants.TOPIC_SIMPLE, msgData);
//        ack.acknowledge();

        *//*
         * 如果需要手工提交异步 consumer.commitSync();
         * 手工同步提交 consumer.commitAsync()
         *//*
    }*/

    @KafkaListener(topics = KafkaConstants.TOPIC_SIMPLE)
//    @KafkaListener(topics = KafkaConstants.TOPIC_SIMPLE, topicPartitions = { @TopicPartition(topic = KafkaConstants.TOPIC_SIMPLE, partitions = { "0" }) })
    public void consumer1(ConsumerRecord<?, ?> record) {
        log.info("consumer1 kafka的topic: {}", record.topic());
        log.info("consumer1 kafka的key: {}", record.key());
        log.info("consumer1 kafka的value: {}", record.value().toString());
        log.info("consumer1 kafka的partition: {}", record.partition());
    }

//    @KafkaListener(topics = KafkaConstants.TOPIC_SIMPLE, topicPartitions = { @TopicPartition(topic = KafkaConstants.TOPIC_SIMPLE, partitions = { "1" }) })
//    @KafkaListener(topics = KafkaConstants.TOPIC_SIMPLE)
    public void consumer2(ConsumerRecord<?, ?> record) {
        log.info("consumer2 kafka的topic: {}", record.topic());
        log.info("consumer2 kafka的key: {}", record.key());
        log.info("consumer2 kafka的value: {}", record.value().toString());
        log.info("consumer2 kafka的partition: {}", record.partition());
    }

//    @KafkaListener(topics = KafkaConstants.TOPIC_SIMPLE, topicPartitions = { @TopicPartition(topic = KafkaConstants.TOPIC_SIMPLE, partitions = { "2" }) })
    public void consumer3(ConsumerRecord<?, ?> record) {
        log.info("consumer3 kafka的topic: {}", record.topic());
        log.info("consumer3 kafka的key: {}", record.key());
        log.info("consumer3 kafka的value: {}", record.value().toString());
        log.info("consumer3 kafka的partition: {}", record.partition());
    }
}
