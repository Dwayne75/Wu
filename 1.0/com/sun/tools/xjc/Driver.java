package com.sun.tools.xjc;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.codemodel.writer.ProgressCodeWriter;
import com.sun.codemodel.writer.PrologCodeWriter;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.SkeletonGenerator;
import com.sun.tools.xjc.generator.marshaller.MarshallerGenerator;
import com.sun.tools.xjc.generator.unmarshaller.UnmarshallerGenerator;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Automaton;
import com.sun.tools.xjc.generator.unmarshaller.automaton.AutomatonToGraphViz;
import com.sun.tools.xjc.generator.validator.ValidatorGenerator;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.util.AnnotationRemover;
import com.sun.tools.xjc.reader.internalizer.DOMForest;
import com.sun.tools.xjc.reader.relaxng.RELAXNGInternalizationLogic;
import com.sun.tools.xjc.reader.xmlschema.parser.XMLSchemaInternalizationLogic;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.tools.xjc.util.NullStream;
import com.sun.tools.xjc.util.Util;
import com.sun.tools.xjc.writer.SignatureWriter;
import com.sun.tools.xjc.writer.Writer;
import com.sun.xml.bind.JAXBAssertionError;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.xml.sax.SAXException;

public class Driver
{
  private static final int MODE_BGM = 0;
  private static final int MODE_SIGNATURE = 1;
  private static final int MODE_SERIALIZE = 2;
  private static final int MODE_CODE = 3;
  private static final int MODE_AUTOMATA = 4;
  private static final int MODE_FOREST = 5;
  private static final int MODE_DRYRUN = 6;
  
  public static void main(String[] args)
    throws Exception
  {
    if (Util.getSystemProperty(Driver.class, "noThreadSwap") != null) {
      _main(args);
    }
    Throwable[] ex = new Throwable[1];
    
    Thread th = new Driver.1(args, ex);
    
    th.start();
    th.join();
    if (ex[0] != null)
    {
      if ((ex[0] instanceof Exception)) {
        throw ((Exception)ex[0]);
      }
      throw ((Error)ex[0]);
    }
  }
  
  private static void _main(String[] args)
    throws Exception
  {
    try
    {
      System.exit(run(args, System.err, System.out));
    }
    catch (BadCommandLineException e)
    {
      System.out.println(e.getMessage());
      System.out.println();
      
      usage(false);
      System.exit(-1);
    }
  }
  
  public static int run(String[] args, PrintStream status, PrintStream out)
    throws Exception
  {
    if (status == null) {
      status = new PrintStream(new NullStream());
    }
    for (int i = 0; i < args.length; i++)
    {
      if (args[i].equals("-help"))
      {
        usage(false);
        return -1;
      }
      if (args[i].equals("-version"))
      {
        status.println(Messages.format("Driver.Version"));
        return -1;
      }
      if (args[i].equals("-private"))
      {
        usage(true);
        return -1;
      }
    }
    Driver.OptionsEx opt = new Driver.OptionsEx();
    opt.setSchemaLanguage(1);
    opt.parseArguments(args);
    
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(opt.getUserClassLoader(contextClassLoader));
    try
    {
      if (!opt.quiet) {
        status.println(Messages.format("Driver.ParsingSchema"));
      }
      ErrorReceiver receiver = new ConsoleErrorReporter(out, !opt.debugMode, opt.quiet);
      if (opt.mode == 5)
      {
        GrammarLoader loader = new GrammarLoader(opt, receiver);
        DOMForest forest = loader.buildDOMForest(opt.getSchemaLanguage() == 2 ? new RELAXNGInternalizationLogic() : new XMLSchemaInternalizationLogic());
        
        forest.dump(System.out);
        return 0;
      }
      try
      {
        AnnotatedGrammar grammar = GrammarLoader.load(opt, receiver);
        if (grammar == null)
        {
          out.println(Messages.format("Driver.ParseFailed"));
          return -1;
        }
      }
      catch (SAXException e)
      {
        if (e.getException() != null) {
          e.getException().printStackTrace(out);
        }
        throw e;
      }
      AnnotatedGrammar grammar;
      if (!opt.quiet) {
        status.println(Messages.format("Driver.CompilingSchema"));
      }
      switch (opt.mode)
      {
      case 0: 
        Writer.writeToConsole(opt.noNS, false, grammar);
        break;
      case 1: 
        SignatureWriter.write(grammar, new OutputStreamWriter(out));
        break;
      case 2: 
        ObjectOutputStream stream = new ObjectOutputStream(out);
        stream.writeObject(AnnotationRemover.remove(grammar));
        stream.close();
        break;
      case 3: 
      case 6: 
        GeneratorContext context = generateCode(grammar, opt, receiver);
        if (context == null)
        {
          out.println(Messages.format("Driver.FailedToGenerateCode"));
          
          return -1;
        }
        if (opt.mode != 6)
        {
          CodeWriter cw = createCodeWriter(opt.targetDir, opt.readOnly);
          if (!opt.quiet) {
            cw = new ProgressCodeWriter(cw, status);
          }
          grammar.codeModel.build(cw);
        }
        break;
      case 4: 
        GeneratorContext context = SkeletonGenerator.generate(grammar, opt, receiver);
        if (context == null) {
          return -1;
        }
        Automaton[] automata = UnmarshallerGenerator.generate(grammar, context, opt);
        for (int i = 0; i < automata.length; i++) {
          AutomatonToGraphViz.convert(automata[i], new File(opt.targetDir, automata[i].getOwner().ref.name() + ".gif"));
        }
        break;
      case 5: 
      default: 
        throw new JAXBAssertionError();
      }
      return 0;
    }
    catch (StackOverflowError e)
    {
      if (opt.debugMode) {
        throw e;
      }
      out.println(Messages.format("Driver.StackOverflow"));
    }
    return -1;
  }
  
  public static String getBuildID()
  {
    return Messages.format("Driver.BuildID");
  }
  
  public static GeneratorContext generateCode(AnnotatedGrammar grammar, Options opt, ErrorReceiver errorReceiver)
  {
    errorReceiver.debug("generating code");
    
    ErrorReceiverFilter ehFilter = new ErrorReceiverFilter(errorReceiver);
    
    GeneratorContext context = SkeletonGenerator.generate(grammar, opt, ehFilter);
    if (context == null) {
      return null;
    }
    if (opt.generateUnmarshallingCode) {
      UnmarshallerGenerator.generate(grammar, context, opt);
    }
    if ((opt.generateValidationCode) || (opt.generateMarshallingCode)) {
      MarshallerGenerator.generate(grammar, context, opt);
    }
    if (opt.generateValidationCode) {
      ValidatorGenerator.generate(grammar, context, opt);
    }
    if (ehFilter.hadError()) {
      return null;
    }
    Iterator itr = opt.enabledModelAugmentors.iterator();
    while (itr.hasNext())
    {
      CodeAugmenter ma = (CodeAugmenter)itr.next();
      ma.run(grammar, context, opt, errorReceiver);
    }
    return context;
  }
  
  protected static void usage(boolean privateUsage)
  {
    if (privateUsage) {
      System.out.println(Messages.format("Driver.Private.Usage"));
    } else {
      System.out.println(Messages.format("Driver.Public.Usage"));
    }
    if (Options.codeAugmenters.length != 0)
    {
      System.out.println(Messages.format("Driver.AddonUsage"));
      for (int i = 0; i < Options.codeAugmenters.length; i++) {
        System.out.println(((CodeAugmenter)Options.codeAugmenters[i]).getUsage());
      }
    }
  }
  
  public static CodeWriter createCodeWriter(File targetDir, boolean readonly)
    throws IOException
  {
    return createCodeWriter(new FileCodeWriter(targetDir, readonly));
  }
  
  public static CodeWriter createCodeWriter(CodeWriter core)
    throws IOException
  {
    String format = Messages.format("Driver.DateFormat") + " '" + Messages.format("Driver.At") + "' " + Messages.format("Driver.TimeFormat");
    
    SimpleDateFormat dateFormat = new SimpleDateFormat(format);
    
    return new PrologCodeWriter(core, Messages.format("Driver.FilePrologComment", dateFormat.format(new Date())));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\Driver.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */