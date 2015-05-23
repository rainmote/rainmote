package knn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class KNN {
	private static double damping = 0.8; //衰减比例
	private Comparator<KNNNode> comparator = new Comparator<KNNNode>() {
		public int compare(KNNNode o1, KNNNode o2) {
			return 0;
		}
	};
	
	private List<Integer> getRandKNum(int k, int max) {
		List<Integer> rand = new ArrayList<Integer>(k);
		for (int i = 0; i < k; i++) {
			int temp = (int) (Math.random() * max);
			if (!rand.contains(temp)) {
				rand.add(temp);
			} else {
				i--;
			}
		}
		return rand;
	}
	
	private double calcDistance(Map<String, Integer> testData, Map<String, Integer> m2) {
		double distance = 0;
		double scale = 1; //衰减因子初始化
		for (Map.Entry<String, Integer> entry : testData.entrySet()) {
			if (entry.getKey() == "posId") {
				continue;
			}
			if (m2.get(entry.getKey()) != null) {
				distance += (m2.get(entry.getKey()) - entry.getValue()) * (m2.get(entry.getKey()) - entry.getValue()) * scale;
				scale *= damping;
			}
		}
		return distance;
	}
	
	public String knn(List<Map<String, Integer>> dataList, Map<String, Integer> testData, int k) {
		PriorityQueue<KNNNode> queue = new PriorityQueue<KNNNode>(k, comparator);
		List<Integer> randNum = getRandKNum(k, dataList.size());
		for (int i = 0; i < k; i++) {
			int index = randNum.get(i);
			Map<String, Integer> currData = dataList.get(index);
			int posId = currData.get("posId"); //每条数据的posId作为分类
			KNNNode node = new KNNNode(index, calcDistance(testData, currData), posId);
			queue.add(node);
		}
		
		for (int i = 0; i < dataList.size(); i++) {
			Map<String, Integer> data = dataList.get(i);
			double distance = calcDistance(testData, data);
			KNNNode top = queue.peek();
			if (top.getDistance() > distance) {
				queue.remove();
				queue.add(new KNNNode(i, distance, data.get("posId")));
			}
		}
		return getMostCategory(queue);
	}
	
	private String getMostCategory(PriorityQueue<KNNNode> queue) {
		Map<Integer, Integer> categoryCount = new HashMap<Integer, Integer>();
		for (int i = 0; i < queue.size(); i++) {
			KNNNode node = queue.remove();
			int posId = node.getPosId();
			if (categoryCount.containsKey(posId)) {
				categoryCount.put(posId, categoryCount.get(posId) + 1);
			} else {
				categoryCount.put(posId, 1);
			}
		}
		int index = -1;
		int maxCount = 0;
		Object[] classes = categoryCount.keySet().toArray();
		for (int i = 0; i < classes.length; i++) {
			if (categoryCount.get(classes[i]) > maxCount) {
				index = i;
				maxCount = categoryCount.get(classes[i]);
			}
		}
		return classes[index].toString();
	}
}
