
import arduino.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.io.*;

import com.fazecast.jSerialComm.SerialPort;

public class APcspFinal {

	public APcspFinal() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		
		Scanner outBound = new Scanner(System.in);
		
		
		String csvFile = "C:\\Users\\dresdon\\Documents\\APCSP\\FinalProject2018\\";
		String fileName = "";
		BufferedReader bufRead = null;
		String line = "";
		String cvsSplitBy = ",";
		int lineCount = 0;
		boolean alreadyTurned = false;
		
		
		System.out.print("Enter the csv filename:  ");
		fileName = outBound.nextLine();
		
		SerialPort comPort[] = SerialPort.getCommPorts();
		for (int index = 0; index < SerialPort.getCommPorts().length; index++) {
			System.out.println("Port[" + index + "]: " + comPort[index].getSystemPortName() + "\tOpen?: " + comPort[index].isOpen());
		}
		
		Arduino robot = new Arduino("COM8", 9600);
		if (robot.openConnection()) {
			System.out.println(robot.getPortDescription() + " opened!");
			System.out.println("Hit return to continue");
			outBound.nextLine();
//			char input = outBound.nextLine().charAt(0);
//			while (input != 'n') {
//				robot.serialWrite(input);
//				input = outBound.nextLine().charAt(0);
//			}

		} else {
			System.out.println(robot.getPortDescription() + "couldn't be opened");
		}
		
		try {
			bufRead = new BufferedReader(new FileReader(csvFile + fileName));

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
		outBound.close();
		robot.closeConnection();
	}
	
static	void delayProgram()	{
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			System.out.println("Sleep didn't work");
		}	
	}


}










/*		
SerialPort commPort = SerialPort.getCommPort("COM8");
System.out.println(commPort.getDescriptivePortName());
System.out.println(commPort.isOpen());
commPort.openPort();
System.out.println(commPort.isOpen());
commPort.closePort();
		
if (comPort[1].openPort())
{
	System.out.println("\t" + comPort[1].getSystemPortName() + " opened!");
	System.out.println("Hit return to continue");
	outBound.nextLine();
}
else
	System.out.println("\tPort still closed");
comPort[1].closePort();		

System.out.println("Now, trying it the other way.  Getting port descriptor using getCommPort");
SerialPort newCommPort = SerialPort.getCommPort("COM8");
System.out.println("System Port Name: " + newCommPort.getSystemPortName());
if (newCommPort.openPort())
{
	System.out.println("\t" + newCommPort.getSystemPortName() + " opened!");
	System.out.println("Hit return to continue");
	outBound.nextLine();
}
else
	System.out.println("\tPort still closed");
*/
	

//System.out.println(comPort[0]);
//System.out.println(comPort[1]);
//System.out.println(SerialPort.getCommPorts().length);
