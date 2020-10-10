package com.sun.tools.xjc.generator.bean.field;

import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CDefaultValue;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldOutline;

final class ConstFieldRenderer
  implements FieldRenderer
{
  private final FieldRenderer fallback;
  
  protected ConstFieldRenderer(FieldRenderer fallback)
  {
    this.fallback = fallback;
  }
  
  public FieldOutline generate(ClassOutlineImpl outline, CPropertyInfo prop)
  {
    if (prop.defaultValue.compute(outline.parent()) == null) {
      return this.fallback.generate(outline, prop);
    }
    return new ConstField(outline, prop);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\field\ConstFieldRenderer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */