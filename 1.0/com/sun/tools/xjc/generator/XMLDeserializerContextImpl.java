package com.sun.tools.xjc.generator;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.tools.xjc.grammar.xducer.DeserializerContext;

public final class XMLDeserializerContextImpl
  implements DeserializerContext
{
  private final JExpression $context;
  
  public XMLDeserializerContextImpl(JExpression _$context)
  {
    this.$context = _$context;
  }
  
  public JExpression ref()
  {
    return this.$context;
  }
  
  public JExpression addToIdTable(JExpression literal)
  {
    return this.$context.invoke("addToIdTable").arg(literal);
  }
  
  public JExpression getObjectFromId(JExpression literal)
  {
    return this.$context.invoke("getObjectFromId").arg(literal);
  }
  
  public JExpression getNamespaceContext()
  {
    return this.$context;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\XMLDeserializerContextImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */