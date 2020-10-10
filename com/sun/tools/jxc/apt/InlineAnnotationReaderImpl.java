package com.sun.tools.jxc.apt;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.AnnotationType;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.MirroredTypesException;
import com.sun.mirror.type.TypeMirror;
import com.sun.xml.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.LocatableAnnotation;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class InlineAnnotationReaderImpl
  extends AbstractInlineAnnotationReaderImpl<TypeMirror, TypeDeclaration, FieldDeclaration, MethodDeclaration>
{
  public static final InlineAnnotationReaderImpl theInstance = new InlineAnnotationReaderImpl();
  
  public <A extends Annotation> A getClassAnnotation(Class<A> a, TypeDeclaration clazz, Locatable srcPos)
  {
    return LocatableAnnotation.create(clazz.getAnnotation(a), srcPos);
  }
  
  public <A extends Annotation> A getFieldAnnotation(Class<A> a, FieldDeclaration f, Locatable srcPos)
  {
    return LocatableAnnotation.create(f.getAnnotation(a), srcPos);
  }
  
  public boolean hasFieldAnnotation(Class<? extends Annotation> annotationType, FieldDeclaration f)
  {
    return f.getAnnotation(annotationType) != null;
  }
  
  public boolean hasClassAnnotation(TypeDeclaration clazz, Class<? extends Annotation> annotationType)
  {
    return clazz.getAnnotation(annotationType) != null;
  }
  
  public Annotation[] getAllFieldAnnotations(FieldDeclaration field, Locatable srcPos)
  {
    return getAllAnnotations(field, srcPos);
  }
  
  public <A extends Annotation> A getMethodAnnotation(Class<A> a, MethodDeclaration method, Locatable srcPos)
  {
    return LocatableAnnotation.create(method.getAnnotation(a), srcPos);
  }
  
  public boolean hasMethodAnnotation(Class<? extends Annotation> a, MethodDeclaration method)
  {
    return method.getAnnotation(a) != null;
  }
  
  private static final Annotation[] EMPTY_ANNOTATION = new Annotation[0];
  
  public Annotation[] getAllMethodAnnotations(MethodDeclaration method, Locatable srcPos)
  {
    return getAllAnnotations(method, srcPos);
  }
  
  private Annotation[] getAllAnnotations(Declaration decl, Locatable srcPos)
  {
    List<Annotation> r = new ArrayList();
    for (AnnotationMirror m : decl.getAnnotationMirrors()) {
      try
      {
        String fullName = m.getAnnotationType().getDeclaration().getQualifiedName();
        Class<? extends Annotation> type = getClass().getClassLoader().loadClass(fullName).asSubclass(Annotation.class);
        
        Annotation annotation = decl.getAnnotation(type);
        if (annotation != null) {
          r.add(LocatableAnnotation.create(annotation, srcPos));
        }
      }
      catch (ClassNotFoundException e) {}
    }
    return (Annotation[])r.toArray(EMPTY_ANNOTATION);
  }
  
  public <A extends Annotation> A getMethodParameterAnnotation(Class<A> a, MethodDeclaration m, int paramIndex, Locatable srcPos)
  {
    ParameterDeclaration[] params = (ParameterDeclaration[])m.getParameters().toArray(new ParameterDeclaration[0]);
    return LocatableAnnotation.create(params[paramIndex].getAnnotation(a), srcPos);
  }
  
  public <A extends Annotation> A getPackageAnnotation(Class<A> a, TypeDeclaration clazz, Locatable srcPos)
  {
    return LocatableAnnotation.create(clazz.getPackage().getAnnotation(a), srcPos);
  }
  
  public TypeMirror getClassValue(Annotation a, String name)
  {
    try
    {
      a.annotationType().getMethod(name, new Class[0]).invoke(a, new Object[0]);
      if (!$assertionsDisabled) {
        throw new AssertionError();
      }
      throw new IllegalStateException("should throw a MirroredTypeException");
    }
    catch (IllegalAccessException e)
    {
      throw new IllegalAccessError(e.getMessage());
    }
    catch (InvocationTargetException e)
    {
      if ((e.getCause() instanceof MirroredTypeException))
      {
        MirroredTypeException me = (MirroredTypeException)e.getCause();
        return me.getTypeMirror();
      }
      throw new RuntimeException(e);
    }
    catch (NoSuchMethodException e)
    {
      throw new NoSuchMethodError(e.getMessage());
    }
  }
  
  public TypeMirror[] getClassArrayValue(Annotation a, String name)
  {
    try
    {
      a.annotationType().getMethod(name, new Class[0]).invoke(a, new Object[0]);
      if (!$assertionsDisabled) {
        throw new AssertionError();
      }
      throw new IllegalStateException("should throw a MirroredTypesException");
    }
    catch (IllegalAccessException e)
    {
      throw new IllegalAccessError(e.getMessage());
    }
    catch (InvocationTargetException e)
    {
      if ((e.getCause() instanceof MirroredTypesException))
      {
        MirroredTypesException me = (MirroredTypesException)e.getCause();
        Collection<TypeMirror> r = me.getTypeMirrors();
        return (TypeMirror[])r.toArray(new TypeMirror[r.size()]);
      }
      throw new RuntimeException(e);
    }
    catch (NoSuchMethodException e)
    {
      throw new NoSuchMethodError(e.getMessage());
    }
  }
  
  protected String fullName(MethodDeclaration m)
  {
    return m.getDeclaringType().getQualifiedName() + '#' + m.getSimpleName();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\jxc\apt\InlineAnnotationReaderImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */