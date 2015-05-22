package knn;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

import util.DatabaseOperator;

public class DatabaseTest {

	/**
	 * @param args
	 */
	public static void _main(String[] args) {
		// TODO Auto-generated method stub
		DatabaseOperator databaseOp = new DatabaseOperator();
		Connection conn = databaseOp.getConnect();
		Statement stam = null;
		try {
			stam = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		List<String> macList = new ArrayList<String>(8);
		
		
		//show resultSet
		String sql = "select id,pos_id,mac1,rssi1,mac2,rssi2,mac3,rssi3,mac4,rssi4,mac5,rssi5,mac6,rssi6,mac7,rssi7,mac8,rssi8" +
				" from finger where " +
				"id=1250;";
		ResultSet res = databaseOp.getResultSet(stam, sql);
		try {
			while(res.next()) {
				System.out.println("id:" + res.getInt(1));
				System.out.println("pos_id:" + res.getInt(2));
				System.out.println("mac1:" + res.getString(3));
				System.out.println("rssi1:" + res.getString(4));
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
