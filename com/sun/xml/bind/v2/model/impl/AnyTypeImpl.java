package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.Location;
import javax.xml.namespace.QName;

class AnyTypeImpl<T, C>
  implements NonElement<T, C>
{
  private final T type;
  private final Navigator<T, C, ?, ?> nav;
  
  public AnyTypeImpl(Navigator<T, C, ?, ?> nav)
  {
    this.type = nav.ref(Object.class);
    this.nav = nav;
  }
  
  public QName getTypeName()
  {
    return name;
  }
  
  public T getType()
  {
    return (T)this.type;
  }
  
  public Locatable getUpstream()
  {
    return null;
  }
  
  public boolean isSimpleType()
  {
    return false;
  }
  
  public Location getLocation()
  {
    return this.nav.getClassLocation(this.nav.asDecl(Object.class));
  }
  
  /**
   * @deprecated
   */
  public final boolean canBeReferencedByIDREF()
  {
    return true;
  }
  
  private static final QName name = new QName("http://www.w3.org/2001/XMLSchema", "anyType");
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\AnyTypeImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */