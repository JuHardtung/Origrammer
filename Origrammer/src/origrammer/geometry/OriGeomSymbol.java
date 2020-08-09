package origrammer.geometry;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
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
	final public static int TYPE_LEADER = 7;
	final public static int TYPE_REPETITION = 8;
	
	private int type = TYPE_NONE;
	private Vector2d p1;
	private Vector2d p2;
	private double size;
	private String text;
	private boolean isReversed;
	private boolean isSelected;
	
	
	public OriGeomSymbol() {
	}
	
	public OriGeomSymbol(OriGeomSymbol gs) {
		this.type = gs.type;
		this.p1 = gs.p1;
		this.p2 = gs.p2;
		this.size = gs.size;
		this.text = gs.text;
		this.isReversed = gs.isReversed;
		this.isSelected = gs.isSelected;
	}
	
	public OriGeomSymbol(Vector2d p1, double size, int type) {
		this.p1 = p1;
		this.size = size;
		this.type = type;
	}
	
	public OriGeomSymbol(Vector2d p1, Vector2d p2, double size) {
		this.p1 = p1;
		this.p2 = p2;
		this.size = size;
	}
	
	public OriGeomSymbol(Vector2d p1, Vector2d p2, double size, int type) {
		this.p1 = p1;
		this.p2 = p2;
		this.size = size;
		this.type = type;
	}
	
	public OriGeomSymbol(Vector2d p1, Vector2d p2, double size, int type, String text, boolean isReversed) {
		this.p1 = p1;
		this.p2 = p2;
		this.size = size;
		this.type = type;
		this.text = text;
		this.isReversed = isReversed;
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
			case TYPE_LEADER:
				shapes = getLeaderSymbolShapes();
				break;
			case TYPE_REPETITION:
				shapes = getRepetitionSymbolShapes();
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
			upArrowPos = new Vector2d(p1.x + 7, p1.y - uv90.y * size / 2 - 0.5);
			lowArrowPos = new Vector2d(p1.x - 7, p1.y + uv90.y * size / 2 + 0.5);
		} else {
			upArrowPos = new Vector2d(p1.x + 7, p1.y + uv90.y * size / 2 + 0.5);
			lowArrowPos = new Vector2d(p1.x - 7, p1.y - uv90.y * size / 2 - 0.5);
		}
		
		Line2D.Double tailL = new Line2D.Double(upArrowPos.x, upArrowPos.y, upArrowPos.x - uv30.x * 15, upArrowPos.y - uv30.y * 15);
		Line2D.Double tailR = new Line2D.Double(upArrowPos.x, upArrowPos.y, upArrowPos.x - uv30.x * 15, upArrowPos.y + uv30.y * 15);
		
		Line2D.Double lowerTailL = new Line2D.Double(lowArrowPos.x, lowArrowPos.y, lowArrowPos.x + uv30.x * 15, lowArrowPos.y + uv30.y * 15);
		Line2D.Double lowerTailR = new Line2D.Double(lowArrowPos.x, lowArrowPos.y, lowArrowPos.x + uv30.x * 15, lowArrowPos.y - uv30.y * 15);
				
		Arc2D.Double rotationCircle = new Arc2D.Double(p1.x-size/2, p1.y-size/2, size, size, 0, 360, Arc2D.OPEN);
		
		shapes.add(tailL);
		shapes.add(tailR);
		shapes.add(lowerTailL);
		shapes.add(lowerTailR);
		shapes.add(rotationCircle);
		return shapes;
	}
	
	private ArrayList<Shape> getHoldSymbolShapes() {
		ArrayList<Shape> shapes = new ArrayList<>();
		Arc2D.Double holdCircle = new Arc2D.Double(p1.x - size / 2, p1.y - size / 2, size, size, 0, 360, Arc2D.OPEN);
		shapes.add(holdCircle);
		return shapes;
	}
	
	private ArrayList<Shape> getHoldAndPullSymbolShapes() {
		ArrayList<Shape> shapes = new ArrayList<>();
		Vector2d uv = GeometryUtil.getUnitVector(p1, p2);
		Arc2D.Double holdCircle = new Arc2D.Double(p1.x - size / 2, p1.y - size / 2, 
													size, size, 0, 360, Arc2D.OPEN);
		
		Vector2d lineStart = new Vector2d(p1.x + uv.x * size / 2, p1.y + uv.y  * size / 2);
		Vector2d lineEnd = new Vector2d(lineStart.x + uv.x * 40, lineStart.y + uv.y * 40);
		Line2D.Double holdLine = new Line2D.Double(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y);
		
		double angle35 = Math.toRadians(35);
		double angle35X = uv.x * Math.cos(angle35) - uv.y * Math.sin(angle35);
		double angle35Y = uv.x * Math.sin(angle35) + uv.y * Math.cos(angle35);
		Vector2d uv35 = new Vector2d(angle35X, angle35Y);
		
		double angle145 = Math.toRadians(145);
		double angle145X = uv.x * Math.cos(angle145) - uv.y * Math.sin(angle145);
		double angle145Y = uv.x * Math.sin(angle145) + uv.y * Math.cos(angle145);
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
		Vector2d uv = GeometryUtil.getUnitVector(p1, p2);

		double angle25 = Math.toRadians(25);
		double angle25X = uv.x * Math.cos(angle25) - uv.y * Math.sin(angle25);
		double angle25Y = uv.x * Math.sin(angle25) + uv.y * Math.cos(angle25);
		Vector2d uv25 = new Vector2d(angle25X, angle25Y);
		
		double angle155 = Math.toRadians(155);
		double angle155X = uv.x * Math.cos(angle155) - uv.y * Math.sin(angle155);
		double angle155Y = uv.x * Math.sin(angle155) + uv.y * Math.cos(angle155);
		Vector2d uv155 = new Vector2d(angle155X, angle155Y);
		
		GeneralPath eyeOutline = new GeneralPath();	
		eyeOutline.moveTo(p1.x - uv155.x * 60, p1.y - uv155.y * 60);
		eyeOutline.lineTo(p1.x, p1.y);
		eyeOutline.lineTo(p1.x + uv25.x * 60, p1.y + uv25.y * 60);

		double angle = Math.toDegrees(Math.atan2(-uv.y, uv.x));
		double upper = angle - 25;
		Arc2D.Double eyeball = new Arc2D.Double(p1.x - size / 2, p1.y - size / 2, size, size, upper, 50, Arc2D.OPEN);
		
		Vector2d irisStart = new Vector2d(p1.x + uv.x * 55, p1.y + uv.y * 55);
		double start = angle + 90 + 27;
		Arc2D.Double iris = new Arc2D.Double(irisStart.x - size/8, irisStart.y - size/8, size/4, size/4, start, 124, Arc2D.OPEN);

		shapes.add(eyeOutline);
		shapes.add(iris);
		shapes.add(eyeball);
		return shapes;
	}
	
	private ArrayList<Shape> getXRayCircleSymbolShapes() {
		ArrayList<Shape> shapes = new ArrayList<>();
		Shape circle = new Ellipse2D.Double(p1.x, p1.y, size, size);
		shapes.add(circle);
		return shapes;
	}
	
	private ArrayList<Shape> getClosedSinkSymbolShapes() {
		ArrayList<Shape> shapes = new ArrayList<>();
		Shape circle = new Ellipse2D.Double(p1.x - size / 2, p1.y - size / 2, size, size);
		shapes.add(circle);
		return shapes;
	}
	
	private ArrayList<Shape> getLeaderSymbolShapes() {
		ArrayList<Shape> shapes = new ArrayList<>();
		Line2D.Double leaderLine = new Line2D.Double(p1.x, p1.y, p2.x, p2.y);
		shapes.add(leaderLine);
		return shapes;
	}
	
	private ArrayList<Shape> getRepetitionSymbolShapes() {
		ArrayList<Shape> shapes = new ArrayList<>();
		Line2D.Double leaderLine = new Line2D.Double(p1.x, p1.y, p2.x, p2.y);
		
		shapes.add(leaderLine);
		return shapes;
	}
	
	public void moveBy(double xTrans, double yTrans) {
		p1.x += xTrans;
		p1.y += yTrans;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Vector2d getP1() {
		return p1;
	}

	public void setP1(Vector2d p1) {
		this.p1 = p1;
	}

	public Vector2d getP2() {
		return p2;
	}

	public void setP2(Vector2d p2) {
		this.p2 = p2;
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
		return "OriGeomSymbol [type=" + type + ", p1=" + p1 + ", p2=" + p2 + ", size=" + size + ", text=" + text
				+ ", isReversed=" + isReversed + ", isSelected=" + isSelected + "]";
	}

}
