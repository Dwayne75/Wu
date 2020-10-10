package com.sun.tools.xjc.reader.xmlschema;

import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSWildcard.Any;
import com.sun.xml.xsom.XSWildcard.Other;
import com.sun.xml.xsom.XSWildcard.Union;
import com.sun.xml.xsom.visitor.XSWildcardFunction;
import java.util.Iterator;
import org.kohsuke.rngom.nc.AnyNameExceptNameClass;
import org.kohsuke.rngom.nc.ChoiceNameClass;
import org.kohsuke.rngom.nc.NameClass;
import org.kohsuke.rngom.nc.NsNameClass;

public final class WildcardNameClassBuilder
  implements XSWildcardFunction<NameClass>
{
  private static final XSWildcardFunction<NameClass> theInstance = new WildcardNameClassBuilder();
  
  public static NameClass build(XSWildcard wc)
  {
    return (NameClass)wc.apply(theInstance);
  }
  
  public NameClass any(XSWildcard.Any wc)
  {
    return NameClass.ANY;
  }
  
  public NameClass other(XSWildcard.Other wc)
  {
    return new AnyNameExceptNameClass(new ChoiceNameClass(new NsNameClass(""), new NsNameClass(wc.getOtherNamespace())));
  }
  
  public NameClass union(XSWildcard.Union wc)
  {
    NameClass nc = null;
    for (Iterator itr = wc.iterateNamespaces(); itr.hasNext();)
    {
      String ns = (String)itr.next();
      if (nc == null) {
        nc = new NsNameClass(ns);
      } else {
        nc = new ChoiceNameClass(nc, new NsNameClass(ns));
      }
    }
    assert (nc != null);
    
    return nc;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\WildcardNameClassBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */