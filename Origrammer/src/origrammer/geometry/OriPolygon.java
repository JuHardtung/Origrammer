package origrammer.geometry;

import java.awt.geom.GeneralPath;
import java.util.ArrayList;

import javax.vecmath.Vector2d;

import origrammer.Constants;
import origrammer.Globals;
import origrammer.Origrammer;

public class OriPolygon {
	
	public OriVertexList vertexList, vertexListCopy;
	public OriDiagonalList diagList;
	private boolean isSelected;
	private int height;
	public ArrayList<OriLine> lines = new ArrayList<OriLine>();
	
	public OriPolygon() {
		height = 0;
	}
	
	public OriPolygon (OriVertexList list) {
		this.vertexList = list;
		vertexListCopy = new OriVertexList();
		if (vertexList.n > 0) {
			listCopy();
		}
		
		diagList = new OriDiagonalList();
		height = 0;
	}
	
	public OriPolygon(OriVertexList vList, OriDiagonalList dList, ArrayList<OriLine> lList, int height) {
		this.vertexList = vList;
		this.diagList = dList;
		this.lines = lList;
		this.height = height;
		vertexListCopy = new OriVertexList();

	}
	
	public OriPolygon(OriPolygon p) {
		this.vertexList = new OriVertexList(p.vertexList);
		vertexListCopy = new OriVertexList();
		lines = new ArrayList<OriLine>();
		for (OriLine l : p.lines) {
			OriLine newL = new OriLine(l);
			lines.add(newL);
		}
		
		this.diagList = new OriDiagonalList(p.diagList);
		this.height = p.height;
	}

	/**
	 * Looks for shared lines that have the old Vertex and changes it to the new one
	 * @param oldV
	 * @param newV
	 */
	public void updateSharedLines(OriVertex oldV, OriVertex newV) {
		for (OriLine curL : Origrammer.diagram.steps.get(Globals.currentStep).sharedLines.keySet()) {
			if (curL.getP0().p.epsilonEquals(oldV.p, Constants.EPSILON)) {
				curL.setP0(newV);
			}
			if (curL.getP1().p.epsilonEquals(oldV.p, Constants.EPSILON)) {
				curL.setP1(newV);
			}
		}
	}
	

	/**
	 * Folds the polygon along {@code foldingLine}. Skips vertices that are <br>
	 * on the {@code foldingLine} and updates the remaining ones. <br>
	 * <br>
	 * After folding, all lines of the polygon that have changed vertices need to be updated. <br>
	 * All {@code sharedLines} that have updated vertices are getting updated as well.
	 * @param foldingLine
	 */
	public void makeFold(OriLine foldingLine) {
		OriVertex curV = vertexList.head;
		
		do { //only fold vertices that are strictly on the left side of the foldingLine
			if (GeometryUtil.isStrictLeft(curV.p, foldingLine.getP0().p, foldingLine.getP1().p)) {
				
				//get closest point to the folding line
				Vector2d tmpClosestCross = GeometryUtil.getClosestPointOnLine(curV.p, foldingLine);
				
				//get dist between vertex and the crossPoint
				double dist = GeometryUtil.Distance(curV.p, tmpClosestCross);
				
				//calc new vertex position with the dist and unitVector
				Vector2d tmpUV = GeometryUtil.getUnitVector(curV.p, tmpClosestCross);
				double newX = curV.p.x + tmpUV.x * dist*2;
				double newY = curV.p.y + tmpUV.y * dist*2;
				OriVertex oldV = curV;
				OriVertex newV = new OriVertex(newX, newY);
				
				//set vertex to the new position after folding
				vertexList.setVertex(oldV, newX, newY);

				curV = curV.next;

				//update all existing lines of the polygon that share the changed OriVertex
				for (OriLine tmpL : lines) {
					
					if (tmpL.getP0().p.epsilonEquals(oldV.p, Constants.EPSILON)) {
						tmpL.setP0(newV);
						if (tmpL.getType() == OriLine.NONE) {
							//update all sharedLines with the vertex (if a shared line is folded)
							updateSharedLines(oldV, newV);
						}
					}
					if (tmpL.getP1().p.epsilonEquals(oldV.p, Constants.EPSILON)) {
						tmpL.setP1(newV);
						if (tmpL.getType() == OriLine.NONE) {
							//update all sharedLines wioth the vertex (if a shared line is folded)
							updateSharedLines(oldV, newV);
						}
					}
				}
			} else {
				curV = curV.next;
			}
		} while (curV != vertexList.head);
	}
	
	/**
	 * Checks if the vertices of the polygon are sorted in a clockwise order.
	 * @return {@code true} if vertices are sorted in clockwise order <br>
	 * 			{@code false} if vertices are sorted in counterclockwise order
	 */
	public boolean isClockwise() {
		OriVertex curV = vertexList.head;
		OriVertex nextV = curV.next;
		double sum = 0;

		do {
			sum +=  (nextV.p.x-curV.p.x) * (nextV.p.y + curV.p.y) ;
			
			curV = nextV;
			nextV = curV.next;
		} while (curV != vertexList.head);

		if (sum < 0) {
			return true; //clockwise
		}
		return false; //counterclockwise
	}
	
	
	public void triangulate() {
		diagList.clearDiagonalList();
		listCopy();
		OriVertex v0, v1, v2, v3, v4;
		OriDiagonal diag;
		int n = vertexListCopy.n;		//number of vertices; shrinks to 3
		boolean earFound = false;		//to prevent infinite loop on improper input
		boolean isClockwise = isClockwise(); //check how the vertices are ordered
		earInit(); //mark all vertices that could be ears
		
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

					diagList.insertBeforeHead(diag);
					
					//update earity of diagonal endPoints
					v1.setEar(isDiagonalCounterClock(v0, v3));
					v3.setEar(isDiagonalCounterClock(v1, v4));
					
					//cut off the ear v2
					v1.next = v3;
					v3.prev = v1;
					vertexListCopy.head = v3; //in case the head was v2
					n--;
					break; //out of inner loop; resume outer loop
				} //end if ear found
				
				//go other way around if vertices are sorted clockwise
				if (isClockwise) {
					v0 = v2.prev;
				} else {
					v2 = v2.next;
				}
				
			} while (v2 != vertexListCopy.head);
			if (!earFound) {
				System.out.println("Polygon is nonsimple: cannot triangulate");
				printPoly();
				break;
			} else {
				earFound = false;
			} //end outer while loop
		}
	}
	
	/**
	 * Marks all vertices that are potential ears
	 */
	public void earInit() {
		OriVertex v0, v1, v2; //three consecutive vertices
		boolean isClockwise = isClockwise();
		//initialize v1-> ear for all vertices
		v1 = vertexListCopy.head;
		
		do {
			v2 = v1.next;
			v0 = v1.prev;
			
			if (isClockwise) {
				v1.isEar = isDiagonalClockwise(v0, v2);
			} else {
				v1.isEar = isDiagonalCounterClock(v0, v2);
			}
			
			v1 = v1.next;
		} while (v1 != vertexListCopy.head);
	}
	
	public boolean isDiagonalCounterClock(OriVertex a, OriVertex b) {
		return inCone(a, b) && inCone(b, a) && diagonalLie(a, b);
	}
	
	
	public boolean isDiagonalClockwise(OriVertex a, OriVertex b) {
		return inConeRight(a, b) && inConeRight(b, a) && diagonalLie(a, b);
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
	 * Adds a vertex to the {@code OriPolygon}. <br>
	 * Checks where in the vertexList the inputVertex should be added. <br>
	 * Adds a corresponding {@code OriLine} to the lines-list.
	 * @param x
	 * @param y
	 */
	public void addVertex(double x, double y) {
		if (vertexList.n > 0) {
			OriVertex inputV = new OriVertex(x, y);
			OriVertex closestV = vertexList.getEdge(inputV);
			OriLine tmpL = new OriLine(closestV, closestV.next, OriLine.NONE);
			OriVertex curV = vertexList.head;
			
			//skip the vertex if it is already part of the polygon
			do {
				if (curV.p.epsilonEquals(inputV.p, Constants.EPSILON)) {
					return;
				}
				curV = curV.next;
			} while (curV != vertexList.head);
			
			vertexList.addVertex(x, y);
			
			//update lines of the polygon
			if (tmpL.isPointOnLine(inputV)) {
				splitLine(tmpL.getP0(), tmpL.getP1(), inputV);
			} else {
				addLine(new OriLine(closestV, closestV.next, OriLine.EDGE));
			}
		} else {
			vertexList.addVertex(x, y);
		}

	}

	public void splitLine(OriVertex v0, OriVertex v1, OriVertex inputV) {
		for (int i=0; i<lines.size(); i++) {
			OriLine curL = lines.get(i);
			if (curL.isSameLine(v0, v1)) {
				lines.remove(curL);
				lines.add(new OriLine(v0, inputV, curL.getType()));
				lines.add(new OriLine(inputV, v1, curL.getType()));

			}
		}
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
	
	/**
	 * Returns {@code true} if and only of the diagonal(a, b) is strictly 
	 * internal to the polygon in the neighborhood of the a endpoint
	 * @param a
	 * @param b
	 * @return
	 */
	public boolean inConeRight(OriVertex a, OriVertex b) {
		OriVertex a0, a1; //a0, a, a1 are consecutive vertices
		
		a1 = a.next;
		a0 = a.prev;
		
		//if a is a convex vertex
		if (GeometryUtil.isRightOn(a.p, a1.p, a0.p)) {
			return GeometryUtil.isStrictRight(a.p, b.p, a0.p) 
					&& GeometryUtil.isStrictRight(b.p, a.p, a1.p);
		} else {
			//else a is reflex
			return !(GeometryUtil.isRightOn(a.p, b.p, a1.p) 
					&& GeometryUtil.isRightOn(b.p, a.p, a0.p));
		}
	}
	
	public boolean isPolygonAbove(OriPolygon poly) {
		if (height < poly.height) {
			return true;
		} else {
			return false;
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
	 * Checks if the {@code OriVertex v} is on a line of the OriPolygon
	 * @param v
	 * @return {@code true} if {@code v} is on a line of the {@code OriPolygon}
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
			OriLine tmpL = new OriLine(curV, curV.next, OriLine.NONE);
			contains = tmpL.isPointOnLine(v);
			if (contains == true) {
				return true;
			} else {
				curV = curV.next;
			}
		} while (!curV.p.equals(vertexList.head.p));
		return false;
	}

	/**
	 * Updates all the lines of a polygon, according to the {@code vertexList}. <br>
	 * Preserves sharedLines.
	 * @param sharedL
	 */
	public void updateLines(OriLine sharedL) {
		//remove all lines that are not shared with other polygons
		removeAllNonSharedLines(); 

		//add all the edge lines back, but skip those lines that are already being shared by other polygons
		OriVertex curV = vertexList.head;
		boolean isDoubleLine;
		do {
			isDoubleLine = false;
			OriLine tmpL = new OriLine(curV, curV.next, OriLine.EDGE);
			if (lines.size() == 0) {
				lines.add(tmpL);
			} else {
				for (int i=0; i<lines.size(); i++) {
					OriLine curL = lines.get(i);
					if (curL.isSameLine(tmpL)) {
						isDoubleLine = true;
					} else if (curL.isPartiallySameLine(tmpL)) {
						System.out.println("Updating lines adds partially same lines");
					}
				}
				if (!isDoubleLine) {
					lines.add(tmpL);
				}
			}
			curV = curV.next;
		} while (curV != vertexList.head);
		
		for (int i=0; i<lines.size(); i++) {
			OriLine l = lines.get(i);
			if (l.isSameLine(sharedL)) {
				l.setType(OriLine.NONE);
			}//TODO: UPDATE THE LINES CORRECTLY; NOT ALL SHAREDLINES ARE GETTING SIGNED AS THAT
			for (OriLine curL : Origrammer.diagram.steps.get(Globals.currentStep).sharedLines.keySet()) {
				if (curL.isSameLine(l)) {
					l.setType(OriLine.NONE);
				} else if (curL.isPartiallySameLine(l)) {
					System.out.println("partially the same, update sharedLines");
					
				}
			}
		}
	}
	
	public void removeAllNonSharedLines() {
		for (int i=0; i<lines.size(); i++) {
			OriLine curL = lines.get(i);
			if (curL.getType() != OriLine.NONE) {
				lines.remove(curL);
				i = -1;
			}
		}
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
		for (OriLine l : lines) {
			l.setSelected(isSelected);
		}
	}
	
	public void addLine(OriLine inputL) {
		boolean lineExists = false;
		if (lines.size() == 0) {
			lines.add(inputL);
		} else {
			for (int i=0; i<lines.size(); i++) {
				OriLine curL = lines.get(i);
				if (curL.isSameLine(inputL)) {
					lineExists = true;
				}
			}
			if (!lineExists) {
				lines.add(inputL);
			}
			
		}
	}
	
	/**
	 * Checks if {@code OriLine l} is part of the polygon.
	 * @param l
	 * @return
	 */
	public boolean isLinePartOfPolygon(OriLine l) {
		for (OriLine curL : lines) {
			if (curL.isSameLine(l)) {
				return true;
			} else if (curL.isPartiallySameLine(l) || l.isPartiallySameLine(curL)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if there are vertices that are in the reversed arrowUV direction
	 * (which means they are folded and their positions have to be updated)
	 * @param l
	 * @param arrowUV
	 * @return
	 */
	public boolean isBeingFolded(OriLine l, Vector2d arrowUV) {
		OriVertex curV = vertexList.head;
		do {
			Vector2d closestPonFoldLine = GeometryUtil.getClosestPointOnLine(curV.p, l);
			if (closestPonFoldLine == null) {
				return false;
			}
			Vector2d tmpUV = GeometryUtil.getUnitVector(closestPonFoldLine, curV.p);

			if (tmpUV.epsilonEquals(arrowUV, Constants.EPSILON)) {
				return true;
			}
			curV = curV.next;
		} while (curV != vertexList.head);
		return false;
	}


	public ArrayList<OriLine> getLines() {
		return lines;
	}

	public void setLines(ArrayList<OriLine> lines) {
		
		this.lines = new ArrayList<OriLine>();
		lines.addAll(lines);
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
				+ ", height=" + height + "]";
	}
	  
}
