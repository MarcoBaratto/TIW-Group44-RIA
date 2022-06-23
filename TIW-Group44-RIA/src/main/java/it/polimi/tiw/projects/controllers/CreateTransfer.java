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
import it.polimi.tiw.projects.enums.ERRORS;
import it.polimi.tiw.projects.exceptions.NotEnoughFundsException;
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
			response.getWriter().println(ERRORS.INCORRECT_PARAMS);
			return;
		}

		// Get users and Bank Accounts
		User user = (User) session.getAttribute("user");
		BankAccount accountOrigin = null;
		BankAccount accountDest = null;
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);

		try {
			accountOrigin = bankAccountDAO.detailsAccount(bankAccountidOrigin);
			accountDest = bankAccountDAO.detailsAccount(bankAccountidDestination);
		}catch(SQLException e){
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(ERRORS.SQL_ERROR_ACCOUNT);
		}
		
		if(accountOrigin==null ||accountOrigin.getIdUser()!=user.getId()
			|| accountDest==null || accountDest.getIdUser()!=userDestination){
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println(ERRORS.NOT_OWNER);
				return;
			}

		if(bankAccountidOrigin == bankAccountidDestination) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(ERRORS.SAME_ACCOUNT);
			return;
		}

		try {
			accountOrigin = bankAccountDAO.detailsAccount(bankAccountidOrigin);
			accountDest = bankAccountDAO.detailsAccount(bankAccountidDestination);
		}catch(SQLException e){
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(ERRORS.SQL_ERROR_ACCOUNT);
		}

		

		TransferDAO transferDAO = new TransferDAO(connection);
		BigDecimal balancesBefore[] = new BigDecimal[2], balancesAfter[] = new BigDecimal[2];
		balancesBefore[0] = accountOrigin.getBalance();
		balancesBefore[1] = accountDest.getBalance();
		
		
		try {
			try {
				//start transaction
				connection.setAutoCommit(false);
				//removes funds from one Account and adds them to the other
				bankAccountDAO.transfer(amount, bankAccountidDestination, bankAccountidOrigin);
				//creates an entry in the Transfer table
				transferDAO.createTransfer(bankAccountidOrigin, bankAccountidDestination, amount, comments);
				//if no Exceptions are thrown the transaction can be committed
				connection.commit();

			}catch(SQLException e) {
				//generic SQL error, rollback and throw exception to show the error
				connection.rollback();
				throw e;
			}catch(NotEnoughFundsException e) {
				//not enough funds, send error and rollback transaction
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println(ERRORS.NO_FUNDS);
				connection.rollback();
				return;
			}finally {
				//finally will always execute 
				connection.setAutoCommit(true);
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(ERRORS.SQL_ERROR_TRANSFER);
			return;
		}
		
		balancesAfter[0] = balancesBefore[0].subtract(amount);
		balancesAfter[1] = balancesBefore[1].add(amount);
		
		ThinTransfer t = new ThinTransfer( balancesBefore, balancesAfter, amount, accountOrigin.getId(), bankAccountidDestination, accountDest.getIdUser(), comments);
		String transferJson = new Gson().toJson(t);
		response.getWriter().println(transferJson);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
