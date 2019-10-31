//* File:                       Parser.java
// * Course:                    COMP3290
//  * Assignment:               Assignment3
//   * Name:                    Juyong Kim  
//    * Student Number:         c3244203
//     * Purpose:               parser
//      * Note:                 certain errors should just close the program by returning null
//       * 						however certain circumstances lead to weird errors, not enought time to fix them up though
//		  *						using tails to fix left recursion

import java.io.*;
import java.util.*;

public class Parser
{
	private Scanner scanner;				//Compiler Scanner reference for token stream
	private Token currentToken;				//Current Token being analysed
	private Token lookAhead;				//Lookahead toekn for LL(1)
	private OutputController outPut;		//Output controller reference
	private SymbolTable symbolTable;
	private boolean debug = true;

    //Constructor
	public Parser(OutputController outputController)
	{
		outPut = outputController;
		scanner = new Scanner(outPut);
		symbolTable = new SymbolTable(null);
	}

	//main parts of the tree, global, functions, mainbody

	//program instantiates the tree and the requirements
	//<program>     ::=  CD19 <id> <consts> <types> <arrays> <funcs> <mainbody>
	public TreeNode program() throws IOException
	{
		String error = "Invalid program structure.";
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		StRec stRec = new StRec();

		if(debug == true){System.out.println("---------------------------------------------------CheckToken Errors: ");}
		
		//Checks for the cd19 token
		currentToken = scanner.nextToken();

		if (!checkToken(Token.TCD19, "Missing CD19 Token")) 
		{
			if(debug == true){System.out.println("TCD19 error in program line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		//checks for the identifier token
		currentToken = scanner.nextToken();

		if (!checkToken(Token.TIDEN, "Invalid TIDEN Token"))
		{
			if(debug == true){System.out.println("TIDEN error in program line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		} 

		stRec.setName(currentToken.getStr());

		//adds the new requirement to the symbol table,
		stRec.setType("CD19");
		node.setType(stRec);
		symbolTable.put(stRec.getName(), stRec);

		//uses the token then moves to the next
		currentToken = scanner.nextToken();

		//Add to symbol table
		node.setValue(TreeNode.NPROG);
		node.setSymbol(stRec);

		//setting the nodes within the tree
		//learning linkedlists and nodes was useful
		node.setLeft(globals());
		node.setMiddle(funcs());
		node.setRight(mainbody());
		
		return node;
	}
	
	//Globals, set to the left side
	private TreeNode globals() throws IOException
	{
		TreeNode node = new TreeNode(TreeNode.NGLOB, consts(), types(), arrays());

		node.setLeft(consts());
		node.setMiddle(types());
		node.setRight(arrays());

		//if there is nothing just don't return it
		if(node.getLeft() == null && node.getRight() == null && node.getMiddle() == null)
		{
			//System.out.println("no globals");
			return null;
		}

		return node;
	}

	//<consts>      ::=  constants <initlist> | ε
	private TreeNode consts() throws IOException
	{
		if (currentToken.value() != Token.TCONS)
		{
			if(debug == true){System.out.println("TCONS error in consts line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		//Consume token
		currentToken = scanner.nextToken();

		return initlist();
	}

//This might cause issues!!!!
	//<initlist>    ::=  <init> | <init> , <initlist>
	private TreeNode initlist() throws IOException
	{
		TreeNode inn = init();

		//<init> , <initlist>
		if (currentToken.value() == Token.TCOMA)
		{
			currentToken = scanner.nextToken();

			return new TreeNode(TreeNode.NILIST, inn, initlist());
		}

		//<init>
		return inn;
	}

	//<init> ::= <id> = <expr>
	private TreeNode init() throws IOException
	{
		String error = "Invalid Initialisation Constant";
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		StRec stRec = new StRec();

		//check identifier
		if (!checkToken(Token.TIDEN, "Invalid Initialisation: Excpected TIDEN"))
		{
			if(debug == true){System.out.println("TIDEN error in init line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		stRec.setName(currentToken.getStr());
		currentToken = scanner.nextToken();

		//check equals token
		if (!checkToken(Token.TEQUL, "Invalid initialisation: Expected a '=' ")) 
		{
			if(debug == true){System.out.println("TEQUL error in init line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		//if error here check ordering
		currentToken = scanner.nextToken();
		node.setValue(TreeNode.NINIT);
		symbolTable.put(stRec.getName(), stRec);
		//node is no longer a NUNDEF, using new logic to prevent null pointer exceptions
		node.setSymbol(stRec);
		node.setLeft(expr());

		return node;
	}

	//<types> ::= types <typelist> | ε
	private TreeNode types() throws IOException
	{
		if (currentToken.value() != Token.TTYPS)
		{
			if(debug == true){System.out.println("TTYPS error in types line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}

		//Consume token
		currentToken = scanner.nextToken();

		return typelist();
	}

	//<arrays> ::= arrays <arrdecls> | ε
	private TreeNode arrays() throws IOException
	{
		if (currentToken.value() != Token.TARRS)
		{
			if(debug == true){System.out.println("TARRS error in arrays line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}

		//Consume token
		currentToken = scanner.nextToken();

		return arrdecls();
	}	

	//functions set into the middle
	//<funcs>       ::=  <func> <funcs> | ε
	private TreeNode funcs() throws IOException
	{
		TreeNode node = new TreeNode(TreeNode.NFUNCS);

		//if there is nothing just don't return it
		if (currentToken.value() != Token.TFUNC)
		{
			if(debug == true){System.out.println("TFUNC error in funcs line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}

		node.setLeft(func());
		node.setRight(funcs());

		return node;
	}

	//and the mainbody is on the right side of the tree
	//if the main statments within the txt does not exist just returns null and 
	//shuts down the parser, not sure if works to the specs but i think that
	//is how it should be
	//<mainbody>    ::=  main <slist> begin <stats> end CD19 <id>
	private TreeNode mainbody() throws IOException 
	{
		String error = "Invalid mainbody format: ";
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		StRec stRec = new StRec();

		//System.out.println("main "+outPut.getLine()+"charPos"+outPut.getCharPos());
		//checks for the main token
		if (!checkToken(Token.TMAIN, error+"Expecting 'MAIN' keyword"))
		{
			if(debug == true){System.out.println("TMAIN error in mainbody line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		currentToken = scanner.nextToken();

		//Enters left node
		node.setLeft(slist());

		//System.out.println("begin "+outPut.getLine()+"charPos"+outPut.getCharPos());
		//checks for the begin token
		if (!checkToken(Token.TBEGN, error+"Expecting 'BEGIN' keyword"))
		{
			if(debug == true){System.out.println("TBEGN error in mainbody line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		currentToken = scanner.nextToken();
		
		//Enters right node
		node.setRight(stats());

		//System.out.println("end "+outPut.getLine()+"charPos"+outPut.getCharPos());
		//System.out.println("above TEND "+currentToken.getStr()+" token value is "+currentToken.debugString() +lookAhead.getStr());
		//Checks for end token
		if (!checkToken(Token.TEND, error+"Expecting 'END' keyword"))
		{
			if(debug == true){System.out.println("TEND error in mainbody line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		currentToken = scanner.nextToken();

		//System.out.println("cd19 "+outPut.getLine()+"charPos"+outPut.getCharPos());
		//Checks for CD19 token
		if (!checkToken(Token.TCD19, error+"Expecting 'CD19' keyword"))
		{
			if(debug == true){System.out.println("TCD19 error in mainbody line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		currentToken = scanner.nextToken();


		//System.out.println("above tiden "+currentToken.getStr()+" token value is "+currentToken.debugString() +lookAhead.getStr());
		//System.out.println(outPut.getLine()+"charPos"+outPut.getCharPos());
		//Check for identifier token
		if (!checkToken(Token.TIDEN, error+"Expected ID "))
		{
			if(debug == true){System.out.println("TIDEN error in mainbody line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}
		
		//setting TIDEN tokens
		stRec.setName(currentToken.getStr());
		stRec.setType("CD19");

		symbolTable.put(stRec.getName(), stRec);

		//Check for EOF token
		currentToken = scanner.nextToken();

		//System.out.println(outPut.getLine()+"charPos"+outPut.getCharPos());
		if (!checkToken(Token.TEOF, "EOF not found"))
		{
			if(debug == true){System.out.println("TEOF error in mainbody line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		//return NMAIN if no problems occur. pretty sure this should fix the null pointer exceptions
		//since it return unidentified now.
		node.setValue(TreeNode.NMAIN);

		return node;
	}

//error might occur here since  recursion!!!!
	//<slist>       ::=  <sdecl> | <sdecl> , <slist>
	private TreeNode slist() throws IOException
	{
		//Enter left node
		TreeNode sdecimal = sdecl();

		//<sdecl> , <slist>
		if (currentToken.value() == Token.TCOMA)
		{
			currentToken = scanner.nextToken();
			//?????????
			return new TreeNode(TreeNode.NSDLST, sdecimal, slist());
		}

		//<sdecl>
		return sdecimal;
	}

	//<typelist>    ::=  <type> <typelist> | <type>
	private TreeNode typelist() throws IOException
	{
		TreeNode node = type();

		//<type> <typelist>
		if (currentToken.value() == Token.TIDEN)
		{
			return new TreeNode(TreeNode.NILIST, node, typelist());
		}

		//type 
		return node;
	}

	//NRTYPE or NATYPE
	//<type>        ::=  <structid> is <fields> end
	//<type>        ::=  <typeid> is array [ <expr> ] of <structid>
	private TreeNode type() throws IOException
	{
		String error = "Invalid struct or array declaration: ";
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		StRec stRec = new StRec();
		StRec stRec2 = new StRec();

		//Check for identifier token
		if (!checkToken(Token.TIDEN, error+"Expected Type IDENTIFIER "))
		{
			if(debug == true){System.out.println("TIDEN error in type line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}
		stRec.setName(currentToken.getStr());
		currentToken = scanner.nextToken();
		
		//node.setSymbol(stRec);

		//Check for IS token
		if (!checkToken(Token.TIS, error+ "Keyword missing, expecting an IS"))
		{
			if(debug == true){System.out.println("TIS error in type line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}
		currentToken = scanner.nextToken();
		

		//Check if NRTYPE node
		if (currentToken.value() != Token.TARAY) // this is a struct
		{
			stRec.setType("Struct");
			symbolTable.put(stRec.getName(), stRec);
			node.setSymbol(stRec);
			node.setLeft(fields());
			//Check for end token
			if (!checkToken(Token.TEND, error+"keyword missing, expecting 'END' "))
			{
				if(debug == true){System.out.println("TEND error in type line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
				return node;
			}
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NRTYPE);
			node.setType(stRec); // set node type
			currentToken = scanner.nextToken();
			return node;
		}
		//Else NATYPE node
		else //so this is a type
		{
			currentToken = scanner.nextToken();
			stRec.setType("Type");
			symbolTable.put(stRec.getName(), stRec);
			node.setSymbol(stRec);

			//Check for right bracket token
			if (!checkToken(Token.TLBRK, error+"missing a '[' "))
			{
				if(debug == true){System.out.println("TLBRK error in type line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
				return node;
			}

			currentToken = scanner.nextToken();
			node.setLeft(expr());
	
			//Check for left bracket token
			if (!checkToken(Token.TRBRK, error+"missing a ']' "))
			{
				if(debug == true){System.out.println("TRBRK error in type line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
				return node;
			}

			currentToken = scanner.nextToken();
	
			//Check for Tof token
			if (!checkToken(Token.TOF, error+"Keyword missing: Expecting an 'OF' "))
			{
				if(debug == true){System.out.println("TOF error in type line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
				return node;
			}

			currentToken = scanner.nextToken();
			
			//Check for identifier token
			if (!checkToken(Token.TIDEN, error+"Expecting a Struct Identifier"))
			{
				if(debug == true){System.out.println("TIDEN error in type line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
				return node;
			}

			//new type of stRec
			stRec2.setName(currentToken.getStr());
			stRec2.setType("Struct");
			node.setType(stRec2);  // set node type
			symbolTable.put(stRec2.getName(), stRec2);

			//NATYPE node return
			node.setValue(TreeNode.NATYPE);
			currentToken = scanner.nextToken();

			return node;
		}
	}
	
	//<fields>      ::=  <sdecl> | <sdecl> , <fields>
	private TreeNode fields() throws IOException
	{
		TreeNode sdecll = sdecl();

		//<sdecl> , <fields>
		if (currentToken.value() == Token.TCOMA)
		{
			currentToken = scanner.nextToken();
			return new TreeNode(TreeNode.NFLIST, sdecll, fields());
		}

		//<sdecl>
		return sdecll;
	}

	//<sdecl>       ::=  <id> : <stype>
	private TreeNode sdecl() throws IOException
	{
		String error = "Invalid variable declaration: ";
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		StRec stRec = new StRec();
		
		//Check for identifier token
		if (!checkToken(Token.TIDEN, error+"Expected valid Identifier")) 
		{
			if(debug == true){System.out.println("TIDEN error in sdecl line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		stRec.setName(currentToken.getStr());
		currentToken = scanner.nextToken();
		
		//Check for colon token
		if (!checkToken(Token.TCOLN, error+ "expected a ':' ")) 
		{
			if(debug == true){System.out.println("TCOLN error in sdecl line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		currentToken = scanner.nextToken();
		
		//Check for integer|real|boolean token
		if (currentToken.value() == Token.TINTG)
		{
			stRec.setType("integer");
		}
		else if (currentToken.value() == Token.TREAL)
		{
			stRec.setType("real");
		}
		else if (currentToken.value() == Token.TBOOL)
		{
			stRec.setType("boolean");
		}
		else
		{
			if (!checkToken(Token.TINTG, error+"Incorrect expressions used")) 
			{
				if(debug == true){System.out.println("TINTG error in sdecl line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
				return node;
			}
		}

		//setting NSDECL node
		node.setValue(TreeNode.NSDECL);
		node.setSymbol(stRec);
		node.setType(stRec);

		symbolTable.put(stRec.getName(), stRec);
		currentToken = scanner.nextToken();

		//returns NSDECL if everything goes well
		return node;
	}

	//<arrdecls>    ::=  <arrdecl> | <arrdecl> , <arrdecls>
	private TreeNode arrdecls() throws IOException
	{
		TreeNode arrdecimal = arrdecl();

		if (currentToken.value() == Token.TCOMA)
		{
			currentToken = scanner.nextToken();

			return new TreeNode(TreeNode.NALIST, arrdecimal, arrdecls());
		}

		return arrdecimal;
	}

	//<arrdecl>     ::=  <id> : <typeid>
	private TreeNode arrdecl() throws IOException
	{
		String error = "Invalid array declaration: ";
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		StRec stRec = new StRec();

		//Check for identifier
		if (!checkToken(Token.TIDEN, error+"Expected valid Identifier"))  
		{
			if(debug == true){System.out.println("TIDEN error in arrdecl line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		stRec.setName(currentToken.getStr());
		currentToken = scanner.nextToken();
		
		//Check for colon token
		if (!checkToken(Token.TCOLN, error+"Expect ':' "))  
		{
			if(debug == true){System.out.println("TCOLN error in arrdecl line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		currentToken = scanner.nextToken();
		
		if (!checkToken(Token.TIDEN, error+"Expect TypeID name."))  
		{
			if(debug == true){System.out.println("TIDEN error in arrdecl line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		//setting node into arrdecimal then returning
		node.setValue(TreeNode.NARRD);
		stRec.setType(currentToken.getStr());
		node.setType(stRec);
		node.setSymbol(stRec);

		//update symbol table
		symbolTable.put(stRec.getName(), stRec);
		currentToken = scanner.nextToken();

		return node;
	}

	//debugging this is not fun......... at all
	//<funcs> ::= <func> <funcs> | ε
	private TreeNode func() throws IOException
	{
		String error = "Invalid function declaration: ";
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		StRec stRec = new StRec();

		//no func, returns null
		if (!checkToken(Token.TFUNC, error+"Keyword missing: expecting FUNCTION"))  
		{
			if(debug == true){System.out.println("TFUNC error in func line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		currentToken = scanner.nextToken();
		
		//no identifier returns null
		if (!checkToken(Token.TIDEN, error+"Expecting Identifier"))  
		{
			if(debug == true){System.out.println("TIDEN error in func line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		stRec.setName(currentToken.getStr());
		currentToken = scanner.nextToken();
		
		//no left parenthesis, return null
		if (!checkToken(Token.TLPAR, error+"Missing a '('")) 
		{
			if(debug == true){System.out.println("TLPAR error in func line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		currentToken = scanner.nextToken();
		//setting the left bracket
		node.setLeft(plist());

		//Check for right paranthesis token
		if (!checkToken(Token.TRPAR, error+"Missing a ')'"))  
		{
			if(debug == true){System.out.println("TRPAR error in func line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		currentToken = scanner.nextToken();
		
		//Check for colon token
		if (!checkToken(Token.TCOLN, error+"Missing a ':'"))  
		{
			if(debug == true){System.out.println("TCOLN error in func line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		currentToken = scanner.nextToken();
		
		//Check for rtype
		if (currentToken.value() == Token.TINTG)
		{
			stRec.setType("integer");
		}
		else if (currentToken.value() == Token.TREAL)
		{
			stRec.setType("real");
		}
		else if (currentToken.value() == Token.TBOOL)
		{
			stRec.setType("boolean");
		}
		else if (currentToken.value() == Token.TVOID)  
		{
			//I HATE COMPILER DESIGN BECAUSE I CANNOT EAT SLEEP OR SHIT WITHOUT 
			//THINKING OF WHY AM I DOING THIS TO MYSELF
			stRec.setType("void");
		}
		else
		{
			if (!checkToken(Token.TINTG, error+"invalid Expression used to describe func"))  
			{
				if(debug == true){System.out.println("TTNTG error in func line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
				currentToken = scanner.nextToken();
				return node;
			}
		}
		
		//setting the locals if it isn't an rtype
		currentToken = scanner.getToken();
		symbolTable.put(stRec.getName(), stRec);
		node.setMiddle(locals());

		//Check for begin token
		if (!checkToken(Token.TBEGN, error+"Missing a BEGIN"))  
		{
			if(debug == true){System.out.println("TBEGN error in func line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		currentToken = scanner.nextToken();
		node.setRight(stats());

		//Check for end token
		if (!checkToken(Token.TEND, error+"Missing an Happily ever after sorta END"))  
		{
			if(debug == true){System.out.println("TEND error in func line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		//returning the NFUND
		node.setValue(TreeNode.NFUND);
		currentToken = scanner.getToken();
		node.setSymbol(stRec);
		node.setType(stRec); 

		return node;
	}

	//<plist>       ::=  <params> | ε
	private TreeNode plist() throws IOException
	{
		if (currentToken.value() == Token.TIDEN || currentToken.value() == Token.TCONS)
		{
			return params();
		}

		if(debug == true){System.out.println("non error: plist threw ε line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
		//return node;		//should be throwing null i think		
		return null;
	}

	//<params>      ::=  <param> , <params> | <param>
	private	TreeNode params() throws IOException
	{
		TreeNode parameter = param();

		if (currentToken.value() == Token.TCOMA)
		{
			currentToken = scanner.getToken();

			return new TreeNode(TreeNode.NPLIST, parameter, params());
		}

		return parameter;
	}

	//<param>       ::=  <sdecl> | <arrdecl> | const <arrdecl>
	private TreeNode param() throws IOException
	{
		TreeNode node = new TreeNode(TreeNode.NUNDEF);

		if (currentToken.value() == Token.TCONS)
		{
			//Consume token
			currentToken = scanner.nextToken();
			//set node to ARRC instead of undefined
			node.setValue(TreeNode.NARRC);
			node.setLeft(arrdecl());

			return node;
		}

		//The value should either be NSIMP or NARRP
		TreeNode check = decl();

		if (check.getValue() == TreeNode.NARRD)
		{
			node.setValue(TreeNode.NARRP);
		}
		else if (check.getValue() == TreeNode.NSDECL)
		{
			node.setValue(TreeNode.NSIMP);
		}
		else
		{
			if(debug == true){System.out.println("non error: param returning null line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}
		node.setLeft(check);
		return node;
	}

	//<funcbody>    ::=  <locals> begin <stats> end
	//being done elsewhere

	//<locals>      ::=  <dlist> | ε
	private TreeNode locals() throws IOException
	{
		if (currentToken.value() != Token.TIDEN)
		{
			if(debug == true){System.out.println("non error: locals returning null line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}

		return dlist();
	}

	//<dlist>       ::=  <decl> | <decl> , <dlist>
	private TreeNode dlist() throws IOException
	{
		TreeNode decimal = decl();

		if (currentToken.value() == Token.TCOMA)
		{
			currentToken = scanner.getToken();

			return new TreeNode(TreeNode.NDLIST, decimal, dlist());
		}

		return decimal;
	}

	//<decl>        ::=  <sdecl> | <arrdecl>
	//NSDECL $ NARRD
	private TreeNode decl() throws IOException
	{
		String error = "Invalid array or variable declaration: ";
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		StRec stRec = new StRec();
		stRec.setName(currentToken.getStr());
		currentToken = scanner.nextToken();
//Switched around colon and identifier!!!!
		//Check for colon token
		if (!checkToken(Token.TCOLN, error+"Missing a ':' ")) 
		{
			if(debug == true){System.out.println("TCOLN error in decl line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return node;
		}

		currentToken = scanner.nextToken();

		//Check for identifier token
		if (currentToken.value() == Token.TIDEN) 
		{
			// NARRD
			node.setValue(TreeNode.NARRD);
			stRec.setType(currentToken.getStr());
		}
		else	// NSDECL
		{
			//Check for rtype
			if (currentToken.value() == Token.TINTG)
			{
				//node.setValue(TreeNode.NSDECL);
				stRec.setType("integer");
			}
			else if (currentToken.value() == Token.TREAL)
			{
				//node.setValue(TreeNode.NSDECL);
				stRec.setType("real");
			}
			else if (currentToken.value() == Token.TBOOL)
			{
				//node.setValue(TreeNode.NSDECL);
				stRec.setType("boolean");
			}
//			else if (currentToken.value() == Token.TIDEN)  
//			{
//				node.setValue(TreeNode.NARRD);
//				stRec.setType(currentToken.getStr());
//			}
			else
			{
				if (!checkToken(Token.TINTG, error+"Incorrect expression used for decl()")) 
				{
					if(debug == true){System.out.println("TINTG error in decl line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
					currentToken = scanner.nextToken();
					//System.out.println("the node is:"+node.getString());
					return node;
				}
			}
			//setting the node here
			node.setValue(TreeNode.NSDECL);
		}

		//System.out.println("----the node is:"+node.getString());
		node.setType(stRec);
		node.setSymbol(stRec);

		symbolTable.put(stRec.getName(), stRec);
		currentToken = scanner.getToken();

		return node;
	}

	//<stats>       ::=  <stat> ; <stats> | <strstat> <stats> | <stat>; | <strstat>
	private TreeNode stats() throws IOException
	{
		// <stat> or <strstat>
		if(currentToken.value() == Token.TFOR || currentToken.value() == Token.TIFTH)
		{
			TreeNode node = strstat();
			return statsHelpFunc(node);
		}
		else
		{
			TreeNode node = stat();
			// needs a  TSEMI token
			if(!checkToken(Token.TSEMI, "Invalid statements declaration: Expected a ';'." ))
			{
				return node;
			}

			currentToken = scanner.getToken();
			
			return statsHelpFunc(node);
		}
	}

	// NSTATS
	// <statsHelpFunc> ::=  <stats> | ε
	private TreeNode statsHelpFunc(TreeNode node)
	{
		// stats: for or if or repeat or id or input or print or printline or return
		// <stat> or <strstat>
		if(currentToken.value() == Token.TFOR
		|| currentToken.value() == Token.TIFTH
		|| currentToken.value() == Token.TREPT
		|| currentToken.value() == Token.TRETN
		|| currentToken.value() == Token.TINPT
		|| currentToken.value() == Token.TPRIN
		|| currentToken.value() == Token.TPRLN
		|| currentToken.value() == Token.TIDEN)
		{
			return new TreeNode(TreeNode.NSTATS, node, stats());
		}
		else
		{
			return node;
		}
	}
	//<stype>       ::=  integer | real | boolean
//	//<stats>       ::=  <stat> ; <stats> | <strstat> <stats> | <stat>; | <strstat>
//	private TreeNode stats() throws IOException
//	{
//		String error = "Invalid statements declaration.";
//		//First non-terminal values expectede
//		int first[] = 
//		{
//			Token.TREPT, Token.TIDEN, Token.TINPT, Token.TPRIN, Token.TPRLN, Token.TRETN, Token.TFOR, Token.TIFTH
//		};
//
//		TreeNode node = new TreeNode(TreeNode.NSTATS);
//		TreeNode temp;
//
//		//Check for next token to decide which non-terminal to enter
//		//Enter strstat node
//		if (currentToken.value() == Token.TFOR || currentToken.value() == Token.TIFTH)
//		{
//			temp = strstat();
//			//Check if next node is stats or empty string
//			for (int i = 0; i < first.length; i++)
//			{
//				if (currentToken.value() == first[i])
//				{
//					node.setLeft(temp);
//					node.setRight(stats());
//					return node;
//				}
//			}
//			return temp;
//		}
//		//Enter stat node
//		else
//		{
//			temp = stat();
//			//Check for semicolon token
//			if (!checkToken(Token.TSEMI, error + currentToken.getStr()))
//			{
//				if(debug == true){System.out.println("TSEMI error in stats line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
//				return null;
//			}
//
//			currentToken = scanner.nextToken();
//			
//
//			//Check if next node is stats or empty string
//			for (int i = 0; i < first.length; i++)
//			{
//				if (currentToken.value() == first[i])
//				{
//					node.setLeft(temp);
//					node.setRight(stats());
//					return node;
//				}
//			}
//			return temp;
//		}
//	}

	//<strstat>     ::=  <forstat> | <ifstat>
	private TreeNode strstat() throws IOException
	{	
		//Check for "if" or "for"
		if (currentToken.value() == Token.TFOR)
		{
			return forstat();
		}
		else
		{
			return ifstat();
		}
	}

	//<stat>        ::=  <repstat> | <asgnstat> | <iostat> | <callstat> | <returnstat>
	private TreeNode stat() throws IOException
	{
		String error = "Invalid statement declaration.";
		//Check for identifier token

		//Lookahead for next non terminal
		if (currentToken.value() == Token.TREPT)
		{
			return repstat();
		}
		else if (currentToken.value() == Token.TRETN)
		{
			return returnstat();
		}
		else if (currentToken.value() == Token.TINPT || currentToken.value() == Token.TPRIN || currentToken.value() == Token.TPRLN)
		{
			return iostat();
		}
		else
		{
			if (!checkToken(Token.TIDEN, error)) 
			{
				if(debug == true){System.out.println("TIDEN error in stat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
				return null;
			}
			if (lookAhead.value() == Token.TLPAR)
			{
				return callstat();
			}
			else
			{
				return asgnstat();
			}
		}
	}

	//<forstat>     ::=  for ( <asgnlist> ; <bool> ) <stats> end
	private TreeNode forstat() throws IOException
	{
		String error = "Invalid For structure.";
		TreeNode node = new TreeNode(TreeNode.NFOR);
		//Check for For token
		if (!checkToken(Token.TFOR, error))
		{
			if(debug == true){System.out.println("TFOR error in forstat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		currentToken = scanner.nextToken();
		

		//Check for left paranthesis
		if (!checkToken(Token.TLPAR, error)) 
		{
			if(debug == true){System.out.println("TLPAR error in forstat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		currentToken = scanner.nextToken();
		

		node.setLeft(asgnlist());

		//Check for semicolon token
		if (!checkToken(Token.TSEMI, error))
		{
			if(debug == true){System.out.println("TSEMI error in forstat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		currentToken = scanner.nextToken();
		

		node.setMiddle(bool());

		//Check for right paranthesis
		if (!checkToken(Token.TRPAR, error))
		{
			if(debug == true){System.out.println("TRPAR error in forstat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		currentToken = scanner.nextToken();
		

		node.setRight(stats());

		//Check for end
		if (!checkToken(Token.TEND, error)) 
		{
			if(debug == true){System.out.println("TEND error in forstat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		currentToken = scanner.nextToken();
		

		return node;
	}

	//<repstat>     ::=  repeat ( <asgnlist> ) <stats> until <bool>
	private TreeNode repstat() throws IOException
	{
		String error = "Invalid repeat structure.";
		TreeNode node = new TreeNode(TreeNode.NREPT);

		//Check for repeat token
		if (!checkToken(Token.TREPT, error))
		{
			if(debug == true){System.out.println("TREPT error in repstat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		currentToken = scanner.nextToken();
		

		//Check for left paranthesis
		if (!checkToken(Token.TLPAR, error)) 
		{
			if(debug == true){System.out.println("TLPAR error in repstat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		currentToken = scanner.nextToken();
		

		node.setLeft(asgnlist());

		//Check for right paranthesis
		if (!checkToken(Token.TRPAR, error)) 
		{
			if(debug == true){System.out.println("TRPAR error in repstat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		currentToken = scanner.nextToken();
		

		node.setMiddle(stats());

		//Check for until
		if (!checkToken(Token.TUNTL, error))
		{
			if(debug == true){System.out.println("TUNTL error in repstat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		currentToken = scanner.nextToken();
		

		node.setRight(bool());

		return node;
	}

	//<asgnlist>    ::=  <alist> | ε
	private TreeNode asgnlist() throws IOException
	{
		if (currentToken.value() != Token.TIDEN)
		{
			if(debug == true){System.out.println("non error: asgnlist returning null line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		return alist();
	}

	//<alist>       ::=  <asgnstat> | <asgnstat> , <alist>
	private TreeNode alist() throws IOException
	{
		TreeNode node = new TreeNode(TreeNode.NASGNS);
		TreeNode temp = asgnstat();

		if (currentToken.value() == Token.TCOMA)
		{
			return temp;
		}

		//Consume token
		currentToken = scanner.nextToken();
		

		node.setLeft(temp);
		node.setRight(alist());

		return node;
	}

	//<ifstat>      ::=  if ( <bool> ) <stats> end
	private TreeNode ifstat() throws IOException
	{
		String error = "Invalid if statement.";
		//Undefined tree node until proper selection
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		//Check for IF token
		if (!checkToken(Token.TIFTH, error)) 
		{
			if(debug == true){System.out.println("TIFTH error in ifstat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		

		//Check for left paranthesis
		if (!checkToken(Token.TLPAR, error)) 
		{
			if(debug == true){System.out.println("TLPAR error in ifstat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		currentToken = scanner.nextToken();
		

		node.setLeft(bool());

		//Check for right paranthesis
		if (!checkToken(Token.TRPAR, error))
		{
			if(debug == true){System.out.println("TRPAR error in ifstat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		currentToken = scanner.nextToken();
		

		node.setMiddle(stats());

		//Check for end or else
		if (currentToken.value() == Token.TEND)
		{
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NIFTH);
			node.setRight(node.getMiddle());
			node.setMiddle(null);
			return node;
		}
		else
		{
			//Check for else
			if (!checkToken(Token.TELSE, error)) 
			{
				if(debug == true){System.out.println("TELSE error in ifstat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
				return null;
			}
	
			currentToken = scanner.nextToken();
			

			node.setRight(stats());

			//Check for end
			if (!checkToken(Token.TEND, error))
			{
				if(debug == true){System.out.println("TEND error in ifstat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
				return null;
			}
			currentToken = scanner.nextToken();
			

			node.setValue(TreeNode.NIFTE);
			return node;
		}

	}

	//<asgnstat>    ::=  <var> <asgnop> <bool>
	private TreeNode asgnstat() throws IOException
	{
		TreeNode variable = var();
		TreeNode assign = asgnop();
		TreeNode bools = bool();

		assign.setLeft(variable);
		assign.setRight(bools);

		return assign;
	}

	//<asgnop>      ::=  = | += | -= | *= | /=
	private TreeNode asgnop() throws IOException
	{
		String error = "Invalid assignment.";
		TreeNode node  = new TreeNode(TreeNode.NUNDEF);

		if (currentToken.value() == Token.TEQUL)
		{
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NASGN);
		}
		else if (currentToken.value() == Token.TPLEQ)
		{
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NPLEQ);
		}
		else if (currentToken.value() == Token.TMNEQ)
		{
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NMNEQ);
		}
		else if (currentToken.value() == Token.TDVEQ)
		{
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NDVEQ);
		}
		else if (currentToken.value() == Token.TSTEQ)
		{
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NSTEQ);
		}
		else
		{
			checkToken(Token.TEQUL, error);
			if(debug == true){System.out.println("non error: returning null in asgnop line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		return node;
	}

	//<iostat>      ::=  input <vlist> | print <prlist> | printline <prlist>
	private TreeNode iostat() throws IOException
	{
		String error = "Invalid input/output statement.";
		TreeNode node = new TreeNode(TreeNode.NUNDEF);

		if (currentToken.value() == Token.TINPT)
		{
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NINPUT);
			node.setLeft(vlist());
		}
		else if (currentToken.value() == Token.TPRIN) 
		{
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NPRINT);
			node.setLeft(prlist());
		}
		else if (currentToken.value() == Token.TPRLN)
		{
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NPRLN);
			node.setLeft(prlist());
		}
		else
		{
			checkToken(Token.TINPT, error);
			if(debug == true){System.out.println("non error: returning null in iostat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		return node;
	}

	//<callstat>    ::=  <id> ( <elist> ) | <id> ( )
	private TreeNode callstat() throws IOException
	{
		String error = "Invalid call statement.";
		TreeNode node = new TreeNode(TreeNode.NCALL);
		StRec stRec = new StRec();

		//Check for identifier token
		if (!checkToken(Token.TIDEN, error)) 
		{
			if(debug == true){System.out.println("TIDEN error in callstat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		stRec.setName(currentToken.getStr());
		currentToken = scanner.nextToken();
		

		//Check for left paranthesis
		if (!checkToken(Token.TLPAR, error)) 
		{
			if(debug == true){System.out.println("TLPAR error in callstat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		currentToken = scanner.nextToken();
		

		if (currentToken.value() == Token.TRPAR)
		{
			//Consume token
			currentToken = scanner.nextToken();
			
		}
		else
		{
			node.setLeft(elist());
			//Check for right paranthesis
			if (!checkToken(Token.TRPAR, error)) 
			{
				if(debug == true){System.out.println("TRPAR error in callstat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
				return null;
			}
			currentToken = scanner.nextToken();
			
		}

		node.setSymbol(stRec);
		symbolTable.put(stRec.getName(), stRec);
		return node;
	}

	//<returnstat>  ::=  return | return <expr>
	private TreeNode returnstat() throws IOException
	{
		String error = "Invalid return statement.";
		//First non-terminal values expectede
		int first[] = {
			Token.TIDEN, Token.TILIT, Token.TFLIT, Token.TTRUE, Token.TFALS, Token.TLPAR
		};
		TreeNode node = new TreeNode(TreeNode.NRETN);

		//Check for return token
		if (!checkToken(Token.TRETN, error))
		{
			if(debug == true){System.out.println("TRETN error in returnstat line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		currentToken = scanner.nextToken();
		

		//Check for return or expr node
		for (int i = 0; i < first.length; i++)
		{
			if (currentToken.value() == first[i])
			{
				node.setLeft(expr());
			}
		}

		return node;
	}

	//<vlist>       ::=  <var> , <vlist> | <var>
	private TreeNode vlist() throws IOException
	{
		TreeNode node = new TreeNode(TreeNode.NVLIST);
		TreeNode temp = var();

		if (currentToken.value() == Token.TCOMA)
		{
			return temp;
		}
		//Consume Token
		currentToken = scanner.nextToken();
		

		node.setLeft(temp);
		node.setRight(vlist());

		return node;
	}

	//<var>         ::=  <id> | <id>[<expr>] . <id>
	private TreeNode var() throws IOException
	{
		String error = "Invalid variable declaration.";
		if (!checkToken(Token.TIDEN, error)) 
		{
			if(debug == true){System.out.println("TIDEN error in var line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		StRec stRec = new StRec(currentToken.getStr());
		currentToken = scanner.nextToken();
		

		//Check for NSIMV or NARRV
		if (currentToken.value() == Token.TLBRK)
		{
			TreeNode node = new TreeNode(TreeNode.NARRV);
			//Consume token
			currentToken = scanner.nextToken();
			

			node.setLeft(expr());

			//Check for right bracket token and consume
			if (!checkToken(Token.TRBRK, error)) 
			{
				if(debug == true){System.out.println("TRBRK error in var line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
				return null;
			}
			currentToken = scanner.nextToken();
			

			//Check for dot token
			if (!checkToken(Token.TDOT, error)) 
			{
				if(debug == true){System.out.println("TDOT error in var line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
				return null;
			}
			

			//Check for identifier token and consume
			if (!checkToken(Token.TIDEN, error)) 
			{
				if(debug == true){System.out.println("TIDEN error in var line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
				return null;
			}
			currentToken = scanner.nextToken();
			

			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);

			return node;
		}
		//NSIMV
		else
		{
			TreeNode node = new TreeNode(TreeNode.NSIMV);
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}
	}

	//<elist>       ::=  <bool> , <elist> | <bool>
	private TreeNode elist() throws IOException
	{
		TreeNode node = new TreeNode(TreeNode.NEXPL);
		TreeNode temp = bool();

		if (currentToken.value() == Token.TCOMA)
		{
			return temp;
		}
		//Consume Token
		currentToken = scanner.nextToken();
		

		node.setLeft(temp);
		node.setRight(elist());

		return node;
	}

	//<bool> ::= <bool><logop> <rel> | <rel>
	private TreeNode bool() throws IOException
	{
		TreeNode temp;
		temp = rel();
		return booltail(temp);
	}

	//properly coded in ll(1) format
	private TreeNode booltail(TreeNode left) throws IOException
	{
		TreeNode parent;
		if (currentToken.value() == Token.TAND || currentToken.value() == Token.TOR || currentToken.value() == Token.TXOR)
		{
			parent = logop();
			parent.setLeft(left);
			parent.setRight(rel());
			return booltail(parent);
		}	
		else
		{
			return left;
		}
	}

	//<rel>         ::=  not <expr> <relop> <expr> | <expr> <relop> <expr> | <expr>
	private TreeNode rel() throws IOException
	{
		TreeNode node = new TreeNode(TreeNode.NNOT);
		TreeNode temp, temp2;

		if (currentToken.value() == Token.TNOT)
		{
			//Consume Token
			currentToken = scanner.nextToken();
			

			node.setLeft(expr());
			node.setMiddle(relop());
			node.setRight(expr());

			return node;
		}

		temp = expr();
		if (currentToken.value() == Token.TEQEQ || currentToken.value() == Token.TNEQL || currentToken.value() == Token.TGEQL ||  currentToken.value() == Token.TLEQL || currentToken.value() == Token.TGRTR || currentToken.value() == Token.TLESS)
		{
			temp2 = relop();
			temp2.setLeft(temp);
			temp2.setRight(expr());
			return temp2;
		}
		else
		{
			return temp;
		}
	}

	//<logop>       ::=  and | or | xor
	private TreeNode logop() throws IOException
	{
		String error = "Invalid logic operation.";
		TreeNode node  = new TreeNode(TreeNode.NUNDEF);

		if (currentToken.value() == Token.TAND)
		{
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NAND);
		}
		else if (currentToken.value() == Token.TOR)
		{
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NOR);
		}
		else if (currentToken.value() == Token.TXOR)
		{
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NXOR);
		}
		else
		{
			checkToken(Token.TAND, error);
			if(debug == true){System.out.println("non error: returning null in logop line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		return node;
	}

	//<relop>       ::=  == | != | > | <= | < | >=
	private TreeNode relop() throws IOException
	{
		String error = "Invalid relation operation.";
		TreeNode node  = new TreeNode(TreeNode.NUNDEF);

		if (currentToken.value() == Token.TEQEQ)
		{
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NEQL);
		}
		else if (currentToken.value() == Token.TNEQL)
		{
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NNEQ);
		}
		else if (currentToken.value() == Token.TGRTR)
		{
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NGRT);
		}
		else if (currentToken.value() == Token.TLEQL)
		{
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NLEQ);
		}
		else if (currentToken.value() == Token.TLESS)
		{
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NLSS);
		}
		else if (currentToken.value() == Token.TGEQL)
		{
			currentToken = scanner.nextToken();
			
			node.setValue(TreeNode.NGEQ);
		}
		else
		{
			checkToken(Token.TEQEQ, error);
			if(debug == true){System.out.println("non error: returning null in relop line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		return node;
	}

	//<expr>        ::=  <expr> + <fact> | <expr> - <fact> | <fact>
	private TreeNode expr() throws IOException
	{
		TreeNode temp;
		temp = term();
		return exprtail(temp);
	}

	//expr llr(1) solution
	private TreeNode exprtail(TreeNode left) throws IOException
	{
		TreeNode parent;
		if (currentToken.value() == Token.TPLUS)
		{
			currentToken = scanner.nextToken();
			
			parent = new TreeNode(TreeNode.NADD);
			parent.setLeft(left);
			parent.setRight(term());
			return(exprtail(parent));
		}
		else if (currentToken.value() == Token.TMINS)
		{
			currentToken = scanner.nextToken();
			
			parent = new TreeNode(TreeNode.NSUB);
			parent.setLeft(left);
			parent.setRight(term());
			return(exprtail(parent));
		}
		else
		{
			return left;
		}
		
	}

	//<fact>        ::=  <fact> * <term> | <fact> / <term> | <fact> % <term> | <term>
	private TreeNode fact() throws IOException
	{
		TreeNode temp;
		temp = exponent();
		return facttail(temp);
	}

	//fact llr(1) solution
	private TreeNode facttail(TreeNode left) throws IOException
	{
		TreeNode parent;
		if (currentToken.value() == Token.TCART)
		{
			currentToken = scanner.nextToken();
			
			parent = new TreeNode(TreeNode.NPOW);
			parent.setLeft(left);
			parent.setRight(exponent());
			return(facttail(parent));
		}
		else
		{
			return left;
		}
	}

	//<term>        ::=  <term> ^ <exponent> | <exponent>
	private TreeNode term() throws IOException
	{
		TreeNode temp;
		temp = fact();
		return termTail(temp);
	}

	//termTail llr(1) solution
	private TreeNode termTail(TreeNode left) throws IOException
	{
		TreeNode parent;
		if (currentToken.value() == Token.TSTAR)
		{
			currentToken = scanner.nextToken();
			
			parent = new TreeNode(TreeNode.NMUL);
			parent.setLeft(left);
			parent.setRight(fact());
			return(exprtail(parent));
		}
		else if (currentToken.value() == Token.TDIVD)
		{
			currentToken = scanner.nextToken();
			
			parent = new TreeNode(TreeNode.NDIV);
			parent.setLeft(left);
			parent.setRight(fact());
			return(exprtail(parent));
		}
		else if (currentToken.value() == Token.TPERC)
		{
			currentToken = scanner.nextToken();
			
			parent = new TreeNode(TreeNode.NMOD);
			parent.setLeft(left);
			parent.setRight(fact());
			return(exprtail(parent));
		}
		else
		{
			return left;
		}
		
	}

	//<exponent>    ::=  <var> | <intlit> | <reallit> | <fncall> | true | false
	//<exponent>    ::=  ( <bool> )
	private TreeNode exponent() throws IOException
	{
		String error = "Invalid exponent operation.";
		TreeNode node = new TreeNode(TreeNode.NUNDEF);
		StRec stRec = new StRec();
		if (currentToken.value() == Token.TILIT)
		{
			node.setValue(TreeNode.NILIT);
			stRec.setName(currentToken.getStr());
			//Consume token
			currentToken = scanner.nextToken();
			
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}
		else if (currentToken.value() == Token.TFLIT)
		{
			node.setValue(TreeNode.NFLIT);
			stRec.setName(currentToken.getStr());
			//Consume token
			currentToken = scanner.nextToken();
			
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}
		else if (currentToken.value() == Token.TIDEN)
		{
			//check for fncall
			if (lookAhead.value() == Token.TLPAR)
			{
				return fncall();
			}
			else
			{
				return var();
			}
		}
		else if (currentToken.value() == Token.TTRUE)
		{
			node.setValue(TreeNode.NTRUE);
			stRec.setName(currentToken.getStr());
			//Consume token
			currentToken = scanner.nextToken();
			
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}
		else if (currentToken.value() == Token.TFALS)
		{
			node.setValue(TreeNode.NFALS);
			stRec.setName(currentToken.getStr());
			//Consume token
			currentToken = scanner.nextToken();
			
			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);
			return node;
		}
		else
		{
			//Check for left paranthesis token and consume
			if (!checkToken(Token.TLPAR, error)) 
			{
				if(debug == true){System.out.println("TLPAR error in exponent line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
				return null;
			}
			currentToken = scanner.nextToken();
			

			TreeNode temp;
			temp = bool();

			//Check for right paranthesis token and consume
			if (!checkToken(Token.TRPAR, error)) 
			{
				if(debug == true){System.out.println("TRPAR error in exponent line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
				return null;
			}
			currentToken = scanner.nextToken();
			

			return temp;
		}
	}

	//<fncall>      ::=  <id> ( <elist> ) | <id> ( )
	private TreeNode fncall() throws IOException
	{
		String error = "Invalid function call.";
		TreeNode node = new TreeNode(TreeNode.NFCALL);
		StRec stRec = new StRec();

		//Check for identifier token
		if (!checkToken(Token.TIDEN, error)) 
		{
			if(debug == true){System.out.println("TIDEN error in fncall line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		stRec.setName(currentToken.getStr());
		currentToken = scanner.nextToken();
		

		//Check for left paranthesis token
		if (!checkToken(Token.TLPAR, error)) 
		{
			if(debug == true){System.out.println("TLPAR error in fncall line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		currentToken = scanner.nextToken();
		

		if (currentToken.value() == Token.TRPAR)
		{
			node.setLeft(elist());
		}

		//Check for right paranthesis token
		if (!checkToken(Token.TRPAR, error))
		{
			if(debug == true){System.out.println("TRPAR error in fncall line: "+outPut.getLine()+" charPos "+outPut.getCharPos());}
			return null;
		}
		currentToken = scanner.nextToken();
		

		return node;
	}

	//<prlist>      ::=  <printitem> , <prlist> | <printitem>
	private TreeNode prlist() throws IOException
	{
		TreeNode node = new TreeNode(TreeNode.NPRLST);
		TreeNode temp = printitem();

		//System.out.println("meep1");
		if (currentToken.value() == Token.TCOMA)
		{
			return temp;
		}

		//Consume token
		currentToken = scanner.nextToken();
		

		node.setLeft(temp);
		node.setRight(prlist());

		return node;
	}

	//<printitem>   ::=  <expr> | <string>
	private TreeNode printitem() throws IOException
	{
		//System.out.println("meep2");
		if (currentToken.value() == Token.TSTRG)
		{
			TreeNode node = new TreeNode(TreeNode.NSTRG);
			StRec stRec = new StRec(currentToken.getStr());

			currentToken = scanner.nextToken();
			

			node.setSymbol(stRec);
			symbolTable.put(stRec.getName(), stRec);

			return node;
		}
		else
		{
			return expr();
		}
	}

	//Prints the appropriate error message 
	private boolean checkToken(int expected, String message)
	{
		//System.out.println("bingo1");										//bingos were used for debugging purposes
		if (currentToken.value() != expected)
		{	
			//System.out.println("bingo2");
			if (currentToken.value() == Token.TUNDF)
			{
				//System.out.println("bingo3");
				outPut.setError("Lexical Error: " + currentToken.getStr());
			}
			else
			{
				//System.out.println("bingo4");
				outPut.setError("Syntax Error: " + message);
			}
			//System.out.println("bingo5");
			return false;
		}
		//System.out.println("bingo6");
		return true;
	}

}