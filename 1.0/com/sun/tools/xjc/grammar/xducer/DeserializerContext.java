package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JExpression;

public abstract interface DeserializerContext
{
  public abstract JExpression addToIdTable(JExpression paramJExpression);
  
  public abstract JExpression getObjectFromId(JExpression paramJExpression);
  
  public abstract JExpression getNamespaceContext();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\xducer\DeserializerContext.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */