package org.elasticsearch.kafka.indexer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by dhyan on 1/28/16.
 */
@SpringBootApplication
public class KafkaESIndexerProcess {
    private static final Logger logger = LoggerFactory.getLogger(KafkaESIndexerProcess.class);
    public static void main(String[] args) throws Exception {
        logger.info("Starting KafkaESIndexerProcess  ");
        SpringApplication.run(KafkaESIndexerProcess.class, args);
        logger.info("KafkaESIndexerProcess is started OK");
    }
}
