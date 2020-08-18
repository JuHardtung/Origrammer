package origrammer.geometry;

import javax.vecmath.Vector2d;

public class OriVertexList {
	public int n; //0 means the list is empty; 1 means only one vertex etc.
	public OriVertex head;
	
	
	public OriVertexList() {
		head = null;
		n = 0;
	}
	
	public OriVertexList(OriVertexList list) {
		head = new OriVertex(list.head);

		OriVertex tmp1 = list.head;
		OriVertex tmp2;
		this.head = null;
		do {
			tmp2 = new OriVertex();
			tmp2.p = tmp1.p;
			this.insertBeforeHead(tmp2);
			tmp1 = tmp1.next;
		} while (tmp1 != list.head);
	}
	


	/**
	 * Adds vertex, inserting in between vertices of the closest edge
	 * @param v
	 */
	public void addVertex(double x, double y) {
		
		OriVertex v = new OriVertex(x, y);
		if (head == null) {
			initHead(v);
		} else {
			if (!isInputDuplicate(v)) {
				//gets vertex of 1st vertex of the closest edge to the point
				OriVertex vNear = getEdge(v);
				if (vNear != null) {
					insertBefore(v, vNear.next);
				}
			}
		}
	}
	
	/**
	 * Adds vertex, inserting in between vertices of the closest edge.
	 * Adds an offset to the vertex.
	 * @param v
	 */
	public void addVertex(double x, double y, double offX, double offY) {
		
		OriVertex v = new OriVertex(x, y, offX, offY);
		if (head == null) {
			initHead(v);
		} else {
			if (!isInputDuplicate(v)) {
				//gets vertex of 1st vertex of the closest edge to the point
				OriVertex vNear = getEdge(v);
				if (vNear != null) {
					insertBefore(v, vNear.next);
				}
			}
		}
	}
	
	public void setVertex(OriVertex vOld, double x, double y) {
		OriVertex v = new OriVertex(x, y);

		if (vOld == head) {
			head = v;
		}
		v.next = vOld.next;
		v.prev = vOld.prev;
		vOld.prev.next = v;
		vOld.next.prev = v;
	}
	
	public void initHead(OriVertex h) {
		head = new OriVertex();
		head = h;
		head.next = head.prev = head;
		n = 1;
	}
	
	public void insertBeforeHead(OriVertex v) {
		OriVertex vertex = new OriVertex(v.p);
		if (head == null) {
			initHead(vertex);
		} else {
			insertBefore(vertex, head);
		}
	}
	
	public void insertBefore(OriVertex newV, OriVertex oldV) {
		if (head == null) {
			initHead(newV);
		} else {
			oldV.prev.next = newV;
			newV.prev = oldV.prev;
			newV.next = oldV;
			oldV.prev = newV;
			n++;
		}
	}
	/**
	 * Inserts a new OriVertex into the the list after {@code oldV}. <br>
	 * Updates: <br>
	 *     {@code oldV.next.prev} <br>
	 *     {@code oldV.next} <br>
	 *     {@code newV.prev} <br>
	 *     {@code newV.next}
	 * 
	 * @param newV
	 * @param oldV
	 */
	public void insertLast(OriVertex oldV, OriVertex newV) {
		if (head == null) {
			initHead(newV);
		} else {
			head.prev = newV;
			newV.next = head;
			newV.prev = oldV;
			oldV.next = newV;
			n++;
		}
	}
	
	public void removeVertex(OriVertex v) {
		if (head == head.next) {
			head = null;
		} else if (v == head) {
			head = head.next;
		}
		
		v.prev.next = v.next;
		v.next.prev = v.prev;
		n--;
	}
	
	
	/**
	 * 
	 * @param v
	 * @return
	 */
	public OriVertex getEdge(OriVertex v) {
		OriVertex vNear = null;
		OriVertex vTemp = head;
		double minDist = 0.0;
		double dist = -1.0;
		int k;
		Vector2d p = new Vector2d();
		
		//input query point
		p.x = v.p.x;
		p.y = v.p.y;

		if (vTemp == null) {
			return vNear;
		}

		do {
			dist = GeometryUtil.distEdgePoint(vTemp.p, vTemp.next.p, p);
			if (vNear == null || dist < minDist) {
				minDist = dist;
				vNear = vTemp;
			} else if (dist == minDist && (GeometryUtil.Distance(vTemp.p, p) < GeometryUtil.Distance(vNear.p, p))) {
				vNear = vTemp;
			}
			vTemp = vTemp.next;
		} while (vTemp != head);

		return vNear;
	}

	/**
	 * Checks if the input {@code OriVertex} already exists in within the OriVertexList
	 * @return
	 */
	public boolean isInputDuplicate(OriVertex testV) {
		OriVertex curV = head;
		
		do {
			if (testV.p.equals(curV.p)) {
				return true; //point is duplicate
			}
			curV = curV.next;
			
		} while (curV != head);
		
		return false;
	}
	
	  /* Printing to the console:
	   */
	  public void printVertices() 
	  {
	    OriVertex temp = head;
	    int i = 1;
	    if (head != null) {
	      do {
		temp.printVertex(i);
		System.out.println("isEar: " + temp.isEar);
		temp = temp.next;
		i++;
	      } while ( temp != head );
	    }
	  }

	@Override
	public String toString() {
		return "OriVertexList [n=" + n + ", head=" + head + "]";
	}
	
	
	
}
