package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.reader.gbind.Element;
import com.sun.xml.xsom.XSParticle;
import java.util.HashSet;
import java.util.Set;

abstract class GElement
  extends Element
{
  final Set<XSParticle> particles = new HashSet();
  
  abstract String getPropertyNameSeed();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\GElement.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */