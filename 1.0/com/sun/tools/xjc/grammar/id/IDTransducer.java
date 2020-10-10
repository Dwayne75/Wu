package com.sun.tools.xjc.grammar.id;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.grammar.xducer.DeserializerContext;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;
import com.sun.tools.xjc.grammar.xducer.SerializerContext;

public class IDTransducer
  extends IdentityTransducer
{
  private final SymbolSpace symbolSpace;
  
  public IDTransducer(JCodeModel _codeModel, SymbolSpace _symbolSpace)
  {
    super(_codeModel);
    this.symbolSpace = _symbolSpace;
  }
  
  public boolean isID()
  {
    return true;
  }
  
  public SymbolSpace getIDSymbolSpace()
  {
    return this.symbolSpace;
  }
  
  public JExpression generateDeserializer(JExpression literal, DeserializerContext context)
  {
    return context.addToIdTable(literal);
  }
  
  public JExpression generateSerializer(JExpression value, SerializerContext context)
  {
    return context.onID(JExpr._this(), value);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\id\IDTransducer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */