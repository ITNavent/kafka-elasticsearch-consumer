/**
  * @author marinapopova
  * Feb 24, 2016
 */
package org.elasticsearch.kafka.indexer.service.impl.examples;

import org.elasticsearch.kafka.indexer.configuration.ElasticSearchConfiguration;
import org.elasticsearch.kafka.indexer.exception.IndexerESNotRecoverableException;
import org.elasticsearch.kafka.indexer.exception.IndexerESRecoverableException;
import org.elasticsearch.kafka.indexer.service.ElasticSearchBatchService;
import org.elasticsearch.kafka.indexer.service.IMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * 
 * This is an example of a customized Message Handler - by implementing IMessageHandler interface
 * and using the ElasticSearchBatchService to delegate most of the non-customized logic
 *
 */
@Service
@Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
public class SimpleMessageHandlerImpl implements IMessageHandler {

	@Autowired
	private ElasticSearchBatchService elasticSearchBatchService = null;
	
	@Autowired
	private ElasticSearchConfiguration esConfiguration;

	@Override
	public String transformMessage(String inputMessage, Long offset) throws Exception {
		// do not do any transformations for this scenario - just return the message as is
		return inputMessage;
	}

	@Override
	public void addMessageToBatch(String inputMessage) throws Exception {
		String eventUUID = null; // we don't need a UUID for this simple scenario
		String routingValue = null; // we don't need routing for this simple scenario		
		elasticSearchBatchService.addEventToBulkRequest(
				inputMessage, esConfiguration.getIndexName(), esConfiguration.getIndexType(), eventUUID, routingValue);
	}

	@Override
	public void postToElasticSearch() throws InterruptedException, IndexerESRecoverableException, IndexerESNotRecoverableException {
		elasticSearchBatchService.postToElasticSearch();
	}
}
