package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.projects.dao.UserDAO;
import it.polimi.tiw.projects.enums.ERRORS;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/CheckRegistration")
@MultipartConfig
public class CheckRegistration extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection = null;

	public CheckRegistration() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	
		String username = null, name = null, surname = null, mail = null, psw = null, repeatPsw = null;
		
		//obtaining parameters from the request
		username = StringEscapeUtils.escapeJava(request.getParameter("username"));
		name = StringEscapeUtils.escapeJava(request.getParameter("name"));
		surname = StringEscapeUtils.escapeJava(request.getParameter("surname"));
		mail = StringEscapeUtils.escapeJava(request.getParameter("mail"));
		psw = StringEscapeUtils.escapeJava(request.getParameter("pwd"));
		repeatPsw = StringEscapeUtils.escapeJava(request.getParameter("repeatPwd"));
			
		String errorMsg = "";
			
		//Checking the parameters
		if(username == null || username.isEmpty() || name == null || name.isEmpty() || psw == null || psw.isEmpty()
				|| repeatPsw == null || repeatPsw.isEmpty()|| mail == null || mail.isEmpty() 
				|| surname == null || surname.isEmpty()) {
			errorMsg = errorMsg + " " + ERRORS.INCORRECT_PARAMS;
		}
			
		if(!psw.equals(repeatPsw))
			errorMsg = errorMsg + " " + ERRORS.PSW_NO_MATCH;
		
		//parse mail
		if(!patternMatches(mail))
			errorMsg = errorMsg + " " + ERRORS.MAIL_ERROR;
		
		UserDAO userDAO = new UserDAO(connection);
		
		//Checking the uniqueness of the nickname
		try {
		if(!userDAO.checkUniqueNickName(username))
			errorMsg = errorMsg + " " + ERRORS.USERNAME_CHOSEN;
		}catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println( ERRORS.SQL_ERROR_USER);
			return;
		}
		
		//If something is wrong, an error message is shown
		if(errorMsg!="") {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(errorMsg);
			return;
		}
		
		//Creating the user
		try{
			userDAO.createUser(username, psw, name, surname, mail);
		}catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(ERRORS.SQL_ERROR_USER);
			return;	
		}
		
		//return the user to the right view
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("User created, you can Login now");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}
	
	public static boolean patternMatches(String emailAddress) {
	    return Pattern.compile("^(.+)@(\\S+)$")
	      .matcher(emailAddress)
	      .matches();
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
