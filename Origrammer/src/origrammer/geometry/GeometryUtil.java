package origrammer.geometry;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;

import javax.vecmath.Vector2d;

import origrammer.Constants;
import origrammer.Globals;
import origrammer.Origrammer;

public class GeometryUtil {


	public static double Distance(Vector2d p0, Vector2d p1) {
		return Distance(p0.x, p0.y, p1.x, p1.y);
	}


	/**   __________________
	 *  \/(x0-x1)²+(y0-y1)²
	 */
	private static double Distance(double x0, double y0, double x1, double y1) {
		return Math.sqrt((x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1));

	}


	public static boolean isSameLineSegment(OriLine l0, OriLine l1) {
		if (Distance(l0.getP0().p, l1.getP0().p) < Constants.EPSILON && Distance(l0.getP1().p, l1.getP1().p) < Constants.EPSILON) {
			return true;
		} if (Distance(l0.getP0().p, l1.getP1().p) < Constants.EPSILON && Distance(l0.getP1().p, l1.getP0().p) < Constants.EPSILON) {
			return true;
		}
		return false;
	}	
	
	/**
	 * Checks if the line segments {@code line(p1,p2)} and {@code line(p3,p4)} are intersecting
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param p4
	 * @return {@code true} if the line segments are intersecting
	 */
	public static boolean isIntersecting(Vector2d p1, Vector2d p2, Vector2d p3, Vector2d p4) {
		Line2D.Double l1 = new Line2D.Double(p1.x, p1.y, p2.x, p2.y);
		Line2D.Double l2 = new Line2D.Double(p3.x, p3.y, p4.x, p4.y);
		
		return l1.intersectsLine(l2);
	}
	
	
	public static double DistancePointToSegment(Vector2d p, Vector2d sp, Vector2d ep) {
		Vector2d sub0, sub, sub0b;

		sub0 = new Vector2d(sp.x - p.x, sp.y - p.y);
		sub0b = new Vector2d(-sub0.x, -sub0.y);
		sub = new Vector2d(ep.x - sp.x, ep.y - sp.y);

		double t = ((sub.x * sub0b.x) + (sub.y * sub0b.y)) / ((sub.x * sub.x) + (sub.y * sub.y));
		
		if (t < 0.0) {
			return Distance(p.x, p.y, sp.x, sp.y);
		} else if (t > 1.0) {
			return Distance(p.x, p.y, ep.x, ep.y);
		} else {
			return Distance(sp.x + t * sub.x, sp.y + t * sub.y, p.x, p.y);
		}
	}
	
	public static double DistancePointToSegment(Vector2d p, Vector2d sp, Vector2d ep, Vector2d nearestPoint) {
		Vector2d sub0, sub, sub0b;

		sub0 = new Vector2d(sp.x - p.x, sp.y - p.y);
		sub0b = new Vector2d(-sub0.x, -sub0.y);
		sub = new Vector2d(ep.x - sp.x, ep.y - sp.y);

		double t = ((sub.x * sub0b.x) + (sub.y * sub0b.y)) / ((sub.x * sub.x) + (sub.y * sub.y));

		if (t < 0.0) {
			nearestPoint.set(sp);
			return Distance(p.x, p.y, sp.x, sp.y);
		} else if (t > 1.0) {
			nearestPoint.set(ep);
			return Distance(p.x, p.y, ep.x, ep.y);
		} else {
			nearestPoint.set(sp.x + t * sub.x, sp.y + t * sub.y);
			return Distance(sp.x + t * sub.x, sp.y + t * sub.y, p.x, p.y);
		}
	}
	
	/**
	 * 
	 * @param p Mouse Position
	 * @param s OriEqualDistSymbol that is tested for selection
	 * @return returns true if mouse is over OriEqualDistSymbol and false if it isn't
	 */
	public static boolean isMouseOverEqualDistSymbol(Point2D.Double p, OriEqualDistSymbol s) {
		Vector2d nv = GeometryUtil.getNormalVector(GeometryUtil.getUnitVector(s.getP0(), s.getP1()));

		double p0x = s.getP0Pos().x + 15*nv.x;
		double p0y = s.getP0Pos().y + 15*nv.y;

		double p1x = s.getP0Pos().x - 15*nv.x;
		double p1y = s.getP0Pos().y - 15*nv.y;

		double p2x = s.getP1Pos().x - 15*nv.x;
		double p2y = s.getP1Pos().y - 15*nv.y;

		double p3x = s.getP1Pos().x + 15*nv.x;
		double p3y = s.getP1Pos().y + 15*nv.y;

		Path2D.Double path = new Path2D.Double();
		path.moveTo(p0x, p0y);
		path.lineTo(p1x, p1y);
		path.lineTo(p2x, p2y);
		path.lineTo(p3x, p3y);
		path.closePath();

		return isMouseOverPath(p, path);
	}
	

	/**
	 * 
	 * @param p Mouse Position
	 * @param s Shape that is tested for selection
	 * @return True if the mouse is over the Shape and false if it isn't
	 */
	public static boolean isMouseOverShapes(Point2D.Double p, ArrayList<Shape> s) {
		for (Shape shape : s) {
			if (shape.intersects(p.x, p.y, 5, 5)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param p Mouse Position
	 * @param f OriFace that is tested for selection
	 * @return True if mouse is over the OriFace and false if it isn't
	 */
	public static boolean isMouseOverFace(Point2D.Double p, OriFace f) {
		return f.path.intersects(p.x, p.y, 5, 5);
	}
	
	/**
	 * 
	 * @param p Mouse Position
	 * @param path Path that is tested for selection
	 * @return True if mouse is over the Path and false if it isn't
	 */
	public static boolean isMouseOverPath(Point2D.Double p, Path2D.Double path) {
		return path.intersects(p.x, p.y, 5, 5);
	}
	
	/**
	 * 
	 * @param p Mouse Position
	 * @param rect Rectangle that is tested for selection
	 * @return True if mouse is over the Rectangle and false if it isn't
	 */
	public static boolean isMouseOverRectangle(Point2D.Double p, Rectangle rect) {
		return rect.intersects(p.x, p.y, 5, 5);
	}


	/**
	 * [a,b,c] = Distance()
	 * 
	 * 		            (ax1 + bx2 + cx3   ay1 + by2 + cy3)
	 * 	Incenter(x,y) = (--------------- , ---------------)
	 *                  (   a + b + c         a + b + c   )
	 * 
	 * @param v0 Vertex of the triangle
	 * @param v1 Vertex of the triangle
	 * @param v2 Vertex of the triangle
	 * @return The triangle incenter
	 */
	public static Vector2d getIncenter(Vector2d v0, Vector2d v1, Vector2d v2) {
		double l0 = Distance(v1, v2);
		double l1 = Distance(v0, v2);
		double l2 = Distance(v0, v1);
		
		Vector2d vc = new Vector2d();
		vc.x = (v0.x * l0 + v1.x * l1 + v2.x * l2) / (l0 + l1 + l2);
		vc.y = (v0.y * l0 + v1.y * l1 + v2.y * l2) / (l0 + l1 + l2);

		return vc;
	}
	
	
	/**
	 * Calculate the angle by three points
	 * 		    (b² + c² - a²)		a = Distance(C, B)
	 * cos(A) = --------------		b = Distance(A, C)
	 * 			     2bc			c = Distance(A, B)
	 * 
	 * @param v0
	 * @param v1
	 * @param v2
	 * @return The angle that v0 is the vertex of
	 */
	public static double measureAngle(Vector2d v0, Vector2d v1, Vector2d v2) {
		double angle = 0;
		double a = Distance(v2, v1);
		double b = Distance(v0, v2);
		double c = Distance(v0, v1);
		
		double cosA = (Math.pow(b, 2) + Math.pow(c, 2) - Math.pow(a, 2)) / (2 * b * c);
		angle = Math.toDegrees(Math.acos(cosA));
		
		return angle;
	}
	
	/**
	 * Calculate the angle between two OriLines
	 * @param l0
	 * @param l1
	 * @return The angle between l0 and l1
	 */
	public static double measureAngle(OriLine l0, OriLine l1) {
		Vector2d l0p0 = l0.getP0().p;
		Vector2d l0p1 = l0.getP1().p;
		Vector2d l1p0 = l1.getP0().p;
		Vector2d l1p1 = l1.getP1().p;

		Vector2d angleVertex;
		Vector2d v0;
		Vector2d v1;
		
		if (l0p0.x == l1p0.x && l0p0.y == l1p0.y) {
			angleVertex = l0p0;
			v0 = l0p1;
			v1 = l1p1;
		} else if (l0p0.x == l1p1.x && l0p0.y == l1p1.y) {
			angleVertex = l0p0;
			v0 = l0p1;
			v1 = l1p0;
		} else if (l0p1.x == l1p0.x && l0p1.y == l1p0.y) {
			angleVertex = l0p1;
			v0 = l0p0;
			v1 = l1p1;
		} else {
			angleVertex = l0p1;
			v0 = l0p0;
			v1 = l1p0;
		}

		return measureAngle(angleVertex, v0, v1);
	}
	
	/**
	 * Calculates the angle between a unitVector and the x-axis
	 * @param x
	 * @param y
	 * @return The angle in Radians
	 */
	public static double measureAngleToXAxis(Vector2d v) {
		double alpha = 0;
				
		if (v.x > 0) {
			alpha = Math.atan(v.y / v.x);
		} else if (v.y >= 0 && v.x < 0) {
			alpha = Math.atan(v.y / v.x) + Math.PI;
		} else if (v.y < 0 && v.x < 0) {
			alpha = Math.atan(v.y / v.x) - Math.PI;
		} else if (v.y > 0 && v.x == 0) {
			alpha = Math.PI / 2;
		} else if (v.y < 0 && v.x == 0) {
			alpha = - (Math.PI / 2);
		} else if (v.y == 0 && v.x == 0) {
		}
		return alpha;
	}
	
	/**
	 * Creates a GeneralPath from an ArrayList<Vector2d> of vertices for the use in OriFilledFace
	 * @param  vList ArrayList<Vector2d>  for the Path
	 * @return GeneralPath
	 */
	public static GeneralPath createPathFromVertices(ArrayList<Vector2d> vList) {
		GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, vList.size());
		path.moveTo(vList.get(0).x, vList.get(0).y);

		for (int i = 1; i < vList.size(); i++) {
			path.lineTo(vList.get(i).x, vList.get(i).y);
		}
		path.closePath();

		return path;
	}
	
	public static ArrayList<double[]> getPointsFromGeneralPath(GeneralPath path) {
		ArrayList<double[]> pointList = new ArrayList<double[]>();
		double[] coords = new double[6];
		int numSubPaths = 0;
		for (PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next()) {
			switch (pi.currentSegment(coords)) {
			case PathIterator.SEG_MOVETO:
				pointList.add(Arrays.copyOf(coords, 2));
				++ numSubPaths;
				break;
			case PathIterator.SEG_LINETO:
				pointList.add(Arrays.copyOf(coords, 2));
				break;
			case PathIterator.SEG_CLOSE:
				if (numSubPaths > 1) {
					throw new IllegalArgumentException("Path contains multiple subpaths");
				}
				return pointList;
			default:
				throw new IllegalArgumentException("Path contains curves");
			}
		}
		throw new IllegalArgumentException("Unclosed path");
	}
	
	/**
	 * Creates a GeneralPath from three lines
	 * @param l0 OriLine 
	 * @param l1 OriLine
	 * @param l2 OriLine
	 * @return GeneralPath
	 * @deprecated Use createPathFromVertices instead and get the vertices from the lines
	 */
	@Deprecated
	public static GeneralPath createPathFromLines(OriLine l0, OriLine l1, OriLine l2) {
		ArrayList<OriLine> lines = new ArrayList<>();
		ArrayList<Vector2d> points = new ArrayList<>();
		lines.add(l0);
		lines.add(l1);
		lines.add(l2);

		for(int i = 1; i < lines.size(); i++) {
			if (lines.get(0).getP0().p.x == lines.get(i).getP0().p.x && lines.get(0).getP0().p.y == lines.get(i).getP0().p.y) {
				points.add(lines.get(0).getP0().p);
			} else if (lines.get(0).getP0().p.x == lines.get(i).getP1().p.x && lines.get(0).getP0().p.y == lines.get(i).getP1().p.y) {
				points.add(lines.get(0).getP0().p);
			} else if (lines.get(0).getP1().p.x == lines.get(i).getP0().p.x && lines.get(0).getP1().p.y == lines.get(i).getP0().p.y) {
				points.add(lines.get(0).getP1().p);
			} else if (lines.get(0).getP1().p.x == lines.get(i).getP1().p.x && lines.get(0).getP1().p.y == lines.get(i).getP1().p.y) {
				points.add(lines.get(0).getP1().p);
			}
		}

		for (int j = 2; j < lines.size(); j++) {
			if (lines.get(1).getP0().p.x == lines.get(j).getP0().p.x && lines.get(1).getP0().p.y == lines.get(j).getP0().p.y) {
				points.add(lines.get(1).getP0().p);
			} else if (lines.get(1).getP0().p.x == lines.get(j).getP1().p.x && lines.get(1).getP0().p.y == lines.get(j).getP1().p.y) {
				points.add(lines.get(1).getP0().p);
			} else if (lines.get(1).getP1().p.x == lines.get(j).getP0().p.x && lines.get(1).getP1().p.y == lines.get(j).getP0().p.y) {
				points.add(lines.get(1).getP1().p);
			} else if (lines.get(1).getP1().p.x == lines.get(j).getP1().p.x && lines.get(1).getP1().p.y == lines.get(j).getP1().p.y) {
				points.add(lines.get(1).getP1().p);
			}
		}

		GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, points.size());
		path.moveTo(points.get(0).x, points.get(0).y);

		for (int i = 0; i < points.size(); i++) {
			path.lineTo(points.get(i).x, points.get(i).y);
		}
		path.closePath();

		return path;
	}
	
	/**
	 * Calculates the new edge points of a Rectangle after rotating
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param degrees
	 * @return The rotated Rectangle2D
	 */
	public static Rectangle2D calcRotatedBox(double x, double y, double width, double height, double degrees) {
		Vector2d v0 = new Vector2d(x, y);
		Vector2d v1 = new Vector2d(x + width, y);
		Vector2d v2 = new Vector2d(x, y + height);
		Vector2d v3 = new Vector2d(x + width, y + height);

		Vector2d newV0 = rotVertex(v0, degrees);
		Vector2d newV1 = rotVertex(v1, degrees);
		Vector2d newV2 = rotVertex(v2, degrees);
		Vector2d newV3 = rotVertex(v3, degrees);
		
		ArrayList<Vector2d> vertexList = new ArrayList<>();
		ArrayList<Vector2d> newEdgePoints = new ArrayList<>();
		
		vertexList.add(newV0);
		vertexList.add(newV1);
		vertexList.add(newV2);
		vertexList.add(newV3);
		
		double smallestX = 0;
		double biggestX = 0;
		double smallestY = 0;
		double biggestY = 0;
				
		for (int i = 0; i<vertexList.size(); i++) {
			if (i == 0) {
				smallestX = vertexList.get(i).x;
				smallestY = vertexList.get(i).y;
			}
			
			if (vertexList.get(i).x < smallestX) {
				smallestX = vertexList.get(i).x;
			} else if (vertexList.get(i).x > biggestX) {
				biggestX = vertexList.get(i).x;
			}
			
			if (vertexList.get(i).y < smallestY) {
				smallestY = vertexList.get(i).y;
			} else if (vertexList.get(i).y > biggestY) {
				biggestY = vertexList.get(i).y;
			}
		}
		
		newEdgePoints.add(new Vector2d(smallestX,biggestY));
		newEdgePoints.add(new Vector2d(biggestX, smallestY));
		
		double newWidth = biggestX - smallestX;
		double newHeight = biggestY - smallestY;
		
		Rectangle2D rect = new Rectangle((int) Math.round(smallestX), (int) Math.round(biggestY), (int) Math.round(newWidth), (int) Math.round(newHeight));

		return rect;
	}
	
	/**
	 * Calculates the position of a vertex after rotating
	 * @param v
	 * @param degrees
	 * @return New Vertex position
	 */
	public static Vector2d rotVertex(Vector2d v, double degrees) {
		double newX = v.x * Math.cos(Math.toRadians(degrees)) - v.y * Math.sin(Math.toRadians(degrees));
		double newY = v.x * Math.sin(Math.toRadians(degrees)) + v.y * Math.cos(Math.toRadians(degrees));
		
		return new Vector2d(newX, newY);
	}
	
	

	/**				  
	 * Calculates the unitVector
	 * <p>
	 *    p1 - p0  <br>
	 *   --------- <br>
	 *   |p1 - p0|
	 * @param p0
	 * @param p1
	 * @return unitVector of line(p0, p1)
	 */
	public static Vector2d getUnitVector(Vector2d p0, Vector2d p1) {
		Vector2d normal = new Vector2d();
		normal.set(p1);
		normal.sub(p0);

		Vector2d uv = new Vector2d();
		uv.set(normal);
		uv.normalize();
		
		if (Double.isNaN(uv.x) || Double.isNaN(uv.y)) {
			uv.x = 0;
			uv.y = 0;
		}
		return uv;
	}
	
	/**
	 * Calculates the normalVector from the unitVector <p>
	 * Rotate unitVector by 90 degrees to get normalVector <br>
	 * 	x' = x * cos(angle) - y * sin(angle) <br>
	 * 	y' = x * sin(angle) + y * cos(angle) <br>
	 * @param uv
	 * @return normalVector of line(p0,p1)
	 */
	public static Vector2d getNormalVector(Vector2d uv) {
		double angleRadian = Math.toRadians(90);
		
		double rx = (uv.x * Math.cos(angleRadian)) - (uv.y * Math.sin(angleRadian));
		double ry = (uv.x * Math.sin(angleRadian)) + (uv.y * Math.cos(angleRadian));
		Vector2d nv = new Vector2d(rx, ry);

		return nv;
	}
	
	
	
	/**
	 * Gets the crossingPoint between two lines.
	 * @param l0
	 * @param l1
	 * @return {@code Vector2d crossingPoint}<br>
	 * 		   {@code null} if the two lines don't cross
	 */
	public static Vector2d getCrossPoint(OriLine l0, OriLine l1) {
		return getCrossPoint(l0.getP0().p, l0.getP1().p, l1.getP0().p, l1.getP1().p);
	}
	
	/**
	 * Gets the crossingPoint between {@code line(p0, p1)} and {@code line(p2, p3)}.
	 * @param l0
	 * @param l1
	 * @return {@code Vector2d crossingPoint}<br>
	 * 		   {@code null} if the two lines don't cross
	 */
	public static Vector2d getCrossPoint(Vector2d p1, Vector2d p2, Vector2d p3, Vector2d p4) {
		
		// Line AB represented as a1x + b1y = c1 
        double a1 = p2.y - p1.y; 
        double b1 = p1.x - p2.x; 
        double c1 = a1*(p1.x) + b1*(p1.y); 
       
        // Line CD represented as a2x + b2y = c2 
        double a2 = p4.y - p3.y; 
        double b2 = p3.x - p4.x; 
        double c2 = a2*(p3.x)+ b2*(p3.y); 
       
        double determinant = a1*b2 - a2*b1; 
       
        if (determinant == 0) { 
            //lines are parallel
            return null;
        } else { 
            double x = (b2*c1 - b1*c2) / determinant; 
            double y = (a1*c2 - a2*c1) / determinant;
            Vector2d cross = new Vector2d(x, y);
            
            //check if the found crossPoint is actually part of the line segment
            double distL1 = Distance(p1, p2);
            double distL2 = Distance(p3, p4);
            double distP1Cross = Distance(p1, cross);
            double distP2Cross = Distance(p2, cross);
            double distP3Cross = Distance(p3, cross);
            double distP4Cross = Distance(p4, cross);
            
            if (distL1 < distP1Cross || distL1 < distP2Cross || distL2 < distP3Cross || distL2 < distP4Cross) {
            	return null; //crossPoint is outside of the line segments
            }
            return cross; 
        } 
	}
	
	
	/**
	 * Gets the closest crossing point of all polygons that are on the specified {@code height}
	 * @param p1
	 * @param uv
	 * @param height
	 * @return
	 */
	public static Vector2d getClosestCrossPoint(Vector2d p1, Vector2d uv, int height) {
		double dist = 0;
		double smallestDist = 1000; //TODO: make it more elegant and not with fixed value
		Vector2d bestCrossPoint = null;
		
		for (OriPolygon p : Origrammer.diagram.steps.get(Globals.currentStep).polygons) {
			if (p.getHeight() == height) {
				for (OriLine l : p.lines) {
					Vector2d crossPoint2 = GeometryUtil.getCrossPoint(l, new OriLine(new OriVertex(p1), new OriVertex(p1.x + uv.x * 900, p1.y + uv.y * 900), Globals.inputLineType));
					if (crossPoint2 != null) {
						if (!crossPoint2.equals(p1)) {
							//check if crossPoint2 is too close to bestCrossPoint
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
			}
		}
		return bestCrossPoint;
	}
	
	/**
	 * Returns the farthest {@code crossingPoint} of a line with origin {@code p1} and direction {@code uv} at {@code height}}.
	 * Checks all lines of the current diagram step at {@code height}
	 * @param p1
	 * @param uv
	 * @return
	 */
	public static Vector2d getFarthestCrossPoint(Vector2d p1, Vector2d uv, int height) {
		double dist = 0;
		double biggestDist = 0;
		Vector2d bestCrossPoint = null;

		for (OriPolygon p : Origrammer.diagram.steps.get(Globals.currentStep).polygons) {
			if (p.getHeight() == height) {
				for (OriLine l : p.lines) {
					Vector2d crossPoint2 = GeometryUtil.getCrossPoint(l, new OriLine(new OriVertex(p1), new OriVertex(p1.x + uv.x * 900, p1.y + uv.y * 900), Globals.inputLineType));
					if (crossPoint2 != null) {
						if (!(GeometryUtil.closeCompare(p1.x, crossPoint2.x, Constants.EPSILON)
								&& GeometryUtil.closeCompare(p1.y, crossPoint2.y, Constants.EPSILON))) {
							dist = GeometryUtil.Distance(p1, crossPoint2);
							if (dist > biggestDist) {
								biggestDist = dist;
								bestCrossPoint = crossPoint2;
							}
						}
					}
				}
			}
		}

		return bestCrossPoint;
	}
	
	
	public static ArrayList<OriPolygon> getCrossPointsUntilEdge(OriLine l) {
		return Origrammer.diagram.steps.get(Globals.currentStep).sharedLines.get(l);
	}
	
	/**
	 * Returns the closest {@code crossingPoint} of a line with origin on {@code p1} and direction {@code uv}.
	 * Checks all lines in the current diagram step
	 * @param p1
	 * @param uv
	 * @return Vector2d crossPoint
	 */
	public static Vector2d getClosestCrossPoint(Vector2d p1, Vector2d uv) {
		double dist = 0;
		double smallestDist = 1000; //TODO: make it more elegant and not with fixed value
		Vector2d bestCrossPoint = null;
		
		if (Globals.virtualFolding) {
			//Check all lines of the polygons for the earliest intersection with the linepoint p1 + direction uv
			for (OriPolygon p : Origrammer.diagram.steps.get(Globals.currentStep).polygons) {
				for (OriLine l : p.lines) {
					Vector2d crossPoint2 = GeometryUtil.getCrossPoint(l, new OriLine(new OriVertex(p1), new OriVertex(p1.x + uv.x * 900, p1.y + uv.y * 900), Globals.inputLineType));
					if (crossPoint2 != null && !crossPoint2.equals(p1)) {
						//check if crossPoint2 is too close to bestCrossPoint
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
		} else {
			//check all OriLines for the earliest intersection with the new AngleBisectorLine
			//set the first intersection as P2 of the AngleBisectorLine
			for (OriLine tmpLine : Origrammer.diagram.steps.get(Globals.currentStep).lines) {
				Vector2d crossPoint2 = GeometryUtil.getCrossPoint(tmpLine, new OriLine(new OriVertex(p1), new OriVertex(p1.x + uv.x * 900, p1.y + uv.y * 900), Globals.inputLineType));
				if (crossPoint2 != null) {
					if (!crossPoint2.equals(p1)) {
						//check if crossPoint2 is too close to bestCrossPoint
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
		}
		return bestCrossPoint;
	}
	
	
	/**
	 * Returns the farthest {@code crossingPoint} of a line with origin {@code p1} and direction {@code uv}.
	 * Checks all lines of the current diagram step
	 * @param p1
	 * @param uv
	 * @return
	 */
	public static Vector2d getFarthestCrossPoint(Vector2d p1, Vector2d uv) {
		double dist = 0;
		double biggestDist = 0;
		Vector2d bestCrossPoint = null;
		int highestPolygon = Origrammer.diagram.steps.get(Globals.currentStep).getHighestPolygonHeight();
		
		for (int i=highestPolygon; i>=0; i--) {
			for (OriPolygon p : Origrammer.diagram.steps.get(Globals.currentStep).polygons) {

				if (p.getHeight() == i) {
					for (OriLine l : p.lines) {
						Vector2d crossPoint2 = GeometryUtil.getCrossPoint(l, new OriLine(new OriVertex(p1), new OriVertex(p1.x + uv.x * 900, p1.y + uv.y * 900), Globals.inputLineType));
						if (crossPoint2 != null) {
							if (!(GeometryUtil.closeCompare(p1.x, crossPoint2.x, Constants.EPSILON)
									&& GeometryUtil.closeCompare(p1.y, crossPoint2.y, Constants.EPSILON))) {
								if (l.getType() == OriLine.EDGE) {
									return crossPoint2;
								}
								dist = GeometryUtil.Distance(p1, crossPoint2);
								if (dist > biggestDist) {
									biggestDist = dist;
									bestCrossPoint = crossPoint2;
								}
							}
						}
					}
				}
			}
		}
		return bestCrossPoint;
	}
	
	/**
	 * Gets the point on {@code line} that is closest to {@code vertex}.
	 * @param vertex
	 * @param line
	 * @return
	 */
	public static Vector2d getClosestPointOnLine(Vector2d vertex, OriLine line) {
		/**    |    ° = vertex
		 *     |    | = line
		 * °---p    p = closestPointOnLine
		 *     |  
		 *     | 
		 */
		Vector2d uv = getUnitVector(line.getP0().p, line.getP1().p);
		Vector2d nv = getNormalVector(uv);
		
		Vector2d newLp1 = new Vector2d(line.getP1().p.x + uv.x*500, line.getP1().p.y + uv.y * 500);
		uv.negate();
		Vector2d newLp0 = new Vector2d(line.getP0().p.x + uv.x*500, line.getP0().p.y + uv.y * 500);
				
		Vector2d crossPoint = null;
		
		crossPoint = GeometryUtil.getCrossPoint(new OriLine(new OriVertex(newLp0), new OriVertex(newLp1), OriLine.NONE), 
				new OriLine(new OriVertex(vertex), new OriVertex(vertex.x + nv.x * 900, vertex.y + nv.y * 900), OriLine.NONE));

		//if no crossPoint was found, try the other direction
		if (crossPoint == null) {
			nv.negate();
			crossPoint = GeometryUtil.getCrossPoint(line, new OriLine(new OriVertex(vertex), new OriVertex(vertex.x + nv.x * 900, vertex.y + nv.y * 900), Globals.inputLineType));
		}
		
		//if a crossPoint was finally found, round it to 10 decimal places
		if (crossPoint != null) {
			crossPoint = round(crossPoint, 10);
		}
		
		return crossPoint;
	}
	
	/**
	 * Returns the closest {@code line} of the current Step, that is crossing the input {@code p1} and direction {@code uv}
	 * @param vertex
	 * @param uv
	 * @return OriLine that crosses
	 */
	public static OriLine getClosestCrossLine(Vector2d vertex, Vector2d uv) {
		//check all OriLines for the earliest intersection with p1 and direction uv
		double bestDist = 10000;
		OriLine bestLine = null;
		for (OriLine tmpLine : Origrammer.diagram.steps.get(Globals.currentStep).lines) {

			Vector2d crossPoint2 = GeometryUtil.getCrossPoint(tmpLine, new OriLine(new OriVertex(vertex), new OriVertex(vertex.x + uv.x * 900, vertex.y + uv.y * 900), Globals.inputLineType));
			if (crossPoint2 != null) {
				if (!crossPoint2.equals(vertex)) {
					double newDist = Distance(crossPoint2, vertex);
					if (newDist < bestDist) {
						bestDist = newDist;
						bestLine =  tmpLine;
					}
				}
			}
		}
		for (OriLine tmpLine : Origrammer.diagram.steps.get(Globals.currentStep).edgeLines) {

			Vector2d crossPoint2 = GeometryUtil.getCrossPoint(tmpLine, new OriLine(new OriVertex(vertex), new OriVertex(vertex.x + uv.x * 900, vertex.y + uv.y * 900), Globals.inputLineType));
			if (crossPoint2 != null) {
				if (!crossPoint2.equals(vertex)) {
					double newDist = Distance(crossPoint2, vertex);
					if (newDist < bestDist) {
						bestDist = newDist;
						bestLine =  tmpLine;
					}
				}
			}
		}
		return bestLine;
	}
	
	/**
	 * Returns {@code true} if {@code a} and {@code b} are within {@code tolerance} of each other
	 * @param a
	 * @param b
	 * @param tolerance
	 * @return
	 */
	public static boolean closeCompare(double a, double b, double tolerance) {
		if (Math.abs(a-b) <= tolerance) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Checks if {@code OriVertex vertex} is already part of the current diagram step.<br>
	 * A new Vertex is only added to the {@code vertices}-list, if it isn't close or the same as any existing vertices.
	 * Checks within tolerance of {@code Constants.EPSILON}.
	 * 
	 * @param vertex
	 * @return {@code true} if {@code vertex} is already in the {@code vertices} list of the current step <br>
	 * {@code false} if {@code vertex} isn't yet in the {@code vertices} list of the current step
	 */
	public static boolean closeCompareOriVertex(OriVertex vertex, OriVertex v) {
			if (closeCompare(vertex.p.x, v.p.x, Constants.EPSILON) 
					&& closeCompare(vertex.p.y, v.p.y, Constants.EPSILON)) {
				return true;
			}
		return false;
	}
	
	
	public static GeneralPath getPathFromLineList(ArrayList<OriLine> list) {
		GeneralPath path = new GeneralPath();
		Vector2d startP = null;
		Vector2d lastP = null;

		OriLine curL;
		//for (OriLine l : list) {
		for (int i=0; i<list.size(); i++) {
			curL = list.get(i);
			if (startP == null && lastP == null) { //initialize the path in the beginning
				path.moveTo(curL.getP0().p.x, curL.getP0().p.y);
				path.lineTo(curL.getP1().p.x, curL.getP1().p.y);
				list.remove(i);
				startP = curL.getP0().p;
				lastP = curL.getP1().p;
				i = -1;
			}else if (lastP.equals(curL.getP0().p)) { //if lastP and curL.P0 are the same
				if (curL.getP1().p.equals(startP)) { //if path is about to be closed
					path.closePath();
				} else {
					path.lineTo(curL.getP1().p.x, curL.getP1().p.y);
					lastP = curL.getP1().p;
					list.remove(i);
					i = -1;
				}
				
			} else if (lastP.equals(curL.getP1().p)) {//if lastP and curL.P1 are the same
				if (curL.getP0().p.equals(startP)) { //if path is about to be closed
					path.closePath();
				} else {
					path.lineTo(curL.getP0().p.x, curL.getP0().p.y);
					lastP = curL.getP0().p;
					list.remove(i);
					i = -1;
				}
			}
		}
		return path;	
	}
	
	public static GeneralPath getFillingSegment(Vector2d v) {
		Vector2d uvUp = new Vector2d(0, -1);
		OriLine closestCrossLine = getClosestCrossLine(v, uvUp);
		ArrayList<OriLine> list = new ArrayList<OriLine>();
		for (OriLine l : Origrammer.diagram.steps.get(Globals.currentStep).lines) {
			if (list.contains(l)) {
				System.out.println("OriLine is already in the list for FilledFace");
			} else if (l == closestCrossLine) {
				list.addAll(getConnectedLines(l, v));
				break;
			}
		}

		GeneralPath path = getPathFromLineList(list);
		return path;
	}
	
	private static ArrayList<OriLine> getConnectedLines(OriLine l1, Vector2d clickPoint) {
		ArrayList<OriLine> list = new ArrayList<OriLine>(); 
		list.add(l1);
		Vector2d vertex = new Vector2d(clickPoint);
		
		OriLine prevLine = l1;
		for (int i=0; i<Origrammer.diagram.steps.get(Globals.currentStep).lines.size(); i++) {
			OriLine curL = Origrammer.diagram.steps.get(Globals.currentStep).lines.get(i);
			Vector2d l1P0 = prevLine.getP0().p;
			Vector2d l1P1 = prevLine.getP1().p;
			Vector2d l2P0 = curL.getP0().p;
			Vector2d l2P1 = curL.getP1().p;

			if (prevLine == curL) {
				System.out.println("OriLine was already used in the last iteration");
			} else if (list.contains(curL)) {
				System.out.println("OriLine is already in the list for FilledFace");
			} else if (l1P0.equals(l2P0)) {
				if (checkIfUnobstructedPathPossible(curL, vertex)) {
					list.add(curL);
					prevLine = curL;
					i = -1;
				}
				
			} else if (l1P0.equals(l2P1)) {
				if (checkIfUnobstructedPathPossible(curL, vertex)) {
					list.add(curL);
					prevLine = curL;
					i = -1;
				}
			} else if (l1P1.equals(l2P0)) {
				if (checkIfUnobstructedPathPossible(curL, vertex)) {
					list.add(curL);
					prevLine = curL;
					i = -1;
				}
			} else if (l1P1.equals(l2P1)) {
				if (checkIfUnobstructedPathPossible(curL, vertex)) {
					list.add(curL);
					prevLine = curL;
					i = -1;
				}
			}
		}
		return list;
	}
	
	/**
	 * Check if the line can be seen from point p without any other blocking {@code OriLines}
	 * @param line
	 * @param vertex
	 * @return
	 */
	private static boolean checkIfUnobstructedPathPossible(OriLine line, Vector2d vertex) {
		Vector2d middlePoint = getLineMiddlePoint(line);
		Vector2d testUv = getUnitVector(vertex, middlePoint);
		OriLine testLine = getClosestCrossLine(vertex, testUv);

		if (line == testLine) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Calculates the middlePoint of {@code OriLine l}.
	 * @param l
	 * @return
	 */
	public static Vector2d getLineMiddlePoint(OriLine l) {
		return getMiddlePointBetweenPoints(l.getP0().p, l.getP1().p);
	}
	
	/**
	 * Calculates the middlePoint of the {@code line(v0, v1)}.
	 * @param v0
	 * @param v1
	 * @return
	 */
	public static Vector2d getMiddlePointBetweenPoints(Vector2d v0, Vector2d v1) {
		Vector2d uv = getUnitVector(v0, v1);
		double halfdist = Distance(v0, v1) / 2;
		Vector2d middlePoint = new Vector2d(v0.x + uv.x * halfdist, v0.y + uv.y * halfdist);
		return middlePoint;
	}
	
	
	/**
	 * Mirrors an {@code OriLine in} along the {@code OriLine mirror}.
	 * @param in
	 * @param mirror
	 * @return the mirrored {@code OriLine}
	 */
	public static OriLine mirrorLine(OriLine in, OriLine mirror) {
		Vector2d uv = getUnitVector(mirror.getP0().p, mirror.getP1().p);
		Vector2d nv = getNormalVector(uv);
		Vector2d cross1 = null;
		Vector2d cross2 = null;
		cross1 = getCrossPoint(new OriLine(in.getP0(), new OriVertex(in.getP0().p.x + nv.x*1000, in.getP0().p.y + nv.y*1000), OriLine.NONE), mirror);
		if (cross1 == null) {
			nv.negate();
			cross1 = getCrossPoint(new OriLine(in.getP0(), new OriVertex(in.getP0().p.x + nv.x*1000, in.getP0().p.y + nv.y*1000), OriLine.NONE), mirror);
		}
		double dist1 = Distance(in.getP0().p, cross1);
		OriVertex newP0 = new OriVertex(cross1.x + nv.x * dist1, cross1.y + nv.y * dist1);


		cross2 = getCrossPoint(new OriLine(in.getP1(), new OriVertex(in.getP1().p.x + nv.x*1000, in.getP1().p.y + nv.y*1000), OriLine.NONE), mirror);
		if (cross2 == null) {
			nv.negate();
			cross2 = getCrossPoint(new OriLine(in.getP1(), new OriVertex(in.getP1().p.x + nv.x*1000, in.getP1().p.y + nv.y*1000), OriLine.NONE), mirror);
		}
		double dist2 = Distance(in.getP1().p, cross2);
		OriVertex newP1 = new OriVertex(cross2.x + nv.x * dist2, cross2.y + nv.y * dist2);

		return new OriLine(newP0, newP1, in.getType());
	}

	/**
	 * Checks on which side of {@code OriLine l} the point {@code Vector2d p} lies.
	 * @param l
	 * @param p
	 * @return
	 */
	public static double checkPointSideOfLine(Vector2d a, Vector2d b, Vector2d p) {
		return Math.signum((b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x));
	}
	
	/**
	 * Removes duplicate entries in {@code ArrayList list}
	 * @param list
	 * @return a new {@code ArrayList} without the duplicates
	 */
	public static ArrayList<OriVertex> removeDuplicatesFromList(ArrayList<OriVertex> list) {
		
		ArrayList<OriVertex> newList = new ArrayList<OriVertex>();
		
		for (OriVertex element : list) {
			if (newList.size() == 0) { //if first entry, just add to list
				newList.add(element);
			} else {
				boolean isDuplicate = false;
				for (OriVertex newV : newList) { //check all current entries for duplicates
					if (newV.p.equals(element.p)) {
						isDuplicate = true;
					}
				}
				if (!isDuplicate) {
					newList.add(element);
				}
			}
		}
		return newList;
	}

	
	public static int areaSign(Vector2d a, Vector2d b, Vector2d c) {
		double area2;
		
		area2 = (b.x - a.x) * (double)(c.y - a.y) -
				(c.x - a.x) * (double)(b.y - a.y);
		
		//the area should be an integer
		if (area2 < -0.5) {
			return 1;
		} else if (area2 > 0.5) {
			return -1;
		} else {
			return 0;
		}
	}
	
	public static boolean isStrictLeft( Vector2d a, Vector2d b, Vector2d c) {
		return areaSign(a, b, c) > 0;
	}
	public static boolean isStrictRight( Vector2d a, Vector2d b, Vector2d c) {
		return areaSign(a, b, c) < 0;
	}
	
	public static boolean isLeftOn( Vector2d a, Vector2d b, Vector2d c) {
		return areaSign(a, b, c) >= 0;
	}
	public static boolean isRightOn( Vector2d a, Vector2d b, Vector2d c) {
		return areaSign(a, b, c) <= 0;
	}
	
	public static boolean isCollinear( Vector2d a, Vector2d b, Vector2d c) {
		return areaSign(a, b, c) == 0;
	}
	
	/**
	 * Checks if the {@code OriPolygon p} is completely on the left side of the line {@code a-b}.
	 * @param p
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean isStrictLeft(OriPolygon p, Vector2d a, Vector2d b) {
		OriVertex curV = p.vertexList.head;
		
		do {
			if (!isStrictLeft(curV.p, a, b)) {
				return false;
			}
			curV = curV.next;
		} while (curV != p.vertexList.head);
		
		return true;
	}
	
	
	public static boolean between(Vector2d a, Vector2d b, Vector2d c) {
		
		if (!isCollinear(a, b, c)) {
			return false;
		}
		
		//if ab not vertical, check betweenness on x; else on y
		if(a.x != b.x) {
			return ((a.x <= c.x) && (c.x <= b.x)) ||
					((a.x >= c.x) && (c.x >= b.x));
		} else {
			return ((a.y <= c.y) && (c.y <= b.y)) ||
					((a.y >= c.y) && (c.y >= b.y));
		}
	}
	
	/**
	 * Returns {@code true} if and only if segments ab & cd intersect, properly or improperly
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 * @return
	 */
	public static boolean intersect(Vector2d a, Vector2d b, Vector2d c, Vector2d d) {
		if (intersectProp(a, b, c, d)) {
			return true;
		} else if (between(a, b, c) ||
				between(a, b, d) ||
				between(c, d, a) ||
				between(c, d, b)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean intersectProp(Vector2d a, Vector2d b, Vector2d c, Vector2d d) {
		//eliminate improper cases
		if (isCollinear(a,b,c) ||
			isCollinear(a,b,d) ||
			isCollinear(c,d,a) ||
			isCollinear(c,d,b)) {
			return false;
		} else {
			return xOr(isStrictLeft(a,b,c), isStrictLeft(a,b,d)) && xOr(isStrictLeft(c, d, a), isStrictLeft(c, d, b));
		}
	}
	
	/**
	 * Exclusive or: true if and only if exactly one argument is true
	 * @param x
	 * @param y
	 * @return
	 */
	public static boolean xOr(boolean x, boolean y) {
		//the arguments are negated to ensure that they are 0/1 values
		return !x ^ !y;
	}
	
	
	public static double distEdgePoint(Vector2d a, Vector2d b, Vector2d c) {
		double r, s;
		double length;
		double dProj = 0.0;
		
		length = GeometryUtil.Distance(b, a);

		r = (((a.y - c.y) * (a.y - b.y))
		   - ((a.x - c.x) * (b.x - a.x))) / (length * length);  
		s = (((a.y - c.y) * (b.x - a.x))
		   - ((a.x - c.x) * (b.y - a.y))) / (length * length);
		
		dProj = Math.abs(s * length);
		
		if ((s != 0.0) && ((0.0 <= r) && (r <= 1.0))) {
			return dProj;
		}
		if ((s == 0.0) && GeometryUtil.between(a, b, c)) {
			return 0.0;
		} else {
			double ca = GeometryUtil.Distance(a, c);
			double cb = GeometryUtil.Distance(b, c);
			return Math.min(ca,  cb);
		}
	}
	

	
	/**
	 * Rounds a {@code double value} to {@code n} decimal places
	 * @param value
	 * @param n
	 * @return the rounded {@code value}
	 */
	public static double round(double value, int n) {
		if (n < 0) {
			throw new IllegalArgumentException();
		}
		
		BigDecimal bd = new BigDecimal(Double.toString(value));
		bd = bd.setScale(n, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	/**
	 * Rounds a {@code Vector2d value} to {@code n} decimal places
	 * @param value
	 * @param n
	 * @return
	 */
	public static Vector2d round(Vector2d value, int n) {
		return new Vector2d(round(value.x, n), round(value.y, n)); 
	}

}
