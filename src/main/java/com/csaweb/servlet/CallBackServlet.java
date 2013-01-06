package com.csaweb.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;

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
		StringBuffer jb = new StringBuffer();
		  String line = null;
		  BufferedReader reader=null;
		  try {
		    reader = request.getReader();
		    line = reader.readLine();
		    while (line != null)   {
		         // Print the content on the console
		    	jb.append(line);
		         line = reader.readLine();
		    }
		   
		      
		  } catch (Exception e) { /*report an error*/ }

		//String jObj = gson.fromJson(jb.toString(),String.class); // this parses the jso
		
		System.out.println("jb"+jb.toString());
		//System.out.println("jObj"+jObj);
		System.out.println("request.getParameter(id)"+request.getParameter("id"));
		logger.log(Level.INFO, "request.getParameter(id)"+request.getParameter("id"));
		logger.log(Level.INFO, "jb"+jb.toString());
		//logger.log(Level.INFO, "jObj"+jObj);
			
	}

}
