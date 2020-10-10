package com.sun.tools.xjc.model.nav;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import java.lang.reflect.Type;

class EagerNType
  implements NType
{
  final Type t;
  
  public EagerNType(Type type)
  {
    this.t = type;
    assert (this.t != null);
  }
  
  public JType toType(Outline o, Aspect aspect)
  {
    try
    {
      return o.getCodeModel().parseType(this.t.toString());
    }
    catch (ClassNotFoundException e)
    {
      throw new NoClassDefFoundError(e.getMessage());
    }
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EagerNType)) {
      return false;
    }
    EagerNType eagerNType = (EagerNType)o;
    
    return this.t.equals(eagerNType.t);
  }
  
  public boolean isBoxedType()
  {
    return false;
  }
  
  public int hashCode()
  {
    return this.t.hashCode();
  }
  
  public String fullName()
  {
    return Navigator.REFLECTION.getTypeName(this.t);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\nav\EagerNType.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */