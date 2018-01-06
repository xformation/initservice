package com.synectiks.schemas.config;

import java.io.File;
import java.net.URL;
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
import com.synectiks.schemas.entities.Permission;
import com.synectiks.schemas.entities.Role;
import com.synectiks.schemas.entities.User;
import com.synectiks.schemas.repositories.PermissionRepository;
import com.synectiks.schemas.repositories.RoleRepository;
import com.synectiks.schemas.repositories.UserRepository;
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

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private RoleRepository roleRepo;
	@Autowired
	private PermissionRepository permRepo;

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
		loadInitialAuthData();
		loadInitialStates();
	}

	/**
	 * Method to load states initial data from "statesJson.json" file
	 */
	private void loadInitialStates() {
		try {
			//ClassPathResource res = new ClassPathResource("statesJson.json");
			URL res = this.getClass().getClassLoader().getResource(statesFileName);
			File file = new File(res.getPath());
			Table tblStates = dynamoDB.getTable(IDBConsts.Tbl_SSM_STATE);
			logger.info("Path: " + res.getPath() + ", file: " + file.exists());
			if (file.exists() && DDBUtils.isTableEmpty(dbMapper, SSMState.class)) {
				logger.info("States table is empty");
				JSONArray jArr = new JSONArray(IUtils.readStringFromFile(file));
				for (int i = 0; i < jArr.length(); i++) {
					JSONObject jObj = jArr.getJSONObject(i);
					JSONArray names = jObj.names();
					Item state = new Item()
							.with(IDBConsts.Col_ID, UUID.randomUUID().toString());
					for (int j = 0; j < names.length(); j++) {
						String key = names.getString(j);
						state.with(key, jObj.opt(key));
					}
					logger.info("Row: " + state);
					tblStates.putItem(state);
					tblStates.waitForActive();
				}
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	/**
	 * Method to check and load initial entry data.
	 */
	private void loadInitialAuthData() {
		try {
			Permission perm = null;
			Role role = null;
			User user = null;
			if (!IUtils.isNull(permRepo)) {
				logger.info("Size of permissions: " + permRepo.count());
				if (permRepo.count() == 0) {
					perm = DDBUtils.createAdminPermission();
					perm = permRepo.save(perm);
				}
				logger.info("Permissions: " + perm);
			}
			if (!IUtils.isNull(roleRepo)) {
				logger.info("Size of roles: " + roleRepo.count());
				if (roleRepo.count() == 0) {
					role = DDBUtils.createAdminRole(perm);
					role = roleRepo.save(role);
				}
				logger.info("Role: " + role);
			}
			if (!IUtils.isNull(userRepo)) {
				logger.info("Size of users: " + userRepo.count());
				if (userRepo.count() == 0) {
					user = DDBUtils.createAdminUser(role);
					user = userRepo.save(user);
				}
				logger.info("User: " + user);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
	}

}
