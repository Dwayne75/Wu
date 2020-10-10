package com.sun.tools.xjc.outline;

import com.sun.codemodel.JDefinedClass;
import com.sun.istack.NotNull;
import com.sun.tools.xjc.model.CEnumLeafInfo;
import java.util.ArrayList;
import java.util.List;

public abstract class EnumOutline
{
  public final CEnumLeafInfo target;
  public final JDefinedClass clazz;
  public final List<EnumConstantOutline> constants = new ArrayList();
  
  @NotNull
  public PackageOutline _package()
  {
    return parent().getPackageContext(this.clazz._package());
  }
  
  @NotNull
  public abstract Outline parent();
  
  protected EnumOutline(CEnumLeafInfo target, JDefinedClass clazz)
  {
    this.target = target;
    this.clazz = clazz;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\outline\EnumOutline.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */