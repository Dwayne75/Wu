package com.sun.codemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class JCommentPart
  extends ArrayList<Object>
{
  public JCommentPart append(Object o)
  {
    add(o);
    return this;
  }
  
  public boolean add(Object o)
  {
    flattenAppend(o);
    return true;
  }
  
  private void flattenAppend(Object value)
  {
    if (value == null) {
      return;
    }
    if ((value instanceof Object[])) {
      for (Object o : (Object[])value) {
        flattenAppend(o);
      }
    } else if ((value instanceof Collection)) {
      for (Object o : (Collection)value) {
        flattenAppend(o);
      }
    } else {
      super.add(value);
    }
  }
  
  protected void format(JFormatter f, String indent)
  {
    if (!f.isPrinting())
    {
      for (Object o : this) {
        if ((o instanceof JClass)) {
          f.g((JClass)o);
        }
      }
      return;
    }
    if (!isEmpty()) {
      f.p(indent);
    }
    Iterator itr = iterator();
    while (itr.hasNext())
    {
      Object o = itr.next();
      if ((o instanceof String))
      {
        String s = (String)o;
        int idx;
        while ((idx = s.indexOf('\n')) != -1)
        {
          String line = s.substring(0, idx);
          if (line.length() > 0) {
            f.p(line);
          }
          s = s.substring(idx + 1);
          f.nl().p(indent);
        }
        if (s.length() != 0) {
          f.p(s);
        }
      }
      else if ((o instanceof JClass))
      {
        ((JClass)o).printLink(f);
      }
      else if ((o instanceof JType))
      {
        f.g((JType)o);
      }
      else
      {
        throw new IllegalStateException();
      }
    }
    if (!isEmpty()) {
      f.nl();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JCommentPart.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */