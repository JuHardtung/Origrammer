package origrammer;


import java.awt.Color;

import origrammer.geometry.OriArrow;
import origrammer.geometry.OriLine;

public class Globals {
	
	public static Constants.ToolbarMode toolbarMode = Constants.ToolbarMode.INPUT_LINE;
	public static Constants.LineInputMode lineEditMode = Constants.LineInputMode.INPUT_LINE;
	public static Constants.VertexInputMode vertexInputMode = Constants.VertexInputMode.ABSOLUTE;
	public static Constants.InputSymbolMode inputSymbolMode = Constants.InputSymbolMode.LEADER;
	public static Constants.MeasureMode measureMode = Constants.MeasureMode.MEASURE_LENGTH;
	
	public static Constants.MountainFoldStyle  mountainFoldStyle = Constants.MountainFoldStyle.DASH_DOT;
	public static Constants.OutsideReverseStyle outsideReverseStyle = Constants.OutsideReverseStyle.AOM_AOA;
	public static Constants.RabbitEarStyle rabbitEarStyle = Constants.RabbitEarStyle.SAOM_SAOM_BAOM;
	
	public static Constants.NewStepOptions newStepOptions = Constants.NewStepOptions.COPY_LAST_STEP;
	public static Constants.PaperShape paperShape = Constants.PaperShape.SQUARE;
	public static Constants.FaceInputDirection faceInputDirection = Constants.FaceInputDirection.FACE_DOWN;
	
	public static int gridDivNum = Config.DEFAULT_GRID_DIV_NUM;
	public static boolean dispVertex = true;
	public static boolean dispFilledFaces = true;
	public static boolean bDispCrossLine = false;
	public static boolean dispColoredLines = false;
	public static boolean automatedArrowPlacement = false;
	public static boolean automatedLinePlacement = false;
	public static int inputLineType = OriLine.TYPE_VALLEY;
	public static int inputArrowType = OriArrow.TYPE_VALLEY;
	
	public static int currentStep = 0;
	public static double SCALE = 1.0;
	
	public static Color DEFAULT_PAPER_COLOR = new Color(133, 133, 133);


}
