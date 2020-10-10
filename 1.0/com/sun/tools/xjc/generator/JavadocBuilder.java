package com.sun.tools.xjc.generator;

import com.sun.codemodel.JType;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.TypeItem;
import java.util.Iterator;
import java.util.Set;

public class JavadocBuilder
{
  public static String listPossibleTypes(FieldUse fu)
  {
    StringBuffer buf = new StringBuffer();
    for (Iterator itr = fu.items.iterator(); itr.hasNext();)
    {
      FieldItem fi = (FieldItem)itr.next();
      TypeItem[] types = fi.listTypes();
      for (int i = 0; i < types.length; i++)
      {
        JType t = types[i].getType();
        if ((t.isPrimitive()) || (t.isArray())) {
          buf.append(t.fullName());
        } else {
          buf.append("{@link " + t.fullName() + "}\n");
        }
      }
    }
    return buf.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\JavadocBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */