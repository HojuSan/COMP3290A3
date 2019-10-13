//* File:                       SymbolTable.java
// * Course:                    COMP3290
//  * Assignment:               Assignment3
//   * Name:                    Juyong Kim  
//    * Student Number:         c3244203
//     * Purpose:               Symbol Table saves the STREC stuff in a hashmap
//      * Note:                 null

import java.util.*;

public class SymbolTable
{
	//variables
	private HashMap<String, StRec> table;
	protected SymbolTable prev;

	//constructor
	public SymbolTable(SymbolTable prev)
	{
		table = new HashMap<>();
		this.prev = prev;
	}

	//setters
	public void put(String s, StRec sym)
	{
		table.put(s, sym);
	}

	//getters
	public StRec get(String s)
	{
		for (SymbolTable e = this; e != null; e = e.prev)
		{
			StRec found = e.table.get(s);
			if (found != null)
			{
				return found;
			}
		}
		return null;
	}

}