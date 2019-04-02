package clusterproject;

import java.awt.Dimension;

import javax.swing.JFrame;

import clusterproject.program.MainWindow;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		/*
		 * final Gpmetis metis = new Gpmetis(); final Grph g = new InMemoryGrph();
		 * g.addUndirectedSimpleEdge(0, 0); g.addUndirectedSimpleEdge(1, 0);
		 * g.addUndirectedSimpleEdge(1, 2); g.addUndirectedSimpleEdge(0, 2);
		 * metis.compute(g, 2, new Random());
		 */
		//TODO: could be looked at for graph partitioning

		final MainWindow window = new MainWindow();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setSize(new Dimension(1000, 800));
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}
}
