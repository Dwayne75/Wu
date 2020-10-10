package com.sun.tools.xjc.generator.bean;

import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.annotation.spec.XmlAccessorTypeWriter;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(Boolean.class)
public enum ImplStructureStrategy
{
  BEAN_ONLY,  INTF_AND_IMPL;
  
  private ImplStructureStrategy() {}
  
  protected abstract Result createClasses(Outline paramOutline, CClassInfo paramCClassInfo);
  
  protected abstract JPackage getPackage(JPackage paramJPackage, Aspect paramAspect);
  
  protected abstract MethodWriter createMethodWriter(ClassOutlineImpl paramClassOutlineImpl);
  
  protected abstract void _extends(ClassOutlineImpl paramClassOutlineImpl1, ClassOutlineImpl paramClassOutlineImpl2);
  
  public static final class Result
  {
    public final JDefinedClass exposed;
    public final JDefinedClass implementation;
    
    public Result(JDefinedClass exposed, JDefinedClass implementation)
    {
      this.exposed = exposed;
      this.implementation = implementation;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\ImplStructureStrategy.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */