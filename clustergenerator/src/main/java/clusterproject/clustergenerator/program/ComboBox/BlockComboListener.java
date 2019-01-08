package clusterproject.clustergenerator.program.ComboBox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

public class BlockComboListener implements ActionListener {
	JComboBox combo;

	Object currentItem;

	public BlockComboListener(JComboBox combo) {
		this.combo = combo;
		combo.setSelectedIndex(0);
		currentItem = combo.getSelectedItem();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final String tempItem = (String) combo.getSelectedItem();
		if (ComboBoxRenderer.SEPARATOR.equals(tempItem)) {
			combo.setSelectedItem(currentItem);
		} else {
			currentItem = tempItem;
		}
	}
}
