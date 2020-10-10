package com.sun.tools.xjc.model.nav;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;

final class NParameterizedType
  implements NClass
{
  final NClass rawType;
  final NType[] args;
  
  NParameterizedType(NClass rawType, NType[] args)
  {
    this.rawType = rawType;
    this.args = args;
    assert (args.length > 0);
  }
  
  public JClass toType(Outline o, Aspect aspect)
  {
    JClass r = this.rawType.toType(o, aspect);
    for (NType arg : this.args) {
      r = r.narrow(arg.toType(o, aspect).boxify());
    }
    return r;
  }
  
  public boolean isAbstract()
  {
    return this.rawType.isAbstract();
  }
  
  public boolean isBoxedType()
  {
    return false;
  }
  
  public String fullName()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(this.rawType.fullName());
    buf.append('<');
    for (int i = 0; i < this.args.length; i++)
    {
      if (i != 0) {
        buf.append(',');
      }
      buf.append(this.args[i].fullName());
    }
    buf.append('>');
    return buf.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\nav\NParameterizedType.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */