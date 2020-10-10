package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.impl.ModelBuilder;
import com.sun.xml.bind.v2.model.nav.Navigator;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public final class Ref<T, C>
{
  public final T type;
  public final Adapter<T, C> adapter;
  public final boolean valueList;
  
  public Ref(T type)
  {
    this(type, null, false);
  }
  
  public Ref(T type, Adapter<T, C> adapter, boolean valueList)
  {
    this.adapter = adapter;
    if (adapter != null) {
      type = adapter.defaultType;
    }
    this.type = type;
    this.valueList = valueList;
  }
  
  public Ref(ModelBuilder<T, C, ?, ?> builder, T type, XmlJavaTypeAdapter xjta, XmlList xl)
  {
    this(builder.reader, builder.nav, type, xjta, xl);
  }
  
  public Ref(AnnotationReader<T, C, ?, ?> reader, Navigator<T, C, ?, ?> nav, T type, XmlJavaTypeAdapter xjta, XmlList xl)
  {
    Adapter<T, C> adapter = null;
    if (xjta != null)
    {
      adapter = new Adapter(xjta, reader, nav);
      type = adapter.defaultType;
    }
    this.type = type;
    this.adapter = adapter;
    this.valueList = (xl != null);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\Ref.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */