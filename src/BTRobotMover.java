import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.*;
import javax.swing.text.*;

import com.fazecast.jSerialComm.SerialPort;

import arduino.*;

/**
 * BTRobotMover.java is a 1.4 application that uses
 * these additional files:
 *   SpringUtilities.java
 *   ...
 */
public class BTRobotMover extends JPanel
                                          implements ActionListener,
             
                                          FocusListener {
    Arduino robot;
	String csvDir = "D:\\Users\\Don\\Documents\\APCSP\\FinalProject2018\\";
    JTextField dirField, fileField;
    JSpinner portSpinner;
    boolean inputSet = false;
    Font regularFont, italicFont;
    JLabel inputDisplay;
    final static int GAP = 10;
    String directory = "";
    String filename = "";
    String port = "";
//    Font bigFont = new Font("Verdana", Font.PLAIN, 20);

    public BTRobotMover() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        JPanel leftHalf = new JPanel() {
            //Don't allow us to stretch vertically.
            public Dimension getMaximumSize() {
                Dimension pref = getPreferredSize();
                return new Dimension(Integer.MAX_VALUE,
                                     pref.height);
            }
        };
//        leftHalf.setFont(bigFont);
        leftHalf.setLayout(new BoxLayout(leftHalf,
                                         BoxLayout.PAGE_AXIS));
        leftHalf.add(createEntryFields());
        leftHalf.add(createButtons());

        add(leftHalf);
        add(createInputDisplay());
    }

    protected JComponent createButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));

        JButton button = new JButton("Process File");
        button.addActionListener(this);
        panel.add(button);

        button = new JButton("Clear File");
        button.addActionListener(this);
        button.setActionCommand("clear");
        panel.add(button);

        //Match the SpringLayout's gap, subtracting 5 to make
        //up for the default gap FlowLayout provides.
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0,
                                                GAP-5, GAP-5));
        return panel;
    }

    /**
     * Called when the user clicks the button or presses
     * Enter in a text field.
     */
    public void actionPerformed(ActionEvent e) {
    	if ("clear".equals(e.getActionCommand())) {
    		inputSet = false;
    		dirField.setText("");
    		fileField.setText("");

    		//We can't just setText on the formatted text
    		//field, since its value will remain set.
    		//            zipField.setValue(null);
    	} else {
    		inputSet = true;
    	}
    	updateDisplays();
    	// Process commands
    	if (inputSet) {
    		System.out.println("Creating robot on " + port);
    		createAndConnectRobot();
    		processCsvCommands();
    	} else {
    		// Close connection
    		System.out.println("Closing connection");
    		robot.closeConnection();
    	}
    }

    protected void createAndConnectRobot() {

    	if ((robot != null) && (robot.getSerialPort().isOpen())) {
    		System.out.println("Port " + port + " is already open");
    		System.out.println("Filename: " + filename);
    	} else {
    		robot = new Arduino(port, 9600);
    		if (robot.openConnection()) {
    			System.out.println("PC successfully connected to Arduino!");
    		} else {
    			System.out.println("Couldn't connect to Arduino on " + port);
    		}
    	}
    }
    
    
    protected void processCsvCommands() {
		BufferedReader bufRead = null;
		String line = "";
		String cvsSplitBy = ",";
		int lineCount = 0;
		boolean alreadyTurned = false;
		
    	try {
			bufRead = new BufferedReader(new FileReader(csvDir + filename));

			while ((line = bufRead.readLine()) != null)	{
				lineCount++;
			
				// use comma as separator
				String[] rMoves = line.split(cvsSplitBy);
				
				System.out.println("Robot Moves [ direction: " + rMoves[1] 
						+ "\tsteps: " + rMoves[2] + " }");
				if (lineCount > 1) {
					for(int index = 0; index < Integer.parseInt(rMoves[2]); index++)	{
						switch(rMoves[1].toLowerCase())	{
							case "forward":
								robot.serialWrite('1');
								break;
							case "backward":
								if (alreadyTurned) {
									robot.serialWrite('1');
								} else {
									robot.serialWrite('2');
									alreadyTurned = true;
								}
								break;
							case "left":
								if (alreadyTurned){
									robot.serialWrite('1');
								} else {
									robot.serialWrite('3');
									alreadyTurned = true;
								}
								break;
							case "right":
								if (alreadyTurned)	{
									robot.serialWrite('1');
								} else {
									robot.serialWrite('4');
									alreadyTurned = true;
								}
								break;
							default:
								System.out.println("Invalid direction");
								robot.serialWrite('0');
								break;
						}
					}
					robot.serialWrite('0');
					alreadyTurned = false;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally  {
			if (bufRead != null)	{
				try {
					bufRead.close();
				} catch (IOException e)	{
					e.printStackTrace();
				}
			}
		}
    }
    
    protected void updateDisplays() {
        inputDisplay.setText(formatInput());
        if (inputSet) {
            inputDisplay.setFont(regularFont);
        } else {
            inputDisplay.setFont(italicFont);
        }
    }

    protected JComponent createInputDisplay() {
        JPanel panel = new JPanel(new BorderLayout());
        inputDisplay = new JLabel();
 //       inputDisplay.setFont(bigFont);
        inputDisplay.setHorizontalAlignment(JLabel.CENTER);
        regularFont = inputDisplay.getFont().deriveFont(Font.PLAIN,
                                                            16.0f);
        italicFont = regularFont.deriveFont(Font.ITALIC);
        updateDisplays();

        //Lay out the panel.
        panel.setBorder(BorderFactory.createEmptyBorder(
                                GAP/2, //top
                                0,     //left
                                GAP/2, //bottom
                                0));   //right
        panel.add(new JSeparator(JSeparator.VERTICAL),
                  BorderLayout.LINE_START);
        panel.add(inputDisplay,
                  BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(200, 150));

        return panel;
    }

    protected String formatInput() {
        if (!inputSet) return "No filename and port set.";

        directory = dirField.getText();
        filename = fileField.getText();
        port = (String)portSpinner.getValue();
  //      String zip = zipField.getText();
        String empty = "";

        if ((directory == null) || empty.equals(directory)) {
            directory = "<em>(no directory specified)</em>";
        }
        if ((filename == null) || empty.equals(filename)) {
            filename = "<em>(no filename specified)</em>";
        }
        if ((port == null) || empty.equals(port)) {
            port = "<em>(no port specified)</em>";
        }
//        else {
//            int abbrevIndex = port.indexOf('(') + 1;
//            port = port.substring(abbrevIndex,
//                                    abbrevIndex + 2);
//        }
////        if ((zip == null) || empty.equals(zip)) {
//           zip = "";
 //       }

        StringBuffer sb = new StringBuffer();
        sb.append("<html><p align=left>");
        sb.append("<B>Processing...</B>");
        sb.append("<br>");
        sb.append("Directory: C:\\Users\\dresdon\\Documents\\APCSP\\FinalProject2018\\");
//        sb.append(directory);
        sb.append("<br>");
        sb.append("Filename: ");
        sb.append(filename);
        sb.append("<br>");
        sb.append("Port: ");
        sb.append(port); //should format
//        sb.append(" ");
//        sb.append(zip);
        sb.append("</p></html>");

        return sb.toString();
    }

    //A convenience method for creating a MaskFormatter.
    protected MaskFormatter createFormatter(String s) {
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter(s);
        } catch (java.text.ParseException exc) {
            System.err.println("formatter is bad: " + exc.getMessage());
            System.exit(-1);
        }
        return formatter;
    }

    /**
     * Called when one of the fields gets the focus so that
     * we can select the focused field.
     */
    public void focusGained(FocusEvent e) {
        Component c = e.getComponent();
        if (c instanceof JFormattedTextField) {
            selectItLater(c);
        } else if (c instanceof JTextField) {
            ((JTextField)c).selectAll();
        }
    }

    //Workaround for formatted text field focus side effects.
    protected void selectItLater(Component c) {
        if (c instanceof JFormattedTextField) {
            final JFormattedTextField ftf = (JFormattedTextField)c;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ftf.selectAll();
                }
            });
        }
    }

    //Needed for FocusListener interface.
    public void focusLost(FocusEvent e) { } //ignore

    protected JComponent createEntryFields() {
        JPanel panel = new JPanel(new SpringLayout());

        String[] labelStrings = {
            "Directory: ",
            "Filename: ",
            "Port: "
 //           "Zip code: "
        };

        JLabel[] labels = new JLabel[labelStrings.length];
        JComponent[] fields = new JComponent[labelStrings.length];
        int fieldNum = 0;

        //Create the text field and set it up.
        dirField  = new JTextField();
        dirField.setColumns(40);
        fields[fieldNum++] = dirField;

        fileField = new JTextField();
        fileField.setColumns(20);
        fields[fieldNum++] = fileField;

        String[] portStrings = getPortStrings();
        portSpinner = new JSpinner(new SpinnerListModel(portStrings));
        fields[fieldNum++] = portSpinner;

//        zipField = new JFormattedTextField(
//                            createFormatter("#####"));
//        fields[fieldNum++] = zipField;

        //Associate label/field pairs, add everything,
        //and lay it out.
        for (int i = 0; i < labelStrings.length; i++) {
            labels[i] = new JLabel(labelStrings[i],
                                   JLabel.TRAILING);
            labels[i].setLabelFor(fields[i]);
            panel.add(labels[i]);
            panel.add(fields[i]);

            //Add listeners to each field.
            JTextField tf = null;
            if (fields[i] instanceof JSpinner) {
                tf = getTextField((JSpinner)fields[i]);
            } else {
                tf = (JTextField)fields[i];
            }
            tf.addActionListener(this);
            tf.addFocusListener(this);
        }
        SpringUtilities.makeCompactGrid(panel,
                                        labelStrings.length, 2,
                                        GAP, GAP, //init x,y
                                        GAP, GAP/2);//xpad, ypad
        return panel;
    }

    // Hmmmm.... Can maybe generate port strings
    public String[] getPortStrings() {
    	SerialPort comPort[] = SerialPort.getCommPorts();
    	String[] portStrings = new String[comPort.length];
    	for (int i = 0; i < comPort.length; i++) {
    		portStrings[i] = comPort[i].getSystemPortName();
    	}
    	return portStrings;
    }

    public JFormattedTextField getTextField(JSpinner spinner) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            return ((JSpinner.DefaultEditor)editor).getTextField();
        } else {
            System.err.println("Unexpected editor type: "
                               + spinner.getEditor().getClass()
                               + " isn't a descendant of DefaultEditor");
            return null;
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void processRobotCsvFile() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame frame = new JFrame("AP CSP Final Project - Robot Mover");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new BTRobotMover();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
    	java.util.Enumeration keys = UIManager.getDefaults().keys();
    	while (keys.hasMoreElements()) {
    		Object key = keys.nextElement();
    		Object value = UIManager.get(key);
    		if (value instanceof javax.swing.plaf.FontUIResource)
    			UIManager.put(key, f);
    	}
    }

    public static void main(String[] args) {
    	setUIFont (new javax.swing.plaf.FontUIResource("Verdana", Font.BOLD, 22));
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                processRobotCsvFile();
            }
        });

    }
}