/**
 * 
 */
package com.synectiks.schemas.converters;

import com.synectiks.commons.converters.StringConverter;
import com.synectiks.schemas.entities.Permission;

/**
 * @author Rajesh
 */
public class PermissionConverter extends StringConverter<Permission>/*OneToManyConverter<Permission>*/ {

	/*@Autowired
	private PermissionRepository repository;*/

	public PermissionConverter() {
		super(Permission.class);
	}

	/*@Override
	protected DynamoDbRepository<Permission, String> getRepository() {
		if (IUtils.isNull(repository)) {
			repository = ContextProvider.getBean(PermissionRepository.class);
		}
		return repository;
	}*/

}
