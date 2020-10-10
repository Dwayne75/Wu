package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.MethodLocatable;
import com.sun.xml.bind.v2.model.core.RegistryInfo;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Location;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.annotation.XmlElementDecl;

final class RegistryInfoImpl<T, C, F, M>
  implements Locatable, RegistryInfo<T, C>
{
  final C registryClass;
  private final Locatable upstream;
  private final Navigator<T, C, F, M> nav;
  private final Set<TypeInfo<T, C>> references = new LinkedHashSet();
  
  RegistryInfoImpl(ModelBuilder<T, C, F, M> builder, Locatable upstream, C registryClass)
  {
    this.nav = builder.nav;
    this.registryClass = registryClass;
    this.upstream = upstream;
    builder.registries.put(getPackageName(), this);
    if (this.nav.getDeclaredField(registryClass, "_useJAXBProperties") != null)
    {
      builder.reportError(new IllegalAnnotationException(Messages.MISSING_JAXB_PROPERTIES.format(new Object[] { getPackageName() }), this));
      
      return;
    }
    for (M m : this.nav.getDeclaredMethods(registryClass))
    {
      XmlElementDecl em = (XmlElementDecl)builder.reader.getMethodAnnotation(XmlElementDecl.class, m, this);
      if (em == null)
      {
        if (this.nav.getMethodName(m).startsWith("create")) {
          this.references.add(builder.getTypeInfo(this.nav.getReturnType(m), new MethodLocatable(this, m, this.nav)));
        }
      }
      else
      {
        ElementInfoImpl<T, C, F, M> ei;
        try
        {
          ei = builder.createElementInfo(this, m);
        }
        catch (IllegalAnnotationException e)
        {
          builder.reportError(e);
        }
        continue;
        
        builder.typeInfoSet.add(ei, builder);
        this.references.add(ei);
      }
    }
  }
  
  public Locatable getUpstream()
  {
    return this.upstream;
  }
  
  public Location getLocation()
  {
    return this.nav.getClassLocation(this.registryClass);
  }
  
  public Set<TypeInfo<T, C>> getReferences()
  {
    return this.references;
  }
  
  public String getPackageName()
  {
    return this.nav.getPackageName(this.registryClass);
  }
  
  public C getClazz()
  {
    return (C)this.registryClass;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\RegistryInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */