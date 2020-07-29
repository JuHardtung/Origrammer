package origrammer;

import javax.vecmath.Vector2d;

import origrammer.geometry.OriGeomSymbol;

public class OriGeomSymbolProxy {
	
	private Vector2d p1;
	private Vector2d p2;
	private double size;
	private int type;
	private String text;
	private boolean isReversed;

	
	public OriGeomSymbolProxy() {
	}
	
	public OriGeomSymbolProxy (OriGeomSymbol g) {
		this.p1 = g.getP1();
		this.p2 = g.getP2();
		this.size = g.getSize();
		this.type = g.getType();
		this.text = g.getText();
		this.isReversed = g.isReversed();
	}
	
	public OriGeomSymbol getSymbol() {
		return new OriGeomSymbol(p1, p2, size, type, text, isReversed);
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
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
	
}
