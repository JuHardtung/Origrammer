package origrammer;

import java.util.ArrayList;

import javax.vecmath.Vector2d;

import origrammer.geometry.OriDiagonal;
import origrammer.geometry.OriDiagonalList;
import origrammer.geometry.OriLine;
import origrammer.geometry.OriPolygon;
import origrammer.geometry.OriVertex;
import origrammer.geometry.OriVertexList;

public class OriPolygonProxy {
	
	public Vector2d[] vertexList;
	public OriDiagonalProxy[] diagList;
	private int height;
	public OriLineProxy[] lines;
	
	
	public OriPolygonProxy() {
	}
	
	public OriPolygonProxy(OriPolygon p) {
		OriVertex curV = p.vertexList.head;
		this.vertexList = new Vector2d[p.vertexList.n];
		int vCount = 0;
		do {
			this.vertexList[vCount] = curV.p;
			vCount++;
			curV = curV.next;
		} while (curV != p.vertexList.head);
		
		if (p.diagList.n > 0) {
			OriDiagonal curD = p.diagList.head;
			this.diagList = new OriDiagonalProxy[p.diagList.n];
			int dCount = 0;
			do {
				this.diagList[dCount] = new OriDiagonalProxy(curD);
				dCount++;
				curD = curD.next;
			} while (dCount < p.diagList.n);
		}

		
		int lineCount = p.lines.size();
		this.lines = new OriLineProxy[lineCount];
		for (int i = 0; i < lineCount; i++) {
			this.lines[i] = new OriLineProxy(p.lines.get(i));
		}
		
		this.height = p.getHeight();
	}
	
	public OriPolygon getPolygon() {
		OriVertexList vList = new OriVertexList();
		vList.initHead(new OriVertex(vertexList[0]));
		for (int i=1; i<vertexList.length; i++) {
			vList.insertBeforeHead(new OriVertex(vertexList[i]));
		}
		
		OriDiagonalList dList = new OriDiagonalList();
		dList.initHead(new OriDiagonal(new OriVertex(diagList[0].getV1()), new OriVertex(diagList[0].getV2())));
		for (int i=1; i<diagList.length; i++) {
			dList.insertBeforeHead(new OriDiagonal(new OriVertex(diagList[i].getV1()), new OriVertex(diagList[i].getV2())));
		}
		
		ArrayList<OriLine> lList = new ArrayList<OriLine>();
		for (int i=0; i<lines.length; i++) {
			OriLineProxy curL = lines[i];
			lList.add(new OriLine(new OriVertex(curL.getP0()), new OriVertex(curL.getP1()), curL.getType(), curL.isStartTransl(), curL.isEndTransl()));
		}
		
		return new OriPolygon(vList, dList, lList, height);
	}

	public Vector2d[] getVertexList() {
		return vertexList;
	}

	public void setVertexList(Vector2d[] vertexList) {
		this.vertexList = vertexList;
	}

	public OriDiagonalProxy[] getDiagList() {
		return diagList;
	}

	public void setDiagList(OriDiagonalProxy[] diagList) {
		this.diagList = diagList;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public OriLineProxy[] getLines() {
		return lines;
	}

	public void setLines(OriLineProxy[] lines) {
		this.lines = lines;
	}
	

}
