package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.reader.annotator.AnnotatorController;
import com.sun.xml.bind.JAXBAssertionError;
import java.util.StringTokenizer;
import java.util.Vector;
import org.dom4j.Element;
import org.xml.sax.Locator;

public class BIConstructor
{
  private final Element dom;
  private final String[] properties;
  
  BIConstructor(Element _node)
  {
    this.dom = _node;
    
    StringTokenizer tokens = new StringTokenizer(_node.attributeValue("properties"));
    
    Vector vec = new Vector();
    while (tokens.hasMoreTokens()) {
      vec.add(tokens.nextToken());
    }
    this.properties = ((String[])vec.toArray(new String[0]));
    if (this.properties.length == 0) {
      throw new JAXBAssertionError("this error should be catched by the validator");
    }
  }
  
  public void createDeclaration(ClassItem cls, AnnotatorController controller)
  {
    cls.addConstructor(this.properties);
  }
  
  public Locator getSourceLocation()
  {
    return DOM4JLocator.getLocationInfo(this.dom);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\dtd\bindinfo\BIConstructor.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */