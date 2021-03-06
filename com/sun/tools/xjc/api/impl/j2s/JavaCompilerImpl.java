package com.sun.tools.xjc.api.impl.j2s;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.TypeMirror;
import com.sun.tools.jxc.apt.InlineAnnotationReaderImpl;
import com.sun.tools.jxc.model.nav.APTNavigator;
import com.sun.tools.xjc.api.J2SJAXBModel;
import com.sun.tools.xjc.api.JavaCompiler;
import com.sun.tools.xjc.api.Reference;
import com.sun.xml.bind.v2.model.core.ErrorHandler;
import com.sun.xml.bind.v2.model.core.Ref;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.impl.ModelBuilder;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

public class JavaCompilerImpl
  implements JavaCompiler
{
  public J2SJAXBModel bind(Collection<Reference> rootClasses, Map<QName, Reference> additionalElementDecls, String defaultNamespaceRemap, AnnotationProcessorEnvironment env)
  {
    ModelBuilder<TypeMirror, TypeDeclaration, FieldDeclaration, MethodDeclaration> builder = new ModelBuilder(InlineAnnotationReaderImpl.theInstance, new APTNavigator(env), Collections.emptyMap(), defaultNamespaceRemap);
    
    builder.setErrorHandler(new ErrorHandlerImpl(env.getMessager()));
    for (Reference ref : rootClasses)
    {
      TypeMirror t = ref.type;
      
      XmlJavaTypeAdapter xjta = (XmlJavaTypeAdapter)ref.annotations.getAnnotation(XmlJavaTypeAdapter.class);
      XmlList xl = (XmlList)ref.annotations.getAnnotation(XmlList.class);
      
      builder.getTypeInfo(new Ref(builder, t, xjta, xl));
    }
    TypeInfoSet r = builder.link();
    if (r == null) {
      return null;
    }
    if (additionalElementDecls == null) {
      additionalElementDecls = Collections.emptyMap();
    } else {
      for (Map.Entry<QName, ? extends Reference> e : additionalElementDecls.entrySet()) {
        if (e.getKey() == null) {
          throw new IllegalArgumentException("nulls in additionalElementDecls");
        }
      }
    }
    return new JAXBModelImpl(r, builder.reader, rootClasses, new HashMap(additionalElementDecls));
  }
  
  private static final class ErrorHandlerImpl
    implements ErrorHandler
  {
    private final Messager messager;
    
    public ErrorHandlerImpl(Messager messager)
    {
      this.messager = messager;
    }
    
    public void error(IllegalAnnotationException e)
    {
      this.messager.printError(e.toString());
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\impl\j2s\JavaCompilerImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */