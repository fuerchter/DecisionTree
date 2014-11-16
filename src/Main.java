import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 * Assignment 3.5
 * What is your result tree?
 * My result tree for the car data is saved as "result.xml". It has a height of 6, meaning that in some part of the tree all attributes have to be checked before a classification can be made.
 * The tree also has a total of 408 nodes.
 * 
 * How are the instances distributed in the leaf nodes?
 * After the first attribute (safety), the instances with a low value can already be classified as unacc. This classifies 576 out of the 1728 instances.
 * 384 further instances can be classified as unacc in the next step (safety="med", persons="2" and safety="high", persons="2"), meaning that with those two attributes more than half of the instances can be classified.
 * 
 * 
 * My program first creates the attributes, classes and examples required to create the DecisionTree by calling getData. This function reads out the "data" file with the template in the function's description.
 * After that the root Node is created. Within the constructor, the classifications (unacc:12,acc:30,good:6,vgood:0 e.g.) are computed from the examples given to the Node. Those classifications are consequently used to compute the entropy.
 * Next createChildren is called for the root Node. This picks the attribute with the best gain and recursively calls itself for its children until a leaf Node is hit (the Node is classified).
 * With that the tree is constructed and can be converted to "output.xml" by calling createXML which itself uses createElement.
 */

public class Main
{

	public static void main(String[] args)
	{
		List<Attribute> attributes=new ArrayList<Attribute>();
		List<String> classes=new ArrayList<String>();
		List<Example> examples=new ArrayList<Example>();
		getData(attributes, classes, examples);
		
		Node root=new Node(attributes, classes, examples);
		root.createChildren(attributes, classes);

		createXML(classes, root);
	}
	
	/*
	 * Read data file with the following template:
	 * attribute1name:value1,value2,...
	 * attribute2name:value1,value2,...
	 * 		<- One line left empty between attributes and classes
	 * class1,class2,...
	 * 		<- One line left empty between classes and examples
	 * attribute1value,attribute2value,classification
	 * attribute1value,attribute2value,classification
	 */
	public static void getData(List<Attribute> attributes, List<String> classes, List<Example> examples)
	{
		try
		{
			//Go through data line by line
			List<String> file=Files.readAllLines(Paths.get("data"), Charset.defaultCharset());
			int part=0;
			for(int i=0; i<file.size(); i++)
			{
				if(file.get(i).equals(""))
				{
					part++;
					continue;
				}
				
				switch(part)
				{
					case 0:
						String[] attString=file.get(i).split(":");
						Attribute attribute=new Attribute(attString[0], attString[1].split(","));
						attributes.add(attribute);
						break;
					case 1:
						List<String> classList=Arrays.asList(file.get(i).split(","));
						classes.addAll(classList);
						break;
					case 2:
						String[] exString=file.get(i).split(",");
						Example example=new Example(Arrays.copyOfRange(exString, 0, exString.length-1), exString[exString.length-1]);
						examples.add(example);
						break;
					default:
						break;
				}
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}		
	}
	
	public static void createXML(List<String> classes, Node node)
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try
		{
			docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			//First tag is <tree> instead of <node>
			Element element = doc.createElement("tree");
			doc.appendChild(createElement(classes, node, doc, element));
			
			//Level-order traversal
			List<Node> current=new ArrayList<Node>();
			current.add(node);
			List<Element> currElements=new ArrayList<Element>();
			currElements.add(element);
			do
			{
				List<Node> next=new ArrayList<Node>();
				List<Element> nextElements=new ArrayList<Element>();
				for(int i=0; i<current.size(); i++)
				{
					//Get next level for current node
					List<Node> children=current.get(i).getChildren();
					if(children!=null)
					{
						for(int j=0; j<children.size(); j++)
						{
							Element nextElement = doc.createElement("node");
							nextElement=createElement(classes, children.get(j), doc, nextElement);
							currElements.get(i).appendChild(nextElement);
							
							next.add(children.get(j));
							nextElements.add(nextElement);
						}
					}
				}
				
				current=next;
				currElements=nextElements;
			}
			while(!current.isEmpty());
			
			//XML fun
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("output.xml"));
	 
			transformer.transform(source, result);
		} catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (TransformerException e)
		{
			e.printStackTrace();
		}
	}
	
	//Convert Node to an XML tag adhering to the format given in the assignment 
	public static Element createElement(List<String> classes,  Node node, Document doc, Element element)
	{
		int[] classifications=node.getC();
		String value="";
		for(int i=0; i<classes.size(); i++)
		{
			value+=classes.get(i)+ ":" +classifications[i];
			if(i<classes.size()-1)
			{
				value+=",";
			}
		}
		element.setAttribute("classes", value);
		
		element.setAttribute("entropy", Float.toString(node.getEntropy()));
		
		Attribute attribute=node.getAttribute();
		if(attribute!=null)
		{
			element.setAttribute(attribute.getName(), node.getValue());
		}
		
		String classification=node.getClassification();
		if(classification!=null)
		{
			element.appendChild(doc.createTextNode(classification));
		}
		return element;
	}
}
