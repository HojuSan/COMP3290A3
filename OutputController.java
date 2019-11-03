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
	private String s = "";
	private Boolean dTestCatch = false;

	//Constructor
	public OutputController(BufferedReader source, PrintWriter listing, StringBuffer errors)
	{
        //setting inside variables
		this.inputStream = source;
		this.listing = listing;
        this.errors = errors;
		currentLine = "  1: ";
		errorLine = "";
		line = 1;
		charPos = 0;
		errorCount = 0;
		System.out.println();
	}

	public int getLine()
	{
		return line;
	}

	public int getCharPos()
	{
		return charPos;
	}

	//Read char by char from text file
	public int readChar() 
	{
		int c = -1;
		try {
			c = inputStream.read();
		} catch (Exception e) {
			//TODO: handle exception
		}
		
		if ((char)c == '\n')
		{
			listing.println(currentLine);
			s+= currentLine+"\n";
            line++;
            
			if (line < 10) 
			{
				currentLine = "  " + line + ": ";
				s+= "  " + line + ": ";
			}
			else if (line < 100)
			{
				currentLine = " " + line + ": ";
				s+= " " + line + ": ";
			}
			else
			{
				currentLine = line + ": ";
				s+= line + ": ";
			}
			charPos = 0;					//when new line reset char position
		}
		//when the parser is finished print this out
		else if ((byte) c == -1&& dTestCatch == false)
		{
			if (errorCount != 0)
			{
				dTestCatch = true;
				listing.println(currentLine);
				listing.println();
				listing.println("!!!!Errors found: " + errorCount);
				listing.println();
				listing.println(errors);
				//listing.println();
				listing.println("Preorder Traversal:");
				listing.println();
				s+= "\n"+currentLine +"\n"+"\n"+"!!!!Errors found: " + errorCount+"\n"+"\n"+errors+"\n"+"Preorder Traversal:"+"\n"+"\n";
			}
			else
			{
				dTestCatch = true;
				listing.println(currentLine);
				listing.println();
				listing.println("Parser has finished");
				listing.println();
				listing.println("Preorder Traversal:");
				listing.println();
				s+= currentLine +"\n"+"\n"+"Parser has finished" +"\n"+"\n"+"Preorder Traversal:"+"\n"+"\n";
			}		
		}
		else
		{
			currentLine += "" + (char)c;
			charPos++;
		}
		//s+=(char)c;
		//System.out.println("column is at "+ charPos);
		return c;
	}

	//when errors occur reset the stream to continue lexical analysis
	public void reset() 
	{
		try {
			inputStream.reset();
		} catch (Exception e) {
			//TODO: handle exception
		}
	}

	//marks the location, will probably need it during parsing
	public void mark(int g) 
	{
		try {
			inputStream.mark(g);
		} catch (Exception e) {
			//TODO: handle exception
		}
	}

	public int getErrorCount()
	{
		return errorCount;
	}

	public void setError(String msg) 			
	{				
		String errorLinePrint = " At Line " + Integer.toString(line) + ": ";

		if (!errorLine.equals("")) 
		{
			errorLine += "\n";				// terminate line for previous error message
		}

		//errorLine += errorLinePrint + msg;
		errorLine += msg;
		errorCount++;
		//listing.println(errorLine);			//print the error above
		//errors.append(currentLine + "\n");
		errors.append(errorLine + "\n");
		errorLine = "";						// reset error message
	}

	public String getString()
	{
		return s;
	}


}
