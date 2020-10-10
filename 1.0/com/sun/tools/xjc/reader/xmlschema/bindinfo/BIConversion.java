package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.tools.xjc.grammar.xducer.Transducer;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public class BIConversion
  extends AbstractDeclarationImpl
{
  private final Transducer transducer;
  
  public BIConversion(Locator loc, Transducer transducer)
  {
    super(loc);
    this.transducer = transducer;
  }
  
  public Transducer getTransducer()
  {
    return this.transducer;
  }
  
  public final QName getName()
  {
    return NAME;
  }
  
  public static final QName NAME = new QName("http://java.sun.com/xml/ns/jaxb", "conversion");
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIConversion.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */