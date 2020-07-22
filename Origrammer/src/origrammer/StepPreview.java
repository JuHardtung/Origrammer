package origrammer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class StepPreview extends JPanel {
	
	private int stepNumber;
	private JLabel stepImage = new JLabel();
	private JLabel stepDescr = new JLabel();
	private boolean isSelected;
	
	private TitledBorder titledBorder;
	private EtchedBorder etchedBorder = new EtchedBorder(BevelBorder.RAISED, getBackground().darker(), getBackground().brighter());
	private EtchedBorder etchedBorderHovered = new EtchedBorder(BevelBorder.RAISED, Color.GRAY.darker(), getBackground().brighter());
	private EtchedBorder etchedBorderGreen = new EtchedBorder(BevelBorder.RAISED, getBackground().darker(), Color.GREEN);

	public StepPreview (int stepNumber) {
		this.stepNumber = stepNumber;
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		titledBorder = new TitledBorder(etchedBorder, "Step " + String.valueOf(stepNumber));
		setBorder(titledBorder);
		stepImage.setAlignmentX(Component.CENTER_ALIGNMENT);
		stepDescr.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(stepImage);
		add(stepDescr);
	}
	

	public int getStepNumber() {
		return stepNumber;
	}

	public void setStepNumber(int stepNumber) {
		this.stepNumber = stepNumber;
		titledBorder.setTitle("Step " + String.valueOf(stepNumber));
	}

	public Icon getStepImageIcon() {
		return stepImage.getIcon();
	}
	
	public void setStepImageIcon(Icon image) {
		stepImage.setIcon(image);
	}
	
	public void setStepImageBounds(int x, int y, int w, int h) {
		stepImage.setBounds(x, y, w, h);
	}
	
	public JLabel getStepImage() {
		return stepImage;
	}

	public void setStepImage(JLabel stepImage) {
		this.stepImage = stepImage;
	}

	public String getStepDescrText() {
		return stepDescr.getText();
	}
	
	public void setStepDescrText(String text) {
		stepDescr.setText(text);
	}
	
	public void setStepDescrBounds(int x, int y, int w, int h) {
		stepDescr.setBounds(x, y, w, h);
	}
	
	public JLabel getStepDescr() {
		return stepDescr;
	}

	public void setStepDescr(JLabel stepDescr) {
		this.stepDescr = stepDescr;
	}

	public boolean isSelected() {
		return isSelected;
	}


	public void setSelected(boolean isSelected) {
		if (isSelected) {
			titledBorder.setBorder(etchedBorderGreen);
		} else {
			titledBorder.setBorder(etchedBorder);
		}
		setBorder(titledBorder);
		this.isSelected = isSelected;
		repaint();
	}
	
	public void setGreenBorder() {
		if (!isSelected) {
			titledBorder.setBorder(etchedBorderGreen);
			titledBorder.setTitle("Step " + String.valueOf(stepNumber));
		}
		setBorder(titledBorder);
		repaint();
	}
	
	public void setHoveredBorder() {
		titledBorder.setBorder(etchedBorderHovered);
		titledBorder.setTitle("Step " + String.valueOf(stepNumber));
		setBorder(titledBorder);
		repaint();
	}
	
	public void setGreyBorder() {
		if (!isSelected) {
			titledBorder.setBorder(etchedBorder);
			titledBorder.setTitle("Step " + String.valueOf(stepNumber));
		}
		setBorder(titledBorder);
		repaint();
	}

	@Override
	public String toString() {
		return "StepPreview [stepNumber=" + stepNumber
			+ " | stepImage=" + stepImage.getBounds() + ", " + stepImage.getIcon() + " | stepDescr=" 
				+ stepDescr.getBounds() +  ", " + stepDescr.getText() + " | isSelected=" + isSelected + "]";
	}

}
