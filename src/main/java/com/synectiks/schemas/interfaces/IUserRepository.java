/**
 * 
 */
package com.synectiks.schemas.interfaces;

import com.synectiks.schemas.entities.User;

/**
 * @author Rajesh
 */
public interface IUserRepository {

	User findById(String id);
	User findByUsername(String username);
}
