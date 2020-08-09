package origrammer;

import javax.vecmath.Vector2d;

import origrammer.geometry.OriLine;
import origrammer.geometry.OriVertex;

public class OriLineProxy {
	
	private OriVertex p0;
	private OriVertex p1;
	private int type;
	private boolean isStartTransl;
	private boolean isEndTransl;
	
	
	public OriLineProxy() {
	}
	
	public OriLineProxy(OriLine l) {
		this.p0 = l.getP0();
		this.p1 = l.getP1();
		this.type = l.getType();
		this.isStartTransl = l.isStartOffset();
		this.isEndTransl = l.isEndOffset();
	}
	
	public OriLine getLine() {
		return new OriLine(p0, p1, type, isStartTransl, isEndTransl);
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

	public boolean isStartTransl() {
		return isStartTransl;
	}

	public void setStartTransl(boolean isStartTransl) {
		this.isStartTransl = isStartTransl;
	}

	public boolean isEndTransl() {
		return isEndTransl;
	}

	public void setEndTransl(boolean isEndTransl) {
		this.isEndTransl = isEndTransl;
	}

	@Override
	public String toString() {
		return "OriLineProxy [p0=" + p0 + ", p1=" + p1 + ", type=" + type + "]";
	}

}
