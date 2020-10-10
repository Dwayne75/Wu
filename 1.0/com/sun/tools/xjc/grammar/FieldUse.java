package com.sun.tools.xjc.grammar;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.generator.field.FieldRendererFactory;
import com.sun.tools.xjc.grammar.util.Multiplicity;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class FieldUse
{
  public final String name;
  public final ClassItem owner;
  public final JCodeModel codeModel;
  public JType type;
  public final Set items = new HashSet();
  public Multiplicity multiplicity;
  
  protected FieldUse(String name, ClassItem _owner)
  {
    this.name = name;
    this.owner = _owner;
    this.codeModel = this.owner.owner.codeModel;
  }
  
  public final JCodeModel getCodeModel()
  {
    return this.owner.owner.codeModel;
  }
  
  public FieldRendererFactory getRealization()
  {
    Iterator itr = this.items.iterator();
    while (itr.hasNext())
    {
      FieldRendererFactory frf = ((FieldItem)itr.next()).realization;
      if (frf != null) {
        return frf;
      }
    }
    return null;
  }
  
  public DefaultValue[] getDefaultValues()
  {
    Iterator itr = this.items.iterator();
    while (itr.hasNext())
    {
      DefaultValue[] dv = ((FieldItem)itr.next()).defaultValues;
      if (dv != null) {
        return dv;
      }
    }
    return null;
  }
  
  public String getJavadoc()
  {
    StringBuffer buf = new StringBuffer();
    FieldItem[] items = getItems();
    for (int i = 0; i < items.length; i++) {
      if (items[i].javadoc != null)
      {
        if (i != 0) {
          buf.append("\n\n");
        }
        buf.append(items[i].javadoc);
      }
    }
    return buf.toString();
  }
  
  public boolean isUnboxable()
  {
    FieldItem[] items = getItems();
    for (int i = 0; i < items.length; i++) {
      if (!items[i].isUnboxable(this.codeModel)) {
        return false;
      }
    }
    return true;
  }
  
  public boolean isDelegated()
  {
    FieldItem[] items = getItems();
    for (int i = 0; i < items.length; i++) {
      if (items[i].isDelegated()) {
        return true;
      }
    }
    return false;
  }
  
  public void disableDelegation()
  {
    FieldItem[] items = getItems();
    for (int i = 0; i < items.length; i++) {
      items[i].setDelegation(false);
    }
  }
  
  public FieldItem[] getItems()
  {
    return (FieldItem[])this.items.toArray(new FieldItem[0]);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\FieldUse.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */