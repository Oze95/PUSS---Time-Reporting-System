package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import core.ServletBase;
import core.User;


/**
 * Servlet implementation class AdministrationTools. 
 * Constructs a page for administration purpose. 
 * Checks first if the user is logged in and then if it is the administrator. 
 * If that is OK it displays all users and a form for adding new users.
 * 
 *  
 *  @version 1.0
 */
@WebServlet("/AdministrationTools")
public class AdministrationTools extends ServletBase {
	private static final long serialVersionUID = 1L;
	private static final int PASSWORD_LENGTH = 8;
	String newPassword;

	/**
	 * @see ServletBase#servletBase()
	 */
	public AdministrationTools() {
		super();
	}

	/**
	 * generates a form for adding new users
	 * @return HTML code for the form
	 */
	private String addUserForm() {
		List<String> groupNames = db.getAllGroupNames();

		String html = "<form name='input' method='get'>\n" +
				"<input type='hidden' name='function' value='addUser'>" +
				"<p> Add username: <br> <input type='text' name='username'>"
				+ "<p> Input e-mail: <br> <input type='text' name='email'><br>"
				+ "<p> Select Group: <br>"
				+"<select name='selectGroup'>";
		html+="<option value=>--No Group--</option>";
		for (String s: groupNames) {
			html+="<option value='"+s+"'>"+s+"</option>";
		}				


		html+="</select>"				
				+ "<br><br><br>"
				+ "<input type='submit' value='Add user'>&nbsp"
				+ "<input type='submit' value='Cancel' name='cancelUser'>"
				+ "</form>";
		return html;
	}

	/**
	 * Creates the html form used for the create group page.
	 * @return
	 */
	private String createGroupForm() {
		String html = "<form name='input' method='get'>\n" +
				"<input type='hidden' name='function' value='createGroup'>" +
				"<p> Add Group name: <br><input type='text' name='groupname'><br><br><input type='submit' value='Create project group'>&nbsp" +
				"<input type='submit' value='Cancel' name='cancelGroup'>" +
				"</form>\n";
		return html;
	}



	/**
	 * Checks if a username corresponds to the requirements for user names. 
	 * @param name The investigated username
	 * @return True if the username corresponds to the requirements
	 */
	private boolean checkNewName(String name) {
		int length = name.length();
		boolean ok = (length >= 4 && length <= 16);
		if (ok)
			for (int i = 0; i < length; i++) {
				int ci = (int)name.charAt(i);
				boolean thisOk = ((ci>=48 && ci<=57) || 
						(ci>=65 && ci<=90) ||
						(ci>=97 && ci<=122));
				ok = ok && thisOk;
			}    	
		return ok;
	}

	/**
	 * Creates a random password.
	 * @return a randomly chosen password
	 */
	private String createPassword() {
		String result = "";
		Random r = new Random();
		for (int i=0; i<PASSWORD_LENGTH; i++)
			result += (char)(r.nextInt(26)+97);
		return result;
	}

	private String manageUsers(String filter) {
		List<User> userList;
		List<String> groupList = db.getAllGroupNames();
		String html = "";
		
		//get users and make dropdown menu.
		List<String> groups = db.getAllGroupNames();
		
		html += "<form name='input' method='get'>"
				+ "<input type='hidden' name='function' value='manageUsers'>"
				+ "<select name='groupNameFilter'><option value='all'>--All Users--</option>"
				+ "<option value=''>--No Group--</option>";
		
		for (String s : groups) {
			html +="<option value='"+s+"'>"+s+"</option>";
		}
		
		html += "</select>"
				+ "<input type='submit' value='Filter'></form><br><br>";

		
		//Get all the users from filtered group.
		if (filter == null || filter.equals("all")) {
			userList = db.getAllUsers();
		} else {
			userList =  db.getGroupMembers(filter);
		}

		html +=  "<form action='AdministrationTools?function=manageUsers' method='post'><table class='list'><tr>" +
				"<th>Username</th>" +
				"<th>Password</th>" +
				"<th>Group</th>"+
				"<th>Role</th>"+
				"<th>Active</th>" +
				"<th></th>" +
				"<th></th>" +
				"</tr>";
		
		for (User user : userList) {

			
			String uRole = user.getRole();
			String uGroup = user.getGroup();

			html += "<tr>";
			html += "<td>" + user.getUsername() + "</td>";
			html += "<td>" + user.getPassword() + "</td>";
			html += "<td><select name='" + user.getUsername() + "'>"; //måste hämta alla grup 
			
			for (String s : groupList) {
				html+="<option " + (uGroup != null && uGroup.equals(s) ? "selected " : "") + "value='" + s + "'>" + s + "</option>";
			}
			
			html+="<option " + (uGroup == null || uGroup.isEmpty() ? "selected " : "") + "value=''>" + "--No Group--"+ "</option></select></td>";
			
			
			if (!user.getUsername().equals("admin")) {
			
			html += "<td><select name='" + user.getUsername() + "'>"
					+ "<option "+ (uRole != null && uRole.equals("UG") ? "selected " :"")+"value='UG'>" + "UG"+ "</option>"
					+ "<option "+ (uRole != null && uRole.equals("SG") ? "selected " :"")+"value='SG'>" + "SG"+ "</option>"
					+ "<option "+ (uRole != null && uRole.equals("TG") ? "selected " :"")+"value='TG'>" + "TG"+ "</option>"
					+ "<option "+ (uRole != null && uRole.equals("PG") ? "selected " :"")+"value='PG'>" + "PG"+ "</option>"		
					+ "</select></td>";
			
			html += "<td>" + (user.isActive() ? "Yes" : "No") + "</td>";
			} else {
				html += "<input type='hidden' name='admin' value='PG'>"
					 + "<td></td><td></td></tr>";
				continue;
			}
			
			html += "<td><a href='AdministrationTools?function=deleteUser&username=" + user.getUsername() + "' ";
			
			if (user.isActive()) {
				html += "onClick='return confirm(\"Are you sure you want to inactivate: " + user.getUsername() + "?\")'>"
						+ "<button class='manageUserButton' type='button'>Deactivate</button></a></td>"; //wrap button in href.
			} else {
				
				html += "onClick='return confirm(\"Are you sure you want to activate: " + user.getUsername() + "?\")'>"
						+ "<button class='manageUserButton' type='button'>Activate</button></a>"
						+ "</td>";
			}

			html += "</tr>";

		}
		html += "</table><br><input type='submit' value='Update changes to Group/Role'></form>";
		return html;
	}

	/**
	 * Creates the html form used for displaying the manage groups page.
	 * @return
	 */
	private String manageGroups(){
		List<String> groupList = db.getAllGroupNames();

		String html = "<table class='list'><tr>" +
				"<th>Group</th>" +
				"<th></th>"
				+ "<th></th>"
				+ "<th></th>"
				+ "<th></th>";
		int itr =0;
		for(String s: groupList){
			html += "<tr>";
			html += "<td>" + s + " &nbsp</td>";

			html += "<td><a href='AdministrationTools?function=deleteGroup&groupName=" + s + "' onClick='return confirm(\"Are you sure you want to delete: "+s+"?\")'>"
					+ "<button class='manageUserButton' type='button'" + s + "'>Delete</button></a></td>"
					+ "<td><button onclick='toggle(\""+s+"\")' class='manageGroupButton'> Change name </button></td>"+
					"<td><p id='"+s+"' ><script>document.getElementById('"+s+"').style.display = 'none'</script>"
					+ " <input type='text' id='test!"+Integer.toString(itr)+"' name='test!"+Integer.toString(itr)+"' placeholder='Enter new name'> &nbsp"
					+ "<a onclick=\"this.href='AdministrationTools?function=changeGroupName&oldGroupName="+s+"&newGroupName=' " //dynamically update the link
					+ "+ document.getElementById('test!"+Integer.toString(itr)+"').value;\">"
					+ "<button name='submitNameChange"+itr+"'>Submit </button></a>"
					+ "</p></td>";
			html += "</tr>";
			itr++;
		}
		html+="</table>";


		return html;
	}


	

	/**
	 * Adds a user and a randomly generated password to the database.
	 * @param username Name to be added
	 * @return true if it was possible to add the name. False if it was not, e.g. 
	 * because the name already exist in the database. 
	 */

	private boolean addUser(String username, String email, String role,int active) {
		String newPassword = createPassword();
		this.newPassword=newPassword;
		return db.addUser(username,newPassword,email,role,active);

	}

	/**
	 * Inactivates a user in the database. 
	 * If the user does not exist in the database nothing happens. 
	 * @param username name of user to be inactivated. 
	 */
	private boolean deleteUser(String username) { //Only inactivates a user

		return db.deleteUser(username);
	}

	/**
	 * Creates a new group with chosen name.
	 * @param groupName
	 * @return true if the group was successfully created.
	 */
	private boolean createGroup(String groupName){
		return db.createGroup(groupName);
	}


	private boolean changeGroupName(String oldName, String newName){
		return db.changeGroupName(oldName, newName);

	}

	/**
	 * Deletes the specified group.
	 * @param groupName
	 * @return
	 */
	private boolean deleteGroup(String groupName){
		return db.deleteGroup(groupName);
	}

	/**
	 * Handles input from the user and displays information for administration. 
	 * 
	 * First it is checked if the user is logged in and that it is the administrator. 
	 * If that is the case all users are listed in a table and then a form for adding new users is shown. 
	 * 
	 * Inputs are given with two HTTP input types: 
	 * addname: name to be added to the database (provided by the form)
	 * deletename: name to be deleted from the database (provided by the URLs in the table)
	 */


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();

		HttpSession session = request.getSession(true);
		String myName = getStringFromSession(session, "username");

