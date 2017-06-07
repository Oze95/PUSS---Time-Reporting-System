package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import core.ServletBase;

/**
 * Servlet implementation class Profile
 */
@WebServlet("/Profile")
public class Profile extends ServletBase {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see ServletBase#ServletBase()
     */
    public Profile() {
        super();
    }
    
    private String changePasswordForm() {
		String html = "<form name='input' method='get'>\n" +
				"<input type='hidden' name='function' value='changePassword'>" +
				"<p> Enter new password: <br> <input type='password' name='password1'>"+
				"<p> Confirm new password: <br> <input type='password' name='password2'>"+
				 "<br><br><input type='submit' value='Change password'>" +
				 "&nbsp<input type='submit' value='Cancel' name='cancelpw'>\n" + 
				"</form>\n";
				
			
    	return html;
	}
    
    private boolean changePassword(String username, String password) {
    	
    	if((password.length() >= 8) && (password.length() <= 32))
    		if(password.matches("[\u0021-\u007E]+")) // Regex Pattern ASCII 33-126
    			return db.changePassword(username, password);
    	
    	return false;
		
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Get the session
		HttpSession session = request.getSession(true);
		PrintWriter out = response.getWriter();
		
		if (!loggedIn(request))
			response.sendRedirect("LogIn");
		
		String username = getStringFromSession(session, "username");
		
		out.println(getPageIntro(request)); //Append html-header
		out.println("<h1>Profile</h1>\n");
		
		String function = request.getParameter("function"); //Determine course of action
		if(function != null) {
			//Get additional data
			String password = request.getParameter("password1");
			String passwordConfirm = request.getParameter("password2");
			
			//if cancel button is pressed, send user back to profile page.
			if(request.getParameter("cancelpw") != null){
				response.sendRedirect("Profile");
			}
			
			switch(function) {
				case "changePassword":
					
					if(password != null) {
					if(username != null) {
						
						//if not same password in both fields
						if(!password.equals(passwordConfirm)){
							responseList.add("Error: Passwords do not match");
							
						}else{
							
							if(changePassword(username, password))
								responseList.add("Password was changed!");
							else
								responseList.add("Error: Provided password was not valid!");
							
						}
						
						
					} else
						responseList.add("Error: Suggested username is not allowed.");
					} 
						out.println(changePasswordForm());
					break;
				default:
					responseList.add("Invalid function parameter!");
					break;
			}
		} else { //Landed on Profile page
			out.println("<p>Welcome, "+username+"</p>");
		}
		
		out.println(getPageOutro()); //Append html-trailer
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}