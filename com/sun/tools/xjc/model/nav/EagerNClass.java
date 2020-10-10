package com.sun.tools.xjc.model.nav;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class EagerNClass
  extends EagerNType
  implements NClass
{
  final Class c;
  
  public EagerNClass(Class type)
  {
    super(type);
    this.c = type;
  }
  
  public boolean isBoxedType()
  {
    return boxedTypes.contains(this.c);
  }
  
  public JClass toType(Outline o, Aspect aspect)
  {
    return o.getCodeModel().ref(this.c);
  }
  
  public boolean isAbstract()
  {
    return Modifier.isAbstract(this.c.getModifiers());
  }
  
  private static final Set<Class> boxedTypes = new HashSet();
  
  static
  {
    boxedTypes.add(Boolean.class);
    boxedTypes.add(Character.class);
    boxedTypes.add(Byte.class);
    boxedTypes.add(Short.class);
    boxedTypes.add(Integer.class);
    boxedTypes.add(Long.class);
    boxedTypes.add(Float.class);
    boxedTypes.add(Double.class);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\nav\EagerNClass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */