package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.util.Which;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.ClassLocatable;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.ErrorHandler;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.Ref;
import com.sun.xml.bind.v2.model.core.RegistryInfo;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

public class ModelBuilder<T, C, F, M>
{
  final TypeInfoSetImpl<T, C, F, M> typeInfoSet;
  public final AnnotationReader<T, C, F, M> reader;
  public final Navigator<T, C, F, M> nav;
  private final Map<QName, TypeInfo> typeNames = new HashMap();
  public final String defaultNsUri;
  final Map<String, RegistryInfoImpl<T, C, F, M>> registries = new HashMap();
  private final Map<C, C> subclassReplacements;
  private ErrorHandler errorHandler;
  private boolean hadError;
  public boolean hasSwaRef;
  private final ErrorHandler proxyErrorHandler = new ErrorHandler()
  {
    public void error(IllegalAnnotationException e)
    {
      ModelBuilder.this.reportError(e);
    }
  };
  private boolean linked;
  
  public ModelBuilder(AnnotationReader<T, C, F, M> reader, Navigator<T, C, F, M> navigator, Map<C, C> subclassReplacements, String defaultNamespaceRemap)
  {
    this.reader = reader;
    this.nav = navigator;
    this.subclassReplacements = subclassReplacements;
    if (defaultNamespaceRemap == null) {
      defaultNamespaceRemap = "";
    }
    this.defaultNsUri = defaultNamespaceRemap;
    reader.setErrorHandler(this.proxyErrorHandler);
    this.typeInfoSet = createTypeInfoSet();
  }
  
  static
  {
    try
    {
      XmlSchema s = null;
      s.location();
    }
    catch (NullPointerException e) {}catch (NoSuchMethodError e)
    {
      Messages res;
      Messages res;
      if (XmlSchema.class.getClassLoader() == null) {
        res = Messages.INCOMPATIBLE_API_VERSION_MUSTANG;
      } else {
        res = Messages.INCOMPATIBLE_API_VERSION;
      }
      throw new LinkageError(res.format(new Object[] { Which.which(XmlSchema.class), Which.which(ModelBuilder.class) }));
    }
    try
    {
      WhiteSpaceProcessor.isWhiteSpace("xyz");
    }
    catch (NoSuchMethodError e)
    {
      throw new LinkageError(Messages.RUNNING_WITH_1_0_RUNTIME.format(new Object[] { Which.which(WhiteSpaceProcessor.class), Which.which(ModelBuilder.class) }));
    }
  }
  
  protected TypeInfoSetImpl<T, C, F, M> createTypeInfoSet()
  {
    return new TypeInfoSetImpl(this.nav, this.reader, BuiltinLeafInfoImpl.createLeaves(this.nav));
  }
  
  public NonElement<T, C> getClassInfo(C clazz, Locatable upstream)
  {
    return getClassInfo(clazz, false, upstream);
  }
  
  public NonElement<T, C> getClassInfo(C clazz, boolean searchForSuperClass, Locatable upstream)
  {
    assert (clazz != null);
    NonElement<T, C> r = this.typeInfoSet.getClassInfo(clazz);
    if (r != null) {
      return r;
    }
    if (this.nav.isEnum(clazz))
    {
      EnumLeafInfoImpl<T, C, F, M> li = createEnumLeafInfo(clazz, upstream);
      this.typeInfoSet.add(li);
      r = li;
      addTypeName(r);
    }
    else
    {
      boolean isReplaced = this.subclassReplacements.containsKey(clazz);
      if ((isReplaced) && (!searchForSuperClass))
      {
        r = getClassInfo(this.subclassReplacements.get(clazz), upstream);
      }
      else if ((this.reader.hasClassAnnotation(clazz, XmlTransient.class)) || (isReplaced))
      {
        r = getClassInfo(this.nav.getSuperClass(clazz), searchForSuperClass, new ClassLocatable(upstream, clazz, this.nav));
      }
      else
      {
        ClassInfoImpl<T, C, F, M> ci = createClassInfo(clazz, upstream);
        this.typeInfoSet.add(ci);
        for (PropertyInfo<T, C> p : ci.getProperties())
        {
          if (p.kind() == PropertyKind.REFERENCE)
          {
            String pkg = this.nav.getPackageName(ci.getClazz());
            if (!this.registries.containsKey(pkg))
            {
              C c = this.nav.findClass(pkg + ".ObjectFactory", ci.getClazz());
              if (c != null) {
                addRegistry(c, (Locatable)p);
              }
            }
          }
          for (Iterator i$ = p.ref().iterator(); i$.hasNext(); t = (TypeInfo)i$.next()) {}
        }
        TypeInfo<T, C> t;
        ci.getBaseClass();
        
        r = ci;
        addTypeName(r);
      }
    }
    XmlSeeAlso sa = (XmlSeeAlso)this.reader.getClassAnnotation(XmlSeeAlso.class, clazz, upstream);
    if (sa != null) {
      for (T t : this.reader.getClassArrayValue(sa, "value")) {
        getTypeInfo(t, (Locatable)sa);
      }
    }
    return r;
  }
  
  private void addTypeName(NonElement<T, C> r)
  {
    QName t = r.getTypeName();
    if (t == null) {
      return;
    }
    TypeInfo old = (TypeInfo)this.typeNames.put(t, r);
    if (old != null) {
      reportError(new IllegalAnnotationException(Messages.CONFLICTING_XML_TYPE_MAPPING.format(new Object[] { r.getTypeName() }), old, r));
    }
  }
  
  public NonElement<T, C> getTypeInfo(T t, Locatable upstream)
  {
    NonElement<T, C> r = this.typeInfoSet.getTypeInfo(t);
    if (r != null) {
      return r;
    }
    if (this.nav.isArray(t))
    {
      ArrayInfoImpl<T, C, F, M> ai = createArrayInfo(upstream, t);
      
      addTypeName(ai);
      this.typeInfoSet.add(ai);
      return ai;
    }
    C c = this.nav.asDecl(t);
    assert (c != null) : (t.toString() + " must be a leaf, but we failed to recognize it.");
    return getClassInfo(c, upstream);
  }
  
  public NonElement<T, C> getTypeInfo(Ref<T, C> ref)
  {
    assert (!ref.valueList);
    C c = this.nav.asDecl(ref.type);
    if ((c != null) && (this.reader.getClassAnnotation(XmlRegistry.class, c, null) != null))
    {
      if (!this.registries.containsKey(this.nav.getPackageName(c))) {
        addRegistry(c, null);
      }
      return null;
    }
    return getTypeInfo(ref.type, null);
  }
  
  protected EnumLeafInfoImpl<T, C, F, M> createEnumLeafInfo(C clazz, Locatable upstream)
  {
    return new EnumLeafInfoImpl(this, upstream, clazz, this.nav.use(clazz));
  }
  
  protected ClassInfoImpl<T, C, F, M> createClassInfo(C clazz, Locatable upstream)
  {
    return new ClassInfoImpl(this, upstream, clazz);
  }
  
  protected ElementInfoImpl<T, C, F, M> createElementInfo(RegistryInfoImpl<T, C, F, M> registryInfo, M m)
    throws IllegalAnnotationException
  {
    return new ElementInfoImpl(this, registryInfo, m);
  }
  
  protected ArrayInfoImpl<T, C, F, M> createArrayInfo(Locatable upstream, T arrayType)
  {
    return new ArrayInfoImpl(this, upstream, arrayType);
  }
  
  public RegistryInfo<T, C> addRegistry(C registryClass, Locatable upstream)
  {
    return new RegistryInfoImpl(this, upstream, registryClass);
  }
  
  public RegistryInfo<T, C> getRegistry(String packageName)
  {
    return (RegistryInfo)this.registries.get(packageName);
  }
  
  public TypeInfoSet<T, C, F, M> link()
  {
    assert (!this.linked);
    this.linked = true;
    for (ElementInfoImpl ei : this.typeInfoSet.getAllElements()) {
      ei.link();
    }
    for (ClassInfoImpl ci : this.typeInfoSet.beans().values()) {
      ci.link();
    }
    for (EnumLeafInfoImpl li : this.typeInfoSet.enums().values()) {
      li.link();
    }
    if (this.hadError) {
      return null;
    }
    return this.typeInfoSet;
  }
  
  public void setErrorHandler(ErrorHandler errorHandler)
  {
    this.errorHandler = errorHandler;
  }
  
  public final void reportError(IllegalAnnotationException e)
  {
    this.hadError = true;
    if (this.errorHandler != null) {
      this.errorHandler.error(e);
    }
  }
  
  public boolean isReplaced(C sc)
  {
    return this.subclassReplacements.containsKey(sc);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\ModelBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */