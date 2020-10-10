package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.xml.bind.JAXBAssertionError;
import java.util.StringTokenizer;
import org.dom4j.Element;
import org.xml.sax.Locator;

public final class BIInterface
{
  private final Element dom;
  private final String name;
  private final String[] members;
  private final String[] fields;
  
  BIInterface(Element e)
  {
    this.dom = e;
    this.name = e.attributeValue("name");
    this.members = parseTokens(e.attributeValue("members"));
    if (e.attribute("properties") != null)
    {
      this.fields = parseTokens(e.attributeValue("properties"));
      throw new JAXBAssertionError("//interface/@properties is not supported");
    }
    this.fields = new String[0];
  }
  
  public String name()
  {
    return this.name;
  }
  
  public String[] members()
  {
    return this.members;
  }
  
  public String[] fields()
  {
    return this.fields;
  }
  
  public Locator getSourceLocation()
  {
    return DOM4JLocator.getLocationInfo(this.dom);
  }
  
  private static String[] parseTokens(String value)
  {
    StringTokenizer tokens = new StringTokenizer(value);
    
    String[] r = new String[tokens.countTokens()];
    int i = 0;
    while (tokens.hasMoreTokens()) {
      r[(i++)] = tokens.nextToken();
    }
    return r;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\dtd\bindinfo\BIInterface.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */