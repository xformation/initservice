/**
 * 
 */
package com.synectiks.schemas.repositories;

import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.synectiks.commons.entities.search.ESEvent.EventType;
import com.synectiks.commons.publisher.ESEventPublisher;
import com.synectiks.commons.utils.IUtils;

/**
 * @author Rajesh
 */
public class DynamoDbRepository<T, ID extends Serializable> implements CrudRepository<T, ID> {

	private static final Logger logger = LoggerFactory.getLogger(DynamoDbRepository.class);
	private static final String MSG_NULL = "Input should not be null";

	@Autowired
	private DynamoDBMapper mapper;
	@Autowired
	private ESEventPublisher publisher;

	private Class<T> domainClass;

	public DynamoDbRepository(Class<T> clazz) {
		this.domainClass = clazz;
	}

	@Override
	public <S extends T> S save(S entity) {
		assertNotNull(MSG_NULL, entity);
		try {
			logger.trace("Saving entity '{}'", entity.getClass().getSimpleName());
			mapper.save(entity);
			// Call elastic to index entity
			publisher.fireEvent(EventType.CREATE, entity);
			return entity;
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
		}
		return null;
	}

	public <S extends T> Iterable<S> save(Iterable<S> entities) {
		assertNotNull(MSG_NULL, entities);
		try {
			mapper.batchSave(entities);
			logger.trace("Saved entitie(s)");
			// Call elastic to index entity
			publisher.fireEvent(EventType.CREATE, entities);
			return entities;
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
		}
		return null;
	}

	public T findOne(ID id) {
		assertNotNull(MSG_NULL, id);
		logger.trace("Loading id {}", id);
		try {
			return mapper.load(domainClass, id);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
		}
		return null;
	}

	public boolean exists(ID id) {
		return (!IUtils.isNull(findOne(id)));
	}

	@Override
	public Iterable<T> findAll() {
		logger.trace("findAll");
		try {
			return scan(new DynamoDBScanExpression());
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll(Iterable<ID> ids) {
		assertNotNull(MSG_NULL, ids);
		logger.trace("findAll ids");
		Map<Class<?>, List<KeyPair>> map = new HashMap<>();
		List<KeyPair> keyPairs = new ArrayList<>();
		for (ID id : ids) {
			keyPairs.add(new KeyPair().withHashKey(id));
		}
		map.put(domainClass, keyPairs);
		DynamoDBTable table = domainClass.getAnnotation(DynamoDBTable.class);
		Map<String, List<Object>> resMap = mapper.batchLoad(keyPairs);
		String tblName = null;
		if (!IUtils.isNull(table)) {
			tblName = table.tableName();
		}
		if (!IUtils.isNullOrEmpty(tblName)){
			return (List<T>) resMap.get(tblName);
		} else {
			if (!IUtils.isNull(resMap) && resMap.size() > 0) {
				for (Entry<String, List<Object>> entry: resMap.entrySet()) {
					return (List<T>) entry.getValue();
				}
			}
		}
		return null;
	}

	@Override
	public Optional<T> findById(ID id) {
		assertNotNull(MSG_NULL, id);
		return Optional.ofNullable(findOne(id));
	}

	@Override
	public long count() {
		return 0;
	}

	public void delete(ID id) {
		assertNotNull(MSG_NULL, id);
		try {
			T entity = findOne(id);
			delete(entity);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
		}
	}

	public void delete(T entity) {
		assertNotNull(MSG_NULL, entity);
		try {
			mapper.delete(entity);
			// Call elastic to remove indexed entity
			publisher.fireEvent(EventType.DELETE, entity);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
		}
	}

	public void delete(Iterable<? extends T> entities) {
		assertNotNull(MSG_NULL, entities);
		try {
			mapper.batchDelete(entities);
			// Call elastic to remove indexed entity
			publisher.fireEvent(EventType.DELETE, entities);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
		}
	}

	@Override
	public void deleteAll() {
		try {
			Iterable<T> entities = findAll();
			mapper.batchDelete(entities);
			// Call elastic to remove indexed entity
			publisher.fireEvent(EventType.DELETE, entities);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
		}
	}

	/**
	 * Method to scan dynamodb with scan expression
	 * @param exp
	 * @return
	 */
	public Iterable<T> scan(DynamoDBScanExpression exp) {
		assertNotNull(MSG_NULL, exp);
		logger.info("Cls: " + domainClass);
		try {
			return mapper.scan(domainClass, exp);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
		}
		return null;
	}

	/**
	 * Method to scan dynamodb with key value pair.
	 * @param key must be part of primary or secondary index
	 * @param val
	 * @return
	 */
	public Iterable<T> findByKeyValue(String key, String val) {
		assertNotNull(MSG_NULL, key);
		logger.info("findByKeyValue: " + key + ", " + val);
		DynamoDBScanExpression exp = new DynamoDBScanExpression();
		exp.addFilterCondition(key, new Condition()
				.withComparisonOperator(ComparisonOperator.EQ)
				.withAttributeValueList(new AttributeValue().withS(val)));
		return scan(exp);
	}

	@Override
	public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
		return save(entities);
	}

	@Override
	public boolean existsById(ID id) {
		return exists(id);
	}

	@Override
	public Iterable<T> findAllById(Iterable<ID> ids) {
		return findAll(ids);
	}

	@Override
	public void deleteById(ID id) {
		this.delete(id);
	}

	@Override
	public void deleteAll(Iterable<? extends T> entities) {
		this.deleteAll(entities);
	}

}
