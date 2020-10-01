package origrammer;

import javax.vecmath.Vector2d;

import origrammer.geometry.OriDiagonal;
import origrammer.geometry.OriVertex;

public class OriDiagonalProxy {

	private Vector2d v1;
	private Vector2d v2;
	
	public OriDiagonalProxy() {
	}
	
	public OriDiagonalProxy(OriDiagonal d) {
		this.v1 = d.v1.p;
		this.v2 = d.v2.p;
	}
	
	
	public OriDiagonal getDiagonal() {
		return new OriDiagonal(new OriVertex(v1), new OriVertex(v2));
	}

	public Vector2d getV1() {
		return v1;
	}

	public void setV1(Vector2d v1) {
		this.v1 = v1;
	}

	public Vector2d getV2() {
		return v2;
	}

	public void setV2(Vector2d v2) {
		this.v2 = v2;
	}
}
