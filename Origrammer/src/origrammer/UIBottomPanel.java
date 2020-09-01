package origrammer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import origrammer.geometry.OriArrow;
import origrammer.geometry.OriEqualAnglSymbol;
import origrammer.geometry.OriEqualDistSymbol;
import origrammer.geometry.OriFace;
import origrammer.geometry.OriGeomSymbol;
import origrammer.geometry.OriLine;
import origrammer.geometry.OriPleatCrimpSymbol;
import origrammer.geometry.OriPolygon;
import origrammer.geometry.OriVertex;

public class UIBottomPanel extends JPanel implements ActionListener, PropertyChangeListener, KeyListener {

	private JFormattedTextField foldDescr = new JFormattedTextField();

	private JButton stepBack = new JButton("<");
	private JFormattedTextField currentStepTF = new JFormattedTextField(new DecimalFormat("###"));
	private JButton stepForth = new JButton(">");

	//FOLD INSTRUCTION
	private JPanel foldDescrPanel = new JPanel();

	//STEP NAVIGATION
	private JPanel stepNavigation = new JPanel();

	private JPanel newStepPanel = new JPanel();
	private JRadioButton newEmptyStep = new JRadioButton("Empty Step", false);
	private JRadioButton newCopiedStep = new JRadioButton("Copy last Step", true);
	private JRadioButton newBaseShapeStep = new JRadioButton("Basic Paper Shape", false);
	private ButtonGroup newStepGroup;

	private MainScreen screen;

	public UIBottomPanel(MainScreen __screen) {
		this.screen = __screen;
		setPreferredSize(new Dimension(500, 100));
		setBackground(new Color(230, 230, 230));

		//ActionListener
		stepBack.addActionListener(this);
		stepForth.addActionListener(this);

		addFoldInstructionPanel();
		addStepNavigationPanel();

		JPanel stepDescrAndNavPanel = new JPanel();
		stepDescrAndNavPanel.add(foldDescrPanel);
		stepDescrAndNavPanel.add(stepNavigation);
		stepDescrAndNavPanel.setLayout(new GridLayout(2, 1, 10, 2));

		add(Box.createRigidArea(new Dimension(190,70)));
		add(stepDescrAndNavPanel);

		foldDescr.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
				changed();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				changed();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				changed();
			}

			public void changed() {
				Origrammer.diagram.steps.get(Globals.currentStep).stepDescription = foldDescr.getText();
				Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();
			}
		});
		newEmptyStep.addActionListener(this);
		newCopiedStep.addActionListener(this);
		newBaseShapeStep.addActionListener(this);

		newStepGroup = new ButtonGroup();
		newStepGroup.add(newEmptyStep);
		newStepGroup.add(newBaseShapeStep);
		newStepGroup.add(newCopiedStep);

		newStepPanel.add(newEmptyStep);
		newStepPanel.add(newCopiedStep);
		newStepPanel.add(newBaseShapeStep);
		newStepPanel.setLayout(new BoxLayout(newStepPanel, BoxLayout.PAGE_AXIS));

		add(newStepPanel);

		stepChanged();
	}

	private void addFoldInstructionPanel() {

		JLabel foldDescrTitle = new JLabel("Fold Instruction", SwingConstants.CENTER);
		foldDescr.setPreferredSize(new Dimension(400, 20));	
		foldDescrPanel.add(foldDescrTitle);
		foldDescrPanel.add(foldDescr);
		foldDescrPanel.setLayout(new GridLayout(2, 1, 10, 2));
		//foldDescrPanel.setLayout(new BoxLayout(foldDescrPanel, BoxLayout.Y_AXIS));
	}

	private void addStepNavigationPanel() {
		stepBack.setPreferredSize(new Dimension(30, 10));
		stepForth.setPreferredSize(new Dimension(30, 10));
		currentStepTF.setHorizontalAlignment(JFormattedTextField.CENTER);
		currentStepTF.setPreferredSize(new Dimension(30, 10));
		currentStepTF.setMaximumSize(new Dimension(30, 10));
		currentStepTF.setValue(Globals.currentStep);
		stepNavigation.add(stepBack);
		stepNavigation.add(currentStepTF);
		stepNavigation.add(stepForth);
		stepNavigation.setLayout(new GridLayout(1, 3, 100, 2));
	}

	/**
	 * Go one {@code Step} forward and create new {@code Step} 
	 * if the current {@code Step} was the last one
	 */
	public void stepForth() {
		Globals.currentStep += 1;
		
		if (Globals.currentStep-1 == Origrammer.diagram.steps.size() - 1) {
			createNewStep();
		}
		Origrammer.diagram.steps.get(Globals.currentStep).unselectAll();
		Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();
		stepChanged();
	}
	
	/**
	 * Creates a new {@code Step}
	 */
	public void createNewStep() {
		if (Globals.newStepOptions == Constants.NewStepOptions.COPY_LAST_STEP) {
			//new step that is a copy of last step
			createStepLastCopy(Globals.currentStep);
		} else if (Globals.newStepOptions == Constants.NewStepOptions.PASTE_DEFAULT_PAPER) {
			//new step with previously chosen paper shape
			createStepPaperShape(Globals.currentStep);
		} else if (Globals.newStepOptions == Constants.NewStepOptions.EMPTY_STEP) {
			//new empty step
			createEmptyStep(Globals.currentStep);
		}
	}
	
	/**
	 * Creates a {@code Step} at {@code stepNumber} position in the diagram
	 * @param stepNumber the position within the diagram
	 */
	public void createStepAfter(int stepNumber) {
		Globals.currentStep += 1;
		
		//if currentStep == last step in diagram --> create new step
		if (Globals.newStepOptions == Constants.NewStepOptions.COPY_LAST_STEP) {
			//new step that is a copy of last step
			createStepLastCopy(stepNumber);
		} else if (Globals.newStepOptions == Constants.NewStepOptions.PASTE_DEFAULT_PAPER) {
			//new step with previously chosen paper shape
			createStepPaperShape(stepNumber);
		} else if (Globals.newStepOptions == Constants.NewStepOptions.EMPTY_STEP) {
			//new empty step
			createEmptyStep(stepNumber);
		}
		Origrammer.diagram.steps.get(Globals.currentStep).unselectAll();
		Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();
		stepChanged();
	}
	
	/**
	 * Creates a new folding {@code Step} that is an exact copy of the previous {@code Step}
	 */
	public void createStepLastCopy(int stepNumber) {
		int prevStep = Globals.currentStep-1;
		Step newStep = new Step();

		for (int i = 0; i<Origrammer.diagram.steps.get(prevStep).polygons.size(); i++) {
			OriPolygon tmpPolygon = new OriPolygon(Origrammer.diagram.steps.get(prevStep).polygons.get(i));
			newStep.polygons.add(tmpPolygon);
		}
		
		HashMap<OriLine, ArrayList<OriPolygon>> tmpSharedLinesList = new HashMap<OriLine, ArrayList<OriPolygon>>();
		for (Map.Entry<OriLine, ArrayList<OriPolygon>> entry : Origrammer.diagram.steps.get(prevStep).sharedLines.entrySet()) {
			OriLine oldKey = entry.getKey();
			ArrayList<OriPolygon> oldValueList = entry.getValue();
			
			OriLine newKey = new OriLine(oldKey);
			ArrayList<OriPolygon> newValueList = new ArrayList<OriPolygon>();

			for (OriPolygon p : newStep.polygons) {
				if (p.vertexList.head.p.epsilonEquals(oldValueList.get(0).vertexList.head.p, Constants.EPSILON)
						&& p.vertexList.head.next.p.epsilonEquals(oldValueList.get(0).vertexList.head.next.p, Constants.EPSILON)
						&& p.vertexList.head.next.next.p.epsilonEquals(oldValueList.get(0).vertexList.head.next.next.p, Constants.EPSILON)) {
					newValueList.add(0, p);
				} else if (p.vertexList.head.p.epsilonEquals(oldValueList.get(1).vertexList.head.p, Constants.EPSILON)
						&& p.vertexList.head.next.p.epsilonEquals(oldValueList.get(1).vertexList.head.next.p, Constants.EPSILON)
						&& p.vertexList.head.next.next.p.epsilonEquals(oldValueList.get(1).vertexList.head.next.next.p, Constants.EPSILON)) {
					newValueList.add(1, p);
				}
			}
			
			tmpSharedLinesList.put(newKey, newValueList);
		}
		newStep.sharedLines = tmpSharedLinesList;

		
		for (int i = 0; i < Origrammer.diagram.steps.get(prevStep).lines.size(); i++) {
			OriLine tmpLine = new OriLine(Origrammer.diagram.steps.get(prevStep).lines.get(i));
			newStep.lines.add(tmpLine);
		}
		
		for (int i = 0; i < Origrammer.diagram.steps.get(prevStep).edgeLines.size(); i++) {
			OriLine tmpLine = new OriLine(Origrammer.diagram.steps.get(prevStep).edgeLines.get(i));
			newStep.edgeLines.add(tmpLine);
		}

		for (int i = 0; i < Origrammer.diagram.steps.get(prevStep).vertices.size(); i++) {
			OriVertex tmpVertex = new OriVertex(Origrammer.diagram.steps.get(prevStep).vertices.get(i));
			newStep.vertices.add(tmpVertex);
		}		

		for (int i = 0; i < Origrammer.diagram.steps.get(prevStep).arrows.size(); i++) {
			OriArrow tmpArrow = new OriArrow(Origrammer.diagram.steps.get(prevStep).arrows.get(i));
			newStep.arrows.add(tmpArrow);
		}

		for (int i = 0; i < Origrammer.diagram.steps.get(prevStep).filledFaces.size(); i++) {
			OriFace tmpFilledFace = new OriFace(Origrammer.diagram.steps.get(prevStep).filledFaces.get(i));
			newStep.filledFaces.add(tmpFilledFace);
		}

		for (int i = 0; i < Origrammer.diagram.steps.get(prevStep).geomSymbols.size(); i++) {
			OriGeomSymbol tmpGeomSymbol = new OriGeomSymbol(Origrammer.diagram.steps.get(prevStep).geomSymbols.get(i));
			newStep.geomSymbols.add(tmpGeomSymbol);
		}

		for (int i = 0; i < Origrammer.diagram.steps.get(prevStep).equalDistSymbols.size(); i++) {
			OriEqualDistSymbol tmpEqualDistSymbol = new OriEqualDistSymbol(Origrammer.diagram.steps.get(prevStep).equalDistSymbols.get(i));
			newStep.equalDistSymbols.add(tmpEqualDistSymbol);
		}

		for (int i = 0; i < Origrammer.diagram.steps.get(prevStep).equalAnglSymbols.size(); i++) {
			OriEqualAnglSymbol tmpEqualAnglSymbol = new OriEqualAnglSymbol(Origrammer.diagram.steps.get(prevStep).equalAnglSymbols.get(i));
			newStep.equalAnglSymbols.add(tmpEqualAnglSymbol);
		}

		for (int i = 0; i < Origrammer.diagram.steps.get(prevStep).pleatCrimpSymbols.size(); i++) {
			OriPleatCrimpSymbol tmpPleatCrimpSymbol = new OriPleatCrimpSymbol(Origrammer.diagram.steps.get(prevStep).pleatCrimpSymbols.get(i));
			newStep.pleatCrimpSymbols.add(tmpPleatCrimpSymbol);
		}
		
		Origrammer.diagram.steps.add(stepNumber, newStep);
		Origrammer.mainFrame.uiStepOverviewPanel.createStepPreview();
	}
	
	/**
	 * Creates a new {@code Step} with only the basic 
	 * paper shape that was being set on file creation
	 */
	public void createStepPaperShape(int stepNumber) {
		Step newStep = new Step();
		
		Origrammer.diagram.steps.add(stepNumber, newStep);
		Origrammer.mainFrame.uiStepOverviewPanel.createStepPreview();
	}
	
	/**
	 * Creates a new {@code Step} without any lines, arrows, or symbols
	 */
	private void createEmptyStep(int stepNumber) {
		Step newStep = new Step();
		newStep.lines.clear();
		Origrammer.diagram.steps.add(stepNumber, newStep);
		Origrammer.mainFrame.uiStepOverviewPanel.createStepPreview();
	}
	
	private void stepBack() {
		if (Globals.currentStep >= 1) {
			Globals.currentStep -= 1;
		}
		Origrammer.diagram.steps.get(Globals.currentStep).unselectAll();
		stepChanged();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == stepBack) {
			stepBack();
		} else if (e.getSource() == stepForth) {
			stepForth();
		} else if (e.getSource() == newCopiedStep) {
			System.out.println("COPY");
			Globals.newStepOptions = Constants.NewStepOptions.COPY_LAST_STEP;
		} else if (e.getSource() == newBaseShapeStep) {
			System.out.println("BASE SHAPE");
			Globals.newStepOptions = Constants.NewStepOptions.PASTE_DEFAULT_PAPER;
		} else if (e.getSource() == newEmptyStep) {
			System.out.println("EMPTY");
			Globals.newStepOptions = Constants.NewStepOptions.EMPTY_STEP;
		}
	}


	public void stepChanged() {
		if (Origrammer.diagram.steps.get(Globals.currentStep).stepDescription == null) {
			foldDescr.setValue("");
		} else {
			foldDescr.setValue(Origrammer.diagram.steps.get(Globals.currentStep).stepDescription);
		}

		if (Origrammer.diagram.steps.size() == Globals.currentStep + 1) {
			//newStepPanel.setVisible(true);
			newEmptyStep.setEnabled(true);
			newCopiedStep.setEnabled(true);
			newBaseShapeStep.setEnabled(true);
		} else {
			//newStepPanel.setVisible(false);
			newEmptyStep.setEnabled(false);
			newCopiedStep.setEnabled(false);
			newBaseShapeStep.setEnabled(false);
		}
		currentStepTF.setValue(Globals.currentStep);
		screen.modeChanged();
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
