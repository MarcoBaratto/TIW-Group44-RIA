package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.beans.Transfer;
import it.polimi.tiw.projects.dao.TransferDAO;
import it.polimi.tiw.projects.enums.ERRORS;
import it.polimi.tiw.projects.dao.BankAccountDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/GetTransfersData")
@MultipartConfig
public class GetTransfersData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetTransfersData() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// If the user is not logged in (not present in session) redirect to the login done by the filter
		
		HttpSession session = request.getSession();
		
		// get and check bankAccountId
		Integer bankAccountId = null;
		try {
			bankAccountId = Integer.parseInt(request.getParameter("bankAccountid"));
		} catch (NumberFormatException | NullPointerException e) {
			// only for debugging e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(ERRORS.INCORRECT_PARAMS);
			return;
		}
				
		User user = (User) session.getAttribute("user");
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		TransferDAO transferDAO = new TransferDAO(connection);

		boolean notAuthorized = true;
		
		//check if the user has the authorization to see the transfers of that bankAccount
		try {
			notAuthorized = bankAccountDAO.checkAssociationAccountUser(user.getId(), bankAccountId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(ERRORS.SQL_ERROR);
			return;
		}
		
		//if the user doesn't have the authorization an error is sent
		if(notAuthorized) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(ERRORS.NOT_OWNER);
			return;
		}
			
		
		List<Transfer> transfers = new ArrayList<>();
		//Get transfers involving this bankAccount
		try {
			transfers = transferDAO.findTransferById(bankAccountId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(ERRORS.SQL_ERROR);
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().create();
		String transfersJson = gson.toJson(transfers);
		response.getWriter().println(transfersJson);
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
