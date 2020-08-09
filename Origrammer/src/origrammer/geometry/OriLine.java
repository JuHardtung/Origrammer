package origrammer.geometry;

import javax.vecmath.Vector2d;

public class OriLine {
	final public static int TYPE_NONE = 0;
	final public static int TYPE_EDGE = 1;
	final public static int TYPE_MOUNTAIN = 2;
	final public static int TYPE_VALLEY = 3;
	final public static int TYPE_XRAY = 4;
	final public static int TYPE_CREASE = 5;
	
	private boolean isSelected;
	private int type = TYPE_NONE;
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
		return "OriLine [p0=" + p0 + ", p1=" + p1 + "]";
	}
}
