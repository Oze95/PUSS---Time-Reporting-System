package servlets;

import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import core.ServletBase;

/**
 * Servlet implementation class LogIn
 * 
 * A log-in page. 
 * 
 * The first thing that happens is that the user is logged out if he/she is logged in. 
 * Then the user is asked for name and password. 
 * If the user is logged in he/she is directed to the functionality page. 
 * 
 * 
 * @version 1.0
 * 
 */
@WebServlet("/LogIn")
public class LogIn extends ServletBase {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogIn() {
        super();
    }
    
    /**
     * Generates a form for login. 
     * @return HTML code for the form
     */
    protected String loginRequestForm() {
    	String html = 	"<h1>Login</h1> " + 
    					"<p>Please enter your user credentials to log in</p>" +
		    			"<p><form name='input' method='post'" +
		    			"<p>Username:<br> <input type='text' name='username' placeholder='Username'><br>" +
		    			"<p>Password:<br> <input type='password' name='password' placeholder='Password'><br>" +
		    			"<p><input type='submit' value='Submit'>";
    	return html;
    }
    
    /**
     * Checks with the database if the user should be accepted
     * @param username The name of the user
     * @param password The password of the user
     * @return true if the user should be accepted
     */
    private boolean checkUser(String username, String password) {
		return db.checkUserCredentials(username, password);
	}
    
	/**
	 * Implementation of all input to the servlet. All post-messages are forwarded to this method. 
	 * 
	 * First logout the user, then check if he/she has provided a username and a password. 
	 * If he/she has, it is checked with the database and if it matches then the session state is 
	 * changed to login, the username that is saved in the session is updated, and the user is 
	 * relocated to the functionality page. 
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Get the session
		HttpSession session = request.getSession(true);

		PrintWriter out = response.getWriter();
		out.println("<html>\n<head>\n<title> Timereporting </title>\n</head>\n<body>\n" +
					"<table style='border-spacing:50px 10px;'>\n<tr>\n<td valign='top'>\n");
		
		if (loggedIn(request)) {
			session.setAttribute("loginstate", LOGIN_FALSE);
			session.setAttribute("username", null);
			responseList.add("You are now logged out");
		}
		
		String username;
		String password;
				
        username = request.getParameter("username"); // get the entered username
        password = request.getParameter("password"); // get the entered password
        if (username != null && password != null) {
        	if (checkUser(username, password)) {
       			session.setAttribute("loginstate", LOGIN_TRUE);  // save the state in the session
       			session.setAttribute("username", username);  // save the name in the session
       			session.setMaxInactiveInterval(1200); // 20 * 60 = 1200
       			response.sendRedirect("Profile");
       		}
       		else {
       			out.println(loginRequestForm());
       			responseList.add("That was not a valid username / password.");
       		}
       	}else{ // name was null, probably because no form has been filled out yet. Display form.
       		out.println(loginRequestForm());
       	}
        
		out.println(getPageOutro()); //Append html-trailer
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
