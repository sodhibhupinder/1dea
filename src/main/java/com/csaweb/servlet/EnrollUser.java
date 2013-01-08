package com.csaweb.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.LoggingMXBean;

import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.csaweb.common.EDatabase;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.User;
import com.restfb.types.Post.Likes;

/**
 * Servlet implementation class MyServlet
 */
@WebServlet(name = "EnrollUser", urlPatterns = { "/EnrollUser" })
public class EnrollUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	final LoggingMXBean mxBean = LogManager.getLoggingMXBean();
	final Logger logger = Logger.getLogger(EnrollUser.class.getName());

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public EnrollUser() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		doPost(request, response);

		// response.getOutputStream().print("Date is" + d.toString());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		// Date d = new Date();

		// response.getWriter().println("Date is " + d.toString());
		//
		// response.getWriter().println("Testing datasource " +
		// testJndiDataSource());
		//
		// response.getWriter().println("Access Token is "+request.getParameter("at"));
		// response.getWriter().println("User Id is "+request.getParameter("id"));
		// response.getWriter().println("User Name is "+request.getParameter("name"));
		//

		// mxBean.setLoggerLevel("com.restfb.HTTP",Level.FINE.getName());
		// FacebookClient facebookClient = new
		// DefaultFacebookClient(request.getParameter("at"));
		// String token=request.getParameter("at");
		// User user = facebookClient.fetchObject("me", User.class);
		//
		// com.restfb.Connection<CategorizedFacebookType> likes =
		// facebookClient.fetchConnection("me/likes",CategorizedFacebookType.class);
		//
		//
		//
		// int i=0;
		// // Prints all 4 people
		// for (CategorizedFacebookType liker : likes.getData())
		// {
		//
		// if(i<10)
		// {
		// String query =
		// "insert into csaweb.like (user_id,object_id,object_type,post_id) values('"+user.getId()+"','"+liker.getId()+"','"+liker.getCategory()+"','"+liker.getName()+"')"
		// ;
		// logger.info(writeToMySql(query));
		// }
		// i++;
		// }
		// // logger.info(liker.getCategory());
		//
		// //String query =
		// "insert into csaweb.user_info (user_id,user_first_name,user_last_name,user_fb_token) values('"+user.getId()+"','"+user.getFirstName()+"','"+user.getLastName()+"','"+token+"')"
		// ;
		response.setContentType("text/plain");
		// PrintWriter out = response.getWriter();
		//
		// out.println("Likes Count:-"+likes.getData().size());

		String email = request.getParameter("email");
		String password = request.getParameter("password");
		System.out.println(email+"Email");
		System.out.println(password+"Password");
		String strErrMsg = null;
		HttpSession session = request.getSession();
		boolean isValidLogon = false;
		try {
			isValidLogon = authenticateLogin(email, password);
			if (isValidLogon) {
				session.setAttribute("userName", email);
			} else {
				strErrMsg = "User name or Password is invalid. Please try again.";
				System.out.println(strErrMsg);
			}
		} catch (Exception e) {
			strErrMsg = "Unable to validate user / password in database";
			System.out.println(strErrMsg);
		}

		if (isValidLogon) {
			response.sendRedirect("/HotClinic");
		} else {
			session.setAttribute("errorMsg", strErrMsg);
			response.sendRedirect("/Error");
		}

		// out.println("Testing Ajax Call from Javascript");
		// out.println("Query is"+ query);
		// out.println(writeToMySql(query));
		// out.flush();
		// out.close();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {

		super.init(config);

	}

	// public String testJndiDataSource() {
	// Connection conn = null;
	// Statement st = null;
	// ResultSet rs = null;
	// StringBuffer sb = new StringBuffer();
	// try {
	// InitialContext ctx = new InitialContext();
	// DataSource ds = (DataSource)
	// ctx.lookup("java:jboss/datasources/MysqlDS");
	//
	// // This works too
	// // Context envCtx = (Context) ctx.lookup("java:comp/env");
	// // DataSource ds = (DataSource) envCtx.lookup("jdbc/TestDB");
	//
	// conn = ds.getConnection();
	//
	// st = conn.createStatement();
	// rs = st.executeQuery("SELECT * FROM csaweb.user_info");
	//
	// while (rs.next()) {
	// String id = rs.getString("id");
	// String firstName = rs.getString("user_first_name");
	// String lastName = rs.getString("user_last_name");
	// sb.append("ID: " + id + ", First Name: " + firstName
	// + ", Last Name: " + lastName + "<br/>");
	// }
	// } catch (Exception ex) {
	// sb.append(ex.getMessage());
	// } finally {
	// try { if (rs != null) rs.close(); } catch (SQLException e) {
	// sb.append(e.getMessage());; }
	// try { if (st != null) st.close(); } catch (SQLException e) {
	// sb.append(e.getMessage());; }
	// try { if (conn != null) conn.close(); } catch (SQLException e) {
	// sb.append(e.getMessage());; }
	// }
	// return sb.toString();
	// }
	//
	// public String writeToMySql(String query) {
	// Connection conn = null;
	// Statement st = null;
	// int rs ;
	// StringBuffer sb = new StringBuffer();
	// try {
	// InitialContext ctx = new InitialContext();
	// DataSource ds = (DataSource)
	// ctx.lookup("java:jboss/datasources/MysqlDS");
	//
	// // This works too
	// // Context envCtx = (Context) ctx.lookup("java:comp/env");
	// // DataSource ds = (DataSource) envCtx.lookup("jdbc/TestDB");
	//
	// conn = ds.getConnection();
	//
	// st = conn.createStatement();
	// rs = st.executeUpdate(query);
	// //
	// // while (rs.next()) {
	// // String id = rs.getString("id");
	// // String firstName = rs.getString("user_first_name");
	// // String lastName = rs.getString("user_last_name");
	// // sb.append("ID: " + id + ", First Name: " + firstName
	// // + ", Last Name: " + lastName + "<br/>");
	// // }
	// } catch (Exception ex) {
	// sb.append(ex.getMessage());
	// } finally {
	//
	// try { if (st != null) st.close(); } catch (SQLException e) {
	// sb.append(e.getMessage());; }
	// try { if (conn != null) conn.close(); } catch (SQLException e) {
	// sb.append(e.getMessage());; }
	// }
	// return sb.toString();
	// }

	private boolean authenticateLogin(String strUserName, String strPassword)
			throws Exception {
		boolean isValid = false;

		try {
			Connection con = EDatabase.borrowConnection();
			List<Object> result = EDatabase.getFirstColumn(
					"SELECT EMAIL FROM USERS where EMAIL =? AND PASSWORD = ? ",
					strUserName, strPassword);

			if (result.equals(strUserName))
				isValid = true;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out
					.println("validateLogon: Error while validating password: "
							+ e.getMessage());
			throw e;
		} finally {
		}
		return isValid;
	}

}
