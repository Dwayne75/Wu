package com.sun.tools.xjc;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.codemodel.writer.PrologCodeWriter;
import com.sun.org.apache.xml.internal.resolver.Catalog;
import com.sun.org.apache.xml.internal.resolver.CatalogManager;
import com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver;
import com.sun.tools.xjc.api.ClassNameAllocator;
import com.sun.tools.xjc.api.SpecVersion;
import com.sun.tools.xjc.generator.bean.field.FieldRendererFactory;
import com.sun.xml.bind.api.impl.NameConverter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class Options
{
  public boolean debugMode;
  public boolean verbose;
  public boolean quiet;
  public boolean readOnly;
  public boolean noFileHeader;
  public boolean strictCheck = true;
  public boolean runtime14 = false;
  public boolean automaticNameConflictResolution = false;
  public static final int STRICT = 1;
  public static final int EXTENSION = 2;
  public int compatibilityMode = 1;
  
  public boolean isExtensionMode()
  {
    return this.compatibilityMode == 2;
  }
  
  public SpecVersion target = SpecVersion.V2_1;
  public File targetDir = new File(".");
  public EntityResolver entityResolver = null;
  private Language schemaLanguage = null;
  public String defaultPackage = null;
  public String defaultPackage2 = null;
  private final List<InputSource> grammars = new ArrayList();
  private final List<InputSource> bindFiles = new ArrayList();
  private String proxyHost = null;
  private String proxyPort = null;
  private String proxyUser = null;
  private String proxyPassword = null;
  public final List<Plugin> activePlugins = new ArrayList();
  private List<Plugin> allPlugins;
  public final Set<String> pluginURIs = new HashSet();
  public ClassNameAllocator classNameAllocator;
  public boolean packageLevelAnnotations = true;
  private FieldRendererFactory fieldRendererFactory = new FieldRendererFactory();
  private Plugin fieldRendererFactoryOwner = null;
  private NameConverter nameConverter = null;
  private Plugin nameConverterOwner = null;
  
  public FieldRendererFactory getFieldRendererFactory()
  {
    return this.fieldRendererFactory;
  }
  
  public void setFieldRendererFactory(FieldRendererFactory frf, Plugin owner)
    throws BadCommandLineException
  {
    if (frf == null) {
      throw new IllegalArgumentException();
    }
    if (this.fieldRendererFactoryOwner != null) {
      throw new BadCommandLineException(Messages.format("FIELD_RENDERER_CONFLICT", new Object[] { this.fieldRendererFactoryOwner.getOptionName(), owner.getOptionName() }));
    }
    this.fieldRendererFactoryOwner = owner;
    this.fieldRendererFactory = frf;
  }
  
  public NameConverter getNameConverter()
  {
    return this.nameConverter;
  }
  
  public void setNameConverter(NameConverter nc, Plugin owner)
    throws BadCommandLineException
  {
    if (nc == null) {
      throw new IllegalArgumentException();
    }
    if (this.nameConverter != null) {
      throw new BadCommandLineException(Messages.format("NAME_CONVERTER_CONFLICT", new Object[] { this.nameConverterOwner.getOptionName(), owner.getOptionName() }));
    }
    this.nameConverterOwner = owner;
    this.nameConverter = nc;
  }
  
  public List<Plugin> getAllPlugins()
  {
    if (this.allPlugins == null)
    {
      this.allPlugins = new ArrayList();
      ClassLoader ucl = getUserClassLoader(getClass().getClassLoader());
      for (Plugin aug : (Plugin[])findServices(Plugin.class, ucl)) {
        this.allPlugins.add(aug);
      }
    }
    return this.allPlugins;
  }
  
  public Language getSchemaLanguage()
  {
    if (this.schemaLanguage == null) {
      this.schemaLanguage = guessSchemaLanguage();
    }
    return this.schemaLanguage;
  }
  
  public void setSchemaLanguage(Language _schemaLanguage)
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
  
  private InputSource fileToInputSource(File source)
  {
    try
    {
      String url = source.toURL().toExternalForm();
      return new InputSource(com.sun.tools.xjc.reader.Util.escapeSpace(url));
    }
    catch (MalformedURLException e) {}
    return new InputSource(source.getPath());
  }
  
  public void addGrammar(File source)
  {
    addGrammar(fileToInputSource(source));
  }
  
  public void addGrammarRecursive(File dir)
  {
    addRecursive(dir, ".xsd", this.grammars);
  }
  
  private void addRecursive(File dir, String suffix, List<InputSource> result)
  {
    File[] files = dir.listFiles();
    if (files == null) {
      return;
    }
    for (File f : files) {
      if (f.isDirectory()) {
        addRecursive(f, suffix, result);
      } else if (f.getPath().endsWith(suffix)) {
        result.add(absolutize(fileToInputSource(f)));
      }
    }
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
  
  public void addBindFile(File bindFile)
  {
    this.bindFiles.add(fileToInputSource(bindFile));
  }
  
  public void addBindFileRecursive(File dir)
  {
    addRecursive(dir, ".xjb", this.bindFiles);
  }
  
  public final List<URL> classpaths = new ArrayList();
  private static String pluginLoadFailure;
  
  public URLClassLoader getUserClassLoader(ClassLoader parent)
  {
    return new URLClassLoader((URL[])this.classpaths.toArray(new URL[this.classpaths.size()]), parent);
  }
  
  public int parseArgument(String[] args, int i)
    throws BadCommandLineException
  {
    if ((args[i].equals("-classpath")) || (args[i].equals("-cp")))
    {
      File file = new File(requireArgument(args[i], args, ++i));
      try
      {
        this.classpaths.add(file.toURL());
      }
      catch (MalformedURLException e)
      {
        throw new BadCommandLineException(Messages.format("Driver.NotAValidFileName", new Object[] { file }), e);
      }
      return 2;
    }
    if (args[i].equals("-d"))
    {
      this.targetDir = new File(requireArgument("-d", args, ++i));
      if (!this.targetDir.exists()) {
        throw new BadCommandLineException(Messages.format("Driver.NonExistentDir", new Object[] { this.targetDir }));
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
      this.defaultPackage = requireArgument("-p", args, ++i);
      if (this.defaultPackage.length() == 0) {
        this.packageLevelAnnotations = false;
      }
      return 2;
    }
    if (args[i].equals("-debug"))
    {
      this.debugMode = true;
      this.verbose = true;
      return 1;
    }
    if (args[i].equals("-nv"))
    {
      this.strictCheck = false;
      return 1;
    }
    if (args[i].equals("-npa"))
    {
      this.packageLevelAnnotations = false;
      return 1;
    }
    if (args[i].equals("-no-header"))
    {
      this.noFileHeader = true;
      return 1;
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
    if (args[i].equals("-XexplicitAnnotation"))
    {
      this.runtime14 = true;
      return 1;
    }
    if (args[i].equals("-XautoNameResolution"))
    {
      this.automaticNameConflictResolution = true;
      return 1;
    }
    if (args[i].equals("-b"))
    {
      addFile(requireArgument("-b", args, ++i), this.bindFiles, ".xjb");
      return 2;
    }
    if (args[i].equals("-dtd"))
    {
      this.schemaLanguage = Language.DTD;
      return 1;
    }
    if (args[i].equals("-relaxng"))
    {
      this.schemaLanguage = Language.RELAXNG;
      return 1;
    }
    if (args[i].equals("-relaxng-compact"))
    {
      this.schemaLanguage = Language.RELAXNG_COMPACT;
      return 1;
    }
    if (args[i].equals("-xmlschema"))
    {
      this.schemaLanguage = Language.XMLSCHEMA;
      return 1;
    }
    if (args[i].equals("-wsdl"))
    {
      this.schemaLanguage = Language.WSDL;
      return 1;
    }
    if (args[i].equals("-extension"))
    {
      this.compatibilityMode = 2;
      return 1;
    }
    if (args[i].equals("-target"))
    {
      String token = requireArgument("-target", args, ++i);
      this.target = SpecVersion.parse(token);
      if (this.target == null) {
        throw new BadCommandLineException(Messages.format("Driver.ILLEGAL_TARGET_VERSION", new Object[] { token }));
      }
      return 2;
    }
    if (args[i].equals("-httpproxyfile"))
    {
      if ((i == args.length - 1) || (args[(i + 1)].startsWith("-"))) {
        throw new BadCommandLineException(Messages.format("Driver.MISSING_PROXYFILE", new Object[0]));
      }
      File file = new File(args[(++i)]);
      if (!file.exists()) {
        throw new BadCommandLineException(Messages.format("Driver.NO_SUCH_FILE", new Object[] { file }));
      }
      try
      {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        parseProxy(in.readLine());
        in.close();
      }
      catch (IOException e)
      {
        throw new BadCommandLineException(Messages.format("Driver.FailedToParse", new Object[] { file, e.getMessage() }), e);
      }
      return 2;
    }
    if (args[i].equals("-httpproxy"))
    {
      if ((i == args.length - 1) || (args[(i + 1)].startsWith("-"))) {
        throw new BadCommandLineException(Messages.format("Driver.MISSING_PROXY", new Object[0]));
      }
      parseProxy(args[(++i)]);
      return 2;
    }
    if (args[i].equals("-host"))
    {
      this.proxyHost = requireArgument("-host", args, ++i);
      return 2;
    }
    if (args[i].equals("-port"))
    {
      this.proxyPort = requireArgument("-port", args, ++i);
      return 2;
    }
    if (args[i].equals("-catalog"))
    {
      File catalogFile = new File(requireArgument("-catalog", args, ++i));
      try
      {
        addCatalog(catalogFile);
      }
      catch (IOException e)
      {
        throw new BadCommandLineException(Messages.format("Driver.FailedToParse", new Object[] { catalogFile, e.getMessage() }), e);
      }
      return 2;
    }
    if (args[i].equals("-source"))
    {
      String version = requireArgument("-source", args, ++i);
      if ((!version.equals("2.0")) && (!version.equals("2.1"))) {
        throw new BadCommandLineException(Messages.format("Driver.DefaultVersion", new Object[0]));
      }
      return 2;
    }
    if (args[i].equals("-Xtest-class-name-allocator"))
    {
      this.classNameAllocator = new ClassNameAllocator()
      {
        public String assignClassName(String packageName, String className)
        {
          System.out.printf("assignClassName(%s,%s)\n", new Object[] { packageName, className });
          return className + "_Type";
        }
      };
      return 1;
    }
    for (Plugin plugin : getAllPlugins()) {
      try
      {
        if (('-' + plugin.getOptionName()).equals(args[i]))
        {
          this.activePlugins.add(plugin);
          plugin.onActivated(this);
          this.pluginURIs.addAll(plugin.getCustomizationURIs());
          
          int r = plugin.parseArgument(this, args, i);
          if (r != 0) {
            return r;
          }
          return 1;
        }
        int r = plugin.parseArgument(this, args, i);
        if (r != 0) {
          return r;
        }
      }
      catch (IOException e)
      {
        throw new BadCommandLineException(e.getMessage(), e);
      }
    }
    return 0;
  }
  
  private void parseProxy(String text)
    throws BadCommandLineException
  {
    String token = "([^@:]+)";
    Pattern p = Pattern.compile("(?:" + token + "(?:\\:" + token + ")?\\@)?" + token + "(?:\\:" + token + ")");
    
    Matcher matcher = p.matcher(text);
    if (!matcher.matches()) {
      throw new BadCommandLineException(Messages.format("Driver.ILLEGAL_PROXY", new Object[] { text }));
    }
    this.proxyUser = matcher.group(1);
    this.proxyPassword = matcher.group(2);
    this.proxyHost = matcher.group(3);
    this.proxyPort = matcher.group(4);
    try
    {
      Integer.valueOf(this.proxyPort);
    }
    catch (NumberFormatException e)
    {
      throw new BadCommandLineException(Messages.format("Driver.ILLEGAL_PROXY", new Object[] { text }));
    }
  }
  
  public String requireArgument(String optionName, String[] args, int i)
    throws BadCommandLineException
  {
    if ((i == args.length) || (args[i].startsWith("-"))) {
      throw new BadCommandLineException(Messages.format("Driver.MissingOperand", new Object[] { optionName }));
    }
    return args[i];
  }
  
  private void addFile(String name, List<InputSource> target, String suffix)
    throws BadCommandLineException
  {
    Object src;
    try
    {
      src = com.sun.tools.xjc.reader.Util.getFileOrURL(name);
    }
    catch (IOException e)
    {
      throw new BadCommandLineException(Messages.format("Driver.NotAFileNorURL", new Object[] { name }));
    }
    if ((src instanceof URL))
    {
      target.add(absolutize(new InputSource(com.sun.tools.xjc.reader.Util.escapeSpace(((URL)src).toExternalForm()))));
    }
    else
    {
      File fsrc = (File)src;
      if (fsrc.isDirectory()) {
        addRecursive(fsrc, suffix, target);
      } else {
        target.add(absolutize(fileToInputSource(fsrc)));
      }
    }
  }
  
  public void addCatalog(File catalogFile)
    throws IOException
  {
    if (this.entityResolver == null)
    {
      CatalogManager.getStaticManager().setIgnoreMissingProperties(true);
      this.entityResolver = new CatalogResolver(true);
    }
    ((CatalogResolver)this.entityResolver).getCatalog().parseCatalog(catalogFile.getPath());
  }
  
  public void parseArguments(String[] args)
    throws BadCommandLineException
  {
    for (int i = 0; i < args.length; i++)
    {
      if (args[i].length() == 0) {
        throw new BadCommandLineException();
      }
      if (args[i].charAt(0) == '-')
      {
        int j = parseArgument(args, i);
        if (j == 0) {
          throw new BadCommandLineException(Messages.format("Driver.UnrecognizedParameter", new Object[] { args[i] }));
        }
        i += j - 1;
      }
      else if (args[i].endsWith(".jar"))
      {
        scanEpisodeFile(new File(args[i]));
      }
      else
      {
        addFile(args[i], this.grammars, ".xsd");
      }
    }
    if ((this.proxyHost != null) || (this.proxyPort != null))
    {
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
          throw new BadCommandLineException(Messages.format("Driver.MissingProxyHost", new Object[0]));
        }
        throw new BadCommandLineException(Messages.format("Driver.MissingProxyPort", new Object[0]));
      }
      if (this.proxyUser != null) {
        System.setProperty("http.proxyUser", this.proxyUser);
      }
      if (this.proxyPassword != null) {
        System.setProperty("http.proxyPassword", this.proxyPassword);
      }
    }
    if (this.grammars.size() == 0) {
      throw new BadCommandLineException(Messages.format("Driver.MissingGrammar", new Object[0]));
    }
    if (this.schemaLanguage == null) {
      this.schemaLanguage = guessSchemaLanguage();
    }
    if (pluginLoadFailure != null) {
      throw new BadCommandLineException(Messages.format("PLUGIN_LOAD_FAILURE", new Object[] { pluginLoadFailure }));
    }
  }
  
  private void scanEpisodeFile(File jar)
    throws BadCommandLineException
  {
    try
    {
      URLClassLoader ucl = new URLClassLoader(new URL[] { jar.toURL() });
      Enumeration<URL> resources = ucl.findResources("META-INF/sun-jaxb.episode");
      while (resources.hasMoreElements())
      {
        URL url = (URL)resources.nextElement();
        addBindFile(new InputSource(url.toExternalForm()));
      }
    }
    catch (IOException e)
    {
      throw new BadCommandLineException(Messages.format("FAILED_TO_LOAD", new Object[] { jar, e.getMessage() }), e);
    }
  }
  
  public Language guessSchemaLanguage()
  {
    String name = ((InputSource)this.grammars.get(0)).getSystemId().toLowerCase();
    if (name.endsWith(".rng")) {
      return Language.RELAXNG;
    }
    if (name.endsWith(".rnc")) {
      return Language.RELAXNG_COMPACT;
    }
    if (name.endsWith(".dtd")) {
      return Language.DTD;
    }
    if (name.endsWith(".wsdl")) {
      return Language.WSDL;
    }
    return Language.XMLSCHEMA;
  }
  
  public CodeWriter createCodeWriter()
    throws IOException
  {
    return createCodeWriter(new FileCodeWriter(this.targetDir, this.readOnly));
  }
  
  public CodeWriter createCodeWriter(CodeWriter core)
  {
    if (this.noFileHeader) {
      return core;
    }
    return new PrologCodeWriter(core, getPrologComment());
  }
  
  public String getPrologComment()
  {
    String format = Messages.format("Driver.DateFormat", new Object[0]) + " '" + Messages.format("Driver.At", new Object[0]) + "' " + Messages.format("Driver.TimeFormat", new Object[0]);
    
    SimpleDateFormat dateFormat = new SimpleDateFormat(format);
    
    return Messages.format("Driver.FilePrologComment", new Object[] { dateFormat.format(new Date()) });
  }
  
  private static <T> T[] findServices(Class<T> clazz, ClassLoader classLoader)
  {
    boolean debug = com.sun.tools.xjc.util.Util.getSystemProperty(Options.class, "findServices") != null;
    try
    {
      Class<?> serviceLoader = Class.forName("java.util.ServiceLoader");
      if (debug) {
        System.out.println("Using java.util.ServiceLoader");
      }
      Iterable<T> itr = (Iterable)serviceLoader.getMethod("load", new Class[] { Class.class, ClassLoader.class }).invoke(null, new Object[] { clazz, classLoader });
      List<T> r = new ArrayList();
      for (T t : itr) {
        r.add(t);
      }
      return r.toArray((Object[])Array.newInstance(clazz, r.size()));
    }
    catch (ClassNotFoundException e) {}catch (IllegalAccessException e)
    {
      Error x = new IllegalAccessError();
      x.initCause(e);
      throw x;
    }
    catch (InvocationTargetException e)
    {
      Throwable x = e.getTargetException();
      if ((x instanceof RuntimeException)) {
        throw ((RuntimeException)x);
      }
      if ((x instanceof Error)) {
        throw ((Error)x);
      }
      throw new Error(x);
    }
    catch (NoSuchMethodException e)
    {
      Error x = new NoSuchMethodError();
      x.initCause(e);
      throw x;
    }
    String serviceId = "META-INF/services/" + clazz.getName();
    
    Set<String> classNames = new HashSet();
    if (debug) {
      System.out.println("Looking for " + serviceId + " for add-ons");
    }
    try
    {
      Enumeration<URL> e = classLoader.getResources(serviceId);
      if (e == null) {
        return (Object[])Array.newInstance(clazz, 0);
      }
      ArrayList<T> a = new ArrayList();
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
            if (classNames.add(impl))
            {
              Class implClass = classLoader.loadClass(impl);
              if (!clazz.isAssignableFrom(implClass))
              {
                pluginLoadFailure = impl + " is not a subclass of " + clazz + ". Skipping";
                if (debug) {
                  System.out.println(pluginLoadFailure);
                }
              }
              else
              {
                if (debug) {
                  System.out.println("Attempting to instanciate " + impl);
                }
                a.add(clazz.cast(implClass.newInstance()));
              }
            }
          }
          reader.close();
        }
        catch (Exception ex)
        {
          StringWriter w = new StringWriter();
          ex.printStackTrace(new PrintWriter(w));
          pluginLoadFailure = w.toString();
          if (debug) {
            System.out.println(pluginLoadFailure);
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
      return a.toArray((Object[])Array.newInstance(clazz, a.size()));
    }
    catch (Throwable e)
    {
      StringWriter w = new StringWriter();
      e.printStackTrace(new PrintWriter(w));
      pluginLoadFailure = w.toString();
      if (debug) {
        System.out.println(pluginLoadFailure);
      }
    }
    return (Object[])Array.newInstance(clazz, 0);
  }
  
  public static String getBuildID()
  {
    return Messages.format("Driver.BuildID", new Object[0]);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\Options.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */