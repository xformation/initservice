/**
 * 
 */
package com.synectiks.schemas.converters;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.synectiks.commons.constants.IDBConsts;
import com.synectiks.commons.entities.dynamodb.Entity;
import com.synectiks.commons.utils.IUtils;
import com.synectiks.schemas.repositories.DynamoDbRepository;
import com.synectiks.schemas.utils.DDBUtils;

/**
 * @author Rajesh
 * @param <T>
 *
 */
public abstract class OneToManyConverter<T extends Entity>
		implements DynamoDBTypeConverter<String, List<T>> {

	private static final Logger logger = LoggerFactory.getLogger(OneToManyConverter.class);

	@Autowired
	private DynamoDBMapper dbMapper;

	private Class<T> cls;

	public OneToManyConverter(Class<T> cls) {
		this.cls = cls;
	}

	protected abstract DynamoDbRepository<T, String> getRepository();

	@Override
	public String convert(List<T> objects) {
		StringBuilder sb = new StringBuilder();
		if (!IUtils.isNull(objects)) {
			objects.forEach(obj -> {
				sb.append(sb.length() > 0 ? ", " : "");
				sb.append(obj.getId());
			});
		} else {
			return null;
		}
		return sb.toString();
	}

	@Override
	public List<T> unconvert(String object) {
		List<String> list = IUtils.getListFromString(object, null);
		if (!IUtils.isNull(list) && !list.isEmpty()) {
			List<T> res = new ArrayList<>();
			list.forEach(id -> {
				if (!IUtils.isNull(getRepository())) {
					res.add(getRepository().findById(id).orElse(null));
				} else {
					res.add(loadEntityById(id));
				}
			});
			return res;
		}
		return null;
	}

	/**
	 * Method to load entity from dynamo db.
	 * @param id
	 * @return
	 */
	private T loadEntityById(String id) {
		if (!IUtils.isNullOrEmpty(id)) {
			if (!IUtils.isNull(dbMapper)) {
				dbMapper.scan(cls, DDBUtils.scanExpression(IDBConsts.Col_ID, id));
			} else {
				logger.error("dbMapper is null");
			}
		}
		return null;
	}

}
