package com.sun.tools.xjc.generator;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.util.CodeModelClassFactory;

public abstract interface GeneratorContext
{
  public abstract AnnotatedGrammar getGrammar();
  
  public abstract JCodeModel getCodeModel();
  
  public abstract LookupTableBuilder getLookupTableBuilder();
  
  public abstract JClass getRuntime(Class paramClass);
  
  public abstract FieldRenderer getField(FieldUse paramFieldUse);
  
  public abstract PackageContext getPackageContext(JPackage paramJPackage);
  
  public abstract ClassContext getClassContext(ClassItem paramClassItem);
  
  public abstract PackageContext[] getAllPackageContexts();
  
  public abstract CodeModelClassFactory getClassFactory();
  
  public abstract ErrorReceiver getErrorReceiver();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\GeneratorContext.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */