package core;

public class User {
	private String username;
	private String password;
	private String role;
	private String group;
	private String email;
	private boolean isActive;

	/**
	 * Creates a user
	 * @param username
	 * @param group
	 * @param role
	 * @param isActive
	 * @param email
	 * @param password
	 */
	public User(String username, String group, String role, boolean isActive, String email, String password){
		this.username = username;
		this.password = password;
		this.group = group;
		this.role = role;
		this.isActive = isActive;
		this.email = email;
	}
	/**
	 * Gets the username.
	 * @return
	 */
	public String getUsername(){
		return username;
	}

	/**
	 * Gets the password.
	 * @return
	 */
	public String getPassword(){
		return password;
	}

	/**
	 * Gets the role.
	 * @return
	 */
	public String getRole(){
		return role;
	}
	/**
	 * Gets the group.
	 * @return
	 */
	public String getGroup(){
		return group;
	}
	/**
	 * Checks if the user is active.
	 * @return true if the user is active.
	 */
	public boolean isActive(){
		return isActive;
	}

	/**
	 * Gets the user email address.
	 * @return
	 */
	public String getEmail(){
		return email;
	}
}
