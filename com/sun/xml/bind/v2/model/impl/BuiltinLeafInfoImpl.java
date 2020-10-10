package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.core.BuiltinLeafInfo;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.nav.Navigator;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

public class BuiltinLeafInfoImpl<TypeT, ClassDeclT>
  extends LeafInfoImpl<TypeT, ClassDeclT>
  implements BuiltinLeafInfo<TypeT, ClassDeclT>
{
  private final QName[] typeNames;
  
  protected BuiltinLeafInfoImpl(TypeT type, QName... typeNames)
  {
    super(type, typeNames.length > 0 ? typeNames[0] : null);
    this.typeNames = typeNames;
  }
  
  public final QName[] getTypeNames()
  {
    return this.typeNames;
  }
  
  /**
   * @deprecated
   */
  public final boolean isElement()
  {
    return false;
  }
  
  /**
   * @deprecated
   */
  public final QName getElementName()
  {
    return null;
  }
  
  /**
   * @deprecated
   */
  public final Element<TypeT, ClassDeclT> asElement()
  {
    return null;
  }
  
  public static <TypeT, ClassDeclT> Map<TypeT, BuiltinLeafInfoImpl<TypeT, ClassDeclT>> createLeaves(Navigator<TypeT, ClassDeclT, ?, ?> nav)
  {
    Map<TypeT, BuiltinLeafInfoImpl<TypeT, ClassDeclT>> leaves = new HashMap();
    for (RuntimeBuiltinLeafInfoImpl<?> leaf : RuntimeBuiltinLeafInfoImpl.builtinBeanInfos)
    {
      TypeT t = nav.ref(leaf.getClazz());
      leaves.put(t, new BuiltinLeafInfoImpl(t, leaf.getTypeNames()));
    }
    return leaves;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\BuiltinLeafInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */