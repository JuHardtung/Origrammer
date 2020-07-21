package origrammer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
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

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class UIStepOverviewPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener, PropertyChangeListener, KeyListener {

	
	private ArrayList<StepPreview> stepPreviewList = new ArrayList<>();
	private JPanel stepOverviewPanel = new JPanel();
	private JScrollPane scrollPane = new JScrollPane(stepOverviewPanel);
	private AffineTransform affineTransform = new AffineTransform();


	
	
	public UIStepOverviewPanel() {
		stepOverviewPanel.addMouseListener(this);
		stepOverviewPanel.addMouseMotionListener(this);
		setPreferredSize(new Dimension(250, 700));
		setBackground(new Color(230, 230, 230));
		stepOverviewPanel.setLayout(new BoxLayout(stepOverviewPanel, BoxLayout.PAGE_AXIS));
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(250, 700));
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
	}
	
	
	public void updateStepOverViewPanel() {
		removeAll();
		
		int steps = stepPreviewList.size();
		if (stepPreviewList.size() <= Globals.currentStep) {
			StepPreview stepPreview = new StepPreview(Globals.currentStep);
			


			BufferedImage bimage = new BufferedImage(Constants.MAINSCREEN_SIZE.width, 
					Constants.MAINSCREEN_SIZE.height, BufferedImage.TYPE_BYTE_INDEXED);
			Graphics2D graphics = bimage.createGraphics();
			
			Globals.renderStepPreview = true;
			Origrammer.mainFrame.mainScreen.paint(graphics);
			Globals.renderStepPreview = false;

			ImageIcon icon = new ImageIcon(rescaleBufImage(bimage, 0.25));
			stepPreview.setStepImageIcon(icon); //SET_IMAGE
			stepPreview.setStepImageBounds(0, 25+steps*175, 100, 100);

			
			stepPreview.setStepDescrBounds(0, 125+steps*175, 100, 25);
			stepPreview.setStepDescrText(Origrammer.diagram.steps.get(Globals.currentStep).stepDescription); //SET_INSTRUCTION
			
			stepPreviewList.add(stepPreview);
			
		} else {
			stepPreviewList.get(Globals.currentStep).setStepNumber(Globals.currentStep);
			
			BufferedImage bimage = new BufferedImage(Constants.MAINSCREEN_SIZE.width, 
					Constants.MAINSCREEN_SIZE.height, BufferedImage.TYPE_BYTE_INDEXED);
			Graphics2D graphics = bimage.createGraphics();
			Globals.renderStepPreview = true;
			Origrammer.mainFrame.mainScreen.paint(graphics);
			Globals.renderStepPreview = false;

			
			ImageIcon icon = new ImageIcon(rescaleBufImage(bimage, 0.25));
			
			stepPreviewList.get(Globals.currentStep).setStepImageIcon(icon);
			stepPreviewList.get(Globals.currentStep).setStepImageBounds(0, 25+steps*175, 100, 100);
			
			stepPreviewList.get(Globals.currentStep).setStepDescrBounds(0, 125+steps*175, 100, 25);
			stepPreviewList.get(Globals.currentStep).setStepDescrText(Origrammer.diagram.steps.get(Globals.currentStep).stepDescription);
			
		}
		
		for (StepPreview p : stepPreviewList) {
			stepOverviewPanel.add(p);
		}

		add(scrollPane);

		revalidate();
		repaint();
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
		if (SwingUtilities.isRightMouseButton(e)) {
			for(StepPreview sp: stepPreviewList) {
				sp.setSelected(false);
			}
		} else {
			//check all stepPreviews for on click and react accordingly
			for (StepPreview sp : stepPreviewList) {
				if (sp.getBounds().intersects(clickPoint.x-2.5, clickPoint.y+2.5, 5, 5)) {
					if (!sp.isSelected()) {
						sp.setSelected(true);
					} else {
						sp.setSelected(false);
					}
				} else {
					sp.setSelected(false);
				}
			}
		}
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
		repaint();
	}	
	
	
	
	

	@Override
	public void keyPressed(KeyEvent e) {		
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
	public void actionPerformed(ActionEvent e) {		
	}





	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}	

}
