package org.kohsuke.rngom.digested;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;
import org.xml.sax.Locator;

public class DAnnotation
{
  static final DAnnotation EMPTY = new DAnnotation();
  final Map<QName, Attribute> attributes;
  final List<Element> contents;
  
  public DAnnotation()
  {
    this.attributes = new HashMap();
    
    this.contents = new ArrayList();
  }
  
  public static class Attribute
  {
    private final String ns;
    private final String localName;
    private final String prefix;
    private String value;
    private Locator loc;
    
    public Attribute(String ns, String localName, String prefix)
    {
      this.ns = ns;
      this.localName = localName;
      this.prefix = prefix;
    }
    
    public Attribute(String ns, String localName, String prefix, String value, Locator loc)
    {
      this.ns = ns;
      this.localName = localName;
      this.prefix = prefix;
      this.value = value;
      this.loc = loc;
    }
    
    public String getNs()
    {
      return this.ns;
    }
    
    public String getLocalName()
    {
      return this.localName;
    }
    
    public String getPrefix()
    {
      return this.prefix;
    }
    
    public String getValue()
    {
      return this.value;
    }
    
    public Locator getLoc()
    {
      return this.loc;
    }
  }
  
  public Attribute getAttribute(String nsUri, String localName)
  {
    return getAttribute(new QName(nsUri, localName));
  }
  
  public Attribute getAttribute(QName n)
  {
    return (Attribute)this.attributes.get(n);
  }
  
  public Map<QName, Attribute> getAttributes()
  {
    return Collections.unmodifiableMap(this.attributes);
  }
  
  public List<Element> getChildren()
  {
    return Collections.unmodifiableList(this.contents);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DAnnotation.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */