package com.sun.tools.xjc.generator.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.MethodWriter;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.grammar.FieldUse;

public class IsSetFieldRenderer
  implements FieldRenderer
{
  private final FieldRenderer core;
  private final ClassContext context;
  private final FieldUse use;
  private final boolean generateUnSetMethod;
  private final boolean generateIsSetMethod;
  
  public static FieldRendererFactory createFactory(FieldRendererFactory core, boolean generateUnSetMethod, boolean generateIsSetMethod)
  {
    return new IsSetFieldRenderer.1(core, generateUnSetMethod, generateIsSetMethod);
  }
  
  public static FieldRendererFactory createFactory(FieldRendererFactory core)
  {
    return createFactory(core, true, true);
  }
  
  public IsSetFieldRenderer(ClassContext _context, FieldUse _use, FieldRenderer _core, boolean generateUnSetMethod, boolean generateIsSetMethod)
  {
    this.core = _core;
    this.context = _context;
    this.use = _use;
    this.generateUnSetMethod = generateUnSetMethod;
    this.generateIsSetMethod = generateIsSetMethod;
  }
  
  public void generate()
  {
    this.core.generate();
    
    MethodWriter writer = this.context.createMethodWriter();
    
    JCodeModel codeModel = this.context.parent.getCodeModel();
    if (this.generateIsSetMethod)
    {
      JExpression hasSetValue = this.core.hasSetValue();
      if (hasSetValue == null) {}
      writer.declareMethod(codeModel.BOOLEAN, "isSet" + this.use.name).body()._return(hasSetValue);
    }
    if (this.generateUnSetMethod) {
      this.core.unsetValues(writer.declareMethod(codeModel.VOID, "unset" + this.use.name).body());
    }
  }
  
  public JBlock getOnSetEventHandler()
  {
    return this.core.getOnSetEventHandler();
  }
  
  public void unsetValues(JBlock body)
  {
    this.core.unsetValues(body);
  }
  
  public void toArray(JBlock block, JExpression $array)
  {
    this.core.toArray(block, $array);
  }
  
  public JExpression hasSetValue()
  {
    return this.core.hasSetValue();
  }
  
  public JExpression getValue()
  {
    return this.core.getValue();
  }
  
  public JClass getValueType()
  {
    return this.core.getValueType();
  }
  
  public FieldUse getFieldUse()
  {
    return this.core.getFieldUse();
  }
  
  public void setter(JBlock body, JExpression newValue)
  {
    this.core.setter(body, newValue);
  }
  
  public JExpression ifCountEqual(int i)
  {
    return this.core.ifCountEqual(i);
  }
  
  public JExpression ifCountGte(int i)
  {
    return this.core.ifCountGte(i);
  }
  
  public JExpression ifCountLte(int i)
  {
    return this.core.ifCountLte(i);
  }
  
  public JExpression count()
  {
    return this.core.count();
  }
  
  public FieldMarshallerGenerator createMarshaller(JBlock block, String uniqueId)
  {
    return this.core.createMarshaller(block, uniqueId);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\field\IsSetFieldRenderer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */