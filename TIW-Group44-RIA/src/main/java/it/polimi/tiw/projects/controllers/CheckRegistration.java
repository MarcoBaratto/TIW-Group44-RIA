package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.projects.dao.UserDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/CheckRegistration")
public class CheckRegistration extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection = null;

	private TemplateEngine templateEngine;

	public CheckRegistration() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	
		String username = null, name = null, surname = null, mail = null, psw = null, repeatPsw = null;
		
			username = StringEscapeUtils.escapeJava(request.getParameter("username"));
			name = StringEscapeUtils.escapeJava(request.getParameter("name"));
			surname = StringEscapeUtils.escapeJava(request.getParameter("surname"));
			mail = StringEscapeUtils.escapeJava(request.getParameter("mail"));
			psw = StringEscapeUtils.escapeJava(request.getParameter("pwd"));
			repeatPsw = StringEscapeUtils.escapeJava(request.getParameter("repeatPwd"));
			
			String errorMsg = "";
			
			if(username == null || username.isEmpty() || name == null || name.isEmpty() || psw == null || psw.isEmpty()
					|| repeatPsw == null || repeatPsw.isEmpty()|| mail == null || mail.isEmpty() 
					|| surname == null || surname.isEmpty()) {
				errorMsg = errorMsg + " Missing param";
			}
			
			if(!psw.equals(repeatPsw))
				errorMsg = errorMsg + " Passwords don't match";
			
			//parse mail
			if(!patternMatches(mail))
				errorMsg = errorMsg + " Incorrect mail format";
			
			UserDAO userDAO = new UserDAO(connection);
			
			try {
			if(!userDAO.checkUniqueNickName(username))
				errorMsg = errorMsg + " This nickname already exists";
			}catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to check Username's uniqueness");
				return;
			}
			
			if(errorMsg!="") {
				String path;
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("errorMsgRegistration", errorMsg);
				path = "/LoginRegistration.html";
				templateEngine.process(path, ctx, response.getWriter());
				return;
			}
			
			try{
				userDAO.createUser(username, psw, name, surname, mail);
			}catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create User");
				return;
			
		}
			
		// return the user to the right view
		String ctxpath = getServletContext().getContextPath();
		String path = ctxpath + "/LoginRegistration.html";
		response.sendRedirect(path);
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
