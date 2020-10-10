package com.sun.tools.xjc.generator.bean;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.fmt.JPropertyFile;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.runtime.JAXBContextFactory;

final class PrivateObjectFactoryGenerator
  extends ObjectFactoryGeneratorImpl
{
  public PrivateObjectFactoryGenerator(BeanGenerator outline, Model model, JPackage targetPackage)
  {
    super(outline, model, targetPackage.subPackage("impl"));
    
    JPackage implPkg = targetPackage.subPackage("impl");
    
    JClass factory = outline.generateStaticClass(JAXBContextFactory.class, implPkg);
    
    JPropertyFile jaxbProperties = new JPropertyFile("jaxb.properties");
    targetPackage.addResourceFile(jaxbProperties);
    jaxbProperties.add("javax.xml.bind.context.factory", factory.fullName());
  }
  
  void populate(CElementInfo ei)
  {
    populate(ei, Aspect.IMPLEMENTATION, Aspect.IMPLEMENTATION);
  }
  
  void populate(ClassOutlineImpl cc)
  {
    populate(cc, cc.implRef);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\PrivateObjectFactoryGenerator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */