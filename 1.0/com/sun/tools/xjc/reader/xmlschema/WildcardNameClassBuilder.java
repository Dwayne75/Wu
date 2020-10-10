package com.sun.tools.xjc.reader.xmlschema;

import com.sun.msv.grammar.ChoiceNameClass;
import com.sun.msv.grammar.DifferenceNameClass;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.NamespaceNameClass;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSWildcard.Any;
import com.sun.xml.xsom.XSWildcard.Other;
import com.sun.xml.xsom.XSWildcard.Union;
import com.sun.xml.xsom.visitor.XSWildcardFunction;
import java.util.Iterator;

public final class WildcardNameClassBuilder
  implements XSWildcardFunction
{
  private static final XSWildcardFunction theInstance = new WildcardNameClassBuilder();
  
  public static NameClass build(XSWildcard wc)
  {
    return (NameClass)wc.apply(theInstance);
  }
  
  public Object any(XSWildcard.Any wc)
  {
    return NameClass.ALL;
  }
  
  public Object other(XSWildcard.Other wc)
  {
    return new DifferenceNameClass(NameClass.ALL, new ChoiceNameClass(new NamespaceNameClass(""), new NamespaceNameClass(wc.getOtherNamespace())));
  }
  
  public Object union(XSWildcard.Union wc)
  {
    NameClass nc = null;
    for (Iterator itr = wc.iterateNamespaces(); itr.hasNext();)
    {
      String ns = (String)itr.next();
      if (nc == null) {
        nc = new NamespaceNameClass(ns);
      } else {
        nc = new ChoiceNameClass(nc, new NamespaceNameClass(ns));
      }
    }
    if (nc == null) {
      throw new JAXBAssertionError();
    }
    return nc;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\WildcardNameClassBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */