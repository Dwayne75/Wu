package com.sun.tools.xjc.model.nav;

import com.sun.codemodel.JClass;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;

class NClassByJClass
  implements NClass
{
  final JClass clazz;
  
  NClassByJClass(JClass clazz)
  {
    this.clazz = clazz;
  }
  
  public JClass toType(Outline o, Aspect aspect)
  {
    return this.clazz;
  }
  
  public boolean isAbstract()
  {
    return this.clazz.isAbstract();
  }
  
  public boolean isBoxedType()
  {
    return this.clazz.getPrimitiveType() != null;
  }
  
  public String fullName()
  {
    return this.clazz.fullName();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\nav\NClassByJClass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */