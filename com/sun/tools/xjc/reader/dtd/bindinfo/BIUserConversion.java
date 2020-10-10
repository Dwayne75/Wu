package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.model.CAdapter;
import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.model.TypeUseFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

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
  
  private static void add(Map<String, BIConversion> m, BIConversion c)
  {
    m.put(c.name(), c);
  }
  
  static void addBuiltinConversions(BindInfo bi, Map<String, BIConversion> m)
  {
    add(m, new BIUserConversion(bi, parse("<conversion name='boolean' type='java.lang.Boolean' parse='getBoolean' />")));
    add(m, new BIUserConversion(bi, parse("<conversion name='byte' type='java.lang.Byte' parse='parseByte' />")));
    add(m, new BIUserConversion(bi, parse("<conversion name='short' type='java.lang.Short' parse='parseShort' />")));
    add(m, new BIUserConversion(bi, parse("<conversion name='int' type='java.lang.Integer' parse='parseInt' />")));
    add(m, new BIUserConversion(bi, parse("<conversion name='long' type='java.lang.Long' parse='parseLong' />")));
    add(m, new BIUserConversion(bi, parse("<conversion name='float' type='java.lang.Float' parse='parseFloat' />")));
    add(m, new BIUserConversion(bi, parse("<conversion name='double' type='java.lang.Double' parse='parseDouble' />")));
  }
  
  private static Element parse(String text)
  {
    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      InputSource is = new InputSource(new StringReader(text));
      return dbf.newDocumentBuilder().parse(is).getDocumentElement();
    }
    catch (SAXException x)
    {
      throw new Error(x);
    }
    catch (IOException x)
    {
      throw new Error(x);
    }
    catch (ParserConfigurationException x)
    {
      throw new Error(x);
    }
  }
  
  public Locator getSourceLocation()
  {
    return DOMLocator.getLocationInfo(this.e);
  }
  
  public String name()
  {
    return DOMUtil.getAttribute(this.e, "name");
  }
  
  public TypeUse getTransducer()
  {
    String ws = DOMUtil.getAttribute(this.e, "whitespace");
    if (ws == null) {
      ws = "collapse";
    }
    String type = DOMUtil.getAttribute(this.e, "type");
    if (type == null) {
      type = name();
    }
    JType t = null;
    
    int idx = type.lastIndexOf('.');
    if (idx < 0) {
      try
      {
        t = JPrimitiveType.parse(this.owner.codeModel, type);
      }
      catch (IllegalArgumentException e)
      {
        type = this.owner.getTargetPackage().name() + '.' + type;
      }
    }
    if (t == null) {
      try
      {
        JDefinedClass cls = this.owner.codeModel._class(type);
        cls.hide();
        t = cls;
      }
      catch (JClassAlreadyExistsException e)
      {
        t = e.getExistingClass();
      }
    }
    String parse = DOMUtil.getAttribute(this.e, "parse");
    if (parse == null) {
      parse = "new";
    }
    String print = DOMUtil.getAttribute(this.e, "print");
    if (print == null) {
      print = "toString";
    }
    JDefinedClass adapter = generateAdapter(this.owner.codeModel, parse, print, t.boxify());
    
    return TypeUseFactory.adapt(CBuiltinLeafInfo.STRING, new CAdapter(adapter));
  }
  
  private JDefinedClass generateAdapter(JCodeModel cm, String parseMethod, String printMethod, JClass inMemoryType)
  {
    JDefinedClass adapter = null;
    
    int id = 1;
    while (adapter == null) {
      try
      {
        JPackage pkg = this.owner.getTargetPackage();
        adapter = pkg._class("Adapter" + id);
      }
      catch (JClassAlreadyExistsException e)
      {
        id++;
      }
    }
    adapter._extends(cm.ref(XmlAdapter.class).narrow(String.class).narrow(inMemoryType));
    
    JMethod unmarshal = adapter.method(1, inMemoryType, "unmarshal");
    JVar $value = unmarshal.param(String.class, "value");
    JExpression inv;
    JExpression inv;
    if (parseMethod.equals("new"))
    {
      inv = JExpr._new(inMemoryType).arg($value);
    }
    else
    {
      int idx = parseMethod.lastIndexOf('.');
      JExpression inv;
      if (idx < 0) {
        inv = inMemoryType.staticInvoke(parseMethod).arg($value);
      } else {
        inv = JExpr.direct(parseMethod + "(value)");
      }
    }
    unmarshal.body()._return(inv);
    
    JMethod marshal = adapter.method(1, String.class, "marshal");
    $value = marshal.param(inMemoryType, "value");
    
    int idx = printMethod.lastIndexOf('.');
    if (idx < 0) {
      inv = $value.invoke(printMethod);
    } else {
      inv = JExpr.direct(printMethod + "(value)");
    }
    marshal.body()._return(inv);
    
    return adapter;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\dtd\bindinfo\BIUserConversion.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */