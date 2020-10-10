package com.sun.tools.xjc.outline;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.CPropertyInfo;

public abstract interface FieldOutline
{
  public abstract ClassOutline parent();
  
  public abstract CPropertyInfo getPropertyInfo();
  
  public abstract JType getRawType();
  
  public abstract FieldAccessor create(JExpression paramJExpression);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\outline\FieldOutline.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */