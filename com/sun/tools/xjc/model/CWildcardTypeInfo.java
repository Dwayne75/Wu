package com.sun.tools.xjc.model;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.model.nav.NavigatorImpl;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.core.WildcardTypeInfo;
import org.w3c.dom.Element;
import org.xml.sax.Locator;

public final class CWildcardTypeInfo
  extends AbstractCTypeInfoImpl
  implements WildcardTypeInfo<NType, NClass>
{
  private CWildcardTypeInfo()
  {
    super(null, null, null);
  }
  
  public static final CWildcardTypeInfo INSTANCE = new CWildcardTypeInfo();
  
  public JType toType(Outline o, Aspect aspect)
  {
    return o.getCodeModel().ref(Element.class);
  }
  
  public NType getType()
  {
    return NavigatorImpl.create(Element.class);
  }
  
  public Locator getLocator()
  {
    return Model.EMPTY_LOCATOR;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CWildcardTypeInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */