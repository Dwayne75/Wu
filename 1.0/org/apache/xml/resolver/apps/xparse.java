package org.apache.xml.resolver.apps;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Vector;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.helpers.Debug;
import org.apache.xml.resolver.tools.ResolvingParser;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * @deprecated
 */
public class xparse
{
  public static void main(String[] paramArrayOfString)
    throws FileNotFoundException, IOException
  {
    String str1 = null;
    int i = 0;
    int j = 10;
    boolean bool1 = true;
    boolean bool2 = true;
    boolean bool3 = i > 2;
    boolean bool4 = true;
    Vector localVector = new Vector();
    for (int k = 0; k < paramArrayOfString.length; k++) {
      if (paramArrayOfString[k].equals("-c"))
      {
        k++;
        localVector.add(paramArrayOfString[k]);
      }
      else if (paramArrayOfString[k].equals("-w"))
      {
        bool2 = false;
      }
      else if (paramArrayOfString[k].equals("-v"))
      {
        bool2 = true;
      }
      else if (paramArrayOfString[k].equals("-n"))
      {
        bool1 = false;
      }
      else if (paramArrayOfString[k].equals("-N"))
      {
        bool1 = true;
      }
      else if (paramArrayOfString[k].equals("-d"))
      {
        k++;
        localObject1 = paramArrayOfString[k];
        try
        {
          i = Integer.parseInt((String)localObject1);
          if (i >= 0)
          {
            Debug.setDebug(i);
            bool3 = i > 2;
          }
        }
        catch (Exception localException1) {}
      }
      else if (paramArrayOfString[k].equals("-E"))
      {
        k++;
        localObject1 = paramArrayOfString[k];
        try
        {
          int m = Integer.parseInt((String)localObject1);
          if (m >= 0) {
            j = m;
          }
        }
        catch (Exception localException2) {}
      }
      else
      {
        str1 = paramArrayOfString[k];
      }
    }
    if (str1 == null)
    {
      System.out.println("Usage: org.apache.xml.resolver.apps.xparse [opts] xmlfile");
      System.exit(1);
    }
    ResolvingParser.validating = bool2;
    ResolvingParser.namespaceAware = bool1;
    Object localObject1 = new ResolvingParser();
    Catalog localCatalog = ((ResolvingParser)localObject1).getCatalog();
    for (int n = 0; n < localVector.size(); n++)
    {
      localObject2 = (String)localVector.elementAt(n);
      localCatalog.parseCatalog((String)localObject2);
    }
    Object localObject2 = new XParseError(bool4, bool3);
    ((XParseError)localObject2).setMaxMessages(j);
    ((ResolvingParser)localObject1).setErrorHandler((ErrorHandler)localObject2);
    String str2 = bool2 ? "validating" : "well-formed";
    String str3 = bool1 ? "namespace-aware" : "namespace-ignorant";
    if (j > 0) {
      System.out.println("Attempting " + str2 + ", " + str3 + " parse");
    }
    Date localDate1 = new Date();
    try
    {
      ((ResolvingParser)localObject1).parse(str1);
    }
    catch (SAXException localSAXException)
    {
      System.out.println("SAX Exception: " + localSAXException);
    }
    catch (Exception localException3)
    {
      localException3.printStackTrace();
    }
    Date localDate2 = new Date();
    long l1 = localDate2.getTime() - localDate1.getTime();
    long l2 = 0L;
    long l3 = 0L;
    long l4 = 0L;
    if (l1 > 1000L)
    {
      l2 = l1 / 1000L;
      l1 %= 1000L;
    }
    if (l2 > 60L)
    {
      l3 = l2 / 60L;
      l2 %= 60L;
    }
    if (l3 > 60L)
    {
      l4 = l3 / 60L;
      l3 %= 60L;
    }
    if (j > 0)
    {
      System.out.print("Parse ");
      if (((XParseError)localObject2).getFatalCount() > 0)
      {
        System.out.print("failed ");
      }
      else
      {
        System.out.print("succeeded ");
        System.out.print("(");
        if (l4 > 0L) {
          System.out.print(l4 + ":");
        }
        if ((l4 > 0L) || (l3 > 0L)) {
          System.out.print(l3 + ":");
        }
        System.out.print(l2 + "." + l1);
        System.out.print(") ");
      }
      System.out.print("with ");
      int i1 = ((XParseError)localObject2).getErrorCount();
      int i2 = ((XParseError)localObject2).getWarningCount();
      if (i1 > 0)
      {
        System.out.print(i1 + " error");
        System.out.print(i1 > 1 ? "s" : "");
        System.out.print(" and ");
      }
      else
      {
        System.out.print("no errors and ");
      }
      if (i2 > 0)
      {
        System.out.print(i2 + " warning");
        System.out.print(i2 > 1 ? "s" : "");
        System.out.print(".");
      }
      else
      {
        System.out.print("no warnings.");
      }
      System.out.println("");
    }
    if (((XParseError)localObject2).getErrorCount() > 0) {
      System.exit(1);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\apps\xparse.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */