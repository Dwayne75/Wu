package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.Location;
import java.lang.annotation.Annotation;

class FieldPropertySeed<TypeT, ClassDeclT, FieldT, MethodT>
  implements PropertySeed<TypeT, ClassDeclT, FieldT, MethodT>
{
  protected final FieldT field;
  private ClassInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> parent;
  
  FieldPropertySeed(ClassInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> classInfo, FieldT field)
  {
    this.parent = classInfo;
    this.field = field;
  }
  
  public <A extends Annotation> A readAnnotation(Class<A> a)
  {
    return this.parent.reader().getFieldAnnotation(a, this.field, this);
  }
  
  public boolean hasAnnotation(Class<? extends Annotation> annotationType)
  {
    return this.parent.reader().hasFieldAnnotation(annotationType, this.field);
  }
  
  public String getName()
  {
    return this.parent.nav().getFieldName(this.field);
  }
  
  public TypeT getRawType()
  {
    return (TypeT)this.parent.nav().getFieldType(this.field);
  }
  
  public Locatable getUpstream()
  {
    return this.parent;
  }
  
  public Location getLocation()
  {
    return this.parent.nav().getFieldLocation(this.field);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\FieldPropertySeed.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */