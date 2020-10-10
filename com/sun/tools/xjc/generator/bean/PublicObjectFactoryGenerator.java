package com.sun.tools.xjc.generator.bean;

import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.Aspect;

final class PublicObjectFactoryGenerator
  extends ObjectFactoryGeneratorImpl
{
  public PublicObjectFactoryGenerator(BeanGenerator outline, Model model, JPackage targetPackage)
  {
    super(outline, model, targetPackage);
  }
  
  void populate(CElementInfo ei)
  {
    populate(ei, Aspect.IMPLEMENTATION, Aspect.EXPOSED);
  }
  
  void populate(ClassOutlineImpl cc)
  {
    populate(cc, cc.ref);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\PublicObjectFactoryGenerator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */