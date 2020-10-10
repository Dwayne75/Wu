package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.model.CElement;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSElementDecl;

class Abstractifier
  extends ClassBinderFilter
{
  public Abstractifier(ClassBinder core)
  {
    super(core);
  }
  
  public CElement complexType(XSComplexType xs)
  {
    CElement ci = super.complexType(xs);
    if ((ci != null) && (xs.isAbstract())) {
      ci.setAbstract();
    }
    return ci;
  }
  
  public CElement elementDecl(XSElementDecl xs)
  {
    CElement ci = super.elementDecl(xs);
    if ((ci != null) && (xs.isAbstract())) {
      ci.setAbstract();
    }
    return ci;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\Abstractifier.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */