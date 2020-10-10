package com.sun.tools.xjc.generator.field;

import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.grammar.FieldUse;

public abstract interface FieldRendererFactory
{
  public abstract FieldRenderer create(ClassContext paramClassContext, FieldUse paramFieldUse);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\field\FieldRendererFactory.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */