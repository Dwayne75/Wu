package com.sun.tools.xjc.generator.field;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.util.Multiplicity;
import java.util.ArrayList;

public class DefaultFieldRendererFactory
  implements FieldRendererFactory
{
  private FieldRendererFactory defaultCollectionFieldRenderer;
  
  public DefaultFieldRendererFactory(JCodeModel codeModel)
  {
    this(new UntypedListFieldRenderer.Factory(codeModel.ref(ArrayList.class)));
  }
  
  public DefaultFieldRendererFactory(FieldRendererFactory defaultCollectionFieldRenderer)
  {
    this.defaultCollectionFieldRenderer = defaultCollectionFieldRenderer;
  }
  
  public FieldRenderer create(ClassContext context, FieldUse fu)
  {
    if (fu.multiplicity.isAtMostOnce())
    {
      if (fu.isUnboxable()) {
        return new OptionalUnboxedFieldRenderer(context, fu);
      }
      return new SingleFieldRenderer(context, fu);
    }
    return this.defaultCollectionFieldRenderer.create(context, fu);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\field\DefaultFieldRendererFactory.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */