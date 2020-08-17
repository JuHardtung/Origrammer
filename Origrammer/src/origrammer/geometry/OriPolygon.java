package origrammer.geometry;

import java.awt.geom.GeneralPath;

import javax.vecmath.Vector2d;

public class OriPolygon {
	
	public OriVertexList vertexList, vertexListCopy;
	public OriDiagonalList diagList;
	private int intCount = 0;			//counts intersections
	private boolean diagDrawn = true;	//diag-s've been drawn after triang?
	private boolean isSelected;
	private int height;
	
	
	
	public OriPolygon (OriVertexList list) {
		this.vertexList = list;
		vertexListCopy = new OriVertexList();
		listCopy();
		diagList = new OriDiagonalList();
		
		intCount = 0;
	}
	
	public OriPolygon(OriPolygon p) {
		this.vertexList = new OriVertexList(p.vertexList);
		vertexListCopy = new OriVertexList();
		this.diagList = p.diagList;
		this.intCount = p.intCount;
		this.height = p.height;
	}
	
	
	public void triangulate() {
		diagList.clearDiagonalList();
		listCopy();
		OriVertex v0, v1, v2, v3, v4;
		OriDiagonal diag;
		int n = vertexListCopy.n;		//number of vertices; shrinks to 3
		boolean earFound = false;		//to prevent infinite loop on improper input
		
		earInit();
		
		//each step of outer loop removes one ear
		while (n > 3) {
			v2 = vertexListCopy.head;
			do {
				if (v2.isEar() ) {
					//ear found; fill variables
					v3 = v2.next;
					v4 = v3.next;
					v1 = v2.prev;
					v0 = v1.prev;
					
					//(v2, v3) is a diagonal
					earFound = true;
					diag = new OriDiagonal(v1, v3);
					//diag.printDiagonal(vertexListCopy.n - n);


					diagList.insertBeforeHead(diag);
					
					//update earity of diagonal endPoints
					v1.setEar(diagonal(v0, v3));
					v3.setEar(diagonal(v1, v4));
					
					//cut off the ear v2
					v1.next = v3;
					v3.prev = v1;
					vertexListCopy.head = v3; //in case the head was v2
					n--;
					break; //out of inner loop; resume outer loop
				} //end if ear found
				v2 = v2.next;
			} while (v2 != vertexListCopy.head);
			if (!earFound) {
				System.out.println("Polygon is nonsimple: cannot triangulate");
				printPoly();
				break;
			} else {
				earFound = false;
				diagDrawn = false;
			} //end outer while loop
		}
	}
	
	public void earInit() {
		OriVertex v0, v1, v2; //three consecutive vertices
		
		//initialize v1-> ear for all vertices
		v1 = vertexListCopy.head;
		
		do {
			v2 = v1.next;
			v0 = v1.prev;
			v1.isEar = diagonal(v0, v2);
			v1 = v1.next;
		} while (v1 != vertexListCopy.head);
	}
	
	public boolean diagonal(OriVertex a, OriVertex b) {
		return inCone(a, b) && inCone(b, a) && diagonalLie(a, b);
	}
	
	public boolean diagonalLie(OriVertex a, OriVertex b) {
		OriVertex c, c1;
		
		//for each edge(c, c1) of p
		c = vertexListCopy.head;
		do {
			c1 = c.next;
			//skip edges incident to a or b
			if ((c != a) && (c1 != a) 
				&& (c != b) && (c1 != b) 
				&& GeometryUtil.intersect(a.p, b.p, c.p, c1.p)) {
				return false;
			}
			c = c.next;
		} while (c != vertexListCopy.head);
		return true;
	}
	
	/**
	 * Returns {@code true} if and only of the diagonal(a, b) is strictly 
	 * internal to the polygon in the neighborhood of the a endpoint
	 * @param a
	 * @param b
	 * @return
	 */
	public boolean inCone(OriVertex a, OriVertex b) {
		OriVertex a0, a1; //a0, a, a1 are consecutive vertices
		
		a1 = a.next;
		a0 = a.prev;
		
		//if a is a convex vertex
		if (GeometryUtil.isLeftOn(a.p, a1.p, a0.p)) {
			return GeometryUtil.isStrictLeft(a.p, b.p, a0.p) 
					&& GeometryUtil.isStrictLeft(b.p, a.p, a1.p);
		} else {
			//else a is reflex
			return !(GeometryUtil.isLeftOn(a.p, b.p, a1.p) 
					&& GeometryUtil.isLeftOn(b.p, a.p, a0.p));
		}
	}
	

	public GeneralPath getShapesForDrawing() {
		GeneralPath tmpPath = new GeneralPath();
		tmpPath = new GeneralPath();
		OriVertex curV = vertexList.head;
		tmpPath.moveTo(curV.p.x, curV.p.y);
		curV = curV.next;

		while (curV != vertexList.head) {
			tmpPath.lineTo(curV.p.x, curV.p.y);
			curV = curV.next;
		}
		tmpPath.closePath();
		return tmpPath;
	}
	
	
	public void listCopy() {
		OriVertex tmp1 = vertexList.head;
		OriVertex tmp2;
		vertexListCopy.head = null;
		do {
			tmp2 = new OriVertex();
			tmp2.p = tmp1.p;
			vertexListCopy.insertBeforeHead(tmp2);
			tmp1 = tmp1.next;
		} while (tmp1 != vertexList.head);
	}
	
	
	public boolean isInside(Vector2d v) {
		GeneralPath tmpPath = new GeneralPath();
		OriVertex curV = vertexList.head;
		tmpPath.moveTo(curV.p.x, curV.p.y);
		curV = curV.next;

		while (curV != vertexList.head) {
			tmpPath.lineTo(curV.p.x, curV.p.y);
			curV = curV.next;
		}
		tmpPath.closePath();

		if (tmpPath.contains(v.x, v.y)) {
			return true;
		} else {
			return false;
		}

	}
	
	/**
	 * Checks if the {@code OriVertex v} is on a edge line of the OriPolygon
	 * @param v
	 * @return {@code true} if {@code v} is on an edge line of the {@code OriPolygon}
	 */
	public boolean contains(OriVertex v) {
		OriVertex curV = null;
		boolean contains;
		
		if (vertexList.head != null) {
			curV = vertexList.head;
		} else {
			return false;
		}
		 
		do {
			contains = GeometryUtil.isPointOnLine(curV, curV.next, v);
			if (contains == true) {
				return true;
			} else {
				curV = curV.next;
			}
		} while (!curV.p.equals(vertexList.head.p));
		return false;
	}
	
	public int getHeight() {
		return height;
	}


	public void setHeight(int height) {
		this.height = height;
	}

	public boolean isSelected() {
		return isSelected;
	}


	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}


	public void printPoly() {
		OriVertex v = vertexList.head;
		int i=0;
		if (v == null) {
			System.out.println("Polygon is empty");
		} else {
			do {
				System.out.println("Vertex " + i + " (" + v.p.x + ", " + v.p.y + ")");
				i++;
				v = v.next;
			} while (v != vertexList.head);

		}
	}
	  
	  public void printDiagonalLines() {
		  diagList.printDiagonals();
	  }

	@Override
	public String toString() {
		return "OriPolygon [vertexList=" + vertexList + ", diagList=" + diagList
				+ ", intCount=" + intCount + ", height=" + height + "]";
	}
	  
}
