package com.sun.tools.xjc.generator.marshaller;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;

abstract interface Pass
{
  public abstract void build(Expression paramExpression);
  
  public abstract String getName();
  
  public abstract void onElement(ElementExp paramElementExp);
  
  public abstract void onExternal(ExternalItem paramExternalItem);
  
  public abstract void onAttribute(AttributeExp paramAttributeExp);
  
  public abstract void onPrimitive(PrimitiveItem paramPrimitiveItem);
  
  public abstract void onValue(ValueExp paramValueExp);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\marshaller\Pass.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */