package com.sun.tools.xjc.api;

import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JType;

public abstract interface TypeAndAnnotation
{
  public abstract JType getTypeClass();
  
  public abstract void annotate(JAnnotatable paramJAnnotatable);
  
  public abstract boolean equals(Object paramObject);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\TypeAndAnnotation.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */