package com.sun.tools.xjc.outline;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.model.CElementInfo;

public abstract class ElementOutline
{
  public final CElementInfo target;
  public final JDefinedClass implClass;
  
  public abstract Outline parent();
  
  public PackageOutline _package()
  {
    return parent().getPackageContext(this.implClass._package());
  }
  
  protected ElementOutline(CElementInfo target, JDefinedClass implClass)
  {
    this.target = target;
    this.implClass = implClass;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\outline\ElementOutline.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */