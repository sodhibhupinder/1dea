package com.csaweb.servlet.twitter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import twitter4j.DirectMessage;
import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;


public class TwitterCallbackServlet extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
		
		response.setContentType("text/html");
		 
		PrintWriter out = response.getWriter();
	

		AccessToken accessToken = null;
		 Twitter twitter = (Twitter) request.getSession().getAttribute("twitter");
	        RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
	        String verifier = request.getParameter("oauth_verifier");
	        try {
	        	accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
	            // Save the AccessToken
	        	saveAccessToken(twitter.getScreenName(), accessToken.getToken(), accessToken.getTokenSecret(), out);
	        	request.getSession().removeAttribute("requestToken");
	        } catch (TwitterException e) {
	            throw new ServletException(e);
	        }
	        //response.sendRedirect(request.getContextPath() + "/");
	        try{
		        System.out.println("IN CALLBACK.......................................................");
		        System.out.println("SCREEN NAME : " + twitter.getScreenName() + "\n");
		        
		        out.println("SCREEN NAME : " + twitter.getScreenName()+ "\n");
		        
/*		        // Direct Messages
		        out.println("Direct Messages......................"+ "\n");
		        ResponseList<DirectMessage> directMessages = twitter.getDirectMessages();
		        if ( directMessages != null && !directMessages.isEmpty() ) {
		        	for(DirectMessage msg : directMessages ) {
		        		out.println(msg.getText()+ "\n");
		        	}
		        }
		        
		        out.println();
		        out.println("Favourites..........................."+ "\n");
			    ResponseList<Status> favStats = twitter.getFavorites();
			    if ( favStats!=null && !favStats.isEmpty() ) {
			    	for( Status sts : favStats ) {
			    		out.println( sts.getText() + "\n");
			    	}
			    }

			    out.println();
			    out.println("Friends Timeline........................"+ "\n");
			    ResponseList<Status> frnsTmeline = twitter.getFriendsTimeline();
			    if ( frnsTmeline!=null && !frnsTmeline.isEmpty() ) {
			    	for( Status sts : frnsTmeline ) {
			    		out.println( sts.getText() + "\n");
			    	}
			    }
		*/	    
			    
	        }catch(TwitterException te) {
	        	te.printStackTrace();
	        }


	        
	}

	private void saveAccessToken(String twitterId, String token, String tokenSecret, PrintWriter out) {

		Connection conn = null;
			Statement st = null;
			int rs ;
			StringBuffer sb = new StringBuffer();
			try {
				InitialContext ctx = new InitialContext();
				DataSource ds = (DataSource) ctx.lookup("java:jboss/datasources/MysqlDS");

				String query = "insert into csaweb.TWITTER_TOKEN_INFO (TWITTER_ID, TOKEN, TOKEN_INFO) values(" + twitterId + "," + token + "," + tokenSecret + ")" ;
				
				out.println( ds );
				out.println(query);
				// This works too
				// Context envCtx = (Context) ctx.lookup("java:comp/env");
				// DataSource ds = (DataSource) envCtx.lookup("jdbc/TestDB");
				
				out.println("Getting connection.................");
				conn = ds.getConnection();
				out.println("Got Conn :   " + conn);

				st = conn.createStatement();
				
				out.println("--- executing update.......");
				rs = st.executeUpdate( query );
				
				out.println("Executed successfully Result Rows :   " + rs);
				conn.commit();
	//
//				while (rs.next()) {
//					String id = rs.getString("id");
//					String firstName = rs.getString("user_first_name");
//					String lastName = rs.getString("user_last_name");
//					sb.append("ID: " + id + ", First Name: " + firstName
//							+ ", Last Name: " + lastName + "<br/>");
//				}
			} catch (Exception ex) {
				out.println("---------------------------" + ex.getMessage());
				sb.append(ex.getMessage());
			} finally {
//				try { if (rs != null) rs.close(); } catch (SQLException e) { sb.append(e.getMessage());; }
//				try { if (st != null) st.close(); } catch (SQLException e) { sb.append(e.getMessage());; }
//				try { if (conn != null) conn.close(); } catch (SQLException e) { sb.append(e.getMessage());; }
			}

		

	}
}
