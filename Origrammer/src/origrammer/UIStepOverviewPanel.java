package origrammer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;


/**
 * The {@code UIStepOverviewPanel} shows an overview over all current steps within the opened diagram.
 * Every step is being shown as a StepPreview, consisting of {@code stepNumber}, 
 * a small preview picture {@code stepImage},
 * and the {@code stepDescription}
 * @author Julian-Tower
 *
 */
public class UIStepOverviewPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener, PropertyChangeListener, KeyListener {

	private ArrayList<StepPreview> stepPreviewList = new ArrayList<>();
	private ScrollablePanel stepOverviewPanel = new ScrollablePanel();
	private JScrollPane scrollPane = new JScrollPane(stepOverviewPanel);
	private AffineTransform affineTransform = new AffineTransform();
	
	private JPanel stepPreviewOptionsPanel = new JPanel();
	private JButton deleteStepPreviewButton = new JButton("Delete");
	private JButton moveStepPreviewUpButton = new JButton("Up");
	private JButton moveStepPreviewDownButton = new JButton("Down");
	private JButton createStepInBetween = new JButton("New");

	final static String MOVE_STEP_UP = "move-step-up";
	final static String MOVE_STEP_DOWN = "move-step-down";

	
	/**
	 * The {@code UIStepOverviewPanel} shows an overview over all current steps within the opened diagram.
	 * Every step is being shown as a StepPreview, consisting of {@code stepNumber}, 
	 * a small preview picture {@code stepImage},
	 * and the {@code stepDescription}
	 */
	public UIStepOverviewPanel() {
		stepOverviewPanel.addMouseListener(this);
		stepOverviewPanel.addMouseMotionListener(this);

		//MOVE STEP UP
		InputMap imMovingUp = stepOverviewPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		imMovingUp.put(KeyStroke.getKeyStroke("UP"), MOVE_STEP_UP);
		ActionMap amMovingUp = stepOverviewPanel.getActionMap();
		amMovingUp.put(MOVE_STEP_UP, new MoveStepUpAction());
		
		//MOVE STEP DOWN
		InputMap imMovingDown = stepOverviewPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		imMovingDown.put(KeyStroke.getKeyStroke("DOWN"), MOVE_STEP_DOWN);
		ActionMap amMovingDown = stepOverviewPanel.getActionMap();
		amMovingDown.put(MOVE_STEP_DOWN, new MoveStepDownAction());
		
		setPreferredSize(new Dimension(250, 700));
		setBackground(new Color(230, 230, 230));
		stepOverviewPanel.setLayout(new BoxLayout(stepOverviewPanel, BoxLayout.PAGE_AXIS));
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(250, 700));
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		
		addStepPreviewOptionsPanel();
		add(scrollPane);
	}
	
	private void addStepPreviewOptionsPanel() {
		deleteStepPreviewButton.addActionListener(this);
		moveStepPreviewUpButton.addActionListener(this);
		moveStepPreviewDownButton.addActionListener(this);
		createStepInBetween.addActionListener(this);

		stepPreviewOptionsPanel.add(deleteStepPreviewButton);
		stepPreviewOptionsPanel.add(moveStepPreviewUpButton);
		stepPreviewOptionsPanel.add(moveStepPreviewDownButton);
		stepPreviewOptionsPanel.add(createStepInBetween);
		add(stepPreviewOptionsPanel);
	}
	
	/**
	 * Creates a new {@code StepPreview} with {@code stepNumber}, 
	 * {@code stepImageIcon}, and {@code stepDescr}
	 */
	public void createStepPreview() {
		StepPreview stepPreview = new StepPreview(Globals.currentStep);

		//render a stepPreview into bimg, rescale from 800x800 -> 200x200 and use as ImageIcon
		BufferedImage bimg = new BufferedImage(Constants.MAINSCREEN_SIZE.width, 
				Constants.MAINSCREEN_SIZE.height, BufferedImage.TYPE_BYTE_INDEXED);
		Graphics2D graphics = bimg.createGraphics();
		Globals.renderStepPreview = true;
		Origrammer.mainFrame.mainScreen.paint(graphics);
		Globals.renderStepPreview = false;

		Icon icon = new ImageIcon(rescaleBufImage(bimg, 0.25));
		stepPreview.setStepImageIcon(icon); //SET_IMAGE
		stepPreview.setStepDescrText(Origrammer.diagram.steps.get(Globals.currentStep).stepDescription); //SET_INSTRUCTION
		
		stepPreviewList.add(Globals.currentStep, stepPreview);
	}
	
	/**
	 * Updates a {@code StepPreview}
	 */
	private void updateStepPreview() {		
		BufferedImage bimage = new BufferedImage(Constants.MAINSCREEN_SIZE.width, 
				Constants.MAINSCREEN_SIZE.height, BufferedImage.TYPE_BYTE_INDEXED);
		Graphics2D graphics = bimage.createGraphics();
		Globals.renderStepPreview = true;
		Origrammer.mainFrame.mainScreen.paint(graphics);
		Globals.renderStepPreview = false;

		ImageIcon icon = new ImageIcon(rescaleBufImage(bimage, 0.25));
		
		stepPreviewList.get(Globals.currentStep).setStepImageIcon(icon);
		stepPreviewList.get(Globals.currentStep).setStepDescrText(Origrammer.diagram.steps.get(Globals.currentStep).stepDescription);
	}
	
	/**
	 * Keeps the {@code StepOverviewPanel} up-to-date.
	 * Creates a new {@code Step} if needed or just updates everything
	 */
	public void updateStepOverViewPanel() {
		if (stepPreviewList.size() <= Globals.currentStep) {
			createStepPreview();
		} else {
			updateStepPreview();
		}
		
		addAllStepPreviews();
		revalidate();
		repaint();
//		System.out.println("After: " + stepPreviewList.size());
//		for (StepPreview p : stepPreviewList) {
//			System.out.println(p.getStepNumber() + " | " + p.getStepDescrText());
//		}
//		System.out.println("____________________________");

	}
	
	/**
	 * Rescales {@code bimg} by 0.25 from 800x800 to 200x200
	 * @param bimg
	 * @return
	 */
	private BufferedImage rescaleBufImage(BufferedImage bimg, double scaleFactor) {
		//rescale source bimage to after
		int w = (int) Math.round(bimg.getWidth()*scaleFactor);
		int h = (int) Math.round(bimg.getHeight()*scaleFactor);
		BufferedImage scaledBimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.scale(scaleFactor, scaleFactor);
		AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		scaledBimg = scaleOp.filter(bimg, scaledBimg);
		return scaledBimg;
	}
	
	/**
	 * Removes all {@code stepPreviewList}-entries and removes them from the {@code stepOverviewPanel}
	 */
	public void removeAllStepPreviews() {
		for (StepPreview sp : stepPreviewList) {
			stepOverviewPanel.remove(sp);
		}
		
		stepPreviewList.clear();
	}
	
	private void addAllStepPreviews() {
		for (StepPreview sp : stepPreviewList) {
			stepOverviewPanel.add(sp);
		}
	}
	
	/**
	 * Moves a {@code StepPreview} up in the {@code stepPreviewList} and
	 * moves the corresponding {@code Step} up in the {@code Origrammer.diagram.steps}-list
	 */
	private void moveStepUp() {
		StepPreview tmpSP = null;
		for (StepPreview sp : stepPreviewList) {
			if (sp.isSelected()) {
				tmpSP = sp;
				break;
			}
		}

		if (tmpSP != null) {
			int currrentStepNum = tmpSP.getStepNumber();
			if (currrentStepNum > 0) {

				//remove all StepPreviewsPanels so they can be added back in right order
				removeAllStepPreviews(); 
				//remove stepPreview from list
				stepPreviewList.remove(currrentStepNum); 
				//put the removed stepPreview back in the list on the right spot
				stepPreviewList.add((currrentStepNum-1), tmpSP); 

				//corresponding stepNumbers of stepPreview have to be corrected
				correctAllStepPreviewStepNumbers();
				//add back all stepPreviewPanels in the right order
				addAllStepPreviews();
				//correct the order of the Steps themselves
				Origrammer.diagram.moveStepUp(currrentStepNum);

				Globals.currentStep -=1;
				stepChanged();
			} else {
				System.out.println("Can't move step further up, it's already the last.");
			}
		} else {
			System.out.println("Can't move the step down, as no step was selected");
		}
	}
	
	/**
	 * Moves a {@code StepPreview} down in the {@code stepPreviewList} and
	 * moves the corresponding {@code Step} down in the {@code Origrammer.diagram.steps}-list
	 */
	private void moveStepDown() {
		StepPreview tmpSP = null;
		for (StepPreview sp : stepPreviewList) {
			if (sp.isSelected()) {
				tmpSP = sp;
				break;
			}
		}
		if (tmpSP != null) {
			int currrentStepNum = tmpSP.getStepNumber();

			if (tmpSP.getStepNumber() < (stepPreviewList.size()-1)) {
				//remove all StepPreviewsPanels so they can be added back in right order
				removeAllStepPreviews(); 
				//remove stepPreview from list
				stepPreviewList.remove(currrentStepNum); 
				//put the removed stepPreview back in the list on the right spot
				stepPreviewList.add((currrentStepNum+1), tmpSP); 

				//corresponding stepNumbers of stepPreview have to be corrected
				correctAllStepPreviewStepNumbers();
				//add back all stepPreviewPanels in the right order
				addAllStepPreviews();
				//correct the order of the Steps themselves
				Origrammer.diagram.moveStepDown(currrentStepNum);

				Globals.currentStep +=1;
				stepChanged();
			} else {
				System.out.println("Can't move step further down, it's already the last.");
			}
		} else {
			System.out.println("Can't move the step down, as no step was selected");
		}
	}
	
	/**
	 * Creates a new {@code Step} after the currently active one
	 */
	private void createStepAfterCurrentStep() {
		if (!(Globals.currentStep == Origrammer.diagram.steps.size()-1)) {
			Origrammer.mainFrame.uiBottomPanel.createStepAfter(Globals.currentStep+1);
			correctAllStepPreviewStepNumbers();
		} else {
			Globals.currentStep += 1;
			Origrammer.mainFrame.uiBottomPanel.createNewStep();
			Origrammer.diagram.steps.get(Globals.currentStep).unselectAll();
			Origrammer.mainFrame.uiStepOverviewPanel.updateStepOverViewPanel();
			stepChanged();
		}
	}
	
	private void deleteSelectedStepPreview() {
		for (int i=0; i<stepPreviewList.size(); i++) {
			if (stepPreviewList.get(i).isSelected()) {
				stepOverviewPanel.remove(stepPreviewList.get(i));
				stepPreviewList.remove(i);
				Origrammer.diagram.deleteStep(i);
			}
		}
		correctAllStepPreviewStepNumbers();

		addAllStepPreviews();
		stepChanged();
	}
	
	/**
	 * Clears the {@code stepPreviewList} when creating a new Diagram
	 */
	public void clearStepPreviews() {
		stepPreviewList.clear();
		
	}
	
	public void stepChanged() {
		revalidate();
		repaint();
		Origrammer.mainFrame.uiBottomPanel.stepChanged();
	}

	/**
	 * Corrects the {@code stepNumber} of all {@code StepPreviews} after moving a step up or down
	 */
	private void correctAllStepPreviewStepNumbers() {
		for (int i = 0; i<stepPreviewList.size(); i++) {
			stepPreviewList.get(i).setStepNumber(i);
		}
	}

	
	@Override
	public void mouseClicked(MouseEvent e) {
		//get mouse click coordinates
		Point2D.Double clickPoint = new Point2D.Double();
		try {
			affineTransform.inverseTransform(e.getPoint(), clickPoint);
		} catch (Exception ex) {
			return;
		}
		
		//if right clicked on the stepPreviewPanel --> unselect everything
		/*if (SwingUtilities.isRightMouseButton(e)) {
			for(StepPreview sp: stepPreviewList) {
				sp.setSelected(false);
			}
		} else {*/
			//check all stepPreviews for on click and react accordingly
			for (StepPreview sp : stepPreviewList) {
				if (sp.getBounds().intersects(clickPoint.x-2.5, clickPoint.y+2.5, 5, 5)) {
					if (!sp.isSelected()) {
						sp.setSelected(true);
						Globals.currentStep = sp.getStepNumber();
						stepChanged();
					} else {
						sp.setSelected(false);
					}
				} else {
					sp.setSelected(false);
				}
			}
		//}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {

		Point2D.Double mousePos = new Point2D.Double();
		try {
			affineTransform.inverseTransform(e.getPoint(), mousePos);
		} catch (Exception ex) {
			return;
		}
		//check if mouse is moved over one of the stepPreviews
		// if so and if it isn't already selected --> change to hoveredBorder 
		for (StepPreview sp : stepPreviewList) {
			if (sp.getBounds().intersects(mousePos.x-2.5, mousePos.y+2.5, 5, 5)) {
				if (!sp.isSelected()) {
					sp.setHoveredBorder();
				}
			} else {
				if (!sp.isSelected()) {
					sp.setGreyBorder();
				}
			}
		}
		revalidate();
		repaint();
	}
	
	
	private class MoveStepUpAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			moveStepUp();
		}
	}
	
	private class MoveStepDownAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			moveStepDown();
		}
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == deleteStepPreviewButton) {
			deleteSelectedStepPreview();
		} else if (e.getSource() == moveStepPreviewUpButton) {
			moveStepUp();
		} else if (e.getSource() == moveStepPreviewDownButton) {
			moveStepDown();
		} else if (e.getSource() == createStepInBetween) {
			createStepAfterCurrentStep();
		}
	}
	
	
	private static class ScrollablePanel extends JPanel implements Scrollable{
        public Dimension getPreferredScrollableViewportSize() {
            return super.getPreferredSize();
        }
 
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }
 
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }
 
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }
 
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
	

	@Override
	public void keyReleased(KeyEvent e) {		
	}
	@Override
	public void keyTyped(KeyEvent e) {		
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {		
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {
	}
	@Override
	public void mouseExited(MouseEvent e) {		
	}
	@Override
	public void mousePressed(MouseEvent e) {		
	}
	@Override
	public void mouseReleased(MouseEvent e) {		
	}
	@Override
	public void mouseDragged(MouseEvent arg0) {		
	}
	@Override
	public void keyPressed(KeyEvent arg0) {		
	}	

}
