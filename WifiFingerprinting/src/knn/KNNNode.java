package knn;

public class KNNNode {
	private int index;
	private double distance;
	private int posId;
	public KNNNode(int index, double distance, int posId) {
		super();
		this.index = index;
		this.distance = distance;
		this.posId = posId;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public int getPosId() {
		return posId;
	}
	public void setPosId(int c) {
		this.posId = c;
	}
}
