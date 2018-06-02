import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SwingExample1 {

	public SwingExample1() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
	
	private static void createAndShowGUI() {
		// Make sure we have nice window decorations
		JFrame.setDefaultLookAndFeelDecorated(true);
		
		// Create and set up the window
		JFrame frame = new JFrame("HelloWorldSwing");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Add the ubiquitous "Hello World" label.
		JLabel label = new JLabel("Hello World");
		frame.getContentPane().add(label);
		
		// Display the window
		frame.pack();
		frame.setVisible(true);
	}

}
