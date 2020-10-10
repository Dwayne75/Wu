package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.generator.field.ArrayFieldRenderer;
import com.sun.tools.xjc.generator.field.FieldRendererFactory;
import com.sun.tools.xjc.generator.field.UntypedListFieldRenderer.Factory;
import java.util.ArrayList;
import org.dom4j.Element;

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
    return this.element.attributeValue("name");
  }
  
  public BIConversion getConversion()
  {
    String cnv = this.element.attributeValue("convert");
    if (cnv == null) {
      return null;
    }
    return this.parent.conversion(cnv);
  }
  
  public final FieldRendererFactory getRealization()
  {
    String v = this.element.attributeValue("collection");
    if (v == null) {
      return null;
    }
    v = v.trim();
    if (v.equals("array")) {
      return ArrayFieldRenderer.theFactory;
    }
    if (v.equals("list")) {
      return new UntypedListFieldRenderer.Factory(this.parent.parent.codeModel.ref(ArrayList.class));
    }
    throw new InternalError("unexpected collection value: " + v);
  }
  
  public final String getPropertyName()
  {
    String r = this.element.attributeValue("property");
    if (r != null) {
      return r;
    }
    return name();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\dtd\bindinfo\BIAttribute.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */