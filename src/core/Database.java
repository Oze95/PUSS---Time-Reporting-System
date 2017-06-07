package core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Database {
	private Connection conn;
	private static Database instance;
	private static String databaseServerAddress = "vm23.cs.lth.se";
	private static String database = "puss1602hbg";				// the database to use, i.e. default schema
	private static String databaseUser = "puss1602hbg";				// database login user
	private static String databasePassword = "hq24jkpf";				// database login password

	private Database(){
		try {
			Class.forName("com.mysql.jdbc.Driver");  //Necessary on Windows computers
			conn = DriverManager.getConnection("jdbc:mysql://" + databaseServerAddress + "/" + 
					database + "?useSSL=false&user=" + databaseUser + "&password=" + databasePassword);
		} catch (SQLException ex) {
			printSQLError(ex);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Returns unique instance of database object using the Singleton design pattern.
	 * @return the unique instance of the database object.
	 */
	public static Database getInstance() {
		if (instance == null) {
			instance = new Database();
		}
		return instance;
	}

	/**
	 * Used to handle SQLExceptions
	 * @param caught SQLException
	 */
	protected void printSQLError(SQLException ex) {
		System.out.println("SQLException: " + ex.getMessage());
		System.out.println("SQLState: " + ex.getSQLState());
		System.out.println("VendorError: " + ex.getErrorCode());
	}

	/**
	 * Checks if there is a user with the specific credentials
	 * @param username
	 * @param password
	 * @return boolean
	 */
	public boolean checkUserCredentials(String username, String password) {
		PreparedStatement ps = null;

		try {
			String sql = "select * from users where binary username=? and binary password=? and active=1";
			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, password);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				return true;
		} catch (SQLException e) {
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Gets all users from the database
	 * @return List<User>
	 * Used to fetch a list containing all the users from the database.
	 * 
	 */
	public List<User> getAllUsers(){
		List<User> userList = new ArrayList<User>();
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement("select * from users");
			ResultSet rs = ps.executeQuery();

			while (rs.next())
				userList.add(new User(rs.getString("username"), rs.getString("projectgroup"), rs.getString("role"), rs.getString("active").equals("1"), rs.getString("email"), rs.getString("password")));
		} catch (SQLException e) {
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}
		return userList;
	}

	/**
	 * Gets the groupMembers from the specific group
	 * @param group
	 * @return List<User>
	 */
	public List<User> getGroupMembers(String group) {
		List<User> userList = new ArrayList<User>();
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement("select * from users where projectgroup=?");
			ps.setString(1, group);
			ResultSet rs = ps.executeQuery();

			while (rs.next())
				userList.add(new User(rs.getString("username"), rs.getString("projectgroup"), rs.getString("role"), rs.getString("active").equals("1"), rs.getString("email"), rs.getString("password")));
		} catch (SQLException e) {
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}
		return userList;
	}

	/**
	 * Gets the User with the specific userName
	 * @param username
	 * @return User user
	 */
	public User getUser(String username){
		User user = null;
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement("select * from users where username=?");
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();

			if (rs.next())
				user = new User(rs.getString("username"), rs.getString("projectgroup"), rs.getString("role"), rs.getString("active").equals("1"), rs.getString("email"), rs.getString("password"));
		} catch (SQLException e) {
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}
		return user;
	}

	/**
	 * Used to check if the user is active.
	 * @param username
	 * @return boolean
	 */
	public boolean isUserActive(String username) {
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement("select active from users where username=?");
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			rs.next();

			if (rs.getInt("active") == 1)
				return true;
		} catch (SQLException e) {
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return false;
	}

	/**
	 * adds user with the specific inparameters.
	 * @param username
	 * @param password
	 * @param email
	 * @param group
	 * @param active
	 * @return boolean resultOk
	 */
	public boolean addUser(String username, String password, String email, String group, int active){
		boolean resultOk = true;
		PreparedStatement ps = null;

		try{
			ps = conn.prepareStatement("insert into users (username,password,email,projectgroup,active,role) values(?,?,?,?,?,?)");
			ps.setString(1, username);
			ps.setString(2, password);
			ps.setString(3, email);
			ps.setString(4, group);
			ps.setInt(5, active);
			ps.setString(6, "UG"); //alla nya anv�ndare ska bli UG n�r de skapas.
			ps.executeUpdate();
		} catch (SQLException ex) {
			resultOk = false;
			System.out.println("Something went wrong in AdministrationTools:addUser(...);");
			printSQLError(ex);
			resultOk = false;
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return resultOk;
	}


	/**
	 * Deletes user with the specific username
	 * @param username
	 * @return boolean resultOk
	 */
	public boolean deleteUser(String username){
		boolean resultOk = false;
		User user = getUser(username);

		if(user.getUsername().equals("admin"))
			return false;

		try {
			if (!isUserActive(username)) {
				if (!(user.getGroup() == null || user.getGroup().isEmpty())) {
					PreparedStatement ps = conn.prepareStatement("update users set active=? where username=?");
					ps.setInt(1, 1);
					ps.setString(2, username);
					ps.executeUpdate();
					resultOk = true;
					ps.close();
				}
			} else {
				PreparedStatement ps = conn.prepareStatement("update users set active=? where username=?");
				ps.setInt(1, 0);
				ps.setString(2, username);
				ps.executeUpdate();
				resultOk = true;
				ps.close();
			}		

			// tog bort detta pga bugg med finally{}... :
			//		try {
			//			int notActive = !isUserActive(username) ? 1 : 0; //typecast negated boolean to int
			//			if (notActive == 1) { // if user is not active, check that projectgroup is not null
			//				if(getUser(username).F() == null || getUser(username).getGroup().isEmpty()) {
			//					return false;
			//				}
			//				else
			//					ps = conn.prepareStatement("update users set active=? where username=?");
			//
			//			} else {
			//				ps = conn.prepareStatement("update users set active=? where username=?");
			//			}
			//			ps.setInt(1, notActive);
			//			ps.setString(2, username);
			//			ps.executeUpdate();
			//			
			//			

		} catch (SQLException ex) {
			resultOk = false;
			System.out.println("Something went wrong in AdministrationTools:deleteUser(...);");
			printSQLError(ex);
		}

		return resultOk;
	}

	/**
	 * Changes password for the specific username
	 * @param username
	 * @param password
	 * @return boolean resultOk
	 */
	public boolean changePassword(String username, String password) {
		boolean resultOk = true;
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement("update users set password=? where username=?");
			ps.setString(1, password);
			ps.setString(2, username);
			ps.executeUpdate();
		} catch (SQLException ex) {
			resultOk = false;
			System.out.println("Something went wrong in AdministrationTools:changePassword(...);");
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return resultOk;
	}
	/**
	 * Creates a group with the specific groupName
	 * @param groupName
	 * @return boolean resultOk
	 */
	public boolean createGroup(String groupName){ 
		boolean resultOk = true;
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement("insert into projectgroups (name) values (?)");
			ps.setString(1, groupName);
			ps.executeUpdate();
		} catch (SQLException ex) {
			resultOk = false;
			System.out.println("Something went wrong in AdministrationTools:createGroup(...);");
			printSQLError(ex);
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return resultOk;
	}

	/**
	 * Makes all the users from a group inactive and makes them group-less and also deletes the group.
	 * @param groupName
	 * @return boolean resultOk
	 */
	public boolean deleteGroup(String groupName){
		boolean resultOk = true;
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;

		try {
			//make it so all the users become inactive and set their group value to null.
			ps = conn.prepareStatement("update users set projectgroup=?, active=?  where projectgroup=? and username <> 'admin'");
			ps.setNull(1, java.sql.Types.VARCHAR);
			ps.setInt(2, 0);			
			ps.setString(3, groupName);
			ps.executeUpdate();

			//delete the group from the database.
			ps2 = conn.prepareStatement("delete from projectgroups where name=?");
			ps2.setString(1, groupName);
			ps2.executeUpdate();

			ps3 = conn.prepareStatement("update users set projectgroup=?, active=? where projectgroup=? and username = 'admin'");
			ps3.setNull(1, java.sql.Types.VARCHAR);
			ps3.setInt(2, 1);
			ps3.setString(3, groupName);
			ps3.executeUpdate();


		} catch (SQLException ex) {
			resultOk = false;
			System.out.println("Something went wrong in AdministrationTools:deleteGroup(...);");
			printSQLError(ex);
		}
		finally {
			try {
				ps.close();
				ps2.close();
				ps3.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return resultOk;
	}
	/**
	 * Gets the group from the specific username
	 * @param username
	 * @return String group
	 */
	public String getGroup(String username) {
		PreparedStatement ps = null;
		String group = null;

		try {
			ps = conn.prepareStatement("select projectgroup from users where username=?");
			ps.setString(1, username);

			ResultSet rs = ps.executeQuery();
			if (rs.next())
				group = rs.getString("projectgroup");
		}
		catch (SQLException e) {
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return group;
	}
	/**
	 * Gets all groupNames from the database
	 * @return List<String> groupList
	 */
	public List<String> getAllGroupNames(){
		List<String> groupList = new ArrayList<String>();
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement("select * from projectgroups");
			ResultSet rs = ps.executeQuery();

			while (rs.next())
				groupList.add(rs.getString("name"));
		} catch (SQLException e) {
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}
		return groupList;
	}
	/**
	 * Changes group for the specific username
	 * @param username
	 * @param group
	 * @return  boolean resultOk
	 */
	public boolean changeGroup(String username, String group){
		boolean resultOk = true;
		PreparedStatement ps = null;

		try{

			ps = conn.prepareStatement("update users set projectgroup=? where username=?");
			ps.setString(1, group);
			ps.setString(2, username);
			ps.executeUpdate();

		} catch (SQLException ex) {
			resultOk = false;
			System.out.println("Something went wrong in AdministrationTools:changeGroup(...);");
			printSQLError(ex);
			resultOk = false;
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return resultOk;
	}

	/**
	 * Fetches time report attributes from database and stores them in an arrayList.
	 * @param username
	 * @param week
	 * @return ArrayList containing timereport attributes.
	 */

	public ArrayList<String> getTimeReportAttributes(String username, int week) {
		ArrayList<String> values = new ArrayList<String>();

		PreparedStatement ps = null;


		try {
			ps = conn.prepareStatement("SELECT * FROM timereports WHERE username =? AND week =?");
			ps.setString(1, username);
			ps.setInt(2, week);
			ResultSet rs = ps.executeQuery();
			if (rs.next()){
				//String date = dateFormat.format(new Date(rs.getDate("date")));
				Timestamp t = rs.getTimestamp("date");
				String actualDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(t);
				values.add(0, rs.getString("username")); // 0 = username
				values.add(1, rs.getString("projectgroup")); // 1 = projectgroup
				values.add(2, rs.getDate("date").toString());
				values.add(3, Integer.toString(rs.getInt("week")));
				values.add(4, Boolean.toString(rs.getBoolean("signed")));
				values.add(5, actualDate);
				//System.out.println("VALUES::: " + values);
				
			}	
		} catch (SQLException ex) {
			System.out.println("Something went wrong in getTimeReportAttributes");
			printSQLError(ex);
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return values;
	}

	/**
	 * Gets the TimeReport times for the specified username and week.
	 * @param username
	 * @param week
	 * @return
	 */
	public HashMap<Integer, Integer> getTimeReportTimes(String username, int week) {
		HashMap<Integer, Integer> values = new HashMap<Integer, Integer>();

		boolean resultOk = true;
		PreparedStatement ps = null;

		int iC = 10;

		try {
			int i = 1;
			ps = conn.prepareStatement("SELECT * FROM activities WHERE username=? AND week =? ORDER BY activityID");
			ps.setString(1, username);
			ps.setInt(2, week);
			ResultSet rs = ps.executeQuery();
			while (rs.next()){
				values.put(rs.getInt("activityID"), rs.getInt("activitytime"));
			}



		} catch (SQLException ex) {
			resultOk = false;
			System.out.println("Something went wrong in getTimeReportTimes(...);");
			printSQLError(ex);
			resultOk = false;
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return values;
	}

	
	


	/**
	 * Gets TimeReports from the specific group.
	 * @param group
	 * @return reports
	 */

	public List<TimeReport> getTimeReportUser(String username) {
		List<TimeReport> reports = new ArrayList<TimeReport>();
		PreparedStatement ps = null, ps2 = null;

		try {
			ps = conn.prepareStatement("select * from timereports where username=?");
			ps2 = conn.prepareStatement("select * from activities where username=? and week=?");

			ps.setString(1, username);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				int week = rs.getInt("week");

				String group = rs.getString("projectgroup");
				ps2.setString(1, username);
				ps2.setInt(2, week);
				ResultSet rs2 = ps2.executeQuery();
				HashMap<Integer, Integer> activities = new HashMap<Integer, Integer>();

				while (rs2.next()) {
					activities.put(rs2.getInt("activityID"), rs2.getInt("activitytime"));
				}
				reports.add(new TimeReport(username, group, week, rs.getBoolean("signed"), rs.getDate("date"), activities));
			}
		} catch (SQLException e) {
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps.close();
				ps2.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return reports;
	}


	/**
	 * Gets all the signed timereports for the specified user.
	 * @param username
	 * @return
	 */
	public List<TimeReport> getTimeReportUserSigned(String username) {
		List<TimeReport> reports = new ArrayList<TimeReport>();
		PreparedStatement ps = null, ps2 = null;

		try {
			ps = conn.prepareStatement("select * from timereports where username=? and signed=?");
			ps2 = conn.prepareStatement("select * from activities where username=? and week=?");

			ps.setString(1, username);
			ps.setString(2, "0");

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				int week = rs.getInt("week");

				String group = rs.getString("projectgroup");
				ps2.setString(1, username);
				ps2.setInt(2, week);
				ResultSet rs2 = ps2.executeQuery();
				HashMap<Integer, Integer> activities = new HashMap<Integer, Integer>();

				while (rs2.next()) {
					activities.put(rs2.getInt("activityID"), rs2.getInt("activitytime"));
				}
				reports.add(new TimeReport(username, group, week, rs.getBoolean("signed"), rs.getDate("date"), activities));
			}
		} catch (SQLException e) {
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps.close();
				ps2.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return reports;
	}

	/**
	 * Gets all the time reports of all users in the project group with the name <i>group</i>.
	 * @param group The name of the project group
	 * @return All users' time reports in the group
	 */

	public List<TimeReport> getTimeReports(String group) {
		List<TimeReport> reports = new ArrayList<TimeReport>();
		PreparedStatement ps = null, ps2 = null;

		try {
			ps = conn.prepareStatement("select * from timereports where projectgroup=?");
			ps2 = conn.prepareStatement("select * from activities where username=? and week=?");

			ps.setString(1, group);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				int week = rs.getInt("week");

				String username = rs.getString("username");
				ps2.setString(1, username);
				ps2.setInt(2, week);
				ResultSet rs2 = ps2.executeQuery();
				HashMap<Integer, Integer> activities = new HashMap<Integer, Integer>();

				while (rs2.next())
					activities.put(rs2.getInt("activityID"), rs2.getInt("activitytime"));

				reports.add(new TimeReport(username, group, week, rs.getBoolean("signed"), rs.getDate("date"), activities));
			}
		} catch (SQLException e) {
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps.close();
				ps2.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return reports;
	}
	/**
	 * Gets TimeReports from a specific group with the specific signStatus
	 * @param group
	 * @param signed
	 * @return List<TimeReport> reports
	 */
	public List<TimeReport> getTimeReports(String group, boolean signed) {
		List<TimeReport> reports = new ArrayList<TimeReport>();
		PreparedStatement ps = null, ps2 = null;

		try {
			ps = conn.prepareStatement("select * from timereports where projectgroup=? and signed=?");
			ps2 = conn.prepareStatement("select * from activities where username=? and week=?");

			ps.setString(1, group);
			ps.setBoolean(2, signed);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				int week = rs.getInt("week");

				String username = rs.getString("username");
				ps2.setString(1, username);
				ps2.setInt(2, week);
				ResultSet rs2 = ps2.executeQuery();
				HashMap<Integer, Integer> activities = new HashMap<Integer, Integer>();


				while (rs2.next())
					activities.put(rs2.getInt("activityID"), rs2.getInt("activitytime"));

				reports.add(new TimeReport(username, group, week, rs.getBoolean("signed"), rs.getDate("date"), activities));
			}
		} catch (SQLException e) {
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps.close();
				ps2.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return reports;
	}


	/**
	 * Gets a filtered list of time reports. group is the only parameter that's not allowed to be null or 0.
	 * @param group
	 * @param username
	 * @param role
	 * @param week
	 * @return List<TimeReport> reports
	 */
	public List<TimeReport> getFilteredTimeReports(String group, String username, String role, int week) {
		List<TimeReport> reports = new ArrayList<TimeReport>();
		String sql = "select * from timereports where projectgroup=?";

		if (username != null)
			sql += "and username=?";
		if (week != 0)
			sql += "and week=?";

		PreparedStatement ps = null, ps2 = null;

		try {
			ps = conn.prepareStatement(sql);
			ps2 = conn.prepareStatement("select * from activities where username=? and week=?");

			ps.setString(1, group);
			int index = 2;

			if (username != null)
				ps.setString(index++, username);
			if (week != 0)
				ps.setInt(index++, week);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String reportUser = rs.getString("username");
				int reportWeek = rs.getInt("week");

				if (role == null || getUser(reportUser).getRole().equals(role)) {
					ps2.setString(1, reportUser);
					ps2.setInt(2, reportWeek);

					ResultSet rs2 = ps2.executeQuery();
					HashMap<Integer, Integer> activities = new HashMap<Integer, Integer>();

					while (rs2.next())
						activities.put(rs2.getInt("activityID"), rs2.getInt("activitytime"));

					reports.add(new TimeReport(reportUser, rs.getString("projectgroup"), reportWeek, rs.getBoolean("signed"), rs.getDate("date"), activities));
				}
			}

		} catch (SQLException e) {
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps.close();
				ps2.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return reports;
	}
	/**
	 * Changes the timereport signStatus that belongs to the username 
	 * @param username
	 * @param week
	 * @param signed
	 * @return boolean 
	 */
	public boolean changeSignStatus(String username, int week, boolean signed) {

		boolean resultOK = false;
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement("update timereports set signed=? where username=? and week=?");
			ps.setBoolean(1, signed);
			ps.setString(2, username);
			ps.setInt(3, week);

			ps.executeUpdate();

			resultOK = true;
		} catch (SQLException e) {
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}
		return resultOK;
	}
	/**
	 * Sets the project role for the specific username
	 * @param username
	 * @param role
	 * @return boolean resultOk
	 */
	public boolean setRole(String username, String role) {
		PreparedStatement ps = null;
		boolean resultOK = false;

		try {
			ps = conn.prepareStatement("update users set role=? where username=?");
			ps.setString(1, role);
			ps.setString(2, username);

			ps.executeUpdate();
			resultOK = true;
		} catch (SQLException e) {
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return resultOK;
	}
	/**
	 * Gets the role from the specific username
	 * @param username
	 * @return String role
	 */
	public String getRole(String username) {
		PreparedStatement ps = null;
		String role = null;

		try {
			ps = conn.prepareStatement("select role from users where username=?");
			ps.setString(1, username);

			ResultSet rs = ps.executeQuery();
			if (rs.next())
				role = rs.getString("role");
		} catch (SQLException e) {
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return role;
	}

	/**
	 * Changes the project group name
	 * @param oldName
	 * @param newName
	 * @return boolean resultOk
	 */
	public boolean changeGroupName(String oldName, String newName){
		boolean resultOk=true;
		PreparedStatement ps = null;
		PreparedStatement ps0 = null;

		try {
			ps = conn.prepareStatement("update projectgroups set name=? where name=?");
			ps.setString(1, newName);
			ps.setString(2, oldName);
			ps.executeUpdate();
		} catch (SQLException e) {
			resultOk=false;
			printSQLError(e);
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}
		
		if (resultOk) {
			try {
				ps0 = conn.prepareStatement("update users set projectgroup=? where projectgroup=?");
				ps0.setString(1, newName);
				ps0.setString(2, oldName);
				ps0.executeUpdate();
			} catch (SQLException e) {
				resultOk=false;
				printSQLError(e);
				e.printStackTrace();
			} finally {
				try {
					ps0.close();
				} catch (SQLException e) {
					printSQLError(e);
					e.printStackTrace();
				}
			}
		}

		return resultOk;
	}
	
	/**
	 * Gets all the weeks that have timereports from a group.
	 * @param group
	 * @return
	 */
	public ArrayList<Integer> getGroupWeeks (String group){
		ArrayList<Integer> weeks = new ArrayList<Integer>();
		PreparedStatement ps = null;
				try {
					ps = conn.prepareStatement("select distinct week from timereports where projectgroup = ? order by week;");
					
					ps.setString(1, group);

					ResultSet rs = ps.executeQuery();

					while (rs.next()) {
						int week = rs.getInt("week");		

						weeks.add(week);
					}
				} catch (SQLException e) {
					printSQLError(e);
					e.printStackTrace();
				}
				finally {
					try {
						ps.close();
					} catch (SQLException e) {
						printSQLError(e);
						e.printStackTrace();
					}
				}
		
		return weeks;
		
	}
/**
 * Inserts activities contained in TimeReport object into the database.
 * @param tr
 * @return true if no issues.
 */
	private boolean insertActivities(TimeReport tr) {
		// TODO Auto-generated method stub
		boolean resultOk=true;
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		int actIDL = 1;
		int actIDR = 1;
		int actTime;
		int actCounter = 0;
		int[] actIDs = {11, 12, 13, 14, 21, 22, 23, 24, 31, 32, 33, 34, 41, 42, 43, 44, 51, 52, 53, 54,
				61, 62, 63, 64, 71, 72, 73, 74, 81, 82, 83, 84, 91, 92, 93, 94,
				110, 120, 130, 140, 150, 160, 170, 180, 190,
				404};
		int actID = actIDs[0];// = actIDL*10 + actIDR;


		try {


			do {
				actTime = tr.getActivityTime(actID);
				ps1 = conn.prepareStatement("INSERT INTO activities VALUES (?, ?, ?, ?)");
				ps1.setString(2, tr.getUsername());			
				ps1.setInt(3, tr.getWeek());	
				ps1.setInt(1, actID);
				ps1.setInt(4, actTime);
				ps1.executeUpdate();

				actCounter++;
				actID = actIDs[actCounter];


			} while (actID != 404); // Ehhh...

		} catch (SQLException e) {
			resultOk=false;
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps1.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}

		}




		return resultOk;
	}

	/**
	 * Inserts a TimeReport into the database.
	 * @param tr
	 * @return
	 */
	public boolean insertTimeReport(TimeReport tr) {
		// TODO Auto-generated method stub
		boolean resultOk=true;
		PreparedStatement ps1 = null;
		java.sql.Timestamp sq = new java.sql.Timestamp(tr.getDate().getTime());
		
		try {

			ps1 = conn.prepareStatement("INSERT INTO timereports VALUES (?, ?, ?, ?, ?)");
			ps1.setString(1, tr.getUsername());			
			ps1.setString(2, tr.getGroup());	
			ps1.setTimestamp(3, sq);	
			ps1.setInt(4, tr.getWeek());	
			System.out.println(tr.isSigned());
			ps1.setBoolean(5, tr.isSigned());	
			ps1.executeUpdate();

			insertActivities(tr);
		} catch (SQLException e) {
			if (e.getClass().isInstance(new com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException())) {
				System.out.println("DUPLICATE D:DDD");

			} else {
				System.out.println("ANNAT FEL???");

				printSQLError(e);
				e.printStackTrace();
			}
			resultOk=false;
		}
		finally {
			try {
				ps1.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return resultOk;
	}

	/**
	 * Deletes a TimeReport from the database.
	 * @param username
	 * @param week
	 * @return
	 */
	public boolean deleteTimeReport(String username, int week) {
		// TODO Auto-generated method stub
		boolean resultOk=true;
		PreparedStatement ps0 = null;
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;


		try {

			deleteActivities(username, week);

			ps0 = conn.prepareStatement("SET foreign_key_checks=?");
			ps0.setInt(1, 0);
			ps0.executeUpdate();

			ps1 = conn.prepareStatement("DELETE FROM timereports WHERE username=? AND week=?");
			ps1.setString(1, username);
			ps1.setInt(2, week);
			ps1.executeUpdate();

			ps2 = conn.prepareStatement("SET foreign_key_checks=?");
			ps2.setInt(1, 1);
			ps2.executeUpdate();

			ps3 = conn.prepareStatement("DELETE FROM activities WHERE username=? AND week=?");
			ps3.setString(1, username);
			ps3.setInt(2, week);
			ps3.executeUpdate();



		} catch (SQLException e) {
			resultOk=false;
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps1.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return resultOk;
	}

	/**
	 * Deleted the specified Activity from the database.
	 * @param username
	 * @param week
	 * @return
	 */
	public boolean deleteActivities(String username, int week) {
		// TODO Auto-generated method stub
		boolean resultOk=true;
		PreparedStatement ps1 = null;
		try {

			ps1 = conn.prepareStatement("DELETE FROM activities WHERE username=? AND week=?");
			ps1.setString(1, username);
			ps1.setInt(2, week);
			ps1.executeUpdate();


		} catch (SQLException e) {
			resultOk=false;
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps1.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}

		return resultOk;
	}

	/**
	 * Creates a HashMap containing the sum by week of all groups.
	 * @param week Integer
	 * @return Returns a hashmap containing key = activityID and value = total time for activityID.
	 */
	public HashMap<Integer, Integer> sumAllByWeekGroup(int week, String groupname) {
		PreparedStatement ps = null;
		HashMap<Integer, Integer> hMapTime = new HashMap<Integer, Integer>();
		try {
			ps = conn.prepareStatement(""
					+ "SELECT activities.activityid, activities.activitytime "
					+ "FROM activities "
					+ "WHERE activities.week = ? "
					+ "AND activities.username in ("
					+ "SELECT timereports.username "
					+ "FROM timereports "
					+ "WHERE timereports.projectgroup = ?)");
			ps.setInt(1, week);
			ps.setString(2, groupname);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Integer activityID = rs.getInt(1);
				Integer time = rs.getInt(2);
				//Create a new pair if they don't exist in hMapTime, else add time to existing value
				if (!hMapTime.containsKey(activityID)) {
					hMapTime.put(activityID, time);
				} else {
					hMapTime.put(activityID, hMapTime.get(activityID) + time); 
				}
			}
		} catch (SQLException e) {
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}
		return hMapTime;
	}
	
	/**
	 * Creates a HashMap containing the sum by week of the specified group.
	 * @param groupname
	 * @return
	 */
	public HashMap<Integer, Integer> sumAllByGroup(String groupname) {
		PreparedStatement ps = null;
		HashMap<Integer, Integer> hMapTime = new HashMap<Integer, Integer>();
		try {
			ps = conn.prepareStatement(""
					+ "SELECT activities.activityid, activities.activitytime "
					+ "FROM activities "
				//	+ "WHERE activities.week = ? "
					+ "WHERE activities.username in ("
					+ "SELECT timereports.username "
					+ "FROM timereports "
					+ "WHERE timereports.projectgroup = ?)");
	//		ps.setInt(1, week);
			ps.setString(1, groupname);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Integer activityID = rs.getInt(1);
				Integer time = rs.getInt(2);
				//Create a new pair if they don't exist in hMapTime, else add time to existing value
				if (!hMapTime.containsKey(activityID)) {
					hMapTime.put(activityID, time);
				} else {
					hMapTime.put(activityID, hMapTime.get(activityID) + time); 
				}
			}
		} catch (SQLException e) {
			printSQLError(e);
			e.printStackTrace();
		}
		finally {
			try {
				ps.close();
			} catch (SQLException e) {
				printSQLError(e);
				e.printStackTrace();
			}
		}
		return hMapTime;
	}
}