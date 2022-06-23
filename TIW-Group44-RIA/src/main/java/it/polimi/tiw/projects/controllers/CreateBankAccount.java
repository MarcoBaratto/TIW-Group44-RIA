package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.BankAccountDAO;
import it.polimi.tiw.projects.enums.ERRORS;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/CreateBankAccount")
@MultipartConfig
public class CreateBankAccount extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection = null;

	public CreateBankAccount() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// If the user is not logged in (not present in session) redirect to the login done by the filter
		HttpSession session = request.getSession();
		
		BigDecimal balance = null;
		boolean isBadRequest = false;
		
		try {
			balance = new BigDecimal(request.getParameter("balance"));
			if(!patternMatches(balance.toString()))
				throw new NumberFormatException();
		}catch(NumberFormatException | NullPointerException e){
			isBadRequest = true;
		}
		if (isBadRequest) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(ERRORS.INCORRECT_PARAMS);
			return;
		}
		

		// Create bank account in DB
		User user = (User) session.getAttribute("user");
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		try {
			bankAccountDAO.createBankAccount(user.getId(), balance);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(ERRORS.SQL_ERROR_ACCOUNT);
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("Account created!");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}
	
	public static boolean patternMatches(String balance) {
		//^[0-9]*\.[0-9]{2}$ or ^[0-9]*\.[0-9][0-9]$
	    return Pattern.compile("^[0-9]*(\\.[0-9]{0,2})?$")
	      .matcher(balance)
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
