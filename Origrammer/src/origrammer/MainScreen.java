package origrammer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
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
	private OriPolygon selectedCandidatePolygon = null;
	private ArrayList<Vector2d> tmpFilledFacePath = null;

	private boolean dispGrid = true;
	//Affine transformation info
	private Dimension preSize;
	private AffineTransform affineTransform = new AffineTransform();

	public Graphics2D g2d;

	public ArrayList<JLabel> arrowLabelList = new ArrayList<>();
	private boolean isMovingSymbols = false;
	private boolean isPressedOverSymbol = false;
	private boolean isReversePerpendicularLineInput = false;
	int tmpArrowWidth;
	int tmpArrowHeight;

	public MainScreen() {
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addComponentListener(this);

		Globals.SCALE = 1.0;
		
		setPreferredSize(Constants.MAINSCREEN_SIZE);
		setLayout(null);
		preSize = getSize();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		removeAll();
		
		setBackground(Color.white);
		g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		//only draw the grid and apply affineTransform (with transX & transY)
		//if it's not a stepPreview that is being rendered
		if (!Globals.renderStepPreview) {
			updateAffineTransform(g2d);
			if (dispGrid) {
				renderGrid(g2d);
			}
		} else {
			affineTransform.setToTranslation(getWidth() * 0.5, getHeight() * 0.5);
			affineTransform.scale(Globals.SCALE, Globals.SCALE);
			g2d.transform(affineTransform);
		}
		
		renderAllPolygons();		
		renderAllFilledFaces();
		renderAllLines();
		renderAllArrows();

		
		renderAllVertices();


		//Symbols
		renderAllOriGeomSymbols();
		renderAllEquDistSymbols();
		renderAllEquAnglSymbols();
		renderAllCrimpPleatSymbols();
		
		//temporary stuff
		renderTmpFilledFace();
		renderTmpLine();
		renderTmpLengthAngleLine();
		renderTmpPerpendicular();
		renderTmpExtendLine();
		renderTmpArrow();
		renderTmpOriSymbols();
		
		//only draw the selectedVertices
		//if it's not a stepPreview that is being rendered
		if (!Globals.renderStepPreview) {
			//show coordinates of selected Vertex
			if (selectedCandidateV != null ) {
				g.setColor(Color.BLACK);
				g.drawString("(" + selectedCandidateV.x + ", " + selectedCandidateV.y + ")", -325, -325);
			}
			renderSelectedVertices();
		}
		renderRectSelection();
		
		renderTmpRotationSymbol();
		renderTmpHoldHereSymbol();
		renderTmpXRayCircleSymbol();
		renderTmpClosedSinkSymbol();
		
		renderTmpEqualAnglSymbol();
		renderTmpCrimpPleatSymbol();
	}
	
	
	private void renderGrid(Graphics2D g2d) {
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


	private void renderAllPolygons() {
		if (Globals.dispPolygons) {
			int highestPolygonHeight = 0;

			for (OriPolygon p : Origrammer.diagram.steps.get(Globals.currentStep).polygons) {
				if (p.getHeight() > highestPolygonHeight) {
					highestPolygonHeight = p.getHeight();
				}
			}

			//TODO: render the different heights
			for (int i=0; i<= highestPolygonHeight; i++) {
				for (OriPolygon p : Origrammer.diagram.steps.get(Globals.currentStep).polygons) {
					if (p.getHeight() == i) {
						GeneralPath tmpPath = p.getShapesForDrawing();

						if (p.isSelected()) {
							g2d.setPaint(new Color(0.0f, 1.0f, 0.0f, 0.25f));

						} else {
							g2d.setPaint(new Color(1.0f, 0.0f, 0.0f, 0.25f));

						}
						g2d.fill(tmpPath);
					}
				}
			}
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
					&& line.isSelected())
					|| (Globals.toolbarMode == Constants.ToolbarMode.FILL_TOOL
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
			Vector2d p0 = line.getP0().p;
			Vector2d p1 = line.getP1().p;

			if (line.getType() == OriLine.TYPE_CREASE) {
				if (line.isStartOffset()) {
					p0 = line.getTranslatedP0();
				} else {
					p0 = line.getP0().p;
				}
				if (line.isEndOffset()) {
					p1 = line.getTranslatedP1();
				} else {
					p1 = line.getP1().p;
				}
			}

			if (line.getType() != OriLine.TYPE_DIAGONAL || Globals.dispTriangulation) {
				g2d.draw(new Line2D.Double(p0.x, p0.y, p1.x, p1.y));
			}

		}
	}
	
	
	private void renderTmpFilledFace() {
		if (tmpFilledFacePath != null) {

			for (Vector2d p : tmpFilledFacePath) {				
				g2d.fill(new Rectangle2D.Double(p.x - 5.0 / Globals.SCALE,
						p.y - 5.0 / Globals.SCALE, 10.0 / Globals.SCALE, 10.0 / Globals.SCALE));
				
			}
		}
	}
	
	/**
	 * Renders the preview OriLine while inputting
	 */
	private void renderTmpLine() {
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
				OriLine l = pickOriLine(currentMousePointLogic);

				if (l != null) {
					Vector2d uv = GeometryUtil.getUnitVector(l.getP0().p, l.getP1().p);
					Vector2d nv = GeometryUtil.getNormalVector(uv);
					Vector2d vertex = pickVertex(currentMousePointLogic);
					Vector2d v2 = null;
					//get point on OriLine that is closest to currentMousePointLogic, 
					//if currentMousePointLogic is not close to a vertex
					if (vertex == null) {
						vertex = new Vector2d();
						Vector2d cp = new Vector2d(currentMousePointLogic.x, currentMousePointLogic.y);
						GeometryUtil.DistancePointToSegment(cp,  l.getP0().p, l.getP1().p, vertex);
					}
					if (isReversePerpendicularLineInput) {
						nv.negate();
					}

					//get crossingPoint in order to get inputLine.P1
					//if there is no crossing point -> reverse inputLine direction
					v2 = GeometryUtil.getClosestCrossPoint(vertex, nv);
					if (v2 == null) {
						v2 = GeometryUtil.getClosestCrossPoint(vertex, new Vector2d(-nv.x, -nv.y));
					}
					if (vertex != null && v2 != null) {
						setColorStrokeByLineType(Globals.inputLineType);
						g2d.draw(new Line2D.Double(vertex.x, vertex.y, v2.x, v2.y));
					}
				}
			}
		}
	}
	
	private void renderTmpExtendLine() {
		if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_LINE) {
			if (Globals.lineEditMode == Constants.LineInputMode.EXTEND_TO_NEXT_LINE) {
				if (firstSelectedV != null) {
					Vector2d cv = selectedCandidateV == null
							? new Vector2d(currentMousePointLogic.getX(), currentMousePointLogic.getY()) : selectedCandidateV;
					Vector2d uv = GeometryUtil.getUnitVector(firstSelectedV, cv);
					
					Vector2d crossPoint1 = GeometryUtil.getClosestCrossPoint(firstSelectedV, uv);
					uv.negate();
					Vector2d crossPoint2 = GeometryUtil.getClosestCrossPoint(cv, uv);

					if (crossPoint1 != null && crossPoint2 != null) {
						g2d.draw(new Line2D.Double(crossPoint2.x, crossPoint2.y, crossPoint1.x, crossPoint1.y));
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
	 * Renders the preview OriArrow while inputting
	 */
	private void renderTmpArrow() {
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

			if (gs.getType() == OriGeomSymbol.TYPE_ROTATION) {
				Font oldFont = getFont();
				g2d.setFont(new Font("Arial", Font.PLAIN, 35));
				String text = gs.getText();
				int textWidth = g2d.getFontMetrics().stringWidth(text);
				g2d.drawString(text, (int) (gs.getP1().x - textWidth / 2 + 4), (int) (gs.getP1().y + 13)); 
				g2d.setFont(oldFont);
				
			} else if (gs.getType() == OriGeomSymbol.TYPE_LEADER) {
				String text = gs.getText();
				g2d.drawString(text, (int) (gs.getP2().x + 2.5), (int) (gs.getP2().y));
				
			} else if (gs.getType() == OriGeomSymbol.TYPE_REPETITION) {
				String text = gs.getText();
				int textWidth = g2d.getFontMetrics().stringWidth(text);
				g2d.drawString(text, (int) (gs.getP2().x + 2.5), (int) (gs.getP2().y - 11));
				g2d.drawRect((int) Math.round(gs.getP2().x), (int) Math.round(gs.getP2().y - 25), textWidth + 5, 25);
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
	
	private void renderTmpOriSymbols() {
		if (firstSelectedV != null) {
			g2d.fill(new Rectangle2D.Double(firstSelectedV.x - 5.0 / Globals.SCALE,
					firstSelectedV.y - 5.0 / Globals.SCALE, 10.0 / Globals.SCALE, 10.0 / Globals.SCALE));
			g2d.setStroke(Config.STROKE_EDGE);
			if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_SYMBOL) {
				Vector2d cv = selectedCandidateV == null
						? new Vector2d(currentMousePointLogic.getX(), currentMousePointLogic.getY()) : selectedCandidateV;
												
						switch (Globals.inputSymbolMode) {	//TODO: render temp methods for all symbols
						case LEADER:
							renderTmpLeaderSymbol(cv);
							break;
						case CRIMPING_PLEATING:
							break;
						case EQUAL_ANGL:
							//renderTmpEqualAnglSymbol();
							break;
						case EQUAL_DIST:
							renderTmpEqualDistSymbol(cv);
							break;
						case FOLD_OVER_AND_OVER:
							break;
						case HOLD_HERE:
							renderTmpHoldHereSymbol(); //TODO: holdHereSymbol preview unnecessary?
							break;
						case HOLD_HERE_AND_PULL:
							renderTmpHoldAndPullSymbol(cv);
							break;
						case NEXT_VIEW:
							renderTmpNextViewHereSymbol(cv);	
							break;
						case REPETITION_BOX:
							renderTmpRepetitionSymbol(cv);
							break;
						case ROTATIONS:
							//renderTmpRotationSymbol(cv);
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
	
	private void renderTmpRotationSymbol() {
		
		if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_SYMBOL 
				&& Globals.inputSymbolMode == Constants.InputSymbolMode.ROTATIONS) {
			Vector2d cv = selectedCandidateV == null 
					? new Vector2d(currentMousePointLogic.getX(), currentMousePointLogic.getY()) : selectedCandidateV;
			OriGeomSymbol gs = new OriGeomSymbol();
			gs.setP1(cv);
			gs.setType(OriGeomSymbol.TYPE_ROTATION);
			if (Origrammer.mainFrame.uiTopPanel.rotationTF.getText().length() == 0) {
				JOptionPane.showMessageDialog(this,  Origrammer.res.getString("Error_EmptyRotationTextField"),
						"Error_EmptyRotationTextField", JOptionPane.ERROR_MESSAGE);
			} else {
				gs.setSize(80); //TODO: adjustmentSlider on TopPanel
				gs.setText(Origrammer.mainFrame.uiTopPanel.rotationTF.getText() + "°");
				gs.setReversed(Origrammer.mainFrame.uiTopPanel.reverseRotSymbol.isSelected());
			}
			Font oldFont = getFont();
			g2d.setFont(new Font("Arial", Font.PLAIN, 35));
			String text = gs.getText();
			int textWidth = g2d.getFontMetrics().stringWidth(text);
			g2d.drawString(text, (int) (gs.getP1().x - textWidth / 2 + 4), (int) (gs.getP1().y + 13)); 
			g2d.setFont(oldFont);
			ArrayList<Shape> shapes = gs.getShapesForDrawing();
			for (Shape s : shapes) {
				g2d.draw(s);
			}	
		}
	}
	
	private void renderTmpHoldHereSymbol() {
		if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_SYMBOL 
				&& Globals.inputSymbolMode == Constants.InputSymbolMode.HOLD_HERE) {
			Vector2d cv = selectedCandidateV == null 
					? new Vector2d(currentMousePointLogic.getX(), currentMousePointLogic.getY()) : selectedCandidateV;
			OriGeomSymbol gs = new OriGeomSymbol();
			gs.setP1(cv);
			gs.setType(OriGeomSymbol.TYPE_HOLD);
			gs.setSize(45);
			ArrayList<Shape> shapes = gs.getShapesForDrawing();
			for (Shape s : shapes) {
				g2d.draw(s);
			}
		}
	}
	
	private void renderTmpHoldAndPullSymbol(Vector2d v2) {
		OriGeomSymbol gs = new OriGeomSymbol();
		gs.setP1(firstSelectedV);
		gs.setP2(v2);
		gs.setType(OriGeomSymbol.TYPE_HOLD_AND_PULL);
		gs.setSize(45);
		ArrayList<Shape> shapes = gs.getShapesForDrawing();
		for (Shape s : shapes) {
			g2d.draw(s);
		}
	}
	
	private void renderTmpNextViewHereSymbol(Vector2d p2) {
		OriGeomSymbol gs = new OriGeomSymbol();
		gs.setP1(firstSelectedV);
		gs.setP2(p2);
		gs.setType(OriGeomSymbol.TYPE_NEXT_VIEW_HERE);
		gs.setSize(100);
		ArrayList<Shape> shapes = gs.getShapesForDrawing();
		for (Shape s : shapes) {
			g2d.draw(s);
		}
	}

	private void renderTmpXRayCircleSymbol() {
		if (currentMouseDraggingPoint != null && (Globals.toolbarMode == Constants.ToolbarMode.INPUT_SYMBOL 
				&& Globals.inputSymbolMode == Constants.InputSymbolMode.X_RAY_CIRCLE )) {
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
	
	private void renderTmpClosedSinkSymbol() {
		if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_SYMBOL 
				&& Globals.inputSymbolMode == Constants.InputSymbolMode.SINKS) {
			Vector2d cv = selectedCandidateV == null 
					? new Vector2d(currentMousePointLogic.getX(), currentMousePointLogic.getY()) : selectedCandidateV;

			OriGeomSymbol gs = new OriGeomSymbol();
			gs.setP1(cv);
			gs.setType(OriGeomSymbol.TYPE_CLOSED_SINK);
			gs.setSize(10);

			ArrayList<Shape> shapes = gs.getShapesForDrawing();
			for (Shape s : shapes) {
				g2d.draw(s);
				g2d.fill(s);
			}
		}
	}

	private void renderTmpLeaderSymbol(Vector2d cv) {
		OriGeomSymbol gs = new OriGeomSymbol();
		gs.setP1(firstSelectedV);
		gs.setP2(cv);
		gs.setType(OriGeomSymbol.TYPE_LEADER);
		gs.setText(Origrammer.mainFrame.uiTopPanel.inputLeaderTextTF.getText());
		//gs.setSize(100);
		String text = gs.getText();
		g2d.drawString(text, (int) (gs.getP2().x+2.5), (int) (gs.getP2().y)); 
		ArrayList<Shape> shapes = gs.getShapesForDrawing();
		for (Shape s : shapes) {
			g2d.draw(s);
		}	
	}
	
	private void renderTmpRepetitionSymbol(Vector2d cv) {
		OriGeomSymbol gs = new OriGeomSymbol();
		gs.setP1(firstSelectedV);
		gs.setP2(cv);
		gs.setType(OriGeomSymbol.TYPE_REPETITION);
		gs.setText(Origrammer.mainFrame.uiTopPanel.inputLeaderTextTF.getText());
		//gs.setSize(100);
		String text = gs.getText();
		int textWidth = g2d.getFontMetrics().stringWidth(text);
		g2d.drawRect((int) Math.round(gs.getP2().x), (int) Math.round(gs.getP2().y-25), textWidth+5, 25);
		g2d.drawString(text, (int) (gs.getP2().x+2.5), (int) (gs.getP2().y-11));
		ArrayList<Shape> shapes = gs.getShapesForDrawing();
		for (Shape s : shapes) {
			g2d.draw(s);
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
	
	private void renderTmpEqualDistSymbol(Vector2d p1) {
		OriEqualDistSymbol eds = new OriEqualDistSymbol();
		eds.setP0(firstSelectedV);
		eds.setP1(p1);
		eds.setTranslationDist(Origrammer.mainFrame.uiTopPanel.sliderEqualDist.getValue());
		eds.setDividerCount(Integer.parseInt(Origrammer.mainFrame.uiTopPanel.equalDistDividerTF.getText()));
		
		ArrayList<Shape> shapes = eds.getShapesForDrawing();
		for (Shape s : shapes) {
			g2d.draw(s);
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
	
	private void renderTmpEqualAnglSymbol() {
		if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_SYMBOL 
				&& Globals.inputSymbolMode == Constants.InputSymbolMode.EQUAL_ANGL 
				&& firstSelectedV != null && secondSelectedV != null) {
			
			Vector2d v1 = firstSelectedV;
			Vector2d v2 = secondSelectedV;
			Vector2d v3 = selectedCandidateV == null
					? new Vector2d(currentMousePointLogic.getX(), currentMousePointLogic.getY()) : selectedCandidateV;
					
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
			OriEqualAnglSymbol eas = new OriEqualAnglSymbol();
			eas.setV(v1);
			eas.setP1(v2);
			eas.setP2(v3);
			
			eas.setDividerCount(Integer.parseInt(Origrammer.mainFrame.uiTopPanel.equalAnglDividerTF.getText()));
			
			ArrayList<Shape> shapes = eas.getShapesForDrawing();
			for (Shape s : shapes) {
				g2d.draw(s);
			}
		}
	}
	
	private void renderAllCrimpPleatSymbols() {
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
	
	private void renderTmpCrimpPleatSymbol() {
		if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_SYMBOL 
				&& Globals.inputSymbolMode == Constants.InputSymbolMode.CRIMPING_PLEATING) {
			Vector2d cv = selectedCandidateV == null 
					? new Vector2d(currentMousePointLogic.getX(), currentMousePointLogic.getY()) : selectedCandidateV;
			OriPleatCrimpSymbol gs = new OriPleatCrimpSymbol();

			gs.setPosition(cv);
			gs.setIsSwitchedDir(Origrammer.mainFrame.uiTopPanel.pleatCB.isSelected());
			gs.setLayersCount(Integer.parseInt(Origrammer.mainFrame.uiTopPanel.pleatTF.getText()));

			if (Origrammer.mainFrame.uiTopPanel.pleatRB.isSelected()) {
				gs.setType(OriPleatCrimpSymbol.TYPE_PLEAT);
			} else {
				gs.setType(OriPleatCrimpSymbol.TYPE_CRIMP);
			}
			ArrayList<Shape> shapes = gs.getShapesForDrawing();
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
				OriVertex v0 = line.getP0();
				OriVertex v1 = line.getP1();
				g2d.fill(new Rectangle2D.Double(v0.p.x - vertexDrawSize / Globals.SCALE,
						v0.p.y - vertexDrawSize / Globals.SCALE, vertexDrawSize * 2 / Globals.SCALE,
						vertexDrawSize * 2 / Globals.SCALE));
				g2d.fill(new Rectangle2D.Double(v1.p.x - vertexDrawSize / Globals.SCALE,
						v1.p.y - vertexDrawSize / Globals.SCALE, vertexDrawSize * 2 / Globals.SCALE,
						vertexDrawSize * 2 / Globals.SCALE));
			}

			for (OriVertex v : Origrammer.diagram.steps.get(Globals.currentStep).vertices) {
				if (v.isSelected() || v.p == selectedCandidateV) {
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
		case OriLine.TYPE_DIAGONAL:
			g2d.setStroke(Config.STROKE_DIAGONAL);
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
			case OriLine.TYPE_DIAGONAL:
				g2d.setColor(Config.LINE_COLOR_CREASE);
			}
		} else {
			g2d.setColor(Config.LINE_COLOR_EDGE);
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
	//#############################   PICK ON MOUSECLICK   ##################################
	//#######################################################################################

	private Vector2d pickVertex(Point2D.Double p) {
		double minDistance = Double.MAX_VALUE;
		Vector2d minPosition = new Vector2d();

		for (OriLine line : Origrammer.diagram.steps.get(Globals.currentStep).lines) {
			double dist0 = p.distance(line.getP0().p.x, line.getP0().p.y);
			if(dist0 < minDistance) {
				minDistance = dist0;
				minPosition.set(line.getP0().p);
			}
			double dist1 = p.distance(line.getP1().p.x, line.getP1().p.y);
			if (dist1 < minDistance) {
				minDistance = dist1;
				minPosition.set(line.getP1().p);
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

	private OriFace pickOriFace(Point2D.Double p) {
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
	
	private OriLine pickOriLine(Point2D.Double p) {
		double minDistance = Double.MAX_VALUE;
		OriLine bestLine = null;

		for(OriLine line : Origrammer.diagram.steps.get(Globals.currentStep).lines) {
			double dist = GeometryUtil.DistancePointToSegment(new Vector2d(p.x, p.y), line.getP0().p, line.getP1().p);
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

	private OriArrow pickOriArrow(Point2D.Double p) {
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
	
	private OriGeomSymbol  pickOriGeomSymbol(Point2D.Double p) {
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

	private OriPleatCrimpSymbol pickPleatCrimpSymbol(Point2D.Double p) {
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
	
	private OriPolygon pickPolygon(Point2D.Double p) {
		OriPolygon bestPolygon = null;
		int highestPolygonHeight = 0;
		for (OriPolygon test : Origrammer.diagram.steps.get(Globals.currentStep).polygons) {
			if (test.getHeight() > highestPolygonHeight) {
				highestPolygonHeight = test.getHeight();
			}
		}

		for (int i=highestPolygonHeight; i >= 0; i--) {
			for (OriPolygon poly : Origrammer.diagram.steps.get(Globals.currentStep).polygons) {
				if (poly.getHeight() == i) {
					boolean pickedP = poly.isInside(new Vector2d(p.x, p.y));
					
					if (pickedP) {
						bestPolygon = poly;
						i = 0;
						break;
					}

				}
			}
		}
		if (bestPolygon != null) {
			return bestPolygon;
		} else {
			return null;
		}
	}
	
	
	//#######################################################################################
	//############################   CREATE ON MOUSECLICK   #################################
	//#######################################################################################

	
	private void createVertexAbsolutePos(Point2D.Double clickPoint) {
		Vector2d v = new Vector2d(clickPoint.x, clickPoint.y);
		
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;

				Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
				OriVertex vertex = new OriVertex(firstSelectedV); //TODO: Check for double entries
				Origrammer.diagram.steps.get(Globals.currentStep).addVertex(vertex);		
				firstSelectedV = null;
			}
		}
	}

	private void createVertexFractionOfLine(Point2D.Double clickPoint) {
		OriLine l = pickOriLine(clickPoint);

		if (firstSelectedL == null && (l != null)) {
			firstSelectedL = l;

			double fraction = Double.parseDouble(Origrammer.mainFrame.uiTopPanel.inputVertexFractionTF.getText());
			double dist = GeometryUtil.Distance(l.getP0().p, l.getP1().p);
			Vector2d uv = GeometryUtil.getUnitVector(l.getP0().p, l.getP1().p);

			double newX = l.getP0().p.x + uv.x * (dist * (fraction / 100)); 
			double newY = l.getP0().p.y + uv.y * (dist * (fraction / 100));

			OriVertex newVertex = new OriVertex(newX, newY);

			OriLine first = new OriLine(l.getP0(), newVertex, l.getType());
			OriLine second = new OriLine(newVertex, l.getP1(), l.getType());
			Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
			Origrammer.diagram.steps.get(Globals.currentStep).addLine(first);
			Origrammer.diagram.steps.get(Globals.currentStep).addLine(second);
			Origrammer.diagram.steps.get(Globals.currentStep).lines.remove(l);

		}
		firstSelectedL = null;
	}

	
	/**
	 * Creates a filled face once 3 vertices are selected
	 * @param clickPoint
	 */
	private void createFilledFace(Point2D.Double clickPoint) {
		//creates OriFace that is to be filled with DEFAULT_PAPER_COLOR
		//OriFace is a GeneralPath, that fully encapsules the clickPoint with OriLines
		Vector2d v = new Vector2d(clickPoint.x, clickPoint.y);
		if (v != null) {

			OriFace newFace;
			GeneralPath path = GeometryUtil.getFillingSegment(v);

			if (Globals.faceInputDirection == Constants.FaceInputDirection.FACE_UP) {
				newFace = new OriFace(path, false, true);
			} else {
				newFace = new OriFace(path, false, false);
			}
			Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
			Origrammer.diagram.steps.get(Globals.currentStep).addFilledFace(newFace);
		}
	}
	
	
	/**
	 * Creates an OriLine that spans from v1 to v2
	 * @param v1
	 * @param v2
	 */
	public void createOriLine(MouseEvent e, Point2D.Double clickPoint) {
		
		Vector2d v = pickVertex(clickPoint);
		if (v == null) {
			if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
				OriLine l = pickOriLine(clickPoint);
				if (l != null) {
					v = new Vector2d();
					Vector2d cp = new Vector2d(clickPoint.x, clickPoint.y);
					GeometryUtil.DistancePointToSegment(cp, l.getP0().p, l.getP1().p, v);
				}
			}
		}
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;
			} else if (secondSelectedV == null) {
				secondSelectedV = v;
				
				//TODO: Check if input OriVertex is really close to an existing one
				OriLine line = new OriLine(new OriVertex(firstSelectedV), new OriVertex(secondSelectedV), Globals.inputLineType);
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
				//Origrammer.diagram.steps.get(Globals.currentStep).addLine(line);
				Origrammer.diagram.steps.get(Globals.currentStep).addNewLine(line);

				if (Globals.automatedArrowPlacement) {
					autoCreateOriArrow(firstSelectedV, secondSelectedV);
				}
				
				firstSelectedV = null;
				secondSelectedV = null;
			}
		}
	}
	
	/**
	 * Creates an angle bisector between two OriLines
	 * @param clickPoint 
	 */
	public void createOriLineAngleBisector(Point2D.Double clickPoint) {
		OriLine l = pickOriLine(clickPoint);

		if (l != null) {
			if (firstSelectedL == null) {
				firstSelectedL = l;
			} else if (secondSelectedL == null) {
				secondSelectedL = l;
				OriVertex crossPoint = new OriVertex(GeometryUtil.getCrossPoint(firstSelectedL, secondSelectedL));

				if (crossPoint.p == null) {
					JOptionPane.showMessageDialog(this,  Origrammer.res.getString("Error_NoCrossPointFound"),
							"Error_NoCrossPointFound", JOptionPane.ERROR_MESSAGE);
				} else {
					Vector2d uv1 = null;
					Vector2d uv2 = null;

					//check if the crossPoint is firstSelectedL.P1 or firstSelected.P2 and get it's UV
					if (GeometryUtil.closeCompare(crossPoint.p.x, firstSelectedL.getP0().p.x, Constants.EPSILON) 
							&& GeometryUtil.closeCompare(crossPoint.p.y, firstSelectedL.getP0().p.y, Constants.EPSILON)) {
						uv1 = GeometryUtil.getUnitVector(firstSelectedL.getP0().p, firstSelectedL.getP1().p);
					} else {
						uv1 = GeometryUtil.getUnitVector(firstSelectedL.getP1().p, firstSelectedL.getP0().p);
					}

					//check if the crossPoint is secondSelectedL.P1 or secondSelectedL.P2 and get it's UV
					if (GeometryUtil.closeCompare(crossPoint.p.x, secondSelectedL.getP0().p.x, Constants.EPSILON) 
							&& GeometryUtil.closeCompare(crossPoint.p.y, secondSelectedL.getP0().p.y, Constants.EPSILON)) {
						uv2 = GeometryUtil.getUnitVector(secondSelectedL.getP0().p, secondSelectedL.getP1().p);
					} else {
						uv2 = GeometryUtil.getUnitVector(secondSelectedL.getP1().p, secondSelectedL.getP0().p);
					}

					Vector2d combinedL1L2Vector = new Vector2d(crossPoint.p.x + uv1.x + uv2.x, crossPoint.p.y + uv1.y + uv2.y);
					Vector2d newUV = GeometryUtil.getUnitVector(crossPoint.p, combinedL1L2Vector);
					OriVertex bestCrossPoint = new OriVertex(GeometryUtil.getClosestCrossPoint(crossPoint.p, newUV));
					if (bestCrossPoint.p == null) {
						JOptionPane.showMessageDialog(this,  Origrammer.res.getString("Error_NoCrossPointFound"),
								"Error_NoCrossPointFound", JOptionPane.ERROR_MESSAGE);
					} else {
						Origrammer.diagram.steps.get(Globals.currentStep).addLine(new OriLine(crossPoint, bestCrossPoint, Globals.inputLineType));
					}
				}
				firstSelectedL = null;
				secondSelectedL = null;
			}
		}
	}
	
	public void createOriLinePerpendicular(MouseEvent e, Point2D.Double clickPoint) {
		OriLine l = pickOriLine(clickPoint);

		if (l != null) {
			Vector2d uv = GeometryUtil.getUnitVector(l.getP0().p, l.getP1().p);
			Vector2d nv = GeometryUtil.getNormalVector(uv);
			OriVertex v = new OriVertex(pickVertex(clickPoint));
			OriVertex v2 = null;
			//get point on OriLine that is closest to currentMousePointLogic, 
			//if currentMousePointLogic is not close to a vertex
			if (v.p == null) {
				v = new OriVertex();
				Vector2d cp = new Vector2d(clickPoint.x, clickPoint.y);
				GeometryUtil.DistancePointToSegment(cp,  l.getP0().p, l.getP1().p, v.p);
			}
			
			if (isReversePerpendicularLineInput) {
				nv.negate();
			}
			//get crossingPoint in order to get inputLine.P1
			//if there is no crossing point -> reverse inputLine direction
			v2 = new OriVertex(GeometryUtil.getClosestCrossPoint(v.p, nv));
			if (v2.p == null) {
				v2 = new OriVertex(GeometryUtil.getClosestCrossPoint(v.p, new Vector2d(-nv.x, -nv.y)));
			}
			if (v != null && v2 != null) {
				Origrammer.diagram.steps.get(Globals.currentStep).addLine(new OriLine(v, v2, Globals.inputLineType));
			}
		}

	}
	
	public void createTriangleInsector(MouseEvent e, Point2D.Double clickPoint) {
		Vector2d v = pickVertex(clickPoint);
		if (v == null) {
			if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
				OriLine l = pickOriLine(clickPoint);
				if (l != null) {
					v = new Vector2d();
					Vector2d cp = new Vector2d(clickPoint.x, clickPoint.y);
					GeometryUtil.DistancePointToSegment(cp, l.getP0().p, l.getP1().p, v);
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
				
				//creates Insector of the triangle with edge points {firstSelectedV, secondSelectedV, v}
				OriVertex incenter = new OriVertex(GeometryUtil.getIncenter(firstSelectedV, secondSelectedV, thirdSelectedV));
				if (incenter.p == null) {
					System.out.println("Failed to calculate the incenter of the triangle");
				} else {
					Origrammer.diagram.steps.get(Globals.currentStep).addLine(new OriLine(incenter, new OriVertex(firstSelectedV), Globals.inputLineType));
					Origrammer.diagram.steps.get(Globals.currentStep).addLine(new OriLine(incenter, new OriVertex(secondSelectedV), Globals.inputLineType));
					Origrammer.diagram.steps.get(Globals.currentStep).addLine(new OriLine(incenter, new OriVertex(thirdSelectedV), Globals.inputLineType));
				}
				
				firstSelectedV = null;
				secondSelectedV = null;
				thirdSelectedV = null;
			}
		}
	}
	
	public void createOriLineExtendToLine(MouseEvent e, Point2D.Double clickPoint) {
		
		Vector2d v = pickVertex(clickPoint);
		if (v == null) {
			if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
				OriLine l = pickOriLine(clickPoint);
				if (l != null) {
					v = new Vector2d();
					Vector2d cp = new Vector2d(clickPoint.x, clickPoint.y);
					GeometryUtil.DistancePointToSegment(cp, l.getP0().p, l.getP1().p, v);
				}
			}
		}
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;
			} else if (secondSelectedV == null) {
				secondSelectedV = v;

				Vector2d uv = GeometryUtil.getUnitVector(firstSelectedV, secondSelectedV);
				OriVertex crossPoint1 = new OriVertex(GeometryUtil.getClosestCrossPoint(firstSelectedV, uv));
				uv.negate();
				OriVertex crossPoint2 = new OriVertex(GeometryUtil.getClosestCrossPoint(crossPoint1.p, uv));
				
				Origrammer.diagram.steps.get(Globals.currentStep).addLine(new OriLine(crossPoint2, crossPoint1, Globals.inputLineType));
				
				firstSelectedV = null;
				secondSelectedV = null;
			}
		}
	}
	
	public void createOriLineLengthAngle(MouseEvent e, Point2D.Double clickPoint) {
		Vector2d v = pickVertex(clickPoint);
		if (v == null) {
			if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
				OriLine l = pickOriLine(clickPoint);
				if (l != null) {
					v = new Vector2d();
					Vector2d cp = new Vector2d(clickPoint.x, clickPoint.y);
					GeometryUtil.DistancePointToSegment(cp, l.getP0().p, l.getP1().p, v);
				}
			}
		}
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;

				if (Origrammer.mainFrame.uiTopPanel.inputLineLengthTF.getText().length() > 0 
						&& Origrammer.mainFrame.uiTopPanel.inputLineAngleTF.getText().length() > 0) {
					String lengthString = Origrammer.mainFrame.uiTopPanel.inputLineLengthTF.getText();
					String angleString = Origrammer.mainFrame.uiTopPanel.inputLineAngleTF.getText();

					Double length = Double.parseDouble(lengthString);
					Double angle = Double.parseDouble(angleString);

					angle = Math.toRadians(angle);
					OriVertex v2 = new OriVertex(length * Math.cos(angle) + firstSelectedV.x, length * Math.sin(angle) + firstSelectedV.y);
					Origrammer.diagram.steps.get(Globals.currentStep).addLine(new OriLine(new OriVertex(firstSelectedV), v2, Globals.inputLineType));
				} else {
					JOptionPane.showMessageDialog(this,  Origrammer.res.getString("Error_NoLengthAngleSpecified"),
							"Error_NoLengthAngleSpecified", JOptionPane.ERROR_MESSAGE);
				}

				firstSelectedV = null;
			}
		}
	}
	
	public void createOriLineMirrored(MouseEvent e, Point2D.Double clickPoint) {
		
		Vector2d v = pickVertex(clickPoint);
		if (v == null) {
			if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
				OriLine l = pickOriLine(clickPoint);
				if (l != null) {
					v = new Vector2d();
					Vector2d cp = new Vector2d(clickPoint.x, clickPoint.y);
					GeometryUtil.DistancePointToSegment(cp, l.getP0().p, l.getP1().p, v);
				}
			}
		}
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;
			} else if (secondSelectedV == null) {
				secondSelectedV = v;

				ArrayList<OriLine> tmpMirroredLineList = new ArrayList<OriLine>();

				for (OriLine l : Origrammer.diagram.steps.get(Globals.currentStep).lines) {
					if (l.isSelected()) {
						OriLine mirroredLine = GeometryUtil.mirrorLine(l, new OriLine(new OriVertex(firstSelectedV), new OriVertex(secondSelectedV), OriLine.TYPE_NONE));
						tmpMirroredLineList.add(mirroredLine);
					}
				}

				for (OriLine l : tmpMirroredLineList) {
					Origrammer.diagram.steps.get(Globals.currentStep).addLine(l);
				}
				
				firstSelectedV = null;
				secondSelectedV = null;
			}
		}
	}
	
	/**
	 * Automatically creates a fitting OriLine after placing an OriArrow
	 * @param v1 Vertex1 from the OriArrow
	 * @param v2 Vertex2 from the OriArrow
	 */
	private void autoCreateOriLine(Vector2d v1, Vector2d v2, int type, boolean isUnfold) {
		double length = GeometryUtil.Distance(v1, v2);
		Vector2d uv = GeometryUtil.getUnitVector(v1, v2);
		Vector2d nv = GeometryUtil.getNormalVector(uv);
		double halfLength = 0.5* length;
		
		Vector2d middleP = new Vector2d(v1.x + uv.x * halfLength, v1.y + uv.y * halfLength);
//		OriVertex lineV1 = new OriVertex(GeometryUtil.getClosestCrossPoint(middleP, nv));
//		nv.negate();
//		OriVertex lineV2 = new OriVertex(GeometryUtil.getClosestCrossPoint(middleP, nv));

		OriVertex lineFarV1 = new OriVertex(GeometryUtil.getFarthestCrossPoint(middleP, nv));
		nv.negate();
		OriVertex lineFarV2 = new OriVertex(GeometryUtil.getFarthestCrossPoint(middleP, nv));
		
		int linesCountPre = Origrammer.diagram.steps.get(Globals.currentStep).lines.size()-1;

		Origrammer.diagram.steps.get(Globals.currentStep).addNewLine(new OriLine(lineFarV1, lineFarV2, type));
		//if the fold is being unfolded immediately, don't auto fold it
		if (!isUnfold && Globals.automatedFolding) {
			makeAutoFold(v1, v2, lineFarV1.p, lineFarV2.p, linesCountPre);
		}
	}
	
	/**
	 * Automatically folds Vertex {@code v1} towards Vertex {@code v2} 
	 * and updates all connected lines
	 * @param v1
	 * @param v2
	 */
	private void makeAutoFold(Vector2d v1, Vector2d v2, Vector2d lineV1, Vector2d lineV2, int linesCountPre) {
		Origrammer.mainFrame.uiBottomPanel.stepForth();
		
		//set all OriLines with type OriLine.TYPE_VALLEY or OriLine.TYPE_MOUNTAIN to OriLine.TYPE_EDGE
		for (OriLine l : Origrammer.diagram.steps.get(Globals.currentStep).lines) {
			if (l.getType() == OriLine.TYPE_VALLEY || l.getType() == OriLine.TYPE_MOUNTAIN) {
				l.setType(OriLine.TYPE_EDGE);
			}
		}
		
		
		Vector2d foldingUV = GeometryUtil.getUnitVector(lineV1, lineV2);
		Vector2d foldingNV = GeometryUtil.getNormalVector(foldingUV);
		
		Vector2d closest;
		double distToV;
		Vector2d vertexMoveUv;
		//TODO: foldingLine doesn't go far enough (doesn't go through lines if there are more behind it)
		OriLine foldingLine = new OriLine(new OriVertex(lineV1), new OriVertex(lineV2), OriLine.TYPE_NONE); 

		System.out.println(Origrammer.diagram.steps.get(Globals.currentStep).polygons.size());
		for (OriPolygon p : Origrammer.diagram.steps.get(Globals.currentStep).polygons) {
			
			OriVertex curV = p.vertexList.head;
			int vCount = 0;
			do {
				
				if (GeometryUtil.isStrictLeft(lineV1, lineV2, curV.p)) {

					closest = GeometryUtil.getClosestPointOnLine(curV.p, foldingLine);
					distToV = GeometryUtil.Distance(curV.p, closest)*2;
					vertexMoveUv = GeometryUtil.getUnitVector(curV.p, closest);
					//p.vertexList.setVertex(curV, curV.p.x + vertexMoveUv.x*distToV, curV.p.y + vertexMoveUv.y*distToV);
					p.vertexList.removeVertex(curV);
					p.vertexList.addVertex(curV.p.x + vertexMoveUv.x*distToV, curV.p.y + vertexMoveUv.y*distToV, 5, 5);
					p.setHeight(1);
					curV = p.vertexList.head;
					vCount = 0;
				} else {
					curV = curV.next;
					vCount++;
				}

			} while(vCount < p.vertexList.n);


			//at the end, check vertexList.head again
			if (GeometryUtil.isStrictLeft(lineV1, lineV2, p.vertexList.head.p)) {
				closest = GeometryUtil.getClosestPointOnLine(p.vertexList.head.p, foldingLine);
				distToV = GeometryUtil.Distance(p.vertexList.head.p, closest)*2;
				vertexMoveUv = GeometryUtil.getUnitVector(p.vertexList.head.p, closest);
				p.vertexList.removeVertex(p.vertexList.head);
				p.vertexList.addVertex(p.vertexList.head.p.x + vertexMoveUv.x*distToV, p.vertexList.head.p.y + vertexMoveUv.y*distToV, 5, 5);
			}
			
		}
		
		Origrammer.diagram.steps.get(Globals.currentStep).addLinesFromVertices();
		

//		//check which vertices are on left side of the folding line and have to be updated
//		for (int i=0; i<Origrammer.diagram.steps.get(Globals.currentStep).lines.size(); i++) {
//			OriLine curLine = Origrammer.diagram.steps.get(Globals.currentStep).lines.get(i);
//			if (GeometryUtil.checkPointSideOfLine(lineV1, lineV2, curLine.getP0().p) == -1.0) {
//				if (!verticesToBeUpdated.contains(curLine.getP0())) {
//					verticesToBeUpdated.add(curLine.getP0());
//				}
//			}
//			if (GeometryUtil.checkPointSideOfLine(lineV1, lineV2, curLine.getP1().p) == -1.0) {
//				if (!verticesToBeUpdated.contains(curLine.getP1())) {
//					verticesToBeUpdated.add(curLine.getP1());
//				}	
//			}
//		}
		
//		verticesToBeUpdated = GeometryUtil.removeDuplicatesFromList(verticesToBeUpdated);
//		
//		
//		//update the positions of all OriVertex
//		//for (OriVertex testV : Origrammer.diagram.steps.get(Globals.currentStep).vertices) {
//		for(int i=0; i<Origrammer.diagram.steps.get(Globals.currentStep).vertices.size(); i++) {
//			OriVertex testV = Origrammer.diagram.steps.get(Globals.currentStep).vertices.get(i);
//			for (OriVertex checkV : verticesToBeUpdated) {
//				if (checkV.p.equals(testV.p)) {
//					closest = GeometryUtil.getClosestPointOnLine(testV.p, foldingLine);
//					distToV = GeometryUtil.Distance(testV.p, closest)*2;
//					Origrammer.diagram.steps.get(Globals.currentStep).vertices.remove(testV);
//					OriVertex inputVertex = new OriVertex(testV.p.x + foldingNV.x * distToV-offset, testV.p.y + foldingNV.y * distToV+offset);
//					Origrammer.diagram.steps.get(Globals.currentStep).vertices.add(inputVertex);
//					
//					//check for a given OriVertex, if any lines use it --> if yes, update them
//					for (OriLine l: Origrammer.diagram.steps.get(Globals.currentStep).lines) {
//						if (l.getP0().p.equals(testV.p)) {
//							System.out.println("updated existing p0");
//							l.setP0(inputVertex);
//						} else if (l.getP1().p.equals(testV.p)) {
//							System.out.println("updated existing p1");
//							l.setP1(inputVertex);
//						}
//					}
//				}
//			}
//		}

		//TODO: fix stuff for multiple creases after each other
		//TODO: rounding for really small numbers
		//TODO: line hierarchy --> prevent lines being split apart
		//TODO: render hierarchy --> render lines in right order
		//TODO: fix Exceptions!!!!
		
		Origrammer.diagram.steps.get(Globals.currentStep).arrows.clear();
		repaint();
	}
	
	
	public void createOriArrow(MouseEvent e, Point2D.Double clickPoint) {
		
		Vector2d v = pickVertex(clickPoint);
		if (v == null) {
			if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
				OriLine l = pickOriLine(clickPoint);
				if (l != null) {
					v = new Vector2d();
					Vector2d cp = new Vector2d(clickPoint.x, clickPoint.y);
					GeometryUtil.DistancePointToSegment(cp, l.getP0().p, l.getP1().p, v);
				}
			}
		}
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;
			} else if (secondSelectedV == null) {
				secondSelectedV = v;

				OriArrow tmpArrow = new OriArrow();

				tmpArrow.setP0(firstSelectedV);
				tmpArrow.setP1(secondSelectedV);
				tmpArrow.setUnfold(Origrammer.mainFrame.uiTopPanel.arrowIsUnfolded.isSelected());
				tmpArrow.setMirrored(Origrammer.mainFrame.uiTopPanel.arrowIsMirrored.isSelected());
				tmpArrow.setType(Globals.inputArrowType);
				tmpArrow.setSelected(false);

				Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
				Origrammer.diagram.steps.get(Globals.currentStep).addArrow(tmpArrow);
				
				if (Globals.automatedLinePlacement) {
					if (tmpArrow.getType() == OriArrow.TYPE_VALLEY) {
						autoCreateOriLine(firstSelectedV, secondSelectedV, OriLine.TYPE_VALLEY, tmpArrow.isUnfold());
					} else if (tmpArrow.getType() == OriArrow.TYPE_MOUNTAIN) {
						autoCreateOriLine(firstSelectedV, secondSelectedV, OriLine.TYPE_MOUNTAIN, tmpArrow.isUnfold());
					}
				}
				firstSelectedV = null;
				secondSelectedV = null;
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
		
		int type = 0;
		if (Globals.inputLineType == OriLine.TYPE_VALLEY) {
			type = OriArrow.TYPE_VALLEY;
		} else if (Globals.inputLineType == OriLine.TYPE_MOUNTAIN) {
			type = OriArrow.TYPE_MOUNTAIN;
		}
		Origrammer.diagram.steps.get(Globals.currentStep).addArrow(new OriArrow(arrowV1, arrowV2, type, false, false));
	}
	
	
	private void createRotationSymbol(Point2D.Double clickPoint) {
		Vector2d v = new Vector2d(clickPoint.x, clickPoint.y);
		
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;

				OriGeomSymbol tmpSymbol = new OriGeomSymbol();
				tmpSymbol.setP1(firstSelectedV);
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
				
				firstSelectedV = null;
			}
		}
	}
	
	private void createHoldSymbol(Point2D.Double clickPoint) {
		Vector2d v = new Vector2d(clickPoint.x, clickPoint.y);
		
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;

				OriGeomSymbol tmpSymbol = new OriGeomSymbol();
				tmpSymbol.setP1(firstSelectedV);
				tmpSymbol.setType(OriGeomSymbol.TYPE_HOLD);
				tmpSymbol.setSize(45); //TODO: adjustmentSlider on TopPanel
				Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
				Origrammer.diagram.steps.get(Globals.currentStep).addGeomSymbol(tmpSymbol);
				
				firstSelectedV = null;
			}
		}
	}
	
	private void createHoldAndPullSymbol(Point2D.Double clickPoint) {
		Vector2d v = new Vector2d(clickPoint.x, clickPoint.y);
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;
			} else if (secondSelectedV == null) {
				secondSelectedV = v;

				OriGeomSymbol tmpSymbol = new OriGeomSymbol();
				tmpSymbol.setP1(firstSelectedV);
				tmpSymbol.setP2(secondSelectedV);
				tmpSymbol.setType(OriGeomSymbol.TYPE_HOLD_AND_PULL); 
				tmpSymbol.setSize(45); //TODO: adjustmentSlider on TopPanel
				Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
				Origrammer.diagram.steps.get(Globals.currentStep).addGeomSymbol(tmpSymbol);
				
				firstSelectedV = null;
				secondSelectedV = null;
			}
		}
	}
	
	private void createNextViewHereSymbol(Point2D.Double clickPoint) {
		Vector2d v = new Vector2d(clickPoint.x, clickPoint.y);
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;
			} else if (secondSelectedV == null) {
				secondSelectedV = v;

				OriGeomSymbol tmpSymbol = new OriGeomSymbol();
				tmpSymbol.setP1(firstSelectedV);
				tmpSymbol.setP2(secondSelectedV);
				tmpSymbol.setType(OriGeomSymbol.TYPE_NEXT_VIEW_HERE);
				tmpSymbol.setSize(100); //TODO: adjustmentSlider on TopPanel
				Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
				Origrammer.diagram.steps.get(Globals.currentStep).addGeomSymbol(tmpSymbol);
				
				firstSelectedV = null;
				secondSelectedV = null;
			}
		}
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
		tmpGeomS.setP1(new Vector2d(sp.x, sp.y));
		tmpGeomS.setType(OriGeomSymbol.TYPE_XRAY_CIRCLE);
		if (width > height) {
			tmpGeomS.setSize(width);
		} else if (height > width) {
			tmpGeomS.setSize(height);
		}
		
		Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
		Origrammer.diagram.steps.get(Globals.currentStep).addGeomSymbol(tmpGeomS);
	}
	
	private void createClosedSinkSymbol(Point2D.Double clickPoint) {
		Vector2d v = new Vector2d(clickPoint.x, clickPoint.y);
		
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;

				OriGeomSymbol tmpSymbol = new OriGeomSymbol();
				tmpSymbol.setP1(firstSelectedV);
				tmpSymbol.setType(OriGeomSymbol.TYPE_CLOSED_SINK);
				tmpSymbol.setSize(10); //TODO: adjustmentSlider on TopPanel
				Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
				Origrammer.diagram.steps.get(Globals.currentStep).addGeomSymbol(tmpSymbol);

				firstSelectedV = null;
			}
		}
	}
	
	private void createLeaderSymbol(Point2D.Double clickPoint) {
		
		Vector2d v = new Vector2d(clickPoint.x, clickPoint.y);
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;
			} else if (secondSelectedV == null) {
				secondSelectedV = v;

				OriGeomSymbol tmpSymbol = new OriGeomSymbol();

				tmpSymbol.setP1(firstSelectedV);
				tmpSymbol.setP2(secondSelectedV);
				if (Origrammer.mainFrame.uiTopPanel.inputLeaderTextTF.getText().length() > 0) {
					tmpSymbol.setText(Origrammer.mainFrame.uiTopPanel.inputLeaderTextTF.getText());
				} else {
					JOptionPane.showMessageDialog(this,  Origrammer.res.getString("Error_EmptyLeaderTextField"),
							"Error_EmptyLeaderTextField", 
							JOptionPane.ERROR_MESSAGE);
				}

				tmpSymbol.setType(OriGeomSymbol.TYPE_LEADER);

				Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
				Origrammer.diagram.steps.get(Globals.currentStep).addGeomSymbol(tmpSymbol);
				
				firstSelectedV = null;
				secondSelectedV = null;
			}
		}
	}
	
	private void createRepetitionBoxSymbol(Point2D.Double clickPoint) {
		Vector2d v = new Vector2d(clickPoint.x, clickPoint.y);
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;
			} else if (secondSelectedV == null) {
				secondSelectedV = v;
				
				OriGeomSymbol tmpSymbol = new OriGeomSymbol();

				tmpSymbol.setP1(firstSelectedV);
				tmpSymbol.setP2(secondSelectedV);
				if (Origrammer.mainFrame.uiTopPanel.inputLeaderTextTF.getText().length() > 0) {
					tmpSymbol.setText(Origrammer.mainFrame.uiTopPanel.inputLeaderTextTF.getText());
				} else {
					JOptionPane.showMessageDialog(this,  Origrammer.res.getString("Error_EmptyLeaderTextField"),
							"Error_EmptyLeaderTextField", 
							JOptionPane.ERROR_MESSAGE);
				}

				tmpSymbol.setType(OriGeomSymbol.TYPE_REPETITION);

				Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
				Origrammer.diagram.steps.get(Globals.currentStep).addGeomSymbol(tmpSymbol);
				
				firstSelectedV = null;
				secondSelectedV = null;
			}
		}
	}

	private void createEqualDistSymbol(MouseEvent e, Point2D.Double clickPoint) {
		
		Vector2d v = pickVertex(clickPoint);
		if (v == null) {
			if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
				OriLine l = pickOriLine(clickPoint);
				if (l != null) {
					v = new Vector2d();
					Vector2d cp = new Vector2d(clickPoint.x, clickPoint.y);
					GeometryUtil.DistancePointToSegment(cp, l.getP0().p, l.getP1().p, v);
				}
			}
		}
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;
			} else if (secondSelectedV == null) {
				secondSelectedV = v;

				OriEqualDistSymbol tmpEquDistSymbol = new OriEqualDistSymbol();

				tmpEquDistSymbol.setTranslationDist(Origrammer.mainFrame.uiTopPanel.sliderEqualDist.getValue());
				tmpEquDistSymbol.setDividerCount(Integer.parseInt(Origrammer.mainFrame.uiTopPanel.equalDistDividerTF.getText()));
				tmpEquDistSymbol.setP0(firstSelectedV);
				tmpEquDistSymbol.setP1(secondSelectedV);

				Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
				Origrammer.diagram.steps.get(Globals.currentStep).addEqualDistSymbol(tmpEquDistSymbol);
				
				firstSelectedV = null;
				secondSelectedV = null;
			}
		}
	}

	private void createEqualAnglSymbol(MouseEvent e, Point2D.Double clickPoint) {
		
		Vector2d v = pickVertex(clickPoint);
		if (v == null) {
			if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
				OriLine l = pickOriLine(clickPoint);
				if (l != null) {
					v = new Vector2d();
					Vector2d cp = new Vector2d(clickPoint.x, clickPoint.y);
					GeometryUtil.DistancePointToSegment(cp, l.getP0().p, l.getP1().p, v);
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

				OriEqualAnglSymbol tmpEquAnglSymbol = new OriEqualAnglSymbol(firstSelectedV, secondSelectedV, thirdSelectedV);
				tmpEquAnglSymbol.setDividerCount(Integer.parseInt(Origrammer.mainFrame.uiTopPanel.equalAnglDividerTF.getText()));

				Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
				Origrammer.diagram.steps.get(Globals.currentStep).addEqualAngleSymbol(tmpEquAnglSymbol);
				
				firstSelectedV = null;
				secondSelectedV = null;
				thirdSelectedV = null;
			}
		}
	}

	private void createPleatCrimpSymbol(Point2D.Double clickPoint) {
		Vector2d v = new Vector2d(clickPoint.x, clickPoint.y);
		
		if (v != null) {
			if (firstSelectedV == null) {
				firstSelectedV = v;
				
				OriPleatCrimpSymbol tmpPCSymbol = new OriPleatCrimpSymbol(firstSelectedV, 
						Origrammer.mainFrame.uiTopPanel.pleatCB.isSelected(),
						Integer.parseInt(Origrammer.mainFrame.uiTopPanel.pleatTF.getText()));
				if (Origrammer.mainFrame.uiTopPanel.pleatRB.isSelected()) {
					tmpPCSymbol.setType(OriPleatCrimpSymbol.TYPE_PLEAT);
				} else {
					tmpPCSymbol.setType(OriPleatCrimpSymbol.TYPE_CRIMP);
				}
				Origrammer.diagram.steps.get(Globals.currentStep).pushUndoInfo();
				Origrammer.diagram.steps.get(Globals.currentStep).addPleatSymbol(tmpPCSymbol);
				
				firstSelectedV = null;
			}
		}
	}


	//#######################################################################################
	//############################   SELECT ON MOUSECLICK   #################################
	//#######################################################################################


	private void selectOnClickPoint(Point2D.Double clickPoint) {
		selectOriVertex(clickPoint);
		selectOriFace(clickPoint);
		selectOriLine(clickPoint);
		selectOriArrow(clickPoint);
		selectOriGeomSymbol(clickPoint);
		selectOriEqualDistSymbol(clickPoint);
		selectOriEqualAnglSymbol(clickPoint);
		selectOriPleatCrimpSymbol(clickPoint);
		selectOriPolygon(clickPoint);

		Origrammer.mainFrame.uiTopPanel.modeChanged();
	}
	
	private void selectOriVertex(Point2D.Double clickPoint) {
		//select OriVertex or unselect all OriVertices if clicked on nothing
		Vector2d vertex = pickVertex(clickPoint);
		if (vertex != null) {
			for (OriVertex v : Origrammer.diagram.steps.get(Globals.currentStep).vertices) {
				if (v.getP().x == vertex.x && v.getP().y == vertex.y) {
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
	
	private void selectOriFace(Point2D.Double clickPoint) {
		//select OriFace or unselect all OriFaces if clicked on nothing
		if (Globals.dispFilledFaces) {
			OriFace f = pickOriFace(clickPoint);
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
	
	private void selectOriLine(Point2D.Double clickPoint) {
		//select OriLine or unselect all OriLines if clicked on nothing
		OriLine l = pickOriLine(clickPoint);
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
	
	private void selectOriArrow(Point2D.Double clickPoint) {
		//select OriArrow or unselect all OriArrows if clicked on nothing
		OriArrow a = pickOriArrow(clickPoint);
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
	
	private void selectOriGeomSymbol(Point2D.Double clickPoint) {
		//select OriGeomSymbol or unselect all OriGeomSymbols if clicked on nothing
		OriGeomSymbol gs = pickOriGeomSymbol(clickPoint);
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
	
	private void selectOriEqualAnglSymbol(Point2D.Double clickPoint) {
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
		OriPleatCrimpSymbol pleat = pickPleatCrimpSymbol(clickPoint);
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
	
	private void selectOriPolygon(Point2D.Double clickPoint) {
		//select OriPleatSymbol or unselect all OriPleatSymbols if clicked on nothing
		OriPolygon poly = pickPolygon(clickPoint);
		if (poly != null) {
			if (!poly.isSelected()) {
				poly.setSelected(true);
			} else if (!isPressedOverSymbol) {
				poly.setSelected(false);
			}
		} else {
			Origrammer.diagram.steps.get(Globals.currentStep).unselectAllPolygons();
		}
	}
	
	//#######################################################################################
	//#######################################################################################
	
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
		OriLine l = pickOriLine(clickPoint);
		if (v == null && l != null) {
			double length = GeometryUtil.Distance(l.getP0().p, l.getP1().p);
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
		OriLine l = pickOriLine(clickPoint);
		if(v == null && l != null) {
			if (firstSelectedL == null) {
				firstSelectedL = l;
			} else {
				//double angle = GeometryUtil.measureAngle(firstSelectedL, l); //use for measuring the spanning angles between 2 lines
				double angle = GeometryUtil.measureAngleToXAxis(GeometryUtil.getUnitVector(firstSelectedL.getP0().p, firstSelectedL.getP1().p));
				Origrammer.mainFrame.uiSidePanel.measureAngleTF.setValue(Math.toDegrees(angle));
				firstSelectedL = null;
			}
		}
	}


	//#######################################################################################
	//###############################   MOUSE LISTENER   ####################################
	//#######################################################################################
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
				createOriArrow(e, clickPoint);
				break;
			case INPUT_SYMBOL:
				inputSymbolMode(e, clickPoint);
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
		//Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();
	}
	
	private void inputVertexMode(MouseEvent e, Point2D.Double clickPoint) {
		switch(Globals.vertexInputMode) {
			case ABSOLUTE:
				createVertexAbsolutePos(clickPoint);
				break;
			case FRACTION_OF_LINE:
				createVertexFractionOfLine(clickPoint);
				break;
			default:
				break;
		}
	}
	
	private void inputLineMode(MouseEvent e, Point2D.Double clickPoint) {
		switch(Globals.lineEditMode) {
			case INPUT_LINE:
				createOriLine(e, clickPoint);
				break;
			case ANGLE_BISECTOR:
				createOriLineAngleBisector(clickPoint);
				break;
			case PERPENDICULAR:
				createOriLinePerpendicular(e, clickPoint);
				break;
			case TRIANGLE_INSECTOR:
				createTriangleInsector(e, clickPoint);
				break;
			case EXTEND_TO_NEXT_LINE:
				createOriLineExtendToLine(e, clickPoint);
				break;
			case BY_LENGTH_AND_ANGLE:
				createOriLineLengthAngle(e, clickPoint);
			case MIRRORED:
				createOriLineMirrored(e, clickPoint);
			default:
				break;
		}
	}
	
	private void inputSymbolMode(MouseEvent e, Point2D.Double clickPoint) {
		switch(Globals.inputSymbolMode) {
			case LEADER:
				createLeaderSymbol(clickPoint);
				break;
			case REPETITION_BOX:
				createRepetitionBoxSymbol(clickPoint);
				break;
			case EQUAL_DIST:
				createEqualDistSymbol(e, clickPoint);
				break;
			case EQUAL_ANGL:
				createEqualAnglSymbol(e, clickPoint);
				break;
			case CRIMPING_PLEATING:
				createPleatCrimpSymbol(clickPoint);
				break;
			case SINKS:
				createClosedSinkSymbol(clickPoint);
				break;
			case FOLD_OVER_AND_OVER:
				//TODO: create the fold over and over symbol
				break;
			case HOLD_HERE:
				createHoldSymbol(clickPoint);
				break;
			case HOLD_HERE_AND_PULL:
				createHoldAndPullSymbol(clickPoint);
				break;
			case NEXT_VIEW:
				createNextViewHereSymbol(clickPoint);
				break;
			case ROTATIONS:
				createRotationSymbol(clickPoint);
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

			OriGeomSymbol pickedGeomSymbol = pickOriGeomSymbol(affineMouseDraggingPoint);
			if (pickOriGeomSymbol(currentMousePointLogic) != null && isPressedOverSymbol || isMovingSymbols) {
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
			
//			OriEqualAnglSymbol pickedEas = pickEqualAnglSymbol(affineMouseDraggingPoint);
//			if (pickEqualAnglSymbol(currentMousePointLogic) != null && isPressedOverSymbol || isMovingSymbols) { //TODO: moving EqualAngleSymbol
//				isMovingSymbols = true;
//				if (pickedEas != null) {
//
//					Vector2d pickV = pickVertex(normMousePointLogic);
//
//					if (pickV != null) {
//						xTrans = pickV.x - pickedEas.getV().x; 
//						yTrans = pickV.y - pickedEas.getV().y;
//						
//					} else {
//
//						xTrans = currentMousePointLogic.x - pickedEas.getV().x;
//						yTrans = currentMousePointLogic.y - pickedEas.getV().y;
//					}
//
//					preMousePoint = e.getPoint();
//					for (OriEqualAnglSymbol eas : Origrammer.diagram.steps.get(Globals.currentStep).equalAnglSymbols) {
//						if (eas.isSelected()) {
//							eas.moveBy(xTrans, yTrans);
//						}
//					}
//				}
//			}

			OriPleatCrimpSymbol pickedPleatSymbol = pickPleatCrimpSymbol(affineMouseDraggingPoint);
			if (pickPleatCrimpSymbol(currentMousePointLogic) != null && isPressedOverSymbol || isMovingSymbols) {
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
				|| Globals.toolbarMode == Constants.ToolbarMode.FILL_TOOL) {
			Vector2d firstV = selectedCandidateV;
				selectedCandidateV = this.pickVertex(currentMousePointLogic);

				if (selectedCandidateV == null) {
					if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
						OriLine l = pickOriLine(currentMousePointLogic);
						if (l != null) {
							selectedCandidateV = new Vector2d();
							Vector2d cp = new Vector2d(currentMousePointLogic.x, currentMousePointLogic.y);
							GeometryUtil.DistancePointToSegment(cp,  l.getP0().p, l.getP1().p, selectedCandidateV);
						}
					}
				}
				if (selectedCandidateV != firstV || firstSelectedV != null 
						|| Globals.lineEditMode == Constants.LineInputMode.BY_LENGTH_AND_ANGLE 
						|| Globals.inputSymbolMode == Constants.InputSymbolMode.ROTATIONS
						|| Globals.inputSymbolMode == Constants.InputSymbolMode.HOLD_HERE
						|| Globals.inputSymbolMode == Constants.InputSymbolMode.CRIMPING_PLEATING
						|| Globals.inputSymbolMode == Constants.InputSymbolMode.SINKS) {
					//repaint the step even when no vertex is detected
					repaint();
				}
		} else if (Globals.toolbarMode == Constants.ToolbarMode.SELECTION_TOOL) {
			//highlighting for all objects when moving over them in selection mode
			OriLine preLine = selectedCandidateL;
			selectedCandidateL = pickOriLine(currentMousePointLogic);
			if (preLine != selectedCandidateL) {
				repaint();
			}

			Vector2d preVertex = selectedCandidateV;
			selectedCandidateV = pickVertex(currentMousePointLogic);
			if (preVertex != selectedCandidateV) {
				repaint();
			}

			OriArrow preArrow = selectedCandidateA;
			selectedCandidateA = pickOriArrow(currentMousePointLogic);
			if (preArrow != selectedCandidateA) {
				repaint();
			}

			OriFace preFace = selectedCandidateF;
			selectedCandidateF = pickOriFace(currentMousePointLogic);
			if (preFace != selectedCandidateF) {
				repaint();
			}

			OriGeomSymbol preGeomSymbol = selectedCandidateGS;
			selectedCandidateGS = pickOriGeomSymbol(currentMousePointLogic);
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
			selectedCandidatePleat = pickPleatCrimpSymbol(currentMousePointLogic);
			if (prePleatSymbol != selectedCandidatePleat) {
				repaint();
			}
			
			OriPolygon prePolygon = selectedCandidatePolygon;
			selectedCandidatePolygon = pickPolygon(currentMousePointLogic);
			if (prePolygon != selectedCandidatePolygon) {
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
				selectedCandidateL = pickOriLine(currentMousePointLogic);
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
			

			if (Globals.lineEditMode == Constants.LineInputMode.PERPENDICULAR) {
				if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
					isReversePerpendicularLineInput = true;
				} else {
					isReversePerpendicularLineInput = false;
				}
			}
			//highlighting when moving over a line
			selectedCandidateL = this.pickOriLine(currentMousePointLogic);
			repaint();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		preMousePoint = e.getPoint();

		//mark OriArrow as selected if in SELECTION_TOOL mode and you have pressed left mouse button while over OriArrow -->
		//only needed for freely movable objects
		if (Globals.toolbarMode == Constants.ToolbarMode.SELECTION_TOOL) {
			OriArrow pickedArrow = pickOriArrow(currentMousePointLogic);
			if (pickedArrow != null) {
				pickedArrow.setSelected(true);
				isPressedOverSymbol = true;
				repaint();
			}

			OriGeomSymbol pickedGeomSymbol = pickOriGeomSymbol(currentMousePointLogic);
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

			OriPleatCrimpSymbol pickedPleatSymbol = pickPleatCrimpSymbol(currentMousePointLogic);
			if (pickedPleatSymbol != null) {
				pickedPleatSymbol.setSelected(true);
				isPressedOverSymbol = true;
				repaint();
			}
			
			OriPolygon pickedPolygon = pickPolygon(currentMousePointLogic);
			if (pickedPolygon != null) {
				pickedPolygon.setSelected(true);
				isPressedOverSymbol = true;
				repaint();
			}
		}
		Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();
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
				Line2D tmpL = new Line2D.Double(l.getP0().p.x, l.getP0().p.y, l.getP1().p.x, l.getP1().p.y);
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
		Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();
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