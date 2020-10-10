package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.generator.field.ArrayFieldRenderer;
import com.sun.tools.xjc.generator.field.FieldRendererFactory;
import com.sun.tools.xjc.generator.field.UntypedListFieldRenderer.Factory;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.xml.bind.JAXBAssertionError;
import java.util.ArrayList;
import org.dom4j.Element;

public abstract class BIContent
{
  protected final Element element;
  protected final BIElement parent;
  
  BIContent(Element x0, BIElement x1, BIContent.1 x2)
  {
    this(x0, x1);
  }
  
  private BIContent(Element e, BIElement _parent)
  {
    this.element = e;
    this.parent = _parent;
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
    return this.element.attributeValue("name");
  }
  
  public final JClass getType()
  {
    try
    {
      String type = this.element.attributeValue("supertype");
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
  
  public final Expression wrap(Expression exp)
    throws BIContent.MismatchException
  {
    if (!checkMatch(exp.peelOccurence())) {
      throw new BIContent.MismatchException();
    }
    FieldItem fi = new FieldItem(getPropertyName(), exp, getType(), null);
    
    fi.realization = getRealization();
    
    return fi;
  }
  
  static BIContent create(Element e, BIElement _parent)
  {
    String tagName = e.getName();
    if (tagName.equals("element-ref")) {
      return new BIContent.1(e, _parent);
    }
    if (tagName.equals("choice")) {
      return new BIContent.2(e, _parent);
    }
    if (tagName.equals("sequence")) {
      return new BIContent.3(e, _parent);
    }
    if ((tagName.equals("rest")) || (tagName.equals("content"))) {
      return new BIContent.4(e, _parent);
    }
    throw new JAXBAssertionError();
  }
  
  protected abstract boolean checkMatch(Expression paramExpression);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\dtd\bindinfo\BIContent.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */