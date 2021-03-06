package com.sun.tools.jxc;

import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.tools.jxc.apt.Options;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.api.util.APTClassLoader;
import com.sun.tools.xjc.api.util.ToolsJarNotFoundException;
import com.sun.xml.bind.util.Which;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;

public class SchemaGenerator
{
  public static void main(String[] args)
    throws Exception
  {
    System.exit(run(args));
  }
  
  public static int run(String[] args)
    throws Exception
  {
    try
    {
      ClassLoader cl = SchemaGenerator.class.getClassLoader();
      if (cl == null) {
        cl = ClassLoader.getSystemClassLoader();
      }
      ClassLoader classLoader = new APTClassLoader(cl, packagePrefixes);
      return run(args, classLoader);
    }
    catch (ToolsJarNotFoundException e)
    {
      System.err.println(e.getMessage());
    }
    return -1;
  }
  
  private static final String[] packagePrefixes = { "com.sun.tools.jxc.", "com.sun.tools.xjc.", "com.sun.istack.tools.", "com.sun.tools.apt.", "com.sun.tools.javac.", "com.sun.tools.javadoc.", "com.sun.mirror." };
  
  public static int run(String[] args, ClassLoader classLoader)
    throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
  {
    Options options = new Options();
    if (args.length == 0)
    {
      usage();
      return -1;
    }
    for (String arg : args)
    {
      if (arg.equals("-help"))
      {
        usage();
        return -1;
      }
      if (arg.equals("-version"))
      {
        System.out.println(Messages.VERSION.format(new Object[0]));
        return -1;
      }
    }
    try
    {
      options.parseArguments(args);
    }
    catch (BadCommandLineException e)
    {
      System.out.println(e.getMessage());
      System.out.println();
      usage();
      return -1;
    }
    Class schemagenRunner = classLoader.loadClass(Runner.class.getName());
    Method mainMethod = schemagenRunner.getDeclaredMethod("main", new Class[] { String[].class, File.class });
    
    List<String> aptargs = new ArrayList();
    if (hasClass(options.arguments)) {
      aptargs.add("-XclassesAsDecls");
    }
    File jaxbApi = findJaxbApiJar();
    if (jaxbApi != null) {
      if (options.classpath != null) {
        options.classpath = (options.classpath + File.pathSeparatorChar + jaxbApi);
      } else {
        options.classpath = jaxbApi.getPath();
      }
    }
    aptargs.add("-cp");
    aptargs.add(options.classpath);
    if (options.targetDir != null)
    {
      aptargs.add("-d");
      aptargs.add(options.targetDir.getPath());
    }
    aptargs.addAll(options.arguments);
    
    String[] argsarray = (String[])aptargs.toArray(new String[aptargs.size()]);
    return ((Integer)mainMethod.invoke(null, new Object[] { argsarray, options.episodeFile })).intValue();
  }
  
  private static File findJaxbApiJar()
  {
    String url = Which.which(JAXBContext.class);
    if (url == null) {
      return null;
    }
    if ((!url.startsWith("jar:")) || (url.lastIndexOf('!') == -1)) {
      return null;
    }
    String jarFileUrl = url.substring(4, url.lastIndexOf('!'));
    if (!jarFileUrl.startsWith("file:")) {
      return null;
    }
    try
    {
      File f = new File(new URL(jarFileUrl).getFile());
      if ((f.exists()) && (f.getName().endsWith(".jar"))) {
        return f;
      }
      return null;
    }
    catch (MalformedURLException e) {}
    return null;
  }
  
  private static boolean hasClass(List<String> args)
  {
    for (String arg : args) {
      if (!arg.endsWith(".java")) {
        return true;
      }
    }
    return false;
  }
  
  private static void usage()
  {
    System.out.println(Messages.USAGE.format(new Object[0]));
  }
  
  public static final class Runner
  {
    public static int main(String[] args, File episode)
      throws Exception
    {
      ClassLoader cl = Runner.class.getClassLoader();
      Class apt = cl.loadClass("com.sun.tools.apt.Main");
      Method processMethod = apt.getMethod("process", new Class[] { AnnotationProcessorFactory.class, String[].class });
      
      com.sun.tools.jxc.apt.SchemaGenerator r = new com.sun.tools.jxc.apt.SchemaGenerator();
      if (episode != null) {
        r.setEpisodeFile(episode);
      }
      return ((Integer)processMethod.invoke(null, new Object[] { r, args })).intValue();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\jxc\SchemaGenerator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */