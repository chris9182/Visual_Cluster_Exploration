package other.diclens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;

//from https://www.cs.umb.edu/~smimarog/diclens/
//modified
public class DiclensGUIView {
	private static final int Y_SIZE = 700;
	private static final int X_SIZE = 1000;
	private static final long serialVersionUID = 4638374634962191043L;
	private static final String FILENAME = "data/paper-example.txt";
	private Container contentPane;
	JTextField filenameText;
	private final JFrame frame;
	JButton runAlgorithmButton;
	private JPanel smstPanel;
	JButton browseBt;
	private JPanel metaClusterPanel;
	private JTextArea longOutput;
	JPanel vennDiagram;
	private JTextArea shortOutput;

	public DiclensGUIView() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e) {
			e.printStackTrace();
		}
		this.frame = new JFrame("DiCLENS");
		(this.contentPane = this.frame.getContentPane()).setLayout(new BorderLayout());
		final JPanel middlePane = new JPanel();
		middlePane.setLayout(new GridLayout(3, 1));
		this.metaClusterPanel = this.generateAGraphPanel();
		middlePane.add(this.generateSmstPanel());
		middlePane.add(this.generateMetaClusterPanel());
		middlePane.add(this.generateOutputPane());
		this.contentPane.add(this.generateTopPane(), "First");
		this.contentPane.add(middlePane, "Center");
		this.frame.setSize(1000, 700);
		this.frame.setDefaultCloseOperation(3);
		this.setVisible();
	}

	private JPanel generateSmstPanel() {
		this.smstPanel = this.generateAGraphPanel();
		return this.addTitleToPanel("Similarity Based Minumum Spanning Tree (SMST)", this.smstPanel);
	}

	private JPanel generateMetaClusterPanel() {
		this.metaClusterPanel = this.generateAGraphPanel();
		return this.addTitleToPanel("Meta Clusters", this.metaClusterPanel);
	}

	private JPanel addTitleToPanel(final String title, final JPanel thePanel) {
		final JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(String.valueOf(title) + ":"), "First");
		panel.add(thePanel, "Center");
		return panel;
	}

	private JPanel generateOutputPane() {
		final JPanel outPanel = new JPanel();
		outPanel.setLayout(new GridLayout(1, 2));
		this.vennDiagram = new JPanel(new GridLayout(1, 1));
		outPanel.add(new JScrollPane(this.vennDiagram));
		this.longOutput = new JTextArea();
		this.shortOutput = new JTextArea();
		final JPanel textOutPanel = new JPanel(new GridLayout(2, 1));
		textOutPanel.add(new JScrollPane(this.longOutput));
		textOutPanel.add(new JScrollPane(this.shortOutput));
		outPanel.add(textOutPanel);
		return this.addTitleToPanel("Final Clusters", outPanel);
	}

	void setVisible() {
		this.frame.setVisible(true);
	}

	private JPanel generateTopPane() {
		final JPanel topPane = new JPanel();
		topPane.add(this.generateSourceFilePanel());
		topPane.add(this.runAlgorithmButton = new JButton("Run Algorithm"));
		topPane.setMaximumSize(new Dimension(1000, 80));
		return topPane;
	}

	private JPanel generateAGraphPanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setAlignmentX(0.0f);
		panel.setAlignmentY(0.5f);
		panel.setBorder(BorderFactory.createLineBorder(Color.black));
		return panel;
	}

	void setSmstGraphViewer(final VisualizationViewer<DiclensMST.Cluster, DiclensMST.Edge> vv) {
		this.setGraphViewer(vv, this.smstPanel);
	}

	void setMetaClusterGraphViewer(final VisualizationViewer<DiclensMST.Cluster, DiclensMST.Edge> vv) {
		this.setGraphViewer(vv, this.metaClusterPanel);
	}

	private void setGraphViewer(final VisualizationViewer<DiclensMST.Cluster, DiclensMST.Edge> vv, final JPanel panel) {
		final GraphZoomScrollPane scroll = new GraphZoomScrollPane(vv);
		panel.removeAll();
		panel.add(scroll, "Center");
		panel.validate();
	}

	private JPanel generateSourceFilePanel() {
		final JLabel jlbFilename = new JLabel("Source file:");
		this.filenameText = new JTextField("data/paper-example.txt", 30);
		final JPanel sourceFilePanel = new JPanel();
		sourceFilePanel.setLayout(new BoxLayout(sourceFilePanel, 0));
		this.browseBt = new JButton("Browse File");
		sourceFilePanel.add(jlbFilename);
		sourceFilePanel.add(this.filenameText);
		sourceFilePanel.add(this.browseBt);
		return sourceFilePanel;
	}

	public void setLongOutputTo(final String text) {
		this.longOutput.setText(text);
	}

	public void draw(final JPanel drawing) {
		this.vennDiagram.removeAll();
		this.vennDiagram.add(drawing);
		this.vennDiagram.validate();
	}

	public void setShortOutputTo(final String text) {
		this.shortOutput.setText(text);
	}
}
