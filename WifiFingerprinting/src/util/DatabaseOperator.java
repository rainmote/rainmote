package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DatabaseOperator {
	
	public Map<String, String> readConfig() {
		Map<String, String> config = new HashMap<String, String>();
		String filepath = "E:\\database.cnf";
		File file = new File(filepath);
		try {
			FileInputStream io = new FileInputStream(file);
			InputStreamReader read = new InputStreamReader(io);
			BufferedReader bu = new BufferedReader(read);
			String line;
			line = bu.readLine();
			config.put("driver", line);
			line = bu.readLine();
			config.put("url", line);
			line = bu.readLine();
			config.put("user", line);
			line = bu.readLine();
			config.put("password", line);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return config;
	}
	
	public Connection getConnect() {
		Connection conn = null;
		Map<String, String> config = readConfig();
		try {
			Class.forName(config.get("driver"));
			String url = config.get("url");
			String user = config.get("user");
			String password = config.get("password");
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
