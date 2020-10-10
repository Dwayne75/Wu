package com.sun.tools.xjc.outline;

import com.sun.codemodel.JEnumConstant;
import com.sun.tools.xjc.model.CEnumConstant;

public abstract class EnumConstantOutline
{
  public final CEnumConstant target;
  public final JEnumConstant constRef;
  
  protected EnumConstantOutline(CEnumConstant target, JEnumConstant constRef)
  {
    this.target = target;
    this.constRef = constRef;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\outline\EnumConstantOutline.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */