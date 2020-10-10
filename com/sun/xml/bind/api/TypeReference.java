package com.sun.xml.bind.api;

import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.nav.ReflectionNavigator;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import javax.xml.namespace.QName;

public final class TypeReference
{
  public final QName tagName;
  public final Type type;
  public final Annotation[] annotations;
  
  public TypeReference(QName tagName, Type type, Annotation... annotations)
  {
    if ((tagName == null) || (type == null) || (annotations == null)) {
      throw new IllegalArgumentException();
    }
    this.tagName = new QName(tagName.getNamespaceURI().intern(), tagName.getLocalPart().intern(), tagName.getPrefix());
    this.type = type;
    this.annotations = annotations;
  }
  
  public <A extends Annotation> A get(Class<A> annotationType)
  {
    for (Annotation a : this.annotations) {
      if (a.annotationType() == annotationType) {
        return (Annotation)annotationType.cast(a);
      }
    }
    return null;
  }
  
  public TypeReference toItemType()
  {
    Type base = Navigator.REFLECTION.getBaseClass(this.type, Collection.class);
    if (base == null) {
      return this;
    }
    return new TypeReference(this.tagName, Navigator.REFLECTION.getTypeArgument(base, 0), new Annotation[0]);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\api\TypeReference.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */