package knn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KNNTest {
	public static void _main(String[] args) {
		List<Map<String, Integer>> dataList = new ArrayList<Map<String, Integer>>();
		List<Map<String, Integer>> testDataList = new ArrayList<Map<String, Integer>>();
		KNN knn = new KNN();
		for (int i = 0; i < testDataList.size(); i++) {
			Map<String, Integer> testData = testDataList.get(i);
			System.out.print("²âÊÔÔª×é: ");
			for (Map.Entry<String, Integer> entry : testData.entrySet()) {
				System.out.print(entry.getValue() + " ");
			}
			System.out.print("\tpos_id: ");
			System.out.println(knn.knn(dataList, testData, 4));
		}
	}
}
