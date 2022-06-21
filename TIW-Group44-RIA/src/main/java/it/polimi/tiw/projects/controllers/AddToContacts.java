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
import javax.servlet.http.HttpSession;

import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.AddressBookDAO;
import it.polimi.tiw.projects.dao.BankAccountDAO;
import it.polimi.tiw.projects.enums.ERRORS;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/AddToContacts")
@MultipartConfig
public class AddToContacts extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	public AddToContacts() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		
		Integer contactId = null, contactAccount = null;
		try {
			contactId = Integer.parseInt(request.getParameter("contactId"));
			contactAccount = Integer.parseInt(request.getParameter("contactAccount"));
		} catch (NumberFormatException | NullPointerException e) {
			// only for debugging e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(ERRORS.INCORRECT_PARAMS);
			return;
		}
		
		User user = (User) session.getAttribute("user");
		boolean notAuthorized;
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		try {
			notAuthorized = bankAccountDAO.checkAssociationAccountUser(contactId, contactAccount);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(ERRORS.SQL_ERROR_ACCOUNT);
			return;
		}
		
		if(notAuthorized) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(ERRORS.NOT_OWNER);
			return;
		}
		
		AddressBookDAO addressBookDAO = new AddressBookDAO(connection);
		try {
			addressBookDAO.createContact(user.getId(), contactAccount);
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(ERRORS.SQL_ERROR_CONTACTS);
			return;
		}catch(Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}