//* File:                       StRec.java
// * Course:                    COMP3290
//  * Assignment:               Assignment3
//   * Name:                    Juyong Kim  
//    * Student Number:         c3244203
//     * Purpose:               Statement Requirement
//      * Note:                 Look Below

public class StRec
{
	private String name;
	private String type;

	public StRec()
	{
		name = null;
		type = null;
	}

	public StRec(String name)
	{
		this();
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public String getType()
	{
		return type;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setType(String type)
	{
		this.type = type;
	}
}