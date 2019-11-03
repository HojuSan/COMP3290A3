//* File:                       A3.java
// * Course:                    COMP2240
//  * Assignment:               Assignment3
//   * Name:                    Juyong Kim  
//    * Student Number:         c3244203
//     * Purpose:               Main file
//      * Note:                 -syntactic processing is not done in this assignment
//		 *						-spaces are delimiters, also /n and /r(carrigage return)
//		  *						-7. Other significant lexical items in CD19 are: semicolon (;) leftbracket ([)
//		   *					rightbracket (]) comma (,) leftparen ( ( ) rightparen ( ) ) equals (=) plus (+)
//		    *					minus (-) star (*) slash (/) percent (%) carat (^) less (<) greater (>) exclamation
//		     *					(!) quote (“) colon (:) dot (.).
//		      *
import java.io.*;
import java.util.*;

public class A3
{
	public static void main(String args[])  throws IOException
	{
		//input setup
		File file = new File(args[0]);	
		//File file = new File("testd2.txt");		
		//File outputFile = new File("outputListing.lst");							//adds file
		PrintWriter pw = new PrintWriter(System.out);					//Prints formatted representations of objects to a text-output stream
		StringBuffer sb = new StringBuffer("");							//a string that can be modified
		BufferedReader br = new BufferedReader(new FileReader(file));	//read char by char
		OutputController output = new OutputController(br, pw, sb);
		
		//A3 Output
		Parser parser  = new Parser(output);
		
		TreeNode.printTree(pw, parser.program());	
		
		//System.out.println("s string: + "+output.getString()+TreeNode.getString());
		
		try (PrintWriter ol = new PrintWriter("outputListing.lst")) {
			ol.println(output.getString()+TreeNode.getString());
		}
		//TreeNode.writeStringToFile(outputFile);
		System.out.println();
		//pw.flush();
		pw.close();
		
	}
}