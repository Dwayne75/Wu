package com.sun.tools.xjc.grammar.util;

import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.BGMWalker;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.xml.bind.JAXBAssertionError;
import java.util.Vector;

public final class TypeItemCollector
  extends BGMWalker
{
  private final Vector vec = new Vector();
  
  public static TypeItem[] collect(Expression e)
  {
    TypeItemCollector tic = new TypeItemCollector();
    e.visit(tic);
    return (TypeItem[])tic.vec.toArray(new TypeItem[tic.vec.size()]);
  }
  
  public Object onClass(ClassItem item)
  {
    this.vec.add(item);
    return null;
  }
  
  public Object onInterface(InterfaceItem item)
  {
    this.vec.add(item);
    return null;
  }
  
  public Object onPrimitive(PrimitiveItem item)
  {
    this.vec.add(item);
    return null;
  }
  
  public Object onExternal(ExternalItem item)
  {
    this.vec.add(item);
    return null;
  }
  
  public Object onSuper(SuperClassItem item)
  {
    throw new JAXBAssertionError();
  }
  
  public Object onField(FieldItem item)
  {
    throw new JAXBAssertionError();
  }
  
  public Object onIgnore(IgnoreItem item)
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\util\TypeItemCollector.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */