package com.sun.tools.xjc.grammar;

import com.sun.codemodel.JExpression;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.grammar.xducer.Transducer;

public final class DefaultValue
{
  public final Transducer xducer;
  public final ValueExp value;
  
  public DefaultValue(Transducer _xducer, ValueExp _value)
  {
    this.xducer = _xducer;
    this.value = _value;
  }
  
  public JExpression generateConstant()
  {
    return this.xducer.generateConstant(this.value);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\DefaultValue.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */