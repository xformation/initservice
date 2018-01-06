/**
 * 
 */
package com.synectiks.schemas.utils;

import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.google.common.collect.Maps;
import com.synectiks.commons.constants.IDBConsts;
import com.synectiks.commons.utils.IUtils;

/**
 * @author Rajesh
 */
public interface DDBUtils {

	/**
	 * Method to check if table is empty, if id is auto generated
	 * @param client DynamoDB Document API client.
	 * @param tblName name of table to check.
	 * @param hashValue
	 * @return
	 */
	static boolean isTableEmpty(AmazonDynamoDB client, String tblName, String hashValue) throws Exception {
		return isTableEmpty(client, tblName, IDBConsts.Col_ID, hashValue);
	}

	/**
	 * Method to check if table is empty
	 * @param client DynamoDB Document API client.
	 * @param tblName name of table to check.
	 * @param key column name
	 * @param value column value 
	 * @return
	 */
	static boolean isTableEmpty(AmazonDynamoDB client, String tblName, String key, String value)
			throws Exception {
		if (!IUtils.isNullOrEmpty(tblName) && !IUtils.isNullOrEmpty(value)) {
			Map<String, AttributeValue> valMap = Maps.newHashMap();
			valMap.put(key, new AttributeValue(value));
			GetItemResult result = client.getItem(new GetItemRequest(tblName, valMap));
			if (!IUtils.isNull(result.getItem()) && !result.getItem().isEmpty()) {
				return false;
			}
		} else {
			throw new Exception("Table or id is null or empty.");
		}
		return true;
	}

	/**
	 * Check if there is no row with name admin
	 * @param mapper
	 * @param class1
	 * @return
	 */
	static boolean isTableEmpty(DynamoDBMapper mapper, Class<?> clazz) throws Exception {
		PaginatedScanList<?> res = mapper.scan(clazz, scanExpression(null, null));
		if (!IUtils.isNull(res) && !res.isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * Method to check if the any row exists with key and value
	 * @param mapper
	 * @param clazz
	 * @param key
	 * @param value
	 * @return
	 * @throws Exception
	 */
	static boolean isTableEmpty(DynamoDBMapper mapper, Class<?> clazz, String key, String value)
			throws Exception {
		if (!IUtils.isNullOrEmpty(key) && !IUtils.isNullOrEmpty(value)) {
			PaginatedScanList<?> res = mapper.scan(clazz, scanExpression(key, value));
			if (!IUtils.isNull(res) && !res.isEmpty()) {
				return false;
			}
		} else {
			throw new Exception("Key or value is null or empty.");
		}
		return true;
	}

	/**
	 * Method create a db scan expression to extract data.
	 * @param key
	 * @param value
	 * @return
	 */
	static DynamoDBScanExpression scanExpression(String key, String value) {
		DynamoDBScanExpression exp = new DynamoDBScanExpression();
		if (!IUtils.isNullOrEmpty(key) && !IUtils.isNullOrEmpty(value)) {
			exp.addFilterCondition(key, new Condition()
					.withComparisonOperator(ComparisonOperator.EQ)
					.withAttributeValueList(new AttributeValue().withS(value)));
		}
		return exp;
	}

}
