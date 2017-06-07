package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import core.ServletBase;
import core.TimeReport;
import core.TimeReport;
import core.User;

/**
 * Servlet implementation class ProjectLeaderTools
 */
@WebServlet("/ProjectLeader")
public class ProjectLeaderTools extends ServletBase {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see ServletBase#ServletBase()
     */
    public ProjectLeaderTools() {
        super();
    }
    
    /**
     * Generates a table with one row per entry in <i>reports</i>.
     * @param reports The time reports to be listed
     * @param sign Whether the list is to be used for signing (true) or unsigning (false) reports
     * @return String HTML code
     */
    private String generateCheckboxTable(List<TimeReport> reports, boolean sign) {
		String html = "<form action='ProjectLeader'><input type='hidden' name='function' value='" + (sign ? "sign" : "unsign") + "Reports'>"
				+ "<table class='list'><tr><th></th><th>User</th><th>Latest update</th><th>Week</th><th>Development</th><th>Informal review</th>"
				+ "<th>Formal review</th><th>Rework</th><th>Other</th><th>Total time</th></tr>";
		
		for (TimeReport r : reports) {
			String username = r.getUsername();
			int week = r.getWeek();
			
			html += "<tr><td><input type='checkbox' name='" + username + "' value='" + week + "'></td><td>" + username + "</td><td>" + r.getDate() + "</td><td>" + week + "</td><td>"
					+ r.getSum("D") + "</td><td>" + r.getSum("I") + "</td><td>" + r.getSum("F") + "</td><td>"
					+ r.getSum("R") + "</td><td>" + r.getSum("Other") + "</td><td>" + r.getTotalTime() + "</td></tr>";
		}
			
		
		html += "</table><br><input type='submit' value='" + (sign ? "Sign" : "Unsign") + "'></form>";
		
		return html;
    }
    
    /**
     * Checks the parameters for the "Sign reports" and "Unsign reports" pages.
     * @param request
     * @param user
     * @param sign
     * @return boolean changed
     */
    private boolean checkSelection(HttpServletRequest request, User user, boolean sign) {
    	Enumeration<String> paramNames = request.getParameterNames();
		boolean changed = false;
		
		while (paramNames.hasMoreElements()) {
			String name = paramNames.nextElement();
			User listUser = db.getUser(name);
				
				if (listUser != null)
					for (String p : request.getParameterValues(name))
						if (db.changeSignStatus(name, Integer.parseInt(p), sign))
							changed = true;
		}
		
		return changed;
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
			
			String function = request.getParameter("function");
			String username = getStringFromSession(session, "username");
			User user = db.getUser(username);
			String role = user.getRole();
			
			String html = "";
			
			if (username.equals("admin") || (role != null && role.equals("PG"))) {
				html += "<h1>Project Leader Tools</h1>";
				
				if (function != null)
					switch (function) {
					case "signReports":
						if (checkSelection(request, user, true))
							responseList.add("Successfully signed one or more time reports.");
						
						html += "<h2>Sign Reports</h2>";
						html += generateCheckboxTable(db.getTimeReports(user.getGroup(), false), true);
						break;
					case "unsignReports":
						if (checkSelection(request, user, false))
							responseList.add("Successfully unsigned one or more time reports.");
						
						html += "<h2>Unsign Reports</h2>";
						html += generateCheckboxTable(db.getTimeReports(user.getGroup(), true), false);
						break;
					case "viewAllReports":
						String inspect = request.getParameter("inspect");
						
						if (inspect != null) {
							response.sendRedirect("TimeReporting?function=ViewTimeReport&" + inspect);
							return;
						}
						
						String fUser = request.getParameter("username");
						String fWeekString = request.getParameter("week");
						String fRole = request.getParameter("role");
						String fActivity = request.getParameter("activity");
						int fWeek = 0;
						
						if (fUser != null && fUser.equals("none"))
							fUser = null;
						
						if (fWeekString != null && !fWeekString.isEmpty())
							fWeek = Integer.parseInt(fWeekString);
						
						if (fRole != null && fRole.equals("none"))
							fRole = null;
							
						int fActivityNum;
						boolean fActivityIsNum = false;
						
						if (fActivity != null)
							if (fActivity.equals("none"))
								fActivity = null;
							else
								try {
									fActivityNum = Integer.parseInt(fActivity);
									if (fActivityNum > 0)
										fActivityIsNum = true;
								} catch (NumberFormatException e) { }
						
						List<TimeReport> reports = db.getFilteredTimeReports(user.getGroup(), fUser, fRole, fWeek);
						
						html += "<h2>View all time reports</h2>"
								+ "<form id='filterTools' action='ProjectLeader'>"
								+ "<input type='hidden' name='function' value='viewAllReports'>"
								+ "<p><b>Filter</b></p>"
								+ "<div>User<br>"
								+ "<select class='filter' name='username'>"
								+ "<option value='none'>-No user filter-</option>";
								for (User u : db.getGroupMembers(user.getGroup())) {
									String uName = u.getUsername();
									html += "<option value='" + uName + "'>" + uName + "</option>";
								}
								html += "</select></div>"
										+ "<div>Week<br>"
										+ "<input class='filter' name='week' type='number' min='1' max='52'></div>"
										+ "<div>Role<br>"
										+ "<select class='filter' name='role'>"
										+ "<option value='none'>-No role filter-</option>"
										+ "<option value='UG'>Developer</option>"
										+ "<option value='SG'>System Manager</option>"
										+ "<option value='TG'>Tester</option>"
										+ "<option value='PG'>Project Leader</option>"
										+ "</select></div>"
										+ "<div>Activity<br>"
										+ "<select class='filter' name='activity'>"
										+ "<option value='none'>-No activity filter-</option>"
										+ "<option value='SDP'>SDP</option>"
										+ "<option value='SRS'>SRS</option>"
										+ "<option value='SVVS'>SVVS</option>"
										+ "<option value='STLDD'>STLDD</option>"
										+ "<option value='SVVI'>SVVI</option>"
										+ "<option value='SDDD'>SDDD</option>"
										+ "<option value='SVVR'>SVVR</option>"
										+ "<option value='SSD'>SSD</option>"
										+ "<option value='Final Report'>Final Report</option>"
										+ "<option value='110'>Functional test</option>"
										+ "<option value='120'>System test</option>"
										+ "<option value='130'>Regression test</option>"
										+ "<option value='140'>Meeting</option>"
										+ "<option value='150'>Lecture</option>"
										+ "<option value='160'>Exercise</option>"
										+ "<option value='170'>Computer exercise</option>"
										+ "<option value='180'>Home reading</option>"
										+ "<option value='190'>Other</option>"
										+ "</select></div><br>"
										+ "<input type='submit' value='Filter'></form>"
										+ "<p><b>Reports</b></p>";
								
								// Tabellen
								
								html += "<form action='ProjectLeader'><input type='hidden' name='function' value='viewAllReports'>" // TODO möjligtvis ändra när TimeReporting har en motsvarande funktion
										+ "<table class='list'><tr><th></th><th>User</th><th>Latest update</th><th>Week</th><th>Development</th><th>Informal review</th>"
										+ "<th>Formal review</th><th>Rework</th><th>Other</th><th>Total time</th><th>Signed</th></tr>";
								
								int totD = 0, totI = 0, totF = 0, totR = 0, totO = 0, totTime = 0, reportCount = 0;
								
								for (TimeReport r : reports) {
									// Activity filter
									if (fActivity == null || (!fActivityIsNum && r.getSum(fActivity) > 0) || (fActivityIsNum && r.getActivityTime(Integer.parseInt(fActivity)) > 0)) {
										String rUsername = r.getUsername();
										int rWeek = r.getWeek();
										reportCount++;
										
										int dTime = r.getSum("D");
										int iTime = r.getSum("I");
										int fTime = r.getSum("F");
										int rTime = r.getSum("R");
										int oTime = r.getSum("Other");
										int tot = r.getTotalTime();
										totD += dTime;
										totI += iTime;
										totF += fTime;
										totR += rTime;
										totO += oTime;
										totTime += tot;
										
										html += "<tr><td><input type='radio' name='inspect' value='username=" + rUsername + "&SelectedWeek=" + rWeek + "'></td><td>" + rUsername + "</td><td>" + r.getDate() + "</td><td>" + rWeek + "</td><td>"
												+ dTime + "</td><td>" + iTime + "</td><td>" + fTime + "</td><td>"
												+ rTime + "</td><td>" + oTime + "</td><td>" + tot + "</td><td>" + (r.isSigned() ? "Yes" : "No") + "</td></tr>";
									}
								}
								
								if (reportCount != 0) {
									html += "<tr><td></td><td>[Sum]</td><td>-</td><td>-</td><td>" + totD + "</td><td>" + totI + "</td><td>" + totF
											+ "</td><td>" + totR + "</td><td>" + totO + "</td><td>" + totTime + "</td><td>-</td></tr>"
											+ "<tr><td></td><td>[Avg]</td><td>-</td><td>-</td><td>" + totD / reportCount + "</td><td>" + totI / reportCount
											+ "</td><td>" + totF / reportCount + "</td><td>" + totR / reportCount + "</td><td>"
											+ totO / reportCount + "</td><td>" + totTime / reportCount + "</td><td>-</td></tr>"
											+ "</table><br><input type='submit' value='Inspect'></form>";
								}
								else {
									html += "<tr><td></td><td>-No reports found-</td></tr></form>";
									responseList.add("No reports found.");
								}
						break;
					default:
						responseList.add("Unknown function, use one of the links provided");
						response.sendRedirect("ProjectLeader");
						return;
					}
				else {
					html += "<a href='ProjectLeader?function=signReports'>Sign reports</a><br>";
					html += "<a href='ProjectLeader?function=unsignReports'>Unsign reports</a><br>";
					html += "<a href='ProjectLeader?function=viewAllReports'>View all time reports</a>";
				}
			}
			else { // Not admin or PG
				responseList.add("You do not have privilege to access this page.");
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