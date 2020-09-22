package origrammer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.PlainDocument;
import javax.vecmath.Vector2d;

import origrammer.geometry.OriArrow;
import origrammer.geometry.OriEqualAnglSymbol;
import origrammer.geometry.OriEqualDistSymbol;
import origrammer.geometry.OriFace;
import origrammer.geometry.OriGeomSymbol;
import origrammer.geometry.OriLine;
import origrammer.geometry.OriPleatCrimpSymbol;
import origrammer.geometry.OriPolygon;

public class UITopPanel extends JPanel implements ActionListener, PropertyChangeListener, KeyListener {

	private String[] lineInputOptions = {"Valley Fold", "Mountain Fold", "X-Ray Fold", "Edge Line",  "Existing Crease"};
	private Object[] arrowInputOptions = {"Valley Fold", "Mountain Fold", "Turn over", 
			new JSeparator(JSeparator.HORIZONTAL),
			"Push here", "Pull out", "Inflate here"};
	private Object[] symbolInputOptions = {"Leader", "Repetition Box", "Next View Here", "Rotations", 
			"Hold Here", "Hold Here and Pull", new JSeparator(JSeparator.HORIZONTAL),  
			"X-Ray Circle", "Fold over and over", "Equal Distances", "Equal Angles", 
			new JSeparator(JSeparator.HORIZONTAL),  "Crimping & Pleating", "Sinks"};


	//INPUT LINES/ ARROWS/ SYMBOLS
	private JPanel inputLinesPanel = new JPanel();
	private JPanel inputArrowPanel = new JPanel();
	private JPanel inputSymbolsPanel = new JPanel();
	private JComboBox<String> menuLineCB = new JComboBox<>(lineInputOptions);
	private JComboBox<Object> menuArrowCB = new JComboBox<>(arrowInputOptions);
	private JComboBox<Object> symbolInputCB = new JComboBox<>(symbolInputOptions);
	
	//INPUT LINES (BY LENGTH & ANGLE)
	private JPanel inputLineLengthAnglePanel = new JPanel();
	private JLabel lengthLabel = new JLabel("Length:");
	public JTextField inputLineLengthTF = new JTextField();
	private JLabel angleLabel = new JLabel("Angle:");
	public JTextField inputLineAngleTF = new JTextField();

	//INPUT VERTICES FRACTION_OF_LINE
	private JPanel inputVertexFractionPanel = new JPanel();
	private JSlider inputVertexFractionSlider = new JSlider(0, 100);
	public JTextField inputVertexFractionTF = new JTextField();

	//INPUT SYMBOL LEADER
	private JPanel inputSymbolLeaderPanel = new JPanel();
	public JTextField inputLeaderTextTF = new JTextField();

	//CHANGE SYMBOL LEADER
	private JPanel changeSymbolLeaderPanel = new JPanel();
	private JTextField changeLeaderTextTF = new JTextField();
	private JButton changeSymbolLeaderButton = new JButton("Set");

	//INPUT SYMBOL REPETITION BOX
	//private JPanel inputSymbolRepetitionPanel = new JPanel();
	//public JTextField inputRepetitionText = new JTextField();

	//FACE UP/ FACE DOWN COLOR
	private JPanel faceDirectionPanel = new JPanel();
	private JRadioButton faceUpInput = new JRadioButton("Face Up", false);
	private JRadioButton faceDownInput = new JRadioButton("Face Down", true);

	//CHANGE LINE/ARROW TYPE
	private JPanel changeLinePanel = new JPanel();
	private JPanel changeArrowPanel = new JPanel();
	private JComboBox<String> changeLineTypeCB = new JComboBox<>(lineInputOptions);
	private JComboBox<Object> changeArrowTypeCB = new JComboBox<>(arrowInputOptions);
	private JButton changeLineButton = new JButton("Set");
	private JButton changeArrowButton = new JButton("Set");

	//Change Existing Crease ends
	private JPanel changeCreaseEndsPanel = new JPanel();
	public JCheckBox startCreaseCB = new JCheckBox("Start");
	public JCheckBox endCreaseCB = new JCheckBox("End");

	//EQUAL DISTANCE SETTINGS
	private JPanel equalDistPanel = new JPanel();
	public JSlider sliderEqualDist = new JSlider(-50, 50);
	public JTextField equalDistDividerTF = new JTextField();
	public JButton equalDistButton = new JButton("Set");

	//EQUAL ANGLE SETTINGS
	private JPanel equalAnglPanel = new JPanel();
	private JSlider equalAnglSlider = new JSlider(50, 400);
	public JTextField equalAnglDividerTF = new JTextField();
	private JButton equalAnglButton = new JButton("Set");

	//PLEATNG/CRIMPING SETTINGS
	public JRadioButton pleatRB = new JRadioButton("Pleat", true);
	public JRadioButton crimpRB = new JRadioButton("Crimp");

	private JPanel pleatPanel = new JPanel();
	public JCheckBox pleatCB = new JCheckBox("reverseDir");
	public JTextField pleatTF = new JTextField();
	private JButton pleatButton = new JButton("Set");

	//ARROWS SETTINGS
	private JPanel arrowPanel = new JPanel();
	public JCheckBox arrowIsMirrored = new JCheckBox("Is Mirrored");
	public JCheckBox arrowIsUnfolded = new JCheckBox("Is Unfolded");
	
	//ROTATION SETTINGS
	private JPanel rotationPanel = new JPanel();
	public JTextField rotationTF = new JTextField();
	private JLabel rotationLabel = new JLabel("° Rotation");
	private JButton setRotationTextButton = new JButton("Set");
	public JCheckBox reverseRotSymbol = new JCheckBox("Reverse");
	
	//NEXT VIEW HERE SETTINGS
	private JPanel nextViewPanel = new JPanel();
	private JSlider nextViewSlider = new JSlider(0, 360);

	MainScreen screen;


	public UITopPanel(MainScreen __screen) {
		this.screen = __screen;
		setPreferredSize(new Dimension(1000, 70));
		setBackground(new Color(230, 230, 230));

		addFilledFacePanel();

		addLineInputPanel();
		addLineTypeChangePanel();
		addLineInputByLengthAnglePanel();
		addChangeExistingCreaseEndsPanel();
		addVertexFractionOfLinePanel();

		addArrowInputPanel();
		addArrowTypeChangePanel();
		addArrowSettingsPanel();

		addSymbolInputPanel();
		addSymbolLeaderPanel();
		addSymbolLeaderChangePanel();
		addEqualDistSymbolPanel();
		addEqualAnglSymbolPanel();
		addPleatCrimpSymbolPanel();
		
		
		//new stuff
		addRotationPanel();
		addNextViewPanel();


		//add all panels to UITopPanel
		add(changeLinePanel);
		add(changeCreaseEndsPanel);
		add(changeArrowPanel);
		add(faceDirectionPanel);
		add(inputLinesPanel);
		add(inputLineLengthAnglePanel);
		add(inputVertexFractionPanel);
		add(inputArrowPanel);
		add(inputSymbolsPanel);
		add(inputSymbolLeaderPanel);
		add(changeSymbolLeaderPanel);
		add(equalDistPanel);
		add(equalAnglPanel);
		add(pleatPanel);
		add(arrowPanel);
		add(rotationPanel);
		add(nextViewPanel);

		modeChanged();
	}


	private void addFilledFacePanel() {
		faceUpInput.addActionListener(this);
		faceDownInput.addActionListener(this);

		ButtonGroup faceDirectionInput = new ButtonGroup();
		faceDirectionInput.add(faceUpInput);
		faceDirectionInput.add(faceDownInput);

		faceDirectionPanel.add(faceUpInput);
		faceDirectionPanel.add(faceDownInput);
		faceDirectionPanel.setLayout(new BoxLayout(faceDirectionPanel, BoxLayout.PAGE_AXIS));
		faceDirectionPanel.setVisible(false);
	}

	private void addLineInputPanel() {
		menuLineCB.setRenderer(new IndentedRenderer());
		menuLineCB.setSelectedIndex(0);
		menuLineCB.addActionListener(this);

		inputLinesPanel.add(menuLineCB);
		inputLinesPanel.setBorder(new TitledBorder(
				new EtchedBorder(BevelBorder.RAISED, 
						getBackground().darker(), 
						getBackground().brighter()), "Input Line Type"));
		inputLinesPanel.setVisible(false);
	}

	private void addLineTypeChangePanel() {
		changeLineButton.addActionListener(this);
		changeLinePanel.add(changeLineTypeCB);
		changeLinePanel.add(changeLineButton);
		changeLinePanel.setBorder(new TitledBorder(
				new EtchedBorder(BevelBorder.RAISED, 
						getBackground().darker(), 
						getBackground().brighter()), "Change Line Type"));
		changeLinePanel.setVisible(false);
	}
	
	private void addLineInputByLengthAnglePanel() {
		inputLineLengthTF.setPreferredSize(new Dimension(50, 25));
		inputLineAngleTF.setPreferredSize(new Dimension(50, 25));		
		inputLineLengthAnglePanel.add(lengthLabel);
		inputLineLengthAnglePanel.add(inputLineLengthTF);
		inputLineLengthAnglePanel.add(angleLabel);
		inputLineLengthAnglePanel.add(inputLineAngleTF);
		changeSymbolLeaderPanel.setBorder(new TitledBorder(
				new EtchedBorder(BevelBorder.RAISED, 
						getBackground().darker(), 
						getBackground().brighter()), "Line Input by Angle & Length"));
	}

	private void addChangeExistingCreaseEndsPanel() {
		startCreaseCB.addActionListener(this);
		endCreaseCB.addActionListener(this);
		changeCreaseEndsPanel.add(startCreaseCB);
		changeCreaseEndsPanel.add(endCreaseCB);
		changeCreaseEndsPanel.setBorder(new TitledBorder(
				new EtchedBorder(BevelBorder.RAISED, 
						getBackground().darker(), 
						getBackground().brighter()), "Existing Crease Settingss"));
		changeCreaseEndsPanel.setVisible(false);
	}

	private void addVertexFractionOfLinePanel() {
		inputVertexFractionSlider.setMajorTickSpacing(10);
		//inputVertexFractionSlider.setSnapToTicks(true);
		inputVertexFractionSlider.setPaintTicks(true);
		inputVertexFractionSlider.addChangeListener(e -> sliderVertexFraction());
		inputVertexFractionTF.setPreferredSize(new Dimension(30, 30));
		PlainDocument docInputVertexFraction = (PlainDocument) inputVertexFractionTF.getDocument();
		docInputVertexFraction.setDocumentFilter(new IntFilter());
		docInputVertexFraction.addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				//setVertexFractionSlider();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				setVertexFractionSlider();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				setVertexFractionSlider();
			}

			private void setVertexFractionSlider() {
				Runnable doSetVertexFractionSlider = new Runnable() {
					@Override
					public void run() {
						String text = inputVertexFractionTF.getText();
						int value = Integer.parseInt(text);

						inputVertexFractionSlider.setValue(value);
					}
				};       
				SwingUtilities.invokeLater(doSetVertexFractionSlider);
			}
		});
		inputVertexFractionTF.setText(Integer.toString(inputVertexFractionSlider.getValue()));
		inputVertexFractionPanel.add(inputVertexFractionSlider);
		inputVertexFractionPanel.add(inputVertexFractionTF);
		inputVertexFractionPanel.setBorder(new TitledBorder(
				new EtchedBorder(BevelBorder.RAISED, 
						getBackground().darker(), 
						getBackground().brighter()), "Input Vertex"));
		inputVertexFractionPanel.setVisible(false);
	}

	private void addArrowInputPanel() {
		menuArrowCB.setRenderer(new SeparatorComboBoxRenderer());
		menuArrowCB.setSelectedIndex(0);
		menuArrowCB.addActionListener(new SeparatorComboBoxListener(menuArrowCB));

		inputArrowPanel.add(menuArrowCB);
		inputArrowPanel.setBorder(new TitledBorder(
				new EtchedBorder(BevelBorder.RAISED, 
						getBackground().darker(), 
						getBackground().brighter()), "Input Arrow Type"));
		inputArrowPanel.setVisible(false);
	}

	private void addArrowTypeChangePanel() {
		changeArrowButton.addActionListener(this);
		changeArrowTypeCB.setRenderer(new SeparatorComboBoxRenderer());
		changeArrowPanel.add(changeArrowTypeCB);
		changeArrowPanel.add(changeArrowButton);
		changeArrowPanel.setBorder(new TitledBorder(
				new EtchedBorder(BevelBorder.RAISED, 
						getBackground().darker(), 
						getBackground().brighter()), "Change Arrow Type"));
		changeArrowPanel.setVisible(false);
	}

	private void addArrowSettingsPanel() {
		arrowIsMirrored.addActionListener(this);
		arrowPanel.add(arrowIsMirrored);
		arrowIsUnfolded.addActionListener(this);
		arrowPanel.add(arrowIsUnfolded);
		arrowPanel.setVisible(false);
	}

	private void addSymbolInputPanel() {
		symbolInputCB.setRenderer(new SeparatorComboBoxRenderer());
		symbolInputCB.setSelectedIndex(0);
		symbolInputCB.addActionListener(new SeparatorComboBoxListener(symbolInputCB));

		inputSymbolsPanel.add(symbolInputCB);
		inputSymbolsPanel.setBorder(new TitledBorder(
				new EtchedBorder(BevelBorder.RAISED, 
						getBackground().darker(), 
						getBackground().brighter()), "Input Symbol Type"));
		inputSymbolsPanel.setVisible(false);
	}

	private void addSymbolLeaderPanel() {
		inputLeaderTextTF.setPreferredSize(new Dimension(150, 25));
		inputSymbolLeaderPanel.add(inputLeaderTextTF);
		inputSymbolLeaderPanel.setBorder(new TitledBorder(
				new EtchedBorder(BevelBorder.RAISED, 
						getBackground().darker(), 
						getBackground().brighter()), "Input Text"));
		inputSymbolLeaderPanel.setVisible(false);
	}

	private void addSymbolLeaderChangePanel() {
		changeLeaderTextTF.setPreferredSize(new Dimension(150, 25));
		changeSymbolLeaderPanel.add(changeLeaderTextTF);
		changeSymbolLeaderPanel.add(changeSymbolLeaderButton);
		changeSymbolLeaderButton.addActionListener(this);
		changeSymbolLeaderPanel.setBorder(new TitledBorder(
				new EtchedBorder(BevelBorder.RAISED, 
						getBackground().darker(), 
						getBackground().brighter()), "Change Text"));
		changeSymbolLeaderPanel.setVisible(false);
	}

	private void addEqualDistSymbolPanel() {
		sliderEqualDist.setMajorTickSpacing(10);
		sliderEqualDist.setPaintTicks(true);
		sliderEqualDist.setSnapToTicks(true);
		sliderEqualDist.addChangeListener(e -> sliderEqualDistChanged());
		equalDistDividerTF.setPreferredSize(new Dimension(20,20));
		PlainDocument docEqualDistDivider = (PlainDocument) equalDistDividerTF.getDocument();
		docEqualDistDivider.setDocumentFilter(new IntFilter());
		equalDistDividerTF.setText(Integer.toString(Globals.gridDivNum));
		equalDistButton.addActionListener(this);
		equalDistPanel.add(sliderEqualDist);
		equalDistPanel.add(equalDistDividerTF);
		equalDistPanel.add(equalDistButton);
		equalDistPanel.setBorder(new TitledBorder(
				new EtchedBorder(BevelBorder.RAISED, 
						getBackground().darker(),
						getBackground().brighter()), "Equal Distance"));
		equalDistPanel.setVisible(false);
	}

	private void addEqualAnglSymbolPanel() {
		equalAnglSlider.setMajorTickSpacing(50);
		equalAnglSlider.setSnapToTicks(true);
		equalAnglSlider.setPaintTicks(true);
		equalAnglSlider.addChangeListener(e -> sliderEqualAnglChanged());
		equalAnglDividerTF.setPreferredSize(new Dimension(20,20));
		PlainDocument docEqualAnglDivider = (PlainDocument) equalDistDividerTF.getDocument();
		docEqualAnglDivider.setDocumentFilter(new IntFilter());
		equalAnglDividerTF.setText(Integer.toString(Globals.gridDivNum));
		equalAnglButton.addActionListener(this);
		equalAnglPanel.add(equalAnglSlider);
		equalAnglSlider.setVisible(false);
		equalAnglPanel.add(equalAnglDividerTF);
		equalAnglPanel.add(equalAnglButton);
		equalAnglPanel.setBorder(new TitledBorder(
				new EtchedBorder(BevelBorder.RAISED, 
						getBackground().darker(),
						getBackground().brighter()), "Equal Angle"));
		equalAnglPanel.setVisible(false);
	}

	private void addPleatCrimpSymbolPanel() {
		ButtonGroup pleatCrimpGroup = new ButtonGroup();
		pleatCrimpGroup.add(pleatRB);
		pleatCrimpGroup.add(crimpRB);
		JPanel crimpPleatPanel = new JPanel();
		crimpPleatPanel.add(pleatRB);
		crimpPleatPanel.add(crimpRB);
		crimpPleatPanel.setLayout(new BoxLayout(crimpPleatPanel, BoxLayout.PAGE_AXIS));
		pleatRB.addActionListener(this);
		crimpRB.addActionListener(this);
		pleatTF.setPreferredSize(new Dimension(20,20));
		pleatTF.setText("2");
		PlainDocument docPleatLayerCount = (PlainDocument) pleatTF.getDocument();
		docPleatLayerCount.setDocumentFilter(new IntFilter());
		pleatButton.addActionListener(this);
		pleatCB.addActionListener(this);
		pleatPanel.add(crimpPleatPanel);
		pleatPanel.add(pleatCB);
		pleatPanel.add(pleatTF);
		pleatPanel.add(pleatButton);
		pleatPanel.setBorder(new TitledBorder(
				new EtchedBorder(BevelBorder.RAISED, 
						getBackground().darker(),
						getBackground().brighter()), "Pleats & Crimps"));
		pleatPanel.setVisible(false);
	}
	
	private void addRotationPanel() {
		rotationTF.addActionListener(this);
		reverseRotSymbol.addActionListener(this);
		setRotationTextButton.addActionListener(this);
		setRotationTextButton.setPreferredSize(new Dimension(53, 25));
		rotationTF.setPreferredSize(new Dimension(30, 20));
		rotationTF.setText("90");
		rotationTF.setHorizontalAlignment(SwingConstants.RIGHT);
		PlainDocument docPleatLayerCount = (PlainDocument) rotationTF.getDocument();
		docPleatLayerCount.setDocumentFilter(new IntFilter());
		rotationPanel.add(rotationTF);
		rotationPanel.add(rotationLabel);
		rotationPanel.add(setRotationTextButton);
		setRotationTextButton.setVisible(false);
		rotationPanel.add(reverseRotSymbol);
		rotationPanel.setBorder(new TitledBorder(
				new EtchedBorder(BevelBorder.RAISED, 
						getBackground().darker(),
						getBackground().brighter()), "Rotation Symbol"));
		rotationPanel.setVisible(false);
	}
	
	private void addNextViewPanel() {
		nextViewSlider.setMajorTickSpacing(90);
		nextViewSlider.setMinorTickSpacing(45);
		nextViewSlider.setPaintTicks(true);
		
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(0, new JLabel("0°"));
		labelTable.put(90, new JLabel("90°"));
		labelTable.put(180, new JLabel("180°"));
		labelTable.put(270, new JLabel("270°"));
		labelTable.put(360, new JLabel("360°"));
		nextViewSlider.setLabelTable(labelTable);
		nextViewSlider.setPaintLabels(true);
		
		nextViewSlider.addChangeListener(e -> sliderNextViewSymbol());
		nextViewPanel.add(nextViewSlider);
		nextViewPanel.setBorder(new TitledBorder(
				new EtchedBorder(BevelBorder.RAISED, 
						getBackground().darker(),
						getBackground().brighter()), "Next View Symbol"));
		nextViewPanel.setVisible(false);
	}



	private void changeLeaderRepetitionText() {
		for (OriGeomSymbol s : Origrammer.diagram.steps.get(Globals.currentStep).geomSymbols) {
			if (s.isSelected() && (s.getType() == OriGeomSymbol.TYPE_LEADER 
					|| s.getType() == OriGeomSymbol.TYPE_REPETITION)) {
				s.setText(changeLeaderTextTF.getText());
			}
		}
	}

	private void setEqualDistanceDividerCount() {
		for (OriEqualDistSymbol eds : Origrammer.diagram.steps.get(Globals.currentStep).equalDistSymbols) {
			if (eds.isSelected()) {
				eds.setDividerCount(Integer.parseInt(equalDistDividerTF.getText()));
			}
			screen.repaint();
		}
	}

	private void setEqualAngleDividerCount() {
		for (OriEqualAnglSymbol eas : Origrammer.diagram.steps.get(Globals.currentStep).equalAnglSymbols) {
			if (eas.isSelected()) {
				eas.setDividerCount(Integer.parseInt(equalAnglDividerTF.getText()));
			}
			screen.repaint();
		}
	}

	private void setPleatLayerCount() {
		for (OriPleatCrimpSymbol pleat : Origrammer.diagram.steps.get(Globals.currentStep).pleatCrimpSymbols) {
			if (pleat.isSelected()) {
				pleat.setLayersCount(Integer.parseInt(pleatTF.getText()));
			}
			screen.repaint();
		}
	}

	private void setPleatIsSwitchedDir() {
		for (OriPleatCrimpSymbol pleat : Origrammer.diagram.steps.get(Globals.currentStep).pleatCrimpSymbols) {
			if (pleat.isSelected()) {
				pleat.setIsSwitchedDir(pleatCB.isSelected());
			}
			screen.repaint();
		}
	}

	private void changeToPleat() {
		for(OriPleatCrimpSymbol pc : Origrammer.diagram.steps.get(Globals.currentStep).pleatCrimpSymbols) {
			if (pc.isSelected()) {
				pc.setType(OriPleatCrimpSymbol.TYPE_PLEAT);
			}
			screen.repaint();
		}
	}

	private void changeToCrimp() {
		for(OriPleatCrimpSymbol pc : Origrammer.diagram.steps.get(Globals.currentStep).pleatCrimpSymbols) {
			if (pc.isSelected()) {
				pc.setType(OriPleatCrimpSymbol.TYPE_CRIMP);
			}
			screen.repaint();
		}
	}

	private void sliderVertexFraction() {
		inputVertexFractionTF.setText(Integer.toString(inputVertexFractionSlider.getValue()));
	}

	private void changeArrowType() {
		for (OriArrow a : Origrammer.diagram.steps.get(Globals.currentStep).arrows) {
			if (a.isSelected()) {
				String arrowType = changeArrowTypeCB.getSelectedItem().toString();

				if (arrowType == "Valley Fold") {
					a.setType(OriArrow.TYPE_VALLEY);
				} else if (arrowType == "Mountain Fold") {
					a.setType(OriArrow.TYPE_MOUNTAIN);
				} else if (arrowType == "Turn over") {
					a.setType(OriArrow.TYPE_TURN_OVER);
				} else if (arrowType == "Push here") {
					a.setType(OriArrow.TYPE_PUSH_HERE);
				} else if (arrowType == "Pull out") {
					a.setType(OriArrow.TYPE_PULL_HERE);
				} else if (arrowType == "Inflate here") {
					a.setType(OriArrow.TYPE_INFLATE_HERE);
				}
			}
		}
		screen.repaint();
	}
	
	private void mirrorAllSelectedArrows() {
		for (OriArrow a : Origrammer.diagram.steps.get(Globals.currentStep).arrows) {
			if (a.isSelected()) {
					a.setMirrored(arrowIsMirrored.isSelected());
			}
		}
	}
	
	private void addUnfoldToAllSelectedArrows() {
		for (OriArrow a : Origrammer.diagram.steps.get(Globals.currentStep).arrows) {
			if (a.isSelected()) {
					a.setUnfold(arrowIsUnfolded.isSelected());
			}
		}
	}

	/**
	 * Sets the translation distance of equalDistanceSymbol
	 */
	private void sliderEqualDistChanged() {		
		for (OriEqualDistSymbol eds : Origrammer.diagram.steps.get(Globals.currentStep).equalDistSymbols) {
			if (eds.isSelected()) {
				eds.setTranslationDist(sliderEqualDist.getValue());
				screen.repaint();
			}
		}
	}

	/**
	 * Sets the translation distance of equalDistanceSymbol
	 */
	private void sliderEqualAnglChanged() {		
		for (OriEqualAnglSymbol eas : Origrammer.diagram.steps.get(Globals.currentStep).equalAnglSymbols) {
			if (eas.isSelected()) {
				eas.setLength(equalAnglSlider.getValue());
				screen.repaint();
			}
		}
	}
	
	private void sliderNextViewSymbol() {
		for (OriGeomSymbol s : Origrammer.diagram.steps.get(Globals.currentStep).geomSymbols) {
			if (s.isSelected()) {
				Vector2d uv = new Vector2d(1,0);
				double angle = Math.toRadians(nextViewSlider.getValue());
				double angleX = uv.x * Math.cos(angle) - uv.y * Math.sin(angle);
				double angleY = uv.x * Math.sin(angle) + uv.y * Math.cos(angle);
				Vector2d direction = new Vector2d(angleX, angleY);
				s.setP2(new Vector2d(s.getP1().x + direction.x * 50, s.getP1().y + direction.y * 50));
			
				screen.repaint();
			}
		}
	}

	private void creaseStartTranslation() {
		for (OriLine l : Origrammer.diagram.steps.get(Globals.currentStep).lines) {
			if (l.isSelected()) {
				if (startCreaseCB.isSelected()) {
					l.setStartOffset(true);
				} else if (!startCreaseCB.isSelected()) {
					l.setStartOffset(false);
				}
			}
		}
	}

	private void creaseEndTranslation() {
		for (OriLine l : Origrammer.diagram.steps.get(Globals.currentStep).lines) {
			if (l.isSelected()) {
				if (endCreaseCB.isSelected()) {
					l.setEndOffset(true);
				} else if (!endCreaseCB.isSelected()) {
					l.setEndOffset(false);
				}
			}
		}
	}
	
	private void reverseRotationSymbol() {
		for (OriGeomSymbol s : Origrammer.diagram.steps.get(Globals.currentStep).geomSymbols) {
			if (s.isSelected() && s.getType() == OriGeomSymbol.TYPE_ROTATION) {
					s.setReversed(reverseRotSymbol.isSelected());
			}
		}
	}
	
	private void setRotationSymbolText() {
		for (OriGeomSymbol s : Origrammer.diagram.steps.get(Globals.currentStep).geomSymbols) {
			if (s.isSelected() && s.getType() == OriGeomSymbol.TYPE_ROTATION) {
				s.setText(rotationTF.getText() + "°");
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == changeLineButton) {
			for (OriLine l : Origrammer.diagram.steps.get(Globals.currentStep).sharedLines.keySet()) {
				if (l.isSelected()) {
					String lineType = changeLineTypeCB.getSelectedItem().toString();

					if (lineType == "Valley Fold") {
						l.setType(OriLine.VALLEY);
					} else if (lineType == "Mountain Fold") {
						l.setType(OriLine.MOUNTAIN);
					} else if (lineType == "X-Ray Fold") {
						l.setType(OriLine.XRAY);
					} else if (lineType == "Edge Line") {
						l.setType(OriLine.EDGE);
					} else if (lineType == "Existing Crease") {
						l.setType(OriLine.CREASE);
					}
				}
			}
			screen.repaint();
		} else if (e.getSource() == changeArrowButton) {
			changeArrowType();
		} else if (e.getSource() == arrowIsMirrored) {
			mirrorAllSelectedArrows();
		} else if (e.getSource() == arrowIsUnfolded) {
			addUnfoldToAllSelectedArrows();
		} else if (e.getSource() == changeSymbolLeaderButton) {
			changeLeaderRepetitionText();
		} else if (e.getSource() == faceUpInput) {
			Globals.faceInputDirection = Constants.FaceInputDirection.FACE_UP;
		} else if (e.getSource() == faceDownInput) {
			Globals.faceInputDirection = Constants.FaceInputDirection.FACE_DOWN;
		} else if (e.getSource() == equalDistButton) {
			setEqualDistanceDividerCount();
		} else if (e.getSource() == equalAnglButton) {
			setEqualAngleDividerCount();
 		} else if (e.getSource() == pleatButton) {
			setPleatLayerCount();
		} else if (e.getSource() == pleatCB) {
			setPleatIsSwitchedDir();
		} else if (e.getSource() == pleatRB) {
			changeToPleat();
		} else if (e.getSource() == crimpRB) {
			changeToCrimp();
		} else if (e.getSource() == startCreaseCB) {
			creaseStartTranslation();				
		} else if (e.getSource() == endCreaseCB) {
			creaseEndTranslation();
		} else if (e.getSource() == reverseRotSymbol) {
			reverseRotationSymbol();
		} else if (e.getSource() == setRotationTextButton) {
			setRotationSymbolText();
		}


		if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_LINE) {
			Object selectedLine = menuLineCB.getSelectedItem();

			if (selectedLine == "Valley Fold") {
				Globals.inputLineType = OriLine.VALLEY;
			} else if (selectedLine == "Mountain Fold") {
				Globals.inputLineType = OriLine.MOUNTAIN;
			} else if (selectedLine == "X-Ray Fold") {				
				Globals.inputLineType = OriLine.XRAY;
			} else if (selectedLine == "Edge Line") {
				Globals.inputLineType = OriLine.EDGE;
			} else if (selectedLine == "Existing Crease") {
				Globals.inputLineType = OriLine.CREASE;
			}
		}
		modeChanged();
	}

	public void modeChanged() {

		if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_LINE) {
			inputLinesPanel.setVisible(true);
			if (Globals.inputLineType == OriLine.CREASE) {
				changeCreaseEndsPanel.setVisible(true);
			} else {
				changeCreaseEndsPanel.setVisible(false);
			}
			if (Globals.lineEditMode == Constants.LineInputMode.BY_LENGTH_AND_ANGLE) {
				if (Origrammer.mainFrame.uiSidePanel.measureLengthTF.getText().length() > 0) {
					inputLineLengthTF.setText(Origrammer.mainFrame.uiSidePanel.measureLengthTF.getText());
				}

				if (Origrammer.mainFrame.uiSidePanel.measureAngleTF.getText().length() > 0) {
					inputLineAngleTF.setText(Origrammer.mainFrame.uiSidePanel.measureAngleTF.getText());
				}

				inputLineLengthAnglePanel.setVisible(true);
			} else {
				inputLineLengthAnglePanel.setVisible(false);
			}
		} else {
			inputLinesPanel.setVisible(false);
			changeCreaseEndsPanel.setVisible(false);
			inputLineLengthAnglePanel.setVisible(false);
		}

		if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_VERTEX
				&& Globals.vertexInputMode == Constants.VertexInputMode.FRACTION_OF_LINE) {
			inputVertexFractionPanel.setVisible(true);
		} else {
			inputVertexFractionPanel.setVisible(false);
		}

		if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_ARROW) {
			inputArrowPanel.setVisible(true);
			arrowPanel.setVisible(true);
		} else {
			inputArrowPanel.setVisible(false);
			arrowPanel.setVisible(false);
		}

		if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_SYMBOL) {
			inputSymbolsPanel.setVisible(true);
			if (Globals.inputSymbolMode == Constants.InputSymbolMode.LEADER 
					|| Globals.inputSymbolMode == Constants.InputSymbolMode.REPETITION_BOX) {
				inputSymbolLeaderPanel.setVisible(true);
			} else {
				inputSymbolLeaderPanel.setVisible(false);
			}
			if (Globals.inputSymbolMode == Constants.InputSymbolMode.EQUAL_DIST) {
				equalDistPanel.setVisible(true);
			} else {
				equalDistPanel.setVisible(false);
			}
			if (Globals.inputSymbolMode == Constants.InputSymbolMode.EQUAL_ANGL) {
				equalAnglPanel.setVisible(true);
			} else {
				equalAnglPanel.setVisible(false);
			}
			if (Globals.inputSymbolMode == Constants.InputSymbolMode.ROTATIONS) {
				rotationPanel.setVisible(true);
			} else {
				rotationPanel.setVisible(false);
			}
			if (Globals.inputSymbolMode == Constants.InputSymbolMode.CRIMPING_PLEATING) {
				pleatPanel.setVisible(true);
			} else {
				pleatPanel.setVisible(false);
			}
		} else {
			inputSymbolsPanel.setVisible(false);
			inputSymbolLeaderPanel.setVisible(false);
			changeSymbolLeaderPanel.setVisible(false);
			//inputSymbolRepetitionPanel.setVisible(false);
			equalDistPanel.setVisible(false);
			equalAnglPanel.setVisible(false);
			rotationPanel.setVisible(false);
			nextViewPanel.setVisible(false);
			pleatPanel.setVisible(false);
		}

		if (Globals.toolbarMode == Constants.ToolbarMode.FILL_TOOL) {
			faceDirectionPanel.setVisible(true);
		} else {
			faceDirectionPanel.setVisible(false);
		}

		if (Globals.toolbarMode == Constants.ToolbarMode.SELECTION_TOOL) {
			
			for (OriPolygon p : Origrammer.diagram.steps.get(Globals.currentStep).polygons) {
				for (OriLine curL : p.lines) {
					if (curL.isSelected()) {
						changeLinePanel.setVisible(true);
						if (curL.getType() == OriLine.CREASE) {
							changeCreaseEndsPanel.setVisible(true);
						}
						break;
					} else {
						for (OriLine sharedL : Origrammer.diagram.steps.get(Globals.currentStep).sharedLines.keySet()) {
							if (sharedL.isSelected()) {
								changeLinePanel.setVisible(true);
								if (sharedL.getType() == OriLine.CREASE) {
									changeCreaseEndsPanel.setVisible(true);
								}
								break;
							} else {
								changeLinePanel.setVisible(false);
								changeCreaseEndsPanel.setVisible(false);
							}
						}
					}
				}
			}
			
			
			for (OriArrow a : Origrammer.diagram.steps.get(Globals.currentStep).arrows) {
				if (a.isSelected()) {
					changeArrowPanel.setVisible(true);
					arrowPanel.setVisible(true);
					break;
				} else {
					changeArrowPanel.setVisible(false);
					arrowPanel.setVisible(false);
				}
			}
			for (OriFace f : Origrammer.diagram.steps.get(Globals.currentStep).filledFaces) {
				if (f.isSelected()) {
					faceDirectionPanel.setVisible(true);
					break;
				} else {
					faceDirectionPanel.setVisible(false);
				}
			}
			for (OriGeomSymbol gs : Origrammer.diagram.steps.get(Globals.currentStep).geomSymbols) {
				if (gs.isSelected()) {
					if (gs.getType() == OriGeomSymbol.TYPE_ROTATION) {
						rotationPanel.setVisible(true);
						setRotationTextButton.setVisible(true);
						break;
					} else if (gs.getType() == OriGeomSymbol.TYPE_NEXT_VIEW_HERE) {
						nextViewPanel.setVisible(true);
						break;
					} else if (gs.getType() == OriGeomSymbol.TYPE_HOLD) {
						//TODO: show editingOptions for OriGeomSymbol.TYPE_HOLD
						break;
						
					} else if (gs.getType() == OriGeomSymbol.TYPE_HOLD_AND_PULL){
						//TODO: show editingOptions for OriGeomSymbol.TYPE_HOLD_AND_PULL
						break;
					} else if (gs.getType() == OriGeomSymbol.TYPE_CLOSED_SINK) {
						//TODO: show editingOptions for OriGeomSymbol.TYPE_CLOSED_SINK
						break;
					} else if (gs.getType() == OriGeomSymbol.TYPE_LEADER
							|| gs.getType() == OriGeomSymbol.TYPE_REPETITION) {
						changeSymbolLeaderPanel.setVisible(true);
						break;
					}
					
				} else {
					rotationPanel.setVisible(false);
					setRotationTextButton.setVisible(false);
					nextViewPanel.setVisible(false);
					changeSymbolLeaderPanel.setVisible(false);
				}
			}
			for (OriEqualDistSymbol eds : Origrammer.diagram.steps.get(Globals.currentStep).equalDistSymbols) {
				if (eds.isSelected()) {
					equalDistPanel.setVisible(true);
					break;
				} else {
					equalDistPanel.setVisible(false);
				}
			}
			for (OriEqualAnglSymbol eas : Origrammer.diagram.steps.get(Globals.currentStep).equalAnglSymbols) {
				if (eas.isSelected()) {
					equalAnglPanel.setVisible(true);
					equalAnglSlider.setVisible(true);
					break;
				} else {
					equalAnglPanel.setVisible(false);
					equalAnglSlider.setVisible(false);
				}
			}
			for (OriPleatCrimpSymbol pcs : Origrammer.diagram.steps.get(Globals.currentStep).pleatCrimpSymbols) {
				if (pcs.isSelected()) {
					pleatPanel.setVisible(true);
					break;
				} else {
					pleatPanel.setVisible(false);
				}
			}
		}
		screen.modeChanged();
	}

	//source: http://esus.com/creating-a-jcombobox-with-a-divider-separator-line/
	class SeparatorComboBoxListener implements ActionListener {
		JComboBox<Object> combobox;
		Object oldItem;

		SeparatorComboBoxListener(JComboBox<Object> combobox) {
			this.combobox = combobox;
			combobox.setSelectedIndex(0);
			oldItem = combobox.getSelectedItem();
		}

		public void actionPerformed(ActionEvent e) {

			if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_ARROW) {
				Object selectedArrow = menuArrowCB.getSelectedItem();

				if (selectedArrow instanceof JSeparator) {
					combobox.setSelectedItem(oldItem);
				} else {
					oldItem = selectedArrow;
				}
				if (selectedArrow == "Valley Fold") {
					Globals.inputArrowType = OriArrow.TYPE_VALLEY;
				} else if (selectedArrow == "Mountain Fold") {
					Globals.inputArrowType = OriArrow.TYPE_MOUNTAIN;
				} else if (selectedArrow == "Turn over") {
					Globals.inputArrowType = OriArrow.TYPE_TURN_OVER;
				} else if (selectedArrow == "Push here") {
					Globals.inputArrowType = OriArrow.TYPE_PUSH_HERE;
				} else if (selectedArrow == "Pull out") {
					Globals.inputArrowType = OriArrow.TYPE_PULL_HERE;
				} else if (selectedArrow == "Inflate here") {
					Globals.inputArrowType = OriArrow.TYPE_INFLATE_HERE;
				}
			} else if (Globals.toolbarMode == Constants.ToolbarMode.INPUT_SYMBOL) {
				Object inputSymbol = symbolInputCB.getSelectedItem();

				if (inputSymbol == "Leader") {
					Globals.inputSymbolMode = Constants.InputSymbolMode.LEADER;
				} else if (inputSymbol == "Repetition Box") {
					Globals.inputSymbolMode = Constants.InputSymbolMode.REPETITION_BOX;
				} else if (inputSymbol == "Equal Distances") {
					Globals.inputSymbolMode = Constants.InputSymbolMode.EQUAL_DIST;
				} else if (inputSymbol == "Equal Angles") {
					Globals.inputSymbolMode = Constants.InputSymbolMode.EQUAL_ANGL;
				} else if (inputSymbol == "Rotations") {
					Globals.inputSymbolMode = Constants.InputSymbolMode.ROTATIONS;
				} else if (inputSymbol == "X-Ray Circle") {
					Globals.inputSymbolMode = Constants.InputSymbolMode.X_RAY_CIRCLE;
				} else if (inputSymbol == "Fold over and over") {
					Globals.inputSymbolMode = Constants.InputSymbolMode.FOLD_OVER_AND_OVER;
				} else if (inputSymbol == "Next View Here") {
					Globals.inputSymbolMode = Constants.InputSymbolMode.NEXT_VIEW;
				} else if (inputSymbol == "Hold Here") {
					Globals.inputSymbolMode = Constants.InputSymbolMode.HOLD_HERE;
				} else if (inputSymbol == "Hold Here and Pull") {
					Globals.inputSymbolMode = Constants.InputSymbolMode.HOLD_HERE_AND_PULL;
				} else if (inputSymbol == "Crimping & Pleating") {
					Globals.inputSymbolMode = Constants.InputSymbolMode.CRIMPING_PLEATING;
				} else if (inputSymbol == "Sinks") {
					Globals.inputSymbolMode = Constants.InputSymbolMode.SINKS;
				}
			}
			modeChanged();

		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {		
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {		
	}

}
