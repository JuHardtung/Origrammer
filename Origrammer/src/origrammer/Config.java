package origrammer;

import java.awt.BasicStroke;
import java.awt.Color;


public class Config {

	final public static Color LINE_COLOR_EDGE = Color.BLACK;
	final public static Color LINE_COLOR_VALLEY = Color.BLUE;
	final public static Color LINE_COLOR_MOUNTAIN = Color.RED;
	final public static Color LINE_COLOR_XRAY = Color.GRAY;
	final public static Color LINE_COLOR_CREASE = Color.LIGHT_GRAY;
	final public static Color LINE_COLOR_HIDDEN = new Color(0.0f, 0.0f, 0.0f, 0.0f);
	final public static Color LINE_COLOR_SELECTED = Color.GREEN;
	final public static Color LINE_COLOR_DIAGONAL = Color.LIGHT_GRAY;
	
	final static float dashed[] = {14.0f, 6.0f};
	final static float dash_dot[] = {14.0f, 6.0f, 4.0f, 6.0f};
	final static float dash_dot_dot[] = {14.0f, 6.0f, 4.0f, 6.0f, 4.0f, 6.0f};
	final static float dotted[] = {2.0f, 2.0f};
	
	final public static BasicStroke STROKE_EDGE = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
	final public static BasicStroke STROKE_VALLEY = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, dashed, 0.0f);
	final public static BasicStroke STROKE_MOUNTAIN_DASH_DOT = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, dash_dot, 0.0f);
	final public static BasicStroke STROKE_MOUNTAIN_DASH_DOT_DOT = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, dash_dot_dot, 0.0f);
	final public static BasicStroke STROKE_XRAY = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, dotted, 0.0f);
	final public static BasicStroke STROKE_CREASE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
	final public static BasicStroke STROKE_DIAGONAL = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, dotted, 0.0f);
	final public static BasicStroke STROKE_HIDDEN = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
	
	final public static BasicStroke STROKE_SELECTED = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
	final public static BasicStroke STROKE_GRID = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
	final public static BasicStroke STROKE_ARROWS = new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);


	final public static int DEFAULT_GRID_DIV_NUM = 4;
	final public static int MRUFILE_NUM = 10;

}
