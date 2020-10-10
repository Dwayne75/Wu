package com.sun.tools.xjc.generator.bean.field;

import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldOutline;

public class IsSetFieldRenderer
  implements FieldRenderer
{
  private final FieldRenderer core;
  private final boolean generateUnSetMethod;
  private final boolean generateIsSetMethod;
  
  public IsSetFieldRenderer(FieldRenderer core, boolean generateUnSetMethod, boolean generateIsSetMethod)
  {
    this.core = core;
    this.generateUnSetMethod = generateUnSetMethod;
    this.generateIsSetMethod = generateIsSetMethod;
  }
  
  public FieldOutline generate(ClassOutlineImpl context, CPropertyInfo prop)
  {
    return new IsSetField(context, prop, this.core.generate(context, prop), this.generateUnSetMethod, this.generateIsSetMethod);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\field\IsSetFieldRenderer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */