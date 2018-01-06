/**
 * 
 */
package com.synectiks.schemas.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.synectiks.schemas.entities.User;
import com.synectiks.schemas.interfaces.IUserRepository;

/**
 * @author Rajesh
 */
@Repository
public interface UserRepository extends CrudRepository<User, String>,
		IUserRepository {

	User findById(long id);
	User findByUsername(String username);

}
