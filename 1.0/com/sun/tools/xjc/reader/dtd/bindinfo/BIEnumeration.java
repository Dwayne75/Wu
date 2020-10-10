package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.msv.datatype.xsd.NmtokenType;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.grammar.xducer.EnumerationXducer;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import java.util.HashMap;
import java.util.StringTokenizer;
import org.dom4j.Element;

public final class BIEnumeration
  implements BIConversion
{
  private final Element e;
  private final Transducer xducer;
  
  private BIEnumeration(Element _e, Transducer _xducer)
  {
    this.e = _e;
    this.xducer = _xducer;
  }
  
  public String name()
  {
    return this.e.attributeValue("name");
  }
  
  public Transducer getTransducer()
  {
    return this.xducer;
  }
  
  static BIEnumeration create(Element dom, BindInfo parent)
  {
    return new BIEnumeration(dom, new EnumerationXducer(parent.nameConverter, parent.classFactory.createClass(parent.getTargetPackage(), dom.attributeValue("name"), null), buildMemberExp(dom), emptyHashMap, null));
  }
  
  static BIEnumeration create(Element dom, BIElement parent)
  {
    return new BIEnumeration(dom, new EnumerationXducer(parent.parent.nameConverter, parent.parent.classFactory.createClass(parent.getClassObject(), dom.attributeValue("name"), null), buildMemberExp(dom), emptyHashMap, null));
  }
  
  private static final HashMap emptyHashMap = new HashMap();
  
  private static Expression buildMemberExp(Element dom)
  {
    String members = dom.attributeValue("members");
    if (members == null) {
      members = "";
    }
    ExpressionPool pool = new ExpressionPool();
    
    Expression exp = Expression.nullSet;
    StringTokenizer tokens = new StringTokenizer(members);
    while (tokens.hasMoreTokens())
    {
      String token = tokens.nextToken();
      
      exp = pool.createChoice(exp, pool.createValue(NmtokenType.theInstance, token));
    }
    return exp;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\dtd\bindinfo\BIEnumeration.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */