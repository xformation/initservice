/**
 * 
 */
package com.synectiks.schemas.entities;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.synectiks.commons.constants.IConsts;
import com.synectiks.commons.constants.IDBConsts;

/**
 * @author Rajesh
 */
@Entity
@Table(name = IDBConsts.Tbl_USERS)
public class User extends PSqlEntity {

	private static final long serialVersionUID = 2864064075933577751L;
	public static User ADMIN = create(IConsts.ADMIN, Arrays.asList(Role.ROLE_ADMIN));
	public static String ADMIN_UUID = "7afb71b8-2db0-4fc0-b66f-47f34d10a5a3";

	public static enum Types {
		CUSTOMER, USER, SUPER;
	}

	private Types type;
	private String username;
	private String password;
	private boolean active = true;
	private String email;
	@OneToMany(targetEntity = Role.class, fetch = FetchType.LAZY)
	private List<Role> roles;

	public User() {
		super();
	}

	public User(String username) {
		this();
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public Types getType() {
		return type;
	}

	public void setType(Types type) {
		this.type = type;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		builder.append("\"active\":").append(active).append(", ");
		if (type != null)
			builder.append("\"type\":\"").append(type).append("\", ");
		if (username != null)
			builder.append("\"username\":\"").append(username).append("\", ");
		if (password != null)
			builder.append("\"password\":\"").append(password).append("\", ");
		if (email != null)
			builder.append("\"email\":\"").append(email).append("\", ");
		if (roles != null)
			builder.append("\"roles\":").append(roles);
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Method to create a user object with username and roles
	 * @param username
	 * @param asList
	 * @return
	 */
	private static User create(String username, List<Role> asList) {
		User user = new User();
		user.setUsername(username);
		user.setType(Types.SUPER);
		user.setRoles(asList);
		return user;
	}

}
