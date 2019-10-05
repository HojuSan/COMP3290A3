//* File:                       A1.java
// * Course:                    COMP2240
//  * Assignment:               Assignment1
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

public class A1
{
	public static void main(String args[])  throws IOException
	{
		File file = new File(args[0]);									//adds file
		PrintWriter pw = new PrintWriter(System.out);					//Prints formatted representations of objects to a text-output stream
		StringBuffer sb = new StringBuffer("");							//a string that can be modified
		BufferedReader br = new BufferedReader(new FileReader(file));	//read char by char
		OutputController output = new OutputController(br, pw, sb);
		Scanner scan = new Scanner(output);

		Token temp;
		
		//currently not sure how things will print
		while(true != scan.isEOF())
		{
			scan.nextToken();
		}
		
		//System.out.println("token num " + scan.getTokenNum());
		System.out.println();
		//pw.close();														//prints/flushes the rest out
	}
}