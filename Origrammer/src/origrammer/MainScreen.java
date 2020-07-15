package origrammer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.vecmath.Vector2d;
import origrammer.geometry.*;

public class MainScreen extends JPanel 
implements MouseListener, MouseMotionListener, MouseWheelListener, ActionListener, ComponentListener {


	private Point2D preMousePoint;
	private Point2D currentMouseDraggingPoint = null;
	private Point2D.Double currentMousePointLogic = new Point2D.Double();
	private double transX;
	private double transY;

	//temp info when editing
	private Vector2d firstSelectedV = null;
	private Vector2d secondSelectedV = null;
	private Vector2d thirdSelectedV = null;
	private Vector2d selectedCandidateV = null;
	private OriLine firstSelectedL = null;
	private OriLine secondSelectedL = null;
	private OriLine thirdSelectedL = null;
	private OriLine selectedCandidateL = null;
	private OriArrow selectedCandidateA = null;
	private OriFace selectedCandidateF = null;
	private OriGeomSymbol selectedCandidateGS = null;
	private OriEqualDistSymbol selectedCandidateEDS = null;
	private OriEqualAnglSymbol selectedCandidateEAS = null;
	private OriPleatCrimpSymbol selectedCandidatePleat = null;
	private OriLeaderBox selectedCandidateLeader = null;

	private boolean dispGrid = true;
	//Affine transformation info
	private Dimension preSize;
	private AffineTransform affineTransform = new AffineTransform();

	private Graphics2D g2d;

	public ArrayList<JLabel> arrowLabelList = new ArrayList<>();
	private boolean isMovingSymbols = false;
	private boolean isPressedOverSymbol = false;
	int tmpArrowWidth;
	int tmpArrowHeight;

	public MainScreen() {
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addComponentListener(this);

		Globals.SCALE = 1.0;
		setBackground(Color.white);
		setPreferredSize(new Dimension(800, 800));
		setLayout(null);

		preSize = getSize();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		removeAll();
		g2d = (Graphics2D) g;

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		updateAffineTransform(g2d);

		if (dispGrid) {
			drawGrid(g2d);
		}

		renderAllFilledFaces();
		renderAllLines();
		renderAllArrows();

		//Symbols
		renderAllOriLeaderBoxes();
		renderAllOriGeomSymbols();
		renderAllEquDistSymbols();
		renderAllEquAnglSymbols();
		renderAllCrimpsPleats();

		renderAllVertices();

		//temporary stuff
		renderTempLine();
		renderTempArrow();
		renderTempLeaderSymbol();
		renderSelectedVertices();
		renderRectSelection();
		renderTempOriGeomSymbol();
		renderTmpLengthAngleLine();
		renderTmpPerpendicular();

		//show coordinates of selected Vertex
		if (selectedCandidateV != null ) {
			g.setColor(Color.BLACK);
			g.drawString("(" + selectedCandidateV.x + ", " + selectedCandidateV.y + ")", -325, -325);
		}
	}

	private void renderAllFilledFaces() {
		if (Globals.dispFilledFaces) {
			for (OriFace f : Origrammer.diagram.steps.get(Globals.currentStep).filledFaces) {
				if (f.isSelected() || selectedCandidateF == f) {
					g2d.setPaint(new Color(200, 100, 100));
					g2d.draw(f.path);
					g2d.fill(f.path);
				} else {
					if (f.isFaceUp()) {
						g2d.setPaint(Origrammer.diagram.getFaceUpColor());
					} else {
						g2d.setPaint(Origrammer.diagram.getFaceDownColor());
					}
					g2d.draw(f.path);
					g2d.fill(f.path);
				}
			}
		}
	}

	private void renderAllLines() {
		for (OriLine line : Origrammer.diagram.steps.get(Globals.currentStep).lines) {
			//render lines according to their LINE_TYPE
			setColorStrokeByLineType(line.getType());

			if (!Globals.dispColoredLines) {
				g2d.setColor(Config.LINE_COLOR_EDGE);
			}

			//if line is selected during INPUT_LINE/SELECTION_TOOL mode, render GREEN
			if ((Globals.toolbarMode == Constants.ToolbarMode.INPUT_LINE
					&& line.isSelected())
					|| (Globals.toolbarMode == Constants.ToolbarMode.SELECTION_TOOL
					&& line.isSelected())) {
				g2d.setColor(Config.LINE_COLOR_SELECTED);
				g2d.setStroke(Config.STROKE_SELECTED);
			}

			if (line == firstSelectedL || line == secondSelectedL || line == thirdSelectedL) {
				g2d.setColor(Color.RED);
				g2d.setStroke(Config.STROKE_SELECTED);
			} else if (line == selectedCandidateL) {
				g2d.setColor(Config.LINE_COLOR_SELECTED);
				g2d.setStroke(Config.STROKE_SELECTED);
			}
			Vector2d p0 = line.getP0();
			Vector2d p1 = line.getP1();

			if (line.getType() == OriLine.TYPE_CREASE) {
				if (line.isStartOffset()) {
					p0 = line.getTranslatedP0();
				} else {
					p0 = line.getP0();
				}
				if (line.isEndOffset()) {
					p1 = line.getTranslatedP1();
				} else {
					p1 = line.getP1();
				}
			}
			g2d.draw(new Line2D.Double(p0.x, p0.y, p1.x, p1.y));
		}
	}
	
	private void renderTmpLengthAngleLine() {
		if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_LINE) {
			if (Globals.lineEditMode == Constants.LineInputMode.BY_LENGTH_AND_ANGLE) {
				if (Origrammer.mainFrame.uiTopPanel.inputLineLengthTF.getText().length() > 0) {
					if (Origrammer.mainFrame.uiTopPanel.inputLineAngleTF.getText().length() > 0) {
						Vector2d v1 = selectedCandidateV == null 
								? new Vector2d(currentMousePointLogic.getX(), currentMousePointLogic.getY()) : selectedCandidateV;

						String lengthString = Origrammer.mainFrame.uiTopPanel.inputLineLengthTF.getText();
						String angleString = Origrammer.mainFrame.uiTopPanel.inputLineAngleTF.getText();

						Double length = Double.parseDouble(lengthString);
						Double angle = Double.parseDouble(angleString);

						angle = Math.toRadians(angle);
						Vector2d v2 = new Vector2d(length * Math.cos(angle) + v1.x, length * Math.sin(angle) + v1.y);

						
						setColorStrokeByLineType(Globals.inputLineType);
						if (!Globals.dispColoredLines) {
							g2d.setColor(Config.LINE_COLOR_EDGE);
						}
						g2d.draw(new Line2D.Double(v1.x, v1.y, v2.x, v2.y));
					}
				}
			}
		}
	}
	
	/**
	 * Renders a preview of the perpendicular input Line. <br>
	 * Hold {@code CTRL} pressed to reverse the direction.
	 */
	private void renderTmpPerpendicular() {
		if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_LINE) {
			if (Globals.lineEditMode == Constants.LineInputMode.PERPENDICULAR) {
				OriLine l = pickLine(currentMousePointLogic);

				if (l != null) {
					Vector2d uv = GeometryUtil.getUnitVector(l.getP0(), l.getP1());
					Vector2d nv = GeometryUtil.getNormalVector(uv);
					Vector2d v = pickVertex(currentMousePointLogic);
					Vector2d v2 = null;
					//get point on OriLine that is closest to currentMousePointLogic, 
					//if currentMousePointLogic is not close to a vertex
					if (v == null) {
						v = new Vector2d();
						Vector2d cp = new Vector2d(currentMousePointLogic.x, currentMousePointLogic.y);
						GeometryUtil.DistancePointToSegment(cp,  l.getP0(), l.getP1(), v);
					}
					//get crossingPoint in order to get inputLine.P1
					//if there is no crossing point -> reverse inputLine direction
					v2 = getEarlierstCrossPoint(v, nv);
					if (v2 == null) {
						v2 = getEarlierstCrossPoint(v, new Vector2d(-nv.x, -nv.y));
					}
					if (v != null && v2 != null) {
						setColorStrokeByLineType(Globals.inputLineType);
						g2d.draw(new Line2D.Double(v.x,v.y,v2.x,v2.y));
					}
				}
			}
		}
	}
	
	private void renderAllArrows() {
		for (OriArrow a : Origrammer.diagram.steps.get(Globals.currentStep).arrows) {
			ArrayList<Shape> shapes = a.getShapesForDrawing();
			if (a.isSelected() || selectedCandidateA == a) {
				g2d.setColor(Config.LINE_COLOR_SELECTED);
				g2d.setStroke(Config.STROKE_ARROWS);
			} else {
				g2d.setColor(Config.LINE_COLOR_EDGE);
				g2d.setStroke(Config.STROKE_ARROWS);
			}
			Color oldColor = g2d.getColor();
			for (Shape s : shapes) {
				if (s.getClass() == GeneralPath.class) {
					g2d.setColor(Color.WHITE);
					g2d.fill(s);
					g2d.setColor(oldColor);
				}
				g2d.draw(s);

			}
		}
	}

	/**
	 * Renders the preview OriLine while inputting
	 */
	private void renderTempLine() {
		if (firstSelectedV != null) {
			setColorStrokeByLineType(Globals.inputLineType);
			if (!Globals.dispColoredLines) {
				g2d.setColor(Config.LINE_COLOR_EDGE);
			}

			g2d.fill(new Rectangle2D.Double(firstSelectedV.x - 5.0 / Globals.SCALE,
					firstSelectedV.y - 5.0 / Globals.SCALE, 10.0 / Globals.SCALE, 10.0 / Globals.SCALE));

			if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_LINE) {
				if (Globals.lineEditMode == Constants.LineInputMode.INPUT_LINE) {
					Vector2d cv = selectedCandidateV == null 
							? new Vector2d(currentMousePointLogic.getX(), currentMousePointLogic.getY()) : selectedCandidateV;
							g2d.draw(new Line2D.Double(firstSelectedV.x, firstSelectedV.y, cv.x, cv.y));
				}
			}
		}
	}
	
	/**
	 * Renders the preview OriArrow while inputting
	 */
	private void renderTempArrow() {
		if (firstSelectedV != null) {
			g2d.fill(new Rectangle2D.Double(firstSelectedV.x - 5.0 / Globals.SCALE,
					firstSelectedV.y - 5.0 / Globals.SCALE, 10.0 / Globals.SCALE, 10.0 / Globals.SCALE));
			
			if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_ARROW) {
				Vector2d cv = selectedCandidateV == null
						? new Vector2d(currentMousePointLogic.getX(), currentMousePointLogic.getY()) : selectedCandidateV;
	
						OriArrow a = new OriArrow();
						a.setP0(firstSelectedV);
						a.setP1(cv);
						a.setType(Globals.inputArrowType);
						a.setMirrored(Origrammer.mainFrame.uiTopPanel.arrowIsMirrored.isSelected());
						a.setUnfold(Origrammer.mainFrame.uiTopPanel.arrowIsUnfolded.isSelected());
						ArrayList<Shape> shapes = a.getShapesForDrawing();
						for (Shape s : shapes) {
							g2d.draw(s);
						}					
			}
		}
	}
	
	private void renderTempLeaderSymbol() {
		if (firstSelectedV != null) {
			g2d.fill(new Rectangle2D.Double(firstSelectedV.x - 5.0 / Globals.SCALE,
					firstSelectedV.y - 5.0 / Globals.SCALE, 10.0 / Globals.SCALE, 10.0 / Globals.SCALE));
			g2d.setStroke(Config.STROKE_EDGE);
			if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_SYMBOL) {
				Vector2d cv = selectedCandidateV == null
						? new Vector2d(currentMousePointLogic.getX(), currentMousePointLogic.getY()) : selectedCandidateV;
												
						switch (Globals.inputSymbolMode) {	//TODO: render temp methods for all symbols
						case LEADER:
							break;
						case CRIMPING_PLEATING:
							break;
						case EQUAL_ANGL:
							break;
						case EQUAL_DIST:
							break;
						case FOLD_OVER_AND_OVER:
							break;
						case HOLD_HERE:
							break;
						case HOLD_HERE_AND_PULL:
							break;
						case NEXT_VIEW:
							OriGeomSymbol gs = new OriGeomSymbol();
							gs.setPosition(firstSelectedV);
							gs.setDirection(GeometryUtil.getUnitVector(firstSelectedV, cv));
							gs.setType(OriGeomSymbol.TYPE_NEXT_VIEW_HERE);
							gs.setSize(100);
							ArrayList<Shape> shapes = gs.getShapesForDrawing();
							for (Shape s : shapes) {
								g2d.draw(s);
							}	
							break;
						case REPETITION_BOX:
							break;
						case ROTATIONS:
							break;
						case SINKS:
							break;
						case X_RAY_CIRCLE:
							break;
						default:
							break;
						}
			}
		}
	}
	
	/**
	 * Sets the {@code Graphics2D Color} and {@code Stroke} depending on {@code type} <br>
	 * (which is the {@code OriLine.type})
	 * @param type
	 */
	private void setColorStrokeByLineType(int type) {
		setLineStrokeByLineType(type);
		setLineColorByLineType(type);
	}

	/**
	 * Sets the {@code Graphics2D Stroke} depending on {@code type} <br>
	 * (which is the {@code OriLine.type}
	 * @param type
	 */
	private void setLineStrokeByLineType(int type) {
		switch (type) {
		case OriLine.TYPE_NONE:
			g2d.setStroke(Config.STROKE_SELECTED);
			break;
		case OriLine.TYPE_VALLEY:
			g2d.setStroke(Config.STROKE_VALLEY);
			break;
		case OriLine.TYPE_MOUNTAIN:
			if (Globals.mountainFoldStyle == Constants.MountainFoldStyle.DASH_DOT) {
				g2d.setStroke(Config.STROKE_MOUNTAIN_DASH_DOT);
			} else if (Globals.mountainFoldStyle == Constants.MountainFoldStyle.DASH_DOT_DOT) {
				g2d.setStroke(Config.STROKE_MOUNTAIN_DASH_DOT_DOT);
			}
			break;
		case OriLine.TYPE_XRAY:
			g2d.setStroke(Config.STROKE_XRAY);
			break;
		case OriLine.TYPE_EDGE:
			g2d.setStroke(Config.STROKE_EDGE);
			break;
		case OriLine.TYPE_CREASE:
			g2d.setStroke(Config.STROKE_CREASE);
		}
	}
	
	/**
	 * Sets the {@code Graphics2D Color} depending on {@code type} <br>
	 * (which is the {@code OriLine.type}) <br>
	 * and if {@code Globals.dispColoredLines} is {@code true}
	 * @param type
	 */
	private void setLineColorByLineType(int type) {
		if (Globals.dispColoredLines) {
			switch (type) {
			case OriLine.TYPE_NONE:
				g2d.setColor(Config.LINE_COLOR_SELECTED);//TODO: TYPE_NONE COLOR
				break;
			case OriLine.TYPE_VALLEY:
				g2d.setColor(Config.LINE_COLOR_VALLEY);
				break;
			case OriLine.TYPE_MOUNTAIN:
				g2d.setColor(Config.LINE_COLOR_MOUNTAIN);
				break;
			case OriLine.TYPE_XRAY:
				g2d.setColor(Config.LINE_COLOR_XRAY);
			case OriLine.TYPE_EDGE:
				g2d.setColor(Config.LINE_COLOR_EDGE);
			case OriLine.TYPE_CREASE:
				g2d.setColor(Config.LINE_COLOR_CREASE);
			}
		} else {
			g2d.setColor(Config.LINE_COLOR_EDGE);
		}
	}

	private void renderSelectedVertices() {
		if (firstSelectedV != null) {
			g2d.setColor(Color.RED);
			g2d.fill(new Rectangle2D.Double(firstSelectedV.x - 5.0 / Globals.SCALE,
					firstSelectedV.y - 5.0 / Globals.SCALE, 10.0 / Globals.SCALE, 10.0 / Globals.SCALE));
		}

		if (secondSelectedV != null) {
			g2d.setColor(Color.RED);
			g2d.fill(new Rectangle2D.Double(secondSelectedV.x - 5.0 / Globals.SCALE,
					secondSelectedV.y - 5.0 / Globals.SCALE, 10.0 / Globals.SCALE, 10.0 / Globals.SCALE));
		}

		if (thirdSelectedV != null) {
			g2d.setColor(Color.RED);
			g2d.fill(new Rectangle2D.Double(thirdSelectedV.x - 5.0 / Globals.SCALE,
					thirdSelectedV.y - 5.0 / Globals.SCALE, 10.0 / Globals.SCALE, 10.0 / Globals.SCALE));
		}

		if (selectedCandidateV != null) {
			g2d.setColor(Color.GREEN);
			g2d.fill(new Rectangle2D.Double(selectedCandidateV.x - 5.0 / Globals.SCALE,
					selectedCandidateV.y - 5.0 / Globals.SCALE, 10.0 / Globals.SCALE, 10.0 / Globals.SCALE));
		}
	}

	private void renderTempOriGeomSymbol() {
		if (currentMouseDraggingPoint != null && (Globals.toolbarMode == Constants.ToolbarMode.INPUT_SYMBOL 
				&& (Globals.inputSymbolMode == Constants.InputSymbolMode.X_RAY_CIRCLE 
				|| Globals.inputSymbolMode == Constants.InputSymbolMode.CRIMPING_PLEATING))) {
			Point2D.Double sp = new Point2D.Double();
			Point2D.Double ep = new Point2D.Double();
			try {
				affineTransform.inverseTransform(preMousePoint, sp);
				affineTransform.inverseTransform(currentMouseDraggingPoint, ep);
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}

			g2d.setStroke(Config.STROKE_SELECTED);

			double width = ep.x - sp.x;
			double height = ep.y - sp.y;
			if (width > height) {
				g2d.draw(new Ellipse2D.Double(sp.x, sp.y, width, width));
			} else if (height > width) {
				g2d.draw(new Ellipse2D.Double(sp.x, sp.y, height, height));
			}
		}
	}

	private void renderAllOriLeaderBoxes() {
		for (OriLeaderBox l : Origrammer.diagram.steps.get(Globals.currentStep).leaderBoxSymbols) {
			if (l.isSelected() || selectedCandidateLeader == l) {
				g2d.setColor(Config.LINE_COLOR_SELECTED);
				g2d.setStroke(Config.STROKE_EDGE);
				l.getLabel().setBorder(new EtchedBorder(BevelBorder.RAISED, Color.GREEN, getBackground().brighter()));
			} else {
				g2d.setColor(Config.LINE_COLOR_EDGE);
				g2d.setStroke(Config.STROKE_EDGE);
				l.getLabel().setBorder(new EtchedBorder(BevelBorder.RAISED, getBackground().darker(), getBackground().brighter()));
			}

			if (l.getType() == OriLeaderBox.TYPE_LEADER) {
				l.getLabel().setBorder(BorderFactory.createEmptyBorder());
			}

			add(l.getLabel());
			g2d.draw(new Line2D.Double(l.line.getP0().x, l.line.getP0().y, l.line.getP1().x, l.line.getP1().y));
		}
	}

	private void renderAllOriGeomSymbols() {
		for (OriGeomSymbol gs : Origrammer.diagram.steps.get(Globals.currentStep).geomSymbols) {		
			if (gs.isSelected() || selectedCandidateGS == gs) {
				g2d.setColor(Config.LINE_COLOR_SELECTED);
				g2d.setStroke(Config.STROKE_EDGE);
			} else {
				g2d.setColor(Config.LINE_COLOR_EDGE);
				g2d.setStroke(Config.STROKE_EDGE);
			}

			ArrayList<Shape> shapes = gs.getShapesForDrawing();
			System.out.println("SHAPES END: " + shapes.toString());

			if (gs.getType() == OriGeomSymbol.TYPE_ROTATION) {
				Font oldFont = getFont();
				g2d.setFont(new Font("Arial", Font.PLAIN, 35));
				String text = gs.getText();
				int textWidth = g2d.getFontMetrics().stringWidth(text);
				g2d.drawString(text, (int) (gs.getPosition().x-textWidth/2+4), (int) (gs.getPosition().y+13)); 
				g2d.setFont(oldFont);
			}

			for (Shape s : shapes) {
				if (gs.getType() == OriGeomSymbol.TYPE_CLOSED_SINK) {
					g2d.draw(s);
					g2d.fill(s);
				} else {
					g2d.draw(s);
				}	
			}
		}
	}


	private void renderAllEquDistSymbols() {
		for (OriEqualDistSymbol eds : Origrammer.diagram.steps.get(Globals.currentStep).equalDistSymbols) {
			if (eds.isSelected() || selectedCandidateEDS == eds) {
				g2d.setStroke(Config.STROKE_EDGE);
				g2d.setColor(Color.GREEN);
			} else {
				g2d.setStroke(Config.STROKE_EDGE);
				g2d.setColor(Color.BLACK);
			}
			ArrayList<Shape> shapes = eds.getShapesForDrawing();
			for (Shape s : shapes) {
				g2d.draw(s);
			}
		}
	}

	private void renderAllEquAnglSymbols() {
		for (OriEqualAnglSymbol eas : Origrammer.diagram.steps.get(Globals.currentStep).equalAnglSymbols) {
			if (eas.isSelected() || selectedCandidateEAS == eas) {
				g2d.setStroke(Config.STROKE_EDGE);
				g2d.setColor(Color.GREEN);
			} else {
				g2d.setStroke(Config.STROKE_EDGE);
				g2d.setColor(Color.BLACK);
			}

			ArrayList<Shape> shapes = eas.getShapesForDrawing();
			for (Shape s : shapes) {
				g2d.draw(s);
			}
		}
	}

	private void renderAllCrimpsPleats() {
		for (OriPleatCrimpSymbol crimpPleat : Origrammer.diagram.steps.get(Globals.currentStep).pleatCrimpSymbols) {
			if (crimpPleat.isSelected() || selectedCandidatePleat == crimpPleat) {
				g2d.setStroke(Config.STROKE_EDGE);
				g2d.setColor(Color.GREEN);
			} else {
				g2d.setStroke(Config.STROKE_EDGE);
				g2d.setColor(Color.BLACK);
			}

			ArrayList<Shape> shapes = crimpPleat.getShapesForDrawing();
			for (Shape s : shapes) {
				g2d.draw(s);
			}
		}
	}

	private void renderAllVertices() {
		if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_VERTEX || Globals.dispVertex) {
			g2d.setColor(Color.BLACK);
			double vertexDrawSize = 3.0;
			for (OriLine line : Origrammer.diagram.steps.get(Globals.currentStep).lines) {
				Vector2d v0 = line.getP0();
				Vector2d v1 = line.getP1();
				g2d.fill(new Rectangle2D.Double(v0.x - vertexDrawSize / Globals.SCALE,
						v0.y - vertexDrawSize / Globals.SCALE, vertexDrawSize * 2 / Globals.SCALE,
						vertexDrawSize * 2 / Globals.SCALE));
				g2d.fill(new Rectangle2D.Double(v1.x - vertexDrawSize / Globals.SCALE,
						v1.y - vertexDrawSize / Globals.SCALE, vertexDrawSize * 2 / Globals.SCALE,
						vertexDrawSize * 2 / Globals.SCALE));
			}

			for (OriVertex v : Origrammer.diagram.steps.get(Globals.currentStep).vertices) {
				if (v.isSelected() || v.getP() == selectedCandidateV) {
					g2d.setColor(Config.LINE_COLOR_SELECTED);
					vertexDrawSize = 5.0;
				} else {
					vertexDrawSize = 3.0;
					g2d.setColor(Config.LINE_COLOR_EDGE);
				}
				g2d.fill(new Rectangle2D.Double(v.getP().x - vertexDrawSize / Globals.SCALE,
						v.getP().y - vertexDrawSize / Globals.SCALE, vertexDrawSize * 2 / Globals.SCALE,
						vertexDrawSize * 2 / Globals.SCALE));
			}
		}
	}

	private void renderRectSelection() {
		if (currentMouseDraggingPoint != null && (Globals.toolbarMode == Constants.ToolbarMode.SELECTION_TOOL)) {
			Point2D.Double sp = new Point2D.Double();
			Point2D.Double ep = new Point2D.Double();
			try {
				affineTransform.inverseTransform(preMousePoint, sp);
				affineTransform.inverseTransform(currentMouseDraggingPoint, ep);
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}

			g2d.setStroke(Config.STROKE_EDGE);
			g2d.setColor(Color.BLACK);
			double sx = Math.min(sp.x, ep.x);
			double sy = Math.min(sp.y, ep.y);
			double w = Math.abs(sp.x - ep.x);
			double h = Math.abs(sp.y - ep.y);
			g2d.draw(new Rectangle2D.Double(sx, sy, w, h));
		}
	}

	//#######################################################################################
	//#######################################################################################

	private void drawGrid(Graphics2D g2d) {
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.setStroke(Config.STROKE_GRID);

		int lineNum = Globals.gridDivNum;
		double step = Origrammer.diagram.steps.get(Globals.currentStep).size / lineNum;
		double paperSize = Origrammer.diagram.paperSize;

		for (int i = 0; i < lineNum * 2 + 1; i++) {
			g2d.draw(new Line2D.Double(step * i - paperSize, -paperSize, 
					step * i - paperSize, paperSize));
			g2d.draw(new Line2D.Double(-paperSize, step * i - paperSize,
					paperSize, step * i - paperSize));
		}
	}

	public void setDispGrid(boolean dispGrid) {
		this.dispGrid = dispGrid;
		resetPickedElements();
		repaint();
	}

	public void modeChanged() {
		resetPickedElements();
		repaint();
	}

	public void resetPickedElements() {
		firstSelectedV = null;
		secondSelectedV = null;
		thirdSelectedV = null;
		selectedCandidateV = null;
		firstSelectedL = null;
		secondSelectedL = null;
		thirdSelectedL = null;
		selectedCandidateL = null;
		selectedCandidateA = null;
		selectedCandidateLeader = null;
	}

	//update the AffineTransform
	private void updateAffineTransform(Graphics2D g2d) {
		affineTransform.setToTranslation(getWidth() * 0.5 + transX, getHeight() * 0.5 + transY);
		affineTransform.scale(Globals.SCALE, Globals.SCALE);
		//affineTransform.setToTranslation(Constants.DEFAULT_PAPER_SIZE, Constants.DEFAULT_PAPER_SIZE);
		g2d.transform(affineTransform);
	}

	public void resetView() {
		transX = 0;
		transY = 0;
		Globals.SCALE = 1.0;
		updateAffineTransform(g2d);
		repaint();
	}

	//#######################################################################################
	//#######################################################################################

	private Vector2d pickVertex(Point2D.Double p) {
		double minDistance = Double.MAX_VALUE;
		Vector2d minPosition = new Vector2d();

		for (OriLine line : Origrammer.diagram.steps.get(Globals.currentStep).lines) {
			double dist0 = p.distance(line.getP0().x, line.getP0().y);
			if(dist0 < minDistance) {
				minDistance = dist0;
				minPosition.set(line.getP0());
			}
			double dist1 = p.distance(line.getP1().x, line.getP1().y);
			if (dist1 < minDistance) {
				minDistance = dist1;
				minPosition.set(line.getP1());
			}
		}

		for (OriVertex v : Origrammer.diagram.steps.get(Globals.currentStep).vertices) {
			double dist = p.distance(v.getP().x, v.getP().y);
			if (dist < minDistance) {
				minDistance = dist;
				minPosition.set(v.getP());
			}
		}

		if (dispGrid) {	
			int lineNum = Globals.gridDivNum;
			double step = Origrammer.diagram.steps.get(Globals.currentStep).size / lineNum;
			double paperSize = Origrammer.diagram.paperSize;        	

			for (int ix = -2; ix < lineNum * 2 - 1; ix++) {
				for (int iy = -2; iy < lineNum * 2 - 1; iy++) {
					double x = -paperSize / 2 + step * ix;
					double y = -paperSize / 2 + step * iy;
					double dist = p.distance(x, y);

					if (dist < minDistance) {
						minDistance = dist;
						minPosition.set(x, y);
					}
				}
			}
		}

		if (minDistance < 10.0 / Globals.SCALE) {
			return minPosition;
		} else {
			return null;
		}
	}

	private OriLine pickLine(Point2D.Double p) {
		double minDistance = Double.MAX_VALUE;
		OriLine bestLine = null;

		for(OriLine line : Origrammer.diagram.steps.get(Globals.currentStep).lines) {
			double dist = GeometryUtil.DistancePointToSegment(new Vector2d(p.x, p.y), line.getP0(), line.getP1());
			if (dist < minDistance) {
				minDistance = dist;
				bestLine = line;
			}
		}

		if (minDistance / Globals.SCALE < 10) {
			return bestLine;
		} else {
			return null;
		}
	}

	private OriArrow pickArrow(Point2D.Double p) {
		OriArrow bestArrow = null;

		for (OriArrow arrow : Origrammer.diagram.steps.get(Globals.currentStep).arrows) {
			boolean pickedA = GeometryUtil.isMouseOverShapes(p, arrow.getShapesForDrawing());
			if (pickedA) {
				bestArrow = arrow;
			}
		}
		if (bestArrow != null) {
			return bestArrow;
		} else {
			return null;
		}
	}
	
	private OriGeomSymbol pickOriSymbol(Point2D.Double p ) {
		OriGeomSymbol bestSymbol = null;
		
		for (OriGeomSymbol symbol : Origrammer.diagram.steps.get(Globals.currentStep).geomSymbols) {
			boolean pickedS = GeometryUtil.isMouseOverShapes(p, symbol.getShapesForDrawing());
			if (pickedS) {
				bestSymbol = symbol;
			}
		}
		if (bestSymbol != null) {
			return bestSymbol;
		} else {
			return null;
		}
	}

	private OriFace pickFace(Point2D.Double p) {
		OriFace bestFace = null;

		for (OriFace face : Origrammer.diagram.steps.get(Globals.currentStep).filledFaces) {
			boolean pickedF = GeometryUtil.isMouseOverFace(p, face);
			if (pickedF) {
				bestFace = face;
			}
		}

		if (bestFace != null) {
			return bestFace;
		} else {
			return null;
		}
	}

	private OriLeaderBox pickLeader(Point2D.Double p) {
		double minDistance = Double.MAX_VALUE;
		OriLeaderBox bestLeader = null;
		for (OriLeaderBox leader : Origrammer.diagram.steps.get(Globals.currentStep).leaderBoxSymbols) {

			if (GeometryUtil.isMouseOverRectangle(p, leader.getLabel().getBounds())) {
				return leader;
			} else {
				double dist = GeometryUtil.DistancePointToSegment(new Vector2d(p.x, p.y), leader.line.getP0(), leader.line.getP1());
				if (dist < minDistance) {
					minDistance = dist;
					bestLeader = leader;
				}
			}
		}

		if (minDistance / Globals.SCALE < 10) {
			return bestLeader;
		} else {
			return null;
		}
	}

	private OriGeomSymbol  pickGeomSymbol(Point2D.Double p) {
		OriGeomSymbol bestSymbol = null;

		for (OriGeomSymbol symbol : Origrammer.diagram.steps.get(Globals.currentStep).geomSymbols) {
			boolean pickedS = GeometryUtil.isMouseOverShapes(p, symbol.getShapesForDrawing());
			if (pickedS) {
				bestSymbol = symbol;
			}
		}
		if (bestSymbol != null) {
			return bestSymbol;
		} else {
			return null;
		}
	}

	private OriEqualDistSymbol pickEqualDistSymbol(Point2D.Double p) {
		OriEqualDistSymbol bestSymbol = null;

		for (OriEqualDistSymbol symbol : Origrammer.diagram.steps.get(Globals.currentStep).equalDistSymbols) {
			boolean pickedS = GeometryUtil.isMouseOverEqualDistSymbol(p, symbol);
			if (pickedS) {
				bestSymbol = symbol;
			}
		}
		if (bestSymbol != null) {
			return bestSymbol;
		} else {
			return null;
		}
	}

	private OriEqualAnglSymbol pickEqualAnglSymbol(Point2D.Double p) {
		OriEqualAnglSymbol bestSymbol = null;

		for (OriEqualAnglSymbol symbol : Origrammer.diagram.steps.get(Globals.currentStep).equalAnglSymbols) {
			boolean pickedS = GeometryUtil.isMouseOverShapes(p, symbol.getShapesForDrawing());
			if (pickedS) {
				bestSymbol = symbol;
			}
		}
		if (bestSymbol != null) {
			return bestSymbol;
		} else {
			return null;
		}
	}

	private OriPleatCrimpSymbol pickPleatSymbol(Point2D.Double p) {
		OriPleatCrimpSymbol bestSymbol = null;

		for (OriPleatCrimpSymbol symbol : Origrammer.diagram.steps.get(Globals.currentStep).pleatCrimpSymbols) {
			boolean pickedS = GeometryUtil.isMouseOverRectangle(p, symbol.getHitbox());
			if (pickedS) {
				bestSymbol = symbol;
			}
		}
		if (bestSymbol != null) {
			return bestSymbol;
		} else {
			return null;
		}
	}

	//#######################################################################################
	//#######################################################################################

	
	private void createOnePointInput(MouseEvent e, Point2D.Double clickPoint, String methodString) {
		Vector2d v = null;
			if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
				OriLine l = pickLine(clickPoint);
				if (l != null) {
					v = new Vector2d();
					Vector2d cp = new Vector2d(clickPoint.x, clickPoint.y);
					GeometryUtil.DistancePointToSegment(cp, l.getP0(), l.getP1(), v);
				}
			}
		if (v == null) {
			v = new Vector2d(clickPoint.x, clickPoint.y);
		}
		
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;

				invokeOneInputMethod(methodString);
				firstSelectedV = null;
			}
		}
	}
	
	private void createTwoPointInput(Point2D.Double clickPoint, String methodString) {
		Vector2d v = new Vector2d(clickPoint.x, clickPoint.y);
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;
			} else if (secondSelectedV == null) {
				secondSelectedV = v;

				invokeTwoInputMethod(methodString);
				firstSelectedV = null;
				secondSelectedV = null;
			}
		}
	}
	
	private void createThreePointInput(Point2D.Double clickPoint, String methodString) {
		Vector2d v = new Vector2d(clickPoint.x, clickPoint.y);
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;
			} else if (secondSelectedV == null) {
				secondSelectedV = v;
			} else if (thirdSelectedV == null) {
				thirdSelectedV = v;
				
				invokeThreeInputMethod(methodString);
				firstSelectedV = null;
				secondSelectedV = null;
				thirdSelectedV = null;
			}
		}
	}
	
	private void createOneVertexInput(MouseEvent e, Point2D.Double clickPoint, String methodString) {
		Vector2d v = pickVertex(clickPoint);
		if (v == null) {
			if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
				OriLine l = pickLine(clickPoint);
				if (l != null) {
					v = new Vector2d();
					Vector2d cp = new Vector2d(clickPoint.x, clickPoint.y);
					double okay = GeometryUtil.DistancePointToSegment(cp, l.getP0(), l.getP1(), v);
					System.out.println("okay: " + okay);
				}
			}
		}
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;

				invokeOneInputMethod(methodString);
				firstSelectedV = null;
			}
		}
	}
	
	private void createTwoVertexInput(MouseEvent e, Point2D.Double clickPoint, String methodString) {
		Vector2d v = pickVertex(clickPoint);
		if (v == null) {
			if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
				OriLine l = pickLine(clickPoint);
				if (l != null) {
					v = new Vector2d();
					Vector2d cp = new Vector2d(clickPoint.x, clickPoint.y);
					GeometryUtil.DistancePointToSegment(cp, l.getP0(), l.getP1(), v);
				}
			}
		}
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;
			} else if (secondSelectedV == null) {
				secondSelectedV = v;

				invokeTwoInputMethod(methodString);
				firstSelectedV = null;
				secondSelectedV = null;
			}
		}
	}
	
	private void createThreeVertexInput(MouseEvent e, Point2D.Double clickPoint, String methodString) {
		Vector2d v = pickVertex(clickPoint);
		if (v == null) {
			if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
				OriLine l = pickLine(clickPoint);
				if (l != null) {
					v = new Vector2d();
					Vector2d cp = new Vector2d(clickPoint.x, clickPoint.y);
					GeometryUtil.DistancePointToSegment(cp, l.getP0(), l.getP1(), v);
				}
			}
		}
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;
			} else if (secondSelectedV == null) {
				secondSelectedV = v;
			} else if (thirdSelectedV == null) {
				thirdSelectedV = v;
				
				invokeThreeInputMethod(methodString);
				firstSelectedV = null;
				secondSelectedV = null;
				thirdSelectedV = null;
			}
		}
	}
	
	/**
	 * Invokes the Method named <code>methodString</code> with 1 input parameters
	 * @param methodString the name of the method that is to be invoked
	 */
	private void invokeOneInputMethod(String methodString) {
		Class<?>[] paramTypes = {Vector2d.class};
		Method method = null;
		try {
			method = MainScreen.class.getDeclaredMethod(methodString, paramTypes);
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		} catch (SecurityException e1) {
			e1.printStackTrace();
		}
		
		try {
			@SuppressWarnings("unused")
			String test = (String) method.invoke(this, firstSelectedV);
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Invokes the Method named <code>methodString</code> with 2 input parameters
	 * @param methodString the name of the method that is to be invoked
	 */
	private void invokeTwoInputMethod(String methodString) {
		Class<?>[] paramTypes = {Vector2d.class, Vector2d.class};
		Class<?>[] paramTypesLine = {Vector2d.class, Vector2d.class, int.class};
		
		if (methodString.equals("createOriLine")) {
			Method method = null;

				try {
					method = MainScreen.class.getMethod(methodString, paramTypesLine);
				} catch (NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
				try {
					String test = (String) method.invoke(this, firstSelectedV, secondSelectedV, OriLine.TYPE_NONE);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
		} else {
			Method method2 = null;
				try {
					method2 = MainScreen.class.getDeclaredMethod(methodString, paramTypes);
				} catch (NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
				try {
					String test = (String) method2.invoke(this, firstSelectedV, secondSelectedV);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
		}
	}
	
	/**
	 * Invokes the Method named <code>methodString</code> with 3 input parameters
	 * @param methodString the name of the method that is to be invoked
	 */
	private void invokeThreeInputMethod(String methodString) {
		Class<?>[] paramTypes = {Vector2d.class, Vector2d.class, Vector2d.class};
		Method method = null;
		try {
			method = MainScreen.class.getDeclaredMethod(methodString, paramTypes);
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		} catch (SecurityException e1) {
			e1.printStackTrace();
		}
		
		try {
			@SuppressWarnings("unused")
			String test = (String) method.invoke(this, firstSelectedV, secondSelectedV, thirdSelectedV);
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Creates an OriLine that spans from v1 to v2
	 * @param v1
	 * @param v2
	 */
	public void createOriLine(Vector2d v1, Vector2d v2, int type) {
		if (type == OriLine.TYPE_NONE) {
			type = Globals.inputLineType;
		} 
		OriLine line = new OriLine(v1, v2, type);
		if (Globals.inputLineType == OriLine.TYPE_CREASE) {
			if (Origrammer.mainFrame.uiTopPanel.startCreaseCB.isSelected()) {
				line.setStartOffset(true);
			} else {
				line.setStartOffset(false);
			}
			if (Origrammer.mainFrame.uiTopPanel.endCreaseCB.isSelected()) {
				line.setEndOffset(true);
			} else {
				line.setEndOffset(false);
			}
		}
		Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
		Origrammer.diagram.steps.get(Globals.currentStep).addLine(line);

		if (Globals.automatedArrowPlacement) {
			autoCreateOriArrow(v1, v2);
		}
	}
	
	/**
	 * Creates an angle bisector between two OriLines
	 * @param clickPoint 
	 */
	public void createOriLineAngleBisector(Point2D.Double clickPoint) {
		OriLine l = pickLine(clickPoint);

		if (l != null) {
			if (firstSelectedL == null) {
				firstSelectedL = l;
			} else if (secondSelectedL == null) {
				secondSelectedL = l;
				Vector2d crossPoint = GeometryUtil.getCrossPoint(firstSelectedL, secondSelectedL);

				if (crossPoint == null) {
					JOptionPane.showMessageDialog(this,  Origrammer.res.getString("Error_NoCrossPointFound"),
							"Error_NoCrossPointFound", JOptionPane.ERROR_MESSAGE);
				} else {
					Vector2d uv1 = null;
					Vector2d uv2 = null;

					//check if the crossPoint is firstSelectedL.P1 or firstSelected.P2 and get it's UV
					if (GeometryUtil.closeCompare(crossPoint.x, firstSelectedL.getP0().x, Constants.EPSILON) 
							&& GeometryUtil.closeCompare(crossPoint.y, firstSelectedL.getP0().y, Constants.EPSILON)) {
						uv1 = GeometryUtil.getUnitVector(firstSelectedL.getP0(), firstSelectedL.getP1());
					} else {
						uv1 = GeometryUtil.getUnitVector(firstSelectedL.getP1(), firstSelectedL.getP0());
					}

					//check if the crossPoint is secondSelectedL.P1 or secondSelectedL.P2 and get it's UV
					if (GeometryUtil.closeCompare(crossPoint.x, secondSelectedL.getP0().x, Constants.EPSILON) 
							&& GeometryUtil.closeCompare(crossPoint.y, secondSelectedL.getP0().y, Constants.EPSILON)) {
						uv2 = GeometryUtil.getUnitVector(secondSelectedL.getP0(), secondSelectedL.getP1());
					} else {
						uv2 = GeometryUtil.getUnitVector(secondSelectedL.getP1(), secondSelectedL.getP0());
					}

					Vector2d combinedL1L2Vector = new Vector2d(crossPoint.x + uv1.x + uv2.x, crossPoint.y + uv1.y + uv2.y);
					Vector2d newUV = GeometryUtil.getUnitVector(crossPoint, combinedL1L2Vector);
					Vector2d bestCrossPoint = getEarlierstCrossPoint(crossPoint, newUV);
					if (bestCrossPoint == null) {
						JOptionPane.showMessageDialog(this,  Origrammer.res.getString("Error_NoCrossPointFound"),
								"Error_NoCrossPointFound", JOptionPane.ERROR_MESSAGE);
					} else {
						createOriLine(crossPoint, bestCrossPoint, OriLine.TYPE_NONE);
					}
				}
				firstSelectedL = null;
				secondSelectedL = null;
			}
		}
	}
	
	/**
	 * Returns the closest {@code crossingPoint} of a line with origin on {@code p1} and direction {@code uv}
	 * @param p1
	 * @param uv
	 * @return
	 */
	private Vector2d getEarlierstCrossPoint(Vector2d p1, Vector2d uv) {
		double dist = 0;
		double smallestDist = 1000; //TODO: make it more elegant and not with fixed value
		Vector2d bestCrossPoint = null;

		//check all OriLines for the earliest intersection with the new AngleBisectorLine
		//set the first intersection as P2 of the AngleBisectorLine
		for (OriLine tmpLine : Origrammer.diagram.steps.get(Globals.currentStep).lines) {

			Vector2d crossPoint2 = GeometryUtil.getCrossPoint(tmpLine, new OriLine(p1, new Vector2d(p1.x + uv.x * 900, p1.y + uv.y * 900), Globals.inputLineType));
			if (crossPoint2 != null) {
				if (!crossPoint2.equals(p1)) {
					//check if crossPoint2 is too close to crossPoint
					if (!(GeometryUtil.closeCompare(p1.x, crossPoint2.x, Constants.EPSILON) 
							&& GeometryUtil.closeCompare(p1.y, crossPoint2.y, Constants.EPSILON))) {
						dist = GeometryUtil.Distance(p1, crossPoint2);
						if (dist < smallestDist) {
							smallestDist = dist;
							bestCrossPoint = crossPoint2;
						}
					}
				}
			}
		}
		return bestCrossPoint;
	}
	
	public void createOriLinePerpendicular(Point2D.Double clickPoint) {
		OriLine l = pickLine(clickPoint);

		if (l != null) {
			Vector2d uv = GeometryUtil.getUnitVector(l.getP0(), l.getP1());
			Vector2d nv = GeometryUtil.getNormalVector(uv);
			Vector2d v = pickVertex(clickPoint);
			Vector2d v2 = null;
			//get point on OriLine that is closest to currentMousePointLogic, 
			//if currentMousePointLogic is not close to a vertex
			if (v == null) {
				v = new Vector2d();
				Vector2d cp = new Vector2d(clickPoint.x, clickPoint.y);
				GeometryUtil.DistancePointToSegment(cp,  l.getP0(), l.getP1(), v);
			}
			//get crossingPoint in order to get inputLine.P1
			//if there is no crossing point -> reverse inputLine direction
			v2 = getEarlierstCrossPoint(v, nv);
			if (v2 == null) {
				v2 = getEarlierstCrossPoint(v, new Vector2d(-nv.x, -nv.y));
			}
			if (v != null && v2 != null) {
				createOriLine(v, v2, OriLine.TYPE_NONE);
			}
		}

	}
	
	public void createTriangleInsector(Vector2d v1, Vector2d v2, Vector2d v3) {
		//creates Insector of the triangle with edge points {firstSelectedV, secondSelectedV, v}
		Vector2d incenter = GeometryUtil.getIncenter(v1, v2, v3);
		if (incenter == null) {
			System.out.println("Failed to calculate the incenter of the triangle");
		} else {
			createOriLine(incenter, v1, OriLine.TYPE_NONE);
			createOriLine(incenter, v2, OriLine.TYPE_NONE);
			createOriLine(incenter, v3, OriLine.TYPE_NONE);
		}
	}
	
	public void createOriLineLengthAngle(Vector2d v1) {
		if (Origrammer.mainFrame.uiTopPanel.inputLineLengthTF.getText().length() > 0 
			&& Origrammer.mainFrame.uiTopPanel.inputLineAngleTF.getText().length() > 0) {
			String lengthString = Origrammer.mainFrame.uiTopPanel.inputLineLengthTF.getText();
			String angleString = Origrammer.mainFrame.uiTopPanel.inputLineAngleTF.getText();
			
			Double length = Double.parseDouble(lengthString);
			Double angle = Double.parseDouble(angleString);
			
			angle = Math.toRadians(angle);
			Vector2d v2 = new Vector2d(length * Math.cos(angle) + v1.x, length * Math.sin(angle) + v1.y);
			
			createOriLine(v1, v2, OriLine.TYPE_NONE);
		} else {
			JOptionPane.showMessageDialog(this,  Origrammer.res.getString("Error_NoLengthAngleSpecified"),
					"Error_NoLengthAngleSpecified", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Automatically creates a fitting OriLine after placing an OriArrow
	 * @param v1 Vertex1 from the OriArrow
	 * @param v2 Vertex2 from the OriArrow
	 */
	private void autoCreateOriLine(Vector2d v1, Vector2d v2, int type) {
		double length = GeometryUtil.Distance(v1, v2);
		Vector2d uv = GeometryUtil.getUnitVector(v1, v2);
		Vector2d nv = GeometryUtil.getNormalVector(uv);
		double halfLength = 0.5* length;
		
		Vector2d middleP = new Vector2d(v1.x + uv.x * halfLength, v1.y + uv.y * halfLength);
		
		Vector2d lineV1 = new Vector2d(middleP.x + nv.x * halfLength, middleP.y + nv.y * halfLength);
		Vector2d lineV2 = new Vector2d(middleP.x - nv.x * halfLength, middleP.y - nv.y*halfLength);
		createOriLine(lineV1, lineV2, type);
	}
	
	
	public void createOriArrow(Vector2d v1, Vector2d v2) {
		OriArrow tmpArrow = new OriArrow();

		tmpArrow.setP0(v1);
		tmpArrow.setP1(v2);
		tmpArrow.setType(Globals.inputArrowType);
		tmpArrow.setSelected(false);

		Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
		Origrammer.diagram.steps.get(Globals.currentStep).addArrow(tmpArrow);
		
		if (Globals.automatedLinePlacement) {
			if (tmpArrow.getType() == OriArrow.TYPE_VALLEY) {
				autoCreateOriLine(v1, v2, OriLine.TYPE_VALLEY);
			} else if (tmpArrow.getType() == OriArrow.TYPE_MOUNTAIN) {
				autoCreateOriLine(v1, v2, OriLine.TYPE_MOUNTAIN);
			}
		}

	}
	
	/**
	 * Automatically creates a fitting OriArrow after placing an OriLine
	 * @param v1 Vertex1 from the OriLine
	 * @param v2 Vertex2 from the OriLine
	 */
	private void autoCreateOriArrow(Vector2d v1, Vector2d v2) {
		double length = GeometryUtil.Distance(v1, v2);
		Vector2d uv = GeometryUtil.getUnitVector(v1, v2);
		Vector2d nv = GeometryUtil.getNormalVector(uv);
		double halfLength = 0.5 * length;
		
		Vector2d middleP = new Vector2d(v1.x + uv.x * halfLength, v1.y + uv.y * halfLength);
		
		Vector2d arrowV1 = new Vector2d(middleP.x + nv.x * halfLength, middleP.y + nv.y * halfLength);
		Vector2d arrowV2 = new Vector2d(middleP.x - nv.x * halfLength, middleP.y - nv.y * halfLength);
		createOriArrow(arrowV1, arrowV2);
	}
	
	private void createRotationSymbol(Vector2d v1) {
		OriGeomSymbol tmpSymbol = new OriGeomSymbol();
		tmpSymbol.setPosition(v1);
		tmpSymbol.setType(OriGeomSymbol.TYPE_ROTATION);

		if (Origrammer.mainFrame.uiTopPanel.rotationTF.getText().length() == 0) {
			JOptionPane.showMessageDialog(this,  Origrammer.res.getString("Error_EmptyRotationTextField"),
					"Error_EmptyRotationTextField", JOptionPane.ERROR_MESSAGE);
		} else {
			tmpSymbol.setSize(80); //TODO: adjustmentSlider on TopPanel
			tmpSymbol.setText(Origrammer.mainFrame.uiTopPanel.rotationTF.getText() + "°");
			tmpSymbol.setReversed(Origrammer.mainFrame.uiTopPanel.reverseRotSymbol.isSelected());
			Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
			Origrammer.diagram.steps.get(Globals.currentStep).addGeomSymbol(tmpSymbol);
		}
	}
	
	private void createHoldSymbol(Vector2d v1) {
		OriGeomSymbol tmpSymbol = new OriGeomSymbol();
		tmpSymbol.setPosition(v1);
		tmpSymbol.setType(OriGeomSymbol.TYPE_HOLD);
		tmpSymbol.setSize(45); //TODO: adjustmentSlider on TopPanel
		Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
		Origrammer.diagram.steps.get(Globals.currentStep).addGeomSymbol(tmpSymbol);
	}
	
	private void createHoldAndPullSymbol(Vector2d v1, Vector2d v2) {
		OriGeomSymbol tmpSymbol = new OriGeomSymbol();
		tmpSymbol.setPosition(firstSelectedV);
		tmpSymbol.setType(OriGeomSymbol.TYPE_HOLD_AND_PULL); 
		Vector2d direction = GeometryUtil.getUnitVector(v1, v2);
		tmpSymbol.setDirection(direction); //TODO: adjustmentSlider on TopPanel
		tmpSymbol.setSize(45); //TODO: adjustmentSlider on TopPanel
		Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
		Origrammer.diagram.steps.get(Globals.currentStep).addGeomSymbol(tmpSymbol);
	}
	
	private void createClosedSinkSymbol(Vector2d v1) {

		OriGeomSymbol tmpSymbol = new OriGeomSymbol();
		tmpSymbol.setPosition(v1);
		tmpSymbol.setType(OriGeomSymbol.TYPE_CLOSED_SINK);
		tmpSymbol.setSize(10); //TODO: adjustmentSlider on TopPanel
		Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
		Origrammer.diagram.steps.get(Globals.currentStep).addGeomSymbol(tmpSymbol);
	}
	
	private void createNextViewHereSymbol(Vector2d v1, Vector2d v2) {
		OriGeomSymbol tmpSymbol = new OriGeomSymbol();
		tmpSymbol.setPosition(v1);
		tmpSymbol.setType(OriGeomSymbol.TYPE_NEXT_VIEW_HERE);
		Vector2d direction = GeometryUtil.getUnitVector(v1, v2);
		tmpSymbol.setDirection(direction); //TODO: adjustmentSlider on TopPanel
		tmpSymbol.setSize(100); //TODO: adjustmentSlider on TopPanel
		Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
		Origrammer.diagram.steps.get(Globals.currentStep).addGeomSymbol(tmpSymbol);
	}
	
	private void createXRayCircle() {
		Point2D.Double sp = new Point2D.Double();
		Point2D.Double ep = new Point2D.Double();
		try {
			affineTransform.inverseTransform(preMousePoint, sp);
			affineTransform.inverseTransform(currentMouseDraggingPoint, ep);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		double width = ep.x - sp.x;
		double height = ep.y - sp.y;
		OriGeomSymbol tmpGeomS = new OriGeomSymbol();
		tmpGeomS.setPosition(new Vector2d(sp.x, sp.y));
		tmpGeomS.setType(OriGeomSymbol.TYPE_XRAY_CIRCLE);
		if (width > height) {
			tmpGeomS.setSize(width);
		} else if (height > width) {
			tmpGeomS.setSize(height);
		}
		
		Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
		Origrammer.diagram.steps.get(Globals.currentStep).addGeomSymbol(tmpGeomS);
	}

	/**
	 * Creates a filled face once 3 vertices are selected
	 * @param clickPoint
	 */
	private void createFilledFace(Point2D.Double clickPoint) {
		//creates OriFace that is to be filled with DEFAULT_PAPER_COLOR --> OriFace is a triangle with 3 OriLines as sides			
		Vector2d v = pickVertex(clickPoint);
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;
			} else if (secondSelectedV == null) {
				secondSelectedV = v;
			} else {
				ArrayList<Vector2d> vList = new ArrayList<>();

				vList.add(v);
				vList.add(firstSelectedV);
				vList.add(secondSelectedV);
				OriFace newFace;
				GeneralPath pathForFilledFace = GeometryUtil.createPathFromVertices(vList);
				if (Globals.faceInputDirection == Constants.FaceInputDirection.FACE_UP) {
					newFace = new OriFace(pathForFilledFace, false, true);
				} else {
					newFace = new OriFace(pathForFilledFace, false, false);
				}
				Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
				Origrammer.diagram.steps.get(Globals.currentStep).addFilledFace(newFace);
				firstSelectedV = null;
				secondSelectedV = null;
				thirdSelectedV = null;
			}
		}		
	}

	private void createLeaderBox(Vector2d v1, Vector2d v2) {
		OriLeaderBox tmpLeader = new OriLeaderBox();

		tmpLeader.line.setP0(v1);
		tmpLeader.line.setP1(v2);

		tmpLeader.setText(Origrammer.mainFrame.uiTopPanel.inputLeaderTextTF.getText());
		tmpLeader.setSelected(false);

		if (tmpLeader.getLabel().getText().length() == 0) {
			JOptionPane.showMessageDialog(this,  Origrammer.res.getString("Error_EmptyLeaderTextField"),
					"Error_EmptyLeaderTextField", 
					JOptionPane.ERROR_MESSAGE);
		} else {
			tmpLeader.setLabelBounds(tmpLeader.getLabelBounds(g2d));

			if (Globals.inputSymbolMode == Constants.InputSymbolMode.LEADER) {
				tmpLeader.setType(OriLeaderBox.TYPE_LEADER);
			} else if (Globals.inputSymbolMode == Constants.InputSymbolMode.REPETITION_BOX) {
				tmpLeader.setType(OriLeaderBox.TYPE_REPETITION);
			}
			Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
			Origrammer.diagram.steps.get(Globals.currentStep).addLeader(tmpLeader);
		}
	}

	private void createEqualDistSymbol(Vector2d v1, Vector2d v2) {
		OriEqualDistSymbol tmpEquDistSymbol = new OriEqualDistSymbol();

		tmpEquDistSymbol.setTranslationDist(Origrammer.mainFrame.uiTopPanel.sliderEqualDist.getValue());
		tmpEquDistSymbol.setDividerCount(Integer.parseInt(Origrammer.mainFrame.uiTopPanel.equalDistDividerTF.getText()));
		tmpEquDistSymbol.setP0(v1);
		tmpEquDistSymbol.setP1(v2);

		Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
		Origrammer.diagram.steps.get(Globals.currentStep).addEqualDistSymbol(tmpEquDistSymbol);

	}

	private void createEqualAnglSymbol(Vector2d v1, Vector2d v2, Vector2d v3) {

		Vector2d uv1 = GeometryUtil.getUnitVector(v1, v2);
		Vector2d uv2 = GeometryUtil.getUnitVector(v1, v3);

		double dist1 = GeometryUtil.Distance(v1, v2);
		double dist2 = GeometryUtil.Distance(v1, v3);

		//if Distance(v,a) and Distance(v,b) are not equal --> set both to the lower value
		if (dist1 < dist2) {
			v3.x = v1.x + uv2.x * dist1;
			v3.y = v1.y + uv2.y * dist1;

		} else if (dist2 < dist1) {
			v2.x = v1.x + uv1.x * dist2;
			v2.y = v1.y + uv1.y * dist2;
		}

		OriEqualAnglSymbol tmpEquAnglSymbol = new OriEqualAnglSymbol(v1, v2, v3);
		tmpEquAnglSymbol.setDividerCount(Integer.parseInt(Origrammer.mainFrame.uiTopPanel.equalAnglDividerTF.getText()));

		Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
		Origrammer.diagram.steps.get(Globals.currentStep).addEqualAngleSymbol(tmpEquAnglSymbol);
	}

	private void createPleatCrimpSymbol(Vector2d v1) {
		OriPleatCrimpSymbol tmpPCSymbol = new OriPleatCrimpSymbol(v1, 
				Origrammer.mainFrame.uiTopPanel.pleatCB.isSelected(),
				Integer.parseInt(Origrammer.mainFrame.uiTopPanel.pleatTF.getText()));
		if (Origrammer.mainFrame.uiTopPanel.pleatRB.isSelected()) {
			tmpPCSymbol.setType(OriPleatCrimpSymbol.TYPE_PLEAT);
		} else {
			tmpPCSymbol.setType(OriPleatCrimpSymbol.TYPE_CRIMP);
		}
		Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
		Origrammer.diagram.steps.get(Globals.currentStep).addPleatSymbol(tmpPCSymbol);
	}

	private void createVertexAbsolutePos(Vector2d v) {
		Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
		OriVertex vertex = new OriVertex(v);
		Origrammer.diagram.steps.get(Globals.currentStep).addVertex(vertex);
	}

	private void createVertexFractionOfLine(Point2D.Double clickPoint) {
		OriLine l = pickLine(clickPoint);

		if (firstSelectedL == null && (l != null)) {
			firstSelectedL = l;

			double fraction = Double.parseDouble(Origrammer.mainFrame.uiTopPanel.inputVertexFractionTF.getText());
			double dist = GeometryUtil.Distance(l.getP0(), l.getP1());
			Vector2d uv = GeometryUtil.getUnitVector(l.getP0(), l.getP1());

			double newX = l.getP0().x + uv.x * (dist * (fraction / 100)); 
			double newY = l.getP0().y + uv.y * (dist * (fraction / 100));

			Vector2d newVertex = new Vector2d(newX, newY);

			OriLine first = new OriLine(l.getP0(), newVertex, l.getType());
			OriLine second = new OriLine(newVertex, l.getP1(), l.getType());
			Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
			Origrammer.diagram.steps.get(Globals.currentStep).addLine(first);
			Origrammer.diagram.steps.get(Globals.currentStep).addLine(second);
			Origrammer.diagram.steps.get(Globals.currentStep).lines.remove(l);

		}
		firstSelectedL = null;
	}

	//#######################################################################################
	//#######################################################################################


	private void selectOnClickPoint(Point2D.Double clickPoint) {
		selectOriLine(clickPoint);
		selectOriVertex(clickPoint);
		selectOriArrow(clickPoint);
		selectOriFace(clickPoint);
		selectOriLeaderBox(clickPoint);
		selectOriGeomSymbol(clickPoint);
		selectOriEqualDistSymbol(clickPoint);
		selectOriEqualSymbol(clickPoint);
		selectOriPleatCrimpSymbol(clickPoint);
		selectOriSymbol(clickPoint);

		Origrammer.mainFrame.uiTopPanel.modeChanged();
	}
	
	private void selectOriLine(Point2D.Double clickPoint) {
		//select OriLine or unselect all OriLines if clicked on nothing
		OriLine l = pickLine(clickPoint);
		if (l != null) {
			if (!l.isSelected()) {
				l.setSelected(true);
			} else {
				l.setSelected(false);
			}
		} else {
			Origrammer.diagram.steps.get(Globals.currentStep).unselectAllLines();
		}
	}
	
	private void selectOriVertex(Point2D.Double clickPoint) {
		//select OriVertex or unselect all OriVertices if clicked on nothing
		Vector2d p = pickVertex(clickPoint);
		if (p != null) {
			for (OriVertex v : Origrammer.diagram.steps.get(Globals.currentStep).vertices) {
				if (v.getP().x == p.x && v.getP().y == p.y) {
					if (v != null) {
						if (!v.isSelected()) {
							v.setSelected(true);
						} else {
							v.setSelected(false);
						}
					} 
				} 
			}
		} else {
			Origrammer.diagram.steps.get(Globals.currentStep).unselectAllVertices();
		}
	}
	
	private void selectOriArrow(Point2D.Double clickPoint) {
		//select OriArrow or unselect all OriArrows if clicked on nothing
		OriArrow a = pickArrow(clickPoint);
		if (a != null) {
			if (!a.isSelected()) {
				a.setSelected(true);
			} else if (!isPressedOverSymbol) {
				a.setSelected(false);
			}
		} else {
			Origrammer.diagram.steps.get(Globals.currentStep).unselectAllArrows();
		}
	}
	
	private void selectOriSymbol(Point2D.Double clickPoint) {
		//select OriSymbol or unselect all OriSymbols if clicked on nothing
		OriGeomSymbol s = pickOriSymbol(clickPoint);
		if (s != null) {
			if (!s.isSelected()) {
				s.setSelected(true);
			} else if (!isPressedOverSymbol) {
				s.setSelected(false);
			}
		} else {
			Origrammer.diagram.steps.get(Globals.currentStep).unselectAllGeomSymbols();
		}
	}
	
	private void selectOriFace(Point2D.Double clickPoint) {
		//select OriFace or unselect all OriFaces if clicked on nothing
		if (Globals.dispFilledFaces) {
			OriFace f = pickFace(clickPoint);
			if (f != null) {
				if (!f.isSelected()) {
					f.setSelected(true);
				} else {
					f.setSelected(false);
				}
			} else {
				Origrammer.diagram.steps.get(Globals.currentStep).unselectAllFaces();
			}
		}
	}
	
	private void selectOriLeaderBox(Point2D.Double clickPoint) {
		//select OriLeader or unselect all OriLeader if clicked on nothing
		OriLeaderBox leader = pickLeader(clickPoint);
		if (leader != null) {
			if (!leader.isSelected()) {
				leader.setSelected(true);
			} else if (!isPressedOverSymbol) {
				leader.setSelected(false);
			}
		} else {
			Origrammer.diagram.steps.get(Globals.currentStep).unselectAllLeaders();
		}
	}
	
	private void selectOriGeomSymbol(Point2D.Double clickPoint) {
		//select OriGeomSymbol or unselect all OriGeomSymbols if clicked on nothing
		OriGeomSymbol gs = pickGeomSymbol(clickPoint);
		if (gs != null) {
			if (!gs.isSelected()) {
				gs.setSelected(true);
			} else if (!isPressedOverSymbol) {
				gs.setSelected(false);
			}
		} else {
			Origrammer.diagram.steps.get(Globals.currentStep).unselectAllGeomSymbols();
		}
	}
	
	private void selectOriEqualDistSymbol(Point2D.Double clickPoint) {
		//select OriEqualDistSymbol or unselect all OriEqualDistSymbols if clicked on nothing
		OriEqualDistSymbol eds = pickEqualDistSymbol(clickPoint);
		if (eds != null) {
			if (!eds.isSelected()) {
				eds.setSelected(true);
			} else if (!isPressedOverSymbol) {
				eds.setSelected(false);
			}
		} else {
			Origrammer.diagram.steps.get(Globals.currentStep).unselectAllEqualDistSymbols();
		}
	}
	
	private void selectOriEqualSymbol(Point2D.Double clickPoint) {
		//select OriEqualAnglSymbol or unselect all OriEqualAnglSymbols if clicked on nothing
		OriEqualAnglSymbol eas = pickEqualAnglSymbol(clickPoint);
		if (eas != null) {
			if (!eas.isSelected()) {
				eas.setSelected(true);
			} else if (!isPressedOverSymbol) {
				eas.setSelected(false);
			}
		} else {
			Origrammer.diagram.steps.get(Globals.currentStep).unselectAllEqualAnglSymbols();
		}
	}
	
	private void selectOriPleatCrimpSymbol(Point2D.Double clickPoint) {
		//select OriPleatSymbol or unselect all OriPleatSymbols if clicked on nothing
		OriPleatCrimpSymbol pleat = pickPleatSymbol(clickPoint);
		if (pleat != null) {
			if (!pleat.isSelected()) {
				pleat.setSelected(true);
			} else if (!isPressedOverSymbol) {
				pleat.setSelected(false);
			}
		} else {
			Origrammer.diagram.steps.get(Globals.currentStep).unselectAllPleatSymbols();
		}
	}
	
	private void measureLength(Point2D.Double clickPoint) {
		//MEASURE LENGTH (vertex - vertex)
		Vector2d v = pickVertex(clickPoint);
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;
			} else {
				double length = GeometryUtil.Distance(firstSelectedV, v);
				Origrammer.mainFrame.uiSidePanel.measureLengthTF.setValue(length);;
				firstSelectedV = null;
			}
		}
		//MEASURE LENGTH (OriLine.v0 - OriLine.v1)
		OriLine l = pickLine(clickPoint);
		if (v == null && l != null) {
			double length = GeometryUtil.Distance(l.getP0(), l.getP1());
			Origrammer.mainFrame.uiSidePanel.measureLengthTF.setValue(length);;
			firstSelectedL = null;
		}
	}

	private void measureAngle(Point2D.Double clickPoint) {
		//MEASURE ANGLE (vertex - vertex - vertex) --> measures angle of first selected vertex
		Vector2d v = pickVertex(clickPoint);
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;
			} else if (secondSelectedV == null) {
				secondSelectedV = v;
			} else {
				double angle = GeometryUtil.measureAngle(firstSelectedV, secondSelectedV, v);
				Origrammer.mainFrame.uiSidePanel.measureAngleTF.setValue(angle);;
				firstSelectedV = null;
				secondSelectedV = null;
			}
		}
		//MEASURE ANGLE (line - line) --> measures angle between two OriLines that share one point
		OriLine l = pickLine(clickPoint);
		if(v == null && l != null) {
			if (firstSelectedL == null) {
				firstSelectedL = l;
			} else {
				//double angle = GeometryUtil.measureAngle(firstSelectedL, l); //use for measuring the spanning angles between 2 lines
				double angle = GeometryUtil.measureAngleToXAxis(GeometryUtil.getUnitVector(firstSelectedL.getP0(), firstSelectedL.getP1()));
				Origrammer.mainFrame.uiSidePanel.measureAngleTF.setValue(Math.toDegrees(angle));
				firstSelectedL = null;
			}
		}
	}


	//############################################
	//############## MOUSE LISTENER ##############
	//############################################
	@Override
	public void mouseClicked(MouseEvent e) {
		//if right clicked, remove any selected vertices
		if (SwingUtilities.isRightMouseButton(e)) {
			if (firstSelectedV != null) {
				firstSelectedV = null;
				repaint();
			} else if (secondSelectedV != null) {
				secondSelectedV = null;
				repaint();
			} else if (thirdSelectedV != null) {
				thirdSelectedV = null;
				repaint();
			}
			Origrammer.diagram.steps.get(Globals.currentStep).unselectAll();
			return;
		}

		//get mouse click coordinates
		Point2D.Double clickPoint = new Point2D.Double();
		try {
			affineTransform.inverseTransform(e.getPoint(), clickPoint);
		} catch (Exception ex) {
			return;
		}
		
		switch(Globals.toolbarMode) {
			case INPUT_LINE:
				inputLineMode(e, clickPoint);
				break;
			case INPUT_VERTEX:
				inputVertexMode(e, clickPoint);
				break;
			case INPUT_ARROW:
				createTwoVertexInput(e, clickPoint, "createOriArrow");
				break;
			case INPUT_SYMBOL:
				createOriSymbol(e, clickPoint);
				break;
			case SELECTION_TOOL:
				selectOnClickPoint(clickPoint);
				break;
			case MEASURE_TOOL:
				measureMode(clickPoint);
				break;
			case FILL_TOOL:
				createFilledFace(clickPoint);
		}
		repaint();
	}
	
	
	private void inputLineMode(MouseEvent e, Point2D.Double clickPoint) {
		switch(Globals.lineEditMode) {
			case INPUT_LINE:
				createTwoVertexInput(e, clickPoint, "createOriLine");
				break;
			case ANGLE_BISECTOR:
				//createTwoLineInput(e, clickPoint, "createOriLineAngleBisector");
				createOriLineAngleBisector(clickPoint);
				break;
			case PERPENDICULAR:
				//createOneVertexInput(e, clickPoint, "createOriLinePerpendicular");
				createOriLinePerpendicular(clickPoint);
				break;
			case TRIANGLE_INSECTOR:
				createThreeVertexInput(e, clickPoint, "createTriangleInsector");
				//createTriangleInsector(clickPoint);
				break;
			case BY_LENGTH_AND_ANGLE:
				createOneVertexInput(e, clickPoint, "createOriLineLengthAngle");
			default:
				break;
		}
	}
	
	private void inputVertexMode(MouseEvent e, Point2D.Double clickPoint) {
		switch(Globals.vertexInputMode) {
			case ABSOLUTE:
				createOnePointInput(e, clickPoint, "createVertexAbsolutePos");
				//createVertexAbsolutePos(clickPoint);
				break;
			case FRACTION_OF_LINE:
				createVertexFractionOfLine(clickPoint);
				break;
			default:
				break;
		}
	}
	
	private void createOriSymbol(MouseEvent e, Point2D.Double clickPoint) {
		switch(Globals.inputSymbolMode) {
			case LEADER:
				createTwoPointInput(clickPoint, "createLeaderBox");
				//createLeaderBox(clickPoint);
				break;
			case REPETITION_BOX:
				createTwoPointInput(clickPoint, "createLeaderBox");
				//createLeaderBox(clickPoint);
				break;
			case EQUAL_DIST:
				createTwoVertexInput(e, clickPoint, "createEqualDistSymbol");
				//createEqualDistSymbol(clickPoint);
				break;
			case EQUAL_ANGL:
				createThreeVertexInput(e, clickPoint, "createEqualAnglSymbol");
				//createEqualAnglSymbol(clickPoint);
				break;
			case CRIMPING_PLEATING:
				createOnePointInput(e, clickPoint, "createPleatCrimpSymbol");
				//createPleatCrimpSymbol(clickPoint);
				break;
			case SINKS:
				createOnePointInput(e, clickPoint, "createClosedSinkSymbol");
				//createClosedSinkSymbol(clickPoint);
				break;
			case FOLD_OVER_AND_OVER:
				break;
			case HOLD_HERE:
				createOnePointInput(e, clickPoint, "createHoldSymbol");
				//createHoldSymbol(clickPoint);
				break;
			case HOLD_HERE_AND_PULL:
				createTwoPointInput(clickPoint, "createHoldAndPullSymbol");
				//createHoldAndPullSymbol(clickPoint);
				break;
			case NEXT_VIEW:
				createTwoPointInput(clickPoint, "createNextViewHereSymbol");
				//createNextViewHereSymbol(clickPoint);
				break;
			case ROTATIONS:
				createOnePointInput(e, clickPoint, "createRotationSymbol");
				//createRotationSymbol(clickPoint);
				break;
			case X_RAY_CIRCLE:
				//is done through MouseDragged(MouseEvent e){}
				//createXRayCircle(OriGeomSymbol.TYPE_XRAY_CIRCLE);
				break;
			default:
				break;
		}
	}
	
	private void measureMode(Point2D.Double clickPoint) {
		switch(Globals.measureMode) {
			case MEASURE_LENGTH:
				measureLength(clickPoint);
				break;
			case MEASURE_ANGLE:
				measureAngle(clickPoint);
				break;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Point2D.Double normMousePointLogic = currentMousePointLogic;
		
		try {
			affineTransform.inverseTransform(e.getPoint(), normMousePointLogic);
		} catch (Exception ex) {
			return;
		}
		
		if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0 &&
				(e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
			//scale diagram with CTRL + dragging mouse
			double moved = e.getX() - preMousePoint.getX() + e.getY() - preMousePoint.getY();
			Globals.SCALE += moved / 150.0;
			if (Globals.SCALE < 0.01) {
				Globals.SCALE = 0.01;
			}
			preMousePoint = e.getPoint();
			updateAffineTransform(g2d);
			repaint();
		} else if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
			//translate diagram with right click + dragging mouse
			transX += (double) (e.getX() - preMousePoint.getX()) / Globals.SCALE;
			transY += (double) (e.getY() - preMousePoint.getY()) / Globals.SCALE;
			preMousePoint = e.getPoint();
			updateAffineTransform(g2d);
			repaint();
		} else if (Globals.toolbarMode == Constants.ToolbarMode.SELECTION_TOOL 
				|| (Globals.toolbarMode == Constants.ToolbarMode.INPUT_SYMBOL
				&& Globals.inputSymbolMode == Constants.InputSymbolMode.X_RAY_CIRCLE)) {
			//moving symbols by dragging (all selected Objects are moved)
			currentMouseDraggingPoint = e.getPoint();
			Point2D.Double affineMouseDraggingPoint = new Point2D.Double();
			try {
				affineTransform.inverseTransform(currentMouseDraggingPoint, affineMouseDraggingPoint);
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			
			double xTrans = (e.getX() - preMousePoint.getX()) / Globals.SCALE;
			double yTrans = (e.getY() - preMousePoint.getY()) / Globals.SCALE;

//			OriArrow pickedArrow = pickArrow(affineMouseDraggingPoint);
//			if (pickArrow(currentMousePointLogic) != null && isPressedOverSymbol || isMovingSymbols) {
//				isMovingSymbols = true;
//				if (pickedArrow != null) {
//					preMousePoint = e.getPoint();
//					for (OriArrow arrow : Origrammer.diagram.steps.get(Globals.currentStep).arrows) {
//						//if selected, move to new position
//						if (arrow.isSelected()) {
//							arrow.moveBy(xTrans, yTrans);
//						}
//					}
//				}
//			}

			OriLeaderBox pickedLeader = pickLeader(affineMouseDraggingPoint);
			if (pickLeader(currentMousePointLogic) != null && isPressedOverSymbol || isMovingSymbols) {
				isMovingSymbols = true;
				if (pickedLeader != null) {
					preMousePoint = e.getPoint();
					for (OriLeaderBox leader : Origrammer.diagram.steps.get(Globals.currentStep).leaderBoxSymbols) {
						//if selected, move to new position
						if (leader.isSelected()) {
							leader.moveBy(xTrans, yTrans);
						}
					}
				}
			}

			OriGeomSymbol pickedGeomSymbol = pickGeomSymbol(affineMouseDraggingPoint);
			if (pickGeomSymbol(currentMousePointLogic) != null && isPressedOverSymbol || isMovingSymbols) {
				isMovingSymbols = true;
				if (pickedGeomSymbol != null) {
					preMousePoint = e.getPoint();
					for (OriGeomSymbol s : Origrammer.diagram.steps.get(Globals.currentStep).geomSymbols) {
						//if selected, move to new position
						if (s.isSelected()) {
							s.moveBy(xTrans, yTrans);
						}
					}
				}
			}

			OriEqualDistSymbol pickedEds = pickEqualDistSymbol(affineMouseDraggingPoint);
			if (pickEqualDistSymbol(currentMousePointLogic) != null && isPressedOverSymbol || isMovingSymbols) {
				isMovingSymbols = true;
				if (pickedEds != null) {

					Vector2d pickV = pickVertex(normMousePointLogic);

					if (pickV != null) {
						xTrans = pickV.x - pickedEds.getP0().x; 
						yTrans = pickV.y - pickedEds.getP0().y;
					} else {
						xTrans = currentMousePointLogic.x - pickedEds.getP0().x;
						yTrans = currentMousePointLogic.y - pickedEds.getP0().y;
					}

					preMousePoint = e.getPoint();
					for (OriEqualDistSymbol eds : Origrammer.diagram.steps.get(Globals.currentStep).equalDistSymbols) {
						if (eds.isSelected()) {
							eds.moveBy(xTrans, yTrans);
						}
					}
				}
			}
			
			OriEqualAnglSymbol pickedEas = pickEqualAnglSymbol(affineMouseDraggingPoint);
			if (pickEqualAnglSymbol(currentMousePointLogic) != null && isPressedOverSymbol || isMovingSymbols) {
				isMovingSymbols = true;
				if (pickedEas != null) {

					Vector2d pickV = pickVertex(normMousePointLogic);

					if (pickV != null) {
						xTrans = pickV.x - pickedEas.getV().x; 
						yTrans = pickV.y - pickedEas.getV().y;
					} else {
						xTrans = currentMousePointLogic.x - pickedEas.getV().x;
						yTrans = currentMousePointLogic.y - pickedEas.getV().y;
					}

					preMousePoint = e.getPoint();
					for (OriEqualAnglSymbol eas : Origrammer.diagram.steps.get(Globals.currentStep).equalAnglSymbols) {
						if (eas.isSelected()) {
							eas.moveBy(xTrans, yTrans);
						}
					}
				}
			}

			OriPleatCrimpSymbol pickedPleatSymbol = pickPleatSymbol(affineMouseDraggingPoint);
			if (pickPleatSymbol(currentMousePointLogic) != null && isPressedOverSymbol || isMovingSymbols) {
				isMovingSymbols = true;
				if (pickedPleatSymbol != null) {
					preMousePoint = e.getPoint();
					for (OriPleatCrimpSymbol p : Origrammer.diagram.steps.get(Globals.currentStep).pleatCrimpSymbols) {
						//if selected, move to new position
						if (p.isSelected()) {
							p.moveBy(xTrans, yTrans);
						}
					}
				}
			}
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		try {
			affineTransform.inverseTransform(e.getPoint(), currentMousePointLogic);
		} catch (Exception ex) {
			return;
		}

		if ((Globals.toolbarMode == Constants.ToolbarMode.INPUT_LINE && Globals.lineEditMode != Constants.LineInputMode.ANGLE_BISECTOR && Globals.lineEditMode != Constants.LineInputMode.PERPENDICULAR)
				|| Globals.toolbarMode == Constants.ToolbarMode.INPUT_ARROW
				|| Globals.toolbarMode == Constants.ToolbarMode.INPUT_SYMBOL
				|| (Globals.toolbarMode == Constants.ToolbarMode.INPUT_VERTEX && Globals.vertexInputMode == Constants.VertexInputMode.ABSOLUTE)	//TODO: maybe just for everything except Selection tool?
				|| Globals.toolbarMode == Constants.ToolbarMode.FILL_TOOL
				) {
				Vector2d firstV = selectedCandidateV;
				selectedCandidateV = this.pickVertex(currentMousePointLogic);

				if (selectedCandidateV == null) {
					if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
						OriLine l = pickLine(currentMousePointLogic);
						if (l != null) {
							selectedCandidateV = new Vector2d();
							Vector2d cp = new Vector2d(currentMousePointLogic.x, currentMousePointLogic.y);
							GeometryUtil.DistancePointToSegment(cp,  l.getP0(), l.getP1(), selectedCandidateV);
						}
					}
				}
				if (selectedCandidateV != firstV || firstSelectedV != null || Globals.lineEditMode == Constants.LineInputMode.BY_LENGTH_AND_ANGLE) {
					repaint();
				}
		} else if (Globals.toolbarMode == Constants.ToolbarMode.SELECTION_TOOL) {
			//highlighting for all objects when moving over them in selection mode
			OriLine preLine = selectedCandidateL;
			selectedCandidateL = pickLine(currentMousePointLogic);
			if (preLine != selectedCandidateL) {
				repaint();
			}

			Vector2d preVertex = selectedCandidateV;
			selectedCandidateV = pickVertex(currentMousePointLogic);
			if (preVertex != selectedCandidateV) {
				repaint();
			}

			OriArrow preArrow = selectedCandidateA;
			selectedCandidateA = pickArrow(currentMousePointLogic);
			if (preArrow != selectedCandidateA) {
				repaint();
			}

			OriFace preFace = selectedCandidateF;
			selectedCandidateF = pickFace(currentMousePointLogic);
			if (preFace != selectedCandidateF) {
				repaint();
			}

			OriLeaderBox preLeader = selectedCandidateLeader;
			selectedCandidateLeader = pickLeader(currentMousePointLogic);
			if (preLeader != selectedCandidateLeader) {
				repaint();
			}

			OriGeomSymbol preGeomSymbol = selectedCandidateGS;
			selectedCandidateGS = pickGeomSymbol(currentMousePointLogic);
			if (preGeomSymbol != selectedCandidateGS) {
				repaint();
			}

			OriEqualDistSymbol preEqualDistSymbol = selectedCandidateEDS;
			selectedCandidateEDS = pickEqualDistSymbol(currentMousePointLogic);
			if (preEqualDistSymbol != selectedCandidateEDS) {
				repaint();
			}

			OriEqualAnglSymbol preEqualAnglSymbol = selectedCandidateEAS;
			selectedCandidateEAS = pickEqualAnglSymbol(currentMousePointLogic);
			if (preEqualAnglSymbol != selectedCandidateEAS) {
				repaint();
			}

			OriPleatCrimpSymbol prePleatSymbol = selectedCandidatePleat;
			selectedCandidatePleat = pickPleatSymbol(currentMousePointLogic);
			if (prePleatSymbol != selectedCandidatePleat) {
				repaint();
			}

		} else if (Globals.toolbarMode == Constants.ToolbarMode.MEASURE_TOOL) {
			Vector2d firstV = selectedCandidateV;
			selectedCandidateV = this.pickVertex(currentMousePointLogic);
			if (selectedCandidateV != firstV || firstSelectedV != null) {
				firstSelectedL = null;
				selectedCandidateL = null;
				repaint();
			} else {
				OriLine preLine = selectedCandidateL;
				selectedCandidateL = pickLine(currentMousePointLogic);
				if (preLine != selectedCandidateL) {
					repaint();
				}
			}
		} else if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_ARROW ||
				Globals.toolbarMode == Constants.ToolbarMode.FILL_TOOL ||
				(Globals.toolbarMode == Constants.ToolbarMode.INPUT_SYMBOL 
				&& (Globals.inputSymbolMode == Constants.InputSymbolMode.EQUAL_DIST 
				|| Globals.inputSymbolMode == Constants.InputSymbolMode.EQUAL_ANGL
				|| Globals.inputSymbolMode == Constants.InputSymbolMode.ROTATIONS
				|| Globals.inputSymbolMode == Constants.InputSymbolMode.HOLD_HERE
				|| Globals.inputSymbolMode == Constants.InputSymbolMode.HOLD_HERE_AND_PULL))) {
			//highlighting when moving over a vertex
			selectedCandidateV = this.pickVertex(currentMousePointLogic);
			repaint();
		} else if ((Globals.toolbarMode == Constants.ToolbarMode.INPUT_VERTEX 
				&& Globals.vertexInputMode == Constants.VertexInputMode.FRACTION_OF_LINE)
				|| (Globals.toolbarMode == Constants.ToolbarMode.INPUT_LINE 
						&& Globals.lineEditMode == Constants.LineInputMode.ANGLE_BISECTOR)
				|| (Globals.toolbarMode == Constants.ToolbarMode.INPUT_LINE
						&& Globals.lineEditMode == Constants.LineInputMode.PERPENDICULAR)) {
			//highlighting when moving over a line
			selectedCandidateL = this.pickLine(currentMousePointLogic);
			repaint();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		preMousePoint = e.getPoint();

		//mark OriArrow as selected if in SELECTION_TOOL mode and you have pressed left mouse button while over OriArrow -->
		//only needed for freely movable objects
		if (Globals.toolbarMode == Constants.ToolbarMode.SELECTION_TOOL) {
			OriArrow pickedArrow = pickArrow(currentMousePointLogic);
			if (pickedArrow != null) {
				pickedArrow.setSelected(true);
				isPressedOverSymbol = true;
				repaint();
			}

			OriLeaderBox pickedLeader = pickLeader(currentMousePointLogic);
			if (pickedLeader != null) {
				pickedLeader.setSelected(true);
				isPressedOverSymbol = true;
				repaint();
			}

			OriGeomSymbol pickedGeomSymbol = pickGeomSymbol(currentMousePointLogic);
			if (pickedGeomSymbol != null) {
				pickedGeomSymbol.setSelected(true);
				isPressedOverSymbol = true;
				repaint();
			}
			
			OriEqualDistSymbol pickedEDS = pickEqualDistSymbol(currentMousePointLogic);
			if (pickedEDS != null) {
				pickedEDS.setSelected(true);
				isPressedOverSymbol = true;
				repaint();
			}
			
			OriEqualAnglSymbol pickedEAS = pickEqualAnglSymbol(currentMousePointLogic);
			if (pickedEAS != null) {
				pickedEAS.setSelected(true);
				isPressedOverSymbol = true;
				repaint();
			}

			OriPleatCrimpSymbol pickedPleatSymbol = pickPleatSymbol(currentMousePointLogic);
			if (pickedPleatSymbol != null) {
				pickedPleatSymbol.setSelected(true);
				isPressedOverSymbol = true;
				repaint();
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_SYMBOL
				&& Globals.inputSymbolMode == Constants.InputSymbolMode.X_RAY_CIRCLE
				&& currentMouseDraggingPoint != null) {
			createXRayCircle();
		} else if (Globals.toolbarMode == Constants.ToolbarMode.SELECTION_TOOL
				&& currentMouseDraggingPoint != null) {
			//Rectangular Selection
			Point2D.Double sp = new Point2D.Double();
			Point2D.Double ep = new Point2D.Double();
			try {
				affineTransform.inverseTransform(preMousePoint, sp);
				affineTransform.inverseTransform(currentMouseDraggingPoint, ep);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			int minX = (int) Math.round(Math.min(sp.x,  ep.x));
			int minY = (int) Math.round(Math.min(sp.y, ep.y));
			int maxX = (int) Math.round(Math.max(sp.x, ep.x));
			int maxY = (int) Math.round(Math.max(sp.y, ep.y));
			Rectangle selectRect = new Rectangle(minX, minY, maxX-minX, maxY-minY);

			//Check if there is a line in the selection rectangle
			for (OriLine l : Origrammer.diagram.steps.get(Globals.currentStep).lines) {
				Line2D tmpL = new Line2D.Double(l.getP0().x, l.getP0().y, l.getP1().x, l.getP1().y);
				if (tmpL.intersects(selectRect)) {
					l.setSelected(true);
				} else {
					l.setSelected(false);
				}
			}

			//Check if there is a vertex in the selection rectangle
			for (OriVertex v : Origrammer.diagram.steps.get(Globals.currentStep).vertices) {
				if (selectRect.contains(new Point2D.Double(v.getP().x, v.getP().y))) {
					v.setSelected(true);
				} else {
					v.setSelected(false);
				}
			}

			//TODO: check if OriArrow is in selection rectangle
//			//Check if there is an arrow in the selection rectangle
			for (OriArrow a : Origrammer.diagram.steps.get(Globals.currentStep).arrows) {
				ArrayList<Shape> shapes = a.getShapesForDrawing();

				for (Shape s : shapes) {
					if (s.intersects(selectRect)) {
						a.setSelected(true);
						break;
					} else {
						a.setSelected(false);
					}
				}
			}

			//Check if there is a OriFace in the selection rectangle (only if filledFaces are rendered)
			if (Globals.dispFilledFaces) {
				for (OriFace f : Origrammer.diagram.steps.get(Globals.currentStep).filledFaces) {
					if (f.path.intersects(selectRect)) {
						f.setSelected(true);
					} else {
						f.setSelected(false);
					}
				}	
			}

			//Check if there is a symbol in the selection rectangle
			for (OriGeomSymbol gs : Origrammer.diagram.steps.get(Globals.currentStep).geomSymbols) {
				ArrayList<Shape> shapes = gs.getShapesForDrawing();
				for (Shape s : shapes) {
					if (s.intersects(selectRect)) {
						gs.setSelected(true);
						break;
					} else {
						gs.setSelected(false);
					}
				}
			}

			//Check if there is a leader in the selection rectangle
			for (OriLeaderBox leader : Origrammer.diagram.steps.get(Globals.currentStep).leaderBoxSymbols) {
				Rectangle tmpR3 = leader.getLabel().getBounds();
				Line2D tmpL2 = new Line2D.Double(leader.line.getP0().x, leader.line.getP0().y, 
						leader.line.getP1().x, leader.line.getP1().y);

				if (tmpR3.intersects(selectRect)) {
					leader.setSelected(true);
				} else if (tmpL2.intersects(selectRect)) {
					leader.setSelected(true);
				} else {
					leader.setSelected(false);
				}
			}

			//Check if there is a equalDistSymbol in the selection rectangle
			for (OriEqualDistSymbol eds : Origrammer.diagram.steps.get(Globals.currentStep).equalDistSymbols) {
				Vector2d nv = GeometryUtil.getNormalVector(GeometryUtil.getUnitVector(eds.getP0(), eds.getP1()));
				//calculates a rectangle that encapsulates the whole symbol 
				//(this fixes inconsistencies with the hitbox, compared to 
				//when only checked for intersection of the symbol lines themselves)
				double p0x = eds.getP0().x + 15 * nv.x;
				double p0y = eds.getP0().y + 15 * nv.y;

				double p1x = eds.getP0().x - 15 * nv.x;
				double p1y = eds.getP0().y - 15 * nv.y;

				double p2x = eds.getP1().x - 15 * nv.x;
				double p2y = eds.getP1().y - 15 * nv.y;

				double p3x = eds.getP1().x + 15 * nv.x;
				double p3y = eds.getP1().y + 15 * nv.y;

				Path2D.Double p = new Path2D.Double();
				p.moveTo(p0x, p0y);
				p.lineTo(p1x, p1y);
				p.lineTo(p2x, p2y);
				p.lineTo(p3x, p3y);
				p.closePath();

				if (p.intersects(selectRect)) {
					eds.setSelected(true);
				} else {
					eds.setSelected(false);
				}
			}

			//Check if there is a equalAnglSymbol in the selection rectangle
			for (OriEqualAnglSymbol eas : Origrammer.diagram.steps.get(Globals.currentStep).equalAnglSymbols) {
				ArrayList<Shape> shapes = eas.getShapesForDrawing();

				for(Shape s : shapes) {
					if(s.intersects(selectRect)) {
						eas.setSelected(true);
						break;
					} else {
						eas.setSelected(false);
					}
				}
			}

			//Check if there is a equalAnglSymbol in the selection rectangle
			for (OriPleatCrimpSymbol p : Origrammer.diagram.steps.get(Globals.currentStep).pleatCrimpSymbols) {
				ArrayList<Shape> shapes = p.getShapesForDrawing();

				for(Shape s : shapes) {
					if(s.intersects(selectRect)) {
						p.setSelected(true);
						break;
					} else {
						p.setSelected(false);
					}
				}
			}

			isPressedOverSymbol = false;
			Origrammer.mainFrame.uiTopPanel.modeChanged();
		}
		currentMouseDraggingPoint = null;
		isMovingSymbols = false;
		repaint();
	}

	@Override
	public void componentResized(ComponentEvent e) {
		if (getWidth() <= 0 || getHeight() <= 0) {
			return;
		}
		preSize = getSize();

		transX = transX - preSize.width * 0.5 + getWidth() * 0.5;
		transY = transY - preSize.height * 0.5 + getHeight() * 0.5;

		//updateAffineTransform(g2d);
		repaint();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		//zoom on diagram with mouseWheel
		double scale_ = (100.0 - e.getWheelRotation() * 5) / 100.0;
		Globals.SCALE *= scale_;

		Origrammer.mainFrame.uiSidePanel.scalingCustomTF.setValue(Globals.SCALE*100);

		//updateAffineTransform(g2d);
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {		
	}
	@Override
	public void mouseExited(MouseEvent arg0) {		
	}
	@Override
	public void componentShown(ComponentEvent arg0) {		
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {		
	}
	@Override
	public void componentHidden(ComponentEvent arg0) {		
	}
	@Override
	public void componentMoved(ComponentEvent arg0) {		
	}
}