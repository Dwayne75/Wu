package com.sun.tools.xjc.generator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.fmt.JPropertyFile;
import com.sun.codemodel.fmt.JSerializedObject;
import com.sun.msv.grammar.Grammar;
import com.sun.msv.writer.relaxng.RELAXNGWriter;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.Constructor;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.util.Multiplicity;
import com.sun.tools.xjc.runtime.DefaultJAXBContextImpl;
import com.sun.tools.xjc.runtime.GrammarInfo;
import com.sun.tools.xjc.runtime.GrammarInfoImpl;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.tools.xjc.util.Util;
import com.sun.xml.bind.ContextFactory_1_0_1;
import com.sun.xml.bind.JAXBAssertionError;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import org.xml.sax.SAXException;

final class ObjectFactoryGenerator
{
  private static final PrintStream debug = Util.getSystemProperty(ObjectFactoryGenerator.class, "debug") != null ? System.out : null;
  private final GeneratorContext context;
  private final AnnotatedGrammar grammar;
  private final JCodeModel codeModel;
  private final Options opt;
  private final JPackage targetPackage;
  private final JVar $grammarInfo;
  private final JVar $rootTagMap;
  private final ObjectFactoryGenerator.DefaultImplementationMapGenerator defImplMapGenerator;
  private final JDefinedClass objectFactory;
  
  public JVar getGrammarInfo()
  {
    return this.$grammarInfo;
  }
  
  public JDefinedClass getObjectFactory()
  {
    return this.objectFactory;
  }
  
  public JVar getRootTagMap()
  {
    return this.$rootTagMap;
  }
  
  ObjectFactoryGenerator(GeneratorContext _context, AnnotatedGrammar _grammar, Options _opt, JPackage _pkg)
  {
    this.context = _context;
    this.grammar = _grammar;
    this.opt = _opt;
    this.codeModel = this.grammar.codeModel;
    this.targetPackage = _pkg;
    
    this.objectFactory = this.context.getClassFactory().createClass(this.targetPackage, "ObjectFactory", null);
    
    this.defImplMapGenerator = new ObjectFactoryGenerator.DefaultImplementationMapGenerator(this, Util.calculateInitialHashMapCapacity(countClassItems(), 0.75F));
    
    this.$rootTagMap = this.objectFactory.field(20, HashMap.class, "rootTagMap", JExpr._new(this.objectFactory.owner().ref(HashMap.class)));
    
    this.objectFactory._extends(this.context.getRuntime(DefaultJAXBContextImpl.class));
    
    JPropertyFile jaxbProperties = new JPropertyFile("jaxb.properties");
    this.targetPackage.addResourceFile(jaxbProperties);
    jaxbProperties.add("javax.xml.bind.context.factory", ContextFactory_1_0_1.class.getName());
    
    jaxbProperties.add("com.sun.xml.bind.jaxbContextImpl", this.context.getRuntime(DefaultJAXBContextImpl.class).fullName());
    if (this.opt.debugMode) {
      if (!this.targetPackage.isUnnamed()) {
        try
        {
          this.codeModel._package("")._class("ObjectFactory")._extends(this.objectFactory);
        }
        catch (JClassAlreadyExistsException e) {}
      }
    }
    this.$grammarInfo = this.objectFactory.field(25, this.context.getRuntime(GrammarInfo.class), "grammarInfo", JExpr._new(this.context.getRuntime(GrammarInfoImpl.class)).arg(this.$rootTagMap).arg(this.defImplMapGenerator.$map).arg(this.objectFactory.dotclass()));
    
    JMethod m1 = this.objectFactory.constructor(1);
    m1.body().invoke("super").arg(this.$grammarInfo);
    m1.javadoc().setComment("Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: " + this.targetPackage.name());
    
    JMethod m2 = this.objectFactory.method(1, this.codeModel.ref(Object.class), "newInstance")._throws(JAXBException.class);
    
    m2.param(Class.class, "javaContentInterface");
    m2.body()._return(JExpr.invoke(JExpr._super(), "newInstance").arg(JExpr.ref("javaContentInterface")));
    
    m2.javadoc().setComment("Create an instance of the specified Java content interface.").addParam("javaContentInterface", "the Class object of the javacontent interface to instantiate").addReturn("a new instance").addThrows("JAXBException", "if an error occurs");
    
    JMethod m3 = this.objectFactory.method(1, this.codeModel.ref(Object.class), "getProperty")._throws(PropertyException.class);
    
    JVar $name = m3.param(String.class, "name");
    m3.body()._return(JExpr._super().invoke("getProperty").arg($name));
    m3.javadoc().setComment("Get the specified property. This method can only be\nused to get provider specific properties.\nAttempting to get an undefined property will result\nin a PropertyException being thrown.").addParam("name", "the name of the property to retrieve").addReturn("the value of the requested property").addThrows("PropertyException", "when there is an error retrieving the given property or value");
    
    JMethod m4 = this.objectFactory.method(1, this.codeModel.VOID, "setProperty")._throws(PropertyException.class);
    
    JVar $name = m4.param(String.class, "name");
    JVar $value = m4.param(Object.class, "value");
    m4.body().invoke(JExpr._super(), "setProperty").arg($name).arg($value);
    
    m4.javadoc().setComment("Set the specified property. This method can only be\nused to set provider specific properties.\nAttempting to set an undefined property will result\nin a PropertyException being thrown.").addParam("name", "the name of the property to retrieve").addParam("value", "the value of the property to be set").addThrows("PropertyException", "when there is an error processing the given property or value");
    
    Grammar purifiedGrammar = AGMBuilder.remove(this.grammar);
    try
    {
      this.targetPackage.addResourceFile(new JSerializedObject("bgm.ser", purifiedGrammar));
    }
    catch (IOException e)
    {
      throw new JAXBAssertionError(e);
    }
    if (debug != null)
    {
      debug.println("---- schema ----");
      try
      {
        RELAXNGWriter w = new RELAXNGWriter();
        OutputFormat format = new OutputFormat("xml", null, true);
        
        format.setIndent(1);
        w.setDocumentHandler(new XMLSerializer(debug, format));
        w.write(purifiedGrammar);
      }
      catch (SAXException e)
      {
        throw new JAXBAssertionError(e);
      }
    }
    this.objectFactory.javadoc().appendComment("This object contains factory methods for each \nJava content interface and Java element interface \ngenerated in the " + this.targetPackage.name() + " package. \n" + "<p>An ObjectFactory allows you to programatically \n" + "construct new instances of the Java representation \n" + "for XML content. The Java representation of XML \n" + "content can consist of schema derived interfaces \n" + "and classes representing the binding of schema \n" + "type definitions, element declarations and model \n" + "groups.  Factory methods for each of these are \n" + "provided in this class.");
  }
  
  void populate(ClassContext cc)
  {
    JMethod m = this.objectFactory.method(1, cc.ref, "create" + getPartlyQualifiedName(cc.ref))._throws(JAXBException.class);
    
    m.body()._return(JExpr._new(cc.implRef));
    
    m.javadoc().appendComment("Create an instance of " + getPartlyQualifiedName(cc.ref)).addThrows("JAXBException", "if an error occurs");
    
    Iterator itr = cc.target.iterateConstructors();
    if (itr.hasNext()) {
      cc.implClass.constructor(1);
    }
    while (itr.hasNext())
    {
      Constructor cons = (Constructor)itr.next();
      
      JMethod m = this.objectFactory.method(1, cc.ref, "create" + getPartlyQualifiedName(cc.ref));
      
      JInvocation inv = JExpr._new(cc.implRef);
      m.body()._return(inv);
      
      m._throws(this.codeModel.ref(JAXBException.class));
      
      m.javadoc().appendComment("Create an instance of " + getPartlyQualifiedName(cc.ref)).addThrows("JAXBException", "if an error occurs");
      
      JMethod c = cc.implClass.constructor(1);
      for (int i = 0; i < cons.fields.length; i++)
      {
        String fieldName = cons.fields[i];
        FieldUse field = cc.target.getField(fieldName);
        if (field == null) {
          throw new UnsupportedOperationException("illegal constructor param name: " + fieldName);
        }
        fieldName = camelize(fieldName);
        
        FieldRenderer renderer = this.context.getField(field);
        JVar $fvar;
        if (field.multiplicity.isAtMostOnce())
        {
          JVar $fvar = m.param(field.type, fieldName);
          JVar $var = c.param(field.type, fieldName);
          
          renderer.setter(c.body(), $var);
        }
        else
        {
          $fvar = m.param(field.type.array(), fieldName);
          JVar $var = c.param(field.type.array(), fieldName);
          
          JForLoop forLoop = c.body()._for();
          JVar $i = forLoop.init(this.codeModel.INT, "___i", JExpr.lit(0));
          forLoop.test($i.lt($var.ref("length")));
          forLoop.update($i.incr());
          
          renderer.setter(forLoop.body(), $var.component($i));
        }
        inv.arg($fvar);
      }
    }
    this.defImplMapGenerator.add(cc.ref, cc.implRef);
  }
  
  private String getPartlyQualifiedName(JDefinedClass cls)
  {
    if ((cls.parentContainer() instanceof JPackage)) {
      return cls.name();
    }
    return getPartlyQualifiedName((JDefinedClass)cls.parentContainer()) + cls.name();
  }
  
  private static String camelize(String s)
  {
    return Character.toLowerCase(s.charAt(0)) + s.substring(1);
  }
  
  private int countClassItems()
  {
    ClassItem[] classItems = this.grammar.getClasses();
    int count = 0;
    for (int i = 0; i < classItems.length; i++) {
      if (classItems[i].getTypeAsDefined()._package() == this.targetPackage) {
        count++;
      }
    }
    return count;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\ObjectFactoryGenerator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */