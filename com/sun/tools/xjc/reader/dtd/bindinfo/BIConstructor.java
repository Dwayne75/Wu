package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.tools.xjc.model.CClassInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.w3c.dom.Element;
import org.xml.sax.Locator;

public class BIConstructor
{
  private final Element dom;
  private final String[] properties;
  
  BIConstructor(Element _node)
  {
    this.dom = _node;
    
    StringTokenizer tokens = new StringTokenizer(DOMUtil.getAttribute(_node, "properties"));
    
    List<String> vec = new ArrayList();
    while (tokens.hasMoreTokens()) {
      vec.add(tokens.nextToken());
    }
    this.properties = ((String[])vec.toArray(new String[0]));
    if (this.properties.length == 0) {
      throw new AssertionError("this error should be catched by the validator");
    }
  }
  
  public void createDeclaration(CClassInfo cls)
  {
    cls.addConstructor(this.properties);
  }
  
  public Locator getSourceLocation()
  {
    return DOMLocator.getLocationInfo(this.dom);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\dtd\bindinfo\BIConstructor.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */