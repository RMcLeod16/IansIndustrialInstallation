/**
 * 
 */
package iansindustrialinstallation;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.awt.event.MouseListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;

import javax.imageio.ImageIO;

import javax.swing.SpringLayout;
import javax.swing.JOptionPane;




/**
 * @author Russell McLeod
 *
 */
// Define class extending Frame and implementing listeners
public class IansIndustrialInstallation extends Frame implements WindowListener, ActionListener, MouseListener, AWTEventListener
{
	int totalX = 30;	// default X value for TextField Grid
	int totalY = 30;	// default Y value for TextField Grid
	
	TextField[][] contents = new TextField[totalX][totalY];	// default array for use when making TextField grid
	String[][] data = new String[totalX][totalY+3];	// default array for storing all data from .csv file 
	
	// Declare most of the controls used by program
	Label lblAddress, lblDate, lblTime, lblHazardLvl, lblHazardName, lblLevel, lblExport, lblLegend, lblAccept, lblConcern, lblDanger;
	Button btnSO2, btnNO2, btnCO, btnObstruct, btnExport, btnClose;
	TextField txtValue, txtAccept, txtConcern, txtDanger;
	
	// Flag for debug logging
	static boolean DEBUG_LOGGING = false;
	
	// Flag for "Manual Mode",which is whether or not data files are loaded by default name
	// or if user manually selects the file to load
	boolean manualMode = false;
	
	// https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html
	// An enumerator to store all our data regarding the four hazards
	public enum Hazard
	{
		// Format is Name, Acceptable (green) limit, Concerning (yellow) limit, Dangerous (red) limit
		SO2 ( "Sulphur Dioxide", 1, 10, 30 ),
		NO2 ( "Nitrogen Dioxide", 1, 10, 30 ),
		CO ( "Carbon Monoxide", 1, 8, 25 ),
		Obstruct ( "Obstructions", 1, 2, 3 );
		
		private final String hazard_name;
		private final int green_limit;
		private final int yellow_limit;
		private final int red_limit;
		
		// constructor for Hazard enum
		Hazard (String name, int green_limit, int yellow_limit, int red_limit)
		{
			this.hazard_name = name;
			this.green_limit = green_limit;
			this.yellow_limit = yellow_limit;
			this.red_limit = red_limit;
		}
		
		// some getters for the enum contents
		private String hazard_name() { return hazard_name; }
		private int green_limit() { return green_limit; }
		private int yellow_limit() { return yellow_limit; }
		private int red_limit() { return red_limit; }
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// Instantiate program
		IansIndustrialInstallation program = new IansIndustrialInstallation();
		
		// check for supported command line arguments
		try
		{
			switch (args[0])
			{
			// Debug Mode-1, logs things to file, appending to existing log file
			case "-d1":
				DEBUG_LOGGING = true;
				DebugPrintln("Debug Logging Mode-1 Enabled. (Append to log)");
				break;
				
			// Debug Mode-2,logs things to file, but deletes old log and creates new one
			case "-d2":
				// http://www.mkyong.com/java/how-to-delete-file-in-java/
				DEBUG_LOGGING = true;
				
				File log = new File("DEBUG.LOG");
				
				// Try to delete old DEBUG.LOG file first if -d2 is selected
				if (log.delete())
				{
					DebugPrintln(log.getName() + " deleted!");
				}
				else
				{
					DebugPrintln("ERROR! Failed to delete " + log.getName() + ". This should be ok.");
				}
				DebugPrintln("Debug Logging Mode-2 Enabled. (Create new log)");
				break;
				
			default:
				break;
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			DebugPrintln("No program arguments found, this should not be an issue. Continuing with program.");
		}

		// call run method
		program.run();
	}
	
	
	// Methods:
	
	private void run()
	{
		DebugPrintln("IAN'S INDUSTRIAL INSTALLATION APPLICATION STARTED!");
		
		GetXYFromFile("Ians_W7_SO2.csv");
		
		// Set bounds for the program frame/window
		int boundsX = (totalX * 30) + 200;
		int boundsY = (totalY * 20) + 120;
		
		// If they are below certain size (too small to fit all of program),
		// set them to a predefined minimum
		if (boundsX < 800)
			boundsX = 800;
		
		if (boundsY < 480)
			boundsY = 480;
		
		// Setup the frame
		setBounds(10, 10, boundsX, boundsY);
        setTitle("Ian's Industrial Installation");
        this.addWindowListener(this);
        this.getToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
        setResizable(false);
        setFocusable(true);
        
        initLayout();
        
        // Display frame on screen
        setVisible(true);
	}

	//  Init some layout stuff
	private void initLayout()
	{
		// Init layout
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		
		// add logo to layout
		ImageComponent logo = new ImageComponent("logo.jpg");
		add(logo);
		layout.putConstraint(SpringLayout.WEST, logo, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, logo, 0, SpringLayout.NORTH, this);
		
		// add other things to layout
		AddLabels(layout);
		AddButtons(layout);
		AddLegend(layout);
		AddDataBoxes(layout);
		
		// Add Hazard Value TextField to layout
		txtValue = AddATextField(layout, txtValue, 3, 115, 410);
	}
	
	private void AddLabels(SpringLayout layout)
	{
		// Add basic labels to layout
		lblAddress = LocateALabel(layout, lblAddress, "Address                            ", 400, 10);
		lblDate = LocateALabel(layout, lblDate, "Date:                  ", 575, 10);
		lblTime = LocateALabel(layout, lblTime, "Time:                  ", 685, 10);
		lblLevel = LocateALabel(layout, lblLevel, "Recorded Level:", 10, 410);
		lblHazardLvl = LocateALabel(layout, lblHazardLvl, "Hazard Levels  ", 25, 40);
		lblHazardName = LocateALabel(layout, lblHazardName, "(Hazard name)             ", 25, 60);
	}
	
	private void AddButtons(SpringLayout layout)
	{
		// Add buttons to layout
		btnSO2 = LocateAButton(layout, btnSO2, Hazard.SO2.hazard_name(), 10, 200, 150, 25);
		btnNO2 = LocateAButton(layout, btnNO2, Hazard.NO2.hazard_name(), 10, 230, 150, 25);
		btnCO = LocateAButton(layout, btnCO, Hazard.CO.hazard_name(), 10, 260, 150, 25);
		btnObstruct = LocateAButton(layout, btnObstruct, Hazard.Obstruct.hazard_name(), 10, 290, 150, 25);
		
		btnExport = LocateAButton(layout, btnExport, "Export .RAF .DAT .RPT", 10, 350, 150, 25);
		btnClose = LocateAButton(layout, btnClose, "Exit Program", 10, 380, 150, 25);
	}
	
	private void AddLegend(SpringLayout layout)
	{
		// Add legend related stuff to layout
		lblLegend = LocateALabel(layout, lblLegend, "Legend", 10,95);
		txtAccept = AddATextField(layout, txtAccept, 1, 10, 120);
		txtConcern = AddATextField(layout, txtConcern, 1, 10, 140);
		txtDanger = AddATextField(layout, txtDanger, 1, 10, 160);
		
		txtAccept.setBackground(new Color(153,255,153));
		txtConcern.setBackground(new Color(255,255,153));
		txtDanger.setBackground(new Color(255,153,153));
		
		lblAccept = LocateALabel(layout, lblAccept, "Acceptable", 45, 120);
		lblConcern = LocateALabel(layout, lblConcern, "Concerning", 45, 140);
		lblDanger = LocateALabel(layout, lblDanger, "Dangerous", 45, 160);
		
	}
	
	// Creates and adds to layout the TextField boxes that are used as a color-coded grid
	private void AddDataBoxes(SpringLayout layout)
	{
		// For each row...
		for (int y = 0; y < totalY; y++)
		{
			// For each cell in row...
			for (int x = 0; x < totalX; x++)
			{
				// Make new TextField
				contents[x][y] = new TextField(1);
				
				// Add it to layout
				add(contents[x][y]);
				// Apply layout constraints to it
				layout.putConstraint(SpringLayout.WEST, contents[x][y], x * 30 + 175, SpringLayout.WEST, this);
                layout.putConstraint(SpringLayout.NORTH, contents[x][y], y * 20 + 70, SpringLayout.NORTH, this);
                
                // Prevent user from altering it
                contents[x][y].setEditable(false);
                // Add Mouse listener to it (for use later)
                contents[x][y].addMouseListener(this);
			}
		}
	}
	
	// Method that grabs the data from the .csv file and refreshes the GUI with relevant stuff
	// (color-coded boxes, certain labels, etc.)
	private void ReloadAndRefresh(Hazard hazard)
	{
		DebugPrintln("Loading information for " + hazard + " to screen...");
		
		String fileName = "Ians_W7_" + hazard + ".csv";	// default file name
		
		// If Manual Mode, set up open file dialog to select file
		if (manualMode)
		{
			// http://stackoverflow.com/questions/7211107/how-to-use-filedialog
			// Make new Open File Dialog
			FileDialog fd = new FileDialog(this, "Open .csv file", FileDialog.LOAD);
			
			// Set it to default directory
			fd.setDirectory("");
			
			// Set it to display only .csv files for selection
			fd.setFile("*.csv");
			
			// Display File Dialog on screen
			fd.setVisible(true);
			
			// Get fileName from user-selected file
			fileName = fd.getFile();
			
			// Turn off manual Mode when done with it
			manualMode = false;
			// Refresh the Hazard button text (it changes when Manual Mode is on)
			RefreshHazardButtonText();
			
			if (fileName == null)
			{
				DebugPrintln("Manual File Loading cancelled.");
				return;
			}
			else
			{
				DebugPrintln("Selected file is " + fileName);
			}
		}
		
		// Load Data to data array[][]
		data = LoadDataFromFile(fileName);

		// Set the TextField Grid color-coding
		SetBoxColors(hazard);
		// Update various GUI controls based on loaded data
		lblHazardLvl.setText(hazard + " Levels");
		lblHazardName.setText("(" + hazard.hazard_name() + ")");
		lblAddress.setText(data[0][0]);
		lblDate.setText("Date: " + data[0][1]);
		lblTime.setText("Time: " + data[0][2]);
	}
	
	// Changes the text on the Hazard-related buttons depending on Manual Mode status
	private void RefreshHazardButtonText()
	{
		if (manualMode)
		{
			// Further reflects that it will open manually selected csv file
			btnSO2.setLabel("Open " + Hazard.SO2 + " csv...");
			btnNO2.setLabel("Open " + Hazard.NO2 + " csv...");
			btnCO.setLabel("Open " + Hazard.CO + " csv...");
			btnObstruct.setLabel("Open " + Hazard.Obstruct + " csv...");
		}
		else
		{
			// Default, just shows hazard name
			btnSO2.setLabel(Hazard.SO2.hazard_name());
			btnNO2.setLabel(Hazard.NO2.hazard_name());
			btnCO.setLabel(Hazard.CO.hazard_name());
			btnObstruct.setLabel(Hazard.Obstruct.hazard_name());
		}
	}

	// Method that grabs first default hazard file
	// and calculates related X and Y from its data
	private void GetXYFromFile(String fileName)
	{
		DebugPrintln("Trying to get X and Y counts from " + fileName + "...");
		try
		{
			// Create new BufferedReader to read file
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			
			// Setup new x and y counts
			int x_Count = 0;
			int y_Count = 0;
			
			// while can read the file..
			while (br.readLine() != null)
			{
				// Count all the lines for y
				y_Count++;
				
				if (y_Count == 3)	// First line with actual data
				{
					// Split the line into each x piece separated by comma
					String[] lineData = br.readLine().split(",");
					// Count the amount of pieces on a line to get x
					x_Count = lineData.length;
				}
			}
			
			// close BufferedReader when done with it
			br.close();
			
			DebugPrintln("Calculated x,y as " +x_Count + "," + (y_Count - 2));

			// Update the totals with new values,
			// and the arrays that use them as a base for their sizes
			totalX = x_Count;
			totalY = y_Count - 2;	// Offset to account for the extra lines in the data files
			contents = new TextField[totalX][totalY];
			data = new String[totalX][totalY+3];
		}
		catch (Exception e)
		{
			System.err.println("ERROR: " + e.getMessage());
			DebugPrintln("ERROR!\t" + e.getMessage());
		}
	}
	
	// Loads ALL the data from the csv file and makes a 2D array out of it
	private String[][] LoadDataFromFile(String fileName)
	{	
		DebugPrintln("Trying to load data from " + fileName + "...");
		String[][] tempdata = new String[totalX][totalY+3];
		
		try
		{
			// Create new BufferedReader to read file
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			
			// For each y coordinate...
			for (int y = 0; y < totalY + 3; y++)
			{
				// Read line from file
				String line = br.readLine();
				
				//  If y is one of the first 3 (which holds, location, date, and time stamp)
				if (y < 3)
				{
					// Each of the above goes in its own y coordinate(do not split the line)
					tempdata[0][y] = line;
				}
				// else if 4th line or more (the lines that hold data readings)
				else if (y >= 3)	
				{
					// split the line separated by comma
					// and put each piece into temp array
					String[] lineData = line.split(",");
					
					// for each of those pieces...
					for (int x = 0; x < lineData.length;x++)
					{
						// put each piece in its respective spot
						// in its respective row in the 2D array 
						tempdata[x][y] = lineData[x];
					}
				}
			}
			// Close the BufferedReader when done with it
			br.close();	
		}
		
		catch (Exception e)
		{
			System.err.println("Error: " + e.getMessage()); // print message on error
			DebugPrintln("ERROR!\t" + e.getMessage());
		}
		
		// return the 2D array that was made
		return tempdata;
	}
	
	private void SetBoxColors(Hazard hazard)
	{
		//For each row...
		for (int y = 0; y < totalY; y++)
		{
			// For each box in row...
			for (int x = 0; x < totalX; x++)
			{
				// Set the color of the box based on the value it is meant to represent
				contents[x][y].setBackground(GetBoxColor(Integer.parseInt(data[x][y+3]), hazard));
			}
		}
	}
	
	// Method to get respective color based on the Hazard and value given to it
	private Color GetBoxColor(int boxValue, Hazard hazard)
	{
		// Setup color variable
		Color boxColor = new Color(255,255,255); //White, default
		
		// If value is 0
		if (boxValue == 0)
		{
			boxColor = new Color(255,255,255);	// Color is white
		}
		
		// Else if the value is above 0 but below the Concerning threshold...
		else if (boxValue < hazard.yellow_limit())
		{
			boxColor = new Color(153,255,153);	// Color is a light green
		}
		
		// Else if it is equal to or above Concerning threshold
		// but below Dangerous threshold...
		else if (boxValue < hazard.red_limit())
		{
			boxColor = new Color(255,255,153);	// Color is a light yellow
		}
		
		// Otherwise...
		// (at this point would be either equal to or above dangerous threshold)
		else
		{
			boxColor = new Color(255,153,153);	// Color is a light red
		}
		// Return the calculated color
		return boxColor;
	}
	
	// Method to get respective color letter based on the Hazard and value given to it
	private String GetColorLetter(int boxValue, Hazard hazard)
	{
		// set up color string variable
		String letter = "W"; //White, default
		
		// If value is 0
		if (boxValue == 0)
		{
			letter = "W";
		}
		
		// Else if the value is above 0 but below the Concerning threshold...
		else if (boxValue < hazard.yellow_limit())
		{
			letter = "G";
		}
		
		// Else if it is equal to or above Concerning threshold
		// but below Dangerous threshold...
		else if (boxValue < hazard.red_limit())
		{
			letter = "Y";
		}
		
		// Otherwise...
		// (at this point would be either equal to or above dangerous threshold)
		else
		{
			letter = "R";
		}
		
		// Return the calculated letter
		return letter;
	}
	
	// Method to generate and and export data for all 4 hazards in formats specified by client
	private void ExportData()
	{
		// Change Export button text to reflect current actions
		btnExport.setLabel("Exporting...");
		
		// http://stackoverflow.com/questions/3634853/how-to-create-a-directory-in-java
		// setup directory to export files into
		File dir = new File("export");
		if (!dir.exists())
		{
			if(!dir.mkdir())
			{
				DebugPrintln("ERROR: Unable to create directory " + dir.getName());
			}
		}
		
		// http://stackoverflow.com/questions/2399590/java-what-does-the-colon-operator-do
		// http://stackoverflow.com/questions/16674881/how-do-i-loop-through-an-enum-in-java
		// For each of the hazards...
		for (Hazard hazard : Hazard.values())
		{
			// Load the data from default files
			String[][] tempdata = LoadDataFromFile("Ians_W7_" + hazard + ".csv");
			
			// Calculate the totalX and totalY for the data
			int temp_totalX = tempdata.length;
			int temp_totalY = (tempdata[0].length);
			
			// declare some StringBuilders which will be used to make specific-formatted lines
			// http://stackoverflow.com/questions/12899953/in-java-how-to-append-a-string-more-efficiently
			
			StringBuilder sbRAF = new StringBuilder();

			StringBuilder sbDAT = null; // new StringBuilder();
			StringBuilder sbRPT = null; // new StringBuilder();
			
			// Declare some String variables used for the final outputs for each export type
			String RAF_output = null;
			String[] DAT_output = new String[temp_totalX * (temp_totalY - 3)];
			String[] RPT_output = new String[temp_totalY - 3];
			
			// a counter used to count each cell of data
			int cellCounter = 0;
			
			// for each row/line... (not counting the first 3 used for non-data-reading stuff)
			for (int y = 3; y < temp_totalY; y++)
			{
				// Make new StringBuilder for RPT file
				sbRPT = new StringBuilder();
				
				// Setup some default vars
				String color = "color";
				int counter = 0;
				
				// for each cell on a row/line...
				for (int x = 0; x < temp_totalX; x++)
				{
					// make new StringBuilder for DAT file
					sbDAT = new StringBuilder();
					
					// Append respective color letter to RAF file StringBuilder
					// Format is just each data recording's letter all on one line
					sbRAF.append(GetColorLetter(Integer.parseInt(tempdata[x][y]), hazard));
					
					// Build string for a line of DAT file
					// format is a  line of "x_coord,Y_coord,colorletter"
					// for each data recording 
					sbDAT.append(x+1);
					sbDAT.append(",");
					sbDAT.append(y-2);
					sbDAT.append(",");
					sbDAT.append(GetColorLetter(Integer.parseInt(tempdata[x][y]), hazard));
					
					// Add the line to output array for DAT file
					DAT_output[cellCounter] = sbDAT.toString();
					cellCounter++;

					// DebugPrintln("Box cell No. " + cellCounter + " exists at [" + (x + 1) + "][" + (y - 2) + "] and has color of " + GetColorLetter(Integer.parseInt(tempdata[x][y]), hazard) + " for the " + hazard.hazard_name() + " hazard.");
					
					// RPT file line generation...
					// format is one line per row, listing color
					// and count of said color occurring consecutively

					// RPT line generation may or may not be bad for one's health...
					
					// If at first data reading in the line...
					if (x == 0)
					{
						// Get the color letter
						color = GetColorLetter(Integer.parseInt(tempdata[0][y]), hazard);
						
						//  set counter to 1
						counter = 1;
					}
					// If not the first data reading in file...
					else
					{
						// If color of current data reading is equal to previous one...
						if (color.equalsIgnoreCase(GetColorLetter(Integer.parseInt(tempdata[x][y]), hazard)))
						{
							// if the last data reading on the line...
							if (x == temp_totalX -1)
							{
								counter++;
								sbRPT.append(color);
								sbRPT.append(",");
								sbRPT.append(counter);
							}
						}
						// Otherwise... (color is different from last one)
						else
						{
							sbRPT.append(color);
							sbRPT.append(",");
							sbRPT.append(counter);
							
							// If not last data reading in the row
							if (x < temp_totalX)
							{
								//add a comma to line
								sbRPT.append(",");
							}
							
							// reset stuff for next line
							color = GetColorLetter(Integer.parseInt(tempdata[x][y]), hazard);
							counter = 0;
						}
						counter++;
					}
				}
				// add generated line to respective array
				RPT_output[y - 3] = sbRPT.toString();
			}
			// add generated line to respective string
			RAF_output = sbRAF.toString();
			
			// Get final results and write them to respective files!
			// WriteLinesToFile(RAF_output, dir.getName() + "\\Ians_W7_" + hazard + ".RAF");
			writeRAF(dir.getName() + "\\Ians_W7_" + hazard + ".RAF", 0, RAF_output);
			WriteLinesToFile(DAT_output, dir.getName() + "\\Ians_W7_" + hazard + ".DAT");
			WriteLinesToFile(RPT_output, dir.getName() + "\\Ians_W7_" + hazard + ".RPT");
		}
		
		// Set Export button text to default when done
		btnExport.setLabel("Export .RAF .DAT .RPT");
		
		// Also display message box informing user that export should be completed
		// when everything is (hopefully) finally done
		// http://stackoverflow.com/questions/7080205/popup-message-boxes
		JOptionPane.showMessageDialog(null,".RAF, .DAT, .RPT File Export should be done!" + System.lineSeparator() + System.lineSeparator() + "Check \"export\\\" folder to access them.",".RAF, .DAT, .RPT File Export",JOptionPane.INFORMATION_MESSAGE);
	}
	
	// http://javarevisited.blogspot.com.au/2015/02/randomaccessfile-example-in-java-read-write-String.html
	private void writeRAF(String fileName, int position, String str)
	{
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
			
			raf.seek(position);
			raf.writeUTF(str);
			
			raf.close();
			// DebugPrintln(fileName + "\t" + position + "\t" + str);
		}
		catch (Exception e)
		{
			System.err.println("ERROR!\t" + e.getMessage());
			DebugPrintln("ERROR!\t" + e.getMessage());
		}
	}
	
	// Method that gets a string, and writes it to a file
	/* Currently Unused, RAF creation is handled by writeRAF()
	private void WriteLinesToFile(String line, String fileName)
	{
		DebugPrintln("Trying to write String line to " + fileName + "...");
		try
		{
			// Set up PrintWriter
			PrintWriter out = new PrintWriter(fileName);
			// Print given string on new line
			out.println(line);
			// Close PrintWriter when done with it
			out.close();
		}
		catch (Exception e)
		{
			System.err.println("ERROR!\t" + e.getMessage());
			DebugPrintln("ERROR!\t" + e.getMessage());
		}
	}
	*/
	
	// Same method as above, but is overload to handle a string[]
	private void WriteLinesToFile(String[] lines, String fileName)
	{
		DebugPrintln("Trying to write String[] lines to " + fileName + "...");
		try
		{
			// Set up PrintWriter
			PrintWriter out = new PrintWriter(fileName);
			// For each String in the given String array...
			for (int x = 0; x < lines.length; x++)
			{
				// Print given string on new line
				out.println(lines[x]);
			}
			// Close PrintWriter when done with it
			out.close();
		}
		catch (Exception e)
		{
			System.err.println("ERROR!\t" + e.getMessage());
			DebugPrintln("ERROR!\t" + e.getMessage());
		}
	}
	
	// inefficient, but it works for now.
	// Method that is used as a wrapper for System.out.println
	// but adds timestamp to the line
	// and can also add that line to a debug log file
	// use -d1 or -d2 argument when running program to output log file
	// -d1 appends it to existing log if it exists
	// -d2 tries to delete old debug log and create new one first
	private static void DebugPrintln(String line)
	{
		// http://stackoverflow.com/questions/1625234/how-to-append-text-to-an-existing-file-in-java
		// http://stackoverflow.com/questions/5175728/how-to-get-the-current-date-time-in-java
		try
		{
			// grab current DateTime for timestamp
			LocalDateTime ldt = LocalDateTime.now();
			// set up StringBuilder
			StringBuilder sb = new StringBuilder();
			
			// Build the string
			//format is "[ timestamp ] TAB given_line"
			sb.append("[");
			sb.append(ldt.toString());
			sb.append("]:\t");
			sb.append(line);
			sb.append(System.lineSeparator());
			
			// update line var to final generated version of line
			line = sb.toString();
			// Print it to console
			System.out.print(line);
			
			// If DEBUG_LOGGING is enabled (via -d1 or -d2 command line switches)
			if (DEBUG_LOGGING)
			{
				// Setup new FileWriter
				FileWriter out = new FileWriter("DEBUG.LOG", true);
				// write line to it
				out.write(line);
				// Close FileWriter when done with it
				out.close();
			}

		}
		catch (Exception e)
		{
			System.err.println("ERROR!\t" + e.getMessage());
		}
	}
	
	// LocateALabel and LocateAButton taken from My-Friend Tracker
    public Label LocateALabel(SpringLayout myLabelLayout, Label myLabel, String LabelCaption, int x, int y) {
        myLabel = new Label(LabelCaption);
        add(myLabel);
        myLabelLayout.putConstraint(SpringLayout.WEST, myLabel, x, SpringLayout.WEST, this);
        myLabelLayout.putConstraint(SpringLayout.NORTH, myLabel, y, SpringLayout.NORTH, this);
        return myLabel;
    }
	
	public Button LocateAButton(SpringLayout myButtonLayout, Button myButton, String ButtonCaption, int x, int y, int w, int h) {
        myButton = new Button(ButtonCaption);
        add(myButton);
        myButtonLayout.putConstraint(SpringLayout.WEST, myButton, x, SpringLayout.WEST, this);
        myButtonLayout.putConstraint(SpringLayout.NORTH, myButton, y, SpringLayout.NORTH, this);
        myButton.setPreferredSize(new Dimension(w, h));
        myButton.addActionListener(this);
        return myButton;
    }
	
	public TextField AddATextField(SpringLayout layout, TextField textfield, int width, int x, int y)
	{
		textfield = new TextField(width);
		add(textfield);
		layout.putConstraint(SpringLayout.WEST, textfield, x, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, textfield, y, SpringLayout.NORTH, this);
		return textfield;
	}
	
	// Simple method that prints a message to console (and debug log) then exits program
	private void Exit()
	{
		DebugPrintln("Exiting Program...");
		System.exit(0);
	}
	
	// Events:
	
	@Override
	public void windowActivated(WindowEvent e)
	{
	}

	@Override
	public void windowClosed(WindowEvent e)
	{	
	}

	@Override
	public void windowClosing(WindowEvent e)
	{
		Exit();
	}

	@Override
	public void windowDeactivated(WindowEvent e)
	{
	}

	@Override
	public void windowDeiconified(WindowEvent e)
	{
	}

	@Override
	public void windowIconified(WindowEvent e)
	{
	}

	@Override
	public void windowOpened(WindowEvent e)
	{	
	}

	// Button-related events
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == btnSO2)
		{
			ReloadAndRefresh(Hazard.SO2);
		}
		
		if (e.getSource() == btnNO2)
		{
			ReloadAndRefresh(Hazard.NO2);
		}
		
		if (e.getSource() == btnCO)
		{
			ReloadAndRefresh(Hazard.CO);
		}
		
		if (e.getSource() == btnObstruct)
		{
			ReloadAndRefresh(Hazard.Obstruct);
		}
		
		if (e.getSource() == btnExport)
		{
			ExportData();
		}
		
		if (e.getSource() == btnClose)
		{
			Exit();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
	}

	// Event used for when mouse cursor moves over a control
	// in this case it is sued with the TextField grid
	@Override
	public void mouseEntered(MouseEvent e)
	{
		/* The TextFields used as color boxes do not have the actual data in them
		 * (A different value box below was wanted by client instead)
		 * and the TextFields know nothing of the 2D array that was used to spawn them...
		 * 
		 * BUT
		 * 
		 * They do know of their coordinates on screen.
		 * And the box TextFields always start at 178,95
		 * (on Windows systems at least...)
		 * regardless of the amount of rows or columns
		 * 
		 * 178,95 = 0,0
		 * 208,95 = 1,0
		 * 178,115 = 0,1
		 * 
		 * difference of 30 for x intervals
		 * difference of 20 for y intervals
		 * 
		 * So, for x, formula is (x_coord - 178) / 30
		 * And, for y, formula is (y_coord - 95) / 20
		 * 
		 * Use these to pull data from 2D array that houses it,
		 * and display in TextField txtValue.
		 *
		*/
		
		int x_coord = e.getComponent().getX();
		int y_coord = e.getComponent().getY();
		
		int x = (x_coord - 178) / 30;
		int y = (y_coord - 95) / 20;
		
		// Display value of calculated grid box to Value TextField
		txtValue.setText(data[x][y+3]);
		
		// Debug output to console used during testing of the above
		DebugPrintln("Box at Coordinates " + x_coord + "," + y_coord + " houses color " + e.getComponent().getBackground().toString() + " for data[" + x + "][" + y + "] which is " + data[x][y+3]);
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
	}
	
	// Used for monitoring the status of CTRL keyboard button regardless of focus 
	// http://stackoverflow.com/questions/12434740/how-do-you-make-key-bindings-for-a-java-awt-frame
	@Override
	public void eventDispatched(AWTEvent event)
	{
		// If the detected event is a KeyEvent
		if (event instanceof KeyEvent)
		{
			KeyEvent key = (KeyEvent)event;
			// If the CTRL key is pressed/held down
			if (key.isControlDown())
			{
				DebugPrintln("CTRL is pressed, manual file select mode ON.");
				// Manual Mode ON! User selects csv file manually to load up
				manualMode = true;
				// Change Hazard button text to reflect the above
				RefreshHazardButtonText();
			}
			// Else if the CTRL key is released
			else if (key.getID() == key.KEY_RELEASED && key.getKeyCode() == key.VK_CONTROL)
			{
				DebugPrintln("CTRL is released, manual file select mode OFF.");
				// Manual Mode OFF. File selected uses default file name
				manualMode = false;
				// Change hazard button text to default
				RefreshHazardButtonText();
			}
			key.consume();
		}
	}
	
	// makes custom ImageComponent from Component
	// taken directly from http://www.tutorialspoint.com/awt/awt_image.htm
	class ImageComponent extends Component
	{
		// Declare BufferedImage
		BufferedImage img;
	
		// Draws image to screen
		public void paint(Graphics g)
		{
			g.drawImage(img, 0, 0, null);
		}
		
		// Constructor, grabs image from file
		public ImageComponent(String path)
		{
			try
			{
				img = ImageIO.read(new File(path));
			}
			catch (Exception e)
			{
				System.err.println("ERROR!:\t" + e.getMessage());
				DebugPrintln("ERROR!:\t" + e.getMessage());
			}
		}
		
		// Grabs dimensions from image
		public Dimension getPreferredSize()
		{
			if (img == null)
			{
				return new Dimension(100,100);
			}
			else
			{
				return new Dimension(img.getWidth(), img.getHeight());
			}
		}
	}
}
