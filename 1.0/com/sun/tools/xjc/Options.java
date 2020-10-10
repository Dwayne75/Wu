package com.sun.tools.xjc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.helpers.Debug;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class Options
{
  public boolean debugMode;
  public boolean verbose;
  public boolean quiet;
  public boolean traceUnmarshaller;
  public boolean readOnly;
  public boolean generateValidationCode = true;
  public boolean generateMarshallingCode = true;
  public boolean generateUnmarshallingCode = true;
  public boolean generateValidatingUnmarshallingCode = true;
  public boolean strictCheck = true;
  public static final int STRICT = 1;
  public static final int EXTENSION = 2;
  public int compatibilityMode = 1;
  public File targetDir = new File(".");
  public EntityResolver entityResolver = null;
  public static final int SCHEMA_DTD = 0;
  public static final int SCHEMA_XMLSCHEMA = 1;
  public static final int SCHEMA_RELAXNG = 2;
  public static final int SCHEMA_WSDL = 3;
  private static final int SCHEMA_AUTODETECT = -1;
  private int schemaLanguage = -1;
  public String defaultPackage = null;
  private final List grammars = new ArrayList();
  private final List bindFiles = new ArrayList();
  String proxyHost = null;
  String proxyPort = null;
  public boolean generateRuntime = true;
  public String runtimePackage = null;
  public final List enabledModelAugmentors = new ArrayList();
  public static final Object[] codeAugmenters = findServices(CodeAugmenter.class.getName());
  
  public int getSchemaLanguage()
  {
    if (this.schemaLanguage == -1) {
      this.schemaLanguage = guessSchemaLanguage();
    }
    return this.schemaLanguage;
  }
  
  public void setSchemaLanguage(int _schemaLanguage)
  {
    this.schemaLanguage = _schemaLanguage;
  }
  
  public InputSource[] getGrammars()
  {
    return (InputSource[])this.grammars.toArray(new InputSource[this.grammars.size()]);
  }
  
  public void addGrammar(InputSource is)
  {
    this.grammars.add(absolutize(is));
  }
  
  private InputSource absolutize(InputSource is)
  {
    try
    {
      URL baseURL = new File(".").getCanonicalFile().toURL();
      is.setSystemId(new URL(baseURL, is.getSystemId()).toExternalForm());
    }
    catch (IOException e) {}
    return is;
  }
  
  public InputSource[] getBindFiles()
  {
    return (InputSource[])this.bindFiles.toArray(new InputSource[this.bindFiles.size()]);
  }
  
  public void addBindFile(InputSource is)
  {
    this.bindFiles.add(absolutize(is));
  }
  
  public final List classpaths = new ArrayList();
  
  public URLClassLoader getUserClassLoader(ClassLoader parent)
  {
    return new URLClassLoader((URL[])this.classpaths.toArray(new URL[this.classpaths.size()]), parent);
  }
  
  protected int parseArgument(String[] args, int i)
    throws BadCommandLineException, IOException
  {
    if ((args[i].equals("-classpath")) || (args[i].equals("-cp")))
    {
      if (i == args.length - 1) {
        throw new BadCommandLineException(Messages.format("Driver.MissingClassPath"));
      }
      this.classpaths.add(new File(args[(++i)]).toURL());
      return 2;
    }
    if (args[i].equals("-d"))
    {
      if (i == args.length - 1) {
        throw new BadCommandLineException(Messages.format("Driver.MissingDir"));
      }
      this.targetDir = new File(args[(++i)]);
      if (!this.targetDir.exists()) {
        throw new BadCommandLineException(Messages.format("Driver.NonExistentDir", this.targetDir));
      }
      return 2;
    }
    if (args[i].equals("-readOnly"))
    {
      this.readOnly = true;
      return 1;
    }
    if (args[i].equals("-p"))
    {
      if (i == args.length - 1) {
        throw new BadCommandLineException(Messages.format("Driver.MissingPackageName"));
      }
      this.defaultPackage = args[(++i)];
      return 2;
    }
    if (args[i].equals("-debug"))
    {
      this.debugMode = true;
      try
      {
        Debug.setDebug(10);
      }
      catch (Throwable _) {}
      return 1;
    }
    if (args[i].equals("-trace-unmarshaller"))
    {
      this.traceUnmarshaller = true;
      return 1;
    }
    if (args[i].equals("-nv"))
    {
      this.strictCheck = false;
      return 1;
    }
    if (args[i].equals("-use-runtime"))
    {
      if (i == args.length - 1) {
        throw new BadCommandLineException(Messages.format("Driver.MissingRuntimePackageName"));
      }
      this.generateRuntime = false;
      this.runtimePackage = args[(++i)];
      return 2;
    }
    if (args[i].equals("-verbose"))
    {
      this.verbose = true;
      return 1;
    }
    if (args[i].equals("-quiet"))
    {
      this.quiet = true;
      return 1;
    }
    if (args[i].equals("-b"))
    {
      if (i == args.length - 1) {
        throw new BadCommandLineException(Messages.format("Driver.MissingFileName"));
      }
      if (args[(i + 1)].startsWith("-")) {
        throw new BadCommandLineException(Messages.format("Driver.MissingFileName"));
      }
      addBindFile(com.sun.tools.xjc.reader.Util.getInputSource(args[(++i)]));
      return 2;
    }
    if (args[i].equals("-dtd"))
    {
      this.schemaLanguage = 0;
      return 1;
    }
    if (args[i].equals("-relaxng"))
    {
      this.schemaLanguage = 2;
      return 1;
    }
    if (args[i].equals("-xmlschema"))
    {
      this.schemaLanguage = 1;
      return 1;
    }
    if (args[i].equals("-wsdl"))
    {
      this.schemaLanguage = 3;
      return 1;
    }
    if (args[i].equals("-extension"))
    {
      this.compatibilityMode = 2;
      return 1;
    }
    if (args[i].equals("-host"))
    {
      if (i == args.length - 1) {
        throw new BadCommandLineException(Messages.format("Driver.MissingProxyHost"));
      }
      if (args[(i + 1)].startsWith("-")) {
        throw new BadCommandLineException(Messages.format("Driver.MissingProxyHost"));
      }
      this.proxyHost = args[(++i)];
      return 2;
    }
    if (args[i].equals("-port"))
    {
      if (i == args.length - 1) {
        throw new BadCommandLineException(Messages.format("Driver.MissingProxyPort"));
      }
      if (args[(i + 1)].startsWith("-")) {
        throw new BadCommandLineException(Messages.format("Driver.MissingProxyPort"));
      }
      this.proxyPort = args[(++i)];
      return 2;
    }
    if (args[i].equals("-catalog"))
    {
      if (i == args.length - 1) {
        throw new BadCommandLineException(Messages.format("Driver.MissingCatalog"));
      }
      addCatalog(new File(args[(++i)]));
      return 2;
    }
    if (args[i].equals("-source"))
    {
      if (i == args.length - 1) {
        return 1;
      }
      return 2;
    }
    for (int j = 0; j < codeAugmenters.length; j++)
    {
      CodeAugmenter ma = (CodeAugmenter)codeAugmenters[j];
      if (("-" + ma.getOptionName()).equals(args[i]))
      {
        this.enabledModelAugmentors.add(ma);
        return 1;
      }
      int r = ma.parseArgument(this, args, i);
      if (r != 0) {
        return r;
      }
    }
    return 0;
  }
  
  public void addCatalog(File catalogFile)
    throws IOException
  {
    if (this.entityResolver == null)
    {
      CatalogManager.ignoreMissingProperties(true);
      this.entityResolver = new CatalogResolver(true);
    }
    ((CatalogResolver)this.entityResolver).getCatalog().parseCatalog(catalogFile.getPath());
  }
  
  public void parseArguments(String[] args)
    throws BadCommandLineException, IOException
  {
    for (int i = 0; i < args.length; i++) {
      if (args[i].charAt(0) == '-')
      {
        int j = parseArgument(args, i);
        if (j == 0) {
          throw new BadCommandLineException(Messages.format("Driver.UnrecognizedParameter", args[i]));
        }
        i += j - 1;
      }
      else
      {
        addGrammar(com.sun.tools.xjc.reader.Util.getInputSource(args[i]));
      }
    }
    if ((this.proxyHost != null) || (this.proxyPort != null)) {
      if ((this.proxyHost != null) && (this.proxyPort != null))
      {
        System.setProperty("http.proxyHost", this.proxyHost);
        System.setProperty("http.proxyPort", this.proxyPort);
        System.setProperty("https.proxyHost", this.proxyHost);
        System.setProperty("https.proxyPort", this.proxyPort);
      }
      else
      {
        if (this.proxyHost == null) {
          throw new BadCommandLineException(Messages.format("Driver.MissingProxyHost"));
        }
        throw new BadCommandLineException(Messages.format("Driver.MissingProxyPort"));
      }
    }
    if (this.grammars.size() == 0) {
      throw new BadCommandLineException(Messages.format("Driver.MissingGrammar"));
    }
    if (this.schemaLanguage == -1) {
      this.schemaLanguage = guessSchemaLanguage();
    }
  }
  
  public int guessSchemaLanguage()
  {
    if (this.grammars.size() > 1) {
      return 1;
    }
    String name = ((InputSource)this.grammars.get(0)).getSystemId().toLowerCase();
    if (name.endsWith(".rng")) {
      return 2;
    }
    if (name.endsWith(".dtd")) {
      return 0;
    }
    if (name.endsWith(".wsdl")) {
      return 3;
    }
    return 1;
  }
  
  private static Object[] findServices(String className)
  {
    return findServices(className, Driver.class.getClassLoader());
  }
  
  private static Object[] findServices(String className, ClassLoader classLoader)
  {
    boolean debug = com.sun.tools.xjc.util.Util.getSystemProperty(Options.class, "findServices") != null;
    
    String serviceId = "META-INF/services/" + className;
    if (debug) {
      System.out.println("Looking for " + serviceId + " for add-ons");
    }
    try
    {
      Enumeration e = classLoader.getResources(serviceId);
      if (e == null) {
        return new Object[0];
      }
      ArrayList a = new ArrayList();
      while (e.hasMoreElements())
      {
        URL url = (URL)e.nextElement();
        BufferedReader reader = null;
        if (debug) {
          System.out.println("Checking " + url + " for an add-on");
        }
        try
        {
          reader = new BufferedReader(new InputStreamReader(url.openStream()));
          String impl;
          while ((impl = reader.readLine()) != null)
          {
            impl = impl.trim();
            if (debug) {
              System.out.println("Attempting to instanciate " + impl);
            }
            Class implClass = classLoader.loadClass(impl);
            a.add(implClass.newInstance());
          }
          reader.close();
        }
        catch (Exception ex)
        {
          if (debug) {
            ex.printStackTrace(System.out);
          }
          if (reader != null) {
            try
            {
              reader.close();
            }
            catch (IOException ex2) {}
          }
        }
      }
      return a.toArray();
    }
    catch (Throwable e)
    {
      if (debug) {
        e.printStackTrace(System.out);
      }
    }
    return new Object[0];
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\Options.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */