package com.sun.tools.xjc.generator.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.grammar.FieldUse;

public abstract interface FieldRenderer
{
  public abstract void generate();
  
  public abstract JBlock getOnSetEventHandler();
  
  public abstract FieldUse getFieldUse();
  
  public abstract void setter(JBlock paramJBlock, JExpression paramJExpression);
  
  public abstract void toArray(JBlock paramJBlock, JExpression paramJExpression);
  
  public abstract void unsetValues(JBlock paramJBlock);
  
  public abstract JExpression hasSetValue();
  
  public abstract JExpression getValue();
  
  public abstract JClass getValueType();
  
  public abstract JExpression ifCountEqual(int paramInt);
  
  public abstract JExpression ifCountGte(int paramInt);
  
  public abstract JExpression ifCountLte(int paramInt);
  
  public abstract JExpression count();
  
  public abstract FieldMarshallerGenerator createMarshaller(JBlock paramJBlock, String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\field\FieldRenderer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */