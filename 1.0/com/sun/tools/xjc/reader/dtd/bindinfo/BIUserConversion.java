package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.generator.util.WhitespaceNormalizer;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.grammar.xducer.UserTransducer;
import com.sun.tools.xjc.grammar.xducer.WhitespaceTransducer;
import java.util.Map;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

public class BIUserConversion
  implements BIConversion
{
  private final BindInfo owner;
  private final Element e;
  
  BIUserConversion(BindInfo bi, Element _e)
  {
    this.owner = bi;
    this.e = _e;
  }
  
  private static void add(Map m, BIConversion c)
  {
    m.put(c.name(), c);
  }
  
  static void addBuiltinConversions(BindInfo bi, Map m)
  {
    DocumentFactory f = DocumentFactory.getInstance();
    
    add(m, new BIUserConversion(bi, f.createElement("conversion").addAttribute("name", "boolean").addAttribute("type", "java.lang.Boolean").addAttribute("parse", "getBoolean")));
    
    add(m, new BIUserConversion(bi, f.createElement("conversion").addAttribute("name", "byte").addAttribute("type", "java.lang.Byte").addAttribute("parse", "parseByte")));
    
    add(m, new BIUserConversion(bi, f.createElement("conversion").addAttribute("name", "short").addAttribute("type", "java.lang.Short").addAttribute("parse", "parseShort")));
    
    add(m, new BIUserConversion(bi, f.createElement("conversion").addAttribute("name", "int").addAttribute("type", "java.lang.Integer").addAttribute("parse", "parseInt")));
    
    add(m, new BIUserConversion(bi, f.createElement("conversion").addAttribute("name", "long").addAttribute("type", "java.lang.Long").addAttribute("parse", "parseLong")));
    
    add(m, new BIUserConversion(bi, f.createElement("conversion").addAttribute("name", "float").addAttribute("type", "java.lang.Float").addAttribute("parse", "parseFloat")));
    
    add(m, new BIUserConversion(bi, f.createElement("conversion").addAttribute("name", "double").addAttribute("type", "java.lang.Double").addAttribute("parse", "parseDouble")));
  }
  
  public Locator getSourceLocation()
  {
    return DOM4JLocator.getLocationInfo(this.e);
  }
  
  private String attValue(String name, String defaultValue)
  {
    String r = this.e.attributeValue(name);
    if (r == null) {
      return defaultValue;
    }
    return r;
  }
  
  public String name()
  {
    return this.e.attributeValue("name");
  }
  
  public Transducer getTransducer()
  {
    String ws = this.e.attributeValue("whitespace");
    if (ws == null) {
      ws = "collapse";
    }
    String type = this.e.attributeValue("type");
    JType t;
    JType t;
    if (type == null)
    {
      t = this.owner.getTargetPackage().ref(name());
    }
    else
    {
      int idx = type.lastIndexOf('.');
      if (idx < 0)
      {
        JType t;
        try
        {
          t = JType.parse(this.owner.codeModel, type);
        }
        catch (IllegalArgumentException e)
        {
          JType t;
          t = this.owner.getTargetPackage().ref(type);
        }
      }
      else
      {
        try
        {
          t = this.owner.codeModel.ref(type);
        }
        catch (ClassNotFoundException e)
        {
          throw new NoClassDefFoundError(e.getMessage());
        }
      }
    }
    try
    {
      return WhitespaceTransducer.create(new UserTransducer(t, attValue("parse", "new"), attValue("print", "toString"), false), this.owner.codeModel, WhitespaceNormalizer.parse(ws));
    }
    catch (IllegalArgumentException e)
    {
      this.owner.errorReceiver.error(new SAXParseException(e.getMessage(), getSourceLocation(), e));
    }
    return new IdentityTransducer(this.owner.codeModel);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\dtd\bindinfo\BIUserConversion.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */