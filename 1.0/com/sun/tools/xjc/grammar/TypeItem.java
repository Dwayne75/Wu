package com.sun.tools.xjc.grammar;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import org.xml.sax.Locator;

public abstract class TypeItem
  extends JavaItem
{
  public TypeItem(String displayName, Locator loc)
  {
    super(displayName, loc);
  }
  
  public abstract JType getType();
  
  public static void sort(TypeItem[] t)
  {
    for (int i = 0; i < t.length - 1; i++)
    {
      int k = i;
      JClass tk = toJClass(t[k]);
      for (int j = i + 1; j < t.length; j++)
      {
        JClass tj = toJClass(t[j]);
        if (tk.isAssignableFrom(tj))
        {
          k = j;
          tk = tj;
        }
      }
      TypeItem tmp = t[i];
      t[i] = t[k];
      t[k] = tmp;
    }
  }
  
  private static JClass toJClass(TypeItem t)
  {
    JType jt = t.getType();
    if (jt.isPrimitive()) {
      return ((JPrimitiveType)jt).getWrapperClass();
    }
    return (JClass)jt;
  }
  
  public String toString()
  {
    return getClass().getName() + '[' + getType().fullName() + ']';
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\TypeItem.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */