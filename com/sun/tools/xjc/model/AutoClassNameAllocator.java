package com.sun.tools.xjc.model;

import com.sun.tools.xjc.api.ClassNameAllocator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AutoClassNameAllocator
  implements ClassNameAllocator
{
  private final ClassNameAllocator core;
  private final Map<String, Set<String>> names = new HashMap();
  
  public AutoClassNameAllocator(ClassNameAllocator core)
  {
    this.core = core;
  }
  
  public String assignClassName(String packageName, String className)
  {
    className = determineName(packageName, className);
    if (this.core != null) {
      className = this.core.assignClassName(packageName, className);
    }
    return className;
  }
  
  private String determineName(String packageName, String className)
  {
    Set<String> s = (Set)this.names.get(packageName);
    if (s == null)
    {
      s = new HashSet();
      this.names.put(packageName, s);
    }
    if (s.add(className)) {
      return className;
    }
    for (int i = 2;; i++) {
      if (s.add(className + i)) {
        return className + i;
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\AutoClassNameAllocator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */