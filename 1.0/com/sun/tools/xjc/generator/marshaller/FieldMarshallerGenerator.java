package com.sun.tools.xjc.generator.marshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.generator.util.BlockReference;

public abstract interface FieldMarshallerGenerator
{
  public abstract FieldRenderer owner();
  
  public abstract JExpression peek(boolean paramBoolean);
  
  public abstract void increment(BlockReference paramBlockReference);
  
  public abstract JExpression hasMore();
  
  public abstract FieldMarshallerGenerator clone(JBlock paramJBlock, String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\marshaller\FieldMarshallerGenerator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */