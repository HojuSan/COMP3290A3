//* File:                       OutputController.java
// * Course:                    COMP2240
//  * Assignment:               Assignment1
//   * Name:                    Juyong Kim  
//    * Student Number:         c3244203
//     * Purpose:               the lexical analyzer converts a sequence of characters into a sequence of 
//      *                       distinct keywords, identifiers, constants, and delimiters.
//       * Note:                Just SCANS doesn't do syntactic processing

import java.io.*;
import java.util.*;

public class OutputController
{
    private BufferedReader inputStream;				//Text file reader	
    private PrintWriter listing = null;				//Currently not 100% sure how this thing works
	private StringBuffer errors = null;             //uses this to show errors
	private String currentLine;						//current line
	private String errorLine;
	private int line;                               //line
	private int charPos;                            //character position
	private int errorCount;                         //error counter

	//Constructor
	public OutputController(BufferedReader source, PrintWriter listing, StringBuffer errors)
	{
        //setting inside variables
		this.inputStream = source;
		this.listing = listing;
        this.errors = errors;
        //set everything to zero, cause we ain't heathens that start at 1
		currentLine = "  0: ";
		errorLine = "";
		line = 0;
		charPos = 0;
		errorCount = 0;
		System.out.println();
	}

	//Read char by char from text file
	public int readChar() throws IOException
	{
		int c = inputStream.read();
		
		if ((char)c == '\n')
		{
			listing.println(currentLine);
            line++;
            
			if (line < 10) 
			{
				currentLine = "  " + line + ": ";
			}
			else if (line < 100)
			{
				currentLine = " " + line + ": ";
			}
			else
			{
				currentLine = line + ": ";
			}
			charPos = 0;					//when new line reset char position
		}
		else if ((byte) c == -1)
		{
			if (errorCount != 0)
			{
				listing.println(currentLine);
				listing.println("Errors found: " + errorCount);
			}
			else
			{
				listing.println(currentLine);
				listing.println("Scanner has finished");
			}		
		}
		else
		{
			currentLine += "" + (char)c;
			charPos++;
		}
		return c;
	}

	//when errors occur reset the stream to continue lexical analysis
	public void reset() throws IOException
	{
		inputStream.reset();
	}

	//marks the location, will probably need it during parsing
	public void mark(int g) throws IOException
	{
		inputStream.mark(g);
	}

	public int getErrorCount()
	{
		return errorCount;
	}

	public void setError(String msg) 			
	{				
		if (!errorLine.equals("")) 
		{
			errorLine += "\n";				// terminate line for previous error message
		}
		errorLine += msg;
		errorCount++;
		listing.println(errorLine);			//print the error above
		errors.append(currentLine + "\n");
		errors.append(errorLine + "\n");
		errorLine = "";						// reset error message
	}

}
