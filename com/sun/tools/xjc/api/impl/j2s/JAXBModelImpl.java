package com.sun.tools.xjc.api.impl.j2s;

import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.TypeMirror;
import com.sun.tools.xjc.api.ErrorListener;
import com.sun.tools.xjc.api.J2SJAXBModel;
import com.sun.tools.xjc.api.Reference;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.core.ArrayInfo;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.ElementInfo;
import com.sun.xml.bind.v2.model.core.EnumLeafInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.Ref;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator;
import com.sun.xml.txw2.output.ResultFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;

final class JAXBModelImpl
  implements J2SJAXBModel
{
  private final Map<QName, Reference> additionalElementDecls;
  private final List<String> classList = new ArrayList();
  private final TypeInfoSet<TypeMirror, TypeDeclaration, FieldDeclaration, MethodDeclaration> types;
  private final AnnotationReader<TypeMirror, TypeDeclaration, FieldDeclaration, MethodDeclaration> reader;
  private XmlSchemaGenerator<TypeMirror, TypeDeclaration, FieldDeclaration, MethodDeclaration> xsdgen;
  private final Map<Reference, NonElement<TypeMirror, TypeDeclaration>> refMap = new HashMap();
  
  public JAXBModelImpl(TypeInfoSet<TypeMirror, TypeDeclaration, FieldDeclaration, MethodDeclaration> types, AnnotationReader<TypeMirror, TypeDeclaration, FieldDeclaration, MethodDeclaration> reader, Collection<Reference> rootClasses, Map<QName, Reference> additionalElementDecls)
  {
    this.types = types;
    this.reader = reader;
    this.additionalElementDecls = additionalElementDecls;
    
    Navigator<TypeMirror, TypeDeclaration, FieldDeclaration, MethodDeclaration> navigator = types.getNavigator();
    for (ClassInfo<TypeMirror, TypeDeclaration> i : types.beans().values()) {
      this.classList.add(i.getName());
    }
    for (ArrayInfo<TypeMirror, TypeDeclaration> a : types.arrays().values())
    {
      String javaName = navigator.getTypeName(a.getType());
      this.classList.add(javaName);
    }
    for (EnumLeafInfo<TypeMirror, TypeDeclaration> l : types.enums().values())
    {
      QName tn = l.getTypeName();
      if (tn != null)
      {
        String javaName = navigator.getTypeName(l.getType());
        this.classList.add(javaName);
      }
    }
    for (Reference ref : rootClasses) {
      this.refMap.put(ref, getXmlType(ref));
    }
    Iterator<Map.Entry<QName, Reference>> itr = additionalElementDecls.entrySet().iterator();
    while (itr.hasNext())
    {
      Map.Entry<QName, Reference> entry = (Map.Entry)itr.next();
      if (entry.getValue() != null)
      {
        NonElement<TypeMirror, TypeDeclaration> xt = getXmlType((Reference)entry.getValue());
        
        assert (xt != null);
        this.refMap.put(entry.getValue(), xt);
        if ((xt instanceof ClassInfo))
        {
          ClassInfo<TypeMirror, TypeDeclaration> xct = (ClassInfo)xt;
          Element<TypeMirror, TypeDeclaration> elem = xct.asElement();
          if ((elem != null) && (elem.getElementName().equals(entry.getKey())))
          {
            itr.remove();
            continue;
          }
        }
        ElementInfo<TypeMirror, TypeDeclaration> ei = types.getElementInfo(null, (QName)entry.getKey());
        if ((ei != null) && (ei.getContentType() == xt)) {
          itr.remove();
        }
      }
    }
  }
  
  public List<String> getClassList()
  {
    return this.classList;
  }
  
  public QName getXmlTypeName(Reference javaType)
  {
    NonElement<TypeMirror, TypeDeclaration> ti = (NonElement)this.refMap.get(javaType);
    if (ti != null) {
      return ti.getTypeName();
    }
    return null;
  }
  
  private NonElement<TypeMirror, TypeDeclaration> getXmlType(Reference r)
  {
    if (r == null) {
      throw new IllegalArgumentException();
    }
    XmlJavaTypeAdapter xjta = (XmlJavaTypeAdapter)r.annotations.getAnnotation(XmlJavaTypeAdapter.class);
    XmlList xl = (XmlList)r.annotations.getAnnotation(XmlList.class);
    
    Ref<TypeMirror, TypeDeclaration> ref = new Ref(this.reader, this.types.getNavigator(), r.type, xjta, xl);
    
    return this.types.getTypeInfo(ref);
  }
  
  public void generateSchema(SchemaOutputResolver outputResolver, ErrorListener errorListener)
    throws IOException
  {
    getSchemaGenerator().write(outputResolver, errorListener);
  }
  
  public void generateEpisodeFile(Result output)
  {
    getSchemaGenerator().writeEpisodeFile(ResultFactory.createSerializer(output));
  }
  
  private synchronized XmlSchemaGenerator<TypeMirror, TypeDeclaration, FieldDeclaration, MethodDeclaration> getSchemaGenerator()
  {
    if (this.xsdgen == null)
    {
      this.xsdgen = new XmlSchemaGenerator(this.types.getNavigator(), this.types);
      for (Map.Entry<QName, Reference> e : this.additionalElementDecls.entrySet())
      {
        Reference value = (Reference)e.getValue();
        if (value != null)
        {
          NonElement<TypeMirror, TypeDeclaration> typeInfo = (NonElement)this.refMap.get(value);
          if (typeInfo == null) {
            throw new IllegalArgumentException(e.getValue() + " was not specified to JavaCompiler.bind");
          }
          this.xsdgen.add((QName)e.getKey(), !(value.type instanceof PrimitiveType), typeInfo);
        }
        else
        {
          this.xsdgen.add((QName)e.getKey(), false, null);
        }
      }
    }
    return this.xsdgen;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\impl\j2s\JAXBModelImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */