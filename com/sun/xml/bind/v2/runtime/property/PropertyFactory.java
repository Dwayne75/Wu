package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeAttributePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeValuePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;

public abstract class PropertyFactory
{
  private static final Constructor<? extends Property>[] propImpls;
  
  static
  {
    Class<? extends Property>[] implClasses = { SingleElementLeafProperty.class, null, null, ArrayElementLeafProperty.class, null, null, SingleElementNodeProperty.class, SingleReferenceNodeProperty.class, SingleMapNodeProperty.class, ArrayElementNodeProperty.class, ArrayReferenceNodeProperty.class, null };
    
    propImpls = new Constructor[implClasses.length];
    for (int i = 0; i < propImpls.length; i++) {
      if (implClasses[i] != null) {
        propImpls[i] = implClasses[i].getConstructors()[0];
      }
    }
  }
  
  public static Property create(JAXBContextImpl grammar, RuntimePropertyInfo info)
  {
    PropertyKind kind = info.kind();
    switch (kind)
    {
    case ATTRIBUTE: 
      return new AttributeProperty(grammar, (RuntimeAttributePropertyInfo)info);
    case VALUE: 
      return new ValueProperty(grammar, (RuntimeValuePropertyInfo)info);
    case ELEMENT: 
      if (((RuntimeElementPropertyInfo)info).isValueList()) {
        return new ListElementProperty(grammar, (RuntimeElementPropertyInfo)info);
      }
      break;
    case REFERENCE: 
    case MAP: 
      break;
    default: 
      if (!$assertionsDisabled) {
        throw new AssertionError();
      }
      break;
    }
    boolean isCollection = info.isCollection();
    boolean isLeaf = isLeaf(info);
    
    Constructor<? extends Property> c = propImpls[(6 + 0 + kind.propertyIndex)];
    try
    {
      return (Property)c.newInstance(new Object[] { grammar, info });
    }
    catch (InstantiationException e)
    {
      throw new InstantiationError(e.getMessage());
    }
    catch (IllegalAccessException e)
    {
      throw new IllegalAccessError(e.getMessage());
    }
    catch (InvocationTargetException e)
    {
      Throwable t = e.getCause();
      if ((t instanceof Error)) {
        throw ((Error)t);
      }
      if ((t instanceof RuntimeException)) {
        throw ((RuntimeException)t);
      }
      throw new AssertionError(t);
    }
  }
  
  static boolean isLeaf(RuntimePropertyInfo info)
  {
    Collection<? extends RuntimeTypeInfo> types = info.ref();
    if (types.size() != 1) {
      return false;
    }
    RuntimeTypeInfo rti = (RuntimeTypeInfo)types.iterator().next();
    if (!(rti instanceof RuntimeNonElement)) {
      return false;
    }
    if (info.id() == ID.IDREF) {
      return true;
    }
    if (((RuntimeNonElement)rti).getTransducer() == null) {
      return false;
    }
    if (!info.getIndividualType().equals(rti.getType())) {
      return false;
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\property\PropertyFactory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */