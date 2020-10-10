package com.sun.xml.bind.v2.schemagen;

import com.sun.xml.bind.v2.schemagen.xmlschema.ContentModelContainer;
import com.sun.xml.bind.v2.schemagen.xmlschema.Particle;

 enum GroupKind
{
  ALL("all"),  SEQUENCE("sequence"),  CHOICE("choice");
  
  private final String name;
  
  private GroupKind(String name)
  {
    this.name = name;
  }
  
  Particle write(ContentModelContainer parent)
  {
    return (Particle)parent._element(this.name, Particle.class);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\GroupKind.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */