package clusterproject.clustergenerator.userInterface.Generator.Panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

public class CSVOptions extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final JFileChooser fileChooser;
	private JLabel selectedLable;
	private final JCheckBox addBox;
	private File selectedFile;

	public CSVOptions() {
		setVisible(false);
		setOpaque(false);
		final BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);
		fileChooser = new JFileChooser();
		fileChooser.setBorder(null);
		fileChooser.setFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return "CSV File";
			}

			@Override
			public boolean accept(File f) {
				return f.getName().endsWith("csv");
			}
		});
		add(fileChooser);
		fileChooser.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				selectedFile = fileChooser.getSelectedFile();
				selectedLable.setText(selectedFile.getName());
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						selectedLable.repaint();

					}
				});
			}
		});
		selectedLable = new JLabel("nothing Selected");
		add(selectedLable);
		addBox = new JCheckBox("Add");
		add(addBox);

	}

	public File getFile() {
		return selectedFile;
	}

	public boolean replacePoints() {
		return !addBox.isSelected();
	}

}
