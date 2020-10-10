package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;

public class IdentityTransducer
  extends TransducerImpl
{
  private final JClass stringType;
  
  public IdentityTransducer(JCodeModel codeModel)
  {
    this.stringType = codeModel.ref(String.class);
  }
  
  public JType getReturnType()
  {
    return this.stringType;
  }
  
  public JExpression generateSerializer(JExpression value, SerializerContext context)
  {
    return value;
  }
  
  public JExpression generateDeserializer(JExpression literal, DeserializerContext context)
  {
    return literal;
  }
  
  public JExpression generateConstant(ValueExp exp)
  {
    return JExpr.lit(obtainString(exp));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\xducer\IdentityTransducer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */