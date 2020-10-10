package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.msv.grammar.Expression;
import com.sun.xml.xsom.XSComplexType;

abstract interface CTBuilder
{
  public abstract boolean isApplicable(XSComplexType paramXSComplexType);
  
  public abstract Expression build(XSComplexType paramXSComplexType);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\ct\CTBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */