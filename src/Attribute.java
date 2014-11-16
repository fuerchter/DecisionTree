
public class Attribute
{
	private String name;
	private String[] possibleValues;
	
	public Attribute(String name, String[] possibleValues)
	{
		this.name=name;
		this.possibleValues=possibleValues;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String[] getPossibleValues()
	{
		return possibleValues;
	}
}
