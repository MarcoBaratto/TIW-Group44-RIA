package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.gson.Gson;

import it.polimi.tiw.projects.beans.BankAccount;
import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.BankAccountDAO;
import it.polimi.tiw.projects.dao.TransferDAO;
import it.polimi.tiw.projects.thinBeans.ThinTransfer;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/CreateTransfer")
@MultipartConfig
public class CreateTransfer extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection = null;


	public CreateTransfer() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// If the user is not logged in (not present in session) redirect to the login done by the filter
		HttpSession session = request.getSession();

		BigDecimal amount = null;
		Integer bankAccountidDestination = null;
		Integer userDestination = null;
		Integer bankAccountidOrigin = null;
		String comments = null;
		boolean isBadRequest = false;

		try {
			amount = new BigDecimal(request.getParameter("amount"));
			amount = amount.setScale(2,RoundingMode.HALF_EVEN);
			bankAccountidDestination = Integer.parseInt(request.getParameter("idDestination"));
			userDestination = Integer.parseInt(request.getParameter("userDestination"));
			comments = StringEscapeUtils.escapeJava(request.getParameter("comments"));
			bankAccountidOrigin = Integer.parseInt(request.getParameter("bankAccountidOrigin"));
		}catch(NumberFormatException | NullPointerException e){
			isBadRequest = true;
		}

		if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || comments  == null || comments.isEmpty()) {
			isBadRequest = true;
		}

		if (isBadRequest) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}

		String errorMsg = "";
		// Get users and Bank Accounts
		User user = (User) session.getAttribute("user");
		BankAccount accountOrigin = null;
		BankAccount accountDest = null;
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		boolean notAuthorizedOrigin = true;
		boolean notAuthorizedDest = true;

		try {
			notAuthorizedOrigin = bankAccountDAO.checkAssociationAccountUser(user.getId(), bankAccountidOrigin);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Not possible to verify Origin Acccount ownership");
			return;
		}

		if(notAuthorizedOrigin) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You are not the account's owner");
			return;
		}

		if(bankAccountidOrigin == bankAccountidDestination) {
			errorMsg += "Origin and Destination are the same BankAccount ";
		}

		try {
			accountOrigin = bankAccountDAO.detailsAccount(bankAccountidOrigin);
			accountDest = bankAccountDAO.detailsAccount(bankAccountidDestination);
		}catch(SQLException e){
			errorMsg = "Error finding accounts ";
		}

		try {
			notAuthorizedDest = bankAccountDAO.checkAssociationAccountUser(userDestination, bankAccountidDestination);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("ERROR: cannot read data");
			return;
		}

		if(notAuthorizedDest) {
			errorMsg += "The recipient isn't the owner of the account ";
		}

		TransferDAO transferDAO = new TransferDAO(connection);
		BigDecimal balancesBefore[] = new BigDecimal[2], balancesAfter[] = new BigDecimal[2];
		balancesBefore[0] = accountOrigin.getBalance();
		balancesBefore[1] = accountDest.getBalance();
		
		if(!errorMsg.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(errorMsg);
			return;
		}
		
		try {
			try {
				connection.setAutoCommit(false);
				try {
					balancesAfter = bankAccountDAO.transfer(amount, bankAccountidDestination, bankAccountidOrigin);
				}catch(SQLException e){
					if(e.getMessage().equals("Insufficent funds ")) {
						if(errorMsg.isEmpty()) {
							errorMsg = e.getMessage();
						} else {
							errorMsg += "and " + e.getMessage();
						}
					}
					throw e;
				}
				if(errorMsg.isEmpty()) {
					try {
						transferDAO.createTransfer(bankAccountidOrigin, bankAccountidDestination, amount, comments);
					}catch(SQLException e) {
						response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						response.getWriter().println("There was a problem during the transfer's creation");
						return;
					}
				}
				connection.commit();
			} catch (SQLException e) {
				connection.rollback();
			} finally {
				connection.setAutoCommit(true);
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("There was a problem accessing data");
			return;
		}
		
		ThinTransfer t = new ThinTransfer( balancesBefore, balancesAfter, amount, accountOrigin.getId(), bankAccountidDestination, accountDest.getIdUser(), comments);
		String transferJson = new Gson().toJson(t);
		response.getWriter().println(transferJson);
	}


	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
