package origrammer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;

import javax.vecmath.Vector2d;

import origrammer.geometry.GeometryUtil;
import origrammer.geometry.OriArrow;
import origrammer.geometry.OriEqualAnglSymbol;
import origrammer.geometry.OriEqualDistSymbol;
import origrammer.geometry.OriFace;
import origrammer.geometry.OriGeomSymbol;
import origrammer.geometry.OriLine;
import origrammer.geometry.OriPleatCrimpSymbol;
import origrammer.geometry.OriVertex;

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
		ArrayList<OriLine> inputLines = getEdgeLines();
		
		for (OriLine l : inputLines) {
			addLine(l);
		}
		
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
	

	public ArrayList<OriLine> getEdgeLines() {
		if (Globals.paperShape == Constants.PaperShape.SQUARE) {
			//TODO: this is setup for default square paper --> todo for different shapes like octagonal etc.
			return getSquareEdgeLines();
		} else if (Globals.paperShape == Constants.PaperShape.RECTANGLE) {
			return getRectEdgeLines();
		} else {
			return null;
		}
	}

	public ArrayList<OriLine> getSquareEdgeLines() {
		ArrayList<OriLine> newLines = new ArrayList<>();

		OriLine l0 = new OriLine(-size/2.0, size/2.0, size/2.0, size/2.0, OriLine.TYPE_EDGE);
		OriLine l1 = new OriLine(size/2.0, size/2.0, size/2.0, -size/2.0, OriLine.TYPE_EDGE);
		OriLine l2 = new OriLine(size/2.0, -size/2.0, -size/2.0, -size/2.0, OriLine.TYPE_EDGE);
		OriLine l3 = new OriLine(-size/2.0, -size/2.0, -size/2.0, size/2.0, OriLine.TYPE_EDGE);
		newLines.add(l0);
		newLines.add(l1);
		newLines.add(l2);
		newLines.add(l3);
		return newLines;
	}


	public ArrayList<OriLine> getRectEdgeLines() {
		ArrayList<OriLine> newLines = new ArrayList<>();

		double width = Origrammer.diagram.recPaperWidth;
		double height = Origrammer.diagram.recPaperHeight;
		double ratio = 0;

		if (width > height) {
			ratio =  height / width;
		} else {
			ratio = width / height;
		}

		OriLine l0 = new OriLine(-size/2.0, size/2.0*ratio, size/2.0, size/2.0*ratio, OriLine.TYPE_EDGE);
		OriLine l1 = new OriLine(size/2.0, size/2.0*ratio, size/2.0, -size/2.0*ratio, OriLine.TYPE_EDGE);
		OriLine l2 = new OriLine(size/2.0, -size/2.0*ratio, -size/2.0, -size/2.0*ratio, OriLine.TYPE_EDGE);
		OriLine l3 = new OriLine(-size/2.0, -size/2.0*ratio, -size/2.0, size/2.0*ratio, OriLine.TYPE_EDGE);
		newLines.add(l0);
		newLines.add(l1);
		newLines.add(l2);
		newLines.add(l3);

		return newLines;
	}



	/**Adds a new OriLine and checks for intersections with others
	 * 
	 * @param inputLine
	 */
	public void addLine(OriLine inputLine) {
		//don't add the line if it already exists
		for (OriLine l : lines) {
			if (isSameLine(l, inputLine)) {
				l.setType(inputLine.getType());
				System.out.println("Line already exists");
				return;
			}
		}
		
		//check if the inputLine is partially the same as existing line
		for (OriLine l : lines) {
			if (isPartiallySameLine(l, inputLine)) {
				System.out.println("partially same Line already exists");
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
		
		//points contains p0 and p1 of inputLine
		ArrayList<Vector2d> points = new ArrayList<>();
		points.add(inputLine.getP0().p);
		points.add(inputLine.getP1().p);
		points.addAll(splitInputLine(inputLine));

		//sort ArrayList<Vector2d> points
		boolean sortByX = Math.abs(inputLine.getP0().p.x - inputLine.getP1().p.x) > Math.abs(inputLine.getP0().p.y - inputLine.getP1().p.y);
		if (sortByX) {
			Collections.sort(points, new PointComparatorX());
		} else {
			Collections.sort(points, new PointComparatorY());
		}

		addInputLine(points, inputLine);
		
		removeDuplicateLines();
	}
	
	
	private void removeDuplicateLines() {
		for (int i=0; i<lines.size(); i++) {
			OriLine l0 = lines.get(i);
			for (int j=0; j<lines.size(); j++) {
				OriLine l1 = lines.get(j);
				if (i != j) {
					if (l1.getP0().p.equals(l0.getP0().p) && l1.getP1().p.equals(l0.getP1().p)) { //case l.p0 == tmpL.p0 && l.p1 == tmpL.p1
						if (i > j) {
							lines.remove(l1);
							i = 0;
							j = 0;
							break;
						} else {
							lines.remove(l0);
							i = 0;
							j = 0;
							break;
						}
						
					} else if (l1.getP0().p.equals(l0.getP1().p) && l1.getP1().p.equals(l0.getP0().p)) {//case l.p0 == tmpL.p1 && l.p1 == tmpL.p0
						if (i > j) {
							lines.remove(l1);
							i = 0;
							j = 0;
							break;
						} else {
							lines.remove(l0);
							i = 0;
							j = 0;
							break;
						}
					}
				}
				
			}
		}
	}
	
	/**
	 * Checks if {@code l0} is the same line as {@code l1}
	 * @param l0
	 * @param l1
	 * @return {@code true} if both lines are the same
	 */
	private boolean isSameLine(OriLine l0, OriLine l1) {
		if (GeometryUtil.closeCompareOriVertex(l0.getP0(), l1.getP0()) && GeometryUtil.closeCompareOriVertex(l0.getP1(), l1.getP1())) {	
			return true;
		} else if (GeometryUtil.closeCompareOriVertex(l0.getP0(), l1.getP1()) && GeometryUtil.closeCompareOriVertex(l0.getP1(), l1.getP0())) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if the two lines are partially the same. As in, do they share a common part
	 * @param toCheckLine
	 * @param inputLine
	 * @return {@code true} if both lines share one vertex at least
	 */
	private boolean isPartiallySameLine(OriLine toCheckLine, OriLine inputLine) {
		Vector2d uv0 = GeometryUtil.getUnitVector(toCheckLine.getP0().p, toCheckLine.getP1().p);
		Vector2d uv1 = GeometryUtil.getUnitVector(inputLine.getP0().p, inputLine.getP1().p);
		Vector2d uv1Neg = uv1;
		boolean isSameDirection = false;
		uv1Neg.negate();
		
		if (uv0.epsilonEquals(uv1, Constants.EPSILON)) {
			isSameDirection = true;
		} else if (uv0.epsilonEquals(uv1Neg, Constants.EPSILON)) {
			isSameDirection = true;
		}

		if (isSameDirection) {
			if (GeometryUtil.closeCompareOriVertex(toCheckLine.getP0(), inputLine.getP0())) {
				splitLinesFromVertex(inputLine.getP1()); //check if second vertex of the line is splitting an existing one
				return true;
			} else if (GeometryUtil.closeCompareOriVertex(toCheckLine.getP0(), inputLine.getP1())) {
				splitLinesFromVertex(inputLine.getP1()); //check if second vertex of the line is splitting an existing one
				return true;
			} else if (GeometryUtil.closeCompareOriVertex(toCheckLine.getP1(), inputLine.getP0())) {
				splitLinesFromVertex(inputLine.getP0()); //check if second vertex of the line is splitting an existing one
				return true;
			} else if (GeometryUtil.closeCompareOriVertex(toCheckLine.getP1(), inputLine.getP1())) {
				splitLinesFromVertex(inputLine.getP0()); //check if second vertex of the line is splitting an existing one
				return true;
			}
		}
		return false;
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
			double distl0V = GeometryUtil.Distance(lines.get(i).getP0().p, vertex.p);
			double distVl1 = GeometryUtil.Distance(vertex.p, lines.get(i).getP1().p);
			double distl0l1 = GeometryUtil.Distance(lines.get(i).getP0().p, lines.get(i).getP1().p);
			
			//check if vertex is on the OriLine l
			//l.p0 ----- vertex --------------- l.p1 == l.p0 ---------------------------- l.p1
			if (GeometryUtil.closeCompare(distl0V + distVl1, distl0l1, Constants.EPSILON)) {
				if (lines.get(i).getP0().p.equals(vertex.p) && lines.get(i).getP1().p.equals(vertex.p)) {
					continue;
				}
				if (lines.get(i).getType() == OriLine.TYPE_CREASE) {
					//TODO: line splitting for OriLine.TYPE_CREASE
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

	public ArrayList<Vector2d> splitInputLine(OriLine inputLine) {
		ArrayList<Vector2d> points = new ArrayList<>();
		//split up the inputLine where it crosses existing lines
		for (OriLine line : lines) {
//			if (GeometryUtil.Distance(inputLine.getP0().p,  line.getP0().p) < POINT_EPS) {
//				continue;
//			}
//			if (GeometryUtil.Distance(inputLine.getP0().p,  line.getP1().p) < POINT_EPS) {
//				continue;
//			}
//			if (GeometryUtil.Distance(inputLine.getP1().p,  line.getP0().p) < POINT_EPS) {
//				continue;
//			}
//			if (GeometryUtil.Distance(inputLine.getP1().p,  line.getP1().p) < POINT_EPS) {
//				continue;
//			}
			if (GeometryUtil.DistancePointToSegment(line.getP0().p, inputLine.getP0().p, inputLine.getP1().p) < POINT_EPS) {
				points.add(line.getP0().p);
			}
			if (GeometryUtil.DistancePointToSegment(line.getP1().p, inputLine.getP0().p, inputLine.getP1().p) < POINT_EPS) {
				points.add(line.getP1().p);
			}

			Vector2d crossPoint = GeometryUtil.getCrossPoint(inputLine, line);
			if (crossPoint != null) {
				points.add(crossPoint);
			}
		}
		return points;
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

	/**
	 * Adds a new Vertex to the current diagram step
	 * @param inputVertex
	 */
	public void addVertex(OriVertex inputVertex) {
		vertices.add(inputVertex);
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
