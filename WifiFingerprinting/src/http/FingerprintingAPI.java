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

import util.DatabaseOperator;

public class FingerprintingAPI {

	private List<Map<String, Integer>> wifiList; //MAC&RSSI信息表格
	private List<HashSet<Integer>> heatmapSetList; //存储每组数据MAC对应pos_id的交集
	private Map<Integer, Integer> positionRangeMap;//每组交集的并集
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FingerprintingAPI finger = new FingerprintingAPI();
		finger.readData();
		finger.calcHeatmap();
		finger.getPositionRange();
		//finger.printWifiList();
	}
	
	public FingerprintingAPI() {
		wifiList = new ArrayList<Map<String, Integer>>();
		heatmapSetList = new ArrayList<HashSet<Integer>>();
		positionRangeMap = new HashMap<Integer, Integer>();
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
			res.close();
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
		System.out.print(positionRangeMap.size() + " ");
		System.out.println(positionRangeMap);
	}
	
	public void printWifiList() {
		Map<String, Integer> wifiInfo;
		for(int i = 0; i < this.wifiList.size(); i++) {
			wifiInfo = this.wifiList.get(i);
//			ArrayList<Map.Entry<String, Integer>> entries = sortMap(wifiInfo);
			for(Map.Entry<String, Integer> entry : wifiInfo.entrySet()) {
				System.out.print(entry.getKey() + "->" + entry.getValue() + "\t");
			}
//			for (int j = 0; j < entries.size(); j++) {
//				System.out.print(entries.get(j).getKey() + "->" + entries.get(j).getValue() + "\t");
//			}
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
	
}
