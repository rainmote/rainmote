package gaussian;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class LeastSquareMethod {
	private Map<Point, Double> pointMap; // µ„-»®÷ÿ
	private List<Map.Entry<Point, Double>> pointList;
	
	public LeastSquareMethod() {
		pointMap = new HashMap<Point, Double>();
	}
	
	public void setPointList(Map<Point, Double> map) {
		this.pointMap.putAll(map);
	}
	
	public Map<Point, Double> getPointList() {
		return this.pointMap;
	}
	
	public Point getMinPoint() {
		double minX = Integer.MAX_VALUE;
		double minY = Integer.MAX_VALUE;
		for (Point key : this.pointMap.keySet()) {
			if (key.getX() < minX) {
				minX = key.getX();
			}
			if (key.getY() < minY) {
				minY = key.getY();
			}
		}
		Point minPoint = new Point(minX, minY);
		return minPoint;
	}
	
	public Point getMaxPoint() {
		double maxX = 0;
		double maxY = 0;
		for (Point key : this.pointMap.keySet()) {
			if (key.getX() > maxX) {
				maxX = key.getX();
			}
			if (key.getY() > maxY) {
				maxY = key.getY();
			}
		}
		Point maxPoint = new Point(maxX, maxY);
		return maxPoint;
	}
	
	public Point calcTargetPosition() {
		Point minPoint = getMinPoint();
		Point maxPoint = getMaxPoint();
		pointList = new ArrayList<Map.Entry<Point, Double>>(this.pointMap.entrySet());
		Collections.sort(pointList, new Comparator<Map.Entry<Point, Double>>() {
			@Override
			public int compare(Entry<Point, Double> o1,
					Entry<Point, Double> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}			
		});
		
		Rectangle rect = new Rectangle(minPoint,
								new Point(maxPoint.getX(), minPoint.getY()),
								maxPoint,
								new Point(minPoint.getX(), maxPoint.getY()));
		do {
			Rectangle area = getTargetRectangle(rect);
			if (area == null) {
				break;
			}
			rect = area;
		} while(true);
		return rect.getRectangleMidpoint();
	}
	
	public Rectangle getTargetRectangle(Rectangle rect) {
		Point midpoint = rect.getRectangleMidpoint();
		Point[] test = new Point[4];
		test[0] = rect.getLeftLowRectangle().getRectangleMidpoint();
		test[1] = rect.getRightLowRectangle().getRectangleMidpoint();
		test[2] = rect.getRightUpperRectangle().getRectangleMidpoint();
		test[3] = rect.getLeftUpperRectangle().getRectangleMidpoint();
		double initDistance = calcDistance(midpoint);
		double distance = initDistance;
		int index = 0;
		for (int i = 0; i < 4; i++) {
			double t = calcDistance(test[i]);
			if (t < distance) {
				distance = t;
				index = i;
			}
		}
		if (index == 0 && distance == initDistance) {
			return null;
		}
		Rectangle r;
		switch(index) {
		case 0:
			r = rect.getLeftLowRectangle();
			break;
		case 1:
			r = rect.getRightLowRectangle();
			break;
		case 2:
			r = rect.getRightUpperRectangle();
			break;
		case 3:
			r = rect.getLeftUpperRectangle();
			break;
		default:
			r = null;
		}
		return r;
	}
	
	public double calcDistance(Point point) {
		double result = 0;
		Point one = this.pointList.get(0).getKey();
		double d = one.getDistance(point);
		double m = this.pointList.get(0).getValue();
		for (int i = 1; i < this.pointList.size(); i++) {
			Point pi = this.pointList.get(i).getKey();
			result += Math.pow((point.getDistance(pi)/d * this.pointList.get(i).getValue()/m - 1), 2);
		}
		return result;
	}
}
