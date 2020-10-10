package com.sun.codemodel;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public final class JEnumConstant
  extends JExpressionImpl
  implements JDeclaration, JAnnotatable
{
  private final String name;
  private final JDefinedClass type;
  private JDocComment jdoc = null;
  private List<JAnnotationUse> annotations = null;
  private List<JExpression> args = null;
  
  JEnumConstant(JDefinedClass type, String name)
  {
    this.name = name;
    this.type = type;
  }
  
  public JEnumConstant arg(JExpression arg)
  {
    if (arg == null) {
      throw new IllegalArgumentException();
    }
    if (this.args == null) {
      this.args = new ArrayList();
    }
    this.args.add(arg);
    return this;
  }
  
  public String getName()
  {
    return this.type.fullName().concat(".").concat(this.name);
  }
  
  public JDocComment javadoc()
  {
    if (this.jdoc == null) {
      this.jdoc = new JDocComment(this.type.owner());
    }
    return this.jdoc;
  }
  
  public JAnnotationUse annotate(JClass clazz)
  {
    if (this.annotations == null) {
      this.annotations = new ArrayList();
    }
    JAnnotationUse a = new JAnnotationUse(clazz);
    this.annotations.add(a);
    return a;
  }
  
  public JAnnotationUse annotate(Class<? extends Annotation> clazz)
  {
    return annotate(this.type.owner().ref(clazz));
  }
  
  public <W extends JAnnotationWriter> W annotate2(Class<W> clazz)
  {
    return TypedAnnotationWriter.create(clazz, this);
  }
  
  public void declare(JFormatter f)
  {
    if (this.jdoc != null) {
      f.nl().g(this.jdoc);
    }
    if (this.annotations != null) {
      for (int i = 0; i < this.annotations.size(); i++) {
        f.g((JGenerable)this.annotations.get(i)).nl();
      }
    }
    f.id(this.name);
    if (this.args != null) {
      f.p('(').g(this.args).p(')');
    }
  }
  
  public void generate(JFormatter f)
  {
    f.t(this.type).p('.').p(this.name);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JEnumConstant.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */