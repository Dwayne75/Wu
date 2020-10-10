package com.sun.tools.xjc.outline;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CClassInfoParent;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.CEnumLeafInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CTypeRef;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import java.util.Collection;

public abstract interface Outline
{
  public abstract Model getModel();
  
  public abstract JCodeModel getCodeModel();
  
  public abstract FieldOutline getField(CPropertyInfo paramCPropertyInfo);
  
  public abstract PackageOutline getPackageContext(JPackage paramJPackage);
  
  public abstract Collection<? extends ClassOutline> getClasses();
  
  public abstract ClassOutline getClazz(CClassInfo paramCClassInfo);
  
  public abstract ElementOutline getElement(CElementInfo paramCElementInfo);
  
  public abstract EnumOutline getEnum(CEnumLeafInfo paramCEnumLeafInfo);
  
  public abstract Collection<EnumOutline> getEnums();
  
  public abstract Iterable<? extends PackageOutline> getAllPackageContexts();
  
  public abstract CodeModelClassFactory getClassFactory();
  
  public abstract ErrorReceiver getErrorReceiver();
  
  public abstract JClassContainer getContainer(CClassInfoParent paramCClassInfoParent, Aspect paramAspect);
  
  public abstract JType resolve(CTypeRef paramCTypeRef, Aspect paramAspect);
  
  public abstract JClass addRuntime(Class paramClass);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\outline\Outline.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */