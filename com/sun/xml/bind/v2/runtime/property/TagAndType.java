package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Name;

class TagAndType
{
  final Name tagName;
  final JaxBeanInfo beanInfo;
  
  TagAndType(Name tagName, JaxBeanInfo beanInfo)
  {
    this.tagName = tagName;
    this.beanInfo = beanInfo;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\property\TagAndType.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */