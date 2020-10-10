package com.sun.tools.xjc.generator;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;

public final class PackageContext
{
  public final JPackage _package;
  public final JDefinedClass objectFactory;
  public final JVar rootTagMap;
  protected final VersionGenerator versionGenerator;
  protected final ObjectFactoryGenerator objectFactoryGenerator;
  
  protected PackageContext(GeneratorContext _context, AnnotatedGrammar _grammar, Options _opt, JPackage _pkg)
  {
    this._package = _pkg;
    
    this.versionGenerator = new VersionGenerator(_context, _grammar, _pkg.subPackage("impl"));
    
    this.objectFactoryGenerator = new ObjectFactoryGenerator(_context, _grammar, _opt, _pkg);
    
    this.objectFactory = this.objectFactoryGenerator.getObjectFactory();
    this.rootTagMap = this.objectFactoryGenerator.getRootTagMap();
    
    this.versionGenerator.generateVersionReference(this.objectFactory);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\PackageContext.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */