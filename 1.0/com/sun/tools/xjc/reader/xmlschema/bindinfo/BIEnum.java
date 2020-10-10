package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public final class BIEnum
  extends AbstractDeclarationImpl
{
  private final String className;
  private final String javadoc;
  private final HashMap members;
  
  public BIEnum(Locator loc, String _className, String _javadoc, HashMap _members)
  {
    super(loc);
    this.className = _className;
    this.javadoc = _javadoc;
    this.members = _members;
  }
  
  public String getClassName()
  {
    return this.className;
  }
  
  public String getJavadoc()
  {
    return this.javadoc;
  }
  
  public HashMap getMembers()
  {
    return this.members;
  }
  
  public QName getName()
  {
    return NAME;
  }
  
  public void setParent(BindInfo p)
  {
    super.setParent(p);
    
    Iterator itr = this.members.entrySet().iterator();
    while (itr.hasNext())
    {
      BIEnumMember mem = (BIEnumMember)((Map.Entry)itr.next()).getValue();
      mem.setParent(p);
    }
  }
  
  public static final QName NAME = new QName("http://java.sun.com/xml/ns/jaxb", "enum");
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIEnum.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */