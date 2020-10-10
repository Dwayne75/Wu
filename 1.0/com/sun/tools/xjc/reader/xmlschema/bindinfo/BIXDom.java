package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.NameClass;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ext.DOMItemFactory;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public class BIXDom
  extends AbstractDeclarationImpl
{
  private final DOMItemFactory factory;
  
  public BIXDom(DOMItemFactory _factory, Locator _loc)
  {
    super(_loc);
    this.factory = _factory;
  }
  
  public Expression create(NameClass nc, AnnotatedGrammar grammar, Locator loc)
  {
    markAsAcknowledged();
    return this.factory.create(nc, grammar, loc);
  }
  
  public final QName getName()
  {
    return NAME;
  }
  
  public static final QName NAME = new QName("http://java.sun.com/xml/ns/jaxb/xjc", "dom");
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIXDom.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */