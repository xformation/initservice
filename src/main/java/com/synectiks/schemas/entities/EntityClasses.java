/**
 * 
 */
package com.synectiks.schemas.entities;

import com.synectiks.commons.entities.Policy;
import com.synectiks.commons.entities.PolicyRuleResult;
import com.synectiks.commons.entities.Rule;
import com.synectiks.commons.entities.SSMachine;
import com.synectiks.commons.entities.SSMState;
import com.synectiks.commons.entities.demo.BillingAddress;
import com.synectiks.commons.entities.demo.Cart;
import com.synectiks.commons.entities.demo.CartItem;
import com.synectiks.commons.entities.demo.Customer;
import com.synectiks.commons.entities.demo.CustomerOrder;
import com.synectiks.commons.entities.demo.Product;
import com.synectiks.commons.entities.demo.ShippingAddress;
import com.synectiks.commons.entities.dynamodb.Entity;
import com.synectiks.commons.entities.dynamodb.Service;
import com.synectiks.commons.entities.dynamodb.Subscription;

/**
 * Enum to hold all entities which tabled needs to created in db
 * @author Rajesh
 */
public enum EntityClasses {

	// demo site entities.
	BillingAdd(BillingAddress.class),
	Cart(Cart.class),
	CartItem(CartItem.class),
	Customer(Customer.class),
	Order(CustomerOrder.class),
	Product(Product.class),
	ShippingAdd(ShippingAddress.class),
	//Category(Category.class),
	Service(Service.class),
	Ssm(SSMachine.class),
	States(SSMState.class),
	Subscription(Subscription.class),
	Policy(Policy.class),
	Rule(Rule.class),
	Result(PolicyRuleResult.class);

	private Class<? extends Entity> cls;
	
	private EntityClasses(Class<? extends Entity> cls) {
		this.cls = cls;
	}

	public Class<? extends Entity> getCls() {
		return cls;
	}

}
