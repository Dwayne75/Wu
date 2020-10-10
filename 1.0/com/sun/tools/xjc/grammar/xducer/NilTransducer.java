package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;

public class NilTransducer
  extends TransducerImpl
{
  private final JCodeModel codeModel;
  
  public NilTransducer(JCodeModel _codeModel)
  {
    this.codeModel = _codeModel;
  }
  
  public JType getReturnType()
  {
    return this.codeModel.NULL;
  }
  
  public JExpression generateSerializer(JExpression value, SerializerContext context)
  {
    return JExpr.lit("true");
  }
  
  public JExpression generateDeserializer(JExpression literal, DeserializerContext context)
  {
    return JExpr._null();
  }
  
  public JExpression generateConstant(ValueExp exp)
  {
    throw new UnsupportedOperationException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\xducer\NilTransducer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */