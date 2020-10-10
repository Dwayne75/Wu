package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.EnumLeafInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.Location;
import java.util.Iterator;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.namespace.QName;

class EnumLeafInfoImpl<T, C, F, M>
  extends TypeInfoImpl<T, C, F, M>
  implements EnumLeafInfo<T, C>, Element<T, C>, Iterable<EnumConstantImpl<T, C, F, M>>
{
  final C clazz;
  NonElement<T, C> baseType;
  private final T type;
  private final QName typeName;
  private EnumConstantImpl<T, C, F, M> firstConstant;
  private QName elementName;
  
  public EnumLeafInfoImpl(ModelBuilder<T, C, F, M> builder, Locatable upstream, C clazz, T type)
  {
    super(builder, upstream);
    this.clazz = clazz;
    this.type = type;
    
    this.elementName = parseElementName(clazz);
    
    this.typeName = parseTypeName(clazz);
    
    XmlEnum xe = (XmlEnum)builder.reader.getClassAnnotation(XmlEnum.class, clazz, this);
    if (xe != null)
    {
      T base = builder.reader.getClassValue(xe, "value");
      this.baseType = builder.getTypeInfo(base, this);
    }
    else
    {
      this.baseType = builder.getTypeInfo(builder.nav.ref(String.class), this);
    }
  }
  
  protected void calcConstants()
  {
    EnumConstantImpl<T, C, F, M> last = null;
    F[] constants = nav().getEnumConstants(this.clazz);
    for (int i = constants.length - 1; i >= 0; i--)
    {
      F constant = constants[i];
      String name = nav().getFieldName(constant);
      XmlEnumValue xev = (XmlEnumValue)this.builder.reader.getFieldAnnotation(XmlEnumValue.class, constant, this);
      String literal;
      String literal;
      if (xev == null) {
        literal = name;
      } else {
        literal = xev.value();
      }
      last = createEnumConstant(name, literal, constant, last);
    }
    this.firstConstant = last;
  }
  
  protected EnumConstantImpl<T, C, F, M> createEnumConstant(String name, String literal, F constant, EnumConstantImpl<T, C, F, M> last)
  {
    return new EnumConstantImpl(this, name, literal, last);
  }
  
  public T getType()
  {
    return (T)this.type;
  }
  
  /**
   * @deprecated
   */
  public final boolean canBeReferencedByIDREF()
  {
    return false;
  }
  
  public QName getTypeName()
  {
    return this.typeName;
  }
  
  public C getClazz()
  {
    return (C)this.clazz;
  }
  
  public NonElement<T, C> getBaseType()
  {
    return this.baseType;
  }
  
  public boolean isSimpleType()
  {
    return true;
  }
  
  public Location getLocation()
  {
    return nav().getClassLocation(this.clazz);
  }
  
  public Iterable<? extends EnumConstantImpl<T, C, F, M>> getConstants()
  {
    if (this.firstConstant == null) {
      calcConstants();
    }
    return this;
  }
  
  public void link()
  {
    getConstants();
    super.link();
  }
  
  /**
   * @deprecated
   */
  public Element<T, C> getSubstitutionHead()
  {
    return null;
  }
  
  public QName getElementName()
  {
    return this.elementName;
  }
  
  public boolean isElement()
  {
    return this.elementName != null;
  }
  
  public Element<T, C> asElement()
  {
    if (isElement()) {
      return this;
    }
    return null;
  }
  
  /**
   * @deprecated
   */
  public ClassInfo<T, C> getScope()
  {
    return null;
  }
  
  public Iterator<EnumConstantImpl<T, C, F, M>> iterator()
  {
    new Iterator()
    {
      private EnumConstantImpl<T, C, F, M> next = EnumLeafInfoImpl.this.firstConstant;
      
      public boolean hasNext()
      {
        return this.next != null;
      }
      
      public EnumConstantImpl<T, C, F, M> next()
      {
        EnumConstantImpl<T, C, F, M> r = this.next;
        this.next = this.next.next;
        return r;
      }
      
      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    };
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\EnumLeafInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */