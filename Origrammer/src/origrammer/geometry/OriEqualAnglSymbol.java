package origrammer.geometry;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import javax.vecmath.Vector2d;

public class OriEqualAnglSymbol {
	
	private Vector2d v = new Vector2d();
	private Vector2d p1 = new Vector2d();
	private Vector2d p2 = new Vector2d();
	private int dividerCount;
	private double length = 0;
	private boolean isSelected;
	
	
	public OriEqualAnglSymbol() {
	}
	
	public OriEqualAnglSymbol(OriEqualAnglSymbol eas) {
		this.v = eas.v;
		this.p1 = eas.p1;
		this.p2 = eas.p2;
		this.dividerCount = eas.dividerCount;
		this.length = eas.length;
		this.isSelected = eas.isSelected;
	}
	
	public OriEqualAnglSymbol(Vector2d v, Vector2d p1, Vector2d p2) {
		this.v = v;
		this.p1 = p1;
		this.p2 = p2;
	}
	
	public OriEqualAnglSymbol(Vector2d v, Vector2d p1, Vector2d p2, int dividerCount) {
		this.v = v;
		this.p1 = p1;
		this.p2 = p2;
		this.dividerCount = dividerCount;
	}
	
	/**
	 * 
	 * @return The shapes required for rendering the OriEqualAnglSymbol
	 */
	public ArrayList<Shape> getShapesForDrawing() {

		ArrayList<Shape> shapes = new ArrayList<>();
		Vector2d uv1 = GeometryUtil.getUnitVector(v, p1);
		Vector2d uv2 = GeometryUtil.getUnitVector(v, p2);
		uv1.y = -uv1.y;
		uv2.y = -uv2.y;
		double dist1 = GeometryUtil.Distance(v, p1);
		double dist2 = GeometryUtil.Distance(v, p2);

		//set length to the smallest dist between Dist(v,p1) and Dist(v,p2)
		if (length == 0) {
			if (dist1 < dist2) {
				length = dist1;
			} else if (dist2 < dist1) {
				length = dist2;
			} else {
				length = dist1;
			}
		}
		
		double angle1 =  Math.toDegrees(GeometryUtil.measureAngleToXAxis(uv1));
		double angle2 = Math.toDegrees(GeometryUtil.measureAngleToXAxis(uv2));
		//making sure that only positive angles are being used
		if (angle1 < 0 ) {
			angle1 += 360;
		}
		if (angle2 < 0 ) {
			angle2 += 360;
		}
		
		double angleExtend = 0;
		double angleStart = 0;
		//check which is the smaller angle and use it as the startAngle
		if (angle1 < angle2) {
			//check if smallest angle is 0 and if other angle is bigger than 180
			//if so --> use bigger angle as start
			if (angle1 == -0.0 && angle2 > 180) {
				angleStart = angle2;
				angleExtend = 360 - angle2;
			} else {
				angleStart = angle1;
				angleExtend = Math.abs(Math.abs(angle2) - Math.abs(angle1));
			}
			
		} else {
			//check if smallest angle is 0 ang if other angle is bigger than 180
			//if so --> use bigger angle as start
			if (angle2 == -0.0 && angle1 > 180) {
				angleStart = angle1;
				angleExtend = 360 - angle1;
			} else {
				angleStart = angle2;
				angleExtend = Math.abs(Math.abs(angle1) - Math.abs(angle2));
			}
		}
		
		double eachDivider = angleExtend / (dividerCount);
		double offset = (eachDivider/100*25)/2;
		eachDivider -= offset*2;
		Vector2d arcTopLeft = new Vector2d(v.x - length, v.y - length);
		Vector2d dividerUV = null;
		double dividerLength = length + 10; //add +10 to have longer dividerLines compared to the arcs

		
		/**
		 * | + offset/2 | + eachDivider | + offset/2 | + offset/2 | + eachDivider | + offset/2 | + offset/2 | + eachDivider | + offset/2 |
		 * |                                         |  								       |									     |
		 * | {    1    } {      2      } {    3.1         3.2    } {      2      } {    3.1         3.2    } {		2	   }		     |
		 */
		for (int i = 0; i < dividerCount; i++) {
			if (i == 0) {
				angleStart += offset;   //1
			} else {
				angleStart += offset;   //3.1

				double angle = Math.toRadians(angleStart);
				double angleX = 1 * Math.cos(angle) - 0 * Math.sin(angle);
				double angleY = 1 * Math.sin(angle) + 0 * Math.cos(angle);
				dividerUV = new Vector2d(angleX, angleY); //get uv for the current dividerLine
				
				angleStart += offset;   //3.2
				
				Line2D dividerLine = new Line2D.Double(v.x, v.y, v.x + dividerUV.x * dividerLength, v.y - dividerUV.y * dividerLength);
				shapes.add(dividerLine);
			}
			
			Arc2D.Double smallArc = new Arc2D.Double(arcTopLeft.x, arcTopLeft.y, length*2, length*2, angleStart, eachDivider, Arc2D.OPEN);
			
			angleStart += eachDivider;   //2
			shapes.add(smallArc);
		}
		
		
		Line2D lineV1 = new Line2D.Double(v.x, v.y, v.x + uv1.x * dividerLength, v.y - uv1.y * dividerLength);
		Line2D lineV2 = new Line2D.Double(v.x, v.y, v.x + uv2.x * dividerLength, v.y  - uv2.y * dividerLength);
	
		shapes.add(lineV1);
		shapes.add(lineV2);
		return shapes;
	}
	
	public void moveBy(double xTrans, double yTrans) {
		v.x += xTrans;
		v.y += yTrans;
		p1.x += xTrans;
		p1.y += yTrans;
		p2.x += xTrans;
		p2.y += yTrans;
	}
	
	/** Sets the positions of Vector2d a and Vector2d b,<br> 
	 * so that the {@code Distance(v, a)} and {@code Distance(v, b) == lineLength}
	 * @param lineLength
	 */
	public void setLineLength(double lineLength) {
		Vector2d uv1 = GeometryUtil.getUnitVector(v, p1);
		Vector2d uv2 = GeometryUtil.getUnitVector(v, p2);
		
		p1.x = v.x + uv1.x * lineLength;
		p1.y = v.y + uv1.y * lineLength;
		
		p2.x = v.x + uv2.x * lineLength;
		p2.y = v.y + uv2.y * lineLength;
	}

	public Vector2d getV() {
		return v;
	}

	public void setV(Vector2d v) {
		this.v = v;
	}

	public Vector2d getP1() {
		return p1;
	}

	public void setP1(Vector2d p1) {
		this.p1 = p1;
	}

	public Vector2d getP2() {
		return p2;
	}

	public void setP2(Vector2d p2) {
		this.p2 = p2;
	}

	public int getDividerCount() {
		return dividerCount;
	}

	public void setDividerCount(int dividerCount) {
		this.dividerCount = dividerCount;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	@Override
	public String toString() {
		return "OriEqualAnglSymbol [v=" + v + ", p1=" + p1 + ", p2=" + p2 + ", dividerCount=" + dividerCount
				+ ", isSelected=" + isSelected + "]";
	}
	
}
