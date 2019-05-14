package com.synectiks.schemas.config;

import java.io.File;
import java.util.UUID;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.synectiks.commons.constants.IDBConsts;
import com.synectiks.commons.entities.SSMState;
import com.synectiks.commons.utils.IUtils;
import com.synectiks.schemas.entities.EntityClasses;
import com.synectiks.schemas.utils.DDBUtils;

/**
 * Class to create all tables in DynamoDB
 * @author Rajesh
 */
@Component
public class DynamoDBInit {

	private static Logger logger = LoggerFactory.getLogger(DynamoDBInit.class);

	@Value("${synecticks.states.file}")
	private String statesFileName;

	@Autowired
	private DynamoDBMapper dbMapper;

	@Autowired
	private AmazonDynamoDB dynamoDBClient;

	@Autowired
	private DynamoDB dynamoDB;

	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {

		logger.trace("Entering createDatabaseTablesIfNotExist()");
		EntityClasses[] entities = EntityClasses.values();
		final ProvisionedThroughput throughput = new ProvisionedThroughput(5L, 5L);
		for (EntityClasses cls : entities) {
			CreateTableRequest request = null;
			try {
				request = dbMapper.generateCreateTableRequest(cls.getCls())
						// .withKeySchema(new KeySchemaElement(IDBConsts.Col_ID,
						// KeyType.HASH))
						.withProvisionedThroughput(throughput);
				if (!IUtils.isNull(request.getGlobalSecondaryIndexes())) {
					request.getGlobalSecondaryIndexes().forEach(item -> {
						item.setProvisionedThroughput(throughput);
					});
				}
				logger.info("Table: {}", request.getTableName());
				DescribeTableResult result = dynamoDBClient
						.describeTable(request.getTableName());
				logger.info("Table status {}, {}, Row count {}", request.getTableName(),
						result.getTable().getTableStatus(),
						result.getTable().getItemCount());
			} catch (ResourceNotFoundException expectedException) {
				CreateTableResult result = dynamoDBClient.createTable(request);
				logger.info("Table creation triggered {}, {}", request.getTableName(),
						result.getTableDescription().getTableStatus());
			}
		}
		loadInitialStates();
	}

	/**
	 * Method to load states initial data from "statesJson.json" file
	 */
	private void loadInitialStates() {
		try {
			File file = DDBUtils.loadResourceFile(
					this.getClass().getClassLoader(), statesFileName);
			Table tblStates = dynamoDB.getTable(IDBConsts.Tbl_SSM_STATE);
			if (!IUtils.isNull(file) && file.exists() &&
					DDBUtils.isTableEmpty(dbMapper, SSMState.class)) {
				logger.info("States table is empty");
				insertRowsInTableFromFile(file, tblStates);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	/**
	 * Method to insert states information into table
	 * @param file
	 * @param tblStates
	 * @throws Exception
	 */
	private void insertRowsInTableFromFile(File file, Table tblStates) throws Exception {
		JSONObject json = new JSONObject(IUtils.readStringFromFile(file));
		JSONArray ssmIds = json.names();
		for (int x = 0; x < ssmIds.length(); x++) {
			String ssmId = ssmIds.getString(x);
			JSONArray jArr = json.optJSONArray(ssmId);
			for (int i = 0; i < jArr.length(); i++) {
				JSONObject jObj = jArr.getJSONObject(i);
				JSONArray names = jObj.names();
				Item state = new Item()
						.with(IDBConsts.Col_ID, UUID.randomUUID().toString());
				// Add machine name id
				state.with("ssmId", ssmId);
				for (int j = 0; j < names.length(); j++) {
					String key = names.getString(j);
					state.with(key, jObj.opt(key));
				}
				logger.info("Row: " + state);
				tblStates.putItem(state);
				tblStates.waitForActive();
			}
		}
	}

}
