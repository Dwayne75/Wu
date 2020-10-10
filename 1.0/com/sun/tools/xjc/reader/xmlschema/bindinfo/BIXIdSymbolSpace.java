package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.id.IDREFTransducer;
import com.sun.tools.xjc.grammar.id.SymbolSpace;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public class BIXIdSymbolSpace
  extends AbstractDeclarationImpl
{
  private final String name;
  
  public BIXIdSymbolSpace(Locator _loc, String _name)
  {
    super(_loc);
    this.name = _name;
  }
  
  public Transducer makeTransducer(Transducer core)
  {
    markAsAcknowledged();
    SymbolSpace ss = getBuilder().grammar.getSymbolSpace(this.name);
    if (core.isID()) {
      return new BIXIdSymbolSpace.1(this, core, ss);
    }
    return new IDREFTransducer(getBuilder().grammar.codeModel, ss, true);
  }
  
  public QName getName()
  {
    return NAME;
  }
  
  public static final QName NAME = new QName("http://java.sun.com/xml/ns/jaxb/xjc", "idSymbolSpace");
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIXIdSymbolSpace.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */