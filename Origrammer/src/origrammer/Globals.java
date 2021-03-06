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
	
	public static Constants.MountainFoldStyle  mountainFoldStyle = Constants.MountainFoldStyle.DASH_DOT_DOT;
	public static Constants.OutsideReverseStyle outsideReverseStyle = Constants.OutsideReverseStyle.AOM_AOA;
	public static Constants.RabbitEarStyle rabbitEarStyle = Constants.RabbitEarStyle.SAOM_SAOM_BAOM;
	
	public static Constants.NewStepOptions newStepOptions = Constants.NewStepOptions.COPY_LAST_STEP;
	public static Constants.PaperShape paperShape = Constants.PaperShape.SQUARE;
	public static Constants.FaceInputDirection faceInputDirection = Constants.FaceInputDirection.FACE_DOWN;
	
	public static boolean virtualFolding = true;
	public static int gridDivNum = Config.DEFAULT_GRID_DIV_NUM;
	public static boolean dispVertex = true;
	public static boolean dispFilledFaces = true;
	public static boolean dispPolygons = true;
	public static boolean dispTriangulation = true;
	public static boolean bDispCrossLine = false;
	public static boolean dispColoredLines = false;
	public static boolean automatedArrowPlacement = false;
	public static boolean automatedLinePlacement = true;
	public static boolean automatedFolding = true;
	public static int inputLineType = OriLine.VALLEY;
	public static int inputArrowType = OriArrow.TYPE_VALLEY;
	public static boolean renderStepPreview = false;
	
	public static int currentStep = 0;
	public static double SCALE = 1.0;
	public static int lowerRenderHeight = 0;
	public static int upperRenderHeight = 100;
	
	public static Color DEFAULT_PAPER_COLOR = new Color(133, 133, 133);
}
