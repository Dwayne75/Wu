package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.bean.field.FieldRenderer;
import com.sun.tools.xjc.generator.bean.field.FieldRendererFactory;
import com.sun.tools.xjc.model.Model;
import java.util.ArrayList;
import org.w3c.dom.Element;

public class BIContent
{
  protected final Element element;
  protected final BIElement parent;
  private final Options opts;
  
  private BIContent(Element e, BIElement _parent)
  {
    this.element = e;
    this.parent = _parent;
    this.opts = this.parent.parent.model.options;
  }
  
  public final FieldRenderer getRealization()
  {
    String v = DOMUtil.getAttribute(this.element, "collection");
    if (v == null) {
      return null;
    }
    v = v.trim();
    if (v.equals("array")) {
      return this.opts.getFieldRendererFactory().getArray();
    }
    if (v.equals("list")) {
      return this.opts.getFieldRendererFactory().getList(this.parent.parent.codeModel.ref(ArrayList.class));
    }
    throw new InternalError("unexpected collection value: " + v);
  }
  
  public final String getPropertyName()
  {
    String r = DOMUtil.getAttribute(this.element, "property");
    if (r != null) {
      return r;
    }
    return DOMUtil.getAttribute(this.element, "name");
  }
  
  public final JClass getType()
  {
    try
    {
      String type = DOMUtil.getAttribute(this.element, "supertype");
      if (type == null) {
        return null;
      }
      int idx = type.lastIndexOf('.');
      if (idx < 0) {
        return this.parent.parent.codeModel.ref(type);
      }
      return this.parent.parent.getTargetPackage().ref(type);
    }
    catch (ClassNotFoundException e)
    {
      throw new NoClassDefFoundError(e.getMessage());
    }
  }
  
  static BIContent create(Element e, BIElement _parent)
  {
    return new BIContent(e, _parent);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\dtd\bindinfo\BIContent.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */