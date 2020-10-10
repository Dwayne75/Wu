package org.apache.xml.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

public class CatalogManager
{
  private static String pFiles = "xml.catalog.files";
  private static String pVerbosity = "xml.catalog.verbosity";
  private static String pPrefer = "xml.catalog.prefer";
  private static String pStatic = "xml.catalog.staticCatalog";
  private static String pAllowPI = "xml.catalog.allowPI";
  private static String pClassname = "xml.catalog.className";
  private static String pIgnoreMissing = "xml.catalog.ignoreMissing";
  private static boolean ignoreMissingProperties = (System.getProperty(pIgnoreMissing) != null) || (System.getProperty(pFiles) != null);
  private static ResourceBundle resources;
  private static String propertyFile = "CatalogManager.properties";
  private static URL propertyFileURI = null;
  private static String defaultCatalogFiles = "./xcatalog";
  private static int defaultVerbosity = 1;
  private static boolean defaultPreferPublic = true;
  private static boolean defaultStaticCatalog = true;
  private static boolean defaultOasisXMLCatalogPI = true;
  private static boolean defaultRelativeCatalogs = true;
  
  private static synchronized void readProperties()
  {
    try
    {
      propertyFileURI = CatalogManager.class.getResource("/" + propertyFile);
      InputStream localInputStream = CatalogManager.class.getResourceAsStream("/" + propertyFile);
      if (localInputStream == null)
      {
        if (!ignoreMissingProperties) {
          System.err.println("Cannot find " + propertyFile);
        }
        return;
      }
      resources = new PropertyResourceBundle(localInputStream);
    }
    catch (MissingResourceException localMissingResourceException)
    {
      if (!ignoreMissingProperties) {
        System.err.println("Cannot read " + propertyFile);
      }
    }
    catch (IOException localIOException)
    {
      if (!ignoreMissingProperties) {
        System.err.println("Failure trying to read " + propertyFile);
      }
    }
  }
  
  public static void ignoreMissingProperties(boolean paramBoolean)
  {
    ignoreMissingProperties = paramBoolean;
  }
  
  public static int verbosity()
  {
    String str = System.getProperty(pVerbosity);
    if (str == null)
    {
      if (resources == null) {
        readProperties();
      }
      if (resources == null) {
        return defaultVerbosity;
      }
      try
      {
        str = resources.getString("verbosity");
      }
      catch (MissingResourceException localMissingResourceException)
      {
        return defaultVerbosity;
      }
    }
    try
    {
      int i = Integer.parseInt(str.trim());
      return i;
    }
    catch (Exception localException)
    {
      System.err.println("Cannot parse verbosity: \"" + str + "\"");
    }
    return defaultVerbosity;
  }
  
  public static boolean relativeCatalogs()
  {
    if (resources == null) {
      readProperties();
    }
    if (resources == null) {
      return defaultRelativeCatalogs;
    }
    try
    {
      String str = resources.getString("relative-catalogs");
      return (str.equalsIgnoreCase("true")) || (str.equalsIgnoreCase("yes")) || (str.equalsIgnoreCase("1"));
    }
    catch (MissingResourceException localMissingResourceException) {}
    return defaultRelativeCatalogs;
  }
  
  public static Vector catalogFiles()
  {
    String str1 = System.getProperty(pFiles);
    int i = 0;
    if (str1 == null)
    {
      if (resources == null) {
        readProperties();
      }
      if (resources != null) {
        try
        {
          str1 = resources.getString("catalogs");
          i = 1;
        }
        catch (MissingResourceException localMissingResourceException)
        {
          System.err.println(propertyFile + ": catalogs not found.");
          str1 = null;
        }
      }
    }
    if (str1 == null) {
      str1 = defaultCatalogFiles;
    }
    StringTokenizer localStringTokenizer = new StringTokenizer(str1, ";");
    Vector localVector = new Vector();
    while (localStringTokenizer.hasMoreTokens())
    {
      String str2 = localStringTokenizer.nextToken();
      URL localURL = null;
      if ((i != 0) && (!relativeCatalogs())) {
        try
        {
          localURL = new URL(propertyFileURI, str2);
          str2 = localURL.toString();
        }
        catch (MalformedURLException localMalformedURLException)
        {
          localURL = null;
        }
      }
      localVector.add(str2);
    }
    return localVector;
  }
  
  public static boolean preferPublic()
  {
    String str = System.getProperty(pPrefer);
    if (str == null)
    {
      if (resources == null) {
        readProperties();
      }
      if (resources == null) {
        return defaultPreferPublic;
      }
      try
      {
        str = resources.getString("prefer");
      }
      catch (MissingResourceException localMissingResourceException)
      {
        return defaultPreferPublic;
      }
    }
    if (str == null) {
      return defaultPreferPublic;
    }
    return str.equalsIgnoreCase("public");
  }
  
  public static boolean staticCatalog()
  {
    String str = System.getProperty(pStatic);
    if (str == null)
    {
      if (resources == null) {
        readProperties();
      }
      if (resources == null) {
        return defaultStaticCatalog;
      }
      try
      {
        str = resources.getString("static-catalog");
      }
      catch (MissingResourceException localMissingResourceException)
      {
        return defaultStaticCatalog;
      }
    }
    if (str == null) {
      return defaultStaticCatalog;
    }
    return (str.equalsIgnoreCase("true")) || (str.equalsIgnoreCase("yes")) || (str.equalsIgnoreCase("1"));
  }
  
  public static boolean allowOasisXMLCatalogPI()
  {
    String str = System.getProperty(pAllowPI);
    if (str == null)
    {
      if (resources == null) {
        readProperties();
      }
      if (resources == null) {
        return defaultOasisXMLCatalogPI;
      }
      try
      {
        str = resources.getString("allow-oasis-xml-catalog-pi");
      }
      catch (MissingResourceException localMissingResourceException)
      {
        return defaultOasisXMLCatalogPI;
      }
    }
    if (str == null) {
      return defaultOasisXMLCatalogPI;
    }
    return (str.equalsIgnoreCase("true")) || (str.equalsIgnoreCase("yes")) || (str.equalsIgnoreCase("1"));
  }
  
  public static String catalogClassName()
  {
    String str = System.getProperty(pClassname);
    if (str == null)
    {
      if (resources == null) {
        readProperties();
      }
      if (resources == null) {
        return null;
      }
      try
      {
        return resources.getString("catalog-class-name");
      }
      catch (MissingResourceException localMissingResourceException)
      {
        return null;
      }
    }
    return str;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\CatalogManager.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */