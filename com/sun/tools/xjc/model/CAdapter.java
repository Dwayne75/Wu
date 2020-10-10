package com.sun.tools.xjc.model;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.model.nav.EagerNClass;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.model.nav.NavigatorImpl;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.core.Adapter;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public final class CAdapter
  extends Adapter<NType, NClass>
{
  private JClass adapterClass1;
  private Class<? extends XmlAdapter> adapterClass2;
  
  public CAdapter(Class<? extends XmlAdapter> adapter, boolean copy)
  {
    super(getRef(adapter, copy), NavigatorImpl.theInstance);
    this.adapterClass1 = null;
    this.adapterClass2 = adapter;
  }
  
  static NClass getRef(final Class<? extends XmlAdapter> adapter, boolean copy)
  {
    if (copy) {
      new EagerNClass(adapter)
      {
        public JClass toType(Outline o, Aspect aspect)
        {
          return o.addRuntime(adapter);
        }
        
        public String fullName()
        {
          throw new UnsupportedOperationException();
        }
      };
    }
    return NavigatorImpl.theInstance.ref(adapter);
  }
  
  public CAdapter(JClass adapter)
  {
    super(NavigatorImpl.theInstance.ref(adapter), NavigatorImpl.theInstance);
    this.adapterClass1 = adapter;
    this.adapterClass2 = null;
  }
  
  public JClass getAdapterClass(Outline o)
  {
    if (this.adapterClass1 == null) {
      this.adapterClass1 = o.getCodeModel().ref(this.adapterClass2);
    }
    return ((NClass)this.adapterType).toType(o, Aspect.EXPOSED);
  }
  
  public boolean isWhitespaceAdapter()
  {
    return (this.adapterClass2 == CollapsedStringAdapter.class) || (this.adapterClass2 == NormalizedStringAdapter.class);
  }
  
  public Class<? extends XmlAdapter> getAdapterIfKnown()
  {
    return this.adapterClass2;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CAdapter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */