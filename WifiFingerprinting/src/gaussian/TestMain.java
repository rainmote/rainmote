package gaussian;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class TestMain {
	public List<Integer> getRandNum(int num, int max) {
		List<Integer> rand = new ArrayList<Integer>(num);
		for (int i = 0; i < num; i++) {
			int temp = (int) (Math.random() * max);
			if (!rand.contains(temp)) {
				rand.add(temp);
			} else {
				i--;
			}
		}
		return rand;
	}
	
	
	public static void main(String[] args) {
		int[] data = {-39,-39,-39,-39,-43,-43,-42,-42,-43,-43,-43,-46,-47,-46,-47,-41,-41,-43,-43,-42,-43,-43,-43,-43,-42,-43,-42,-43,-43,-43,-32,-36,-42,-43,-43,-42,-42,-42,-42,-43,-43,-43,-41,-33,-33,-33,-48,-41,-43,-44,-46,-39,-49,-36,-45,-40,-38,-37,-33,-42,-42,-42,-42,-42,-42,-41,-41,-41,-40,-39,-42,-41,-41,-42,-34,-39,-37,-36,-40,-41,-40,-43,-41,-43,-40,-40,-42,-42,-43,-43,-43,-41,-42,-42,-43,-43,-43,-43,-43,-43,-43,-43,-43,-43,-43,-43,-41,-43,-42,-42,-42,-43,-43,-42,-42,-43,-42,-42,-43,-42,-43,-42,-42,-40,-42,-42,-42,-40,-42,-42,-42,-42,-42,-42,-42,-42,-42,-43,-42,-43,-43,-43,-43,-43,-39,-39,-43,-43,-42,-42,-41,-43,-43,-42,-43,-43,-43,-43,-43,-42,-42,-43,-39,-41,-43,-42,-42,-42,-42,-42,-41,-41,-41,-41,-41,-41,-41,-42,-42,-41,-42,-42,-42,-42,-42,-42,-42,-42,-42,-42,-42,-40,-42,-42,-42,-42,-41,-42,-42,-42,-42,-42,-42,-41,-42,-41,-42,-42,-41,-41,-42,-41,-42,-42,-42,-42,-42,-42,-42,-42,-41,-42,-42,-42,-42,-41,-41,-43,-43,-43,-43,-43,-43,-43,-42,-43,-42,-43,-43,-43,-42,-42,-42,-42,-41,-42,-42,-42,-42,-42,-42,-42,-41,-42,-42,-42,-42,-42,-42,-42,-42,-40,-42,-42,-40,-42,-42,-42,-40,-42,-42,-42,-42,-42,-42,-42,-43,-42,-42,-42,-42,-42,-42,-40,-42,-42,-42,-42,-42,-42,-42,-42,-42,-42,-43,-44,-43,-42,-42,-43,-42,-42,-41,-43,-42,-42,-42,-41,-42,-41,-41,-41,-43,-42,-42,-43,-42,-39,-41,-41,-42,-42,-48,-43,-43,-43,-43,-43,-41,-41,-42,-41,-41,-41,-42,-42,-41,-40,-39};
		int count = 339;
		Gaussian gaussian = new Gaussian();
		TestMain t = new TestMain();
		//List<Integer> randNum = t.getRandNum(200, count);
		List<Integer> l = new ArrayList<Integer>() ;
		for(int i = 0; i < count; i++) {
			//l.add(data[randNum.get(i)]);
			l.add(data[i]);
		}
		gaussian.setData(l);
		gaussian.calcGaussian();
		System.out.println("Average: " + gaussian.getAverage());
		System.out.println("Variance: " + gaussian.getVariance());
		System.out.println("Deviation: " + Math.sqrt(gaussian.getVariance()));
		System.out.println("Expectation: " + gaussian.getExpectation());
	} 
}
