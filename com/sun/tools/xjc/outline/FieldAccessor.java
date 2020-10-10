package com.sun.tools.xjc.outline;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.model.CPropertyInfo;

public abstract interface FieldAccessor
{
  public abstract void toRawValue(JBlock paramJBlock, JVar paramJVar);
  
  public abstract void fromRawValue(JBlock paramJBlock, String paramString, JExpression paramJExpression);
  
  public abstract void unsetValues(JBlock paramJBlock);
  
  public abstract JExpression hasSetValue();
  
  public abstract FieldOutline owner();
  
  public abstract CPropertyInfo getPropertyInfo();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\outline\FieldAccessor.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */