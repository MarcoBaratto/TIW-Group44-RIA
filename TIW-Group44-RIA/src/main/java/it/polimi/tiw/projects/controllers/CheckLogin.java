package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.projects.thinBeans.ThinUser;
import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.UserDAO;
import it.polimi.tiw.projects.enums.ERRORS;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/CheckLogin")
@MultipartConfig
public class CheckLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	public CheckLogin() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// obtain and escape params
		String usrn = null;
		String pwd = null;
		try {
			usrn = StringEscapeUtils.escapeJava(request.getParameter("username"));
			pwd = StringEscapeUtils.escapeJava(request.getParameter("pwd"));
			if (usrn == null || pwd == null || usrn.isEmpty() || pwd.isEmpty()) {
				throw new Exception(ERRORS.INCORRECT_PARAMS.toString());
			}
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		}

		// query db to authenticate for user
		UserDAO userDao = new UserDAO(connection);
		User user = null;
		try {
			user = userDao.checkCredentials(usrn, pwd);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(ERRORS.SQL_ERROR);
			return;
		}

		// If the user exists, add info to the session and go to home page, otherwise
		// show login page with error message

		if (user == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println(ERRORS.LOGIN_ERROR);
			return;
		} else {
			request.getSession().setAttribute("user", user);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			Gson gson = new GsonBuilder().create();
			String userJson = gson.toJson(new ThinUser(user.getUsername(), user.getId()));
			response.getWriter().println(userJson);
		}
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}