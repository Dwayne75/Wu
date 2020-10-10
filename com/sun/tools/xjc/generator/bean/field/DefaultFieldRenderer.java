package com.sun.tools.xjc.generator.bean.field;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.generator.bean.BeanGenerator;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldOutline;
import java.util.ArrayList;

final class DefaultFieldRenderer
  implements FieldRenderer
{
  private final FieldRendererFactory frf;
  private FieldRenderer defaultCollectionFieldRenderer;
  
  DefaultFieldRenderer(FieldRendererFactory frf)
  {
    this.frf = frf;
  }
  
  public DefaultFieldRenderer(FieldRendererFactory frf, FieldRenderer defaultCollectionFieldRenderer)
  {
    this.frf = frf;
    this.defaultCollectionFieldRenderer = defaultCollectionFieldRenderer;
  }
  
  public FieldOutline generate(ClassOutlineImpl outline, CPropertyInfo prop)
  {
    return decideRenderer(outline, prop).generate(outline, prop);
  }
  
  private FieldRenderer decideRenderer(ClassOutlineImpl outline, CPropertyInfo prop)
  {
    if (!prop.isCollection())
    {
      if (prop.isUnboxable()) {
        return this.frf.getRequiredUnboxed();
      }
      return this.frf.getSingle();
    }
    if (this.defaultCollectionFieldRenderer == null) {
      return this.frf.getList(outline.parent().getCodeModel().ref(ArrayList.class));
    }
    return this.defaultCollectionFieldRenderer;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\field\DefaultFieldRenderer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */