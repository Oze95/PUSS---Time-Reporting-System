package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



import core.ServletBase;
import core.TimeReport;
import core.User;

/**
 * Servlet implementation class TimeReporting
 */
@WebServlet("/TimeReporting")
public class TimeReporting extends ServletBase {
	private static final long serialVersionUID = 1L;
	private int updateWeek;
	private String username;
	private HashMap<Integer, Integer> activities = new HashMap<Integer, Integer>();

	/**
	 * @see ServletBase#ServletBase()
	 */
	public TimeReporting() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		java.util.Date dateCreated = new java.util.Date();
	
		if (!loggedIn(request))
			response.sendRedirect("LogIn");
		else {
			// Get the session
			HttpSession session = request.getSession(true);
			PrintWriter out = response.getWriter();
			out.println(getPageIntro(request)); //Append html-header
	
			String function = request.getParameter("function");
			username = getStringFromSession(session, "username");
			
			String week = (request.getParameter("week"));
			String group = db.getGroup(username);
			String role = db.getRole(username);
			//String selectedWeek = (request.getParameter("SelectedWeek"));
			String selectedWeek = (request.getParameter("SelectedWeek"));
	
	
			String[] paramName = request.getParameterValues("activities");
		
			int actID;
			int[] actIDs = {11, 12, 13, 14, 21, 22, 23, 24, 31, 32, 33, 34, 41, 42, 43, 44, 51, 52, 53, 54,
					61, 62, 63, 64, 71, 72, 73, 74, 81, 82, 83, 84, 91, 92, 93, 94,
					110, 120, 130, 140, 150, 160, 170, 180, 190,
					404};
	
	
			if (paramName != null) {
				for (int i = 0; i < paramName.length; i++) {
	
					actID = actIDs[i];
	
					try {
						if (paramName[i] == "") activities.put(actID, 0);
						else activities.put(actID, Integer.parseInt(paramName[i]));
	
					} catch (NumberFormatException e) {
						responseList.add("In some way you have entered null in a textfield, please try again.");
					}
				}
			}
	
	
			String html = "<h1>Time reporting</h1>\n";
	
			if (function == null) { // No function, list available options
	
				html += generateTimeReportMenu(username, role);
	
	
			}	else 
				switch (function) {
	
				case "createTimeReportForm":
					html += (createNewTimeReportForm(session));
	
					break;
	
				case "updateTimeReportListForm":
					html += (updateTimeReportListForm(session));
					break;
	
				case "Cancel":
					html += generateTimeReportMenu(username, role);
					break;
	
				case "createTimeReport":
	
					if (week != "") {
						TimeReport trNew = new TimeReport(username, group, Integer.parseInt(week), false, new java.sql.Date(dateCreated.getTime()), activities);
						if(db.insertTimeReport(trNew)) {
	
							responseList.add("This timereport was sucessfully created");
							html += generateTimeReportMenu(username, role);
	
						} else {
							responseList.add("Timereport was not created, timereport with same week already exists");
							html += (createNewTimeReportForm(session));
						}
	
	
					} else {
	
						responseList.add("The following fields must have a value: Week");
	
						function = "createTimeReport";
						html += (createNewTimeReportForm(session));
					}
					break;
	
				case "updateTimeReport":
	
					if (week != "") {
	
	
						TimeReport trUpdate = new TimeReport(username, group, Integer.parseInt(week), false, new java.sql.Date(dateCreated.getTime()), activities);
	
						if (updateWeek == Integer.parseInt(week)){
							db.deleteTimeReport(username, Integer.parseInt(week));
							db.insertTimeReport(trUpdate);
							responseList.add("Timereport was updated");
							html += generateTimeReportMenu(username, role);
	
						}else{
							if (isDuplicateEntry(session, week)) {
								responseList.add("Week already exists");
								html += generateTimeReportMenu(username, role);
	
							} else {
								db.deleteTimeReport(username, updateWeek);
								db.insertTimeReport(trUpdate);
								responseList.add("Timereport was updated");
								html += generateTimeReportMenu(username, role);
							} 
	
						}
					} else {
						responseList.add("Week cannot be empty");
						function = "createTimeReport";
						html += (generateTimeReportMenu(username, role));
					}
	
	
					break;
	
				case "updateTimeReportForm": 
					html += updateTimeReportForm(session, selectedWeek);
					break;
	
				case "viewTimeReportListForm":
					html += (viewTimeReportListForm(session));
					break;
	
				case "ViewTimeReport":
					String reportUser = username;
	
					if (username.equals("admin") || (role != null && role.equals("PG"))) {
						String paraUsername = request.getParameter("username");
	
						if (paraUsername != null)
							reportUser = paraUsername;
					}
	
					html += viewTimeReportForm(session, selectedWeek, reportUser);
					break;
	
				case "viewSummaryListForm":
					html += (viewSummaryList(session));
					break;
	
				case "ViewSummaryForm":
					html += (viewSummary(session, selectedWeek));
					break;
					
				case "ViewAllSummaryForm":
					html += (viewAllSummary(session, group));
					break;
	
				default: // Unknown function
					response.sendRedirect("TimeReporting");
					break;
				}
	
			out.println(html);
			out.println(getPageOutro()); //Append html-trailer
		}
	}

	/**
	 * Creates String with HTML code to create a time report form
	 * @param session
	 * @return String with HTML code to create a time report form
	 */
	private String createNewTimeReportForm(HttpSession session) {


		User user = db.getUser(getStringFromSession(session, "username"));

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String html = "<h2>Create new time report</h2><form name='input' method='post get'" +
				"	<p>" +
				"	<table class='timereporting'>"+
				"	<tr>"+
				"	<td colspan=2><b>Name:</b></td><td colspan=2 class='tdwithborders'>" + user.getUsername() + "</td>"+
				"	<td colspan=2 name='date' value='weeew' class='tdwithborders'><b>Date:</b></td><td>" + dateFormat.format(new java.util.Date()).replace(" ", "<br>") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td colspan=2 ><b>Project group</b>:</td><td class='tdwithborders' colspan=2>" + user.getGroup()+ "</td>"+
				"	<td colspan=2 ><b>Week:</b></td><td> <input type='number' max='52' min='1' name='week' onkeypress='return isNumber(event)'/></td>"+
				"	</tr>"+
				"	<tr><td class='tdwithborders markup' colspan=6><font size=+1><b>Total time this week</b></font></td><td></td></tr>"+
				"	<tr><td class='tdwithborders markup' colspan=7><font size=+1><b>Number of minutes per activity</b></font></td></tr><tr>"+
				"	<th>Number</th><th>Activity</th><th width=75>D <font color=\"grey\" title=\"Developing new code, test cases and documentation including documentation of the system\">(?)</font></th><th width=75>I <font color=\"grey\" title=\"Time spent preparing and at meeting for informal reviews\">(?)</font></th><th width=75>F <font color=\"grey\" title=\"Time spent preparing and at meeting for formal reviews\">(?)</font></th><th width=75>R <font color=\"grey\" title=\"Time spent improving, revising or correction documents and design objects\">(?)</font></th><th>Total time</th>"+
				"	</tr>"+
				"	<tr>"+
				"	<td>11</td>"+
				"	<td>SDP</td>"+
				"	<td id='11'><input type ='text' name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td id='12'><input type ='text' name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td id='13'><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/'></td>"+
				"	<td id='14'><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/'></td>"+
				"	<td id='15'></td></form>"+
				"	</tr>"+
				"	<tr>"+
				"	<td>12</td>"+
				"	<td>SRS</td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td>13</td>"+
				"	<td>SVVS</td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td>14</td>"+
				"	<td>STLDD</td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td>15</td>"+
				"	<td>SVVI</td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td>16</td>"+
				"	<td>SDDD</td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td>17</td>"+
				"	<td>SVVR</td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td>18</td>"+
				"	<td>SSD</td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td>19</td>"+
				"	<td>Final Report</td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	<td></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<th>Sum</th>"+
				"	<td style=\"background-color: lightgrey;\"/>"+
				"	<td></td>"+
				"	<td></td>"+
				"	<td></td>"+
				"	<td></td>"+
				"	<td style=\"background-color: lightgrey;\"/>"+
				"	</tr>"+
				"	<tr>"+
				"	<td>21</td>"+
				"	<td colspan=5 >Functional test</td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td>22</td>"+
				"	<td colspan=5>System test</td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td>23</td>"+
				"	<td colspan=5>Regression test</td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td>30</td>"+
				"	<td colspan=5>Meeting</td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td>41</td>"+
				"	<td colspan=5>Lecture</td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td>42</td>"+
				"	<td colspan=5>Exercise</td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td>43</td>"+
				"	<td colspan=5>Computer exercise</td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td>44</td>"+
				"	<td colspan=5>Home reading</td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td>100</td>"+
				"	<td colspan=5>Other</td>"+
				"	<td><input type =\"text\" name='activities' maxlength='5' onkeypress='return isNumber(event)'/></td>"+
				"	</tr>"+
				"	<!--<tr><td colspan=7 class=\"markup\" NOWRAP><b><font size=+1>Part C: Time spent at different types of sub activities</font></b><br>(The values are summed up automatically)</td></tr>"+
				"	<tr><th colspan=2>Activity type</th><th>Activity code</th><th colspan=3>Description</th><th>Sum</th></tr>"+
				"	<tr>"+
				"	<td colspan=2>Development and<br>documentation</td>"+
				"	<td>D</td>"+
				"	<td colspan=3>Developing new code, test cases<br>and documentation including<br>documentation of the system</td>"+
				"	<td><input type =\"text\" name='activities'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td colspan=2>Informal review</td>"+
				"	<td>I</td>"+
				"	<td colspan=3>Time spent preparing and at<br>meeting for informal reviews</td>"+
				"	<td><input type =\"text\" name='activities'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td colspan=2>Formal reviews</td>"+
				"	<td>F</td>"+
				"	<td colspan=3>Time spent preparing and at<br>meeting for formal reviews</td>"+
				"	<td><input type =\"text\" name='activities'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td colspan=2>Rework,<br>improvement or<br>correction</td>"+
				"	<td>R</td>"+
				"	<td colspan=3>Time spent improving, revising<br>or correction documents and design<br>objects</td>"+
				"	<td><input type =\"text\" name='activities'></td>"+
				"	</tr>-->"+
				"	<tr>"+
				"	<td class='tdwithborders markup' colspan=6><font size=+1><b>Signed</b></font></td><td></td></tr>"+
				"	</table>"+
				"   </form>"+
				"	<p><button type='submit' name='function' value='Cancel' style='float: right;'>Cancel</button>" +
				"	<p><button type='submit' name='function' value='createTimeReport' style='float: right;'>Create</button>" +
				"	</form>\n";

		return html;
	}

	/**
	 * Creates String with HTML code to view reports from a specific user
	 * @param session
	 * @return String with HTML code to view reports from a specific user
	 */
	private String viewTimeReportListForm(HttpSession session) {
		User user = db.getUser(getStringFromSession(session, "username"));
	
		List<TimeReport> reports = db.getTimeReportUser(user.getUsername());
		String html = 
				 "<h2> View time reports </h2>"+
				 "<form name='input' method='post get'" +
				 "</form>"+	
				 "<input type='text' name ='SelectedWeek' id='SelectedWeek' placeholder='Enter week here'>"+
				 "<table class = 'list'>" +
				
				 "<tr>" +
				 "<th></th>" +
				 "<th>Latest update</th>"+
				 "<th>Week</th>"+
				 "<th>Development</th>"+
				 "<th>Informal review</th>"+
				 "<th>Formal review</th>" +
				 "<th>Rework</th>" +
				 "<th>Other</th>" +
				 "<th>Total time</th>" +
				 "<th>Signed</th>"
				 + "</tr>";
	
		for (int i = 0; i < reports.size(); i++) {
			html += 
					"<tr>"+
							"<td> <input type='radio' value='" + reports.get(i).getWeek() + "' name ='weekselectors' onchange='findWeek();'> </td>"+
							"<td>" + reports.get(i).getDate() + "</td>"+
							"<td>" + reports.get(i).getWeek() + "</td>"+
							"<td>" + reports.get(i).getSum("D") + "</td>"+
							"<td>" + reports.get(i).getSum("I") + "</td>"+
							"<td>" + reports.get(i).getSum("F") + "</td>"+
							"<td>" + reports.get(i).getSum("R") + "</td>"+
							"<td>" + reports.get(i).getSum("Other") + "</td>"+
							"<td>" + reports.get(i).getTotalTime() + "</td>"+
							"<td>" + convertSigned(reports.get(i).isSigned()) + "</td>"+
							"</tr>";
		}
	
		html += "<button type='submit' name='function' value='ViewTimeReport' method='post get'>View selected time report</button>";
	
		return html;
	}

	
	/**
	 * Creates String with HTML code to view a time report for a specific week
	 * @param session
	 * @param selectedWeek The selected week
	 * @param reportUser The current user
	 * @return String with HTML code to view a time report for a specific week
	 */
	private String viewTimeReportForm(HttpSession session, String selectedWeek, String reportUser) {
		User user = db.getUser(reportUser);
		User accUser = db.getUser(username);
	
		if (user == null || !user.getGroup().equals(accUser.getGroup()))
			user = accUser;
	
		updateWeek = Integer.parseInt(selectedWeek);

		HashMap<Integer, Integer> activities = db.getTimeReportTimes(user.getUsername(), Integer.parseInt(selectedWeek));
		ArrayList<String> attributes = db.getTimeReportAttributes(user.getUsername(), Integer.parseInt(selectedWeek));

		TimeReport tr = new TimeReport(user.getUsername(), attributes.get(1), Integer.parseInt(selectedWeek), false, new Date(12345678), activities);
		String actualDate = attributes.get(5);

		String html = 
				"<h2>View Time Report</h2><p>" + 
				"<form name='input' method='post get'" +
				"	<p>" +
				"	<table class = 'timereporting'>"+
				"	<tr>"+
				"	<td colspan=2 ><b>Name:</b></td><td colspan=2 class='tdwithborders'>" + user.getUsername() + "</td>"+
				"	<td colspan=2 name='date' value='weeew' class='tdwithborders'><b>Date:</b></td><td>" + actualDate.replace(" ", "<br>") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td colspan=2 ><b>Project group:</b></td><td class='tdwithborders' colspan=2>" + tr.getGroup() + "</td>"+
				"	<td colspan=2 class='tdwithborders'><b>Week:</b></td><td>" + selectedWeek + "</td>"+
				"	</tr>"+
				"	<tr><td class='tdwithborders markup' colspan=6><font size=+1><b>Total time this week</b></font></td><td>" + tr.getTotalTime() + "</td></tr>"+
				"	<tr><td class='tdwithborders markup' colspan=7><font size=+1><b>Number of minutes per activity</b></font></td></tr>" +
				"   <tr>"+
				"	<th>Number</th><th>Activity</th>"
				+ "<th width=75>D <font color=\"grey\" title=\"Developing new code, test cases and documentation including documentation of the system\">(?)</font></th>"
				+ "<th width=75>I <font color=\"grey\" title=\"Time spent preparing and at meeting for informal reviews\">(?)</font></th>"
				+ "<th width=75>F <font color=\"grey\" title=\"Time spent preparing and at meeting for formal reviews\">(?)</font></th>"
				+ "<th width=75>R <font color=\"grey\" title=\"Time spent improving, revising or correction documents and design objects\">(?)</font></th>"
				+ "<th>Total time</th>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>11</td>"+
				"	<td class='tdwithborders'>SDP</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(11) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(12) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(13) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(14) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SDP") + "</td></form>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>12</td>"+
				"	<td class='tdwithborders'>SRS</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(21) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(22) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(23) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(24) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SRS") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>13</td>"+
				"	<td class='tdwithborders'>SVVS</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(31) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(32) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(33) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(34) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SVVS") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>14</td>"+
				"	<td class='tdwithborders'>STLDD</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(41) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(42) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(43) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(44) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("STLDD") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>15</td>"+
				"	<td class='tdwithborders'>SVVI</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(51) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(52) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(53) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(54) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SVVI") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>16</td>"+
				"	<td class='tdwithborders'>SDDD</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(61) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(62) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(63) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(64) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SDDD") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>17</td>"+
				"	<td class='tdwithborders'>SVVR</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(71) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(72) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(73) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(74) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SVVR") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>18</td>"+
				"	<td class='tdwithborders'>SSD</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(81) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(82) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(83) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(84) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SSD") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>19</td>"+
				"	<td class='tdwithborders'>Final Report</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(91) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(92) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(93) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(94) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("Final Report") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<th class='tdwithborders'>Sum</th>"+
				"	<td class='tdwithborders' style=\"background-color: lightgrey;\"/>"+
				"	<td class='tdwithborders'>" + tr.getSum("D") + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("I") + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("F") + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("R") + "</td>"+
				"	<td class='tdwithborders' style=\"background-color: lightgrey;\"/>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>21</td>"+
				"	<td colspan=5 class='tdwithborders'>Functional test</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(110) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>22</td>"+
				"	<td class='tdwithborders' colspan=5>System test</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(120) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>23</td>"+
				"	<td class='tdwithborders' colspan=5>Regression test</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(130) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>30</td>"+
				"	<td class='tdwithborders' colspan=5>Meeting</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(140) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>41</td>"+
				"	<td class='tdwithborders' colspan=5>Lecture</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(150) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>42</td>"+
				"	<td class='tdwithborders' colspan=5>Exercise</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(160) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>43</td>"+
				"	<td class='tdwithborders' colspan=5>Computer exercise</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(170) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>44</td>"+
				"	<td class='tdwithborders' colspan=5>Home reading</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(180) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>100</td>"+
				"	<td class='tdwithborders' colspan=5>Other</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(190) + "</td>"+		
				"	</tr>"+
				"	<!--<tr><td colspan=7 class=\"markup\" NOWRAP><b><font size=+1>Part C: Time spent at different types of sub activities</font></b><br>(The values are summed up automatically)</td></tr>"+
				"	<tr><th colspan=2>Activity type</th><th>Activity code</th><th colspan=3>Description</th><th>Sum</th></tr>"+
				"	<tr>"+
				"	<td class='tdwithborders' colspan=2>Development and<br>documentation</td>"+
				"	<td class='tdwithborders'>D</td>"+
				"	<td class='tdwithborders' colspan=3>Developing new code, test cases<br>and documentation including<br>documentation of the system</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders' colspan=2>Informal review</td>"+
				"	<td class='tdwithborders'>I</td>"+
				"	<td class='tdwithborders' colspan=3>Time spent preparing and at<br>meeting for informal reviews</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders' colspan=2>Formal reviews</td>"+
				"	<td class='tdwithborders'>F</td>"+
				"	<td class='tdwithborders' colspan=3>Time spent preparing and at<br>meeting for formal reviews</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders' colspan=2>Rework,<br>improvement or<br>correction</td>"+
				"	<td class='tdwithborders'>R</td>"+
				"	<td class='tdwithborders' colspan=3>Time spent improving, revising<br>or correction documents and design<br>objects</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities'></td>"+
				"	</tr>-->"+
				"	<tr>"+
				"	<td class='tdwithborders markup' colspan=6><font size=+1><b>Signed</b></font></td><td>" + convertSigned(attributes.get(4)) + "</td></tr>"+

				"	</table>"+
				"   </form>";

	
		return html;
	}
	
	/**
	 * Creates a String with HTML code to view a a list of time report from the currently user
	 * @param session
	 * @return String with HTML code to view a a list of time report from the currently user
	 */
	private String updateTimeReportListForm(HttpSession session) {
		User user = db.getUser(getStringFromSession(session, "username"));
	
		List<TimeReport> reports = db.getTimeReportUserSigned(user.getUsername());
		String html = "<html>"+
				""+
				"<head>"+
				"</head>"+
				 "<h2>Update time reports</h2>"+
				 ""+"<form name='input' method='post get'" +
				 "</form>"+	
				 "<input type='text' name ='SelectedWeek' id='SelectedWeek' placeholder='Enter week here'>"+
				 "<table class = 'list'>"
				 + "<tr>"
				 + "<th></th>" +
				 "<th>Latest update</th>"+
				 "<th>Week</th>"+
				 "<th>Development</th>"+
				 "<th>Informal review</th>"+
				 "<th>Formal review</th>" +
				 "<th>Rework</th>" +
				 "<th>Other</th>" +
				 "<th>Total time</th>" +
				 "<th>Signed</th>"
				 + "</tr>";
	
		for (int i = 0; i < reports.size(); i++) {
			html += 
					"<tr>"+
							"<td> <input type='radio' value='" + reports.get(i).getWeek() + "' name ='weekselectors' onchange='findWeek();'> </td>"+
							"<td>" + reports.get(i).getDate() + "</td>"+
							"<td>" + reports.get(i).getWeek() + "</td>"+
							"<td>" + reports.get(i).getSum("D") + "</td>"+
							"<td>" + reports.get(i).getSum("I") + "</td>"+
							"<td>" + reports.get(i).getSum("F") + "</td>"+
							"<td>" + reports.get(i).getSum("R") + "</td>"+
							"<td>" + reports.get(i).getSum("Other") + "</td>"+
							"<td>" + reports.get(i).getTotalTime() + "</td>"+
							"<td>" + convertSigned(reports.get(i).isSigned()) + "</td>"+
							"</tr>";
		}
		html += 
				"<button type='submit' name='function' value='updateTimeReportForm' method='post get'>Update selected time report</button>";
	
	
		return html;
	
	}
	
	
	/**
	 * Creates String with HTML code to view and edit a time report for a specific week
	 * @param session
	 * @param selectedWeek The selected week
	 * @return String with HTML code to view and edit a time report for a specific week
	 */
	private String updateTimeReportForm(HttpSession session, String selectedWeek) {
		User user = db.getUser(getStringFromSession(session, "username"));
		updateWeek = Integer.parseInt(selectedWeek);
	
		HashMap<Integer, Integer> activities = db.getTimeReportTimes(user.getUsername(), Integer.parseInt(selectedWeek));
		ArrayList<String> attributes = db.getTimeReportAttributes(user.getUsername(), Integer.parseInt(selectedWeek));
		TimeReport tr = new TimeReport(user.getUsername(), user.getGroup(), Integer.parseInt(selectedWeek), false, new Date(12345678), activities);
	
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	
		String html = "<h2> Update time report</h2>"
				+ "	<p><form name='input' method='post get'" +
	
				"	<p>" +
				"	<table class = 'timereporting'>"+
				"	<tr>"+
				"	<td colspan=2 ><b>Name:</b></td><td colspan=2 class='tdwithborders'>" + user.getUsername() + "</td>"+
				"	<td colspan=2 class='tdwithborders'><b>Date:</b></td><td>" + dateFormat.format(new java.util.Date()).replace(" ", "<br>") + "</td>"+
	
				"	</tr>"+
				"	<tr>"+
				"	<td colspan=2 ><b>Project group:</b></td><td class='tdwithborders' colspan=2>" + user.getGroup()+ "</td>"+
				"	<td colspan=2 class='tdwithborders'><b>Week:</b></td><td> <input type='number' max ='52' min ='1' name='week' value='" + selectedWeek + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	</tr>"+
				"	<tr><td class='tdwithborders markup' colspan=6><font size=+1><b>Total time this week</b></font></td><td>" + tr.getTotalTime() + "</td></tr>"+
				"	<tr><td class='tdwithborders markup' colspan=7><font size=+1><b>Number of minutes per activity</b></font></td></tr><tr>"+
				"	<th>Number</th>"
				+ "<th>Activity</th>"
				+ "<th width=75>D <font color=\"grey\" title=\"Developing new code, test cases and documentation including documentation of the system\">(?)</font></th>"
				+ "<th width=75>I <font color=\"grey\" title=\"Time spent preparing and at meeting for informal reviews\">(?)</font></th>"
				+ "<th width=75>F <font color=\"grey\" title=\"Time spent preparing and at meeting for formal reviews\">(?)</font></th>"
				+ "<th width=75>R <font color=\"grey\" title=\"Time spent improving, revising or correction documents and design objects\">(?)</font></th><th>Total time</th>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>11</td>"+
				"	<td class='tdwithborders'>SDP</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(11) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(12) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(13) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(14) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SDP") + "</td></form>"+
	
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>12</td>"+
				"	<td class='tdwithborders'>SRS</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(21) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(22) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(23) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(24) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SRS") + "</td>"+
	
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>13</td>"+
				"	<td class='tdwithborders'>SVVS</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(31) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(32) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(33) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(34) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SVVS") + "</td>"+
	
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>14</td>"+
				"	<td class='tdwithborders'>STLDD</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(41) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(42) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(43) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(44) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'>" + tr.getSum("STLDD") + "</td>"+
	
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>15</td>"+
				"	<td class='tdwithborders'>SVVI</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(51) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(52) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(53) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(54) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SVVI") + "</td>"+
	
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>16</td>"+
				"	<td class='tdwithborders'>SDDD</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(61) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(62) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(63) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(64) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SDDD") + "</td>"+
	
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>17</td>"+
				"	<td class='tdwithborders'>SVVR</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(71) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(72) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(73) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(74) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SVVR") + "</td>"+
	
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>18</td>"+
				"	<td class='tdwithborders'>SSD</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(81) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(82) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(83) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(84) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SSD") + "</td>"+
	
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>19</td>"+
				"	<td class='tdwithborders'>Final Report</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(81) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(82) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(83) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(84) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	<td class='tdwithborders'>" + tr.getSum("Final Report") + "</td>"+
				"	</tr>"+
	
				"	<tr>"+
				"	<th class='tdwithborders'>Sum</th>"+
				"	<td class='tdwithborders' style=\"background-color: lightgrey;\"/>"+
				"	<td class='tdwithborders'>" + tr.getSum("D") + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("I") + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("F") + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("R") + "</td>"+
				"	<td class='tdwithborders' style=\"background-color: lightgrey;\"/>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>21</td>"+
				"	<td colspan=5 class='tdwithborders'>Functional test</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(110) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>22</td>"+
				"	<td class='tdwithborders' colspan=5>System test</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(120) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>23</td>"+
				"	<td class='tdwithborders' colspan=5>Regression test</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(130) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>30</td>"+
				"	<td class='tdwithborders' colspan=5>Meeting</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(140) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>41</td>"+
				"	<td class='tdwithborders' colspan=5>Lecture</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(150) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>42</td>"+
				"	<td class='tdwithborders' colspan=5>Exercise</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(160) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>43</td>"+
				"	<td class='tdwithborders' colspan=5>Computer exercise</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(170) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>44</td>"+
				"	<td class='tdwithborders' colspan=5>Home reading</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(180) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>100</td>"+
				"	<td class='tdwithborders' colspan=5>Other</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities', value='" + tr.getActivityTime(190) + "'maxlength='5' onkeypress='return isNumber(event)'></td>"+
				"	</tr>"+
				"	<!--<tr><td colspan=7 class=\"markup\" NOWRAP><b><font size=+1>Part C: Time spent at different types of sub activities</font></b><br>(The values are summed up automatically)</td></tr>"+
				"	<tr><th colspan=2>Activity type</th><th>Activity code</th><th colspan=3>Description</th><th>Sum</th></tr>"+
				"	<tr>"+
				"	<td class='tdwithborders' colspan=2>Development and<br>documentation</td>"+
				"	<td class='tdwithborders'>D</td>"+
				"	<td class='tdwithborders' colspan=3>Developing new code, test cases<br>and documentation including<br>documentation of the system</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders' colspan=2>Informal review</td>"+
				"	<td class='tdwithborders'>I</td>"+
				"	<td class='tdwithborders' colspan=3>Time spent preparing and at<br>meeting for informal reviews</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders' colspan=2>Formal reviews</td>"+
				"	<td class='tdwithborders'>F</td>"+
				"	<td class='tdwithborders' colspan=3>Time spent preparing and at<br>meeting for formal reviews</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders' colspan=2>Rework,<br>improvement or<br>correction</td>"+
				"	<td class='tdwithborders'>R</td>"+
				"	<td class='tdwithborders' colspan=3>Time spent improving, revising<br>or correction documents and design<br>objects</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities'></td>"+
				"	</tr>-->"+
				"	<tr>"+
				"	<td class='tdwithborders markup' colspan=6><font size=+1><b>Signed</b></font></td><td>" + convertSigned(attributes.get(4)) + "</td></tr>"+
				"	</tr>"+
				"	</table>"+
				"   </form>"+
				"	<p><button type='submit' name='function' value='Cancel' style='float: right;'>Cancel</button>" +
				"	<p><button type='submit' name='function' value='updateTimeReport' style='float: right;'>Update</button>" +
				"	</form>\n";
	
		return html;
	
	}
	/**
	 * Creates String with HTML code with a list of summaries for each week's time reports from the current user's group
	 * @param session
	 * @return String with HTML code with a list of summaries for each week's time reports from the current user's group
	 */
	private String viewSummaryList(HttpSession session) {
		User user = db.getUser(getStringFromSession(session, "username"));

		List<Integer> weekNumbers = db.getGroupWeeks(user.getGroup());

		ArrayList<TimeReport> reports = new ArrayList<TimeReport>();

		for (int i = 0; i < weekNumbers.size(); i++) {
			reports.add(new TimeReport("Summariser", user.getGroup(), weekNumbers.get(i), 
			false, new java.sql.Date(new java.util.Date().getTime()), 
			db.sumAllByWeekGroup(weekNumbers.get(i), user.getGroup())));
		}



		String html =
				"<h2>View Summaries</h2>" +
				"<form name='input' method='post get'" +
				"</form>"+
				"<input type='text' name='SelectedWeek' id='SelectedWeek' placeholder='Enter week here'>"+
				"<button type='submit' name='function' value='ViewSummaryForm' method='post get'>View</button>"+
				"<button type='submit' name='function' value='ViewAllSummaryForm' method='post get'>View All</button>"+
				
				"<table class='list'>"+
				
				"<tr>"
				+ "<td></td>"+
				"<th>Group</th>"+
				"<th>Latest update</th>" +
				"<th>Week</th>" +
				"<th>Development</th>"+
				"<th>Informal review</th>"+
				"<th>Formal review</th>"+
				"<th>Rework</th>"+
				"<th>Other</th>"+
				"<th>Total time</th>"+
				"</tr>";
		
		for (int i = 0; i < reports.size(); i++) {
			html +=
				"<tr>"+		
					"<td><input type='radio' name='weekselectors' value='" + reports.get(i).getWeek() + "' onchange='findWeek();'></td>"+
					"<td>" + reports.get(i).getGroup() + "</td>"+
					"<td>" + reports.get(i).getDate() + "</td>"+
					"<td>" + reports.get(i).getWeek() + "</td>"+
					"<td>" + reports.get(i).getSum("D") + "</td>"+
					"<td>" + reports.get(i).getSum("I") + "</td>"+
					"<td>" + reports.get(i).getSum("F") + "</td>"+
					"<td>" + reports.get(i).getSum("R") + "</td>"+
					"<td>" + reports.get(i).getSum("Other") + "</td>"+
					"<td>" + reports.get(i).getTotalTime() + "</td>"+
				"</tr>";
		}
		html+=	"</table>";

		return html;
	}

	/**
	 * Creates  String with HTML code to view a summary of all time reports for a specific weeks from the user's group
	 * @param session
	 * @param selectedWeek The selected week
	 * @return String with HTML code to view a summary of all time reports for a specific weeks from the user's group
	 * 
	 */
	private String viewSummary(HttpSession session, String selectedWeek) {

		User user = db.getUser(getStringFromSession(session, "username"));
		updateWeek = Integer.parseInt(selectedWeek);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


		TimeReport tr = new TimeReport("Summariser", user.getGroup(), Integer.parseInt(selectedWeek),
				false, new java.sql.Date(new java.util.Date().getTime()),
				db.sumAllByWeekGroup(Integer.parseInt(selectedWeek), user.getGroup()));

		String html = 

				"   <h2>View Summary</h2><p><form name='input' method='post get'" +
				"	<p>" +
				"	<table class='timereporting'>"+
				"	<tr>"+
				"	<td colspan=2 ><b>Name:</b></td><td colspan=2 class='tdwithborders'>" + user.getUsername() + "</td>"+
				"	<td colspan=2 class='tdwithborders'><b></b></td><td></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td colspan=2 ><b>Project group:</b></td><td class='tdwithborders' colspan=2>" + user.getGroup()+ "</td>"+
				"	<td colspan=2 class='tdwithborders'><b>Week:</b></td><td>" + selectedWeek + "</td>"+
				"	</tr>"+
				"	<tr><td class='tdwithborders markup' colspan=6><font size=+1><b>Total time this week</b></font></td><td>" + tr.getTotalTime() + "</td></tr>"+
				"	<tr><td class='tdwithborders markup' colspan=6><font size=+1><b>Number of minutes per activity</b></font></td><td class='tdwithborders'></td></tr><tr>"+
				"	<th>Number</th><th>Activity</th><th width=75>D <font color=\"grey\" title=\"Developing new code, test cases and documentation including documentation of the system\">(?)</font></th><th width=75>I <font color=\"grey\" title=\"Time spent preparing and at meeting for informal reviews\">(?)</font></th><th width=75>F <font color=\"grey\" title=\"Time spent preparing and at meeting for formal reviews\">(?)</font></th><th width=75>R <font color=\"grey\" title=\"Time spent improving, revising or correction documents and design objects\">(?)</font></th><th>Total time</th>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>11</td>"+
				"	<td class='tdwithborders'>SDP</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(11) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(12) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(13) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(14) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SDP") + "</td></form>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>12</td>"+
				"	<td class='tdwithborders'>SRS</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(21) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(22) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(23) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(24) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SRS") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>13</td>"+
				"	<td class='tdwithborders'>SVVS</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(31) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(32) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(33) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(34) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SVVS") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>14</td>"+
				"	<td class='tdwithborders'>STLDD</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(41) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(42) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(43) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(44) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("STLDD") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>15</td>"+
				"	<td class='tdwithborders'>SVVI</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(51) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(52) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(53) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(54) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SVVI") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>16</td>"+
				"	<td class='tdwithborders'>SDDD</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(61) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(62) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(63) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(64) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SDDD") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>17</td>"+
				"	<td class='tdwithborders'>SVVR</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(71) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(72) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(73) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(74) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SVVR") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>18</td>"+
				"	<td class='tdwithborders'>SSD</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(81) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(82) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(83) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(84) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SSD") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>19</td>"+
				"	<td class='tdwithborders'>Final Report</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(91) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(92) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(93) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(94) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("Final Report") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<th class='tdwithborders'>Sum</th>"+
				"	<td class='tdwithborders' style=\"background-color: lightgrey;\"/>"+
				"	<td class='tdwithborders'>" + tr.getSum("D") + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("I") + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("F") + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("R") + "</td>"+
				"	<td class='tdwithborders' style=\"background-color: lightgrey;\"/>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>21</td>"+
				"	<td colspan=5 class='tdwithborders'>Functional test</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(110) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>22</td>"+
				"	<td class='tdwithborders' colspan=5>System test</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(120) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>23</td>"+
				"	<td class='tdwithborders' colspan=5>Regression test</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(130) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>30</td>"+
				"	<td class='tdwithborders' colspan=5>Meeting</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(140) + "</td>"+	
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>41</td>"+
				"	<td class='tdwithborders' colspan=5>Lecture</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(150) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>42</td>"+
				"	<td class='tdwithborders' colspan=5>Exercise</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(160) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>43</td>"+
				"	<td class='tdwithborders' colspan=5>Computer exercise</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(170) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>44</td>"+
				"	<td class='tdwithborders' colspan=5>Home reading</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(180) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>100</td>"+
				"	<td class='tdwithborders' colspan=5>Other</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(190) + "</td>"+		
				"	</tr>"+
				"	<!--<tr><td colspan=7 class=\"markup\" NOWRAP><b><font size=+1>Part C: Time spent at different types of sub activities</font></b><br>(The values are summed up automatically)</td></tr>"+
				"	<tr><th colspan=2>Activity type</th><th>Activity code</th><th colspan=3>Description</th><th>Sum</th></tr>"+
				"	<tr>"+
				"	<td class='tdwithborders' colspan=2>Development and<br>documentation</td>"+
				"	<td class='tdwithborders'>D</td>"+
				"	<td class='tdwithborders' colspan=3>Developing new code, test cases<br>and documentation including<br>documentation of the system</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders' colspan=2>Informal review</td>"+
				"	<td class='tdwithborders'>I</td>"+
				"	<td class='tdwithborders' colspan=3>Time spent preparing and at<br>meeting for informal reviews</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders' colspan=2>Formal reviews</td>"+
				"	<td class='tdwithborders'>F</td>"+
				"	<td class='tdwithborders' colspan=3>Time spent preparing and at<br>meeting for formal reviews</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders' colspan=2>Rework,<br>improvement or<br>correction</td>"+
				"	<td class='tdwithborders'>R</td>"+
				"	<td class='tdwithborders' colspan=3>Time spent improving, revising<br>or correction documents and design<br>objects</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities'></td>"+
				"	</tr>-->"+
				"	<tr>"+
				"	<td class='tdwithborders markup' colspan=6><font size=+1><b>Signature</b></font></td><td>" + convertSigned(tr.isSigned()) + "</td></tr>"+
				"	</table>"+
				"   </form>";


		return html;
	}
	/**
	 * Creates String with HTML code to view all the summary of all time reports for all weeks for the user's group
	 * @param session
	 * @param group The user's current group
	 * @return String with HTML code to view all the summary of all time reports for all weeks for the user's group
	 */
	private String viewAllSummary(HttpSession session, String group) {

		User user = db.getUser(getStringFromSession(session, "username"));
		
		TimeReport tr = new TimeReport("Summariser", user.getGroup(), Integer.parseInt("0"), false, new java.sql.Date(new java.util.Date().getTime()), db.sumAllByGroup(user.getGroup()));

		String html = 
				"<script>"
				+ "function goBack() {"
				+ "window.history.back();"
				+ "}"
				+ "</script>"+

				"<h2>View Summary</h2><p><form name='input' method='post get'" +
				"	<p>" +
				"	<table class='timereporting'>"+
				"	<tr>"+
				"	<td colspan=2 ><b>Name:</b></td><td colspan=2 class='tdwithborders'>" + user.getUsername() + "</td>"+
				"	<td colspan=2 class='tdwithborders'><b>Date:</b></td><td>-</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td colspan=2 ><b>Project group:</b></td><td class='tdwithborders' colspan=2>" + user.getGroup()+ "</td>"+
				"	<td colspan=2 class='tdwithborders'><b>Week:</b></td><td>" + "All weeks" + "</td>"+
				"	</tr>"+
				"	<tr><td class='tdwithborders markup' colspan=6><font size=+1><b>Total time this week</b></font></td><td>" + tr.getTotalTime() + "</td></tr>"+
				"	<tr><td class='tdwithborders markup' colspan=6><font size=+1><b>Number of minutes per activity</b></font></td><td class='tdwithborders'></td></tr><tr>"+
				"	<th>Number</th><th>Activity</th><th width=75>D <font color=\"grey\" title=\"Developing new code, test cases and documentation including documentation of the system\">(?)</font></th><th width=75>I <font color=\"grey\" title=\"Time spent preparing and at meeting for informal reviews\">(?)</font></th><th width=75>F <font color=\"grey\" title=\"Time spent preparing and at meeting for formal reviews\">(?)</font></th><th width=75>R <font color=\"grey\" title=\"Time spent improving, revising or correction documents and design objects\">(?)</font></th><th>Total time</th>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>11</td>"+
				"	<td class='tdwithborders'>SDP</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(11) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(12) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(13) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(14) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SDP") + "</td></form>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>12</td>"+
				"	<td class='tdwithborders'>SRS</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(21) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(22) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(23) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(24) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SRS") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>13</td>"+
				"	<td class='tdwithborders'>SVVS</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(31) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(32) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(33) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(34) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SVVS") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>14</td>"+
				"	<td class='tdwithborders'>STLDD</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(41) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(42) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(43) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(44) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("STLDD") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>15</td>"+
				"	<td class='tdwithborders'>SVVI</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(51) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(52) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(53) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(54) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SVVI") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>16</td>"+
				"	<td class='tdwithborders'>SDDD</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(61) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(62) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(63) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(64) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SDDD") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>17</td>"+
				"	<td class='tdwithborders'>SVVR</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(71) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(72) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(73) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(74) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SVVR") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>18</td>"+
				"	<td class='tdwithborders'>SSD</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(81) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(82) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(83) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(84) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("SSD") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>19</td>"+
				"	<td class='tdwithborders'>Final Report</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(91) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(92) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(93) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(94) + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("Final Report") + "</td>"+
				"	</tr>"+
				"	<tr>"+
				"	<th class='tdwithborders'>Sum</th>"+
				"	<td class='tdwithborders' style=\"background-color: lightgrey;\"/>"+
				"	<td class='tdwithborders'>" + tr.getSum("D") + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("I") + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("F") + "</td>"+
				"	<td class='tdwithborders'>" + tr.getSum("R") + "</td>"+
				"	<td class='tdwithborders' style=\"background-color: lightgrey;\"/>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>21</td>"+
				"	<td colspan=5 class='tdwithborders'>Functional test</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(110) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>22</td>"+
				"	<td class='tdwithborders' colspan=5>System test</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(120) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>23</td>"+
				"	<td class='tdwithborders' colspan=5>Regression test</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(130) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>30</td>"+
				"	<td class='tdwithborders' colspan=5>Meeting</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(140) + "</td>"+	
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>41</td>"+
				"	<td class='tdwithborders' colspan=5>Lecture</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(150) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>42</td>"+
				"	<td class='tdwithborders' colspan=5>Exercise</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(160) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>43</td>"+
				"	<td class='tdwithborders' colspan=5>Computer exercise</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(170) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>44</td>"+
				"	<td class='tdwithborders' colspan=5>Home reading</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(180) + "</td>"+		
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders'>100</td>"+
				"	<td class='tdwithborders' colspan=5>Other</td>"+
				"	<td class='tdwithborders'>" + tr.getActivityTime(190) + "</td>"+		
				"	</tr>"+
				"	<!--<tr><td colspan=7 class=\"markup\" NOWRAP><b><font size=+1>Part C: Time spent at different types of sub activities</font></b><br>(The values are summed up automatically)</td></tr>"+
				"	<tr><th colspan=2>Activity type</th><th>Activity code</th><th colspan=3>Description</th><th>Sum</th></tr>"+
				"	<tr>"+
				"	<td class='tdwithborders' colspan=2>Development and<br>documentation</td>"+
				"	<td class='tdwithborders'>D</td>"+
				"	<td class='tdwithborders' colspan=3>Developing new code, test cases<br>and documentation including<br>documentation of the system</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders' colspan=2>Informal review</td>"+
				"	<td class='tdwithborders'>I</td>"+
				"	<td class='tdwithborders' colspan=3>Time spent preparing and at<br>meeting for informal reviews</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders' colspan=2>Formal reviews</td>"+
				"	<td class='tdwithborders'>F</td>"+
				"	<td class='tdwithborders' colspan=3>Time spent preparing and at<br>meeting for formal reviews</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities'></td>"+
				"	</tr>"+
				"	<tr>"+
				"	<td class='tdwithborders' colspan=2>Rework,<br>improvement or<br>correction</td>"+
				"	<td class='tdwithborders'>R</td>"+
				"	<td class='tdwithborders' colspan=3>Time spent improving, revising<br>or correction documents and design<br>objects</td>"+
				"	<td class='tdwithborders'><input type =\"text\" name='activities'></td>"+
				"	</tr>-->"+
				"	<tr>"+
				"	<td class='tdwithborders markup' colspan=6><font size=+1><b>Signed</b></font></td><td>-</td></tr>"+
				"	</table>"+
				"   </form>";

		return html;
	}
	/**
	 * Creates String with HTML code to generate the "Time reporting" menu.
	 * @param username The current user
	 * @param role The current user's role
	 * @return String with HTML code to generate the "Time reporting" menu.
	 * 	  
	 */
	private String generateTimeReportMenu(String username, String role) {
		String html = 
				  "<a href='TimeReporting?function=createTimeReportForm'>Create new time report</a><br>"
				+ "<a href='TimeReporting?function=updateTimeReportListForm'>Update time report</a><br>"
				+ "<a href='TimeReporting?function=viewTimeReportListForm'>View time reports</a><br>"
				+ "<a href='TimeReporting?function=viewSummaryListForm'>View summaries</a><br>";
	
		if (username.equals("admin") || (role != null && role.equals("PG"))) {
			html += "<a href='ProjectLeader?function=signReports'>Sign reports</a><br>";
			html += "<a href='ProjectLeader?function=unsignReports'>Unsign reports</a><br>";
			html += "<a href='ProjectLeader?function=viewAllReports'>View all time reports</a>";
		}
	
		return html;
	}
	/**
	 * Checks if there's no duplicate week of the selected week in the database
	 * @param session
	 * @param week The selected week
	 * @return true if there's no duplicate week of the selected week in the database
	 * 
	 */
	private boolean isDuplicateEntry(HttpSession session, String week) {
		User user = db.getUser(getStringFromSession(session, "username"));
		List<TimeReport> reports = db.getTimeReportUser(user.getUsername());
	
		for (int i = 0; i < reports.size(); i++){
			if (reports.get(i).getWeek() == Integer.parseInt(week)){
				return true;
			}
		}
		return false;
	
	}
	
	/**
	 * Converts a String to boolean
	 * @param bool
	 * @return String "Yes" if bool equals "TRUE" else returns "No"
	 * 
	 */
	private String convertSigned(String bool) {
		if (bool.equals("TRUE")) return "Yes";
		return "No";
	}
	
	/**
	 * Converts a boolean to String
	 * @param bool
	 * @return String "Yes" if bool equals "TRUE" else returns "No"
	 * 
	 */
	private String convertSigned(boolean bool) {
		if (bool) return "Yes";
		return "No";
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}