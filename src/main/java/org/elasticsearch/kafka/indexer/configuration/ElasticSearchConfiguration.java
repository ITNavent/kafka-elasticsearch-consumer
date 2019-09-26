package org.elasticsearch.kafka.indexer.configuration;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("elasticsearch")
public class ElasticSearchConfiguration {
    @NotNull
    private String clusterName;
    @NotNull
    private List<String> hosts;
	@NotNull
	private String indexName;

	private String indexType = "kafkaESType";

    // sleep time in ms between attempts to index data into ES again
    private int indexingRetrySleepTime = 10000;
    // number of times to try to index data into ES if ES cluster is not reachable
    private int numberOfIndexingRetryAttempts = 2;

    
    public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	public List<String> getHosts() {
		return hosts;
	}
	public void setHosts(List<String> hosts) {
		this.hosts = hosts;
	}
	public int getIndexingRetrySleepTime() {
		return indexingRetrySleepTime;
	}
	public void setIndexingRetrySleepTime(int indexingRetrySleepTime) {
		this.indexingRetrySleepTime = indexingRetrySleepTime;
	}
	public int getNumberOfIndexingRetryAttempts() {
		return numberOfIndexingRetryAttempts;
	}
	public void setNumberOfIndexingRetryAttempts(int numberOfIndexingRetryAttempts) {
		this.numberOfIndexingRetryAttempts = numberOfIndexingRetryAttempts;
	}
	public String getIndexName() {
		return indexName;
	}
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
	public String getIndexType() {
		return indexType;
	}
	public void setIndexType(String indexType) {
		this.indexType = indexType;
	}

}
