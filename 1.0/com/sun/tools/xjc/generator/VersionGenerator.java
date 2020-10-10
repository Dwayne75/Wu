package com.sun.tools.xjc.generator;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.util.CodeModelClassFactory;

final class VersionGenerator
{
  private final JCodeModel codeModel;
  private final GeneratorContext context;
  private final JPackage targetPackage;
  public final JDefinedClass versionClass;
  
  VersionGenerator(GeneratorContext _context, AnnotatedGrammar _grammar, JPackage _pkg)
  {
    this.context = _context;
    this.codeModel = _grammar.codeModel;
    this.targetPackage = _pkg;
    
    this.versionClass = this.context.getClassFactory().createClass(this.targetPackage, "JAXBVersion", null);
    
    generate();
  }
  
  private void generate()
  {
    this.versionClass.field(25, this.codeModel.ref(String.class), "version", JExpr.lit(Messages.format("VersionGenerator.versionField")));
  }
  
  void generateVersionReference(JDefinedClass impl)
  {
    impl.field(25, this.codeModel.ref(Class.class), "version", this.versionClass.dotclass());
  }
  
  void generateVersionReference(ClassContext cc)
  {
    generateVersionReference(cc.implClass);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\VersionGenerator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */