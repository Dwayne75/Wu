package com.sun.xml.bind.v2.model.impl;

import com.sun.istack.FinalArrayList;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.MethodLocatable;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.ValuePropertyInfo;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Location;
import com.sun.xml.bind.v2.util.EditDistance;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlType.DEFAULT;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

class ClassInfoImpl<T, C, F, M>
  extends TypeInfoImpl<T, C, F, M>
  implements ClassInfo<T, C>, Element<T, C>
{
  protected final C clazz;
  private final QName elementName;
  private final QName typeName;
  private FinalArrayList<PropertyInfoImpl<T, C, F, M>> properties;
  private final String[] propOrder;
  private ClassInfoImpl<T, C, F, M> baseClass;
  private boolean baseClassComputed = false;
  private boolean hasSubClasses = false;
  protected PropertySeed<T, C, F, M> attributeWildcard;
  private M factoryMethod = null;
  private static final SecondaryAnnotation[] SECONDARY_ANNOTATIONS;
  private static final Annotation[] EMPTY_ANNOTATIONS;
  private static final HashMap<Class, Integer> ANNOTATION_NUMBER_MAP;
  
  ClassInfoImpl(ModelBuilder<T, C, F, M> builder, Locatable upstream, C clazz)
  {
    super(builder, upstream);
    this.clazz = clazz;
    assert (clazz != null);
    
    this.elementName = parseElementName(clazz);
    
    XmlType t = (XmlType)reader().getClassAnnotation(XmlType.class, clazz, this);
    this.typeName = parseTypeName(clazz, t);
    if (t != null)
    {
      String[] propOrder = t.propOrder();
      if (propOrder.length == 0) {
        this.propOrder = null;
      } else if (propOrder[0].length() == 0) {
        this.propOrder = DEFAULT_ORDER;
      } else {
        this.propOrder = propOrder;
      }
    }
    else
    {
      this.propOrder = DEFAULT_ORDER;
    }
    if (nav().isInterface(clazz)) {
      builder.reportError(new IllegalAnnotationException(Messages.CANT_HANDLE_INTERFACE.format(new Object[] { nav().getClassName(clazz) }), this));
    }
    if ((!hasFactoryConstructor(t)) && 
      (!nav().hasDefaultConstructor(clazz)))
    {
      Messages msg;
      Messages msg;
      if (nav().isInnerClass(clazz)) {
        msg = Messages.CANT_HANDLE_INNER_CLASS;
      } else {
        msg = Messages.NO_DEFAULT_CONSTRUCTOR;
      }
      builder.reportError(new IllegalAnnotationException(msg.format(new Object[] { nav().getClassName(clazz) }), this));
    }
  }
  
  public ClassInfoImpl<T, C, F, M> getBaseClass()
  {
    if (!this.baseClassComputed)
    {
      this.baseClassComputed = true;
      
      C s = nav().getSuperClass(this.clazz);
      if ((s == null) || (s == nav().asDecl(Object.class)))
      {
        this.baseClass = null;
      }
      else
      {
        NonElement<T, C> b = this.builder.getClassInfo(s, true, this);
        if ((b instanceof ClassInfoImpl))
        {
          this.baseClass = ((ClassInfoImpl)b);
          this.baseClass.hasSubClasses = true;
        }
        else
        {
          this.baseClass = null;
        }
      }
    }
    return this.baseClass;
  }
  
  public final Element<T, C> getSubstitutionHead()
  {
    ClassInfoImpl<T, C, F, M> c = getBaseClass();
    while ((c != null) && (!c.isElement())) {
      c = c.getBaseClass();
    }
    return c;
  }
  
  public final C getClazz()
  {
    return (C)this.clazz;
  }
  
  /**
   * @deprecated
   */
  public ClassInfoImpl<T, C, F, M> getScope()
  {
    return null;
  }
  
  public final T getType()
  {
    return (T)nav().use(this.clazz);
  }
  
  public boolean canBeReferencedByIDREF()
  {
    for (PropertyInfo<T, C> p : getProperties()) {
      if (p.id() == ID.ID) {
        return true;
      }
    }
    ClassInfoImpl<T, C, F, M> base = getBaseClass();
    if (base != null) {
      return base.canBeReferencedByIDREF();
    }
    return false;
  }
  
  public final String getName()
  {
    return nav().getClassName(this.clazz);
  }
  
  public <A extends Annotation> A readAnnotation(Class<A> a)
  {
    return reader().getClassAnnotation(a, this.clazz, this);
  }
  
  public Element<T, C> asElement()
  {
    if (isElement()) {
      return this;
    }
    return null;
  }
  
  public List<? extends PropertyInfo<T, C>> getProperties()
  {
    if (this.properties != null) {
      return this.properties;
    }
    XmlAccessType at = getAccessType();
    
    this.properties = new FinalArrayList();
    
    findFieldProperties(this.clazz, at);
    
    findGetterSetterProperties(at);
    if ((this.propOrder == DEFAULT_ORDER) || (this.propOrder == null))
    {
      XmlAccessOrder ao = getAccessorOrder();
      if (ao == XmlAccessOrder.ALPHABETICAL) {
        Collections.sort(this.properties);
      }
    }
    else
    {
      ClassInfoImpl<T, C, F, M>.PropertySorter sorter = new PropertySorter();
      for (PropertyInfoImpl p : this.properties) {
        sorter.checkedGet(p);
      }
      Collections.sort(this.properties, sorter);
      sorter.checkUnusedProperties();
    }
    PropertyInfoImpl vp = null;
    PropertyInfoImpl ep = null;
    for (PropertyInfoImpl p : this.properties) {
      switch (p.kind())
      {
      case ELEMENT: 
      case REFERENCE: 
      case MAP: 
        ep = p;
        break;
      case VALUE: 
        if (vp != null) {
          this.builder.reportError(new IllegalAnnotationException(Messages.MULTIPLE_VALUE_PROPERTY.format(new Object[0]), vp, p));
        }
        if (getBaseClass() != null) {
          this.builder.reportError(new IllegalAnnotationException(Messages.XMLVALUE_IN_DERIVED_TYPE.format(new Object[0]), p));
        }
        vp = p;
        break;
      case ATTRIBUTE: 
        break;
      default: 
        if (!$assertionsDisabled) {
          throw new AssertionError();
        }
        break;
      }
    }
    if ((ep != null) && (vp != null)) {
      this.builder.reportError(new IllegalAnnotationException(Messages.ELEMENT_AND_VALUE_PROPERTY.format(new Object[0]), vp, ep));
    }
    return this.properties;
  }
  
  private void findFieldProperties(C c, XmlAccessType at)
  {
    C sc = nav().getSuperClass(c);
    if (shouldRecurseSuperClass(sc)) {
      findFieldProperties(sc, at);
    }
    for (F f : nav().getDeclaredFields(c))
    {
      Annotation[] annotations = reader().getAllFieldAnnotations(f, this);
      if (nav().isTransient(f))
      {
        if (hasJAXBAnnotation(annotations)) {
          this.builder.reportError(new IllegalAnnotationException(Messages.TRANSIENT_FIELD_NOT_BINDABLE.format(new Object[] { nav().getFieldName(f) }), getSomeJAXBAnnotation(annotations)));
        }
      }
      else if (nav().isStaticField(f))
      {
        if (hasJAXBAnnotation(annotations)) {
          addProperty(createFieldSeed(f), annotations);
        }
      }
      else
      {
        if ((at == XmlAccessType.FIELD) || ((at == XmlAccessType.PUBLIC_MEMBER) && (nav().isPublicField(f))) || (hasJAXBAnnotation(annotations))) {
          addProperty(createFieldSeed(f), annotations);
        }
        checkFieldXmlLocation(f);
      }
    }
  }
  
  public final boolean hasValueProperty()
  {
    ClassInfoImpl<T, C, F, M> bc = getBaseClass();
    if ((bc != null) && (bc.hasValueProperty())) {
      return true;
    }
    for (PropertyInfo p : getProperties()) {
      if ((p instanceof ValuePropertyInfo)) {
        return true;
      }
    }
    return false;
  }
  
  public PropertyInfo<T, C> getProperty(String name)
  {
    for (PropertyInfo<T, C> p : getProperties()) {
      if (p.getName().equals(name)) {
        return p;
      }
    }
    return null;
  }
  
  private <T extends Annotation> T getClassOrPackageAnnotation(Class<T> type)
  {
    T t = reader().getClassAnnotation(type, this.clazz, this);
    if (t != null) {
      return t;
    }
    return reader().getPackageAnnotation(type, this.clazz, this);
  }
  
  private XmlAccessType getAccessType()
  {
    XmlAccessorType xat = (XmlAccessorType)getClassOrPackageAnnotation(XmlAccessorType.class);
    if (xat != null) {
      return xat.value();
    }
    return XmlAccessType.PUBLIC_MEMBER;
  }
  
  private XmlAccessOrder getAccessorOrder()
  {
    XmlAccessorOrder xao = (XmlAccessorOrder)getClassOrPackageAnnotation(XmlAccessorOrder.class);
    if (xao != null) {
      return xao.value();
    }
    return XmlAccessOrder.UNDEFINED;
  }
  
  private final class PropertySorter
    extends HashMap<String, Integer>
    implements Comparator<PropertyInfoImpl>
  {
    PropertyInfoImpl[] used = new PropertyInfoImpl[ClassInfoImpl.this.propOrder.length];
    private Set<String> collidedNames;
    
    PropertySorter()
    {
      super();
      for (String name : ClassInfoImpl.this.propOrder) {
        if (put(name, Integer.valueOf(size())) != null) {
          ClassInfoImpl.this.builder.reportError(new IllegalAnnotationException(Messages.DUPLICATE_ENTRY_IN_PROP_ORDER.format(new Object[] { name }), ClassInfoImpl.this));
        }
      }
    }
    
    public int compare(PropertyInfoImpl o1, PropertyInfoImpl o2)
    {
      int lhs = checkedGet(o1);
      int rhs = checkedGet(o2);
      
      return lhs - rhs;
    }
    
    private int checkedGet(PropertyInfoImpl p)
    {
      Integer i = (Integer)get(p.getName());
      if (i == null)
      {
        if (p.kind().isOrdered) {
          ClassInfoImpl.this.builder.reportError(new IllegalAnnotationException(Messages.PROPERTY_MISSING_FROM_ORDER.format(new Object[] { p.getName() }), p));
        }
        i = Integer.valueOf(size());
        put(p.getName(), i);
      }
      int ii = i.intValue();
      if (ii < this.used.length)
      {
        if ((this.used[ii] != null) && (this.used[ii] != p))
        {
          if (this.collidedNames == null) {
            this.collidedNames = new HashSet();
          }
          if (this.collidedNames.add(p.getName())) {
            ClassInfoImpl.this.builder.reportError(new IllegalAnnotationException(Messages.DUPLICATE_PROPERTIES.format(new Object[] { p.getName() }), p, this.used[ii]));
          }
        }
        this.used[ii] = p;
      }
      return i.intValue();
    }
    
    public void checkUnusedProperties()
    {
      for (int i = 0; i < this.used.length; i++) {
        if (this.used[i] == null)
        {
          String unusedName = ClassInfoImpl.this.propOrder[i];
          String nearest = EditDistance.findNearest(unusedName, new AbstractList()
          {
            public String get(int index)
            {
              return ((PropertyInfoImpl)ClassInfoImpl.this.properties.get(index)).getName();
            }
            
            public int size()
            {
              return ClassInfoImpl.this.properties.size();
            }
          });
          ClassInfoImpl.this.builder.reportError(new IllegalAnnotationException(Messages.PROPERTY_ORDER_CONTAINS_UNUSED_ENTRY.format(new Object[] { unusedName, nearest }), ClassInfoImpl.this));
        }
      }
    }
  }
  
  public boolean hasProperties()
  {
    return !this.properties.isEmpty();
  }
  
  private static <T> T pickOne(T... args)
  {
    for (T arg : args) {
      if (arg != null) {
        return arg;
      }
    }
    return null;
  }
  
  private static <T> List<T> makeSet(T... args)
  {
    List<T> l = new FinalArrayList();
    for (T arg : args) {
      if (arg != null) {
        l.add(arg);
      }
    }
    return l;
  }
  
  private static final class ConflictException
    extends Exception
  {
    final List<Annotation> annotations;
    
    public ConflictException(List<Annotation> one)
    {
      this.annotations = one;
    }
  }
  
  private static final class DupliateException
    extends Exception
  {
    final Annotation a1;
    final Annotation a2;
    
    public DupliateException(Annotation a1, Annotation a2)
    {
      this.a1 = a1;
      this.a2 = a2;
    }
  }
  
  private static enum SecondaryAnnotation
  {
    JAVA_TYPE(1, new Class[] { XmlJavaTypeAdapter.class }),  ID_IDREF(2, new Class[] { XmlID.class, XmlIDREF.class }),  BINARY(4, new Class[] { XmlInlineBinaryData.class, XmlMimeType.class, XmlAttachmentRef.class }),  ELEMENT_WRAPPER(8, new Class[] { XmlElementWrapper.class }),  LIST(16, new Class[] { XmlList.class }),  SCHEMA_TYPE(32, new Class[] { XmlSchemaType.class });
    
    final int bitMask;
    final Class<? extends Annotation>[] members;
    
    private SecondaryAnnotation(int bitMask, Class<? extends Annotation>... members)
    {
      this.bitMask = bitMask;
      this.members = members;
    }
  }
  
  private static enum PropertyGroup
  {
    TRANSIENT(new boolean[] { false, false, false, false, false, false }),  ANY_ATTRIBUTE(new boolean[] { true, false, false, false, false, false }),  ATTRIBUTE(new boolean[] { true, true, true, false, true, true }),  VALUE(new boolean[] { true, true, true, false, true, true }),  ELEMENT(new boolean[] { true, true, true, true, true, true }),  ELEMENT_REF(new boolean[] { true, false, false, true, false, false }),  MAP(new boolean[] { false, false, false, true, false, false });
    
    final int allowedsecondaryAnnotations;
    
    private PropertyGroup(boolean... bits)
    {
      int mask = 0;
      assert (bits.length == ClassInfoImpl.SECONDARY_ANNOTATIONS.length);
      for (int i = 0; i < bits.length; i++) {
        if (bits[i] != 0) {
          mask |= ClassInfoImpl.SECONDARY_ANNOTATIONS[i].bitMask;
        }
      }
      this.allowedsecondaryAnnotations = (mask ^ 0xFFFFFFFF);
    }
    
    boolean allows(ClassInfoImpl.SecondaryAnnotation a)
    {
      return (this.allowedsecondaryAnnotations & a.bitMask) == 0;
    }
  }
  
  static
  {
    SECONDARY_ANNOTATIONS = SecondaryAnnotation.values();
    
    EMPTY_ANNOTATIONS = new Annotation[0];
    
    ANNOTATION_NUMBER_MAP = new HashMap();
    
    Class[] annotations = { XmlTransient.class, XmlAnyAttribute.class, XmlAttribute.class, XmlValue.class, XmlElement.class, XmlElements.class, XmlElementRef.class, XmlElementRefs.class, XmlAnyElement.class, XmlMixed.class };
    
    HashMap<Class, Integer> m = ANNOTATION_NUMBER_MAP;
    for (Class c : annotations) {
      m.put(c, Integer.valueOf(m.size()));
    }
    int index = 20;
    for (SecondaryAnnotation sa : SECONDARY_ANNOTATIONS)
    {
      for (Class member : sa.members) {
        m.put(member, Integer.valueOf(index));
      }
      index++;
    }
  }
  
  private void checkConflict(Annotation a, Annotation b)
    throws ClassInfoImpl.DupliateException
  {
    assert (b != null);
    if (a != null) {
      throw new DupliateException(a, b);
    }
  }
  
  private void addProperty(PropertySeed<T, C, F, M> seed, Annotation[] annotations)
  {
    XmlTransient t = null;
    XmlAnyAttribute aa = null;
    XmlAttribute a = null;
    XmlValue v = null;
    XmlElement e1 = null;
    XmlElements e2 = null;
    XmlElementRef r1 = null;
    XmlElementRefs r2 = null;
    XmlAnyElement xae = null;
    XmlMixed mx = null;
    
    int secondaryAnnotations = 0;
    try
    {
      for (Annotation ann : annotations)
      {
        Integer index = (Integer)ANNOTATION_NUMBER_MAP.get(ann.annotationType());
        if (index != null) {
          switch (index.intValue())
          {
          case 0: 
            checkConflict(t, ann);t = (XmlTransient)ann; break;
          case 1: 
            checkConflict(aa, ann);aa = (XmlAnyAttribute)ann; break;
          case 2: 
            checkConflict(a, ann);a = (XmlAttribute)ann; break;
          case 3: 
            checkConflict(v, ann);v = (XmlValue)ann; break;
          case 4: 
            checkConflict(e1, ann);e1 = (XmlElement)ann; break;
          case 5: 
            checkConflict(e2, ann);e2 = (XmlElements)ann; break;
          case 6: 
            checkConflict(r1, ann);r1 = (XmlElementRef)ann; break;
          case 7: 
            checkConflict(r2, ann);r2 = (XmlElementRefs)ann; break;
          case 8: 
            checkConflict(xae, ann);xae = (XmlAnyElement)ann; break;
          case 9: 
            checkConflict(mx, ann);mx = (XmlMixed)ann; break;
          default: 
            secondaryAnnotations |= 1 << index.intValue() - 20;
          }
        }
      }
      PropertyGroup group = null;
      int groupCount = 0;
      if (t != null)
      {
        group = PropertyGroup.TRANSIENT;
        groupCount++;
      }
      if (aa != null)
      {
        group = PropertyGroup.ANY_ATTRIBUTE;
        groupCount++;
      }
      if (a != null)
      {
        group = PropertyGroup.ATTRIBUTE;
        groupCount++;
      }
      if (v != null)
      {
        group = PropertyGroup.VALUE;
        groupCount++;
      }
      if ((e1 != null) || (e2 != null))
      {
        group = PropertyGroup.ELEMENT;
        groupCount++;
      }
      if ((r1 != null) || (r2 != null) || (xae != null) || (mx != null))
      {
        group = PropertyGroup.ELEMENT_REF;
        groupCount++;
      }
      if (groupCount > 1)
      {
        List<Annotation> err = makeSet(new Annotation[] { t, aa, a, v, (Annotation)pickOne(new Annotation[] { e1, e2 }), (Annotation)pickOne(new Annotation[] { r1, r2, xae }) });
        throw new ConflictException(err);
      }
      if (group == null)
      {
        assert (groupCount == 0);
        if ((nav().isSubClassOf(seed.getRawType(), nav().ref(Map.class))) && (!seed.hasAnnotation(XmlJavaTypeAdapter.class))) {
          group = PropertyGroup.MAP;
        } else {
          group = PropertyGroup.ELEMENT;
        }
      }
      if ((secondaryAnnotations & group.allowedsecondaryAnnotations) != 0)
      {
        for (SecondaryAnnotation sa : SECONDARY_ANNOTATIONS) {
          if (!group.allows(sa)) {
            for (Class<? extends Annotation> m : sa.members)
            {
              Annotation offender = seed.readAnnotation(m);
              if (offender != null)
              {
                this.builder.reportError(new IllegalAnnotationException(Messages.ANNOTATION_NOT_ALLOWED.format(new Object[] { m.getSimpleName() }), offender));
                
                return;
              }
            }
          }
        }
        if (!$assertionsDisabled) {
          throw new AssertionError();
        }
      }
      switch (group)
      {
      case TRANSIENT: 
        return;
      case ANY_ATTRIBUTE: 
        if (this.attributeWildcard != null)
        {
          this.builder.reportError(new IllegalAnnotationException(Messages.TWO_ATTRIBUTE_WILDCARDS.format(new Object[] { nav().getClassName(getClazz()) }), aa, this.attributeWildcard));
          
          return;
        }
        this.attributeWildcard = seed;
        if (inheritsAttributeWildcard())
        {
          this.builder.reportError(new IllegalAnnotationException(Messages.SUPER_CLASS_HAS_WILDCARD.format(new Object[0]), aa, getInheritedAttributeWildcard()));
          
          return;
        }
        if (!nav().isSubClassOf(seed.getRawType(), nav().ref(Map.class)))
        {
          this.builder.reportError(new IllegalAnnotationException(Messages.INVALID_ATTRIBUTE_WILDCARD_TYPE.format(new Object[] { nav().getTypeName(seed.getRawType()) }), aa, getInheritedAttributeWildcard()));
          
          return;
        }
        return;
      case ATTRIBUTE: 
        this.properties.add(createAttributeProperty(seed));
        return;
      case VALUE: 
        this.properties.add(createValueProperty(seed));
        return;
      case ELEMENT: 
        this.properties.add(createElementProperty(seed));
        return;
      case ELEMENT_REF: 
        this.properties.add(createReferenceProperty(seed));
        return;
      case MAP: 
        this.properties.add(createMapProperty(seed));
        return;
      }
      if (!$assertionsDisabled) {
        throw new AssertionError();
      }
    }
    catch (ConflictException x)
    {
      List<Annotation> err = x.annotations;
      
      this.builder.reportError(new IllegalAnnotationException(Messages.MUTUALLY_EXCLUSIVE_ANNOTATIONS.format(new Object[] { nav().getClassName(getClazz()) + '#' + seed.getName(), ((Annotation)err.get(0)).annotationType().getName(), ((Annotation)err.get(1)).annotationType().getName() }), (Annotation)err.get(0), (Annotation)err.get(1)));
    }
    catch (DupliateException e)
    {
      this.builder.reportError(new IllegalAnnotationException(Messages.DUPLICATE_ANNOTATIONS.format(new Object[] { e.a1.annotationType().getName() }), e.a1, e.a2));
    }
  }
  
  protected ReferencePropertyInfoImpl<T, C, F, M> createReferenceProperty(PropertySeed<T, C, F, M> seed)
  {
    return new ReferencePropertyInfoImpl(this, seed);
  }
  
  protected AttributePropertyInfoImpl<T, C, F, M> createAttributeProperty(PropertySeed<T, C, F, M> seed)
  {
    return new AttributePropertyInfoImpl(this, seed);
  }
  
  protected ValuePropertyInfoImpl<T, C, F, M> createValueProperty(PropertySeed<T, C, F, M> seed)
  {
    return new ValuePropertyInfoImpl(this, seed);
  }
  
  protected ElementPropertyInfoImpl<T, C, F, M> createElementProperty(PropertySeed<T, C, F, M> seed)
  {
    return new ElementPropertyInfoImpl(this, seed);
  }
  
  protected MapPropertyInfoImpl<T, C, F, M> createMapProperty(PropertySeed<T, C, F, M> seed)
  {
    return new MapPropertyInfoImpl(this, seed);
  }
  
  private void findGetterSetterProperties(XmlAccessType at)
  {
    Map<String, M> getters = new LinkedHashMap();
    Map<String, M> setters = new LinkedHashMap();
    
    C c = this.clazz;
    do
    {
      collectGetterSetters(this.clazz, getters, setters);
      
      c = nav().getSuperClass(c);
    } while (shouldRecurseSuperClass(c));
    Set<String> complete = new TreeSet(getters.keySet());
    complete.retainAll(setters.keySet());
    
    resurrect(getters, complete);
    resurrect(setters, complete);
    for (String name : complete)
    {
      M getter = getters.get(name);
      M setter = setters.get(name);
      
      Annotation[] ga = getter != null ? reader().getAllMethodAnnotations(getter, new MethodLocatable(this, getter, nav())) : EMPTY_ANNOTATIONS;
      Annotation[] sa = setter != null ? reader().getAllMethodAnnotations(setter, new MethodLocatable(this, setter, nav())) : EMPTY_ANNOTATIONS;
      
      boolean hasAnnotation = (hasJAXBAnnotation(ga)) || (hasJAXBAnnotation(sa));
      boolean isOverriding = false;
      if (!hasAnnotation) {
        isOverriding = ((getter != null) && (nav().isOverriding(getter, c))) || ((setter != null) && (nav().isOverriding(setter, c)));
      }
      if (((at == XmlAccessType.PROPERTY) && (!isOverriding)) || ((at == XmlAccessType.PUBLIC_MEMBER) && (isConsideredPublic(getter)) && (isConsideredPublic(setter)) && (!isOverriding)) || (hasAnnotation)) {
        if ((getter != null) && (setter != null) && (!nav().getReturnType(getter).equals(nav().getMethodParameters(setter)[0])))
        {
          this.builder.reportError(new IllegalAnnotationException(Messages.GETTER_SETTER_INCOMPATIBLE_TYPE.format(new Object[] { nav().getTypeName(nav().getReturnType(getter)), nav().getTypeName(nav().getMethodParameters(setter)[0]) }), new MethodLocatable(this, getter, nav()), new MethodLocatable(this, setter, nav())));
        }
        else
        {
          Annotation[] r;
          Annotation[] r;
          if (ga.length == 0)
          {
            r = sa;
          }
          else
          {
            Annotation[] r;
            if (sa.length == 0)
            {
              r = ga;
            }
            else
            {
              r = new Annotation[ga.length + sa.length];
              System.arraycopy(ga, 0, r, 0, ga.length);
              System.arraycopy(sa, 0, r, ga.length, sa.length);
            }
          }
          addProperty(createAccessorSeed(getter, setter), r);
        }
      }
    }
    getters.keySet().removeAll(complete);
    setters.keySet().removeAll(complete);
  }
  
  private void collectGetterSetters(C c, Map<String, M> getters, Map<String, M> setters)
  {
    C sc = nav().getSuperClass(c);
    if (shouldRecurseSuperClass(sc)) {
      collectGetterSetters(sc, getters, setters);
    }
    Collection<? extends M> methods = nav().getDeclaredMethods(c);
    Map<String, List<M>> allSetters = new LinkedHashMap();
    for (M method : methods)
    {
      boolean used = false;
      if (!nav().isBridgeMethod(method))
      {
        String name = nav().getMethodName(method);
        int arity = nav().getMethodParameters(method).length;
        if (nav().isStaticMethod(method))
        {
          ensureNoAnnotation(method);
        }
        else
        {
          String propName = getPropertyNameFromGetMethod(name);
          if ((propName != null) && (arity == 0))
          {
            getters.put(propName, method);
            used = true;
          }
          propName = getPropertyNameFromSetMethod(name);
          if ((propName != null) && (arity == 1))
          {
            List<M> propSetters = (List)allSetters.get(propName);
            if (null == propSetters)
            {
              propSetters = new ArrayList();
              allSetters.put(propName, propSetters);
            }
            propSetters.add(method);
            used = true;
          }
          if (!used) {
            ensureNoAnnotation(method);
          }
        }
      }
    }
    for (Map.Entry<String, M> entry : getters.entrySet())
    {
      propName = (String)entry.getKey();
      M getter = entry.getValue();
      List<M> propSetters = (List)allSetters.remove(propName);
      if (null != propSetters)
      {
        getterType = nav().getReturnType(getter);
        for (M setter : propSetters)
        {
          T setterType = nav().getMethodParameters(setter)[0];
          if (setterType.equals(getterType))
          {
            setters.put(propName, setter);
            break;
          }
        }
      }
    }
    String propName;
    T getterType;
    for (Map.Entry<String, List<M>> e : allSetters.entrySet()) {
      setters.put(e.getKey(), ((List)e.getValue()).get(0));
    }
  }
  
  private boolean shouldRecurseSuperClass(C sc)
  {
    return (sc != null) && ((this.builder.isReplaced(sc)) || (reader().hasClassAnnotation(sc, XmlTransient.class)));
  }
  
  private boolean isConsideredPublic(M m)
  {
    return (m == null) || (nav().isPublicMethod(m));
  }
  
  private void resurrect(Map<String, M> methods, Set<String> complete)
  {
    for (Map.Entry<String, M> e : methods.entrySet()) {
      if (!complete.contains(e.getKey())) {
        if (hasJAXBAnnotation(reader().getAllMethodAnnotations(e.getValue(), this))) {
          complete.add(e.getKey());
        }
      }
    }
  }
  
  private void ensureNoAnnotation(M method)
  {
    Annotation[] annotations = reader().getAllMethodAnnotations(method, this);
    for (Annotation a : annotations) {
      if (isJAXBAnnotation(a))
      {
        this.builder.reportError(new IllegalAnnotationException(Messages.ANNOTATION_ON_WRONG_METHOD.format(new Object[0]), a));
        
        return;
      }
    }
  }
  
  private static boolean isJAXBAnnotation(Annotation a)
  {
    return ANNOTATION_NUMBER_MAP.containsKey(a.annotationType());
  }
  
  private static boolean hasJAXBAnnotation(Annotation[] annotations)
  {
    return getSomeJAXBAnnotation(annotations) != null;
  }
  
  private static Annotation getSomeJAXBAnnotation(Annotation[] annotations)
  {
    for (Annotation a : annotations) {
      if (isJAXBAnnotation(a)) {
        return a;
      }
    }
    return null;
  }
  
  private static String getPropertyNameFromGetMethod(String name)
  {
    if ((name.startsWith("get")) && (name.length() > 3)) {
      return name.substring(3);
    }
    if ((name.startsWith("is")) && (name.length() > 2)) {
      return name.substring(2);
    }
    return null;
  }
  
  private static String getPropertyNameFromSetMethod(String name)
  {
    if ((name.startsWith("set")) && (name.length() > 3)) {
      return name.substring(3);
    }
    return null;
  }
  
  protected PropertySeed<T, C, F, M> createFieldSeed(F f)
  {
    return new FieldPropertySeed(this, f);
  }
  
  protected PropertySeed<T, C, F, M> createAccessorSeed(M getter, M setter)
  {
    return new GetterSetterPropertySeed(this, getter, setter);
  }
  
  public final boolean isElement()
  {
    return this.elementName != null;
  }
  
  public boolean isAbstract()
  {
    return nav().isAbstract(this.clazz);
  }
  
  public boolean isOrdered()
  {
    return this.propOrder != null;
  }
  
  public final boolean isFinal()
  {
    return nav().isFinal(this.clazz);
  }
  
  public final boolean hasSubClasses()
  {
    return this.hasSubClasses;
  }
  
  public final boolean hasAttributeWildcard()
  {
    return (declaresAttributeWildcard()) || (inheritsAttributeWildcard());
  }
  
  public final boolean inheritsAttributeWildcard()
  {
    return getInheritedAttributeWildcard() != null;
  }
  
  public final boolean declaresAttributeWildcard()
  {
    return this.attributeWildcard != null;
  }
  
  private PropertySeed<T, C, F, M> getInheritedAttributeWildcard()
  {
    for (ClassInfoImpl<T, C, F, M> c = getBaseClass(); c != null; c = c.getBaseClass()) {
      if (c.attributeWildcard != null) {
        return c.attributeWildcard;
      }
    }
    return null;
  }
  
  public final QName getElementName()
  {
    return this.elementName;
  }
  
  public final QName getTypeName()
  {
    return this.typeName;
  }
  
  public final boolean isSimpleType()
  {
    List<? extends PropertyInfo> props = getProperties();
    if (props.size() != 1) {
      return false;
    }
    return ((PropertyInfo)props.get(0)).kind() == PropertyKind.VALUE;
  }
  
  void link()
  {
    getProperties();
    
    Map<String, PropertyInfoImpl> names = new HashMap();
    for (PropertyInfoImpl<T, C, F, M> p : this.properties)
    {
      p.link();
      PropertyInfoImpl old = (PropertyInfoImpl)names.put(p.getName(), p);
      if (old != null) {
        this.builder.reportError(new IllegalAnnotationException(Messages.PROPERTY_COLLISION.format(new Object[] { p.getName() }), p, old));
      }
    }
    super.link();
  }
  
  public Location getLocation()
  {
    return nav().getClassLocation(this.clazz);
  }
  
  private boolean hasFactoryConstructor(XmlType t)
  {
    if (t == null) {
      return false;
    }
    String method = t.factoryMethod();
    T fClass = reader().getClassValue(t, "factoryClass");
    if (method.length() > 0)
    {
      if (fClass.equals(nav().ref(XmlType.DEFAULT.class))) {
        fClass = nav().use(this.clazz);
      }
      for (M m : nav().getDeclaredMethods(nav().asDecl(fClass))) {
        if ((nav().getMethodName(m).equals(method)) && (nav().getReturnType(m).equals(nav().use(this.clazz))) && (nav().getMethodParameters(m).length == 0) && (nav().isStaticMethod(m)))
        {
          this.factoryMethod = m;
          break;
        }
      }
      if (this.factoryMethod == null) {
        this.builder.reportError(new IllegalAnnotationException(Messages.NO_FACTORY_METHOD.format(new Object[] { nav().getClassName(nav().asDecl(fClass)), method }), this));
      }
    }
    else if (!fClass.equals(nav().ref(XmlType.DEFAULT.class)))
    {
      this.builder.reportError(new IllegalAnnotationException(Messages.FACTORY_CLASS_NEEDS_FACTORY_METHOD.format(new Object[] { nav().getClassName(nav().asDecl(fClass)) }), this));
    }
    return this.factoryMethod != null;
  }
  
  public Method getFactoryMethod()
  {
    return (Method)this.factoryMethod;
  }
  
  public String toString()
  {
    return "ClassInfo(" + this.clazz + ')';
  }
  
  private static final String[] DEFAULT_ORDER = new String[0];
  
  protected void checkFieldXmlLocation(F f) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\ClassInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */