package com.sun.tools.xjc.outline;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.istack.NotNull;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import java.util.List;

public abstract class ClassOutline
{
  @NotNull
  public final CClassInfo target;
  @NotNull
  public final JDefinedClass ref;
  @NotNull
  public final JDefinedClass implClass;
  @NotNull
  public final JClass implRef;
  
  @NotNull
  public abstract Outline parent();
  
  @NotNull
  public PackageOutline _package()
  {
    return parent().getPackageContext(this.ref._package());
  }
  
  protected ClassOutline(CClassInfo _target, JDefinedClass exposedClass, JClass implRef, JDefinedClass _implClass)
  {
    this.target = _target;
    this.ref = exposedClass;
    this.implRef = implRef;
    this.implClass = _implClass;
  }
  
  public final FieldOutline[] getDeclaredFields()
  {
    List<CPropertyInfo> props = this.target.getProperties();
    FieldOutline[] fr = new FieldOutline[props.size()];
    for (int i = 0; i < fr.length; i++) {
      fr[i] = parent().getField((CPropertyInfo)props.get(i));
    }
    return fr;
  }
  
  public final ClassOutline getSuperClass()
  {
    CClassInfo s = this.target.getBaseClass();
    if (s == null) {
      return null;
    }
    return parent().getClazz(s);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\outline\ClassOutline.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */