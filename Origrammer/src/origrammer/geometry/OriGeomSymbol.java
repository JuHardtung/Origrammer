package origrammer.geometry;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import javax.vecmath.Vector2d;

public class OriGeomSymbol {

	
	final public static int TYPE_NONE = 0;
	final public static int TYPE_ROTATION = 1;
	final public static int TYPE_HOLD = 2;
	final public static int TYPE_HOLD_AND_PULL = 3;
	final public static int TYPE_NEXT_VIEW_HERE = 4;
	final public static int TYPE_XRAY_CIRCLE = 5;
	final public static int TYPE_CLOSED_SINK = 6;
	
	private int type = TYPE_NONE;
	private Vector2d position;
	private Vector2d direction;
	private double size;
	private String text;
	private boolean isReversed;
	private boolean isSelected;
	
	
	public OriGeomSymbol() {
	}
	
	public OriGeomSymbol(OriGeomSymbol gs) {
		this.type = gs.type;
		this.position = gs.position;
		this.direction = gs.direction;
		this.size = gs.size;
		this.text = gs.text;
		this.isReversed = gs.isReversed;
		this.isSelected = gs.isSelected;
	}
	
	public OriGeomSymbol(Vector2d position, double size) {
		this.position = position;
		this.size = size;
	}
	
	public OriGeomSymbol(Vector2d position, Vector2d direction, double size) {
		this.position = position;
		this.direction = direction;
		this.size = size;
	}
	
	public OriGeomSymbol(Vector2d position, Vector2d direction, double size, int type) {
		this.position = position;
		this.direction = direction;
		this.size = size;
		this.type = type;
	}
	
	public ArrayList<Shape> getShapesForDrawing() {
		ArrayList<Shape> shapes = new ArrayList<>();
		switch(type) {
			case TYPE_ROTATION:
				shapes = getRotationSymbolShapes();
				break;
			case TYPE_HOLD:
				shapes = getHoldSymbolShapes();
				break;
			case TYPE_HOLD_AND_PULL:
				shapes = getHoldAndPullSymbolShapes();
				break;
			case TYPE_NEXT_VIEW_HERE:
				shapes = getNextViewHereSymbolShapes();
				break;
			case TYPE_XRAY_CIRCLE:
				shapes = getXRayCircleSymbolShapes();
				break;
			case TYPE_CLOSED_SINK:
				shapes = getClosedSinkSymbolShapes();
				break;
		}
		return shapes;
	}
	
	private ArrayList<Shape> getRotationSymbolShapes() {
		ArrayList<Shape> shapes = new ArrayList<>();
		
		Vector2d uv = new Vector2d(1, 0);
		Vector2d uv90 = new Vector2d(0,-1);
		
		double angle30 = Math.toRadians(30);
		double angleMinus45X = uv.x * Math.cos(angle30) - uv.y * Math.sin(angle30);
		double angleMinus45Y = uv.x * Math.sin(angle30) + uv.y * Math.cos(angle30);
		Vector2d uv30 = new Vector2d(angleMinus45X, angleMinus45Y);
		
		Vector2d upArrowPos;
		Vector2d lowArrowPos;
		
		if (isReversed) {
			upArrowPos = new Vector2d(position.x + 7, position.y - uv90.y * size / 2 - 0.5);
			lowArrowPos = new Vector2d(position.x - 7, position.y + uv90.y * size / 2 + 0.5);
		} else {
			upArrowPos = new Vector2d(position.x + 7, position.y + uv90.y * size / 2 + 0.5);
			lowArrowPos = new Vector2d(position.x - 7, position.y - uv90.y * size / 2 - 0.5);
		}
		
		Line2D.Double tailL = new Line2D.Double(upArrowPos.x, upArrowPos.y, upArrowPos.x - uv30.x * 15, upArrowPos.y - uv30.y * 15);
		Line2D.Double tailR = new Line2D.Double(upArrowPos.x, upArrowPos.y, upArrowPos.x - uv30.x * 15, upArrowPos.y + uv30.y * 15);
		
		Line2D.Double lowerTailL = new Line2D.Double(lowArrowPos.x, lowArrowPos.y, lowArrowPos.x + uv30.x * 15, lowArrowPos.y + uv30.y * 15);
		Line2D.Double lowerTailR = new Line2D.Double(lowArrowPos.x, lowArrowPos.y, lowArrowPos.x + uv30.x * 15, lowArrowPos.y - uv30.y * 15);
				
		Arc2D.Double rotationCircle = new Arc2D.Double(position.x-size/2, position.y-size/2, size, size, 0, 360, Arc2D.OPEN);
		
		shapes.add(tailL);
		shapes.add(tailR);
		shapes.add(lowerTailL);
		shapes.add(lowerTailR);
		shapes.add(rotationCircle);
		return shapes;
	}
	
	private ArrayList<Shape> getHoldSymbolShapes() {
		ArrayList<Shape> shapes = new ArrayList<>();
		Arc2D.Double holdCircle = new Arc2D.Double(position.x - size / 2, position.y - size / 2, size, size, 0, 360, Arc2D.OPEN);
		shapes.add(holdCircle);
		return shapes;
	}
	
