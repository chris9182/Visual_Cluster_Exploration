package clusterproject.clustergenerator.userInterface.ComboBox;

import java.awt.Component;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

//based on: http://www.java2s.com/Code/Java/Swing-Components/BlockComboBoxExample.htm
public class ComboBoxRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 1L;
	public static final String SEPARATOR = "SEPARATOR";
	JSeparator separator;

	public ComboBoxRenderer() {
		setOpaque(true);
		setBorder(new EmptyBorder(1, 1, 1, 1));
		separator = new JSeparator(JSeparator.HORIZONTAL);
	}

	public static Vector<String> makeVectorData(String[][] str) {
		boolean needSeparator = false;
		final Vector<String> data = new Vector<String>();
		for (int i = 0; i < str.length; i++) {
			if (str[i] == null || str[i].length < 1)
				continue;
			if (needSeparator) {
				data.addElement(SEPARATOR);
			}
			for (int j = 0; j < str[i].length; j++) {
				data.addElement(str[i][j]);
				needSeparator = true;
			}
		}
		return data;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		final String str = (value == null) ? "" : value.toString();
		if (SEPARATOR.equals(str)) {
			return separator;
		}
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		setFont(list.getFont());
		setText(str);
		return this;
	}
}
