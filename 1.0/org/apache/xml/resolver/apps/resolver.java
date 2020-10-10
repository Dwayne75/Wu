package org.apache.xml.resolver.apps;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.helpers.Debug;
import org.apache.xml.resolver.tools.CatalogResolver;

public class resolver
{
  public static void main(String[] paramArrayOfString)
    throws FileNotFoundException, IOException
  {
    int i = 0;
    Vector localVector = new Vector();
    int j = 0;
    String str1 = null;
    String str2 = null;
    String str3 = null;
    String str4 = null;
    String str5 = null;
    int k = 0;
    for (int m = 0; m < paramArrayOfString.length; m++) {
      if (paramArrayOfString[m].equals("-c"))
      {
        m++;
        localVector.add(paramArrayOfString[m]);
      }
      else if (paramArrayOfString[m].equals("-p"))
      {
        m++;
        str3 = paramArrayOfString[m];
      }
      else if (paramArrayOfString[m].equals("-s"))
      {
        m++;
        str4 = paramArrayOfString[m];
      }
      else if (paramArrayOfString[m].equals("-n"))
      {
        m++;
        str2 = paramArrayOfString[m];
      }
      else if (paramArrayOfString[m].equals("-u"))
      {
        m++;
        str5 = paramArrayOfString[m];
      }
      else if (paramArrayOfString[m].equals("-a"))
      {
        k = 1;
      }
      else if (paramArrayOfString[m].equals("-d"))
      {
        m++;
        localObject1 = paramArrayOfString[m];
        try
        {
          i = Integer.parseInt((String)localObject1);
          if (i >= 0) {
            Debug.setDebug(i);
          }
        }
        catch (Exception localException) {}
      }
      else
      {
        str1 = paramArrayOfString[m];
      }
    }
    if (str1 == null) {
      usage();
    }
    if (str1.equalsIgnoreCase("doctype"))
    {
      j = Catalog.DOCTYPE;
      if ((str3 == null) && (str4 == null))
      {
        System.out.println("DOCTYPE requires public or system identifier.");
        usage();
      }
    }
    else if (str1.equalsIgnoreCase("document"))
    {
      j = Catalog.DOCUMENT;
    }
    else if (str1.equalsIgnoreCase("entity"))
    {
      j = Catalog.ENTITY;
      if ((str3 == null) && (str4 == null) && (str2 == null))
      {
        System.out.println("ENTITY requires name or public or system identifier.");
        usage();
      }
    }
    else if (str1.equalsIgnoreCase("notation"))
    {
      j = Catalog.NOTATION;
      if ((str3 == null) && (str4 == null) && (str2 == null))
      {
        System.out.println("NOTATION requires name or public or system identifier.");
        usage();
      }
    }
    else if (str1.equalsIgnoreCase("public"))
    {
      j = Catalog.PUBLIC;
      if (str3 == null)
      {
        System.out.println("PUBLIC requires public identifier.");
        usage();
      }
    }
    else if (str1.equalsIgnoreCase("system"))
    {
      j = Catalog.SYSTEM;
      if (str4 == null)
      {
        System.out.println("SYSTEM requires system identifier.");
        usage();
      }
    }
    else if (str1.equalsIgnoreCase("uri"))
    {
      j = Catalog.URI;
      if (str5 == null)
      {
        System.out.println("URI requires a uri.");
        usage();
      }
    }
    else
    {
      System.out.println(str1 + " is not a recognized keyword.");
      usage();
    }
    if (k != 0)
    {
      localObject1 = null;
      localObject2 = null;
      try
      {
        String str6 = System.getProperty("user.dir");
        str6.replace('\\', '/');
        localObject1 = new URL("file:///" + str6 + "/basename");
      }
      catch (MalformedURLException localMalformedURLException1)
      {
        String str7 = System.getProperty("user.dir");
        str7.replace('\\', '/');
        Debug.message(1, "Malformed URL on cwd", str7);
        localObject1 = null;
      }
      try
      {
        localObject2 = new URL((URL)localObject1, str4);
        str4 = ((URL)localObject2).toString();
      }
      catch (MalformedURLException localMalformedURLException2)
      {
        try
        {
          localObject2 = new URL("file:///" + str4);
        }
        catch (MalformedURLException localMalformedURLException3)
        {
          Debug.message(1, "Malformed URL on system id", str4);
        }
      }
    }
    Object localObject1 = new CatalogResolver();
    Object localObject2 = ((CatalogResolver)localObject1).getCatalog();
    for (int n = 0; n < localVector.size(); n++)
    {
      str8 = (String)localVector.elementAt(n);
      ((Catalog)localObject2).parseCatalog(str8);
    }
    String str8 = null;
    if (j == Catalog.DOCTYPE)
    {
      System.out.println("Resolve DOCTYPE (name, publicid, systemid):");
      if (str2 != null) {
        System.out.println("       name: " + str2);
      }
      if (str3 != null) {
        System.out.println("  public id: " + str3);
      }
      if (str4 != null) {
        System.out.println("  system id: " + str4);
      }
      if (str5 != null) {
        System.out.println("        uri: " + str5);
      }
      str8 = ((Catalog)localObject2).resolveDoctype(str2, str3, str4);
    }
    else if (j == Catalog.DOCUMENT)
    {
      System.out.println("Resolve DOCUMENT ():");
      str8 = ((Catalog)localObject2).resolveDocument();
    }
    else if (j == Catalog.ENTITY)
    {
      System.out.println("Resolve ENTITY (name, publicid, systemid):");
      if (str2 != null) {
        System.out.println("       name: " + str2);
      }
      if (str3 != null) {
        System.out.println("  public id: " + str3);
      }
      if (str4 != null) {
        System.out.println("  system id: " + str4);
      }
      str8 = ((Catalog)localObject2).resolveEntity(str2, str3, str4);
    }
    else if (j == Catalog.NOTATION)
    {
      System.out.println("Resolve NOTATION (name, publicid, systemid):");
      if (str2 != null) {
        System.out.println("       name: " + str2);
      }
      if (str3 != null) {
        System.out.println("  public id: " + str3);
      }
      if (str4 != null) {
        System.out.println("  system id: " + str4);
      }
      str8 = ((Catalog)localObject2).resolveNotation(str2, str3, str4);
    }
    else if (j == Catalog.PUBLIC)
    {
      System.out.println("Resolve PUBLIC (publicid, systemid):");
      if (str3 != null) {
        System.out.println("  public id: " + str3);
      }
      if (str4 != null) {
        System.out.println("  system id: " + str4);
      }
      str8 = ((Catalog)localObject2).resolvePublic(str3, str4);
    }
    else if (j == Catalog.SYSTEM)
    {
      System.out.println("Resolve SYSTEM (systemid):");
      if (str4 != null) {
        System.out.println("  system id: " + str4);
      }
      str8 = ((Catalog)localObject2).resolveSystem(str4);
    }
    else if (j == Catalog.URI)
    {
      System.out.println("Resolve URI (uri):");
      if (str5 != null) {
        System.out.println("        uri: " + str5);
      }
      str8 = ((Catalog)localObject2).resolveURI(str5);
    }
    else
    {
      System.out.println("resType is wrong!? This can't happen!");
      usage();
    }
    System.out.println("Result: " + str8);
  }
  
  public static void usage()
  {
    System.out.println("Usage: resolver [options] keyword");
    System.out.println("");
    System.out.println("Where:");
    System.out.println("");
    System.out.println("-c catalogfile  Loads a particular catalog file.");
    System.out.println("-n name         Sets the name.");
    System.out.println("-p publicId     Sets the public identifier.");
    System.out.println("-s systemId     Sets the system identifier.");
    System.out.println("-a              Makes the system URI absolute before resolution");
    System.out.println("-u uri          Sets the URI.");
    System.out.println("-d integer      Set the debug level.");
    System.out.println("keyword         Identifies the type of resolution to perform:");
    System.out.println("                doctype, document, entity, notation, public, system,");
    System.out.println("                or uri.");
    System.exit(1);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\apps\resolver.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */