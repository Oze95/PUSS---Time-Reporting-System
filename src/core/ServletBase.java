package core;

import java.util.ArrayList;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 *  This class is the superclass for all servlets in the application. 
 *  It includes basic functionality required by many servlets, like for example a page head 
 *  written by all servlets, and the connection to the database. 
 *  
 *  This application requires a database.
 *  For username and password, see the constructor in this class.
 *  
 * 
 *  @version 1.0
 *  
 */
public class ServletBase extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	// Define states
	protected static final int LOGIN_FALSE = 0;
	protected static final int LOGIN_TRUE = 1;
	protected static ArrayList<String> responseList;
	protected Database db;
	
	/**
	 * Constructs a servlet and makes a connection to the database.
	 */
    public ServletBase() {
		db = Database.getInstance();
		responseList = new ArrayList<String>();
    }
    
    /**
     * Checks if a user is logged in or not.
     * @param request The HTTP Servlet request (so that the session can be found)
     * @return true if the user is logged in, otherwise false.
     */
    protected boolean loggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Object objectState = session.getAttribute("loginstate");
        String username = getStringFromSession(request.getSession(), "username"); // <--
        boolean active = true;
       
        if (username != null && !username.isEmpty()) { // <--
            active = db.isUserActive(username);
            if (!active) {
                responseList.add("Your account is not active.");
                return false;
            }
        }
           
        int state = LOGIN_FALSE;
        if (objectState != null)
            state = (Integer) objectState;
        return (state == LOGIN_TRUE);
   
    }
    
    /**
     * Used by servlets to get attribute values from the session
     * @param current session, name of the attribute
     * @return Returns the string stored in a variable if it has a value, otherwise null.
     */
    protected String getStringFromSession(HttpSession session, String attrib) {
    	Object nameObj = session.getAttribute(attrib);
		if (nameObj != null)
			return (String) nameObj;
    	return null;
    }
    
    /**
     * Constructs the header of all servlets. 
     * @param request TODO
     * @return String with html code for the header. 
     */
    protected String getPageIntro(HttpServletRequest request) {
    	String username = getStringFromSession(request.getSession(), "username");
    	String groupname = "";
    	String role = "";
    	boolean isAdmin = false;
    	String rolename;
    	
    	if (username != null) {
			User user = db.getUser(username);
			groupname = user.getGroup();
			role = user.getRole();
			
			if (username.equals("admin"))
				isAdmin = true; // Admin has a different sidebar
    	}
    	switch(role) {
	    	case "PG":
	    		rolename = "Project Leader";
	    		break;
	    	case "UG":
	    		rolename = "Developer";
	    		break;
	    	case "SG":
	    		rolename = "System Manager";
	    		break;
	    	case "TG":
	    		rolename = "Tester";
	    		break;
	    	default:
	    		rolename = null;
    	}
    	
    	String intro = "<html><script type='text/javascript' src='scripts.js'></script>"
    			+ "<link rel='stylesheet' type='text/css' href='styles.css'> "
    			+ "\n<head> <title>Timereporting</title>\n</head>\n<body>\n"
    			+ "<table style='border-spacing:50px 10px;'>\n"
    			+ "<tr>\n"
    			+ "<td valign='top'>\n" //Sidebar
    			+ "<b>User: </b>" + username + "<br>\n"
    			+ "<b>Project Group: </b>" + (groupname == null ? "-" : groupname) + "<br>\n"
    			+ "<b>Role: </b>" + (rolename == null ? "-" : rolename) + "<br><br><br>\n"
    			+ "<a href='MemberList'>Member list</a><br>\n";
		intro += "<a href='TimeReporting'>Time reporting</a><br>\n";
		intro += "<a href='Profile?function=changePassword'>Change password</a><br>\n";
		if (isAdmin)
			intro += "<a href='AdministrationTools'>Administration tools</a><br>\n";
		intro += "<a href='LogIn'>Log out</a><br>\n"
				+ "</td>\n" //End of Sidebar
				+ "<td valign='top'>\n"; //Content
    	return intro;
    }

    /**
     * Constructs the trailer of all servlets. 
     * @return String with html code for the trailer. 
     */
    protected String getPageOutro() {
    	String outro = "";
    	if (!responseList.isEmpty()) {
    		for(String s : responseList)
    			outro += "<script>window.alert(\""+s+"\")</script>\n";
    		responseList = new ArrayList<String>();
    	}
    	outro += "</td>\n</tr>\n</table>\n</body>\n</html>"; //End of Content and (Response)
    	return outro;
    }
}
