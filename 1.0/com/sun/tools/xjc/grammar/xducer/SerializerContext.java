package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpression;

public abstract interface SerializerContext
{
  public abstract void declareNamespace(JBlock paramJBlock, JExpression paramJExpression1, JExpression paramJExpression2, JExpression paramJExpression3);
  
  public abstract JExpression getNamespaceContext();
  
  public abstract JExpression onID(JExpression paramJExpression1, JExpression paramJExpression2);
  
  public abstract JExpression onIDREF(JExpression paramJExpression);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\xducer\SerializerContext.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */