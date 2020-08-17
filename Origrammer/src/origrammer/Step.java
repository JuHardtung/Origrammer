package origrammer;

import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;

import javax.vecmath.Vector2d;

import origrammer.geometry.GeometryUtil;
import origrammer.geometry.OriArrow;
import origrammer.geometry.OriDiagonal;
import origrammer.geometry.OriEqualAnglSymbol;
import origrammer.geometry.OriEqualDistSymbol;
import origrammer.geometry.OriFace;
import origrammer.geometry.OriGeomSymbol;
import origrammer.geometry.OriLine;
import origrammer.geometry.OriPleatCrimpSymbol;
import origrammer.geometry.OriPolygon;
import origrammer.geometry.OriVertex;
import origrammer.geometry.OriVertexList;

class PointComparatorX implements Comparator<Object> {
	@Override
	public int compare(Object v1, Object v2) {
		if (((Vector2d) v1).x > ((Vector2d) v2).x) {
			return 1;
		} else if (((Vector2d) v1).x < ((Vector2d) v2).x) {
			return -1;
		} else {
			return 0;
		}
	}
}

class PointComparatorY implements Comparator<Object> {
	@Override
	public int compare(Object v1, Object v2) {
		if (((Vector2d) v1).y > ((Vector2d) v2).y) {
			return 1;
		} else if (((Vector2d) v1).y < ((Vector2d) v2).y) {
			return -1;
		} else {
			return 0;
		}
	}
}

class UndoInfo {
	public ArrayList<OriLine> lines = new ArrayList<>();
	public ArrayList<OriVertex> vertices = new ArrayList<>();
	public ArrayList<OriArrow> arrows = new ArrayList<>();
	public ArrayList<OriFace> filledFaces = new ArrayList<>();
	public ArrayList<OriGeomSymbol> geomSymbols = new ArrayList<>();
	public ArrayList<OriEqualDistSymbol> equalDistSymbols = new ArrayList<>();
	public ArrayList<OriEqualAnglSymbol> equalAnglSymbols = new ArrayList<>();
	public ArrayList<OriPleatCrimpSymbol> pleatCrimpSymbols = new ArrayList<>();
}


public class Step {

	public ArrayList<OriLine> lines = new ArrayList<>();
	public ArrayList<OriVertex> vertices = new ArrayList<>();
	public ArrayList<OriArrow> arrows = new ArrayList<>();
	public ArrayList<OriFace> filledFaces = new ArrayList<>();
	public ArrayList<OriGeomSymbol> geomSymbols = new ArrayList<>();
	public ArrayList<OriEqualDistSymbol> equalDistSymbols = new ArrayList<>();
	public ArrayList<OriEqualAnglSymbol> equalAnglSymbols = new ArrayList<>();
	public ArrayList<OriPleatCrimpSymbol> pleatCrimpSymbols = new ArrayList<>();
	
	public ArrayList<OriPolygon> polygons = new ArrayList<>();
	
	//OriVertexList vertexList = new OriVertexList();
	//OriPolygon paperPolygon;

	private Stack<UndoInfo> undoStack = new Stack<UndoInfo>();
	public CopiedObjects copiedObjects = new CopiedObjects();
	public String stepDescription;

	public static final double POINT_EPS = 1.0e-6;

	public double size = Constants.DEFAULT_PAPER_SIZE;


	public Step() {
		if (Globals.newStepOptions == Constants.NewStepOptions.PASTE_DEFAULT_PAPER) {
			initFirstStep();
		}
	}

	public void initFirstStep() {
		
		OriVertexList vertexList = getEdgeLines();

		OriPolygon paperPolygon = new OriPolygon(vertexList);
		polygons.add(paperPolygon);

		addLinesFromVertices();
		
	}
	
	public void addLinesFromVertices() {
		clearLinesExceptCreases();

		addEdgeLinesFromVertices();
		
		addTriangulationDiagonals();

		removeDuplicateLines();
	}

	private void addEdgeLinesFromVertices() {
		for (OriPolygon p : polygons) {
			OriVertex curP0 = p.vertexList.head;
			OriVertex curP1;
			boolean duplicateLine = false;
			
			for (int i=0; i<p.vertexList.n; i++) {
				curP1 = curP0.next;
				for (OriLine l : lines) {
					if (l.isSameLine(curP0, curP1)) {
						duplicateLine = true;
						break;
					}
				}
				
				if (!duplicateLine) {
					lines.add(new OriLine(curP0, curP1, OriLine.TYPE_EDGE));
				}
				curP0 = curP1;
				duplicateLine = false;
			}
		}
	}
	
	/**
	 * Goes through all polygons, triangulates them and adds the diagonals to the {@code lines} 
	 * if they are not duplicate or overlapping another existing line.
	 */
	private void addTriangulationDiagonals() {

		for (OriPolygon p : polygons) {
			p.triangulate();
			OriDiagonal curDia = p.diagList.head;

			for (int i=0; i<p.diagList.n; i++) {
				lines.add(new OriLine(curDia.v1, curDia.v2, OriLine.TYPE_DIAGONAL));
				curDia = curDia.next;
			}
		}
	}
	
	/**
	 * Splits an existing polygon into two smaller ones. <br>
	 * The polygon gets split at the vertices of {@code inputLine}
	 * @param inputLine
	 * @param polygon
	 */
	private ArrayList<OriPolygon> splitPolygon(OriLine inputLine, OriPolygon polygon) {
		polygon.diagList.clearDiagonalList();
		OriVertex p0 = inputLine.getP0();
		OriVertex p1 = inputLine.getP1();
		int type = inputLine.getType();
		OriVertexList tmpOldList = polygon.vertexList;
		
		OriVertexList newList1 = new OriVertexList();
		OriVertexList newList2 = new OriVertexList();
		
		OriVertex curV = tmpOldList.head;
		//NEW LIST 1
		do {
			if (curV.p.epsilonEquals(p0.p, Constants.EPSILON)) {
				//add the line.p0 to newList; skip all vertices until line.p1 and add p1; 
				//add all remaining vertices until back to head
				newList1.insertBeforeHead(curV);
				do { //skip all vertices right of the folding line
					curV = curV.next;
				} while (!curV.p.epsilonEquals(p1.p, Constants.EPSILON));
				newList1.insertBeforeHead(curV); //insert 2nd vertex of folding line
			
			} else if (curV.p.epsilonEquals(p1.p, Constants.EPSILON)) {
				//add the line.p1 to newList; skip all vertices until line.p0 and add p0; 
				//add all remaining vertices until back to head
				newList1.insertBeforeHead(curV);
				do { //skip all vertices right of the folding line
					curV = curV.next;
				} while (!curV.p.epsilonEquals(p0.p, Constants.EPSILON));
				newList1.insertBeforeHead(curV); //insert 2nd vertex of folding line
			
				
			} else {
				//curV doesn't equal the inputLine points --> just add it to new list;
				newList1.addVertex(curV.p.x, curV.p.y);
			}
			curV = curV.next;
		} while (!curV.p.epsilonEquals(tmpOldList.head.p, Constants.EPSILON));
		
		//curV back at the head
		curV = tmpOldList.head;

		//NEW LIST 2
		//Set head to either p0 or p1, SKIP all other vertices
		while (!curV.p.epsilonEquals(p0.p, Constants.EPSILON) && !curV.p.epsilonEquals(p1.p, Constants.EPSILON)) {
			curV = curV.next;
		} 
		
		
		if (curV.p.epsilonEquals(p0.p, Constants.EPSILON)) {
			//add p0 to newList2, as it is the first vertex
			newList2.addVertex(curV.p.x, curV.p.y);
			
			curV = curV.next;
			while (!curV.p.epsilonEquals(p1.p, Constants.EPSILON)) {
				//add all vertices that are between p0 and p1 (right side of the line)
				newList2.insertBeforeHead(new OriVertex(curV.p.x, curV.p.y));
				curV = curV.next;
			}
			newList2.insertBeforeHead(new OriVertex(curV.p.x, curV.p.y));
			
		} else if (curV.p.epsilonEquals(p1.p, Constants.EPSILON)) {
			newList2.addVertex(curV.p.x, curV.p.y);
			
			curV = curV.next;
			while (!curV.p.epsilonEquals(p0.p, Constants.EPSILON)) {
				
				newList2.insertBeforeHead(new OriVertex(curV.p.x, curV.p.y));
				curV = curV.next;
			}
			newList2.insertBeforeHead(new OriVertex(curV.p.x, curV.p.y));
		} else {
			System.out.println("head does not equal inputLine.p0 or inputLine.p1");
		}

		ArrayList<OriPolygon> splitPList = new ArrayList<OriPolygon>();
		OriPolygon newPolygon1 = new OriPolygon(newList1);
		OriPolygon newPolygon2 = new OriPolygon(newList2);
		splitPList.add(newPolygon1);
		splitPList.add(newPolygon2);
		return  splitPList;
	}
	
	private ArrayList<OriPolygon> getAffectedPolygons(OriLine inputLine) {
		ArrayList<OriPolygon> pList = new ArrayList<>();
		for (OriPolygon polygon : polygons) {
			if (inputLine.intersects(polygon)) {
				pList.add(polygon);
			}
		}
		return pList;
	}
	
	
	/**Adds a new OriLine and checks for intersections with others
	 * 
	 * @param inL
	 */
	public void addLineToPolygons(OriLine inL) {
		boolean p0Added = false;
		boolean p1Added = false;
		if (inL.getType() == OriLine.TYPE_MOUNTAIN || 
				inL.getType() == OriLine.TYPE_VALLEY) {
			//get all the polygons that are affected by the inputLine
			ArrayList<OriPolygon> tmpPolygonList = getAffectedPolygons(inL);

			//add the 2 vertices of the input line to the affected polygons
			for (OriPolygon p: tmpPolygonList) {
				if (p.contains(inL.getP0())) {
					p0Added = true;
					p.vertexList.addVertex(inL.getP0().p.x, inL.getP0().p.y);
				}

				if (p.contains(inL.getP1())) {
					p1Added = true;
					p.vertexList.addVertex(inL.getP1().p.x, inL.getP1().p.y);
				}
				
				//only split polygons if 2 new points are added
				if (p0Added && p1Added) {
					polygons.remove(p);
					polygons.addAll(splitPolygon(inL, p));
				}
				p0Added = false;
				p1Added = false;
			}
		}
	}
	
	/**Adds a new OriLine and checks for intersections with others
	 * 
	 * @param inL
	 */
	public void addLine(OriLine inputLine) {
		//don't add the line if it already exists
		for (OriLine l : lines) {
			if (inputLine.isSameLine(l)) {
				l.setType(inputLine.getType());
				return;
			}
		}
		
		//check if the inputLine is partially the same as existing line
		for (OriLine l : lines) {
			if (inputLine.isPartiallySameLine(l)) {
				return;
			}
		}
		
		// Check if the vertices of inputLine already exist or not
		// and split existing lines, if the vertices of 
		// inputLine lie on an existing line			
		boolean isNewVertexP0 = true;
		boolean isNewVertexP1 = true;

		for (OriVertex v : vertices) {
			if (GeometryUtil.closeCompareOriVertex(inputLine.getP0(), v)) {
				//CASE input.p0 is similar to existing v -> just make input.p0 = v as to avoid rounding issues
				inputLine.setP0(v);
				isNewVertexP0 = false;
			} else if (GeometryUtil.closeCompareOriVertex(inputLine.getP1(), v)) {
				//CASE input.p1 is similar to existing v -> just make input.p1 = v as to avoid rounding issues
				inputLine.setP1(v);
				isNewVertexP1 = false;
			}
		}

		if (isNewVertexP0) { 
			//CASE input.p0 is a new vertex and might split existing lines
			vertices.add(inputLine.getP0());
			splitLinesFromVertex(inputLine.getP0());
		}
		if (isNewVertexP1) { 
			//CASE input.p1 is a new vertex and might split existing lines
			vertices.add(inputLine.getP1());
			splitLinesFromVertex(inputLine.getP1());
		}
		splitExistingLines(inputLine);

		lines.add(inputLine);
	}
	
	/**
	 * Clears die diagList from the paperPolygon but leaves the existing crease lines.
	 * The existing crease lines are originally added to check for intersections while creating new diagonals.
	 */
	public void clearLinesExceptCreases() {
		for (int i=0; i<lines.size(); i++) {
			int type = lines.get(i).getType();
			if (type == OriLine.TYPE_DIAGONAL || type == OriLine.TYPE_EDGE) {
				lines.remove(i);
				i = -1;
			}
//			if (polygons.size() != 0) {
//				for (OriPolygon p : polygons) {
//					
//					if (p.diagList.n != 0) {
//						OriDiagonal curDiag = p.diagList.head;
//						OriDiagonal preCur = curDiag.prev;
//						OriDiagonal nextCur = curDiag.next;
//
//						do {
//							if (l.getP0() == curDiag.v1 && l.getP1() == curDiag.v2
//									|| l.getP0() == curDiag.v2 && l.getP1() == curDiag.v1) {
//
//								preCur.next = nextCur;
//								nextCur.prev = preCur;
//								p.diagList.n--;
//								System.out.println("removed ");
//							}
//							curDiag = curDiag.next;
//						} while (curDiag != p.diagList.head);
//					}
//				}
//			}
		}
	}
	
	/**
	 * Adds a new Vertex to the current diagram step
	 * @param inputVertex
	 */
	public void addVertex(OriVertex inputVertex) {
		//vertices.add(inputVertex);
		//vertexList.addVertex(inputVertex.p.x, inputVertex.p.y);
		
//		paperPolygon.diagList.head = null;
//		paperPolygon.listCopy(); 	//TODO: add vertex doesn't work with the polygonList
//		paperPolygon.triangulate();
		addLinesFromVertices();

	}
	
	 
	public void cutObjects() {
		copyObjects();
		deleteSelectedLines();
		deleteSelectedVertices();
		deleteSelectedArrows();
		deleteSelectedFaces();
		deleteSelectedGeomSymbols();
		deleteSelectedEqualDistSymbols();
		deleteSelectedEqualAnglSymbols();
		deleteSelectedPleatSymbols();
		Origrammer.mainFrame.mainScreen.repaint();
	}
	
	public void copyObjects() {
		copiedObjects.clear();
		
		copiedObjects.lines = getSelectedLines();
		copiedObjects.vertices = getSelectedVertices();
		copiedObjects.arrows = getSelectedArrows();
		copiedObjects.filledFaces = getSelectedFaces();
		copiedObjects.geomSymbols = getSelectedGeomSymbols();
		copiedObjects.equalDistSymbols = getSelectedEqualDistSymbols();
		copiedObjects.equalAnglSymbols = getSelectedEqualAnglSymbols();
		copiedObjects.pleatCrimpSymbols = getSelectedPleatSymbols();
	}
	
	
	public void pasteCopiedObjects() {
		for (OriLine l : copiedObjects.lines) {
			OriLine inL = new OriLine(l);
			
			inL.setP0(new OriVertex(l.getP0().p.x + 20, l.getP0().p.y + 20));
			inL.setP1(new OriVertex(l.getP1().p.x + 20, l.getP1().p.y + 20));	//TODO: maybe snapping to grid? --> issue with copy/pasting multiple different OriObjects
			addLine(inL);													//TODO: could use line-snapping-to-grid only when lines are copied --> else just move everything by (20, 20)
		}
//		for (OriVertex v : copiedObjects.vertices) {
//			OriVertex inV = new OriVertex(v);			//TODO: doesn't make sense as you can simply add more vertices  --> don't include them
//			addVertex(inV);
//		}
//		for (OriArrow a : copiedObjects.arrows) {
//			OriArrow inA = new OriArrow(a);
//			inA.setPosition(new Vector2d(a.getPosition().x + 20, a.getPosition().y + 20)); //TODO: make OriArrow copy/paste-able
//			addArrow(inA);
//		}
//		for (OriFace f : copiedObjects.filledFaces) {
//			OriFace inF = new OriFace(f);
//															//TODO: doesn't make sense as FilledFaces are not movable?  --> don't include them
//			addFilledFace(inF);
//		}
		for (OriGeomSymbol gs : copiedObjects.geomSymbols) {
			OriGeomSymbol inGs = new OriGeomSymbol(gs);
			inGs.setP1(new Vector2d(gs.getP1().x + 20, gs.getP1().y + 20));
			addGeomSymbol(inGs);
		}
		for (OriEqualDistSymbol eds : copiedObjects.equalDistSymbols) {
			OriEqualDistSymbol inEds = new OriEqualDistSymbol(eds);
			inEds.setP0(new Vector2d(eds.getP0().x + 20, eds.getP0().y + 20));
			inEds.setP1(new Vector2d(eds.getP1().x + 20, eds.getP1().y + 20));
			addEqualDistSymbol(inEds);
		}
		for (OriEqualAnglSymbol eas : copiedObjects.equalAnglSymbols) {
			OriEqualAnglSymbol inEas = new OriEqualAnglSymbol(eas);
			inEas.setV(new Vector2d(eas.getV().x + 20, eas.getV().y + 20));
			inEas.setP1(new Vector2d(eas.getP1().x + 20, eas.getP1().y + 20));
			inEas.setP2(new Vector2d(eas.getP2().x + 20, eas.getP2().y + 20));

			addEqualAngleSymbol(inEas);
		}
		for (OriPleatCrimpSymbol pcs : copiedObjects.pleatCrimpSymbols) {
			OriPleatCrimpSymbol inPcs = new OriPleatCrimpSymbol(pcs);
			inPcs.setPosition(new Vector2d(pcs.getPosition().x + 20, pcs.getPosition().y + 20));
			addPleatSymbol(inPcs);
		}
	}
	
	public void pushUndoInfo() { //TODO: include OriObject changes in the undoInfo --> might have to rework the undo system
		UndoInfo ui = new UndoInfo();
		for (OriLine l : Origrammer.diagram.steps.get(Globals.currentStep).lines) {
			ui.lines.add(new OriLine(l));
		}
		for (OriVertex v : Origrammer.diagram.steps.get(Globals.currentStep).vertices) {
			ui.vertices.add(new OriVertex(v));
		}
		for (OriArrow a : Origrammer.diagram.steps.get(Globals.currentStep).arrows) {
			ui.arrows.add(new OriArrow(a));
		}
		for (OriFace f : Origrammer.diagram.steps.get(Globals.currentStep).filledFaces) {
			ui.filledFaces.add(new OriFace(f));
		}
		for (OriGeomSymbol gs : Origrammer.diagram.steps.get(Globals.currentStep).geomSymbols) {
			ui.geomSymbols.add(new OriGeomSymbol(gs));
		}
		for (OriEqualDistSymbol eds : Origrammer.diagram.steps.get(Globals.currentStep).equalDistSymbols) {
			ui.equalDistSymbols.add(new OriEqualDistSymbol(eds));
		}
		for (OriEqualAnglSymbol eas : Origrammer.diagram.steps.get(Globals.currentStep).equalAnglSymbols) {
			ui.equalAnglSymbols.add(new OriEqualAnglSymbol(eas));
		}
		for (OriPleatCrimpSymbol pcs : Origrammer.diagram.steps.get(Globals.currentStep).pleatCrimpSymbols) {
			ui.pleatCrimpSymbols.add(new OriPleatCrimpSymbol(pcs));
		}
		undoStack.push(ui);
	}
	
	public void popUndoInfo() {
		if (undoStack.isEmpty()) {
			System.out.println("STACK EMPTY");
			return;
		}
		UndoInfo ui = undoStack.pop();
		Origrammer.diagram.steps.get(Globals.currentStep).lines.clear();
		Origrammer.diagram.steps.get(Globals.currentStep).lines.addAll(ui.lines);
		Origrammer.diagram.steps.get(Globals.currentStep).vertices.clear();
		Origrammer.diagram.steps.get(Globals.currentStep).vertices.addAll(ui.vertices);
		Origrammer.diagram.steps.get(Globals.currentStep).arrows.clear();
		Origrammer.diagram.steps.get(Globals.currentStep).arrows.addAll(ui.arrows);
		Origrammer.diagram.steps.get(Globals.currentStep).filledFaces.clear();
		Origrammer.diagram.steps.get(Globals.currentStep).filledFaces.addAll(ui.filledFaces);
		Origrammer.diagram.steps.get(Globals.currentStep).geomSymbols.clear();
		Origrammer.diagram.steps.get(Globals.currentStep).geomSymbols.addAll(ui.geomSymbols);
		Origrammer.diagram.steps.get(Globals.currentStep).equalDistSymbols.clear();
		Origrammer.diagram.steps.get(Globals.currentStep).equalDistSymbols.addAll(ui.equalDistSymbols);
		Origrammer.diagram.steps.get(Globals.currentStep).equalAnglSymbols.clear();
		Origrammer.diagram.steps.get(Globals.currentStep).equalAnglSymbols.addAll(ui.equalAnglSymbols);
		Origrammer.diagram.steps.get(Globals.currentStep).pleatCrimpSymbols.clear();
		Origrammer.diagram.steps.get(Globals.currentStep).pleatCrimpSymbols.addAll(ui.pleatCrimpSymbols);
	}
	

	public OriVertexList getEdgeLines() {
		if (Globals.paperShape == Constants.PaperShape.SQUARE) {
			//TODO: this is setup for default square paper --> todo for different shapes like octagonal etc.
			return getSquareEdgeLines();
		} else if (Globals.paperShape == Constants.PaperShape.RECTANGLE) {
			return getRectEdgeLines();
		} else {
			return null;
		}
	}

	/**
	 * Creates the {@code OriVertexList} with the vertices for a square paper.
	 * @return
	 */
	public OriVertexList getSquareEdgeLines() {
		
		OriVertex v0 = new OriVertex(-size/2.0, size/2.0);
		
		OriVertexList vertexList = new OriVertexList();
		vertexList.initHead(v0);

		vertexList.addVertex(size/2.0, size/2.0);
		vertexList.addVertex(size/2.0, -size/2.0);
		vertexList.addVertex(-size/2.0, -size/2.0);
		
		return vertexList;
	}


	/**
	 * Creates the {@code OriVertexList} with the vertices for a rectangular paper.
	 * @return
	 */
	public OriVertexList getRectEdgeLines() {

		double width = Origrammer.diagram.recPaperWidth;
		double height = Origrammer.diagram.recPaperHeight;
		double ratio = 0;

		if (width > height) {
			ratio =  height / width;
		} else {
			ratio = width / height;
		}

		OriVertex v0 = new OriVertex(-size/2.0, size/2.0*ratio);
		
		OriVertexList vertexList = new OriVertexList();
		vertexList.initHead(v0);

		vertexList.addVertex(size/2.0, size/2.0*ratio);
		vertexList.addVertex(size/2.0, -size/2.0*ratio);
		vertexList.addVertex(-size/2.0, -size/2.0*ratio);
		
		return vertexList;	
	}
	
	
	private void removeDuplicateLines() {
		for (int i=0; i<lines.size(); i++) {
			OriLine curL = lines.get(i);
			
			for (int j=0; j<lines.size(); j++) {
				OriLine tmpL = lines.get(j);
				
				if (i != j) {
					if (curL.isSameLine(tmpL)) {
						if (curL.getLength() > tmpL.getLength()) {
							lines.remove(curL);
						} else {
							lines.remove(tmpL);
						}
						i = 0;
						j = 0;
						break;
					} /*else if (curL.isPartiallySameLine(tmpL)) {
						if (curL.getLength() > tmpL.getLength()) {
							lines.remove(curL);
						} else {
							lines.remove(tmpL);
						}
						i = 0;
						j = 0;
						break;
					}*/
				}
			}
		}
	}
	
	/**
	 * Checks all existing {@code OriVertex vertices} and returns {@code true} if a similar vertex exists
	 * @param vertex
	 * @return
	 */
	private OriVertex checkAllVerticesForSimilarOriVertex(OriVertex vertex) {
		for (OriVertex v : vertices) {
			if (GeometryUtil.closeCompareOriVertex(vertex, v)) {
				return v;
			}
		}
		return null;
	}

	
	private void splitLinesFromVertex(OriVertex vertex) {
		for (int i=0; i<lines.size(); i++) {
			OriVertex lP0 = lines.get(i).getP0();
			OriVertex lP1 = lines.get(i).getP1();
			int type = lines.get(i).getType();
			//check if vertex is on the OriLine l
			if (GeometryUtil.isPointOnLine(lP0, lP1, vertex)) {
				//if the vertex is either lP0 or lP1, no need to split the line
				if (lP0.p.epsilonEquals(vertex.p, Constants.EPSILON) || lP1.p.epsilonEquals(vertex.p, Constants.EPSILON)) {
					continue;
				}
				if (type == OriLine.TYPE_DIAGONAL) {
					//TODO: line splitting for OriLine.TYPE_DIAGONAL
				} else {
					//create 2 new lines and remove the old one
					lines.add(new OriLine(lines.get(i).getP0(), vertex, lines.get(i).getType()));
					lines.add(new OriLine(vertex, lines.get(i).getP1(), lines.get(i).getType()));
					lines.remove(lines.get(i));
					break;
				}
				
			}
		}
	}
	
	public void addLines(ArrayList<OriLine> lineList) {
		for (OriLine l : lineList) {
			addLine(l);
		}
	}

	public void addInputLine(ArrayList<Vector2d> points, OriLine inputLine) {
		OriVertex prePoint = new OriVertex(points.get(0));

		for (int i = 1; i < points.size(); i++) {
			OriVertex p = new OriVertex(points.get(i));
			if (GeometryUtil.Distance(prePoint.p, p.p) < POINT_EPS) {
				continue;
			}

			OriLine newLine = new OriLine(prePoint, p, inputLine.getType());

			//update isStartOffset and isEndOffset if p0 and p1 got switched after sorting
			if (prePoint == inputLine.getP0()) {
				newLine.setStartOffset(inputLine.isStartOffset());
			} else if (prePoint == inputLine.getP1()) {
				newLine.setStartOffset(inputLine.isEndOffset());
			}
			if (p == inputLine.getP0()) {
				newLine.setEndOffset(inputLine.isStartOffset());
			} else if (p == inputLine.getP1()) {
				newLine.setEndOffset(inputLine.isEndOffset());
			}
			lines.add(newLine);
			prePoint = p;
		}
	}

	
	public void addNewLine(OriLine inputLine) {
		inputLine.getP0().setP(GeometryUtil.round(inputLine.getP0().p, 10));
		inputLine.getP1().setP(GeometryUtil.round(inputLine.getP1().p, 10));
		
		ArrayList<OriLine> splitLines = splitInputLine(inputLine);
		if (splitLines.size() == 0) {
			addLineToPolygons(inputLine);
			addLine(inputLine);
		}
		
		for (OriLine l : splitLines) {
			addLineToPolygons(l);
			addLine(l);
		}
		addLinesFromVertices();
	}
	
	/**
	 * Splits the inputLine where it intersects with existing lines.
	 * Adds these smaller lines one by one.
	 * @param inputLine
	 * @return
	 */
	public ArrayList<OriLine> splitInputLine(OriLine inputLine) {
		ArrayList<Vector2d> points = new ArrayList<>();
		//split up the inputLine where it crosses existing lines
		for (OriPolygon p : polygons) {
			OriVertex curV = p.vertexList.head;
			OriVertex curV1 = curV.next;
			do {
				Vector2d crossPoint = GeometryUtil.getCrossPoint(inputLine, new OriLine(curV, curV1, OriLine.TYPE_NONE));
				if (crossPoint != null && !points.contains(crossPoint)) {
					points.add(new Vector2d(GeometryUtil.round(crossPoint.x, 10), GeometryUtil.round(crossPoint.y, 10)));
				}
				curV = curV.next;
				curV1 = curV.next;
			} while (curV != p.vertexList.head);

		}

//		for (OriLine line : lines) {
//			if (line.getType() == OriLine.TYPE_DIAGONAL) {
//				continue;
//			}
////			if (GeometryUtil.Distance(inputLine.getP0().p,  line.getP0().p) < POINT_EPS) {
////				continue;
////			}
////			if (GeometryUtil.Distance(inputLine.getP0().p,  line.getP1().p) < POINT_EPS) {
////				continue;
////			}
////			if (GeometryUtil.Distance(inputLine.getP1().p,  line.getP0().p) < POINT_EPS) {
////				continue;
////			}
////			if (GeometryUtil.Distance(inputLine.getP1().p,  line.getP1().p) < POINT_EPS) {
////				continue;
////			}
////			if (GeometryUtil.DistancePointToSegment(line.getP0().p, inputLine.getP0().p, inputLine.getP1().p) < POINT_EPS) {
////				System.out.println("this never happens");
////				points.add(line.getP0().p);
////			}
////			if (GeometryUtil.DistancePointToSegment(line.getP1().p, inputLine.getP0().p, inputLine.getP1().p) < POINT_EPS) {
////				System.out.println("this never happens");
////				points.add(line.getP1().p);
////			}
//			
//
//			Vector2d crossPoint = GeometryUtil.getCrossPoint(inputLine, line);
//			if (crossPoint != null && !points.contains(crossPoint)) {
//					points.add(crossPoint);
//
//			}
//		}
		
		ArrayList<Vector2d> sortedList = new ArrayList<Vector2d>();

		//sort points
		sortedList.add(inputLine.getP0().p);

		boolean didAdd = false;
		
		for (Vector2d p : points) {
			if (p.epsilonEquals(inputLine.getP0().p, Constants.EPSILON)) {
				continue;
			}
			double curDist = GeometryUtil.Distance(inputLine.getP0().p, p);
			if (sortedList.size() == 1) {
				sortedList.add(p);
				continue;
			}
			for (int i=1; i<sortedList.size(); i++) {
				double checkDist = GeometryUtil.Distance(inputLine.getP0().p, sortedList.get(i));
				if (curDist < checkDist) {
					sortedList.add(i, p);
					didAdd = true;
					break;
				}
				
			}
			if (!didAdd) {
				sortedList.add(p);
			}
			didAdd = false;
			
		}
		
		ArrayList<OriLine> inLines = new ArrayList<>();
		
		for (int i=0; i<sortedList.size()-1; i++) {
			OriVertex tmpV1 = new OriVertex(sortedList.get(i));
			OriVertex tmpV2 = new OriVertex(sortedList.get(i+1));
			OriLine tmpLine = new OriLine(tmpV1, tmpV2, inputLine.getType());
			inLines.add(tmpLine);
		}
		
		return inLines;
	}

	/** Checks if the inputLine crosses any existing lines and splits them if necessary.
	 *  Also checks the correct direction in combination with the start and end offset for OriLine TYPE_CREASES
	 * @param inputLine The entered Line
	 */
	public void splitExistingLines(OriLine inputLine) {
		ArrayList<OriLine> tmpLines = new ArrayList<>();
		tmpLines.addAll(lines);

		//if new line crosses another one, split them up to smaller lines
		for (OriLine l : tmpLines) {
			if (l.getType() == OriLine.TYPE_DIAGONAL) {
				continue;
			}
			OriVertex crossPoint = new OriVertex(GeometryUtil.getCrossPoint(l, inputLine));
			if (crossPoint.p == null) {
				continue;
			} else { //a crossPoint was found
				OriVertex existingVertex = checkAllVerticesForSimilarOriVertex(crossPoint);
				if (existingVertex != null){ //an existing vertex similar to crossPoint was found
					crossPoint = existingVertex;
				} else { //no existing vertex similar to crossPoint was found
					vertices.add(crossPoint);
				}
			}
			

			lines.remove(l);

			//when splitting lines, check what direction the existing line is facing --> according to that disregard some of the offsetFlafs for the endPoints
			if (l.getP0().p.x < l.getP1().p.x || ((l.getP0().p.x == l.getP1().p.x) && (l.getP0().p.y < l.getP1().p.y))) {
				if (GeometryUtil.Distance(l.getP0().p, crossPoint.p) > POINT_EPS) { //l.p0 ---------> crossPoint
					//when inputLine only shares an endpoint of an existing line (and not crossing it), don't disregard the offsetFlag for the endPoint
					//the side of a split up line where the crossPoint is should not have an offset, except for when the crossPoint equals one of the inputLine points
					if (crossPoint.equals(inputLine.getP0()) || crossPoint.equals(inputLine.getP1())) {
						lines.add(new OriLine(l.getP0(), crossPoint, l.getType(), l.isStartOffset(), l.isEndOffset()));
					} else {
						lines.add(new OriLine(l.getP0(), crossPoint, l.getType(), l.isStartOffset(), false));
					}
				}
				if (GeometryUtil.Distance(crossPoint.p, l.getP1().p) > POINT_EPS) { // crossPoint ---------> l.p1
					if (crossPoint.equals(inputLine.getP0()) || crossPoint.equals(inputLine.getP1())) {
						lines.add(new OriLine(crossPoint, l.getP1(), l.getType(), l.isStartOffset(), l.isEndOffset()));
					} else {
						lines.add(new OriLine(crossPoint, l.getP1(), l.getType(), false, l.isEndOffset()));
					}
				}
			} else if (l.getP1().p.x < l.getP0().p.x || ((l.getP1().p.x == l.getP1().p.x) && (l.getP1().p.y < l.getP0().p.y))) {
				if (GeometryUtil.Distance(l.getP1().p, crossPoint.p) > POINT_EPS) { //l.p1 ---------> crossPoint
					if (crossPoint.equals(inputLine.getP0()) || crossPoint.equals(inputLine.getP1())) {
						lines.add(new OriLine(crossPoint, l.getP1(), l.getType(), l.isStartOffset(), l.isEndOffset()));
					} else {
						lines.add(new OriLine(crossPoint, l.getP1(), l.getType(), false, l.isEndOffset()));
					}
				}
				if (GeometryUtil.Distance(crossPoint.p, l.getP0().p) > POINT_EPS) { //crossPoint ----------> l.p0
					if (crossPoint.equals(inputLine.getP0()) || crossPoint.equals(inputLine.getP1())) {
						lines.add(new OriLine(l.getP0(), crossPoint, l.getType(), l.isStartOffset(), l.isEndOffset()));
					} else {
						lines.add(new OriLine(l.getP0(), crossPoint, l.getType(), l.isStartOffset(), false));
					}
				}
			}
		}
	}


	public void addTriangleInsectorLines(OriVertex v0, OriVertex v1, OriVertex v2) {
		OriVertex incenter = new OriVertex(GeometryUtil.getIncenter(v0.p, v1.p, v2.p));
		if (incenter.p == null) {
			System.out.println("Failed to calculate the incenter of the triangle");
		}
		Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
		addLine(new OriLine(incenter, v0, Globals.inputLineType));
		addLine(new OriLine(incenter, v1, Globals.inputLineType));
		addLine(new OriLine(incenter, v2, Globals.inputLineType));
	}


	/** Adds a new Arrow to the current diagram step
	 * 
	 * @param inputArrow
	 */
	public void addArrow(OriArrow inputArrow) {
		arrows.add(inputArrow);
	}
	
	/**
	 * Adds a list of arrows to the current diagram step
	 * @param arrowList
	 */
	public void addArrows(ArrayList<OriArrow> arrowList) {
		arrows.addAll(arrowList);
	}
	

	
	/** Adds a new FilledFace to the current diagram step
	 * 
	 * @param inputFace
	 */
	public void addFilledFace(OriFace inputFace) {
		filledFaces.add(inputFace);
	}

	/**
	 * Adds a new OriGeomSymbol to the current diagram step
	 * @param inputSymbol
	 */
	public void addGeomSymbol(OriGeomSymbol inputSymbol) {
		geomSymbols.add(inputSymbol);
	}

	/**
	 * Adds a new OriEqualDistSymbol to the current diagram step
	 * @param inputSymbol
	 */
	public void addEqualDistSymbol(OriEqualDistSymbol inputSymbol) {
		equalDistSymbols.add(inputSymbol);
	}

	/**
	 * Adds a new OriEqualAngleSymbol to the current diagram step
	 * @param inputSymbol
	 */
	public void addEqualAngleSymbol(OriEqualAnglSymbol inputSymbol) {
		equalAnglSymbols.add(inputSymbol);
	}

	/**
	 * Adds a new OriPleatSymbol to the current diagram step
	 * @param inputSymbol
	 */
	public void addPleatSymbol(OriPleatCrimpSymbol inputSymbol) {
		pleatCrimpSymbols.add(inputSymbol);
	}

	
	/**********************
	 ****  SELECT ALL  ****
	 **********************/
	public void selectAll() {
		selectAllLines();
		selectAllVertices();
		selectAllArrows();
		selectAllFaces();
		selectAllGeomSymbols();
		selectAllEqualDistSymbols();
		selectAllEqualAnglSymbols();
		selectAllPleatSymbols();
	}

	public void selectAllLines() {
		for (OriLine l : lines) {
			l.setSelected(true);
		}
	}

	public void selectAllVertices() {
		for (OriVertex v : vertices) {
			v.setSelected(true);
		}
	}

	public void selectAllArrows() {
		for (OriArrow a : arrows) {
			a.setSelected(true);
		}
	}

	public void selectAllFaces() {
		for (OriArrow a : arrows) {
			a.setSelected(true);
		}
	}

	public void selectAllGeomSymbols() {
		for (OriGeomSymbol s : geomSymbols) {
			s.setSelected(true);
		}
	}

	public void selectAllEqualDistSymbols() {
		for (OriEqualDistSymbol eds : equalDistSymbols) {
			eds.setSelected(true);
		}
	}

	public void selectAllEqualAnglSymbols() {
		for (OriEqualAnglSymbol eas : equalAnglSymbols) {
			eas.setSelected(true);
		}
	}

	public void selectAllPleatSymbols() {
		for (OriPleatCrimpSymbol pleat : pleatCrimpSymbols) {
			pleat.setSelected(true);
		}
	}
	
	
	public int getSelectedObjectsCount() {
		int count = 0;
		for (OriLine l : lines) {
			if (l.isSelected() == true) {
				count++;
			}
		}
		for (OriVertex v : vertices) {
			if (v.isSelected() == true) {
				count++;
			}
		}
		for (OriArrow a : arrows) {
			if (a.isSelected() == true) {
				count++;
			}
		}
		for (OriFace f : filledFaces) {
			if (f.isSelected() == true) {
				count++;
			}
		}
		for (OriGeomSymbol gs : geomSymbols) {
			if (gs.isSelected() == true) {
				count++;
			}
		}
		for (OriEqualDistSymbol eds : equalDistSymbols) {
			if (eds.isSelected() == true) {
				count++;
			}
		}
		for (OriEqualAnglSymbol eas : equalAnglSymbols) {
			if (eas.isSelected() == true) {
				count++;
			}
		}
		for (OriPleatCrimpSymbol pcs : pleatCrimpSymbols) {
			if (pcs.isSelected() == true) {
				count++;
			}
		}

		return count;
	}

	public void unselectAll() {
		unselectAllLines();
		unselectAllVertices();
		unselectAllArrows();
		unselectAllFaces();
		unselectAllGeomSymbols();
		unselectAllEqualDistSymbols();
		unselectAllEqualAnglSymbols();
		unselectAllPleatSymbols();
		unselectAllPolygons();
	}

	public void unselectAllLines() {
		for (OriLine l : lines) {
			l.setSelected(false);
		}
	}

	public void unselectAllVertices() {
		for (OriVertex v : vertices) {
			v.setSelected(false);
		}
	}

	public void unselectAllArrows() {
		for (OriArrow a : arrows) {
			a.setSelected(false);
		}
	}

	public void unselectAllFaces() {
		for (OriFace f : filledFaces) {
			f.setSelected(false);
		}
	}

	public void unselectAllGeomSymbols() {
		for (OriGeomSymbol s : geomSymbols) {
			s.setSelected(false);
		}
	}

	public void unselectAllEqualDistSymbols() {
		for (OriEqualDistSymbol eds : equalDistSymbols) {
			eds.setSelected(false);
		}
	}

	public void unselectAllEqualAnglSymbols() {
		for (OriEqualAnglSymbol eas : equalAnglSymbols) {
			eas.setSelected(false);
		}
	}

	public void unselectAllPleatSymbols() {
		for (OriPleatCrimpSymbol pleat : pleatCrimpSymbols) {
			pleat.setSelected(false);
		}
	}
	
	public void unselectAllPolygons() {
		for (OriPolygon poly : polygons) {
			poly.setSelected(false);
		}
	}
	
	
	/********************************
	 ****  GET SELECTED OBJECTS  ****
	 ********************************/
	
	public ArrayList<OriLine> getSelectedLines() {
		ArrayList<OriLine> selectedLines = new ArrayList<>();

		for (OriLine line : lines) {
			if (line.isSelected()) {
				selectedLines.add(line);
			}
		}
		return selectedLines;
	}
	
	public ArrayList<OriVertex> getSelectedVertices() {
		ArrayList<OriVertex> selectedVertices = new ArrayList<>();

		for (OriVertex v : vertices) {
			if (v.isSelected()) {
				selectedVertices.add(v);
			}
		}
		return selectedVertices;
	}
	
	public ArrayList<OriArrow> getSelectedArrows() {
		ArrayList<OriArrow> selectedArrows = new ArrayList<>();

		for (OriArrow arrow : arrows) {
			if(arrow.isSelected()) {
				selectedArrows.add(arrow);
			}
		}
		return selectedArrows;
	}
	
	public ArrayList<OriFace> getSelectedFaces() {
		ArrayList<OriFace> selectedFaces = new ArrayList<>();

		for (OriFace face : filledFaces) {
			if (face.isSelected()) {
				selectedFaces.add(face);
			}
		}
		return selectedFaces;
	}
	
	public ArrayList<OriGeomSymbol> getSelectedGeomSymbols() {
		ArrayList<OriGeomSymbol> selectedGeomS = new ArrayList<>();

		for (OriGeomSymbol gs : geomSymbols) {
			if (gs.isSelected()) {
				selectedGeomS.add(gs);
			}
		}
		return selectedGeomS;
	}
	
	public ArrayList<OriEqualDistSymbol> getSelectedEqualDistSymbols() {
		ArrayList<OriEqualDistSymbol> selectedEqualDistS = new ArrayList<>();

		for (OriEqualDistSymbol eds : equalDistSymbols) {
			if (eds.isSelected()) {
				selectedEqualDistS.add(eds);
			}
		}
		return selectedEqualDistS;
	}
	
	public ArrayList<OriEqualAnglSymbol> getSelectedEqualAnglSymbols() {
		ArrayList<OriEqualAnglSymbol> selectedEqualAnglS = new ArrayList<>();

		for (OriEqualAnglSymbol eas : equalAnglSymbols) {
			if (eas.isSelected()) {
				selectedEqualAnglS.add(eas);
			}
		}
		return selectedEqualAnglS;
	}
	
	public ArrayList<OriPleatCrimpSymbol> getSelectedPleatSymbols() {
		ArrayList<OriPleatCrimpSymbol> selectedPleatS = new ArrayList<>();

		for (OriPleatCrimpSymbol pleat : pleatCrimpSymbols) {
			if (pleat.isSelected()) {
				selectedPleatS.add(pleat);
			}
		}
		return selectedPleatS;
	}
	

	/********************************
	 *** DELETE SELECTED OBJECTS ****
	 ********************************/
	
	public void deleteAllSelectedObjects() {
		deleteSelectedLines();
		deleteSelectedVertices();
		deleteSelectedArrows();
		deleteSelectedFaces();
		deleteSelectedGeomSymbols();
		deleteSelectedEqualDistSymbols();
		deleteSelectedEqualAnglSymbols();
		deleteSelectedPleatSymbols();
	}

	/**
	 * Deletes all selected lines of the current diagram step
	 */
	public void deleteSelectedLines() {
		ArrayList<OriLine> selectedLines = getSelectedLines();

		if (selectedLines.size() != 0) {
			Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
			for (OriLine line : selectedLines) {
				lines.remove(line);
			}
		}
	}

	/**
	 * Deletes all selected vertices of the current diagram step
	 */
	public void deleteSelectedVertices() {
		ArrayList<OriVertex> selectedVertices = getSelectedVertices();

		if (selectedVertices.size() != 0) {
			Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
			for (OriVertex v : selectedVertices) {
				vertices.remove(v);
			}
		}
	}

	/**
	 * Deletes all selected arrows of the current diagram step
	 */
	public void deleteSelectedArrows() {
		ArrayList<OriArrow> selectedArrows = getSelectedArrows();

		if (selectedArrows.size() != 0) {
			Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
			for (OriArrow arrow : selectedArrows) {
				arrows.remove(arrow);
			}
		}
	}

	/**
	 * Deletes all selected faces of the current diagram step
	 */
	public void deleteSelectedFaces() {
		ArrayList<OriFace> selectedFaces = getSelectedFaces();
		
		if (selectedFaces.size() != 0) {
			Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
			for (OriFace face : selectedFaces)  {
				filledFaces.remove(face);
			}
		}
	}

	/**
	 * Deletes all selected OriGeomSymbols of the current diagram step
	 */
	public void deleteSelectedGeomSymbols() {
		ArrayList<OriGeomSymbol> selectedGeomS = getSelectedGeomSymbols();

		if (selectedGeomS.size() != 0) {
			Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
			for (OriGeomSymbol gs : selectedGeomS) {
				geomSymbols.remove(gs);
			}
		}
	}

	/**
	 * Deletes all selected OriEqualDistSymbols of the current diagram step
	 */
	public void deleteSelectedEqualDistSymbols() {
		ArrayList<OriEqualDistSymbol> selectedEqualDistS = getSelectedEqualDistSymbols();

		if (selectedEqualDistS.size() != 0) {
			Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
			for (OriEqualDistSymbol eds : selectedEqualDistS) {
				equalDistSymbols.remove(eds);
			}
		}
	}

	/**
	 * Deletes all selected OriEqualAnglSymbols of the current diagram step
	 */
	public void deleteSelectedEqualAnglSymbols() {
		ArrayList<OriEqualAnglSymbol> selectedEqualAnglS = getSelectedEqualAnglSymbols();

		if (selectedEqualAnglS.size() != 0) {
			Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
			for (OriEqualAnglSymbol eas : selectedEqualAnglS) {
				equalAnglSymbols.remove(eas);
			}
		}
	}

	/**
	 * Deletes all selected OriPleatSymbols of the current diagram step
	 */
	public void deleteSelectedPleatSymbols() {
		ArrayList<OriPleatCrimpSymbol> selectedPleatS = getSelectedPleatSymbols();

		if (selectedPleatS.size() != 0) {
			Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
			for (OriPleatCrimpSymbol pleat : selectedPleatS) {
				pleatCrimpSymbols.remove(pleat);
			}	
		}
	}

	public String getStepDescription() {
		return stepDescription;
	}

	public void setStepDescription(String stepDescription) {
		this.stepDescription = stepDescription;
	}

	@Override
	public String toString() {
		return "Step [lines=" + lines + ", vertices=" + vertices + ", arrows=" + arrows
				+ ", stepDescription=" + stepDescription + "]";
	}

}
