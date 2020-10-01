package origrammer;

import java.util.ArrayList;

import origrammer.geometry.OriLine;
import origrammer.geometry.OriPolygon;

public class OriSharedLineProxy {

	private OriLineProxy line;
	private OriPolygonProxy poly1;
	private OriPolygonProxy poly2;
	
	public OriSharedLineProxy() {
	}
	
	public OriSharedLineProxy(OriLine l, ArrayList<OriPolygon> polyList) {
		this.line = new OriLineProxy(l);
		this.poly1 = new OriPolygonProxy(polyList.get(0));
		this.poly2 = new OriPolygonProxy(polyList.get(1));
	}
	
	public OriLineProxy getLine() {
		return line;
	}

	public void setLine(OriLineProxy line) {
		this.line = line;
	}

	public OriPolygonProxy getPoly1() {
		return poly1;
	}

	public void setPoly1(OriPolygonProxy poly1) {
		this.poly1 = poly1;
	}

	public OriPolygonProxy getPoly2() {
		return poly2;
	}

	public void setPoly2(OriPolygonProxy poly2) {
		this.poly2 = poly2;
	}
}
