package gaussian;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Gaussian {
	private double average;
	private double variance;
	private double expectation;
	private List<Integer> data;
	
	public Gaussian() {
		average = 0;
		variance = 0;
		expectation = 0;
	}
	
	private double calcAverage() {
		double sum = 0;
		for (int i = 0;i < data.size(); i++) {
			sum += data.get(i);
		}
		return sum / data.size();
	}
	
	private double calcVariance(double avg) {
		int count = data.size();
		double sum = 0.0;
		for (int i = 0; i < count; i++) {
			sum += (this.data.get(i) - avg) * (this.data.get(i) - avg);
		}
		return sum / count;
	}
	
	private double calcExpectation() {
		int count = data.size();
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < count; i++) {
			int num = this.data.get(i);
			if (map.containsKey(num)) {
				map.put(num, map.get(num)+1);
			} else {
				map.put(num, 1);
			}
			
		}
		double sum = 0;
		for(Map.Entry<Integer, Integer> entry : map.entrySet()) {
			sum += entry.getKey() * entry.getValue() / (double)count;
		}
		return sum;
	}
	
	public void calcGaussian() {
		this.average = calcAverage();
		this.variance = calcVariance(this.average);
		this.expectation = calcExpectation();
	}

	public double getAverage() {
		return average;
	}

	public void setAverage(double average) {
		this.average = average;
	}

	public double getVariance() {
		return variance;
	}

	public void setVariance(double variance) {
		this.variance = variance;
	}
	
	public double getExpectation() {
		return expectation;
	}
	
	public void setExpectation(double expectation) {
		this.expectation = expectation;
	}

	public List<Integer> getData() {
		return data;
	}

	public void setData(List<Integer> data) {
		this.data = data;
	}
	
}
