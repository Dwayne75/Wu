package com.sun.tools.xjc.generator.validator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.Grammar;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.trex.TREXGrammar;
import com.sun.msv.grammar.util.ExpressionPrinter;
import com.sun.msv.verifier.DocumentDeclaration;
import com.sun.msv.verifier.regexp.REDocumentDeclaration;
import com.sun.msv.writer.relaxng.RELAXNGWriter;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.runtime.ValidatableObject;
import com.sun.tools.xjc.util.Util;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.bind.validator.SchemaDeserializer;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.zip.GZIPOutputStream;
import org.xml.sax.SAXException;

public class ValidatorGenerator
{
  private static final PrintStream debug = Util.getSystemProperty(ValidatorGenerator.class, "debug") != null ? System.out : null;
  
  public static void generate(AnnotatedGrammar grammar, GeneratorContext context, Options opt)
  {
    JCodeModel codeModel = grammar.codeModel;
    
    ClassItem[] cis = grammar.getClasses();
    for (int i = 0; i < cis.length; i++)
    {
      ClassItem ci = cis[i];
      JDefinedClass cls = context.getClassContext(ci).implClass;
      
      cls._implements(context.getRuntime(ValidatableObject.class));
      
      JMethod method = cls.method(1, Class.class, "getPrimaryInterface");
      
      method.body()._return(((JClass)ci.getType()).dotclass());
      
      ExpressionPool pool = new ExpressionPool();
      Expression fragment = createSchemaFragment(ci, pool);
      if ((opt.debugMode) && (opt.verbose))
      {
        System.out.println(ci.getType().fullName());
        System.out.println(ExpressionPrinter.printFragment(fragment));
        System.out.println();
      }
      if (debug != null)
      {
        debug.println("---- schema fragment for " + ci.name + " ----");
        try
        {
          TREXGrammar g = new TREXGrammar(pool);
          g.exp = fragment;
          RELAXNGWriter w = new RELAXNGWriter();
          OutputFormat format = new OutputFormat("xml", null, true);
          
          format.setIndent(1);
          w.setDocumentHandler(new XMLSerializer(debug, format));
          w.write(g);
        }
        catch (SAXException e)
        {
          e.printStackTrace();
          throw new JAXBAssertionError();
        }
      }
      StringWriter sw = new StringWriter();
      saveFragmentTo(fragment, pool, new StringOutputStream(sw));
      
      String deserializeMethodName = "deserialize";
      if (sw.getBuffer().length() > 32768)
      {
        sw = new StringWriter();
        try
        {
          saveFragmentTo(fragment, pool, new GZIPOutputStream(new StringOutputStream(sw)));
          
          deserializeMethodName = "deserializeCompressed";
        }
        catch (IOException e)
        {
          throw new InternalError(e.getMessage());
        }
      }
      JFieldVar $schemaFragment = cls.field(20, Grammar.class, "schemaFragment");
      JExpression encodedFragment;
      JExpression encodedFragment;
      if (Util.getSystemProperty(ValidatorGenerator.class, "noSplit") != null)
      {
        encodedFragment = JExpr.lit(sw.toString());
      }
      else
      {
        int len = sw.getBuffer().length();
        StringBuffer buf = new StringBuffer(len);
        for (int j = 0; j < len; j += 60)
        {
          buf.append('\n');
          if (j != 0) {
            buf.append('+');
          } else {
            buf.append(' ');
          }
          buf.append(JExpr.quotify('"', sw.getBuffer().substring(j, Math.min(j + 60, len))));
        }
        encodedFragment = JExpr.direct(buf.toString());
      }
      JMethod m = cls.method(1, DocumentDeclaration.class, "createRawValidator");
      
      m.body()._if($schemaFragment.eq(JExpr._null()))._then().assign($schemaFragment, codeModel.ref(SchemaDeserializer.class).staticInvoke(deserializeMethodName).arg(encodedFragment));
      
      m.body()._return(JExpr._new(codeModel.ref(REDocumentDeclaration.class)).arg($schemaFragment));
    }
  }
  
  private static void saveFragmentTo(Expression fragment, ExpressionPool pool, OutputStream os)
  {
    try
    {
      ObjectOutputStream oos = new ObjectOutputStream(os);
      oos.writeObject(fragment);
      oos.writeObject(pool);
      oos.close();
    }
    catch (IOException e)
    {
      throw new JAXBAssertionError(e);
    }
  }
  
  private static Expression createSchemaFragment(ClassItem ci, ExpressionPool pool)
  {
    Expression exp;
    if (ci.agm.exp == null) {
      exp = ci.exp;
    } else {
      exp = ci.agm.exp;
    }
    Expression exp = exp.visit(new SchemaFragmentBuilder(new ExpressionPool()));
    
    return exp.visit(new ValidatorGenerator.1(pool));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\validator\ValidatorGenerator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */