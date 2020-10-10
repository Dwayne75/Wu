package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Location;
import com.sun.xml.bind.v2.runtime.SwaRefAdapter;
import java.lang.annotation.Annotation;
import java.util.Collection;
import javax.activation.MimeType;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import javax.xml.namespace.QName;

abstract class PropertyInfoImpl<T, C, F, M>
  implements PropertyInfo<T, C>, Locatable, Comparable<PropertyInfoImpl>
{
  protected final PropertySeed<T, C, F, M> seed;
  private final boolean isCollection;
  private final ID id;
  private final MimeType expectedMimeType;
  private final boolean inlineBinary;
  private final QName schemaType;
  protected final ClassInfoImpl<T, C, F, M> parent;
  private final Adapter<T, C> adapter;
  
  protected PropertyInfoImpl(ClassInfoImpl<T, C, F, M> parent, PropertySeed<T, C, F, M> spi)
  {
    this.seed = spi;
    this.parent = parent;
    if (parent == null) {
      throw new AssertionError();
    }
    MimeType mt = Util.calcExpectedMediaType(this.seed, parent.builder);
    if ((mt != null) && (!kind().canHaveXmlMimeType))
    {
      parent.builder.reportError(new IllegalAnnotationException(Messages.ILLEGAL_ANNOTATION.format(new Object[] { XmlMimeType.class.getName() }), this.seed.readAnnotation(XmlMimeType.class)));
      
      mt = null;
    }
    this.expectedMimeType = mt;
    this.inlineBinary = this.seed.hasAnnotation(XmlInlineBinaryData.class);
    
    T t = this.seed.getRawType();
    
    XmlJavaTypeAdapter xjta = getApplicableAdapter(t);
    if (xjta != null)
    {
      this.isCollection = false;
      this.adapter = new Adapter(xjta, reader(), nav());
    }
    else
    {
      this.isCollection = ((nav().isSubClassOf(t, nav().ref(Collection.class))) || (nav().isArrayButNotByteArray(t)));
      
      xjta = getApplicableAdapter(getIndividualType());
      if (xjta == null)
      {
        XmlAttachmentRef xsa = (XmlAttachmentRef)this.seed.readAnnotation(XmlAttachmentRef.class);
        if (xsa != null)
        {
          parent.builder.hasSwaRef = true;
          this.adapter = new Adapter(nav().asDecl(SwaRefAdapter.class), nav());
        }
        else
        {
          this.adapter = null;
          
          xjta = (XmlJavaTypeAdapter)this.seed.readAnnotation(XmlJavaTypeAdapter.class);
          if (xjta != null)
          {
            T adapter = reader().getClassValue(xjta, "value");
            parent.builder.reportError(new IllegalAnnotationException(Messages.UNMATCHABLE_ADAPTER.format(new Object[] { nav().getTypeName(adapter), nav().getTypeName(t) }), xjta));
          }
        }
      }
      else
      {
        this.adapter = new Adapter(xjta, reader(), nav());
      }
    }
    this.id = calcId();
    this.schemaType = Util.calcSchemaType(reader(), this.seed, parent.clazz, getIndividualType(), this);
  }
  
  public ClassInfoImpl<T, C, F, M> parent()
  {
    return this.parent;
  }
  
  protected final Navigator<T, C, F, M> nav()
  {
    return this.parent.nav();
  }
  
  protected final AnnotationReader<T, C, F, M> reader()
  {
    return this.parent.reader();
  }
  
  public T getRawType()
  {
    return (T)this.seed.getRawType();
  }
  
  public T getIndividualType()
  {
    if (this.adapter != null) {
      return (T)this.adapter.defaultType;
    }
    T raw = getRawType();
    if (!isCollection()) {
      return raw;
    }
    if (nav().isArrayButNotByteArray(raw)) {
      return (T)nav().getComponentType(raw);
    }
    T bt = nav().getBaseClass(raw, nav().asDecl(Collection.class));
    if (nav().isParameterizedType(bt)) {
      return (T)nav().getTypeArgument(bt, 0);
    }
    return (T)nav().ref(Object.class);
  }
  
  public final String getName()
  {
    return this.seed.getName();
  }
  
  private boolean isApplicable(XmlJavaTypeAdapter jta, T declaredType)
  {
    if (jta == null) {
      return false;
    }
    T type = reader().getClassValue(jta, "type");
    if (declaredType.equals(type)) {
      return true;
    }
    T adapter = reader().getClassValue(jta, "value");
    T ba = nav().getBaseClass(adapter, nav().asDecl(XmlAdapter.class));
    if (!nav().isParameterizedType(ba)) {
      return true;
    }
    T inMemType = nav().getTypeArgument(ba, 1);
    
    return nav().isSubClassOf(declaredType, inMemType);
  }
  
  private XmlJavaTypeAdapter getApplicableAdapter(T type)
  {
    XmlJavaTypeAdapter jta = (XmlJavaTypeAdapter)this.seed.readAnnotation(XmlJavaTypeAdapter.class);
    if ((jta != null) && (isApplicable(jta, type))) {
      return jta;
    }
    XmlJavaTypeAdapters jtas = (XmlJavaTypeAdapters)reader().getPackageAnnotation(XmlJavaTypeAdapters.class, this.parent.clazz, this.seed);
    if (jtas != null) {
      for (XmlJavaTypeAdapter xjta : jtas.value()) {
        if (isApplicable(xjta, type)) {
          return xjta;
        }
      }
    }
    jta = (XmlJavaTypeAdapter)reader().getPackageAnnotation(XmlJavaTypeAdapter.class, this.parent.clazz, this.seed);
    if (isApplicable(jta, type)) {
      return jta;
    }
    C refType = nav().asDecl(type);
    if (refType != null)
    {
      jta = (XmlJavaTypeAdapter)reader().getClassAnnotation(XmlJavaTypeAdapter.class, refType, this.seed);
      if ((jta != null) && (isApplicable(jta, type))) {
        return jta;
      }
    }
    return null;
  }
  
  public Adapter<T, C> getAdapter()
  {
    return this.adapter;
  }
  
  public final String displayName()
  {
    return nav().getClassName(this.parent.getClazz()) + '#' + getName();
  }
  
  public final ID id()
  {
    return this.id;
  }
  
  private ID calcId()
  {
    if (this.seed.hasAnnotation(XmlID.class))
    {
      if (!getIndividualType().equals(nav().ref(String.class))) {
        this.parent.builder.reportError(new IllegalAnnotationException(Messages.ID_MUST_BE_STRING.format(new Object[] { getName() }), this.seed));
      }
      return ID.ID;
    }
    if (this.seed.hasAnnotation(XmlIDREF.class)) {
      return ID.IDREF;
    }
    return ID.NONE;
  }
  
  public final MimeType getExpectedMimeType()
  {
    return this.expectedMimeType;
  }
  
  public final boolean inlineBinaryData()
  {
    return this.inlineBinary;
  }
  
  public final QName getSchemaType()
  {
    return this.schemaType;
  }
  
  public final boolean isCollection()
  {
    return this.isCollection;
  }
  
  protected void link()
  {
    if (this.id == ID.IDREF) {
      for (TypeInfo<T, C> ti : ref()) {
        if (!ti.canBeReferencedByIDREF()) {
          this.parent.builder.reportError(new IllegalAnnotationException(Messages.INVALID_IDREF.format(new Object[] { this.parent.builder.nav.getTypeName(ti.getType()) }), this));
        }
      }
    }
  }
  
  public Locatable getUpstream()
  {
    return this.parent;
  }
  
  public Location getLocation()
  {
    return this.seed.getLocation();
  }
  
  protected final QName calcXmlName(XmlElement e)
  {
    if (e != null) {
      return calcXmlName(e.namespace(), e.name());
    }
    return calcXmlName("##default", "##default");
  }
  
  protected final QName calcXmlName(XmlElementWrapper e)
  {
    if (e != null) {
      return calcXmlName(e.namespace(), e.name());
    }
    return calcXmlName("##default", "##default");
  }
  
  private QName calcXmlName(String uri, String local)
  {
    
    if ((local.length() == 0) || (local.equals("##default"))) {
      local = this.seed.getName();
    }
    if (uri.equals("##default"))
    {
      XmlSchema xs = (XmlSchema)reader().getPackageAnnotation(XmlSchema.class, this.parent.getClazz(), this);
      if (xs != null) {
        switch (xs.elementFormDefault())
        {
        case QUALIFIED: 
          QName typeName = this.parent.getTypeName();
          if (typeName != null) {
            uri = typeName.getNamespaceURI();
          } else {
            uri = xs.namespace();
          }
          if (uri.length() == 0) {
            uri = this.parent.builder.defaultNsUri;
          }
          break;
        case UNQUALIFIED: 
        case UNSET: 
          uri = "";
        }
      } else {
        uri = "";
      }
    }
    return new QName(uri.intern(), local.intern());
  }
  
  public int compareTo(PropertyInfoImpl that)
  {
    return getName().compareTo(that.getName());
  }
  
  public final <A extends Annotation> A readAnnotation(Class<A> annotationType)
  {
    return this.seed.readAnnotation(annotationType);
  }
  
  public final boolean hasAnnotation(Class<? extends Annotation> annotationType)
  {
    return this.seed.hasAnnotation(annotationType);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\PropertyInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */