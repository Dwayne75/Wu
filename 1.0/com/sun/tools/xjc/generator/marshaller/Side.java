package com.sun.tools.xjc.generator.marshaller;

import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.FieldItem;

abstract interface Side
{
  public abstract void onChoice(ChoiceExp paramChoiceExp);
  
  public abstract void onZeroOrMore(Expression paramExpression);
  
  public abstract void onMarshallableObject();
  
  public abstract void onField(FieldItem paramFieldItem);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\marshaller\Side.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */