package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import core.ServletBase;
import core.User;

/**
 * Servlet implementation class MemberList
 */
@WebServlet("/MemberList")
public class MemberList extends ServletBase {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see ServletBase#ServletBase()
     */
    public MemberList() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!loggedIn(request))
			response.sendRedirect("LogIn");
		else {
			// Get the session
			HttpSession session = request.getSession(true);
			PrintWriter out = response.getWriter();
			out.println(getPageIntro(request)); //Append html-header
			
			out.println("<h1>Member list</h1>\n");
			
			String html = "<form action='MemberList' method='post'><table class='list'><tr><th>Username</th><th>E-Mail</th><th>Role</th></tr>";
			
			String username = getStringFromSession(session, "username");
			User user = db.getUser(username);
			String role = user.getRole();
			
			if (username.equals("admin") || (role != null && role.equals("PG"))) {
				Enumeration<String> paramNames = request.getParameterNames();
				boolean changedRole = false;
				
				while (paramNames.hasMoreElements()) {
					String name = paramNames.nextElement();
					User listUser = db.getUser(name);
					
					if (listUser != null && !name.equals("admin")) {
						String userRole = listUser.getRole() != null ? listUser.getRole() : ""; 
						String setRole = request.getParameter(name);
						
						if (!userRole.equals(setRole) && !userRole.equals("PG") && listUser.getGroup().equals(user.getGroup()) &&
								(setRole.equals("TG") || setRole.equals("UG") || setRole.equals("SG"))) {
							db.setRole(name, setRole);
							changedRole = true;
						}
					}
				}
				
				if (changedRole)
					responseList.add("Successfully changed one or more group members' role(s).");
				
				for (User u : db.getGroupMembers(user.getGroup()))
					if (u.isActive() && !u.getUsername().equals("admin")) {
						String uRole = u.getRole();
						html += "<tr><td>" + u.getUsername() + "</td><td>" + (u.getEmail() != null ? u.getEmail() : "N/A")  + "</td><td>";
						if (uRole == null || !uRole.equals("PG"))
							html += "<select name='" + u.getUsername() + "'>"
									+ (uRole == null ? "<option value='N/A'>N/A</option>" : "")
									+ "<option " + (uRole != null && uRole.equals("UG") ? "selected " : "") + "value='UG'>Developer</option>"
									+ "<option " + (uRole != null && uRole.equals("SG") ? "selected " : "") + "value='SG'>System Manager</option>"
									+ "<option " + (uRole != null && uRole.equals("TG") ? "selected " : "") + "value='TG'>Tester</option>"
									+ "</select>";
						else // Role is PG
							html += "Project Leader";
						
						html += "</td></tr>";
						
					}
				
				html += "</table><br><input type='submit' value='Change'></form>";
			}
			else { // Not admin or PG
				for (User u : db.getGroupMembers(user.getGroup()))
					if (u.isActive() && !u.getUsername().equals("admin")) // Only list active users
						html += "<tr><td>" + u.getUsername() + "</td><td>" + (u.getEmail() != null ? u.getEmail() : "N/A")  + "</td><td>" + (u.getRole() != null ? u.getRole() : "N/A")  + "</td></tr>";
				
				html += "</table>";
			}
			
			out.println(html);
			
			out.println(getPageOutro()); //Append html-trailer
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}