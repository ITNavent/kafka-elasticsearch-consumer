package org.elasticsearch.kafka.indexer.configuration;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("kafka.consumer")
public class KafkaConsumerConfiguration {
	
    @NotNull
    private String topic;
    @NotNull
    private String group;
    @NotNull
    private String client;
    @NotNull
    private String brokers;

    private String startOption = "RESTART";

    private int partitions = 20;
    
    private int sessionTimeout = 10000 ;
    // interval in MS to poll Kafka brokers for messages, in case there were no messages during the previous interval
    private long pollInterval = 10000;
    // Max number of bytes to fetch in one poll request PER partition
    // default is 1M = 1048576
    private int maxPartitionFetchBytes = 1048576;
    
	private Map<Integer, Long> customOffsetsMap;

	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getClient() {
		return client;
	}
	public void setClient(String client) {
		this.client = client;
	}
	public String getBrokers() {
		return brokers;
	}
	public void setBrokers(String brokers) {
		this.brokers = brokers;
	}
	public int getPartitions() {
		return partitions;
	}
	public void setPartitions(int partitions) {
		this.partitions = partitions;
	}
	public int getSessionTimeout() {
		return sessionTimeout;
	}
	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}
	public long getPollInterval() {
		return pollInterval;
	}
	public void setPollInterval(long pollInterval) {
		this.pollInterval = pollInterval;
	}
	public int getMaxPartitionFetchBytes() {
		return maxPartitionFetchBytes;
	}
	public void setMaxPartitionFetchBytes(int maxPartitionFetchBytes) {
		this.maxPartitionFetchBytes = maxPartitionFetchBytes;
	}
	public Map<Integer, Long> getCustomOffsetsMap() {
		return customOffsetsMap;
	}
	public void setCustomOffsetsMap(Map<Integer, Long> customOffsetsMap) {
		this.customOffsetsMap = customOffsetsMap;
	}
	public String getStartOption() {
		return startOption;
	}
	public void setStartOption(String startOption) {
		this.startOption = startOption;
	}

}
