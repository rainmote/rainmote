package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseOperator {
	
	public Connection getConnect() {
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://:3306/positioning";
			String user = "";
			String password = "";
			conn = DriverManager.getConnection(url, user, password);
			if (conn == null)
			{
				System.out.println("Connection database failed!");
				return conn;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	public ResultSet getResultSet(Statement stam, String sql) {
		ResultSet res = null;
		try {
			res = stam.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}
}
