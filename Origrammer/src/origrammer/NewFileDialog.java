package origrammer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.PlainDocument;
import javax.vecmath.Vector2d;

import origrammer.geometry.OriArrow;
import origrammer.geometry.OriEqualDistSymbol;
import origrammer.geometry.OriGeomSymbol;
import origrammer.geometry.OriLine;
import origrammer.geometry.OriVertex;

public class NewFileDialog  extends JDialog implements ActionListener, ComponentListener {
	
	//MODEL
	private JPanel modelPanel = new JPanel();
	private JLabel titleLabel = new JLabel("Title:");
	private JTextField titleTF = new JTextField("Untitled");
	private JLabel authorLabel = new JLabel("Author:");
	private JTextField authorTF = new JTextField();
	private JLabel commentsLabel = new JLabel("Comments:");
	private JTextArea commentsTF = new JTextArea();
	
	//PAPER SHAPE
	private JPanel paperShapePanel = new JPanel();
	private JRadioButton squareRB = new JRadioButton("Square", true);
	private JRadioButton rectangleRB = new JRadioButton("Rectangle", false);
	private JRadioButton triangleRB = new JRadioButton("Triangle (TBD)", false);
	private JRadioButton polygonRB = new JRadioButton("Polygon (TBD)", false);
	private ButtonGroup paperShapeBG = new ButtonGroup();
	private JCheckBox rotatedCB = new JCheckBox("Rotated (TBD)");
	
	//PAPER SIZE SQUARE
	private JPanel paperSizePanel = new JPanel();
	private JLabel paperSideSizeLabel = new JLabel("Side:");
	private JTextField paperSizeTF = new JTextField("30");
	
	//PAPER SIZE RECTANGLE
	private JPanel paperSizeRectPanel = new JPanel();
	private JLabel paperWidthLabel = new JLabel("Width:");
	private JTextField paperWidthTF = new JTextField();
	private JLabel paperHeightLabel = new JLabel("Height:");
	private JTextField paperHeightTF = new JTextField();
	
	//FOLDING PRESETS
	private JPanel foldingPresetsPanel = new JPanel();
	private JRadioButton noPresetRB = new JRadioButton("None", true);
	private JRadioButton divide3rdRB = new JRadioButton("Divide into 3rds", false);
	private JRadioButton divide5thRB = new JRadioButton("Divide into 5th", false);
	private JRadioButton divide7thRB = new JRadioButton("Divide into 7th", false);
	private JRadioButton birdBaseRB = new JRadioButton("Bird Base", false);
	private JRadioButton waterbombRB = new JRadioButton("Waterbomb Base", false);
	private JRadioButton kiteBaseRB = new JRadioButton("Kite Base", false);
	private JRadioButton fishBaseRB = new JRadioButton("Fish Base", false);
	private JRadioButton frogBaseRB = new JRadioButton("Frog Base", false);
	private JRadioButton preliminaryRB = new JRadioButton("Preliminary Fold", false);
	private ButtonGroup foldingPresetsBG = new ButtonGroup();
	
	//VIRTUAL FOLDING
	private JPanel virtualFoldingPanel = new JPanel();
	private JLabel virtualFoldingLabel = new JLabel("Simulate real folding of the paper");
	private JCheckBox virtualFoldingCB = new JCheckBox();

	//PAPER COLOR
	private JPanel paperColorPanel = new JPanel();
	private JLabel faceUpLabel = new JLabel("Face Up:");
	private JLabel faceDownLabel = new JLabel("Face Down:");
	private JButton faceUpColor = new JButton();
	private JButton switchColors = new JButton("Switch");
	private JButton faceDownColor = new JButton();
	
	//INSTRUCTIONS
	private JPanel instructPanel = new JPanel();
	
	//PAPER OPTIONS
	private JPanel paperOptionsPanel = new JPanel();
	
	private JPanel diagramOptionsPanel = new JPanel();

	private JButton okButton = new JButton(Origrammer.res.getString("Pref_okButton"));
	private JButton cancelButton = new JButton(Origrammer.res.getString("Pref_cancelButton"));
	
	private Color tmpColor = null;
	
	private JPanel jContentPane = null;
	private MainScreen __screen;
	
	
	public NewFileDialog(JFrame frame, MainScreen __screen) {
		super(frame);
		this.__screen = __screen;
		init();
	}
	
	private void init() {
		this.addComponentListener(this);
		this.setSize(750, 425);
		this.setContentPane(getJContentPane());
		this.setTitle("New Model");
	}
	
	private JPanel getJContentPane() {
		squareRB.addActionListener(this);
		rectangleRB.addActionListener(this);
		triangleRB.addActionListener(this);
		polygonRB.addActionListener(this);

		rotatedCB.addActionListener(this);

		switchColors.addActionListener(this);
		faceUpColor.addActionListener(this);
		faceDownColor.addActionListener(this);

		okButton.addActionListener(this);
		cancelButton.addActionListener(this);

		if (jContentPane == null) {
			addModelPanel();
			addPaperOptionsPanel();
			addDiagramOptionsPanel();
			
			SpringLayout buttonLayout = new SpringLayout();
			JPanel buttonPanel = new JPanel();
			okButton.setHorizontalAlignment(SwingConstants.RIGHT);
			cancelButton.setHorizontalAlignment(SwingConstants.RIGHT);

			buttonPanel.add(okButton);
			buttonPanel.add(cancelButton);
			buttonPanel.setLayout(buttonLayout);
			SpringUtilities.makeCompactGrid(buttonPanel, 1, 2, 6, 6, 6, 6);

			jContentPane = new JPanel();
			jContentPane.add(modelPanel);
			jContentPane.add(paperOptionsPanel);
			jContentPane.add(diagramOptionsPanel);

			diagramOptionsPanel.add(buttonPanel);

			SpringLayout layout = new SpringLayout();
			jContentPane.setLayout(layout);
			SpringUtilities.makeCompactGrid(jContentPane, 1, 3, 6, 6, 6, 6);

		}
		modeChanged();

		return jContentPane;
	}
	
	private void addModelPanel() {
		titleTF.setPreferredSize(new Dimension(300, 25));
		titleTF.setMaximumSize(new Dimension(500, 25));

		titleLabel.setAlignmentX(LEFT_ALIGNMENT);
		titleTF.setAlignmentX(LEFT_ALIGNMENT);

		authorTF.setPreferredSize(new Dimension(300, 25));
		authorTF.setMaximumSize(new Dimension(500, 25));

		authorLabel.setAlignmentX(LEFT_ALIGNMENT);
		authorTF.setAlignmentX(LEFT_ALIGNMENT);

		commentsTF.setLineWrap(true);
		commentsLabel.setAlignmentX(LEFT_ALIGNMENT);
		commentsTF.setAlignmentX(LEFT_ALIGNMENT);
		JScrollPane scroll = new JScrollPane(commentsTF);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setAlignmentX(LEFT_ALIGNMENT);
		scroll.setPreferredSize(new Dimension(300, 200));
		scroll.setMaximumSize(new Dimension(500, 200));

		modelPanel.add(titleLabel);
		modelPanel.add(titleTF);
		modelPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		modelPanel.add(authorLabel);
		modelPanel.add(authorTF);
		modelPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		modelPanel.add(commentsLabel);
		modelPanel.add(scroll);

		modelPanel.setLayout(new BoxLayout(modelPanel, BoxLayout.PAGE_AXIS));
		modelPanel.setBorder(new TitledBorder(new EtchedBorder(BevelBorder.RAISED, getBackground().darker(), getBackground().brighter()), "Model"));
	}
	
	private void addPaperShapePanel() {
		paperShapeBG.add(squareRB);
		paperShapeBG.add(rectangleRB);
		paperShapeBG.add(triangleRB);
		paperShapeBG.add(polygonRB);

		rotatedCB.setAlignmentX(0.9f);

		paperShapePanel.add(squareRB);
		paperShapePanel.add(rectangleRB);
		paperShapePanel.add(triangleRB);
		paperShapePanel.add(polygonRB);
		paperShapePanel.add(rotatedCB);

		triangleRB.setEnabled(false);
		polygonRB.setEnabled(false);
		rotatedCB.setEnabled(false);

		paperShapePanel.setLayout(new GridLayout(5, 1, 1, 0));
		paperShapePanel.setBorder(new TitledBorder(new EtchedBorder(BevelBorder.RAISED, getBackground().darker(), getBackground().brighter()), "Paper Shape"));
		paperOptionsPanel.add(paperShapePanel);
	}
	
	private void addPaperSizeSquarePanel() {
		PlainDocument docPaperSize = (PlainDocument) paperSizeTF.getDocument();
		docPaperSize.setDocumentFilter(new IntFilter());
		paperSizeTF.setHorizontalAlignment(JTextField.RIGHT);
		paperSizeTF.setPreferredSize(new Dimension(150, 25));
		paperSizeTF.setMaximumSize(new Dimension(200, 25));
		JLabel paperSizeUnit = new JLabel("cm");

		paperSizePanel.add(paperSideSizeLabel);
		paperSizePanel.add(paperSizeTF);
		paperSizePanel.add(paperSizeUnit);
		paperSizePanel.setBorder(new TitledBorder(new EtchedBorder(BevelBorder.RAISED, getBackground().darker(), getBackground().brighter()), "Paper Size"));
		paperOptionsPanel.add(paperSizePanel);
	}
	
	private void addPaperSizeRectPanel() {
		PlainDocument docPaperWidth = (PlainDocument) paperWidthTF.getDocument();
		JLabel paperWidthRectUnit = new JLabel("cm");
		docPaperWidth.setDocumentFilter(new IntFilter());
		paperWidthTF.setHorizontalAlignment(JTextField.RIGHT);
		paperWidthTF.setPreferredSize(new Dimension(100, 25));
		JPanel paperWidthRect = new JPanel();

		PlainDocument docPaperHeight = (PlainDocument) paperHeightTF.getDocument();
		JLabel paperHeightRectUnit = new JLabel("cm");
		docPaperHeight.setDocumentFilter(new IntFilter());
		paperHeightTF.setHorizontalAlignment(JTextField.RIGHT);
		paperHeightTF.setPreferredSize(new Dimension(100, 25));
		JPanel paperHeightRect = new JPanel();

		paperWidthRect.add(paperWidthLabel);
		paperWidthRect.add(paperWidthTF);
		paperWidthRect.add(paperWidthRectUnit);
		paperHeightRect.add(paperHeightLabel);
		paperHeightRect.add(paperHeightTF);
		paperHeightRect.add(paperHeightRectUnit);

		paperSizeRectPanel.add(paperWidthRect);
		paperSizeRectPanel.add(paperHeightRect);
		paperSizeRectPanel.setBorder(new TitledBorder(new EtchedBorder(BevelBorder.RAISED, getBackground().darker(), getBackground().brighter()), "Paper Size"));
		paperSizeRectPanel.setLayout(new BoxLayout(paperSizeRectPanel, BoxLayout.PAGE_AXIS));
		paperOptionsPanel.add(paperSizeRectPanel);
	}
	
	private void addFoldingPresetsPanel() {
	
		foldingPresetsBG.add(noPresetRB);
		foldingPresetsBG.add(divide3rdRB);
		foldingPresetsBG.add(divide5thRB);
		foldingPresetsBG.add(divide7thRB);
		foldingPresetsBG.add(birdBaseRB);
		foldingPresetsBG.add(waterbombRB);
		foldingPresetsBG.add(kiteBaseRB);
		foldingPresetsBG.add(fishBaseRB);
		foldingPresetsBG.add(frogBaseRB);
		foldingPresetsBG.add(preliminaryRB);
		
		foldingPresetsPanel.add(noPresetRB);
		foldingPresetsPanel.add(divide3rdRB);
		foldingPresetsPanel.add(divide5thRB);
		foldingPresetsPanel.add(divide7thRB);
		foldingPresetsPanel.add(birdBaseRB);
		foldingPresetsPanel.add(waterbombRB);
		foldingPresetsPanel.add(kiteBaseRB);
		foldingPresetsPanel.add(fishBaseRB);
		foldingPresetsPanel.add(frogBaseRB);
		foldingPresetsPanel.add(preliminaryRB);
		
		divide7thRB.setEnabled(false);
		birdBaseRB.setEnabled(false);
		waterbombRB.setEnabled(false);
		kiteBaseRB.setEnabled(false);
		fishBaseRB.setEnabled(false);
		fishBaseRB.setEnabled(false);
		frogBaseRB.setEnabled(false);
		preliminaryRB.setEnabled(false);

		foldingPresetsPanel.setLayout(new GridLayout(10, 1, 1, 0));
		foldingPresetsPanel.setBorder(new TitledBorder(new EtchedBorder(BevelBorder.RAISED, getBackground().darker(), getBackground().brighter()), "Folding Presets"));
		diagramOptionsPanel.add(foldingPresetsPanel);
	}
	
	private void addVirtualFoldingPanel() {
		virtualFoldingCB.setSelected(true);
		virtualFoldingPanel.add(virtualFoldingLabel);
		virtualFoldingPanel.add(virtualFoldingCB);
		virtualFoldingPanel.setBorder(new TitledBorder(new EtchedBorder(BevelBorder.RAISED, getBackground().darker(), getBackground().brighter()), "Virtual Folding"));
		diagramOptionsPanel.add(virtualFoldingPanel);
	}
	
	private void addPaperColorPanel() {
		faceUpColor.setPreferredSize(new Dimension(100, 50));
		faceUpColor.setOpaque(true);
		faceUpColor.setBackground(new Color(255, 255, 255, 255));
		switchColors.setPreferredSize(new Dimension(25, 25));
		faceDownColor.setPreferredSize(new Dimension(100, 50));
		faceDownColor.setBackground(new Color(133, 133, 133, 255));
		faceDownColor.setOpaque(true);

		paperColorPanel.add(faceUpLabel);
		paperColorPanel.add(Box.createRigidArea(new Dimension(25,25)));
		paperColorPanel.add(faceDownLabel);
		paperColorPanel.add(faceUpColor);
		paperColorPanel.add(switchColors);
		paperColorPanel.add(faceDownColor);

		paperColorPanel.setLayout(new GridLayout(2, 3, 0, 0));
		paperColorPanel.setBorder(new TitledBorder(new EtchedBorder(BevelBorder.RAISED, getBackground().darker(), getBackground().brighter()), "Paper Color"));
		paperOptionsPanel.add(paperColorPanel);
	}
	
	private void addInstructionsPanel() {
		JTextArea instructTF = new JTextArea();
		instructTF.setLineWrap(true);
		instructTF.setAlignmentX(LEFT_ALIGNMENT);
		JScrollPane scroll2 = new JScrollPane(instructTF);
		scroll2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll2.setAlignmentX(LEFT_ALIGNMENT);
		scroll2.setPreferredSize(new Dimension(300, 75));
		scroll2.setMaximumSize(new Dimension(500, 100));

		instructPanel.add(scroll2);
		instructPanel.setBorder(new TitledBorder(new EtchedBorder(BevelBorder.RAISED, getBackground().darker(), getBackground().brighter()), "Instructions"));
		//paperOptionsPanel.add(instructPanel);
	}
	
	
	private void addPaperOptionsPanel() {
		addPaperShapePanel();
		addPaperSizeSquarePanel();
		addPaperSizeRectPanel();
		addPaperColorPanel();

		addInstructionsPanel();
		
		paperOptionsPanel.setLayout(new BoxLayout(paperOptionsPanel, BoxLayout.PAGE_AXIS));
	}
	
	private void addDiagramOptionsPanel() {
		addFoldingPresetsPanel();
		addVirtualFoldingPanel();
		diagramOptionsPanel.setLayout(new BoxLayout(diagramOptionsPanel, BoxLayout.PAGE_AXIS));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == squareRB) {
			Globals.paperShape = Constants.PaperShape.SQUARE;
			modeChanged();
		} else if (e.getSource() == rectangleRB) {
			Globals.paperShape = Constants.PaperShape.RECTANGLE;
			modeChanged();
		} else if (e.getSource() == triangleRB) {
			Globals.paperShape = Constants.PaperShape.TRIANGLE;
			modeChanged();
		} else if (e.getSource() == polygonRB) {
			Globals.paperShape = Constants.PaperShape.POLYGON;
			modeChanged();
		} else if (e.getSource() == switchColors) {
			tmpColor = faceUpColor.getBackground();
			faceUpColor.setBackground(faceDownColor.getBackground());
			faceDownColor.setBackground(tmpColor);
		} else if (e.getSource() == faceUpColor) {
			Color newColor = JColorChooser.showDialog(null, "Choose a Color", faceUpColor.getBackground());
			if (newColor == null) {
				faceUpColor.setBackground(new Color(255, 255, 255, 255));
			} else {
				faceUpColor.setBackground(newColor);
			}
		} else if (e.getSource() == faceDownColor) {
			Color newColor = JColorChooser.showDialog(null, "Choose a Color", faceDownColor.getBackground());
			if (newColor == null) {
				faceDownColor.setBackground(new Color(255, 255, 255, 255));
			} else {
				faceDownColor.setBackground(newColor);
			}
		} else if (e.getSource() == okButton) {
			if (titleTF.getText().isEmpty()) {
				JOptionPane.showMessageDialog(this,  Origrammer.res.getString("Error_EmptyTitle"),
						"Error_EmptyTitle", 
						JOptionPane.ERROR_MESSAGE);
			} else if (Globals.paperShape == Constants.PaperShape.SQUARE) {
				if (paperSizeTF.getText().isEmpty()) {
					JOptionPane.showMessageDialog(this,  Origrammer.res.getString("Error_EmptySideLength"),
							"Error_EmptyWidth", 
							JOptionPane.ERROR_MESSAGE);
				} else {
					createNewDiagram();
					dispose();
				}
			} else if (Globals.paperShape == Constants.PaperShape.RECTANGLE) {
				if (paperWidthTF.getText().isEmpty()) {
					JOptionPane.showMessageDialog(this,  Origrammer.res.getString("Error_EmptyWidth"),
							"Error_EmptyWidth", 
							JOptionPane.ERROR_MESSAGE);
				} else if (paperHeightTF.getText().isEmpty()) {
					JOptionPane.showMessageDialog(this, Origrammer.res.getString("Error_EmptyHeight"),
							"Error_EmptyHeight", 
							JOptionPane.ERROR_MESSAGE);
				} else {
					createNewDiagram();
					dispose();
				}
			}

		} else if (e.getSource() == cancelButton) {
			dispose();		
		}
	}
	
	public void modeChanged() {
		if (Globals.paperShape == Constants.PaperShape.SQUARE) {
			paperSizePanel.setVisible(true);
			paperSizeRectPanel.setVisible(false);
		} else if (Globals.paperShape == Constants.PaperShape.RECTANGLE) {
			paperSizePanel.setVisible(false);
			paperSizeRectPanel.setVisible(true);
		}
	}
	
	private void createFromFoldingPreset() {
		
		if (noPresetRB.isSelected()) {
			System.out.println("No folding preset");
		} else if (divide3rdRB.isSelected()) {
			createDivideInto3Preset();
		} else if (divide5thRB.isSelected()) {
			createDivideInto5Preset();
		} else if (divide7thRB.isSelected()) {
			
		} else if (birdBaseRB.isSelected()) {
			
		} else if (waterbombRB.isSelected()) {
			
		} else if (kiteBaseRB.isSelected()) {
			
		} else if (fishBaseRB.isSelected()) {
			
		} else if (frogBaseRB.isSelected()) {
			
		} else if (preliminaryRB.isSelected()) {
			
		}
	
	}
	
	private void makeLineExistingCreases() {
		if (Globals.virtualFolding) {
			if (Origrammer.diagram.steps.get(Globals.currentStep).sharedLines.keySet().size() > 0) {
				for (OriLine l : Origrammer.diagram.steps.get(Globals.currentStep).sharedLines.keySet()) {
					l.setType(OriLine.CREASE);
//					l.setStartOffset(true);
//					l.setEndOffset(true);
				}	
			}	
		} else {
			for (OriLine l : Origrammer.diagram.steps.get(Globals.currentStep).lines) {
				if (l.getType() != OriLine.EDGE) {
					l.setType(OriLine.CREASE);
				}
			}
		}
			
	}
	
	private void createDivideInto3Preset() {
		OriGeomSymbol tmpGeomSymbol;
		OriEqualDistSymbol equalDistSymbol;
		OriVertex minus300Zero = new OriVertex(-300, 0);
		OriVertex threeHundretZero = new OriVertex(300, 0);
				
		//FULL 300
		OriVertex minusPlus = new OriVertex(-300, 300);
		OriVertex plusMinus = new OriVertex(300, -300);
		OriVertex minusMinus = new OriVertex(-300, -300);
		
		//300 0
		OriVertex plusZero = new OriVertex(300, 0);
		
		//300 100
		OriVertex plusHundret = new OriVertex(300, 100);
		OriVertex minusHundret = new OriVertex(-300, 100);
		OriVertex hundretPlus = new OriVertex(100, 300);
		OriVertex hundretMinus = new OriVertex(100, -300);
		
		//300 -100
		OriVertex plusMinusHundret = new OriVertex(300, -100);
		OriVertex minusMinusHundret = new OriVertex(-300, -100);
        OriVertex minusHundretPlus = new OriVertex(-100, 300);
		OriVertex minusHundretMinus = new OriVertex(-100, -300);
		
		//STEP 0
		Origrammer.diagram.steps.get(Globals.currentStep).addNewLine(new OriLine(minus300Zero, threeHundretZero, OriLine.VALLEY, true, true));
		Origrammer.diagram.steps.get(Globals.currentStep).addArrow(new OriArrow(new Vector2d(0, 300), new Vector2d(0, -300), OriArrow.TYPE_VALLEY, false, true));
		Origrammer.diagram.steps.get(Globals.currentStep).setStepDescription("Fold und unfold in half");
		Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();
		
		//STEP 1
		Origrammer.mainFrame.uiBottomPanel.stepForth();
		makeLineExistingCreases();
		Origrammer.diagram.steps.get(Globals.currentStep).arrows.clear();
		Origrammer.diagram.steps.get(Globals.currentStep).addNormalLine(new OriLine(minusPlus, plusMinus, OriLine.VALLEY, true, true));
		Origrammer.diagram.steps.get(Globals.currentStep).addArrow(new OriArrow(new Vector2d(-300, -300), new Vector2d(300, 300), OriArrow.TYPE_VALLEY, false, true));
		Origrammer.diagram.steps.get(Globals.currentStep).setStepDescription("Fold und unfold diagonally");
		Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();
		
		//STEP 2
		Origrammer.mainFrame.uiBottomPanel.stepForth();
		makeLineExistingCreases();
		Origrammer.diagram.steps.get(Globals.currentStep).arrows.clear();
		Origrammer.diagram.steps.get(Globals.currentStep).addNewLine(new OriLine(minusMinus, plusZero, OriLine.VALLEY, true, true));
		Origrammer.diagram.steps.get(Globals.currentStep).addArrow(new OriArrow(new Vector2d(150, -300), new Vector2d(-50, 50), OriArrow.TYPE_VALLEY, false, true));
		
		Origrammer.diagram.steps.get(Globals.currentStep).lines.get(3).setStartOffset(false);
		Origrammer.diagram.steps.get(Globals.currentStep).lines.get(8).setEndOffset(true);
		Origrammer.diagram.steps.get(Globals.currentStep).lines.get(9).setEndOffset(false);

		Origrammer.diagram.steps.get(Globals.currentStep).setStepDescription("Fold und unfold from the top left corner to the right edge of the horizontal crease");
		Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();
		
		//STEP 3
		Origrammer.mainFrame.uiBottomPanel.stepForth();
		makeLineExistingCreases();
		Origrammer.diagram.steps.get(Globals.currentStep).arrows.clear();
		Origrammer.diagram.steps.get(Globals.currentStep).addNewLine(new OriLine(minusMinusHundret, plusMinusHundret, OriLine.VALLEY, true, true));
		Origrammer.diagram.steps.get(Globals.currentStep).addNewLine(new OriLine(hundretMinus, hundretPlus, OriLine.VALLEY, true, true));

		Origrammer.diagram.steps.get(Globals.currentStep).addArrow(new OriArrow(new Vector2d(-200, -300), new Vector2d(-200, 100), OriArrow.TYPE_VALLEY, false, true));
		Origrammer.diagram.steps.get(Globals.currentStep).addArrow(new OriArrow(new Vector2d(300, 200), new Vector2d(-100, 200), OriArrow.TYPE_VALLEY, true, true));
		Origrammer.diagram.steps.get(Globals.currentStep).addGeomSymbol(new OriGeomSymbol(new Vector2d(77.5, -122.5), 45, OriGeomSymbol.TYPE_XRAY_CIRCLE));

		Origrammer.diagram.steps.get(Globals.currentStep).setStepDescription("Fold and unfold vertically and horizontally through the marked point.");
		Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();
		
		//STEP 4
		Origrammer.mainFrame.uiBottomPanel.stepForth();
		makeLineExistingCreases();
		Origrammer.diagram.steps.get(Globals.currentStep).lines.remove(17);
		Origrammer.diagram.steps.get(Globals.currentStep).lines.remove(16);
		Origrammer.diagram.steps.get(Globals.currentStep).lines.remove(15);
		Origrammer.diagram.steps.get(Globals.currentStep).lines.remove(14);
		Origrammer.diagram.steps.get(Globals.currentStep).lines.remove(13);
		Origrammer.diagram.steps.get(Globals.currentStep).lines.remove(12);
		Origrammer.diagram.steps.get(Globals.currentStep).lines.remove(2);
		Origrammer.diagram.steps.get(Globals.currentStep).lines.remove(1);

		Origrammer.diagram.steps.get(Globals.currentStep).arrows.clear();
		Origrammer.diagram.steps.get(Globals.currentStep).geomSymbols.clear();
		Origrammer.diagram.steps.get(Globals.currentStep).addNewLine(new OriLine(minusHundretMinus, minusHundretPlus, OriLine.VALLEY, true, true));
		Origrammer.diagram.steps.get(Globals.currentStep).addNewLine(new OriLine(minusHundret, plusHundret, OriLine.VALLEY, true, true));

		Origrammer.diagram.steps.get(Globals.currentStep).lines.get(7).setEndOffset(false);
		Origrammer.diagram.steps.get(Globals.currentStep).lines.get(8).setStartOffset(false);
		Origrammer.diagram.steps.get(Globals.currentStep).lines.get(8).setEndOffset(false);
		Origrammer.diagram.steps.get(Globals.currentStep).lines.get(20).setStartOffset(false);


		Origrammer.diagram.steps.get(Globals.currentStep).addArrow(new OriArrow(new Vector2d(-300, -200), new Vector2d(100, -200), OriArrow.TYPE_VALLEY, true, true));
		Origrammer.diagram.steps.get(Globals.currentStep).addArrow(new OriArrow(new Vector2d(200, 300), new Vector2d(200, -100), OriArrow.TYPE_VALLEY, false, true));

		Origrammer.diagram.steps.get(Globals.currentStep).setStepDescription("Fold and unfold the edges to the previously made creases. Unused crease lines will no longer be shown.");
		Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();
		
		//STEP 5
		Origrammer.mainFrame.uiBottomPanel.stepForth();
		makeLineExistingCreases();
		Origrammer.diagram.steps.get(Globals.currentStep).arrows.clear();
		equalDistSymbol = new OriEqualDistSymbol(new Vector2d(-300, -300), new Vector2d(300, -300), 40, 3);
		Origrammer.diagram.steps.get(Globals.currentStep).addEqualDistSymbol(equalDistSymbol);
		equalDistSymbol = new OriEqualDistSymbol(new Vector2d(-300, -300), new Vector2d(-300, 300), -40, 3);
		Origrammer.diagram.steps.get(Globals.currentStep).addEqualDistSymbol(equalDistSymbol);
		
		Origrammer.diagram.steps.get(Globals.currentStep).lines.get(15).setEndOffset(false);
		Origrammer.diagram.steps.get(Globals.currentStep).lines.get(22).setStartOffset(false);
		Origrammer.diagram.steps.get(Globals.currentStep).lines.get(24).setEndOffset(false);
		Origrammer.diagram.steps.get(Globals.currentStep).lines.get(25).setStartOffset(false);
		Origrammer.diagram.steps.get(Globals.currentStep).lines.get(25).setEndOffset(false);
		Origrammer.diagram.steps.get(Globals.currentStep).lines.get(26).setStartOffset(false);
		Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();
	}
	
	/**
	 * Creates the steps with all lines/arrows/symbols required to divide the paper into a 5-by-5 grid
	 */
	private void createDivideInto5Preset() {
		ArrayList<OriLine> lineList = new ArrayList<OriLine>();
		ArrayList<OriArrow> arrowList = new ArrayList<OriArrow>();
		OriGeomSymbol tmpGeomSymbol;
		OriEqualDistSymbol equalDistSymbol;
		
		//FULL 300
		OriVertex minusPlus = new OriVertex(-300, 300);
		OriVertex plusMinus = new OriVertex(300, -300);
		
		//-51.4718 -300
		OriVertex minus51Minus = new OriVertex(-51.471862576141724, -300);
		
		//180 300
		OriVertex plus180Plus = new OriVertex(180, 300);
		OriVertex plus180Minus = new OriVertex(180, -300);
		OriVertex minus180Plus = new OriVertex(-180, 300);
		OriVertex minus180Minus = new OriVertex(-180, -300);
		
		//60 300
		OriVertex plus60Plus = new OriVertex(60, 300);
		OriVertex plus60Minus = new OriVertex(60, -300);
		OriVertex minus60Plus = new OriVertex(-60, 300);
		OriVertex minus60Minus = new OriVertex(-60, -300);
		
		//300 180
		OriVertex plusPlus180 = new OriVertex(300, 180);
		OriVertex plusMinus180 = new OriVertex(300, -180);
		OriVertex minusPlus180 = new OriVertex(-300, 180);
		OriVertex minusMinus180 = new OriVertex(-300, -180);
		
		//300 60
		OriVertex plusPlus60 = new OriVertex(300, 60);
		OriVertex plusMinus60 = new OriVertex(300, -60);
		OriVertex minusPlus60 = new OriVertex(-300, 60);
		OriVertex minusMinus60 = new OriVertex(-300, -60);

		
		//STEP 0
		lineList.add(new OriLine(minusPlus, plusMinus, OriLine.VALLEY, true, true));
		arrowList.add(new OriArrow(new Vector2d(-300, -300), new Vector2d(300, 300), OriArrow.TYPE_VALLEY, false, true));
		Origrammer.diagram.steps.get(Globals.currentStep).addLines(lineList);
		Origrammer.diagram.steps.get(Globals.currentStep).addArrows(arrowList);
		Origrammer.diagram.steps.get(Globals.currentStep).setStepDescription("Fold und unfold diagonally");
		Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();

		//STEP 1
		lineList.clear();
		arrowList.clear();
		Origrammer.mainFrame.uiBottomPanel.stepForth();
		makeLineExistingCreases();
		lineList.add(new OriLine(minusPlus, minus51Minus, OriLine.VALLEY, true, true));
		Origrammer.diagram.steps.get(Globals.currentStep).arrows.clear();
		arrowList.add(new OriArrow(new Vector2d(-300, -150), new Vector2d(0, 0), OriArrow.TYPE_VALLEY, false, true));
		Origrammer.diagram.steps.get(Globals.currentStep).addLines(lineList);
		Origrammer.diagram.steps.get(Globals.currentStep).addArrows(arrowList);
		Origrammer.diagram.steps.get(Globals.currentStep).setStepDescription("Fold and unfold the angle bisector");
		Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();

		//STEP 2
		lineList.clear();
		arrowList.clear();
		Origrammer.mainFrame.uiBottomPanel.stepForth();
		makeLineExistingCreases();
		lineList.add(new OriLine(minusPlus, minus180Minus, OriLine.VALLEY, true, true));
		Origrammer.diagram.steps.get(Globals.currentStep).arrows.clear();
		arrowList.add(new OriArrow(new Vector2d(-300, -187.5), new Vector2d(-112.5, -150), OriArrow.TYPE_VALLEY, false, true));
		Origrammer.diagram.steps.get(Globals.currentStep).addLines(lineList);
		Origrammer.diagram.steps.get(Globals.currentStep).addArrows(arrowList);
		Origrammer.diagram.steps.get(Globals.currentStep).setStepDescription("Fold and unfold the next angle bisector");
		Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();

		//STEP 3
		lineList.clear();
		arrowList.clear();
		Origrammer.mainFrame.uiBottomPanel.stepForth();
		makeLineExistingCreases();
		lineList.add(new OriLine(minus180Minus, minus180Plus, OriLine.VALLEY, true, true));
		Origrammer.diagram.steps.get(Globals.currentStep).arrows.clear();
		arrowList.add(new OriArrow(new Vector2d(-300, -60), new Vector2d(-60, -60), OriArrow.TYPE_VALLEY, false, true));
		Origrammer.diagram.steps.get(Globals.currentStep).addLines(lineList);
		Origrammer.diagram.steps.get(Globals.currentStep).addArrows(arrowList);
		tmpGeomSymbol = new OriGeomSymbol(new Vector2d(-202.5, -322.5), 45, OriGeomSymbol.TYPE_XRAY_CIRCLE);
		Origrammer.diagram.steps.get(Globals.currentStep).addGeomSymbol(tmpGeomSymbol);
		Origrammer.diagram.steps.get(Globals.currentStep).setStepDescription("Use the crease mark of the last step to make a vertical fold at 1/5th");
		Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();
		
		//STEP 4
		lineList.clear();
		arrowList.clear();
		Origrammer.mainFrame.uiBottomPanel.stepForth();
		makeLineExistingCreases();
		lineList.add(new OriLine(plus60Minus, plus60Plus, OriLine.VALLEY, true, true));
		Origrammer.diagram.steps.get(Globals.currentStep).arrows.clear();
		arrowList.add(new OriArrow(new Vector2d(300, 0), new Vector2d(-180, 0), OriArrow.TYPE_VALLEY, true, true));
		Origrammer.diagram.steps.get(Globals.currentStep).geomSymbols.clear();
		Origrammer.diagram.steps.get(Globals.currentStep).addLines(lineList);
		Origrammer.diagram.steps.get(Globals.currentStep).addArrows(arrowList);
		Origrammer.diagram.steps.get(Globals.currentStep).setStepDescription("Fold and unfold the right edge onto the crease made in the last step. Unused crease lines will no longer be shown.");
		Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();

		
		//STEP 5
		lineList.clear();
		arrowList.clear();
		Origrammer.mainFrame.uiBottomPanel.stepForth();
		makeLineExistingCreases();
		lineList.add(new OriLine(minus60Minus, minus60Plus, OriLine.VALLEY, true, true));
		lineList.add(new OriLine(plus180Minus, plus180Plus, OriLine.VALLEY, true, true));
		Origrammer.diagram.steps.get(Globals.currentStep).arrows.clear();
		arrowList.add(new OriArrow(new Vector2d(300, 0), new Vector2d(60, 0), OriArrow.TYPE_VALLEY, true, true));
		arrowList.add(new OriArrow(new Vector2d(-300, -60), new Vector2d(180, -60), OriArrow.TYPE_VALLEY, false, true));
		Origrammer.diagram.steps.get(Globals.currentStep).addLines(lineList);
		Origrammer.diagram.steps.get(Globals.currentStep).addArrows(arrowList);
		equalDistSymbol = new OriEqualDistSymbol(new Vector2d(-300, -300), new Vector2d(300, -300), -40, 5);
		Origrammer.diagram.steps.get(Globals.currentStep).addEqualDistSymbol(equalDistSymbol);
		Origrammer.diagram.steps.get(Globals.currentStep).setStepDescription("Fold and unfold the last 2 lines.");
		Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();
		
		//STEP 6
		lineList.clear();
		arrowList.clear();
		Origrammer.mainFrame.uiBottomPanel.stepForth();
		makeLineExistingCreases();
		
		lineList.add(new OriLine(minusMinus180, plusMinus180, OriLine.VALLEY, true, true));
		lineList.add(new OriLine(minusMinus60, plusMinus60, OriLine.VALLEY, true, true));
		lineList.add(new OriLine(minusPlus60, plusPlus60, OriLine.VALLEY, true, true));
		lineList.add(new OriLine(minusPlus180, plusPlus180, OriLine.VALLEY, true, true));
		Origrammer.diagram.steps.get(Globals.currentStep).arrows.clear();
		arrowList.add(new OriArrow(new Vector2d(60, -300), new Vector2d(60, -60), OriArrow.TYPE_VALLEY, true, true));
		arrowList.add(new OriArrow(new Vector2d(-180, -300), new Vector2d(-180, 180), OriArrow.TYPE_VALLEY, false, true));
		arrowList.add(new OriArrow(new Vector2d(180, 300), new Vector2d(180, -180), OriArrow.TYPE_VALLEY, false, true));
		arrowList.add(new OriArrow(new Vector2d(-60, 300), new Vector2d(-60, 60), OriArrow.TYPE_VALLEY, false, true));
		Origrammer.diagram.steps.get(Globals.currentStep).addLines(lineList);
		Origrammer.diagram.steps.get(Globals.currentStep).addArrows(arrowList);
		equalDistSymbol = new OriEqualDistSymbol(new Vector2d(-300, -300), new Vector2d(-300, 300), 40, 5);
		Origrammer.diagram.steps.get(Globals.currentStep).addEqualDistSymbol(equalDistSymbol);
		Origrammer.diagram.steps.get(Globals.currentStep).setStepDescription("Fold and unfold the last 2 lines.");
		Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();
		
		
		//STEP 7
		lineList.clear();
		arrowList.clear();
		Origrammer.mainFrame.uiBottomPanel.stepForth();
		makeLineExistingCreases();
		Origrammer.diagram.steps.get(Globals.currentStep).arrows.clear();
		Origrammer.diagram.steps.get(Globals.currentStep).addLines(lineList);
		Origrammer.diagram.steps.get(Globals.currentStep).setStepDescription("Done.");
		Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();
	}
	
	private void createNewDiagram() {

		Globals.newStepOptions = Constants.NewStepOptions.PASTE_DEFAULT_PAPER;
		Globals.virtualFolding = virtualFoldingCB.isSelected();

		Diagram tmpDiagram = new Diagram(Constants.DEFAULT_PAPER_SIZE, 
										faceUpColor.getBackground(), 
										faceDownColor.getBackground());

		if (Globals.paperShape == Constants.PaperShape.SQUARE) {
			tmpDiagram.setPaperWidth(Integer.parseInt(paperSizeTF.getText()));
			tmpDiagram.setPaperHeight(Integer.parseInt(paperSizeTF.getText()));
		} else if (Globals.paperShape == Constants.PaperShape.RECTANGLE) {
			tmpDiagram.setPaperWidth(Integer.parseInt(paperWidthTF.getText()));
			tmpDiagram.setPaperHeight(Integer.parseInt(paperHeightTF.getText()));
		}
		tmpDiagram.setTitle(titleTF.getText());
		tmpDiagram.setAuthor(authorTF.getText());
		tmpDiagram.setComments(commentsTF.getText());
		
		tmpDiagram.setFaceUpColor(faceUpColor.getBackground());
		tmpDiagram.setFaceDownColor(faceDownColor.getBackground());
		Origrammer.diagram = tmpDiagram;
		Globals.currentStep = 0;

		Origrammer.mainFrame.uiStepOverviewPanel.removeAllStepPreviews();
		Step step = new Step();
		Origrammer.diagram.steps.add(step);
		Globals.newStepOptions = Constants.NewStepOptions.COPY_LAST_STEP;
		createFromFoldingPreset();
		
		
		__screen.modeChanged();
		
		

		Origrammer.mainFrame.uiSidePanel.dispGridCheckBox.setSelected(true);
		Origrammer.mainFrame.mainScreen.setDispGrid(true);
		Origrammer.mainFrame.updateUI();
	}

	@Override
	public void componentHidden(ComponentEvent e) {		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}
	

}
