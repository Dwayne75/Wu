package com.sun.tools.xjc.grammar.id;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.util.WhitespaceNormalizer;
import com.sun.tools.xjc.grammar.xducer.DeserializerContext;
import com.sun.tools.xjc.grammar.xducer.SerializerContext;
import com.sun.tools.xjc.grammar.xducer.TransducerImpl;
import com.sun.xml.bind.marshaller.IdentifiableObject;

public class IDREFTransducer
  extends TransducerImpl
{
  private final JCodeModel codeModel;
  private final SymbolSpace symbolSpace;
  private final boolean whitespaceNormalization;
  
  public IDREFTransducer(JCodeModel _codeModel, SymbolSpace _symbolSpace, boolean _whitespaceNormalization)
  {
    this.codeModel = _codeModel;
    this.symbolSpace = _symbolSpace;
    this.whitespaceNormalization = _whitespaceNormalization;
  }
  
  public SymbolSpace getIDSymbolSpace()
  {
    return this.symbolSpace;
  }
  
  public JType getReturnType()
  {
    return this.symbolSpace.getType();
  }
  
  public JExpression generateSerializer(JExpression literal, SerializerContext context)
  {
    return context.onIDREF(JExpr.cast(this.codeModel.ref(IdentifiableObject.class), literal));
  }
  
  public JExpression generateDeserializer(JExpression literal, DeserializerContext context)
  {
    return JExpr.cast(this.symbolSpace.getType(), context.getObjectFromId(this.whitespaceNormalization ? WhitespaceNormalizer.COLLAPSE.generate(this.codeModel, literal) : literal));
  }
  
  public boolean needsDelayedDeserialization()
  {
    return true;
  }
  
  public String toString()
  {
    return "IDREFTransducer:" + this.symbolSpace.toString();
  }
  
  public JExpression generateConstant(ValueExp exp)
  {
    throw new UnsupportedOperationException(Messages.format("IDREFTransducer.ConstantIDREFError"));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\id\IDREFTransducer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */