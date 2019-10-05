//* File:                       SymbolTable.java
// * Course:                    COMP3290
//  * Assignment:               Assignment3
//   * Name:                    Juyong Kim  
//    * Student Number:         c3244203
//     * Purpose:               Symbol Table
//      * Note:                 Look Below

import java.util.*;

public class SymbolTable
{
	private HashMap<String, StRec> table;
	protected SymbolTable prev;

	public SymbolTable (SymbolTable prev)
	{
		table = new HashMap<>();
		this.prev = prev;
	}

	public void put (String s, StRec sym)
	{
		table.put(s, sym);
	}

	public StRec get (String s)
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