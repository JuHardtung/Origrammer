package origrammer.geometry;

import javax.vecmath.Vector2d;

import origrammer.Constants;
import origrammer.Globals;
import origrammer.Origrammer;

public class OriLine {
	final public static int NONE 	 = 0;
	final public static int EDGE 	 = 1;
	final public static int MOUNTAIN = 2;
	final public static int VALLEY 	 = 3;
	final public static int XRAY 	 = 4;
	final public static int CREASE 	 = 5;
	final public static int DIAGONAL = 6;
	
	private boolean isSelected;
	private int type = NONE;
	private OriVertex p0 = new OriVertex();
	private OriVertex p1 = new OriVertex();
	private boolean isStartOffset;
	private boolean isEndOffset;

	
	public OriLine() {
	}
	
	public OriLine(OriLine l) {
		this.type = l.type;
		this.p0 = l.getP0();
		this.p0 = l.getP0();
		this.p1 = l.getP1();
	}
	
	public OriLine(OriVertex p0, OriVertex p1, int type) {
		this.type = type;
		this.p0 = p0;
		this.p1 = p1;
	}

	public OriLine(double x0, double y0, double x1, double y1, int type) {
		this.type = type;
		this.p0.p.set(x0, y0);
		this.p1.p.set(x1, y1);
	}
	
	public OriLine(OriVertex p0, OriVertex p1, int type, boolean isStartTrans, boolean isEndTrans) {
		this.type = type;
		this.p0 = p0;
		this.p1 = p1;
		this.isStartOffset = isStartTrans;
		this.isEndOffset = isEndTrans;
	}
	
	
	public Vector2d getTranslatedP0() {
		Vector2d uv = GeometryUtil.getUnitVector(p0.p, p1.p);
		
		double newX = p0.p.x + uv.x * 25;
		double newY = p0.p.y + uv.y * 25;
		return new Vector2d(newX, newY);
	}
	
	public Vector2d getTranslatedP1() {
		Vector2d uv = GeometryUtil.getUnitVector(p0.p, p1.p);
		
		double newX = p1.p.x - uv.x * 25;
		double newY = p1.p.y - uv.y * 25;
		return new Vector2d(newX, newY);
	}
	
	public void moveBy(double xTrans, double yTrans) {
		p0.p.x += xTrans;
		p0.p.y += yTrans;
		p1.p.x += xTrans;
		p1.p.y += yTrans;
	}
	
	/**
	 * Checks if {@code line} is also an edgeLine.
	 * @param line
	 * @return returns {@code true} if {@code line} is an edgeLine
	 */
	public boolean isEdgeLine() {
		for (OriPolygon curP : Origrammer.diagram.steps.get(Globals.currentStep).polygons) {
			for (OriLine curL : curP.lines) {
				if (curL.getType() == OriLine.EDGE && isSameLine(curL)) {
					System.out.println("is an edge line");
					return true;
				}
			}
		}
//		for (OriLine l : Origrammer.diagram.steps.get(Globals.currentStep).edgeLines) {
//			if (l.getP0().p.equals(p0.p) && l.getP1().p.equals(p1.p) 
//				|| l.getP1().p.equals(p0.p) && l.getP0().p.equals(p1.p)) {
//				System.out.println("is an edge line");
//				return true;
//			}
//		}
		return false;
	}
	
	/**
	 * Checks if the line intersects the {@code OriPolygon p}
	 * @param p
	 * @return {@code true}, if it intersects in at least one point. Otherwise {@code false}.
	 */
	public boolean intersects(OriPolygon p) {
		OriVertex curV = p.vertexList.head;
		boolean cross;
		
		do {
			 cross = GeometryUtil.isIntersecting(p0.p, p1.p, curV.p, curV.next.p);
			if (cross == true) {
				return true;
			} else {
				curV = curV.next;
			}
		} while (!curV.p.equals(p.vertexList.head.p));
		return false;	
	}
	
	/**
	 * Checks if {@code this.line} is the same line as {@code l}
	 * @param l
	 * @return {@code true} if both lines are the same
	 */
	public boolean isSameLine(OriLine l) {
		if (l == null) {
			return false;
		}
		if (GeometryUtil.closeCompareOriVertex(p0, l.getP0()) && GeometryUtil.closeCompareOriVertex(p1, l.getP1())) {	
			return true;
		} else if (GeometryUtil.closeCompareOriVertex(p0, l.getP1()) && GeometryUtil.closeCompareOriVertex(p1, l.getP0())) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if {@code this.line} is the same line as {@code l(v0, v1)}
	 * @param v0
	 * @param v1
	 * @return {@code true} if both lines are the same
	 */
	public boolean isSameLine(OriVertex v0, OriVertex v1) {
		return isSameLine(new OriLine(v0, v1, OriLine.NONE));
	}
	
	
	/**
	 * Checks if the two lines are partially the same. As in, do they share a common part
	 * @param toCheckLine
	 * @param inputLine
	 * @return {@code true} if both lines share one vertex at least
	 */
	public boolean isPartiallySameLine(OriLine inputLine) {
		Vector2d uv0 = GeometryUtil.getUnitVector(p0.p, p1.p);
		Vector2d uv1 = GeometryUtil.getUnitVector(inputLine.getP0().p, inputLine.getP1().p);
		Vector2d uv1Neg = new Vector2d(uv1);
		boolean isSameDirection = false;
		uv1Neg.negate();
		
		if (uv0.epsilonEquals(uv1, Constants.EPSILON)) {
			isSameDirection = true;
		} else if (uv0.epsilonEquals(uv1Neg, Constants.EPSILON)) {
			isSameDirection = true;
		}

		if (isSameDirection) {
			if (GeometryUtil.closeCompareOriVertex(p0, inputLine.getP0())) {
				return isPointOnLine(inputLine.getP1());
				//splitLinesFromVertex(inputLine.getP1()); //check if second vertex of the line is splitting an existing one
				//return true;
			} else if (GeometryUtil.closeCompareOriVertex(p0, inputLine.getP1())) {
				return isPointOnLine(inputLine.getP0());
//				splitLinesFromVertex(inputLine.getP1()); //check if second vertex of the line is splitting an existing one
//				return true;
			} else if (GeometryUtil.closeCompareOriVertex(p1, inputLine.getP0())) {
				return isPointOnLine(inputLine.getP1());
//				splitLinesFromVertex(inputLine.getP0()); //check if second vertex of the line is splitting an existing one
//				return true;
			} else if (GeometryUtil.closeCompareOriVertex(p1, inputLine.getP1())) {
				return isPointOnLine(inputLine.getP0());
//				splitLinesFromVertex(inputLine.getP0()); //check if second vertex of the line is splitting an existing one
//				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Check if {@code Vector2d v} is on the line(lP1, lP2).<br>
	 * <br>
	 * dist(l.p0, vertex) + dist(vertex, l.p1) == dist(l.p0-l.p1)<br>
	 * l.p0 ------ vertex --------------- l.p1 == l.p0 ------------------------- l.p1<br>
	 * @param lP1
	 * @param lP2
	 * @param v
	 * @return {@code true} if the vertex is on the line
	 */
	public boolean isPointOnLine(Vector2d v) {
		double distl0V = GeometryUtil.Distance(p0.p, v);
		double distVl1 = GeometryUtil.Distance(v, p1.p);
		double distl0l1 = GeometryUtil.Distance(p0.p, p1.p);
		
		if (GeometryUtil.closeCompare(distl0V + distVl1, distl0l1, Constants.EPSILON)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Check if {@code OriVertex v} is on the line(lP1, lP2).<br>
	 * <br>
	 * dist(l.p0, vertex) + dist(vertex, l.p1) == dist(l.p0-l.p1)<br>
	 * l.p0 ------ vertex --------------- l.p1 == l.p0 ------------------------- l.p1<br>
	 * @param lP1
	 * @param lP2
	 * @param v
	 * @return {@code true} if the vertex is on the line
	 */
	public boolean isPointOnLine(OriVertex v) {
		return isPointOnLine(v.p);
	}
	
	
	public double getLength() {
		return GeometryUtil.Distance(p0.p, p1.p);
	}

	public OriVertex getP0() {
		return p0;
	}

	public void setP0(OriVertex p0) {
		this.p0 = p0;
	}

	public OriVertex getP1() {
		return p1;
	}

	public void setP1(OriVertex p1) {
		this.p1 = p1;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public boolean isStartOffset() {
		return isStartOffset;
	}

	public void setStartOffset(boolean isStartOffset) {
		this.isStartOffset = isStartOffset;
	}

	public boolean isEndOffset() {
		return isEndOffset;
	}

	public void setEndOffset(boolean isEndOffset) {
		this.isEndOffset = isEndOffset;
	}

	@Override
	public String toString() {
		return "OriLine [isSelected=" + isSelected + ", type=" + type + ", p0=" + p0 + ", p1=" + p1 + ", isStartOffset="
				+ isStartOffset + ", isEndOffset=" + isEndOffset + "]";
	}
	
	public String toStringSmall() {
		return "OriLine [p0=" + p0.p + ", p1=" + p1.p + "]";
	}
}