		// check that the user is logged in
		if (!loggedIn(request))
			response.sendRedirect("LogIn");
		else if (myName.equals("admin")) {
			out.println(getPageIntro(request)); //Append html-header
			out.println("<h1>Administration page</h1>");

			String function = request.getParameter("function"); //Determine course of action
			if(function != null) {
				//Get additional data
				String username = request.getParameter("username");
				String email = request.getParameter("email");
				String groupname;
				String filter;



				switch(function) {

				case "manageUsers":
					
					boolean changed = false;
					
					Enumeration<String> paramList = request.getParameterNames();
					String userName;
					String[] groupAndRole;
					
					while (paramList.hasMoreElements()) {
						
						userName = paramList.nextElement();
						User user = db.getUser(userName);
						
						if (user != null) {
							
							String currentGroup = user.getGroup();
							String currentRole = user.getRole();
							
							groupAndRole = request.getParameterValues(userName);
							
							String setGroup = groupAndRole[0];
							String setRole = groupAndRole[1];
							
							if (currentGroup == null) {
								
								db.changeGroup(userName, setGroup);
								changed = true;
							} else if (!currentGroup.equals(setGroup)) {
								
								if (setGroup.isEmpty() && !user.getUsername().equals("admin") && user.isActive())
									db.deleteUser(userName);
								
								db.changeGroup(userName, setGroup);
								changed = true;
							}
							if (!currentRole.equals(setRole)) {
								
								db.setRole(userName, setRole);
								changed = true;
							}
						}
					}
					
					if (changed) 
						responseList.add("Successfully changed group(s) and/or role(s).");
					
					filter = request.getParameter("groupNameFilter");
					out.println(manageUsers(filter));
					
					break;

				case "manageGroups":
					out.println(manageGroups());
					break;



				case "addUser": // check if the administrator wants to add a new user in the form
					String group="";
					group = request.getParameter("selectGroup");
					int active=1;
					if(group!=null && group.equals("")){
						group=null;
						active=0;
					}



					if(request.getParameter("cancelUser") != null){
						response.sendRedirect("AdministrationTools");
						break;
					}

					if (username != null) {
						if (checkNewName(username)) {
							if (addUser(username,email,group,active))		
								responseList.add("Successfully added user '" + username +"' "
										+ (group==null ? group=" (No Group) " :"in group '"+group+"'") +" with the password '" + newPassword+"'.");
							else
								responseList.add("Error: Not possible to add suggested username: " + username);
						} else
							responseList.add("Error: Suggested username not allowed");
						response.sendRedirect("AdministrationTools?function=addUser");
						return; //We want to redirect here and NOT at the end of the method
					} else
						out.println(addUserForm());
					break;


				case "deleteUser": // check if the administrator wants to delete a user by clicking the URL in the list
					if (username != null) {
						if (checkNewName(username)) {
							if(deleteUser(username))
								responseList.add((!db.isUserActive(username)? "Success! user: "+username+" was deactivated."  : "Success! user: "+username+" was activated. "));
							else
								responseList.add("Error: Not possible to " + (db.isUserActive(username) ? "inactivate user: " : "activate user: ") + username);
						} else
							responseList.add("Error: Suggested username not allowed");
						response.sendRedirect("AdministrationTools?function=manageUsers");
						return; //We want to redirect here and NOT at the end of the method
					}
					break;

				case "deleteGroup":
					groupname=request.getParameter("groupName");
					if(deleteGroup(groupname)){
						responseList.add(groupname + " has been deleted.");
					}else{
						responseList.add("Error: Not possible to delete group: " + groupname);

					}
					out.println(manageGroups());
					break;

				case "changeGroupName":
					String oldname = request.getParameter("oldGroupName");
					String newname = request.getParameter("newGroupName");
					groupname = request.getParameter("groupname");

					if (oldname != null && newname != null) {
						if (checkNewName(newname)){

							if (changeGroupName(oldname,newname)) {
								responseList.add("Success!");
							} else {
								responseList.add("Error: Not possible to change group name to: " + newname);
							}

						} else {
							responseList.add("Error: Not possible to change to the suggested project group name: " + newname);
						}

						response.sendRedirect("AdministrationTools?function=manageGroups");
						return;
					}

					out.println(manageGroups());

					break;

				case "createGroup":
					groupname = request.getParameter("groupname");

					if(request.getParameter("cancelGroup") != null){
						response.sendRedirect("AdministrationTools");
						break;
					}
					if (groupname != null) {
						if(checkNewName(groupname)){

							if(createGroup(groupname)){
								responseList.add("Success: group "+groupname+" was created.");
							}else{
								responseList.add("Error: group " + groupname+ " already exists.");
							}
						}else{
							responseList.add("Error: Not possible to add suggested project group name: " + groupname);
						}


						response.sendRedirect("AdministrationTools?function=createGroup");
						return;
					}
					out.println(createGroupForm());
					break;
				default:
					responseList.add("Invalid function parameter!");
					break;
				}
			} else { //Landed on AdministrationTools page
				out.println("<a href='AdministrationTools?function=manageUsers'>Manage users</a><br>");
				out.println("<a href='AdministrationTools?function=manageGroups'>Manage groups</a><br>");
				out.println("<a href='AdministrationTools?function=addUser'>Create user</a><br>");
				out.println("<a href='AdministrationTools?function=createGroup'>Create project group</a><br>");
			}
			out.println(getPageOutro()); //Append html-trailer
		} else  // name not admin
			response.sendRedirect("Profile");
	}

	/**
	 * All requests are forwarded to the doGet method. 
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
