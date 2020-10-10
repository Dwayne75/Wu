package com.sun.tools.xjc.generator.bean.field;

import com.sun.codemodel.JClass;

public class FieldRendererFactory
{
  public FieldRenderer getDefault()
  {
    return this.DEFAULT;
  }
  
  public FieldRenderer getArray()
  {
    return ARRAY;
  }
  
  public FieldRenderer getRequiredUnboxed()
  {
    return REQUIRED_UNBOXED;
  }
  
  public FieldRenderer getSingle()
  {
    return SINGLE;
  }
  
  public FieldRenderer getSinglePrimitiveAccess()
  {
    return SINGLE_PRIMITIVE_ACCESS;
  }
  
  public FieldRenderer getList(JClass coreList)
  {
    return new UntypedListFieldRenderer(coreList);
  }
  
  public FieldRenderer getConst(FieldRenderer fallback)
  {
    return new ConstFieldRenderer(fallback);
  }
  
  private final FieldRenderer DEFAULT = new DefaultFieldRenderer(this);
  private static final FieldRenderer ARRAY = new GenericFieldRenderer(ArrayField.class);
  private static final FieldRenderer REQUIRED_UNBOXED = new GenericFieldRenderer(UnboxedField.class);
  private static final FieldRenderer SINGLE = new GenericFieldRenderer(SingleField.class);
  private static final FieldRenderer SINGLE_PRIMITIVE_ACCESS = new GenericFieldRenderer(SinglePrimitiveAccessField.class);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\field\FieldRendererFactory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */