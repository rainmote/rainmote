package gaussian;

public class Point {
	private double x;
	private double y;
	
	public Point() {
		this.x = 0;
		this.y = 0;
	}
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getDistancePower(Point b) {
		return (b.getX() - this.x)*(b.getX() - this.x) + (b.getY() - this.y)*(b.getY() - this.y);
	}
	
	public double getDistance(Point b) {
		return Math.sqrt(getDistancePower(b));
	}
	
	public Point getMidpoint(Point b) {
		Point a = new Point();
		a.setX((b.getX() + this.x) / 2);
		a.setY((b.getY() + this.y) / 2);
		return a;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
	
}
