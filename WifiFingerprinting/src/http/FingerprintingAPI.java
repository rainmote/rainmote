package http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import knn.KNN;

import util.DatabaseOperator;

public class FingerprintingAPI {

	private List<Map<String, Integer>> wifiList; //MAC&RSSI信息表格
	private List<HashSet<Integer>> heatmapSetList; //存储每组数据MAC对应pos_id的交集
	private Map<Integer, Integer> positionRangeMap;//每组交集的并集
	private List<Map<String, Integer>> calcWifiList; //从数据库读取出来MAC&RSSI,指针
	
	public int calcKNN() {
		List<Map.Entry<Integer, Integer>> tmap = new ArrayList<Map.Entry<Integer, Integer>>(positionRangeMap.entrySet());
		Collections.sort(tmap, new Comparator<Map.Entry<Integer, Integer>>() {

			@Override
			public int compare(Entry<Integer, Integer> o1,
					Entry<Integer, Integer> o2) {
				return o2.getValue() - o1.getValue();
			}});
		
		
		int count = tmap.get(0).getValue(); //获取热点图最大区域
		List<Integer> posIds = new ArrayList<Integer>();
		for (int i = 0; i < tmap.size(); i++) {
			if (tmap.get(i).getValue() == count) {
				posIds.add(tmap.get(i).getKey());
			}
		}
		List<Map<String, Integer>> dataList = this.calcWifiList;
		dataList.addAll(getWifiListFromDatabase(posIds)); //从数据库获取选择的pos_id对应的mac,rssi信息
		List<Map<String, Integer>> testDataList = this.wifiList;
		KNN knn = new KNN();
		for (int j = 0; j < testDataList.size(); j++) {
			Map<String, Integer> testData = testDataList.get(j);
			System.out.print("测试元组: ");
			for (Map.Entry<String, Integer> entry : testData.entrySet()) {
				System.out.print(entry.getValue() + " ");
			}
			System.out.print("\tpos_id: " + knn.knn(dataList, testData, 4) + "\n");
		}
		return 0;
	}
	
	public List<Map<String, Integer>> getWifiListFromDatabase(List<Integer> posIds) {
		DatabaseOperator databaseOp = new DatabaseOperator();
		Connection conn = databaseOp.getConnect();
		Statement stam = null;
		Statement stam2 = null;
		try {
			stam = conn.createStatement();
			stam2 = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String posIdStr = posIds.toString();
		posIdStr = posIdStr.substring(1, posIdStr.length() - 1);
		List<Map<String, Integer>> list = new ArrayList<Map<String, Integer>>();
		//show resultSet
		String sql = "select time_info,pos_id from finger where pos_id in (" + posIdStr + ") group by time_info";
		System.out.println("\nSql:"+sql);
		ResultSet res = databaseOp.getResultSet(stam, sql);
		try {
			while(res.next()) {
				Map<String, Integer> row = new TreeMap<String, Integer>();
				String timeinfo = res.getString(1);
				sql = "select mac,rssi from finger where time_info='" + timeinfo + "'";
				ResultSet t = databaseOp.getResultSet(stam2, sql);
				while(t.next()) {
					row.put(t.getString(1), t.getInt(2));
				}
				row.put("posId", res.getInt(2));
				list.add(row);
				t.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			res.close();
			stam.close();
			stam2.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public FingerprintingAPI() {
		wifiList = new ArrayList<Map<String, Integer>>();
		heatmapSetList = new ArrayList<HashSet<Integer>>();
		positionRangeMap = new HashMap<Integer, Integer>();
		calcWifiList = new ArrayList<Map<String, Integer>>();
	}
	
	public void calcHeatmap() {
		DatabaseOperator databaseOp = new DatabaseOperator();
		Connection conn = databaseOp.getConnect();
		Statement stam = null;
		ResultSet res = null;
		try {
			stam = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		for(int i = 0; i < wifiList.size(); i++) {
			Map<String, Integer> wifiInfo = wifiList.get(i);
			HashSet<Integer> hashset = new HashSet<Integer>();
			int count = 0;
			String macStr = "";
			for(Map.Entry<String, Integer> entry : wifiInfo.entrySet()) {				
				macStr += "'" + entry.getKey() + "',";
				count++;
			}
			macStr = macStr.substring(0, macStr.length() - 1);
			
			//获取每条数据的热点图
			String sql = "select  t.pos_id from (select pos_id,mac from finger group by pos_id,mac) as t " +
						"where t.mac in (" + macStr + ") group by t.pos_id having count(0)=" + count;
			res = databaseOp.getResultSet(stam, sql);
			//System.out.println(sql);
			try {	
				while(res.next()) {
					int pos_id = res.getInt(1);
					//记录交集
					hashset.add(pos_id);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			//System.out.println(i + ": " + hashset);
			this.heatmapSetList.add(hashset);
		}
		
		try {
			if (res != null) {
				res.close();
			}
			
			stam.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void getPositionRange() {
		for (int i = 0; i < this.heatmapSetList.size(); i++) {
			HashSet<Integer> hashset = this.heatmapSetList.get(i);
			Iterator<Integer> it = hashset.iterator();
			while (it.hasNext()) {
				int pos = (Integer) it.next();
				if (positionRangeMap.containsKey(pos)) {
					positionRangeMap.put(pos, positionRangeMap.get(pos)+1);
				} else {
					positionRangeMap.put(pos, 1);
				}
			}
		}
		List<Map.Entry<Integer, Integer>> tmap = new ArrayList<Map.Entry<Integer, Integer>>(positionRangeMap.entrySet());
		Collections.sort(tmap, new Comparator<Map.Entry<Integer, Integer>>() {

			@Override
			public int compare(Entry<Integer, Integer> o1,
					Entry<Integer, Integer> o2) {
				return o2.getValue() - o1.getValue();
			}});
		for (int i = 0; i < tmap.size(); i++) {
			System.out.print(tmap.get(i).getKey() + "->" + tmap.get(i).getValue() + "\t");
		}
		//System.out.println(positionRangeMap);
	}
	
	public void printWifiList() {
		Map<String, Integer> wifiInfo;
		for(int i = 0; i < this.wifiList.size(); i++) {
			wifiInfo = this.wifiList.get(i);
			ArrayList<Map.Entry<String, Integer>> entries = sortMap(wifiInfo);
//			for(Map.Entry<String, Integer> entry : wifiInfo.entrySet()) {
//				System.out.print(entry.getKey() + "->" + entry.getValue() + "\t");
//			}
			for (int j = 0; j < entries.size(); j++) {
				System.out.print(entries.get(j).getKey() + "->" + entries.get(j).getValue() + "\t");
			}
			System.out.println("");
		}
	}
	
	public ArrayList<Map.Entry<String, Integer>> sortMap(Map<String, Integer> map) {
		List<Map.Entry<String, Integer>> arrayList = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
		Collections.sort(arrayList, new Comparator<Map.Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o2.getValue() - o1.getValue();
			}
		});
		return (ArrayList<Entry<String, Integer>>) arrayList;
	}

	public void readData() {
		String filepath = "D:\\data\\176.txt";
		File file = new File(filepath);
		try {
			FileInputStream io = new FileInputStream(file);
			InputStreamReader read = new InputStreamReader(io);
			BufferedReader bu = new BufferedReader(read);
			String line = bu.readLine();
			String[] data = line.split("\t");
			do {
				Map<String, Integer> wifiInfo = new TreeMap<String, Integer>();
//				System.out.print("id:" + data[0]);
//				System.out.print("\tpos_id:" + data[1]);
				String timeinfo = data[4];
				while(timeinfo.equals(data[4]) && line != null) {
					wifiInfo.put(data[2], Integer.parseInt(data[3]));
					line = bu.readLine();
					if(line != null) {
						String[] d = line.split("\t");
						data = d;
					}
				}
				this.wifiList.add(wifiInfo);
			} while (line != null);
//				List<Map.Entry<String, Integer>> arrayList = new ArrayList<Map.Entry<String, Integer>>(wifiInfo.entrySet());
//				Collections.sort(arrayList, new Comparator<Map.Entry<String, Integer>>() {
//					@Override
//					public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
//						return o1.getValue() - o2.getValue();
//					}
//				});
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addWifiData(Map<String, Integer> data) {
		this.wifiList.add(data);
	}
	
}
