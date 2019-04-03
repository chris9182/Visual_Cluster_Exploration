package clusterproject;

import java.awt.Dimension;

import javax.swing.JFrame;

import clusterproject.program.MainWindow;

public class App {
	public static void main(String[] args) {
		// TODO: could be looked at for graph partitioning
		// org.apache.giraph.partition.GraphPartitionerFactory<WritableComparable,
		// Writable, Writable>

		final MainWindow window = new MainWindow();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setSize(new Dimension(1000, 800));
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}
}
