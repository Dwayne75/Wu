package com.sun.tools.xjc.model;

import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.api.ClassNameAllocator;

final class ClassNameAllocatorWrapper
  implements ClassNameAllocator
{
  private final ClassNameAllocator core;
  
  ClassNameAllocatorWrapper(ClassNameAllocator core)
  {
    if (core == null) {
      core = new ClassNameAllocator()
      {
        public String assignClassName(String packageName, String className)
        {
          return className;
        }
      };
    }
    this.core = core;
  }
  
  public String assignClassName(String packageName, String className)
  {
    return this.core.assignClassName(packageName, className);
  }
  
  public String assignClassName(JPackage pkg, String className)
  {
    return this.core.assignClassName(pkg.name(), className);
  }
  
  public String assignClassName(CClassInfoParent parent, String className)
  {
    if ((parent instanceof CClassInfoParent.Package))
    {
      CClassInfoParent.Package p = (CClassInfoParent.Package)parent;
      return assignClassName(p.pkg, className);
    }
    return className;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\ClassNameAllocatorWrapper.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */