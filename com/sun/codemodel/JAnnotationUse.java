package com.sun.codemodel;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class JAnnotationUse
  extends JAnnotationValue
{
  private final JClass clazz;
  private Map<String, JAnnotationValue> memberValues;
  
  JAnnotationUse(JClass clazz)
  {
    this.clazz = clazz;
  }
  
  private JCodeModel owner()
  {
    return this.clazz.owner();
  }
  
  private void addValue(String name, JAnnotationValue annotationValue)
  {
    if (this.memberValues == null) {
      this.memberValues = new LinkedHashMap();
    }
    this.memberValues.put(name, annotationValue);
  }
  
  public JAnnotationUse param(String name, boolean value)
  {
    addValue(name, new JAnnotationStringValue(JExpr.lit(value)));
    return this;
  }
  
  public JAnnotationUse param(String name, int value)
  {
    addValue(name, new JAnnotationStringValue(JExpr.lit(value)));
    return this;
  }
  
  public JAnnotationUse param(String name, String value)
  {
    addValue(name, new JAnnotationStringValue(JExpr.lit(value)));
    return this;
  }
  
  public JAnnotationUse annotationParam(String name, Class<? extends Annotation> value)
  {
    JAnnotationUse annotationUse = new JAnnotationUse(owner().ref(value));
    addValue(name, annotationUse);
    return annotationUse;
  }
  
  public JAnnotationUse param(String name, final Enum value)
  {
    addValue(name, new JAnnotationValue()
    {
      public void generate(JFormatter f)
      {
        f.t(JAnnotationUse.this.owner().ref(value.getDeclaringClass())).p('.').p(value.name());
      }
    });
    return this;
  }
  
  public JAnnotationUse param(String name, JEnumConstant value)
  {
    addValue(name, new JAnnotationStringValue(value));
    return this;
  }
  
  public JAnnotationUse param(String name, Class value)
  {
    return param(name, this.clazz.owner().ref(value));
  }
  
  public JAnnotationUse param(String name, JType type)
  {
    JClass clazz = type.boxify();
    addValue(name, new JAnnotationStringValue(clazz.dotclass()));
    return this;
  }
  
  public JAnnotationArrayMember paramArray(String name)
  {
    JAnnotationArrayMember arrayMember = new JAnnotationArrayMember(owner());
    addValue(name, arrayMember);
    return arrayMember;
  }
  
  /**
   * @deprecated
   */
  public JAnnotationUse annotate(Class<? extends Annotation> clazz)
  {
    JAnnotationUse annotationUse = new JAnnotationUse(owner().ref(clazz));
    return annotationUse;
  }
  
  public void generate(JFormatter f)
  {
    f.p('@').g(this.clazz);
    if (this.memberValues != null)
    {
      f.p('(');
      boolean first = true;
      if (isOptimizable()) {
        f.g((JGenerable)this.memberValues.get("value"));
      } else {
        for (Map.Entry<String, JAnnotationValue> mapEntry : this.memberValues.entrySet())
        {
          if (!first) {
            f.p(',');
          }
          f.p((String)mapEntry.getKey()).p('=').g((JGenerable)mapEntry.getValue());
          first = false;
        }
      }
      f.p(')');
    }
  }
  
  private boolean isOptimizable()
  {
    return (this.memberValues.size() == 1) && (this.memberValues.containsKey("value"));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JAnnotationUse.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */