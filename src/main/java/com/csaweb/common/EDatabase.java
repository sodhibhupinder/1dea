package com.csaweb.common;

import java.awt.Point;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
/**
 * Provides utility methods to access MySQL database
 *  
 * @author Sasha
 *
 */
public class EDatabase {
		

	public static final int MAX_CONNECTIONS=50;//max number of connections to MySQL db 
	
	public static final int CONNECTION_TIMEOUT=60;//max time to wait for new connection (seconds)
	
	//change to "true" to enable connection pool statistics	
	public static final boolean TRACE_CONNECTION_POOLING=false;
	
	//keeps list of active connections to track possible connection leaks
	public static final Set<Integer> connectionStats=new HashSet<Integer>();
	
	//pool of connections
	private static EleganceConnectionPoolManager poolMgr;
	
	/*
	 * Returns connection from pool. inits pool if needed
	 * Remember to call EDatabase.returnConnection(Connection) method when the connection is not needed to prevent connection leaks.
	 */
	public static Connection borrowConnection() throws SQLException {
		synchronized (EDatabase.class) {
			if (poolMgr == null) {
				createPool();
			}
		}
		
		try {
			return poolMgr.getConnection();				 
		} catch (Throwable e) {
			// ELog.error("Troubles getting db connection. Will reset pool and try once more ",e);
			createPool();
			return poolMgr.getConnection();	
		}
	}
	

	/*
	 * returns connection to pool 
	 */
	public static void returnConnection(Connection con) {
		try {
			try {
				if (TRACE_CONNECTION_POOLING) // ELog.info("Connection returned to pool "+con.hashCode());
				EDatabase.connectionStats.remove(con.hashCode());
			} catch (Throwable e) {
				// ELog.error("can't close connection",e);
			}
			con.close(); 
		} catch(Throwable e) {
			 // ELog.error("can't close connection",e);
		}
	}
	
	//init database connection pool
	private static void createPool() {

		removePool();
		
//		Connection conn = null;
//		Statement st = null;
//		ResultSet rs = null;
//		StringBuffer sb = new StringBuffer();
//		MysqlConnectionPoolDataSource ds = null;
//		try {
//			InitialContext ctx = new InitialContext();
//			 ds = (MysqlConnectionPoolDataSource) ctx.lookup("java:jboss/datasources/MysqlDS");
//
//			// This works too
//			// Context envCtx = (Context) ctx.lookup("java:comp/env");
//			// DataSource ds = (DataSource) envCtx.lookup("jdbc/TestDB");
//			
//			conn = ds.getConnection();
//
//		//	st = conn.createStatement();
//		//	rs = st.executeQuery("SELECT * FROM csaweb.user_info");
//
//			while (rs.next()) {
//				String id = rs.getString("id");
//				String firstName = rs.getString("user_first_name");
//				String lastName = rs.getString("user_last_name");
//				sb.append("ID: " + id + ", First Name: " + firstName
//						+ ", Last Name: " + lastName + "<br/>");
//			}
//		} catch (Exception ex) {
//			sb.append(ex.getMessage());
//		} finally {
//			try { if (rs != null) rs.close(); } catch (SQLException e) { sb.append(e.getMessage());; }
//			try { if (st != null) st.close(); } catch (SQLException e) { sb.append(e.getMessage());; }
//			try { if (conn != null) conn.close(); } catch (SQLException e) { sb.append(e.getMessage());; }
//		}
		MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();

		dataSource.setDatabaseName("csaweb");
		dataSource.setServerName("127.6.124.129");
		dataSource.setUser("admin");
		dataSource.setPassword("nkpTVr6m7685");
//		
	//	ds.setDumpQueriesOnException(true);
		
		//ds.setConnectionLifecycleInterceptors(ConnectionInterceptor.class.getName());
		
		poolMgr = new EleganceConnectionPoolManager(dataSource);
	}
	
	public static void removePool() {
		if (poolMgr != null) {
			try {
				poolMgr.dispose();
			} catch (Throwable e) {
				// ELog.error("Can't dispose db pool.Ignoring.",e);
			}
			poolMgr = null;
		}
	}
	
	
	/**
	 *  Executes SQL SELECT query
	 * 
	 *  @inputs sql SQL SELECT query with zero or more parameters. 
	 *  @inputs params list of parameters for the sql query of type String or Integer.
	 *  @return list of object arrays. each array is the same length. the type of each object in array depends on underlying SQL type. 
	 *  see http://dev.mysql.com/doc/refman/5.1/en/connector-j-reference-type-conversions.html for mappings  
	 *  
	 *  TIP: "??" gets expanded to ?,?,?,?  if params[0] is a collection. useful for dynamic "IN (A,B,C)" clauses.       
	 */
	public static final List<Object[]> get(String sql, Object... params) throws SQLException {

		List<Object[]> result = new ArrayList<Object[]>();

		Connection con = null;

		PreparedStatement pst = null;
		ResultSet rs = null;

		try {
			
			con = EDatabase.borrowConnection();
			
			if (params==null) params=new Object[] {};
			
			if (params.length>0 && params[0] instanceof Collection) {
				sql=sql.replaceAll("\\?\\?",preparePlaceHolders(((Collection)params[0]).size()));				
			}
			
			pst = con.prepareStatement(sql);
			
			
			int i = 1;
			for (Object p : params) {
				if (p instanceof Collection) {					
					setValues(pst, ((Collection)params[0]).toArray());
				} else if (p instanceof String) {
					pst.setString(i, (String) p);
				} else if (p instanceof Integer) {
					pst.setInt(i, (Integer) p);
				} else {
					throw new IllegalStateException("only strings and ints are supportrded yet " + i + ":" + p);
				}
				i++;
			}
			rs = pst.executeQuery();
			boolean first = true;
			int columnCount = 0;
			while (rs.next()) {

				if (first) {
					ResultSetMetaData meta = rs.getMetaData();
					columnCount = meta.getColumnCount();
					first = false;
				}

				Object[] row = new Object[columnCount];

				for (int c = 1; c <= columnCount; c++) {
					row[c-1] = rs.getObject(c);
				}

				result.add(row);
			}
		} finally {

			if (rs != null)
				rs.close();
			if (pst != null)
				pst.close();
			EDatabase.returnConnection(con);
		}

		return result;
	}
	
	/**
	 *  Executes SQL UPDATE query
	 * 
	 *  @inputs sql SQL SELECT query with zero or more parameters. 
	 *  @inputs params list of parameters for the sql query of type String or Integer.
	 *  @return number of affected rows. 
	 *  
	 *  TIP: "??" gets expanded to ?,?,?,?  if params[0] is a collection. useful for dynamic "IN (A,B,C)" clauses.       
	 */
	public static final int update(String sql, Object... params) throws SQLException {

		Connection con = null;

		PreparedStatement pst = null;
	

		try {
			
			con = EDatabase.borrowConnection();
			
			if (params==null) params=new Object[] {};
			
			if (params.length>0 && params[0] instanceof Collection) {
				sql=sql.replaceAll("\\?\\?",preparePlaceHolders(((Collection)params[0]).size()));				
			}
			
			pst = con.prepareStatement(sql);
			
			
			int i = 1;
			for (Object p : params) {
				if (p instanceof Collection) {					
					setValues(pst, ((Collection)params[0]).toArray());
				} else if (p instanceof String) {
					pst.setString(i, (String) p);
				} else if (p instanceof Integer) {
					pst.setInt(i, (Integer) p);
				} else {
		 			throw new IllegalStateException("only strings and ints are supportrded yet " + i + ":" + p);
				}
			}
			
			return pst.executeUpdate();
			
		} finally {		
			if (pst!=null) pst.close();
			EDatabase.returnConnection(con);
		}
	}

	/*
	 * returns first row
	 */
	public static final Object[] getFirstRow(String sql, Object... params) throws SQLException {
		List<Object[]> list=get(sql,params);
		if (list.isEmpty()) return null;
		return list.get(0);
	}
	
	/*
	 * returns first column
	 */
	public static final List<Object> getFirstColumn(String sql, Object... params) throws SQLException {
		
		List<Object> result= new ArrayList<Object>();
		
		List<Object[]> list=get(sql,params);
		
		for(Object[] o:list) {
			result.add(o[0]);
		}
		
		return result;
	}
	

	//returns internal stats of the pool. used for debugging
	public static String  getConnectionStats() {
		return "active: "+poolMgr.getActiveConnections()+"|inactive:"+poolMgr.getInactiveConnections()+"|abandoned:"+connectionStats;		 
	}
	
	//represents a unique point in image.
	public static final class XYKey {
		private String key;
		
		public XYKey(String imageNumber, int x, int y) {
			this.key=imageNumber+"_"+x+"_"+y;
		}
	
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			return result;
		}
	
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			XYKey other = (XYKey) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			return true;
		}
		
		
		
	}
	

//	//returns object by PK
//	public static Object getObject(Integer objName) {
//		String sql="select OBJ_Name from object where OBJ_Name = ?";		
//		try {
//			List<Object> list=EDatabase.getFirstColumn(sql,objName);
//			if (list.isEmpty()) return null;
//		
//			return list.get(0); 
//		} catch (Throwable e) {
//			throw new IllegalStateException("Cant get object "+objName,e);
//		}
//		
//	}

	public static Integer[] getObjectXYZ(Integer objName) {
		
		
		String sql="select objName1,objName2,x1,y1,z1,x2,y2,z2 from display2 where objName1=? or objName2=? limit 1";		
		try {
			Object[] obj=EDatabase.getFirstRow(sql, new Object[] {objName,objName});
			if (obj==null) return null;
			
			Integer x;
			Integer y;
			Integer z;
			
			if (new Integer((String)obj[0]).equals(objName)) {
				x=(Integer)obj[2];
				y=(Integer)obj[3];
				z=(Integer)obj[4];
			} else {
				x=(Integer)obj[5];
				y=(Integer)obj[6];
				z=(Integer)obj[7];
			}
			return new Integer[] {x,y,z};
		} catch (Throwable e2)  {
			throw new IllegalStateException("Cant validate input ",e2);
		}	
	}
	
	

	
	private static String preparePlaceHolders(int length) {
	    StringBuilder builder = new StringBuilder();
	    for (int i = 0; i < length;) {
	        builder.append("?");
	        if (++i < length) {
	            builder.append(",");
	        }
	    }
	    return builder.toString();
	}

	private static void setValues(PreparedStatement preparedStatement, Object... values) throws SQLException {
	    for (int i = 0; i < values.length; i++) {
	        preparedStatement.setObject(i + 1, values[i]);
	    }
	}
	
	
}

/*
 * connection pool implementation
 */
final class EleganceConnectionPoolManager {

	//MySQL datasource
	private ConnectionPoolDataSource dataSource;
	
	//max number of connections
	private int maxConnections;
	
	//max time to wait for connection
	private long timeoutMs;
	
	//logger
	private PrintWriter logWriter;
	
	//semaphore used for sharing/locking connections 
	private Semaphore semaphore;
	
	//list of connections
	private LinkedList<PooledConnection> recycledConnections;
	
	//number of currently used connections
	private int activeConnections;
	
	//event listener
	private PoolConnectionEventListener poolConnectionEventListener;
	
	//true if connecxtion was disposed
	private boolean isDisposed;
	
	//true if need to purge the connection
	private boolean doPurgeConnection;

	//thrown when timeout occurs
	public static class TimeoutException extends RuntimeException {
		private static final long serialVersionUID = 1;

		public TimeoutException() {
			super("Timeout while waiting for a free database connection.");
		}

		public TimeoutException(String msg) {
			super(msg);
		}
	}

	//the only contructor to create connection pool
	public EleganceConnectionPoolManager(ConnectionPoolDataSource dataSource) {
		this(dataSource,EDatabase.MAX_CONNECTIONS, EDatabase.CONNECTION_TIMEOUT); 
	}

	private EleganceConnectionPoolManager(ConnectionPoolDataSource dataSource, int maxConnections, int timeout) {
		this.dataSource = dataSource;
		this.maxConnections = maxConnections;
		this.timeoutMs = timeout * 1000L;
		try {
			logWriter = dataSource.getLogWriter();
		} catch (SQLException e) {
		}
		if (maxConnections < 1) {
			throw new IllegalArgumentException("Invalid maxConnections value.");
		}
		semaphore = new Semaphore(maxConnections, true);
		recycledConnections = new LinkedList<PooledConnection>();
		poolConnectionEventListener = new PoolConnectionEventListener();
	}

	
	//frees up all underlying connections
	public synchronized void dispose() throws SQLException {
		if (isDisposed) {
			return;
		}
		isDisposed = true;
		SQLException e = null;
		while (!recycledConnections.isEmpty()) {
			PooledConnection pconn = recycledConnections.remove();
			try {
				pconn.close();
			} catch (SQLException e2) {
				if (e == null) {
					e = e2;
				}
			}
		}
		if (e != null) {
			throw e;
		}
	}

	//returns database connection from pool
	public Connection getConnection() throws SQLException {
		return getConnection2(timeoutMs);
	}

	private Connection getConnection2(long timeoutMs) throws SQLException {
		
		synchronized (this) {
			if (isDisposed) {
				throw new IllegalStateException("Connection pool has been disposed.");
			}
		}
		try {
			if (!semaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS)) {
				throw new TimeoutException();
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while waiting for a database connection.", e);
		}
		boolean ok = false;
		try {
			Connection conn = getConnection3();
			
			if (EDatabase.TRACE_CONNECTION_POOLING)  {
				
				StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
			
				//ELog.info("Connection borrowed from pool "+conn.hashCode()+" by  "+stackTraceElements[4]+":"+stackTraceElements[4].getMethodName());
			}
			EDatabase.connectionStats.add(conn.hashCode());
			
			ok = true;
			return conn;
		} finally {
			if (!ok) {
				semaphore.release();
			}
		}
	}

	private synchronized Connection getConnection3() throws SQLException {
		if (isDisposed) {
			throw new IllegalStateException("Connection pool has been disposed.");
		} // test again with lock
		PooledConnection pconn;
		if (!recycledConnections.isEmpty()) {
			pconn = recycledConnections.remove();
		} else {
			pconn = dataSource.getPooledConnection();
			pconn.addConnectionEventListener(poolConnectionEventListener);
		}
		Connection conn = pconn.getConnection();
		activeConnections++;
		assertInnerState();
		return conn;
	}

	
	private synchronized void purgeConnection(Connection conn) {
		try {
			doPurgeConnection = true;
			conn.close();
		} catch (SQLException e) {
		}

		finally {
			doPurgeConnection = false;
		}
	}

	private synchronized void recycleConnection(PooledConnection pconn) {
		if (isDisposed || doPurgeConnection) {
			disposeConnection(pconn);
			return;
		}
		if (activeConnections <= 0) {
			throw new AssertionError();
		}
		activeConnections--;
		semaphore.release();
		recycledConnections.add(pconn);
		assertInnerState();
	}

	private synchronized void disposeConnection(PooledConnection pconn) {
		pconn.removeConnectionEventListener(poolConnectionEventListener);
		if (!recycledConnections.remove(pconn)) {

			if (activeConnections <= 0) {
				throw new AssertionError();
			}
			activeConnections--;
			semaphore.release();
		}
		closeConnectionAndIgnoreException(pconn);
		assertInnerState();
	}

	private void closeConnectionAndIgnoreException(PooledConnection pconn) {
		try {
			pconn.close();
		} catch (SQLException e) {
			log("Error while closing database connection: " + e.toString());
		}
	}

	private void log(String msg) {
		String s = "MiniConnectionPoolManager: " + msg;
		try {
			if (logWriter == null) {
				//ELog.info(s);
			} else {
				logWriter.println(s);
			}
		} catch (Exception e) {
		}
	}

	private void assertInnerState() {
		if (activeConnections < 0) {
			throw new AssertionError();
		}
		if (activeConnections + recycledConnections.size() > maxConnections) {
			throw new AssertionError();
		}
		if (activeConnections + semaphore.availablePermits() > maxConnections) {
			throw new AssertionError();
		}
	}
	
	//registers connection events
	private class PoolConnectionEventListener implements ConnectionEventListener {
		public void connectionClosed(ConnectionEvent event) {
			
			PooledConnection pconn = (PooledConnection) event.getSource();
			
			try {
				if (EDatabase.connectionStats.contains(pconn.getConnection().hashCode())) {
					if (EDatabase.TRACE_CONNECTION_POOLING) //ELog.info("Connection returned to pool "+pconn.getConnection().hashCode());
					EDatabase.connectionStats.remove(pconn.getConnection().hashCode());
				}
			} catch (Throwable e) {
				// ELog.info("cant ptin close con stats "+e);
			}
			
			recycleConnection(pconn);
		}

		public void connectionErrorOccurred(ConnectionEvent event) {
			PooledConnection pconn = (PooledConnection) event.getSource();
			try {
				if (EDatabase.TRACE_CONNECTION_POOLING) // ELog.info("Connection returned to pool after error"+pconn.getConnection().hashCode());
				EDatabase.connectionStats.remove(pconn.getConnection().hashCode());
			} catch (Throwable e) {
				// ELog.info("cant ptin close con stats "+e);
			}
			
			disposeConnection(pconn);
		}
	}

	//returns number of active connections
	public synchronized int getActiveConnections() {
		return activeConnections;
	}

	//returns number of inactive connections
	public synchronized int getInactiveConnections() {
		return recycledConnections.size();
	}		
	

	
	/*
	public static FilterOptions getUserSettings(String login) {
		String sql=
			"select id,login,name,value from useroption\n"+
			"where login='sasha'\n";
		
		List<Object[]> list;
		try {
			list=get(sql,login);
			update(
					"CREATE TABLE  `elegance`.`useroption` (\n"+
					"`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY ,\n"+
					"`login` VARCHAR( 255 ) NOT NULL ,\n"+
					"`name` VARCHAR( 255 ) NOT NULL ,\n"+
					"`value` VARCHAR( 255 ) NOT NULL\n"+
					") ENGINE = MYISAM 	\n"	
			);
		} catch(SQLException e){//table does not exists
			
			try 
			{
					
				list=get(sql,login);
				
			} catch(SQLException  e1){
					Util.info("Cant get user props "+Util.e2s(e1));
					throw new IllegalStateException("Cant get user props ",e1);
			}
		}
		
		
		return null;
	}
	*/
	

		
}