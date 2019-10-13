//* File:                       StRec.java
// * Course:                    COMP3290
//  * Assignment:               Assignment3
//   * Name:                    Juyong Kim  
//    * Student Number:         c3244203
//     * Purpose:               Syntax Tree Requirements
//      * Note:                 Pretty simple class

public class StRec
{
	//variables
	private String name;
	private String type;

	//constructor
	public StRec()
	{
		name = null;
		type = null;
	}

	//setters
	public StRec(String name)
	{
		this();
		this.name = name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public void setType(String type)
	{
		this.type = type;
	}

	//getters
	public String getName()
	{
		return name;
	}
	public String getType()
	{
		return type;
	}
}