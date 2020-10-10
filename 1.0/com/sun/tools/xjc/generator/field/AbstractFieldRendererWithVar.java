package com.sun.tools.xjc.generator.field;

import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.grammar.FieldUse;

abstract class AbstractFieldRendererWithVar
  extends AbstractFieldRenderer
{
  private JFieldVar field;
  
  protected AbstractFieldRendererWithVar(ClassContext _context, FieldUse _fu)
  {
    super(_context, _fu);
  }
  
  public final void generate()
  {
    this.field = generateField();
    generateAccessors();
  }
  
  public JFieldVar ref()
  {
    return this.field;
  }
  
  protected JFieldVar generateField()
  {
    return generateField(getValueType());
  }
  
  public abstract void generateAccessors();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\field\AbstractFieldRendererWithVar.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */