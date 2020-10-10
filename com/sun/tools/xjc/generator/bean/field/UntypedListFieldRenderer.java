package com.sun.tools.xjc.generator.bean.field;

import com.sun.codemodel.JClass;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldOutline;

public final class UntypedListFieldRenderer
  implements FieldRenderer
{
  private JClass coreList;
  
  protected UntypedListFieldRenderer(JClass coreList)
  {
    this.coreList = coreList;
  }
  
  public FieldOutline generate(ClassOutlineImpl context, CPropertyInfo prop)
  {
    return new UntypedListField(context, prop, this.coreList);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\field\UntypedListFieldRenderer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */