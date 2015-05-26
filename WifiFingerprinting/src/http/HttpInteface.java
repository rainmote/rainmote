package http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
	private Selector selector;
	private SelectionKey serverkey;
	
	public HttpInteface() {
		executorService = Executors.newFixedThreadPool(
				Runtime.getRuntime().availableProcessors() * POOL_MULTIPLE);
		try {
			selector = Selector.open(); //����ѡ����
			serverSocketChannel = ServerSocketChannel.open(); //�򿪼����ŵ�
			//serverSocketChannel.socket().setReuseAddress(true);
			serverSocketChannel.socket().bind(new InetSocketAddress(port));
			serverSocketChannel.configureBlocking(false);
			// �������ŵ����ܹ�ע��ѡ��������ע�������ָ�����ŵ����Խ���Accept����
			serverkey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Server start...");
	}
	
	public void service() {
		while (true) {
			try {
				// ȡ��ѡ�����ļ����¼�
				selector.select(); 
				// ȡ��ͨ���ڼ����¼��ļ���
				Set<SelectionKey> keys = selector.selectedKeys();
				// ���������¼�
				for (Iterator<SelectionKey> it = keys.iterator(); it.hasNext(); ) {
					SelectionKey key = (SelectionKey) it.next();
					// �Ƴ����¼�
					it.remove();
					// check if it's a connection request
					if (key.isAcceptable()) {
						// ȡ����Ӧ��SocketChannel
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel channel = server.accept();
						if (channel == null) {
							continue;
						}
						//channel.configureBlocking(false);
						// �ڴ�ͨ����ע���¼�
						//channel.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE);
						new Thread(new Handler(channel)).start();
						//executorService.execute(new Handler(channel));	
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		new HttpInteface().service();
	}

}


class Handler implements Runnable {
	private static final int RECVBUFFERSIZE = 15 * 1024;
	private static final int SENDBUFFERSIZE = 1024;
	private static final long timeout = 30 * 1000;
	private static String page = "fingerprinting.php";
	private TcpChannel channel;

	public Handler(SocketChannel channel) throws Exception {
		this.channel = new TcpChannel(channel, System.currentTimeMillis() + timeout, SelectionKey.OP_READ);
	}

	public void run() {
		try {
			do {
				work();
			} while (false);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			channel.cleanup();
		}
		//handle(channel);
	}
	
	private void work() throws IOException {
		System.out.println("work running...");
		byte[] cache = new byte[RECVBUFFERSIZE];
		byte[] reply = new byte[SENDBUFFERSIZE];
		read(cache, reply);
	}
	
	private void read(byte[] cache, byte[] reply) throws IOException {
		channel.recv(ByteBuffer.wrap(cache));
		String request = new String(cache, "UTF-16");
		System.out.println(request);
	}
	
	/*
	public void handle(TcpChannel channel2) {
		int error = 0;
		String errorCode = "";
		int len = 0;
		FingerprintingAPI fingerAPI = new FingerprintingAPI();
		try {
			Socket socket = channel2.socket();
			ByteBuffer buffer = ByteBuffer.allocate(1024*128);
			if (channel2.read(buffer) == -1) {
				channel2.close();
				return;
			}
			
			buffer.flip();
			String request = decode(buffer);
			System.out.println(request);
			do {
				String firstLineOfRequest=request.substring(0,request.indexOf("\r\n")); 
	            if (firstLineOfRequest.indexOf(page) == -1) {
	            	//δ����fingerprintingAPI.phpҳ��, ��������
	            }
//	            System.out.println("Accept: " + socket.getInetAddress() + 
//						":" + socket.getPort());
	            System.out.println(request);
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
					//System.out.println("json string:" + jsonStr);
					if (jsonStr.length() != len && len != 0) {
						error = -1;
						errorCode = "Packet length inconsistency";
						break;
					}
					jsonStr = jsonStr.substring(jsonStr.indexOf('=') + 1);
					
					JSONArray ja = new JSONArray(jsonStr);
					for (int i = 0; i < ja.length(); i++) {
						JSONObject jo = ja.getJSONObject(i);
						JSONArray rssiArray = jo.getJSONArray("rssis");
						Map<String, Integer> rssiMap = new TreeMap<String, Integer>();
						for (int j = 0; j < rssiArray.length(); j++) {
							JSONObject t = rssiArray.getJSONObject(j);
							System.out.println(t.getString("mac") + "->" + t.getInt("rssi"));
							rssiMap.put(t.getString("mac"), t.getInt("rssi"));
						}
						fingerAPI.addWifiData(rssiMap);
					}
				}
			} while(false);
			
			if (error == 0) {
				System.out.println("print Wifi List ok");
				fingerAPI.printWifiList();
				fingerAPI.calcHeatmap();
				fingerAPI.getPositionRange();
				fingerAPI.calcKNN();
				
				StringBuffer sb = new StringBuffer("HTTP/1.1 200 OK\r\n");
				sb.append("Content-Type:text/html\r\n\r\n");
				sb.append("ok");
				channel2.write(encode(sb.toString()));
			} else {
				StringBuffer sb = new StringBuffer("HTTP/1.1 204 No Content\r\n");
				sb.append("Content-Type:text/html\r\n\r\n");
				sb.append(errorCode);
				channel2.write(encode(sb.toString()));
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (channel2 != null) {
					channel2.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Charset charset = Charset.forName("UTF-16");
	
	public String decode(ByteBuffer buffer) {
		String str = null;
		try {
			str = charset.newDecoder().decode(buffer).toString();
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}
	
	public ByteBuffer encode(String str) {
		ByteBuffer buffer = charset.encode(str);
		return buffer;
	}*/
	
}
