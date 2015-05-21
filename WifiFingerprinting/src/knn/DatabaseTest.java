package knn;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import util.DatabaseOperator;

public class DatabaseTest {

	/**
	 * @param args
	 */
	public static void Testmain(String[] args) {
		// TODO Auto-generated method stub
		DatabaseOperator databaseOp = new DatabaseOperator();
		Connection conn = databaseOp.getConnect();
		Statement stam = null;
		try {
			stam = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//show resultSet
		String sql = "Select * from wifi where num = 99;";
		ResultSet res = databaseOp.getResultSet(stam, sql);
		try {
			while(res.next()) {
				System.out.println("BSSID:" + res.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			res.close();
			stam.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//insert something into table
	}
}
