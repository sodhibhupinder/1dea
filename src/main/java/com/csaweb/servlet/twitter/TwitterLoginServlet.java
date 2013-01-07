package com.csaweb.servlet.twitter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;


public class TwitterLoginServlet extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
		String userId = request.getParameter("userId");
		response.setContentType("text/html");
		 
		PrintWriter out = response.getWriter();

	    Twitter twitter = new TwitterFactory().getInstance();
	    twitter.setOAuthConsumer("BR8JwXg62cxsFZq1MIYA", "Ox5CSVL8lNJ5EcSC5IDzrY3SQDgyK4Gt5Id3y4mzRXA");
	       
		// Check the user already Authorized...
		AccessToken accessToken = isUserAuthorized(userId);
		
		if ( accessToken != null ) {
			try{

				twitter.setOAuthAccessToken(accessToken);
				
				out.println("Screen Name : " + twitter.getScreenName());
				out.println("Screen Name : " + twitter.getScreenName());
	        } catch (TwitterException e) {
	            throw new ServletException(e);
	        }	
		} else {
			// First Time user redirect to Twitter Login Page
		
	       
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
	
	private AccessToken isUserAuthorized(String userId) {
		AccessToken accessToken = null;
		Connection conn = null;
			Statement st = null;
			ResultSet rs = null;
			StringBuffer sb = new StringBuffer();
			try {
				InitialContext ctx = new InitialContext();
				DataSource ds = (DataSource) ctx.lookup("java:jboss/datasources/MysqlDS");

				String query = "select TWITTER_ID, TOKEN, TOKEN_SECRET from csaweb.TWITTER_TOKEN_INFO " ;
		
				conn = ds.getConnection();
				st = conn.createStatement();
				rs = st.executeQuery( query );
				
				if ( rs != null ) {
					while (rs.next()) {
						String id = rs.getString("TWITTER_ID");
						if ( userId.equalsIgnoreCase(id)) {
							// Already Authorized
							accessToken = new AccessToken( rs.getString("TOKEN"), rs.getString("TOKEN_SECRET"));
							break;
						}
					}
				}

			} catch (Exception ex) {
				sb.append(ex.getMessage());
			} finally {

			}

			return accessToken;

	}

}
