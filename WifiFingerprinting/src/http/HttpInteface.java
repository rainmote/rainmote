package http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

public class HttpInteface {
	private static int port = 8888;
	private ServerSocketChannel serverSocketChannel = null;
	private ExecutorService executorService;
	private static final int POOL_MULTIPLE = 4;
	
	public HttpInteface() {
		executorService = Executors.newFixedThreadPool(
				Runtime.getRuntime().availableProcessors() * POOL_MULTIPLE);
		try {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().setReuseAddress(true);
			serverSocketChannel.socket().bind(new InetSocketAddress(port));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Server start...");
	}
	
	public void service() {
		while (true) {
			SocketChannel socketChannel = null;
			try {
				socketChannel = serverSocketChannel.accept();
				executorService.execute(new Handler(socketChannel));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String[] args) {
		new HttpInteface().service();
	}

}


class Handler implements Runnable {
	private static String page = "fingerprinting.php";
	private SocketChannel socketChannel;
	
	public Handler(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	public void run() {
		handle(socketChannel);
	}
	
	public void handle(SocketChannel socketChannel) {
		int error = 0;
		String errorCode = "";
		int len = 0;
		FingerprintingAPI fingerAPI = new FingerprintingAPI();
		try {
			Socket socket = socketChannel.socket();
			ByteBuffer buffer = ByteBuffer.allocate(1024*128);
			socketChannel.read(buffer);
			buffer.flip();
			
			String request = decode(buffer);
			System.out.println(request);
			do {
				String firstLineOfRequest=request.substring(0,request.indexOf("\r\n")); 
	            if (firstLineOfRequest.indexOf(page) == -1) {
	            	//未请求fingerprintingAPI.php页面, 不作处理
	            }
//	            System.out.println("Accept: " + socket.getInetAddress() + 
//						":" + socket.getPort());
	            //System.out.println(request);
				Pattern p = Pattern.compile("Content-Length: \\d+");
				Matcher m = p.matcher(request);
				if (m.find()) {
					String lenStr = m.group();
					len = Integer.parseInt(lenStr.substring("Content-Length: ".length()));
	//				StringBuffer sb = new StringBuffer("HTTP/1.1 100 Continue\r\n");
	//				socketChannel.write(encode(sb.toString()));
	//				socketChannel.read(buffer);
	//				request = decode(buffer);
	//				System.out.println("==100-continue: "+ request);
				}
	            
				
				//filter post data
				p = Pattern.compile("content=\\[.*\\]");
				m = p.matcher(request);
				if (m.find()) {
					String jsonStr = m.group();
//					System.out.println(jsonStr.length());
					if (jsonStr.length() != len && len != 0) {
						error = -1;
						errorCode = "Packet length inconsistency";
						break;
					}
					jsonStr = jsonStr.substring(jsonStr.indexOf('=') + 1);
					//System.out.println(jsonStr);
					JSONArray ja = new JSONArray(jsonStr);
					for (int i = 0; i < ja.length(); i++) {
						JSONObject jo = ja.getJSONObject(i);
						JSONArray rssiArray = jo.getJSONArray("rssis");
						Map<String, Integer> rssiMap = new TreeMap<String, Integer>();
						for (int j = 0; j < rssiArray.length(); j++) {
							JSONObject t = rssiArray.getJSONObject(j);
							rssiMap.put(t.getString("mac"), t.getInt("rssi"));
						}
						fingerAPI.addWifiData(rssiMap);
					}
				}
			} while(false);
			
			if (error == 0) {
				fingerAPI.printWifiList();
				fingerAPI.calcHeatmap();
				fingerAPI.getPositionRange();
				fingerAPI.calcKNN();
				
				StringBuffer sb = new StringBuffer("HTTP/1.1 200 OK\r\n");
				sb.append("Content-Type:text/html\r\n\r\n");
				sb.append("ok");
				socketChannel.write(encode(sb.toString()));
			} else {
				StringBuffer sb = new StringBuffer("HTTP/1.1 204 No Content\r\n");
				sb.append("Content-Type:text/html\r\n\r\n");
				sb.append(errorCode);
				socketChannel.write(encode(sb.toString()));
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (socketChannel != null) {
					socketChannel.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Charset charset = Charset.forName("UTF-8");
	
	public String decode(ByteBuffer buffer) {
		CharBuffer charBuffer = charset.decode(buffer);
		return charBuffer.toString();
	}
	
	public ByteBuffer encode(String str) {
		return charset.encode(str);
	}
	
}
