package com.sun.xml.bind.v2.model.impl;

import com.sun.istack.FinalArrayList;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.AnnotationSource;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.ElementInfo;
import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.TypeRef;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Location;
import com.sun.xml.bind.v2.runtime.SwaRefAdapter;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.activation.MimeType;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlElementDecl.GLOBAL;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

class ElementInfoImpl<T, C, F, M>
  extends TypeInfoImpl<T, C, F, M>
  implements ElementInfo<T, C>
{
  private final QName tagName;
  private final NonElement<T, C> contentType;
  private final T tOfJAXBElementT;
  private final T elementType;
  private final ClassInfo<T, C> scope;
  private final XmlElementDecl anno;
  private ElementInfoImpl<T, C, F, M> substitutionHead;
  private FinalArrayList<ElementInfoImpl<T, C, F, M>> substitutionMembers;
  private final M method;
  private final Adapter<T, C> adapter;
  private final boolean isCollection;
  private final ID id;
  private final ElementInfoImpl<T, C, F, M>.PropertyImpl property;
  private final MimeType expectedMimeType;
  private final boolean inlineBinary;
  private final QName schemaType;
  
  protected class PropertyImpl
    implements ElementPropertyInfo<T, C>, TypeRef<T, C>, AnnotationSource
  {
    protected PropertyImpl() {}
    
    public NonElement<T, C> getTarget()
    {
      return ElementInfoImpl.this.contentType;
    }
    
    public QName getTagName()
    {
      return ElementInfoImpl.this.tagName;
    }
    
    public List<? extends TypeRef<T, C>> getTypes()
    {
      return Collections.singletonList(this);
    }
    
    public List<? extends NonElement<T, C>> ref()
    {
      return Collections.singletonList(ElementInfoImpl.this.contentType);
    }
    
    public QName getXmlName()
    {
      return ElementInfoImpl.this.tagName;
    }
    
    public boolean isCollectionRequired()
    {
      return false;
    }
    
    public boolean isCollectionNillable()
    {
      return true;
    }
    
    public boolean isNillable()
    {
      return true;
    }
    
    public String getDefaultValue()
    {
      String v = ElementInfoImpl.this.anno.defaultValue();
      if (v.equals("\000")) {
        return null;
      }
      return v;
    }
    
    public ElementInfoImpl<T, C, F, M> parent()
    {
      return ElementInfoImpl.this;
    }
    
    public String getName()
    {
      return "value";
    }
    
    public String displayName()
    {
      return "JAXBElement#value";
    }
    
    public boolean isCollection()
    {
      return ElementInfoImpl.this.isCollection;
    }
    
    public boolean isValueList()
    {
      return ElementInfoImpl.this.isCollection;
    }
    
    public boolean isRequired()
    {
      return true;
    }
    
    public PropertyKind kind()
    {
      return PropertyKind.ELEMENT;
    }
    
    public Adapter<T, C> getAdapter()
    {
      return ElementInfoImpl.this.adapter;
    }
    
    public ID id()
    {
      return ElementInfoImpl.this.id;
    }
    
    public MimeType getExpectedMimeType()
    {
      return ElementInfoImpl.this.expectedMimeType;
    }
    
    public QName getSchemaType()
    {
      return ElementInfoImpl.this.schemaType;
    }
    
    public boolean inlineBinaryData()
    {
      return ElementInfoImpl.this.inlineBinary;
    }
    
    public PropertyInfo<T, C> getSource()
    {
      return this;
    }
    
    public <A extends Annotation> A readAnnotation(Class<A> annotationType)
    {
      return ElementInfoImpl.this.reader().getMethodAnnotation(annotationType, ElementInfoImpl.this.method, ElementInfoImpl.this);
    }
    
    public boolean hasAnnotation(Class<? extends Annotation> annotationType)
    {
      return ElementInfoImpl.this.reader().hasMethodAnnotation(annotationType, ElementInfoImpl.this.method);
    }
  }
  
  public ElementInfoImpl(ModelBuilder<T, C, F, M> builder, RegistryInfoImpl<T, C, F, M> registry, M m)
    throws IllegalAnnotationException
  {
    super(builder, registry);
    
    this.method = m;
    this.anno = ((XmlElementDecl)reader().getMethodAnnotation(XmlElementDecl.class, m, this));
    assert (this.anno != null);
    assert ((this.anno instanceof Locatable));
    
    this.elementType = nav().getReturnType(m);
    T baseClass = nav().getBaseClass(this.elementType, nav().asDecl(JAXBElement.class));
    if (baseClass == null) {
      throw new IllegalAnnotationException(Messages.XML_ELEMENT_MAPPING_ON_NON_IXMLELEMENT_METHOD.format(new Object[] { nav().getMethodName(m) }), this.anno);
    }
    this.tagName = parseElementName(this.anno);
    T[] methodParams = nav().getMethodParameters(m);
    
    Adapter<T, C> a = null;
    if (methodParams.length > 0)
    {
      XmlJavaTypeAdapter adapter = (XmlJavaTypeAdapter)reader().getMethodAnnotation(XmlJavaTypeAdapter.class, m, this);
      if (adapter != null)
      {
        a = new Adapter(adapter, reader(), nav());
      }
      else
      {
        XmlAttachmentRef xsa = (XmlAttachmentRef)reader().getMethodAnnotation(XmlAttachmentRef.class, m, this);
        if (xsa != null)
        {
          TODO.prototype("in APT swaRefAdapter isn't avaialble, so this returns null");
          a = new Adapter(this.owner.nav.asDecl(SwaRefAdapter.class), this.owner.nav);
        }
      }
    }
    this.adapter = a;
    
    this.tOfJAXBElementT = (methodParams.length > 0 ? methodParams[0] : nav().getTypeArgument(baseClass, 0));
    if (this.adapter == null)
    {
      T list = nav().getBaseClass(this.tOfJAXBElementT, nav().asDecl(List.class));
      if (list == null)
      {
        this.isCollection = false;
        this.contentType = builder.getTypeInfo(this.tOfJAXBElementT, this);
      }
      else
      {
        this.isCollection = true;
        this.contentType = builder.getTypeInfo(nav().getTypeArgument(list, 0), this);
      }
    }
    else
    {
      this.contentType = builder.getTypeInfo(this.adapter.defaultType, this);
      this.isCollection = false;
    }
    T s = reader().getClassValue(this.anno, "scope");
    if (s.equals(nav().ref(XmlElementDecl.GLOBAL.class)))
    {
      this.scope = null;
    }
    else
    {
      NonElement<T, C> scp = builder.getClassInfo(nav().asDecl(s), this);
      if (!(scp instanceof ClassInfo)) {
        throw new IllegalAnnotationException(Messages.SCOPE_IS_NOT_COMPLEXTYPE.format(new Object[] { nav().getTypeName(s) }), this.anno);
      }
      this.scope = ((ClassInfo)scp);
    }
    this.id = calcId();
    
    this.property = createPropertyImpl();
    
    this.expectedMimeType = Util.calcExpectedMediaType(this.property, builder);
    this.inlineBinary = reader().hasMethodAnnotation(XmlInlineBinaryData.class, this.method);
    this.schemaType = Util.calcSchemaType(reader(), this.property, registry.registryClass, getContentInMemoryType(), this);
  }
  
  final QName parseElementName(XmlElementDecl e)
  {
    String local = e.name();
    String nsUri = e.namespace();
    if (nsUri.equals("##default"))
    {
      XmlSchema xs = (XmlSchema)reader().getPackageAnnotation(XmlSchema.class, nav().getDeclaringClassForMethod(this.method), this);
      if (xs != null) {
        nsUri = xs.namespace();
      } else {
        nsUri = this.builder.defaultNsUri;
      }
    }
    return new QName(nsUri.intern(), local.intern());
  }
  
  protected ElementInfoImpl<T, C, F, M>.PropertyImpl createPropertyImpl()
  {
    return new PropertyImpl();
  }
  
  public ElementPropertyInfo<T, C> getProperty()
  {
    return this.property;
  }
  
  public NonElement<T, C> getContentType()
  {
    return this.contentType;
  }
  
  public T getContentInMemoryType()
  {
    if (this.adapter == null) {
      return (T)this.tOfJAXBElementT;
    }
    return (T)this.adapter.customType;
  }
  
  public QName getElementName()
  {
    return this.tagName;
  }
  
  public T getType()
  {
    return (T)this.elementType;
  }
  
  /**
   * @deprecated
   */
  public final boolean canBeReferencedByIDREF()
  {
    return false;
  }
  
  private ID calcId()
  {
    if (reader().hasMethodAnnotation(XmlID.class, this.method)) {
      return ID.ID;
    }
    if (reader().hasMethodAnnotation(XmlIDREF.class, this.method)) {
      return ID.IDREF;
    }
    return ID.NONE;
  }
  
  public ClassInfo<T, C> getScope()
  {
    return this.scope;
  }
  
  public ElementInfo<T, C> getSubstitutionHead()
  {
    return this.substitutionHead;
  }
  
  public Collection<? extends ElementInfoImpl<T, C, F, M>> getSubstitutionMembers()
  {
    if (this.substitutionMembers == null) {
      return Collections.emptyList();
    }
    return this.substitutionMembers;
  }
  
  void link()
  {
    if (this.anno.substitutionHeadName().length() != 0)
    {
      QName name = new QName(this.anno.substitutionHeadNamespace(), this.anno.substitutionHeadName());
      
      this.substitutionHead = this.owner.getElementInfo(null, name);
      if (this.substitutionHead == null) {
        this.builder.reportError(new IllegalAnnotationException(Messages.NON_EXISTENT_ELEMENT_MAPPING.format(new Object[] { name.getNamespaceURI(), name.getLocalPart() }), this.anno));
      } else {
        this.substitutionHead.addSubstitutionMember(this);
      }
    }
    else
    {
      this.substitutionHead = null;
    }
    super.link();
  }
  
  private void addSubstitutionMember(ElementInfoImpl<T, C, F, M> child)
  {
    if (this.substitutionMembers == null) {
      this.substitutionMembers = new FinalArrayList();
    }
    this.substitutionMembers.add(child);
  }
  
  public Location getLocation()
  {
    return nav().getMethodLocation(this.method);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\ElementInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */