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
import com.sun.xml.bind.JAXBAssertionError;
import java.util.Collection;
import java.util.Hashtable;

public final class FieldItemCollector
  extends BGMWalker
{
  private final Hashtable m = new Hashtable();
  
  public static FieldItem[] collect(Expression exp)
  {
    FieldItemCollector fim = new FieldItemCollector();
    exp.visit(fim);
    return (FieldItem[])fim.m.values().toArray(new FieldItem[fim.m.values().size()]);
  }
  
  public Object onSuper(SuperClassItem item)
  {
    return null;
  }
  
  public Object onField(FieldItem item)
  {
    this.m.put(item.name, item);
    return null;
  }
  
  public Object onIgnore(IgnoreItem item)
  {
    return null;
  }
  
  public Object onClass(ClassItem item)
  {
    throw new JAXBAssertionError();
  }
  
  public Object onInterface(InterfaceItem item)
  {
    throw new JAXBAssertionError();
  }
  
  public Object onPrimitive(PrimitiveItem item)
  {
    throw new JAXBAssertionError();
  }
  
  public Object onExternal(ExternalItem item)
  {
    throw new JAXBAssertionError();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\util\FieldItemCollector.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */