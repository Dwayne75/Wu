package com.sun.tools.xjc.generator.bean.field;

import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldOutline;

public abstract interface FieldRenderer
{
  public abstract FieldOutline generate(ClassOutlineImpl paramClassOutlineImpl, CPropertyInfo paramCPropertyInfo);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\field\FieldRenderer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */