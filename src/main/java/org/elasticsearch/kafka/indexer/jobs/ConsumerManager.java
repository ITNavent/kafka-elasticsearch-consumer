package org.elasticsearch.kafka.indexer.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.kafka.indexer.configuration.KafkaConsumerConfiguration;
import org.elasticsearch.kafka.indexer.service.IMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * @author marinapopova
 *         Apr 14, 2016
 */
@Service
public class ConsumerManager {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerManager.class);
    private static final String KAFKA_CONSUMER_THREAD_NAME_FORMAT = "kafka-elasticsearch-consumer-thread-%d";
    public static final String PROPERTY_SEPARATOR = ".";

    @Autowired
    private KafkaConsumerConfiguration kafkaConsumerConfiguration;
    
    @Autowired
    private ObjectFactory<IMessageHandler> messageHandlerObjectFactory;


    private ExecutorService consumersThreadPool = null;
    private List<ConsumerWorker> consumers = new ArrayList<>();
    private Properties kafkaProperties;

    private AtomicBoolean running = new AtomicBoolean(false);


	private void init() {
        logger.info("init() is starting ....");
        kafkaProperties = new Properties();
        kafkaProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConsumerConfiguration.getBrokers());
        kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerConfiguration.getGroup());
        kafkaProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaProperties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaConsumerConfiguration.getSessionTimeout());
        kafkaProperties.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, kafkaConsumerConfiguration.getMaxPartitionFetchBytes());
        kafkaProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        determineOffsetForAllPartitionsAndSeek(StartOptionParser.getStartOption(kafkaConsumerConfiguration.getStartOption()));
        initConsumers(kafkaConsumerConfiguration.getPartitions());
    }

    private void initConsumers(int consumerPoolCount) {
        logger.info("initConsumers() started, consumerPoolCount={}", consumerPoolCount);
        consumers = new ArrayList<>();
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(KAFKA_CONSUMER_THREAD_NAME_FORMAT).build();
        consumersThreadPool = Executors.newFixedThreadPool(consumerPoolCount, threadFactory);

        for (int consumerNumber = 0; consumerNumber < consumerPoolCount; consumerNumber++) {
			ConsumerWorker consumer = new ConsumerWorker(consumerNumber, kafkaConsumerConfiguration.getClient(),
					kafkaConsumerConfiguration.getTopic(), kafkaProperties,
					kafkaConsumerConfiguration.getPollInterval(), messageHandlerObjectFactory.getObject());
            consumers.add(consumer);
            consumersThreadPool.submit(consumer);
        }
    }

    private void shutdownConsumers() {
        logger.info("shutdownConsumers() started ....");

        if (consumers != null) {
            for (ConsumerWorker consumer : consumers) {
                consumer.shutdown();
            }
        }
        if (consumersThreadPool != null) {
            consumersThreadPool.shutdown();
            try {
                consumersThreadPool.awaitTermination(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.warn("Got InterruptedException while shutting down consumers, aborting");
            }
        }
        if (consumers != null) {
            consumers.forEach(consumer -> consumer.getPartitionOffsetMap()
                    .forEach((topicPartition, offset)
                            -> logger.info("Offset position during the shutdown for consumerId : {}, partition : {}, offset : {}", consumer.getConsumerId(), topicPartition.partition(), offset.offset())));
        }
        logger.info("shutdownConsumers() finished");
    }

    /**
     * Determines start offsets for kafka partitions and seek to that offsets
     * @param startOption start option
     */
    public void determineOffsetForAllPartitionsAndSeek(StartOption startOption) {
        logger.info("in determineOffsetForAllPartitionsAndSeek(): ");
        if (startOption == StartOption.RESTART) {
        	logger.info("startOption is empty or set to RESTART - consumers will start from RESTART for all partitions");
        	return;
        }

        Consumer<String, String> consumer = getConsumerInstance(kafkaProperties);
        consumer.subscribe(Arrays.asList(kafkaConsumerConfiguration.getTopic()));

        //Make init poll to get assigned partitions
        consumer.poll(kafkaConsumerConfiguration.getPollInterval());

        Set<TopicPartition> assignedTopicPartitions = consumer.assignment();
        Map<TopicPartition, Long> offsetsBeforeSeek = new HashMap<>();
        for (TopicPartition topicPartition : assignedTopicPartitions) {
            offsetsBeforeSeek.put(topicPartition, consumer.position(topicPartition));
        }

        switch (startOption) {
            case CUSTOM:
                if(kafkaConsumerConfiguration.getCustomOffsetsMap() == null) {
                    logger.error("Custom offset map is not defined . Consumers will start from RESTART for all partitions");
                    consumer.close();
                    return;
                	
                }
            	
                //apply custom start offset options to partitions from file
                if (kafkaConsumerConfiguration.getCustomOffsetsMap().size() == assignedTopicPartitions.size()) {
                    for (TopicPartition topicPartition : assignedTopicPartitions) {
                        Long startOffset = kafkaConsumerConfiguration.getCustomOffsetsMap().get(topicPartition.partition());
                        if (startOffset == null) {
                            logger.error("There is no custom start option for partition {}. Consumers will start from RESTART for all partitions", topicPartition.partition());
                            consumer.close();
                            return;
                        }

                        consumer.seek(topicPartition, startOffset);
                    }
                } else {
                    logger.error("Defined custom consumer start options has missed partitions. Expected {} partitions but was defined {}. Consumers will start from RESTART for all partitions",
                            assignedTopicPartitions.size(), kafkaConsumerConfiguration.getCustomOffsetsMap().size());
                    consumer.close();
                    return;
                }
                break;
            case EARLIEST:
                consumer.seekToBeginning(assignedTopicPartitions);
                break;
            case LATEST:
                consumer.seekToEnd(assignedTopicPartitions);
                break;
            default:
                consumer.close();
                return;
        }

        Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();
        assignedTopicPartitions.forEach(partition -> offsetsToCommit.put(partition, new OffsetAndMetadata(consumer.position(partition))));
        consumer.commitSync(offsetsToCommit);
        for (TopicPartition topicPartition : assignedTopicPartitions) {
            logger.info("Offset for partition: {} is moved from : {} to {} with startOption: {}",
                    topicPartition.partition(), offsetsBeforeSeek.get(topicPartition), consumer.position(topicPartition), startOption);
        }

        consumer.close();
    }

    public Consumer<String, String> getConsumerInstance(Properties properties) {
        return new KafkaConsumer<>(properties);
    }

    @PostConstruct
    public void postConstruct() {
        start();
    }

    @PreDestroy
    public void preDestroy() {
        stop();
    }

    synchronized public void start() {
        if (!running.getAndSet(true)) {
            init();
        } else {
            logger.warn("Already running");
        }
    }

    synchronized public void stop() {
        if (running.getAndSet(false)) {
            shutdownConsumers();
        } else {
            logger.warn("Already stopped");
        }
    }

    public static void extractAndSetKafkaProperties(Properties applicationProperties, Properties kafkaProperties, String kafkaPropertyPrefix) {
        if(applicationProperties != null && applicationProperties.size() >0){
            for(Map.Entry <Object,Object> currentPropertyEntry :applicationProperties.entrySet()){
                String propertyName = currentPropertyEntry.getKey().toString();
                if(StringUtils.isNotBlank(propertyName) && propertyName.contains(kafkaPropertyPrefix)){
                    String validKafkaConsumerProperty = propertyName.replace(kafkaPropertyPrefix, StringUtils.EMPTY);
                    kafkaProperties.put(validKafkaConsumerProperty, currentPropertyEntry.getValue());
                    logger.info("Adding kafka property with prefix: {}, key: {}, value: {}", kafkaPropertyPrefix, validKafkaConsumerProperty, currentPropertyEntry.getValue());
                }
            }
        }
    }

}
