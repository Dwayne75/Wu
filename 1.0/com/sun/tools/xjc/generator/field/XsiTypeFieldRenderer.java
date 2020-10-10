package com.sun.tools.xjc.generator.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.grammar.FieldUse;

public class XsiTypeFieldRenderer
  extends SingleFieldRenderer
{
  private final JClass defaultObject;
  
  public XsiTypeFieldRenderer(ClassContext context, FieldUse fu, JClass _defaultObject)
  {
    super(context, fu);
    this.defaultObject = _defaultObject;
  }
  
  protected JFieldVar generateField()
  {
    return this.context.implClass.field(2, this.fu.type, "_" + this.fu.name, JExpr._new(this.defaultObject));
  }
  
  public JExpression ifCountEqual(int i)
  {
    if (i == 1) {
      return JExpr.TRUE;
    }
    return JExpr.FALSE;
  }
  
  public JExpression ifCountGte(int i)
  {
    if (i <= 1) {
      return JExpr.TRUE;
    }
    return JExpr.FALSE;
  }
  
  public JExpression ifCountLte(int i)
  {
    if (i == 0) {
      return JExpr.FALSE;
    }
    return JExpr.TRUE;
  }
  
  public JExpression count()
  {
    return JExpr.lit(1);
  }
  
  public FieldMarshallerGenerator createMarshaller(JBlock block, String uniqueId)
  {
    return new XsiTypeFieldRenderer.1(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\field\XsiTypeFieldRenderer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */