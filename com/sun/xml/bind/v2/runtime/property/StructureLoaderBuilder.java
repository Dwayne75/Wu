package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.util.QNameMap;
import javax.xml.namespace.QName;

public abstract interface StructureLoaderBuilder
{
  public static final QName TEXT_HANDLER = new QName("\000", "text");
  public static final QName CATCH_ALL = new QName("\000", "catchAll");
  
  public abstract void buildChildElementUnmarshallers(UnmarshallerChain paramUnmarshallerChain, QNameMap<ChildLoader> paramQNameMap);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\property\StructureLoaderBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */