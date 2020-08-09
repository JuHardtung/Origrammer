package origrammer.geometry;

import javax.vecmath.Vector2d;

public class OriVertex {
	
	public Vector2d p = new Vector2d();
	private Vector2d offset = new Vector2d();
	private boolean isSelected = false;

	
	public OriVertex() {
	}
	
	public OriVertex(OriVertex v) {
		this.p = v.p;
		this.isSelected = v.isSelected;
	}
	
	public OriVertex(Vector2d p) {
		this.p = p;
	}
	
	public OriVertex(double x, double y) {
		p.set(x, y);
	}

	public Vector2d getP() {
		return p;
	}

	public void setP(Vector2d p) {
		this.p = p;
	}
	
	public void setP(double x, double y) {
		this.p.x = x;
		this.p.y = y;
	}

	public Vector2d getOffset() {
		return offset;
	}

	public void setOffset(Vector2d offset) {
		this.offset = offset;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	@Override
	public String toString() {
		return "OriVertex [p=" + p + "]";
	}
}
