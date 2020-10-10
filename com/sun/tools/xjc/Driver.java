package com.sun.tools.xjc;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.writer.ZipCodeWriter;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.tools.xjc.api.ErrorListener;
import com.sun.tools.xjc.generator.bean.BeanGenerator;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.reader.gbind.Expression;
import com.sun.tools.xjc.reader.gbind.Graph;
import com.sun.tools.xjc.reader.internalizer.DOMForest;
import com.sun.tools.xjc.reader.xmlschema.ExpressionBuilder;
import com.sun.tools.xjc.reader.xmlschema.parser.XMLSchemaInternalizationLogic;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.tools.xjc.util.NullStream;
import com.sun.tools.xjc.util.Util;
import com.sun.tools.xjc.writer.SignatureWriter;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchemaSet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class Driver
{
  public static void main(String[] args)
    throws Exception
  {
    try
    {
      System.setProperty("java.net.useSystemProxies", "true");
    }
    catch (SecurityException e) {}
    if (Util.getSystemProperty(Driver.class, "noThreadSwap") != null) {
      _main(args);
    }
    final Throwable[] ex = new Throwable[1];
    
    Thread th = new Thread()
    {
      public void run()
      {
        try
        {
          Driver._main(this.val$args);
        }
        catch (Throwable e)
        {
          ex[0] = e;
        }
      }
    };
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
      if (e.getMessage() != null)
      {
        System.out.println(e.getMessage());
        System.out.println();
      }
      usage(e.getOptions(), false);
      System.exit(-1);
    }
  }
  
  public static int run(String[] args, final PrintStream status, PrintStream out)
    throws Exception
  {
    run(args, new XJCListener()
    {
      ConsoleErrorReporter cer = new ConsoleErrorReporter(this.val$out == null ? new PrintStream(new NullStream()) : this.val$out);
      
      public void generatedFile(String fileName, int count, int total)
      {
        message(fileName);
      }
      
      public void message(String msg)
      {
        if (status != null) {
          status.println(msg);
        }
      }
      
      public void error(SAXParseException exception)
      {
        this.cer.error(exception);
      }
      
      public void fatalError(SAXParseException exception)
      {
        this.cer.fatalError(exception);
      }
      
      public void warning(SAXParseException exception)
      {
        this.cer.warning(exception);
      }
      
      public void info(SAXParseException exception)
      {
        this.cer.info(exception);
      }
    });
  }
  
  public static int run(String[] args, @NotNull final XJCListener listener)
    throws BadCommandLineException
  {
    for (String arg : args) {
      if (arg.equals("-version"))
      {
        listener.message(Messages.format("Driver.Version", new Object[0]));
        return -1;
      }
    }
    final OptionsEx opt = new OptionsEx();
    opt.setSchemaLanguage(Language.XMLSCHEMA);
    try
    {
      opt.parseArguments(args);
    }
    catch (WeAreDone _)
    {
      return -1;
    }
    catch (BadCommandLineException e)
    {
      e.initOptions(opt);
      throw e;
    }
    if ((opt.defaultPackage != null) && (opt.defaultPackage.length() == 0)) {
      listener.message(Messages.format("Driver.WarningMessage", new Object[] { Messages.format("Driver.DefaultPackageWarning", new Object[0]) }));
    }
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(opt.getUserClassLoader(contextClassLoader));
    try
    {
      if (!opt.quiet) {
        listener.message(Messages.format("Driver.ParsingSchema", new Object[0]));
      }
      final boolean[] hadWarning = new boolean[1];
      
      ErrorReceiver receiver = new ErrorReceiverFilter(listener)
      {
        public void info(SAXParseException exception)
        {
          if (opt.verbose) {
            super.info(exception);
          }
        }
        
        public void warning(SAXParseException exception)
        {
          hadWarning[0] = true;
          if (!opt.quiet) {
            super.warning(exception);
          }
        }
        
        public void pollAbort()
          throws AbortException
        {
          if (listener.isCanceled()) {
            throw new AbortException();
          }
        }
      };
      if (opt.mode == Mode.FOREST)
      {
        ModelLoader loader = new ModelLoader(opt, new JCodeModel(), receiver);
        try
        {
          DOMForest forest = loader.buildDOMForest(new XMLSchemaInternalizationLogic());
          forest.dump(System.out);
          return 0;
        }
        catch (SAXException e) {}catch (IOException e)
        {
          receiver.error(e);
        }
        return -1;
      }
      if (opt.mode == Mode.GBIND) {
        try
        {
          XSSchemaSet xss = new ModelLoader(opt, new JCodeModel(), receiver).loadXMLSchema();
          Iterator<XSComplexType> it = xss.iterateComplexTypes();
          while (it.hasNext())
          {
            XSComplexType ct = (XSComplexType)it.next();
            XSParticle p = ct.getContentType().asParticle();
            if (p != null)
            {
              Expression tree = ExpressionBuilder.createTree(p);
              System.out.println("Graph for " + ct.getName());
              System.out.println(tree.toString());
              Graph g = new Graph(tree);
              System.out.println(g.toString());
              System.out.println();
            }
          }
          return 0;
        }
        catch (SAXException e)
        {
          return -1;
        }
      }
      Model model = ModelLoader.load(opt, new JCodeModel(), receiver);
      if (model == null)
      {
        listener.message(Messages.format("Driver.ParseFailed", new Object[0]));
        return -1;
      }
      if (!opt.quiet) {
        listener.message(Messages.format("Driver.CompilingSchema", new Object[0]));
      }
      switch (opt.mode)
      {
      case SIGNATURE: 
        try
        {
          SignatureWriter.write(BeanGenerator.generate(model, receiver), new OutputStreamWriter(System.out));
          
          return 0;
        }
        catch (IOException e)
        {
          receiver.error(e);
          return -1;
        }
      case CODE: 
      case DRYRUN: 
      case ZIP: 
        receiver.debug("generating code");
        
        Outline outline = model.generateCode(opt, receiver);
        if (outline == null)
        {
          listener.message(Messages.format("Driver.FailedToGenerateCode", new Object[0]));
          
          return -1;
        }
        listener.compiled(outline);
        if (opt.mode == Mode.DRYRUN) {
          break label744;
        }
        try
        {
          CodeWriter cw;
          CodeWriter cw;
          if (opt.mode == Mode.ZIP)
          {
            OutputStream os;
            OutputStream os;
            if (opt.targetDir.getPath().equals(".")) {
              os = System.out;
            } else {
              os = new FileOutputStream(opt.targetDir);
            }
            cw = opt.createCodeWriter(new ZipCodeWriter(os));
          }
          else
          {
            cw = opt.createCodeWriter();
          }
          if (!opt.quiet) {
            cw = new ProgressCodeWriter(cw, listener, model.codeModel.countArtifacts());
          }
          model.codeModel.build(cw);
        }
        catch (IOException e)
        {
          receiver.error(e);
          return -1;
        }
      }
      if (!$assertionsDisabled) {
        throw new AssertionError();
      }
      label744:
      if (opt.debugMode) {
        try
        {
          new FileOutputStream(new File(opt.targetDir, hadWarning[0] != 0 ? "hadWarning" : "noWarning")).close();
        }
        catch (IOException e)
        {
          receiver.error(e);
          return -1;
        }
      }
      return 0;
    }
    catch (StackOverflowError e)
    {
      if (opt.verbose) {
        throw e;
      }
      listener.message(Messages.format("Driver.StackOverflow", new Object[0]));
    }
    return -1;
  }
  
  public static String getBuildID()
  {
    return Messages.format("Driver.BuildID", new Object[0]);
  }
  
  private static enum Mode
  {
    CODE,  SIGNATURE,  FOREST,  DRYRUN,  ZIP,  GBIND;
    
    private Mode() {}
  }
  
  static class OptionsEx
    extends Options
  {
    protected Driver.Mode mode = Driver.Mode.CODE;
    public boolean noNS = false;
    
    public int parseArgument(String[] args, int i)
      throws BadCommandLineException
    {
      if (args[i].equals("-noNS"))
      {
        this.noNS = true;
        return 1;
      }
      if (args[i].equals("-mode"))
      {
        i++;
        if (i == args.length) {
          throw new BadCommandLineException(Messages.format("Driver.MissingModeOperand", new Object[0]));
        }
        String mstr = args[i].toLowerCase();
        for (Driver.Mode m : Driver.Mode.values()) {
          if ((m.name().toLowerCase().startsWith(mstr)) && (mstr.length() > 2))
          {
            this.mode = m;
            return 2;
          }
        }
        throw new BadCommandLineException(Messages.format("Driver.UnrecognizedMode", new Object[] { args[i] }));
      }
      if (args[i].equals("-help"))
      {
        Driver.usage(this, false);
        throw new Driver.WeAreDone(null);
      }
      if (args[i].equals("-private"))
      {
        Driver.usage(this, true);
        throw new Driver.WeAreDone(null);
      }
      return super.parseArgument(args, i);
    }
  }
  
  public static void usage(@Nullable Options opts, boolean privateUsage)
  {
    if (privateUsage) {
      System.out.println(Messages.format("Driver.Private.Usage", new Object[0]));
    } else {
      System.out.println(Messages.format("Driver.Public.Usage", new Object[0]));
    }
    if ((opts != null) && (opts.getAllPlugins().size() != 0))
    {
      System.out.println(Messages.format("Driver.AddonUsage", new Object[0]));
      for (Plugin p : opts.getAllPlugins()) {
        System.out.println(p.getUsage());
      }
    }
  }
  
  private static final class WeAreDone
    extends BadCommandLineException
  {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\Driver.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */