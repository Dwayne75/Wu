package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.Location;
import java.beans.Introspector;
import java.lang.annotation.Annotation;

class GetterSetterPropertySeed<TypeT, ClassDeclT, FieldT, MethodT>
  implements PropertySeed<TypeT, ClassDeclT, FieldT, MethodT>
{
  protected final MethodT getter;
  protected final MethodT setter;
  private ClassInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> parent;
  
  GetterSetterPropertySeed(ClassInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> parent, MethodT getter, MethodT setter)
  {
    this.parent = parent;
    this.getter = getter;
    this.setter = setter;
    if ((getter == null) && (setter == null)) {
      throw new IllegalArgumentException();
    }
  }
  
  public TypeT getRawType()
  {
    if (this.getter != null) {
      return (TypeT)this.parent.nav().getReturnType(this.getter);
    }
    return (TypeT)this.parent.nav().getMethodParameters(this.setter)[0];
  }
  
  public <A extends Annotation> A readAnnotation(Class<A> annotation)
  {
    return this.parent.reader().getMethodAnnotation(annotation, this.getter, this.setter, this);
  }
  
  public boolean hasAnnotation(Class<? extends Annotation> annotationType)
  {
    return this.parent.reader().hasMethodAnnotation(annotationType, getName(), this.getter, this.setter, this);
  }
  
  public String getName()
  {
    if (this.getter != null) {
      return getName(this.getter);
    }
    return getName(this.setter);
  }
  
  private String getName(MethodT m)
  {
    String seed = this.parent.nav().getMethodName(m);
    String lseed = seed.toLowerCase();
    if ((lseed.startsWith("get")) || (lseed.startsWith("set"))) {
      return camelize(seed.substring(3));
    }
    if (lseed.startsWith("is")) {
      return camelize(seed.substring(2));
    }
    return seed;
  }
  
  private static String camelize(String s)
  {
    return Introspector.decapitalize(s);
  }
  
  public Locatable getUpstream()
  {
    return this.parent;
  }
  
  public Location getLocation()
  {
    if (this.getter != null) {
      return this.parent.nav().getMethodLocation(this.getter);
    }
    return this.parent.nav().getMethodLocation(this.setter);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\GetterSetterPropertySeed.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */