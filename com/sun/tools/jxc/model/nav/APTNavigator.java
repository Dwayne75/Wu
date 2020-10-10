package com.sun.tools.jxc.model.nav;

import com.sun.istack.tools.APTTypeVisitor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.ConstructorDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.PrimitiveType.Kind;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.TypeVariable;
import com.sun.mirror.type.VoidType;
import com.sun.mirror.type.WildcardType;
import com.sun.mirror.util.Declarations;
import com.sun.mirror.util.SourcePosition;
import com.sun.mirror.util.TypeVisitor;
import com.sun.mirror.util.Types;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.Location;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APTNavigator
  implements Navigator<TypeMirror, TypeDeclaration, FieldDeclaration, MethodDeclaration>
{
  private final AnnotationProcessorEnvironment env;
  private final PrimitiveType primitiveByte;
  private static final Map<Class, PrimitiveType.Kind> primitives;
  
  public APTNavigator(AnnotationProcessorEnvironment env)
  {
    this.env = env;
    this.primitiveByte = env.getTypeUtils().getPrimitiveType(PrimitiveType.Kind.BYTE);
  }
  
  public TypeDeclaration getSuperClass(TypeDeclaration t)
  {
    if ((t instanceof ClassDeclaration))
    {
      ClassDeclaration c = (ClassDeclaration)t;
      ClassType sup = c.getSuperclass();
      if (sup != null) {
        return sup.getDeclaration();
      }
      return null;
    }
    return this.env.getTypeDeclaration(Object.class.getName());
  }
  
  public TypeMirror getBaseClass(TypeMirror type, TypeDeclaration sup)
  {
    return (TypeMirror)this.baseClassFinder.apply(type, sup);
  }
  
  public String getClassName(TypeDeclaration t)
  {
    return t.getQualifiedName();
  }
  
  public String getTypeName(TypeMirror typeMirror)
  {
    return typeMirror.toString();
  }
  
  public String getClassShortName(TypeDeclaration t)
  {
    return t.getSimpleName();
  }
  
  public Collection<FieldDeclaration> getDeclaredFields(TypeDeclaration c)
  {
    List<FieldDeclaration> l = new ArrayList(c.getFields());
    return sort(l);
  }
  
  public FieldDeclaration getDeclaredField(TypeDeclaration clazz, String fieldName)
  {
    for (FieldDeclaration fd : clazz.getFields()) {
      if (fd.getSimpleName().equals(fieldName)) {
        return fd;
      }
    }
    return null;
  }
  
  public Collection<MethodDeclaration> getDeclaredMethods(TypeDeclaration c)
  {
    List<MethodDeclaration> l = new ArrayList(c.getMethods());
    return sort(l);
  }
  
  private <A extends Declaration> List<A> sort(List<A> l)
  {
    if (l.isEmpty()) {
      return l;
    }
    SourcePosition pos = ((Declaration)l.get(0)).getPosition();
    if (pos != null) {
      Collections.sort(l, SOURCE_POS_COMPARATOR);
    } else {
      Collections.reverse(l);
    }
    return l;
  }
  
  public ClassDeclaration getDeclaringClassForField(FieldDeclaration f)
  {
    return (ClassDeclaration)f.getDeclaringType();
  }
  
  public ClassDeclaration getDeclaringClassForMethod(MethodDeclaration m)
  {
    return (ClassDeclaration)m.getDeclaringType();
  }
  
  public TypeMirror getFieldType(FieldDeclaration f)
  {
    return f.getType();
  }
  
  public String getFieldName(FieldDeclaration f)
  {
    return f.getSimpleName();
  }
  
  public String getMethodName(MethodDeclaration m)
  {
    return m.getSimpleName();
  }
  
  public TypeMirror getReturnType(MethodDeclaration m)
  {
    return m.getReturnType();
  }
  
  public TypeMirror[] getMethodParameters(MethodDeclaration m)
  {
    Collection<ParameterDeclaration> ps = m.getParameters();
    TypeMirror[] r = new TypeMirror[ps.size()];
    int i = 0;
    for (ParameterDeclaration p : ps) {
      r[(i++)] = p.getType();
    }
    return r;
  }
  
  public boolean isStaticMethod(MethodDeclaration m)
  {
    return hasModifier(m, Modifier.STATIC);
  }
  
  private boolean hasModifier(Declaration d, Modifier mod)
  {
    return d.getModifiers().contains(mod);
  }
  
  public boolean isSubClassOf(TypeMirror sub, TypeMirror sup)
  {
    if (sup == DUMMY) {
      return false;
    }
    return this.env.getTypeUtils().isSubtype(sub, sup);
  }
  
  private String getSourceClassName(Class clazz)
  {
    Class<?> d = clazz.getDeclaringClass();
    if (d == null) {
      return clazz.getName();
    }
    String shortName = clazz.getName().substring(d.getName().length() + 1);
    return getSourceClassName(d) + '.' + shortName;
  }
  
  public TypeMirror ref(Class c)
  {
    if (c.isArray()) {
      return this.env.getTypeUtils().getArrayType(ref(c.getComponentType()));
    }
    if (c.isPrimitive()) {
      return getPrimitive(c);
    }
    TypeDeclaration t = this.env.getTypeDeclaration(getSourceClassName(c));
    if (t == null) {
      return DUMMY;
    }
    return this.env.getTypeUtils().getDeclaredType(t, new TypeMirror[0]);
  }
  
  public TypeMirror use(TypeDeclaration t)
  {
    assert (t != null);
    return this.env.getTypeUtils().getDeclaredType(t, new TypeMirror[0]);
  }
  
  public TypeDeclaration asDecl(TypeMirror m)
  {
    m = this.env.getTypeUtils().getErasure(m);
    if ((m instanceof DeclaredType))
    {
      DeclaredType d = (DeclaredType)m;
      return d.getDeclaration();
    }
    return null;
  }
  
  public TypeDeclaration asDecl(Class c)
  {
    return this.env.getTypeDeclaration(getSourceClassName(c));
  }
  
  public <T> TypeMirror erasure(TypeMirror t)
  {
    Types tu = this.env.getTypeUtils();
    t = tu.getErasure(t);
    if ((t instanceof DeclaredType))
    {
      DeclaredType dt = (DeclaredType)t;
      if (!dt.getActualTypeArguments().isEmpty()) {
        return tu.getDeclaredType(dt.getDeclaration(), new TypeMirror[0]);
      }
    }
    return t;
  }
  
  public boolean isAbstract(TypeDeclaration clazz)
  {
    return hasModifier(clazz, Modifier.ABSTRACT);
  }
  
  public boolean isFinal(TypeDeclaration clazz)
  {
    return hasModifier(clazz, Modifier.FINAL);
  }
  
  public FieldDeclaration[] getEnumConstants(TypeDeclaration clazz)
  {
    EnumDeclaration ed = (EnumDeclaration)clazz;
    Collection<EnumConstantDeclaration> constants = ed.getEnumConstants();
    return (FieldDeclaration[])constants.toArray(new EnumConstantDeclaration[constants.size()]);
  }
  
  public TypeMirror getVoidType()
  {
    return this.env.getTypeUtils().getVoidType();
  }
  
  public String getPackageName(TypeDeclaration clazz)
  {
    return clazz.getPackage().getQualifiedName();
  }
  
  public TypeDeclaration findClass(String className, TypeDeclaration referencePoint)
  {
    return this.env.getTypeDeclaration(className);
  }
  
  public boolean isBridgeMethod(MethodDeclaration method)
  {
    return method.getModifiers().contains(Modifier.VOLATILE);
  }
  
  public boolean isOverriding(MethodDeclaration method, TypeDeclaration base)
  {
    ClassDeclaration sc = (ClassDeclaration)base;
    
    Declarations declUtil = this.env.getDeclarationUtils();
    for (;;)
    {
      for (MethodDeclaration m : sc.getMethods()) {
        if (declUtil.overrides(method, m)) {
          return true;
        }
      }
      if (sc.getSuperclass() == null) {
        return false;
      }
      sc = sc.getSuperclass().getDeclaration();
    }
  }
  
  public boolean isInterface(TypeDeclaration clazz)
  {
    return clazz instanceof InterfaceDeclaration;
  }
  
  public boolean isTransient(FieldDeclaration f)
  {
    return f.getModifiers().contains(Modifier.TRANSIENT);
  }
  
  public boolean isInnerClass(TypeDeclaration clazz)
  {
    return (clazz.getDeclaringType() != null) && (!clazz.getModifiers().contains(Modifier.STATIC));
  }
  
  public boolean isArray(TypeMirror t)
  {
    return t instanceof ArrayType;
  }
  
  public boolean isArrayButNotByteArray(TypeMirror t)
  {
    if (!isArray(t)) {
      return false;
    }
    ArrayType at = (ArrayType)t;
    TypeMirror ct = at.getComponentType();
    
    return !ct.equals(this.primitiveByte);
  }
  
  public TypeMirror getComponentType(TypeMirror t)
  {
    if ((t instanceof ArrayType))
    {
      ArrayType at = (ArrayType)t;
      return at.getComponentType();
    }
    throw new IllegalArgumentException();
  }
  
  public TypeMirror getTypeArgument(TypeMirror typeMirror, int i)
  {
    if ((typeMirror instanceof DeclaredType))
    {
      DeclaredType d = (DeclaredType)typeMirror;
      TypeMirror[] args = (TypeMirror[])d.getActualTypeArguments().toArray(new TypeMirror[0]);
      return args[i];
    }
    throw new IllegalArgumentException();
  }
  
  public boolean isParameterizedType(TypeMirror t)
  {
    if ((t instanceof DeclaredType))
    {
      DeclaredType d = (DeclaredType)t;
      return !d.getActualTypeArguments().isEmpty();
    }
    return false;
  }
  
  public boolean isPrimitive(TypeMirror t)
  {
    return t instanceof PrimitiveType;
  }
  
  static
  {
    primitives = new HashMap();
    
    primitives.put(Integer.TYPE, PrimitiveType.Kind.INT);
    primitives.put(Byte.TYPE, PrimitiveType.Kind.BYTE);
    primitives.put(Float.TYPE, PrimitiveType.Kind.FLOAT);
    primitives.put(Boolean.TYPE, PrimitiveType.Kind.BOOLEAN);
    primitives.put(Short.TYPE, PrimitiveType.Kind.SHORT);
    primitives.put(Long.TYPE, PrimitiveType.Kind.LONG);
    primitives.put(Double.TYPE, PrimitiveType.Kind.DOUBLE);
    primitives.put(Character.TYPE, PrimitiveType.Kind.CHAR);
  }
  
  public TypeMirror getPrimitive(Class primitiveType)
  {
    assert (primitiveType.isPrimitive());
    if (primitiveType == Void.TYPE) {
      return getVoidType();
    }
    return this.env.getTypeUtils().getPrimitiveType((PrimitiveType.Kind)primitives.get(primitiveType));
  }
  
  private static final TypeMirror DUMMY = new TypeMirror()
  {
    public void accept(TypeVisitor v)
    {
      throw new IllegalStateException();
    }
  };
  private final APTTypeVisitor<TypeMirror, TypeDeclaration> baseClassFinder = new APTTypeVisitor()
  {
    public TypeMirror onClassType(ClassType type, TypeDeclaration sup)
    {
      TypeMirror r = onDeclaredType(type, sup);
      if (r != null) {
        return r;
      }
      if (type.getSuperclass() != null)
      {
        r = onClassType(type.getSuperclass(), sup);
        if (r != null) {
          return r;
        }
      }
      return null;
    }
    
    protected TypeMirror onPrimitiveType(PrimitiveType type, TypeDeclaration param)
    {
      return type;
    }
    
    protected TypeMirror onVoidType(VoidType type, TypeDeclaration param)
    {
      return type;
    }
    
    public TypeMirror onInterfaceType(InterfaceType type, TypeDeclaration sup)
    {
      return onDeclaredType(type, sup);
    }
    
    private TypeMirror onDeclaredType(DeclaredType t, TypeDeclaration sup)
    {
      if (t.getDeclaration().equals(sup)) {
        return t;
      }
      for (InterfaceType i : t.getSuperinterfaces())
      {
        TypeMirror r = onInterfaceType(i, sup);
        if (r != null) {
          return r;
        }
      }
      return null;
    }
    
    public TypeMirror onTypeVariable(TypeVariable t, TypeDeclaration sup)
    {
      for (ReferenceType r : t.getDeclaration().getBounds())
      {
        TypeMirror m = (TypeMirror)apply(r, sup);
        if (m != null) {
          return m;
        }
      }
      return null;
    }
    
    public TypeMirror onArrayType(ArrayType type, TypeDeclaration sup)
    {
      return null;
    }
    
    public TypeMirror onWildcard(WildcardType type, TypeDeclaration sup)
    {
      for (ReferenceType r : type.getLowerBounds())
      {
        TypeMirror m = (TypeMirror)apply(r, sup);
        if (m != null) {
          return m;
        }
      }
      return null;
    }
  };
  
  public Location getClassLocation(TypeDeclaration decl)
  {
    return getLocation(decl.getQualifiedName(), decl.getPosition());
  }
  
  public Location getFieldLocation(FieldDeclaration decl)
  {
    return getLocation(decl);
  }
  
  public Location getMethodLocation(MethodDeclaration decl)
  {
    return getLocation(decl);
  }
  
  public boolean hasDefaultConstructor(TypeDeclaration t)
  {
    if (!(t instanceof ClassDeclaration)) {
      return false;
    }
    ClassDeclaration c = (ClassDeclaration)t;
    for (ConstructorDeclaration init : c.getConstructors()) {
      if (init.getParameters().isEmpty()) {
        return true;
      }
    }
    return false;
  }
  
  public boolean isStaticField(FieldDeclaration f)
  {
    return hasModifier(f, Modifier.STATIC);
  }
  
  public boolean isPublicMethod(MethodDeclaration m)
  {
    return hasModifier(m, Modifier.PUBLIC);
  }
  
  public boolean isPublicField(FieldDeclaration f)
  {
    return hasModifier(f, Modifier.PUBLIC);
  }
  
  public boolean isEnum(TypeDeclaration t)
  {
    return t instanceof EnumDeclaration;
  }
  
  private Location getLocation(MemberDeclaration decl)
  {
    return getLocation(decl.getDeclaringType().getQualifiedName() + '.' + decl.getSimpleName(), decl.getPosition());
  }
  
  private Location getLocation(final String name, final SourcePosition sp)
  {
    new Location()
    {
      public String toString()
      {
        if (sp == null) {
          return name + " (Unknown Source)";
        }
        return name + '(' + sp.file().getName() + ':' + sp.line() + ')';
      }
    };
  }
  
  private static final Comparator<Declaration> SOURCE_POS_COMPARATOR = new Comparator()
  {
    public int compare(Declaration d1, Declaration d2)
    {
      if (d1 == d2) {
        return 0;
      }
      SourcePosition p1 = d1.getPosition();
      SourcePosition p2 = d2.getPosition();
      if (p1 == null) {
        return p2 == null ? 0 : 1;
      }
      if (p2 == null) {
        return -1;
      }
      int fileComp = p1.file().compareTo(p2.file());
      if (fileComp == 0)
      {
        long diff = p1.line() - p2.line();
        if (diff == 0L)
        {
          diff = Long.signum(p1.column() - p2.column());
          if (diff != 0L) {
            return (int)diff;
          }
          return Long.signum(System.identityHashCode(d1) - System.identityHashCode(d2));
        }
        return diff < 0L ? -1 : 1;
      }
      return fileComp;
    }
  };
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\jxc\model\nav\APTNavigator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */