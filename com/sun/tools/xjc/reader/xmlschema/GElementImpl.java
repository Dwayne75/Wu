package com.sun.tools.xjc.reader.xmlschema;

import com.sun.xml.xsom.XSElementDecl;
import javax.xml.namespace.QName;

final class GElementImpl
  extends GElement
{
  public final QName tagName;
  public final XSElementDecl decl;
  
  public GElementImpl(QName tagName, XSElementDecl decl)
  {
    this.tagName = tagName;
    this.decl = decl;
  }
  
  public String toString()
  {
    return this.tagName.toString();
  }
  
  String getPropertyNameSeed()
  {
    return this.tagName.getLocalPart();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\GElementImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */