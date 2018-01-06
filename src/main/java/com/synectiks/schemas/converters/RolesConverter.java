/**
 * 
 */
package com.synectiks.schemas.converters;

import com.synectiks.commons.converters.StringArrayConverter;
import com.synectiks.schemas.entities.Role;

/**
 * @author Rajesh
 */
public class RolesConverter extends StringArrayConverter<Role>/*OneToManyConverter<Role>*/ {

	public RolesConverter() {
		super(Role.class);
	}

}
