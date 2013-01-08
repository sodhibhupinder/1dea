package com.csaweb.servlet.twitter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;


public class TwitterLoginServlet extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
		String userId = request.getParameter("userId");
		System.out.println(userId);
		
		response.setContentType("text/html");
		 
		PrintWriter out = response.getWriter();

		out.println(userId);
		
	       Twitter twitter = new TwitterFactory().getInstance();
	       twitter.setOAuthConsumer("BR8JwXg62cxsFZq1MIYA", "Ox5CSVL8lNJ5EcSC5IDzrY3SQDgyK4Gt5Id3y4mzRXA");
	       
	        request.getSession().setAttribute("twitter", twitter);
	        try {
	            StringBuffer callbackURL = request.getRequestURL();
	            int index = callbackURL.lastIndexOf("/");
	            callbackURL.replace(index, callbackURL.length(), "").append("/twitterCallback");

	            RequestToken requestToken = twitter.getOAuthRequestToken(callbackURL.toString());
	            request.getSession().setAttribute("requestToken", requestToken);
	            response.sendRedirect(requestToken.getAuthenticationURL());
	        } catch (TwitterException e) {
	            throw new ServletException(e);
	        }
	}

}
