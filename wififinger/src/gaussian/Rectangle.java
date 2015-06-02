package gaussian;

public class Rectangle {
	private Point leftUpper;
	private Point leftLow;
	private Point rightLow;
	private Point rightUpper;
	
	public Rectangle(Point leftLow, Point rightLow, Point rightUpper, Point leftUpper) {
		this.leftLow = leftLow;
		this.rightLow = rightLow;
		this.rightUpper = rightUpper;
		this.leftUpper = leftUpper;
	}
	
	public Point getRectangleMidpoint() {
		return this.leftLow.getMidpoint(this.rightUpper);
	}
	
	public Rectangle getLeftLowRectangle() {
		Rectangle r = new Rectangle(this.leftLow,
							this.leftLow.getMidpoint(this.rightLow),
							getRectangleMidpoint(),
							this.leftLow.getMidpoint(this.leftUpper));
		return r;
	}
	
	public Rectangle getRightLowRectangle() {
		Rectangle r = new Rectangle(this.leftLow.getMidpoint(this.rightLow),
							this.rightLow,
							this.rightLow.getMidpoint(this.rightUpper),
							getRectangleMidpoint());
		return r;
	}
	
	public Rectangle getRightUpperRectangle() {
		Rectangle r = new Rectangle(getRectangleMidpoint(),
							this.rightLow.getMidpoint(this.rightUpper),
							this.rightUpper,
							this.rightUpper.getMidpoint(this.leftUpper));
		return r;
	}
	
	public Rectangle getLeftUpperRectangle() {
		Rectangle r = new Rectangle(this.leftLow.getMidpoint(this.leftUpper),
							getRectangleMidpoint(),
							this.rightUpper.getMidpoint(this.leftUpper),
							this.leftUpper);
		return r;
	}
	
	public Rectangle getPointArea(Point a) {
		Point midpoint = getRectangleMidpoint();
		if (a.getX() < midpoint.getX() && a.getY() < midpoint.getY()) {
			return getLeftLowRectangle();
		} else if (a.getX() > midpoint.getX() && a.getY() < midpoint.getY()) {
			return getRightLowRectangle();
		} else if (a.getX() > midpoint.getX() && a.getY() > midpoint.getY()) {
			return getRightUpperRectangle();
		} else if (a.getX() < midpoint.getX() && a.getY() > midpoint.getY()) {
			return getLeftUpperRectangle();
		}
		return null;
	}
}
