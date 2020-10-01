package origrammer;

import java.util.ArrayList;
import java.util.Arrays;

import origrammer.geometry.OriLine;
import origrammer.geometry.OriPolygon;


public class StepDataSet {
	
	public OriLineProxy[] lines;
	public OriPolygonProxy[] polygons;
	public OriSharedLineProxy[] sharedLines;
	public OriVertexProxy[] vertices;
	public OriArrowProxy[] arrows;
	public OriFaceProxy[] faces;
	public OriGeomSymbolProxy[] geomSymbols;
	public OriPleatCrimpProxy[] pleatCrimpSymbols;
	public OriEqualDistProxy[] equalDistSymbols;
	public OriEqualAnglProxy[] equalAnglSymbols;
	public String stepDescription;

	
	public StepDataSet() {
	}
	
	public StepDataSet(Step s) {
		int lineCount = s.lines.size();
		lines = new OriLineProxy[lineCount];
		for (int i = 0; i < lineCount; i++) {
			lines[i] = new OriLineProxy(s.lines.get(i));
		}
		
		int polyCount = s.polygons.size();
		polygons = new OriPolygonProxy[polyCount];
		for (int i=0; i<polyCount; i++) {
			polygons[i] = new OriPolygonProxy(s.polygons.get(i));
		}
		
		int sharedLinesCount = s.sharedLines.size();
		sharedLines = new OriSharedLineProxy[sharedLinesCount];
		int counter = 0;
		for (OriLine l : s.sharedLines.keySet()) {
			sharedLines[counter] = new OriSharedLineProxy(l, s.sharedLines.get(l));
			counter++;
		}

		int vertexCount = s.vertices.size();
		vertices = new OriVertexProxy[vertexCount];
		for (int i = 0; i < vertexCount; i++) {
			vertices[i] = new OriVertexProxy(s.vertices.get(i));
		}

		int arrowCount = s.arrows.size();
		arrows = new OriArrowProxy[arrowCount];
		for (int i = 0; i < arrowCount; i++) {
			arrows[i] = new OriArrowProxy(s.arrows.get(i));
		}

		int faceCount = s.filledFaces.size();
		faces = new OriFaceProxy[faceCount];
		for (int i = 0; i < faceCount; i++) {
			faces[i] = new OriFaceProxy(s.filledFaces.get(i));
		}

		int geomSymbolCount = s.geomSymbols.size();
		geomSymbols = new OriGeomSymbolProxy[geomSymbolCount];
		for (int i = 0; i < geomSymbolCount; i++) {
			geomSymbols[i] = new OriGeomSymbolProxy(s.geomSymbols.get(i));
		}

		int pleatCrimpCount = s.pleatCrimpSymbols.size();
		pleatCrimpSymbols = new OriPleatCrimpProxy[pleatCrimpCount];
		for (int i = 0; i < pleatCrimpCount; i++) {
			pleatCrimpSymbols[i] = new OriPleatCrimpProxy(s.pleatCrimpSymbols.get(i));
		}

		int equalDistCount = s.equalDistSymbols.size();
		equalDistSymbols = new OriEqualDistProxy[equalDistCount];
		for (int i = 0; i < equalDistCount; i++) {
			equalDistSymbols[i] = new OriEqualDistProxy(s.equalDistSymbols.get(i));
		}

		int equalAnglCount = s.equalAnglSymbols.size();
		equalAnglSymbols = new OriEqualAnglProxy[equalAnglCount];
		for (int i = 0; i < equalAnglCount; i++) {
			equalAnglSymbols[i] = new OriEqualAnglProxy(s.equalAnglSymbols.get(i));
		}
		stepDescription = s.stepDescription;
	}

//	public void recover(Step s) {
//		s.lines.clear();
//		for (int i = 0; i < lines.length; i++) {
//			s.lines.add(lines[i].getLine());
//		}
//		s.vertices.clear();
//		for (int i = 0; i < vertices.length; i++) {
//			s.vertices.add(vertices[i].getVertex());
//		}
//		s.arrows.clear();
//		for (int i = 0; i < arrows.length; i++) {
//			s.arrows.add(arrows[i].getArrow());
//		}
//		s.filledFaces.clear();
//		for (int i = 0; i < faces.length; i++) {
//			s.filledFaces.add(faces[i].getFace());
//		}
//		s.geomSymbols.clear();
//		for(int i = 0; i < geomSymbols.length; i++) {
//			s.geomSymbols.add(geomSymbols[i].getSymbol());
//		}
//		s.pleatCrimpSymbols.clear();
//		for(int i = 0; i < pleatCrimpSymbols.length; i++) {
//			s.pleatCrimpSymbols.add(pleatCrimpSymbols[i].getSymbol());
//		}
//		s.equalDistSymbols.clear();
//		for (int i = 0; i < equalDistSymbols.length; i++) {
//			s.equalDistSymbols.add(equalDistSymbols[i].getSymbol());
//		}
//		s.equalAnglSymbols.clear();
//		for (int i = 0; i < equalAnglSymbols.length; i++) {
//			s.equalAnglSymbols.add(equalAnglSymbols[i].getSymbol());
//		}
//		s.stepDescription = stepDescription;
//	}
	
	public Step getStep() {
		Step tmpStep = new Step();
		
		for (int i = 0; i<lines.length; i++) {
			tmpStep.lines.add(lines[i].getLine());
		}
		for (int i=0; i<polygons.length; i++) {
			tmpStep.polygons.add(polygons[i].getPolygon());
		}
		
		for (int i=0; i<sharedLines.length; i++) {
			OriPolygonProxy firstPoly = sharedLines[i].getPoly1();
			OriPolygonProxy secondPoly = sharedLines[i].getPoly2();
			ArrayList<OriPolygon> newValues = new ArrayList<OriPolygon>();
			
			for (int j=0; i<tmpStep.polygons.size(); j++) {
				OriPolygon p = tmpStep.polygons.get(j);
				if (newValues.size() < 1 
						&& p.vertexList.head.p.epsilonEquals(firstPoly.vertexList[0], Constants.EPSILON)
						&& p.vertexList.head.next.p.epsilonEquals(firstPoly.vertexList[1], Constants.EPSILON)
						&& p.vertexList.head.next.next.p.epsilonEquals(firstPoly.vertexList[2], Constants.EPSILON)) {
					newValues.add(0, p);
					i=0;
				} else if (p.vertexList.head.p.epsilonEquals(secondPoly.vertexList[0], Constants.EPSILON)
						&& p.vertexList.head.next.p.epsilonEquals(secondPoly.vertexList[1], Constants.EPSILON)
						&& p.vertexList.head.next.next.p.epsilonEquals(secondPoly.vertexList[2], Constants.EPSILON)) {
					if (newValues.size() == 1) {
						newValues.add(1, p);
						break;
					}
				}
			}
			
			tmpStep.sharedLines.put(sharedLines[i].getLine().getLine(), newValues);
		}
		
		for (int i = 0; i < vertices.length; i++) {
			tmpStep.vertices.add(vertices[i].getVertex());
		}
		for (int i = 0; i < arrows.length; i++) {
			tmpStep.arrows.add(arrows[i].getArrow());
		}
		for (int i = 0; i < faces.length; i++) {
			tmpStep.filledFaces.add(faces[i].getFace());
		}
		for (int i = 0; i < geomSymbols.length; i++) {
			tmpStep.geomSymbols.add(geomSymbols[i].getSymbol());
		}
		for (int i = 0; i < pleatCrimpSymbols.length; i++) {
			tmpStep.pleatCrimpSymbols.add(pleatCrimpSymbols[i].getSymbol());
		}
		for (int i = 0; i < equalDistSymbols.length; i++) {
			tmpStep.equalDistSymbols.add(equalDistSymbols[i].getSymbol());		
		}
		for (int i = 0; i < equalAnglSymbols.length; i++) {
			tmpStep.equalAnglSymbols.add(equalAnglSymbols[i].getSymbol());
		}
		
		tmpStep.stepDescription = stepDescription;		
		return tmpStep;
	}

	public OriLineProxy[] getLines() {
		return lines;
	}

	public void setLines(OriLineProxy[] lines) {
		this.lines = lines;
	}

	public OriArrowProxy[] getArrows() {
		return arrows;
	}

	public void setArrows(OriArrowProxy[] arrows) {
		this.arrows = arrows;
	}

	@Override
	public String toString() {
		return "StepDataSet [lines=" + Arrays.toString(lines) + ", stepDescription=" + stepDescription + "]";
	}

}
