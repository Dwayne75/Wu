package com.sun.tools.xjc.grammar.ext;

import com.sun.msv.grammar.NameClass;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ExternalItem;
import org.xml.sax.Locator;

public abstract class DOMItemFactory
{
  public abstract ExternalItem create(NameClass paramNameClass, AnnotatedGrammar paramAnnotatedGrammar, Locator paramLocator);
  
  public static DOMItemFactory getInstance(String type)
    throws DOMItemFactory.UndefinedNameException
  {
    type = type.toUpperCase();
    if (type.equals("W3C")) {
      return W3CDOMItem.factory;
    }
    if (type.equals("DOM4J")) {
      return Dom4jItem.factory;
    }
    throw new DOMItemFactory.UndefinedNameException(type);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\ext\DOMItemFactory.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */