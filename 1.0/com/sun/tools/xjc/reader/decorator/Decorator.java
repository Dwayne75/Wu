package com.sun.tools.xjc.reader.decorator;

import com.sun.msv.grammar.Expression;
import com.sun.msv.reader.State;

public abstract interface Decorator
{
  public abstract Expression decorate(State paramState, Expression paramExpression);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\decorator\Decorator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */