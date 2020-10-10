package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.bean.field.FieldRenderer;
import com.sun.tools.xjc.generator.bean.field.FieldRendererFactory;
import com.sun.tools.xjc.model.Model;
import java.util.ArrayList;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

public class BIAttribute
{
  private final BIElement parent;
  private final Element element;
  
  BIAttribute(BIElement _parent, Element _e)
  {
    this.parent = _parent;
    this.element = _e;
  }
  
  public final String name()
  {
    return this.element.getAttribute("name");
  }
  
  public BIConversion getConversion()
  {
    if (this.element.getAttributeNode("convert") == null) {
      return null;
    }
    String cnv = this.element.getAttribute("convert");
    return this.parent.conversion(cnv);
  }
  
  public final FieldRenderer getRealization()
  {
    Attr a = this.element.getAttributeNode("collection");
    if (a == null) {
      return null;
    }
    String v = this.element.getAttribute("collection").trim();
    
    FieldRendererFactory frf = this.parent.parent.model.options.getFieldRendererFactory();
    if (v.equals("array")) {
      return frf.getArray();
    }
    if (v.equals("list")) {
      return frf.getList(this.parent.parent.codeModel.ref(ArrayList.class));
    }
    throw new InternalError("unexpected collection value: " + v);
  }
  
  public final String getPropertyName()
  {
    String r = DOMUtil.getAttribute(this.element, "property");
    if (r != null) {
      return r;
    }
    return name();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\dtd\bindinfo\BIAttribute.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */