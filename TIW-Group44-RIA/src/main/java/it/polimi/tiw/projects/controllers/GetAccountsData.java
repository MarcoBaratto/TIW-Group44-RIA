package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.projects.beans.BankAccount;
import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.BankAccountDAO;
import it.polimi.tiw.projects.enums.ERRORS;
import it.polimi.tiw.projects.thinBeans.ThinBankAccount;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/GetAccountsData")
@MultipartConfig
public class GetAccountsData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetAccountsData() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// If the user is not logged in (not present in session) redirect to the login is done by the filter
		
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		List<BankAccount> bankAccounts = new ArrayList<>();

		try {
			bankAccounts = bankAccountDAO.findAccountsByUser(user.getId());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(ERRORS.SQL_ERROR_ACCOUNT);
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().create();
		ArrayList<ThinBankAccount> thinBankAccounts = new ArrayList<>();
		for (BankAccount bankAccount : bankAccounts) {
			thinBankAccounts.add(new ThinBankAccount(bankAccount.getId(), bankAccount.getBalance()));
		}

		String bankAccountsJson = gson.toJson(bankAccounts.stream().map(b->new ThinBankAccount(b.getId(),b.getBalance())).collect(Collectors.toList()));
		response.getWriter().println(bankAccountsJson);
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
