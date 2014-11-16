import java.util.ArrayList;
import java.util.List;


public class Node
{
	private List<Example> examples;
	
	private float entropy;
	private int[] classifications;
	private List<Node> children;
	
	private Attribute attribute;
	private String value;
	private String classification; //Only set for leaf node
	
	public Node(List<Attribute> attributes, List<String> classes, List<Example> examples, Attribute attribute, String value)
	{
		this.examples=examples;
		classifications=computeClassifications(classes);
		entropy=computeEntropy();
		
		children=null;
		
		this.attribute=attribute;
		this.value=value;
		classification=null;
	}
	
	public Node(List<Attribute> attributes, List<String> classes, List<Example> examples)
	{
		this(attributes, classes, examples, null, null);
	}
	
	public int getExampleCount()
	{
		return examples.size();
	}
	
	public float getEntropy()
	{
		return entropy;
	}
	
	public int[] getC()
	{
		return classifications;
	}
	
	public List<Node> getChildren()
	{
		return children;
	}
	
	public Attribute getAttribute()
	{
		return attribute;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public String getClassification()
	{
		return classification;
	}
	
	public void createChildren(List<Attribute> attributes, List<String> classes)
	{
		for(int i=0; i<classifications.length; i++)
		{
			if(classifications[i]==examples.size())
			{
				classification=classes.get(i); //Classified, no children need to be created
				return;
			}
		}
		
		float maxGain=0;
		List<Node> bestChildren=new ArrayList<Node>();
		for(int i=0; i<attributes.size(); i++)
		{
			String[] possibleValues=attributes.get(i).getPossibleValues();
			List<Node> newChildren=new ArrayList<Node>();
			for(int j=0; j<possibleValues.length; j++)
			{
				//Gathering all examples that fit possibleValue[j] for attribute[i]
				List<Example> newExamples=new ArrayList<Example>();
				for(int k=0; k<examples.size(); k++)
				{
					if(examples.get(k).getValues()[i].equals(possibleValues[j]))
					{
						newExamples.add(examples.get(k));
					}
				}
				
				if(!newExamples.isEmpty()) //Only add nodes that actually have examples attached to them
				{
					Node newNode=new Node(attributes, classes, newExamples, attributes.get(i), possibleValues[j]);
					newChildren.add(newNode);
				}
			}
			
			float gain=computeGain(newChildren);
			if(gain>maxGain)
			{
				maxGain=gain;
				bestChildren=newChildren;
				if(gain==entropy) //Highest possible gain found
				{
					break;
				}
			}
		}
		children=bestChildren;
		
		for(int i=0; i<children.size(); i++)
		{
			children.get(i).createChildren(attributes, classes);
		}
	}
	
	private int[] computeClassifications(List<String> classes)
	{
		int[] classifications=new int[classes.size()];
		for(int i=0; i<examples.size(); i++)
		{
			for(int j=0; j<classes.size(); j++)
			{
				if(examples.get(i).getC().equals(classes.get(j)))
				{
					classifications[j]++; //Find out what class the current example belongs to and add it to the array
					break;
				}
			}
		}
		return classifications;
	}
	
	//Assumes that classifications array is filled
	private float computeEntropy()
	{
		//if classification is [2, 1, 0, 0] e.g. use log2 or log4?
		float result=0;
		for(int i=0; i<classifications.length; i++)
		{
			if(classifications[i]!=0)
			{
				float p=classifications[i]/(float)examples.size();
				result-=p*(Math.log(p)/Math.log(classifications.length)); //Entropy formula
			}
		}
		return result;
	}
	
	private float computeGain(List<Node> children)
	{
		float result=entropy;
		for(int i=0; i<children.size(); i++)
		{
			int exampleCount=children.get(i).getExampleCount();
			result-=((float)exampleCount/(float)examples.size())*children.get(i).getEntropy();
		}
		return result;
	}
}
