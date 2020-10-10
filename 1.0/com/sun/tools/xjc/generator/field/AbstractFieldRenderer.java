package com.sun.tools.xjc.generator.field;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.MethodWriter;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.xml.bind.JAXBAssertionError;

abstract class AbstractFieldRenderer
  implements FieldRenderer
{
  protected final JCodeModel codeModel;
  protected final ClassContext context;
  protected final FieldUse fu;
  protected final MethodWriter writer;
  
  protected AbstractFieldRenderer(ClassContext _context, FieldUse _fu)
  {
    this.context = _context;
    this.fu = _fu;
    this.codeModel = _context.parent.getCodeModel();
    this.writer = this.context.createMethodWriter();
  }
  
  public final FieldUse getFieldUse()
  {
    return this.fu;
  }
  
  protected final JFieldVar generateField(JType type)
  {
    return this.context.implClass.field(2, type, "_" + this.fu.name);
  }
  
  protected final void _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\field\AbstractFieldRenderer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */