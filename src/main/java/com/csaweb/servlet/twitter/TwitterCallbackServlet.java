package com.csaweb.servlet.twitter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.DirectMessage;
import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.RequestToken;


public class TwitterCallbackServlet extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
		
		response.setContentType("text/html");
		 
		PrintWriter out = response.getWriter();
	

		 Twitter twitter = (Twitter) request.getSession().getAttribute("twitter");
	        RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
	        String verifier = request.getParameter("oauth_verifier");
	        try {
	            twitter.getOAuthAccessToken(requestToken, verifier);
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

}
