//* File:                       Parser.java
// * Course:                    COMP3290
//  * Assignment:               Assignment3
//   * Name:                    Juyong Kim  
//    * Student Number:         c3244203
//     * Purpose:               parser
//      * Note:                 Look Below

import java.io.*;
import java.util.*;

public class Parser
{
	private Scanner scanner;				//Compiler Scanner reference for token stream
	private Token currentToken;				//Current Token being analysed
	private Token lookahead;				//Lookahead toekn for LL(1)
	private OutputController outPut;		//Output controller reference
	private SymbolTable symbolTable;

    //Constructor
	public Parser(OutputController outputController)
	{
		outPut = outputController;
		scanner = new Scanner(outPut);
		symbolTable = new SymbolTable(null);
	}
}