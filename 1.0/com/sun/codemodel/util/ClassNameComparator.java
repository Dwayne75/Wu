package com.sun.codemodel.util;

import com.sun.codemodel.JClass;
import java.util.Comparator;

public class ClassNameComparator
  implements Comparator
{
  public int compare(Object l, Object r)
  {
    return ((JClass)l).fullName().compareTo(((JClass)r).fullName());
  }
  
  public static final Comparator theInstance = new ClassNameComparator();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\util\ClassNameComparator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */