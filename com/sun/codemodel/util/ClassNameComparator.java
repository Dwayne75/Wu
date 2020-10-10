package com.sun.codemodel.util;

import com.sun.codemodel.JClass;
import java.util.Comparator;

public class ClassNameComparator
  implements Comparator<JClass>
{
  public int compare(JClass l, JClass r)
  {
    return l.fullName().compareTo(r.fullName());
  }
  
  public static final Comparator<JClass> theInstance = new ClassNameComparator();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\util\ClassNameComparator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */