package com.sun.tools.xjc.api.impl.s2j;

import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.api.TypeAndAnnotation;
import com.sun.tools.xjc.generator.annotation.spec.XmlJavaTypeAdapterWriter;
import com.sun.tools.xjc.model.CAdapter;
import com.sun.tools.xjc.model.CNonElement;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.runtime.SwaRefAdapter;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlList;

final class TypeAndAnnotationImpl
  implements TypeAndAnnotation
{
  private final TypeUse typeUse;
  private final Outline outline;
  
  public TypeAndAnnotationImpl(Outline outline, TypeUse typeUse)
  {
    this.typeUse = typeUse;
    this.outline = outline;
  }
  
  public JType getTypeClass()
  {
    CAdapter a = this.typeUse.getAdapterUse();
    NType nt;
    NType nt;
    if (a != null) {
      nt = (NType)a.customType;
    } else {
      nt = (NType)this.typeUse.getInfo().getType();
    }
    JType jt = nt.toType(this.outline, Aspect.EXPOSED);
    
    JPrimitiveType prim = jt.boxify().getPrimitiveType();
    if ((!this.typeUse.isCollection()) && (prim != null)) {
      jt = prim;
    }
    if (this.typeUse.isCollection()) {
      jt = jt.array();
    }
    return jt;
  }
  
  public void annotate(JAnnotatable programElement)
  {
    if ((this.typeUse.getAdapterUse() == null) && (!this.typeUse.isCollection())) {
      return;
    }
    CAdapter adapterUse = this.typeUse.getAdapterUse();
    if (adapterUse != null) {
      if (adapterUse.getAdapterIfKnown() == SwaRefAdapter.class) {
        programElement.annotate(XmlAttachmentRef.class);
      } else {
        ((XmlJavaTypeAdapterWriter)programElement.annotate2(XmlJavaTypeAdapterWriter.class)).value(((NClass)adapterUse.adapterType).toType(this.outline, Aspect.EXPOSED));
      }
    }
    if (this.typeUse.isCollection()) {
      programElement.annotate(XmlList.class);
    }
  }
  
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    
    builder.append(getTypeClass());
    return builder.toString();
  }
  
  public boolean equals(Object o)
  {
    if (!(o instanceof TypeAndAnnotationImpl)) {
      return false;
    }
    TypeAndAnnotationImpl that = (TypeAndAnnotationImpl)o;
    return this.typeUse == that.typeUse;
  }
  
  public int hashCode()
  {
    return this.typeUse.hashCode();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\impl\s2j\TypeAndAnnotationImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */