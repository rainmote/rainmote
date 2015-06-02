package http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class WifiFingerprinting extends HttpServlet {

	private String result = "";
	/**
	 * Constructor of the object.
	 */
	public WifiFingerprinting() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		result = "{\"code\": 1,\"detail\": \"GET method nonsupport\"}";
		out.print(result);
		out.flush();
		out.close();
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		//request.setAttribute("Content-Type", "application/json");
		PrintWriter out = response.getWriter();
		String jsonStr = request.getParameter("content");
		
		if("".equals(jsonStr)) {
			result = "{\"code\": 1,\"detail\": \"Request Parameter NULL\"}";
		} else {
			//result = "{\"detail\":{\"y\":\"0.5592\",\"x\":\"0.3356\"},\"code\":0}";
			result = handler(out, jsonStr);
		}
		
		out.println(result);
		out.flush();
		out.close();
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}
	
	public String handler(PrintWriter out, String jsonStr) {
		FingerprintingAPI fingerAPI = new FingerprintingAPI();
		fingerAPI.info(jsonStr);
		try {
			JSONArray ja = new JSONArray(jsonStr);
			if (ja.isNull(0)) {
				fingerAPI.info("Parameter Exception");
				return "{\"code\": 1,\"detail\": \"Parameter Exception.\"}";
			}
			for (int i = 0; i < ja.length(); i++) {
				JSONObject jo = ja.getJSONObject(i);
				String buildId = jo.getString("buildingId");
				String floor = jo.getString("floor");
				fingerAPI.setMapId(buildId + "_" + floor);
				JSONArray rssiArray = jo.getJSONArray("rssis");
				Map<String, Integer> rssiMap = new TreeMap<String, Integer>();
				for (int j = 0; j < rssiArray.length(); j++) {
					JSONObject t = rssiArray.getJSONObject(j);
					rssiMap.put(t.getString("mac"), t.getInt("rssi"));
				}
				fingerAPI.addWifiData(rssiMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		int ret;
		//fingerAPI.printWifiList();
		ret = fingerAPI.calcHeatmap(); 
		if (ret < 0) {
			return "{\"code\": 1,\"detail\": \"Database Operation Exception. Status:" + ret + "\"}";
		}
		ret = fingerAPI.getPositionRange();
		if (ret < 0) {
			return "{\"code\": 1,\"detail\": \"Get Range Exception. Status:" + ret + "\"}";
		}
		ret = fingerAPI.calcKNN();
		if (ret < 0) {
			return "{\"code\": 1,\"detail\": \"KNN Exception. Status:" + ret + "\"}";
		}
		String result = fingerAPI.getPositionResult();
		if ("".equals(result)) {
			result = "{\"code\": 1,\"detail\": \"Result null.\"}";
		}
		return result;
	}

}
