package com.sun.xml.xsom.impl.parser;

import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.impl.Ref.Element;
import com.sun.xml.xsom.impl.Ref.Type;

public class SubstGroupBaseTypeRef
  implements Ref.Type
{
  private final Ref.Element e;
  
  public SubstGroupBaseTypeRef(Ref.Element _e)
  {
    this.e = _e;
  }
  
  public XSType getType()
  {
    return this.e.get().getType();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\parser\SubstGroupBaseTypeRef.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */