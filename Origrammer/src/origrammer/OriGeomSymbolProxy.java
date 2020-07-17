package origrammer;

import javax.vecmath.Vector2d;

import origrammer.geometry.OriGeomSymbol;

public class OriGeomSymbolProxy {
	
	private Vector2d p1;
	private Vector2d p2;
	private double size;
	private int type;

	
	public OriGeomSymbolProxy() {
	}
	
	public OriGeomSymbolProxy (OriGeomSymbol g) {
		this.p1 = g.getP1();
		this.p2 = g.getP2();
		this.size = g.getSize();
		this.type = g.getType();
	}
	
	public OriGeomSymbol getSymbol() {
		return new OriGeomSymbol(p1, p2, size, type);
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
	
}
