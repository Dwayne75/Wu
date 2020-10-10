package com.sun.codemodel;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public final class JAnnotationArrayMember
  extends JAnnotationValue
  implements JAnnotatable
{
  private final List<JAnnotationValue> values = new ArrayList();
  private final JCodeModel owner;
  
  JAnnotationArrayMember(JCodeModel owner)
  {
    this.owner = owner;
  }
  
  public JAnnotationArrayMember param(String value)
  {
    JAnnotationValue annotationValue = new JAnnotationStringValue(JExpr.lit(value));
    this.values.add(annotationValue);
    return this;
  }
  
  public JAnnotationArrayMember param(boolean value)
  {
    JAnnotationValue annotationValue = new JAnnotationStringValue(JExpr.lit(value));
    this.values.add(annotationValue);
    return this;
  }
  
  public JAnnotationArrayMember param(int value)
  {
    JAnnotationValue annotationValue = new JAnnotationStringValue(JExpr.lit(value));
    this.values.add(annotationValue);
    return this;
  }
  
  public JAnnotationArrayMember param(float value)
  {
    JAnnotationValue annotationValue = new JAnnotationStringValue(JExpr.lit(value));
    this.values.add(annotationValue);
    return this;
  }
  
  public JAnnotationArrayMember param(Class value)
  {
    JAnnotationValue annotationValue = new JAnnotationStringValue(JExpr.lit(value.getName()));
    this.values.add(annotationValue);
    return this;
  }
  
  public JAnnotationArrayMember param(JType type)
  {
    JClass clazz = type.boxify();
    JAnnotationValue annotationValue = new JAnnotationStringValue(clazz.dotclass());
    this.values.add(annotationValue);
    return this;
  }
  
  public JAnnotationUse annotate(Class<? extends Annotation> clazz)
  {
    return annotate(this.owner.ref(clazz));
  }
  
  public JAnnotationUse annotate(JClass clazz)
  {
    JAnnotationUse a = new JAnnotationUse(clazz);
    this.values.add(a);
    return a;
  }
  
  public <W extends JAnnotationWriter> W annotate2(Class<W> clazz)
  {
    return TypedAnnotationWriter.create(clazz, this);
  }
  
  /**
   * @deprecated
   */
  public JAnnotationArrayMember param(JAnnotationUse value)
  {
    this.values.add(value);
    return this;
  }
  
  public void generate(JFormatter f)
  {
    f.p('{').nl().i();
    
    boolean first = true;
    for (JAnnotationValue aValue : this.values)
    {
      if (!first) {
        f.p(',').nl();
      }
      f.g(aValue);
      first = false;
    }
    f.nl().o().p('}');
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JAnnotationArrayMember.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */