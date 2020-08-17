package origrammer.geometry;

import javax.vecmath.Vector2d;

public class OriVertex {
	
	public OriVertex prev, next;
	public Vector2d p;
	private Vector2d offset = new Vector2d();
	public boolean isEar = false;
	int vnum;
	boolean onHull;
	boolean mark;
	private boolean isSelected = false;

	
	public OriVertex() {
		prev = next = null;
		p = new Vector2d();
		vnum = 0;
		onHull = false;
		mark = false;
		
	}
	
	public OriVertex(OriVertex v) {
		this.p = v.p;
		this.isSelected = v.isSelected;
	}
	
	public OriVertex(Vector2d p) {
		this.p = p;
	}
	
	public OriVertex(double x, double y) {
		p = new Vector2d();
		p.x = x;
		p.y = y;
		prev = next = null;
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
	
//	public OriVertex getPrev() {
//		return prev;
//	}
//
//	public void setPrev(OriVertex prev) {
//		this.prev = prev;
//	}
//
//	public OriVertex getNext() {
//		return next;
//	}
//
//	public void setNext(OriVertex next) {
//		this.next = next;
//	}

	public boolean isEar() {
		return isEar;
	}

	public void setEar(boolean isEar) {
		this.isEar = isEar;
	}

	public void printVertex(int index) {
		System.out.println("V" + index + " = ");
		System.out.println(" (" + p.x + "," + p.y + ")");
	}

	@Override
	public String toString() {
		return "OriVertex [p=" + p + ", offset=" + offset + ", isEar=" + isEar
				+ ", isSelected=" + isSelected + "]";
	}
}
