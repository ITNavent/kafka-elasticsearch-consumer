# Welcome to the kafka-elasticsearch-standalone-consumer wiki!

NOTE: This is a forked version from [kafka-elasticsearch-standalone-consumer](https://github.com/ppine7/kafka-elasticsearch-standalone-consumer).

Main changes from original version:
* Change to SpringBoot application
* Change configuration method

## Architecture of the kafka-elasticsearch-standalone-consumer [indexer]

![](img/IndexerV2Design.jpg)


# Introduction

**Kafka Standalone Consumer [Indexer] will read messages from Kafka, in batches, process and bulk-index them into ElasticSearch.**


# How to use ? 

### Download lastest version

1. Download [latest version from nexus](http://http//nexus.navent.biz:8081/nexus/service/local/artifact/maven/redirect?r=releases&g=org.elasticsearch.kafka.indexer&a=kafka-elasticsearch-consumer&v=LATEST).

2. Configure **kafka.consumer** properties on application.yml or application.properties, more details inside a file. 

3. Configure **elasticsearch** properties on application.yml or application.properties, more details inside a file.

4. run the app: 
	
	`java -jar kafka-elasticsearch-consumer.jar`

# Versions

* Kafka Version: 1.0.x

* ElasticSearch: 6.6.0

* JDK 1.8

* Spring Boot 2.1.6

# Configuration

Indexer application can be configured by SpringBoot [application.yml](src/main/resources/application.yml) or [application.properties](src/main/resources/application.properties) you have to adjust properties for your env:

Logging properties are specified in the logback.xml file - you have to adjust properties for your env:
[logback-spring.xml](src/main/resources/logback-spring.xml).
You can specify your own logback config file via `-Dlogback.configurationFile=/abs-path/your-logback.xml` property

## Requeried properties
You must configure the following properties

### Kafka consumer config
**kafka.consumer.topic** with kafka topic name

**kafka.consumer.group** with kafka group id

**kafka.consumer.client** with kafka client name

**kafka.consumer.brokers** with kafka broker list

### ElasticSearch config
**elasticsearch.hosts** with elasticsearch hosts list

**elasticsearch.cluster-name** with elasticsearch cluster name

**elasticsearch.index-name** with elasticsearch index name (on lowercase)

## Aditionals properties
You can also configure:

**kafka.consumer.start-option** The value of this property can be `RESTART`, `EARLIEST`, `LATEST`. By default `RESTART` option is used for all partitions.

**kafka.consumer.partitions** kafka consumer partitions are threads to consume the topic. By default 20

**kafka.consumer.session-timeout** The timeout used to detect consumer failures in ms. By default 10000

**kafka.consumer.poll-interval** Interval in MS to poll Kafka brokers for messages. By default 10000

**kafka.consumer.max-partition-fetch-bytes** Max number of bytes to fetch in one poll request PER partition. By default 1048576


**elasticsearch.indexing-retry-sleep-time** sleep time in ms between attempts to index data into ES again. By default 10000

**elasticsearch.number-of-indexing-retry-attempts** number of times to try to index data into ES if ES cluster is not reachable. By default 2

# License

kafka-elasticsearch-standalone-consumer

	Licensed under the Apache License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may obtain
	a copy of the License at

	     http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an
	"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
	KIND, either express or implied.  See the License for the
	specific language governing permissions and limitations
	under the License.

# Contributors

 - [Krishna Raj](https://github.com/reachkrishnaraj)
 - [Marina Popova](https://github.com/ppine7)
 - [Dhyan Muralidharan](https://github.com/dhyan-yottaa)
 - [Andriy Pyshchyk](https://github.com/apysh)
 - [Vitalii Chernyak](https://github.com/merlin-zaraza)
 
# Editors

 - [Socrates Clinis](https://github.com/sclinis)
