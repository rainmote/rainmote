package knn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;


public class KNN {
	private static double damping = 0.8; //衰减比例

	public KNN() {
		
	}
	
	private Comparator<KNNNode> comparator = new Comparator<KNNNode>() {
		public int compare(KNNNode o1, KNNNode o2) {
			double t = o2.getDistance() - o1.getDistance();
			if (t > 0) {
				return 1;
			} else if (t < 0) {
				return -1;
			} 
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
		double distance = 0; //初始化距离
		double scale = 1; //衰减因子初始化
		for (Map.Entry<String, Integer> entry : testData.entrySet()) {
			if (entry.getKey() == "posId") { //忽略非MAC地址的数据
				continue;
			}
			if (m2.get(entry.getKey()) != null) { //如果MAC地址相同
				//按照权重计算距离
				distance += (m2.get(entry.getKey()) - entry.getValue()) * 
							(m2.get(entry.getKey()) - entry.getValue()) * scale;
				scale *= damping;
			}
		}
		return distance;
	}
	
	public String knn(List<Map<String, Integer>> dataList, Map<String, Integer> testData, int k) {
		PriorityQueue<KNNNode> queue = new PriorityQueue<KNNNode>(k, comparator);
		List<Integer> randNum = getRandKNum(k, dataList.size());
		//从训练元组中随机选取k个初始化优先级队列
		for (int i = 0; i < k; i++) {
			int index = randNum.get(i); //产生随机数
			Map<String, Integer> currData = dataList.get(index); //获取训练数据
			int posId = currData.get("posId"); //每条数据的posId作为分类
			KNNNode node = new KNNNode(index, calcDistance(testData, currData), posId);
			queue.add(node); //将元组加入优先级队列
		}
		//遍历训练元组
		for (int i = 0; i < dataList.size(); i++) {
			Map<String, Integer> data = dataList.get(i);
			double distance = calcDistance(testData, data); //计算测试元组与训练元组的距离
			KNNNode top = queue.peek(); //获取优先级队列距离最大的元素
			if (top.getDistance() > distance) { //如果队首元组距离大于当前训练元组距离
				queue.remove(); //移除队首元素
				queue.add(new KNNNode(i, distance, data.get("posId"))); //将当前元组加入队列
			}
		}
		return getMostCategory(queue); //获取分类结果
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
