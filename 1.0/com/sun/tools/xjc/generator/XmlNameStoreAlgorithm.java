package com.sun.tools.xjc.generator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.NameClassAndExpression;
import com.sun.msv.grammar.SimpleNameClass;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class XmlNameStoreAlgorithm
{
  static Class class$java$lang$String;
  static Class class$javax$xml$namespace$QName;
  
  public abstract JExpression getNamespaceURI();
  
  public abstract JExpression getLocalPart();
  
  public abstract JType getType(JCodeModel paramJCodeModel);
  
  public abstract void onNameUnmarshalled(JCodeModel paramJCodeModel, JBlock paramJBlock, JVar paramJVar1, JVar paramJVar2);
  
  public abstract void populate(ClassContext paramClassContext);
  
  public static XmlNameStoreAlgorithm get(NameClassAndExpression item)
  {
    return get(item.getNameClass());
  }
  
  public static XmlNameStoreAlgorithm get(NameClass nc)
  {
    if ((nc instanceof SimpleNameClass)) {
      return new XmlNameStoreAlgorithm.Simple((SimpleNameClass)nc, null);
    }
    Set namespaces = new HashSet();
    nc.simplify().visit(new XmlNameStoreAlgorithm.1(namespaces));
    if (namespaces.size() == 1) {
      return new XmlNameStoreAlgorithm.UniqueNamespace((String)namespaces.iterator().next(), null);
    }
    return XmlNameStoreAlgorithm.Any.access$200();
  }
  
  static Class class$(String x0)
  {
    try
    {
      return Class.forName(x0);
    }
    catch (ClassNotFoundException x1)
    {
      throw new NoClassDefFoundError(x1.getMessage());
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\XmlNameStoreAlgorithm.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */