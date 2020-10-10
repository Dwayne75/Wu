package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.nav.Navigator;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class Adapter<TypeT, ClassDeclT>
{
  public final ClassDeclT adapterType;
  public final TypeT defaultType;
  public final TypeT customType;
  
  public Adapter(XmlJavaTypeAdapter spec, AnnotationReader<TypeT, ClassDeclT, ?, ?> reader, Navigator<TypeT, ClassDeclT, ?, ?> nav)
  {
    this(nav.asDecl(reader.getClassValue(spec, "value")), nav);
  }
  
  public Adapter(ClassDeclT adapterType, Navigator<TypeT, ClassDeclT, ?, ?> nav)
  {
    this.adapterType = adapterType;
    TypeT baseClass = nav.getBaseClass(nav.use(adapterType), nav.asDecl(XmlAdapter.class));
    
    assert (baseClass != null);
    if (nav.isParameterizedType(baseClass)) {
      this.defaultType = nav.getTypeArgument(baseClass, 0);
    } else {
      this.defaultType = nav.ref(Object.class);
    }
    if (nav.isParameterizedType(baseClass)) {
      this.customType = nav.getTypeArgument(baseClass, 1);
    } else {
      this.customType = nav.ref(Object.class);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\Adapter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */