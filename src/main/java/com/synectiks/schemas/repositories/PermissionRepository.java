/**
 * 
 */
package com.synectiks.schemas.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.synectiks.schemas.entities.Permission;

/**
 * @author Rajesh
 */
@Repository
public interface PermissionRepository extends CrudRepository<Permission, String> {

}