	private ArrayList<Shape> getHoldAndPullSymbolShapes() {
		ArrayList<Shape> shapes = new ArrayList<>();
		Arc2D.Double holdCircle = new Arc2D.Double(position.x - size / 2, position.y - size / 2, 
													size, size, 0, 360, Arc2D.OPEN);
		
		Vector2d lineStart = new Vector2d(position.x + direction.x * size / 2, position.y + direction.y  * size / 2);
		Vector2d lineEnd = new Vector2d(lineStart.x + direction.x * 40, lineStart.y + direction.y * 40);
		Line2D.Double holdLine = new Line2D.Double(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y);
		
		double angle35 = Math.toRadians(35);
		double angle35X = direction.x * Math.cos(angle35) - direction.y * Math.sin(angle35);
		double angle35Y = direction.x * Math.sin(angle35) + direction.y * Math.cos(angle35);
		Vector2d uv35 = new Vector2d(angle35X, angle35Y);
		
		double angle145 = Math.toRadians(145);
		double angle145X = direction.x * Math.cos(angle145) - direction.y * Math.sin(angle145);
		double angle145Y = direction.x * Math.sin(angle145) + direction.y * Math.cos(angle145);
		Vector2d uv145 = new Vector2d(angle145X, angle145Y);

		Line2D.Double arrowHeadL = new Line2D.Double(lineEnd.x, lineEnd.y, lineEnd.x - uv35.x * 15, lineEnd.y - uv35.y * 15);
		Line2D.Double arrowHeadR = new Line2D.Double(lineEnd.x, lineEnd.y, lineEnd.x + uv145.x * 15, lineEnd.y + uv145.y * 15);

		shapes.add(arrowHeadL);
		shapes.add(arrowHeadR);
		shapes.add(holdLine);
		shapes.add(holdCircle);
		return shapes;
	}
	
	private ArrayList<Shape> getNextViewHereSymbolShapes() {
		ArrayList<Shape> shapes = new ArrayList<>();
		
		double angle25 = Math.toRadians(25);
		double angle25X = direction.x * Math.cos(angle25) - direction.y * Math.sin(angle25);
		double angle25Y = direction.x * Math.sin(angle25) + direction.y * Math.cos(angle25);
		Vector2d uv25 = new Vector2d(angle25X, angle25Y);
		
		double angle155 = Math.toRadians(155);
		double angle155X = direction.x * Math.cos(angle155) - direction.y * Math.sin(angle155);
		double angle155Y = direction.x * Math.sin(angle155) + direction.y * Math.cos(angle155);
		Vector2d uv155 = new Vector2d(angle155X, angle155Y);
		
		GeneralPath eyeOutline = new GeneralPath();	
		eyeOutline.moveTo(position.x - uv155.x * 60, position.y - uv155.y * 60);
		eyeOutline.lineTo(position.x, position.y);
		eyeOutline.lineTo(position.x + uv25.x * 60, position.y + uv25.y * 60);

		double angle = Math.toDegrees(Math.atan2(-direction.y, direction.x));
		double upper = angle - 25;
		Arc2D.Double eyeball = new Arc2D.Double(position.x - size / 2, position.y - size / 2, size, size, upper, 50, Arc2D.OPEN);
		
		Vector2d irisStart = new Vector2d(position.x + direction.x * 55, position.y + direction.y * 55);
		double start = angle + 90 + 27;
		Arc2D.Double iris = new Arc2D.Double(irisStart.x - size/8, irisStart.y - size/8, size/4, size/4, start, 124, Arc2D.OPEN);

		shapes.add(iris);
		shapes.add(eyeball);
		shapes.add(eyeOutline);
		return shapes;
	}
	
	private ArrayList<Shape> getXRayCircleSymbolShapes() {
		ArrayList<Shape> shapes = new ArrayList<>();
		Shape circle = new Ellipse2D.Double(position.x, position.y, size, size);
		shapes.add(circle);
		return shapes;
	}
	
	private ArrayList<Shape> getClosedSinkSymbolShapes() {
		ArrayList<Shape> shapes = new ArrayList<>();
		Shape circle = new Ellipse2D.Double(position.x - size / 2, position.y - size / 2, size, size);
		shapes.add(circle);
		return shapes;
	}
	
	public void moveBy(double xTrans, double yTrans) {
		position.x += xTrans;
		position.y += yTrans;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Vector2d getPosition() {
		return position;
	}

	public void setPosition(Vector2d position) {
		this.position = position;
	}

	public Vector2d getDirection() {
		return direction;
	}

	public void setDirection(Vector2d direction) {
		this.direction = direction;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isReversed() {
		return isReversed;
	}

	public void setReversed(boolean isReversed) {
		this.isReversed = isReversed;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	@Override
	public String toString() {
		return "OriGeomSymbol [type=" + type + ", position=" + position + ", direction=" + direction + ", size=" + size
				+ ", isSelected=" + isSelected + "]";
	}

}
