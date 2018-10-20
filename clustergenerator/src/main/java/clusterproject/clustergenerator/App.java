package clusterproject.clustergenerator;

import java.awt.Dimension;

import javax.swing.JFrame;

import clusterproject.clustergenerator.userInterface.MainWindow;

/**
 * Hello world!
 *
 */
public class App
{
	public static void main( String[] args )
	{
		final MainWindow window=new MainWindow();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setSize(new Dimension(1000, 800));
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}
}
