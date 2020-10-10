package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.core.EnumConstant;
import com.sun.xml.bind.v2.model.core.EnumLeafInfo;

class EnumConstantImpl<T, C, F, M>
  implements EnumConstant<T, C>
{
  protected final String lexical;
  protected final EnumLeafInfoImpl<T, C, F, M> owner;
  protected final String name;
  protected final EnumConstantImpl<T, C, F, M> next;
  
  public EnumConstantImpl(EnumLeafInfoImpl<T, C, F, M> owner, String name, String lexical, EnumConstantImpl<T, C, F, M> next)
  {
    this.lexical = lexical;
    this.owner = owner;
    this.name = name;
    this.next = next;
  }
  
  public EnumLeafInfo<T, C> getEnclosingClass()
  {
    return this.owner;
  }
  
  public final String getLexicalValue()
  {
    return this.lexical;
  }
  
  public final String getName()
  {
    return this.name;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\EnumConstantImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */