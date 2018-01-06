/**
 * 
 */
package com.synectiks.schemas.entities;

import com.synectiks.commons.entities.SSMState;
import com.synectiks.commons.entities.dynamodb.Entity;
import com.synectiks.commons.entities.dynamodb.Service;
import com.synectiks.commons.entities.dynamodb.Subscription;

/**
 * Enum to hold all entities which tabled needs to created in db
 * @author Rajesh
 */
public enum EntityClasses {

	//Category(Category.class),
	Service(Service.class),
	States(SSMState.class),
	Subscription(Subscription.class);

	private Class<? extends Entity> cls;
	
	private EntityClasses(Class<? extends Entity> cls) {
		this.cls = cls;
	}

	public Class<? extends Entity> getCls() {
		return cls;
	}

}
