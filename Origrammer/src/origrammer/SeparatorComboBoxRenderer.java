package origrammer;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

//source: http://esus.com/creating-a-jcombobox-with-a-divider-separator-line/
public class SeparatorComboBoxRenderer extends BasicComboBoxRenderer implements ListCellRenderer {
	public SeparatorComboBoxRenderer() {
		super();
	}

	public Component getListCellRendererComponent(JList list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		lbl.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		}
		else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		setFont(list.getFont());
		if (value instanceof Icon) {
			setIcon((Icon)value);
		}
		if (value instanceof JSeparator) {
			return (Component) value;
		}
		else {
			setText((value == null) ? "" : value.toString());
		}

		return lbl;
	} 
}
