package com.csaweb.servlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class CallBackServlet
 */
@WebServlet("/CallBackFB")
public class CallBackServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       Logger logger = Logger.getLogger(CallBackServlet.class.getName());
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CallBackServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String hubMode= (String) request.getAttribute("hub.mode");
		String hubChallenge= (String)request.getAttribute("hub.challenge");
		String hubVerifyToken= (String)request.getAttribute("hub.verify_token");
		
		response.getWriter().print(request.getParameter("hub.challenge"));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Gson gson = new Gson();
		String jObj = gson.fromJson(request.getParameter("user"),String.class); // this parses the json
		System.out.println("jObj"+jObj);
		System.out.println("request.getParameter(id)"+request.getParameter("id"));
		logger.log(Level.INFO, "request.getParameter(id)"+request.getParameter("id"));
		logger.log(Level.INFO, "jObj"+jObj);
			
	}

}
