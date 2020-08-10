package origrammer;

import javax.vecmath.Vector2d;

import origrammer.geometry.OriArrow;

public class OriArrowProxy {
	
	private Vector2d p0;
	private Vector2d p1;
	private int type;
	private boolean isMirrored;
	private boolean isUnfold;
	
	public OriArrowProxy() {
	}
	
	public OriArrowProxy(OriArrow a) {
		this.p0 = a.getP0();
		this.p1 = a.getP1();
		this.type = a.getType();
		this.isMirrored = a.isMirrored();
		this.isUnfold = a.isUnfold();                                                     		
	}
	
	public OriArrow getArrow() {
		return new OriArrow(p0, p1, type, isMirrored, isUnfold);
	}

	public Vector2d getP0() {
		return p0;
	}

	public void setP0(Vector2d p0) {
		this.p0 = p0;
	}

	public Vector2d getP1() {
		return p1;
	}

	public void setP1(Vector2d p1) {
		this.p1 = p1;
	}

	public boolean isMirrored() {
		return isMirrored;
	}

	public void setMirrored(boolean isMirrored) {
		this.isMirrored = isMirrored;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isUnfold() {
		return isUnfold;
	}

	public void setUnfold(boolean isUnfold) {
		this.isUnfold = isUnfold;
	}

	@Override
	public String toString() {
		return "OriArrowProxy [p0=" + p0 + ", p1=" + p1 + ", type=" + type + ", isMirrored=" + isMirrored
				+ ", isUnfold=" + isUnfold + "]";
	}
	
}
