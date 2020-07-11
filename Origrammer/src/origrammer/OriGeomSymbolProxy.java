package origrammer;

import javax.vecmath.Vector2d;

import origrammer.geometry.OriGeomSymbol;

public class OriGeomSymbolProxy {
	
	private Vector2d position;
	private Vector2d direction;
	private double size;
	private int type;

	
	public OriGeomSymbolProxy() {
	}
	
	public OriGeomSymbolProxy (OriGeomSymbol g) {
		this.position = g.getPosition();
		this.direction = g.getDirection();
		this.size = g.getSize();
		this.type = g.getType();
	}
	
	public OriGeomSymbol getSymbol() {
		return new OriGeomSymbol(position, direction, size, type);
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
}
