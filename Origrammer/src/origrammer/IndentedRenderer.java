package origrammer;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

//Indents JComboBox entries
public class IndentedRenderer extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(JList list, Object value,
			int index,boolean isSelected,boolean cellHasFocus) {
		JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		lbl.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		return lbl;
	}
}
