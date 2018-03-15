/**
 * 
 */
package com.synectiks.schemas.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

/**
 * @author Rajesh
 */
@Configuration
public class DynamoDbConfig {

	private static final Logger logger = LoggerFactory.getLogger(DynamoDbConfig.class);

	@Value("${amazon.dynamodb.endpoint}")
	private String dynamoDbEndpoint;

	@Bean
	public AmazonDynamoDB amazonDynamoDB() {
		AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
				.withEndpointConfiguration(new EndpointConfiguration(dynamoDbEndpoint,
						Regions.EU_WEST_1.getName()))
				.build();
		logger.debug("DynamoDB client initialized");
		return amazonDynamoDB;
	}

	@Bean
	public DynamoDBMapper dynamoDbMapper(AmazonDynamoDB amazonDynamoDB) {
		logger.trace("Entering dynamoDbMapper()");
		return new DynamoDBMapper(amazonDynamoDB);
	}

}
