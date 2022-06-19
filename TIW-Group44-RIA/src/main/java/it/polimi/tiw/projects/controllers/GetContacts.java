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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.projects.beans.AddressBook;
import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.AddressBookDAO;
import it.polimi.tiw.projects.enums.ERRORS;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/GetContacts")
@MultipartConfig
public class GetContacts extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	public GetContacts() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		
		User user = (User) session.getAttribute("user");
		
		AddressBookDAO addressBookDAO = new AddressBookDAO(connection);
		
		AddressBook addressBook = null;
		
		try {
			addressBook = addressBookDAO.getAddressBook(user.getId());
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(ERRORS.SQL_ERROR);
			return;
		}
		
		if(addressBook==null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("No contacts");
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().create();
		String contactsJson = gson.toJson(addressBook.getContacts());
		response.getWriter().println(contactsJson);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		doGet(request,response);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
}
		