package org.apache.xml.resolver.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.helpers.Debug;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class CatalogResolver
  implements EntityResolver, URIResolver
{
  public boolean namespaceAware = true;
  public boolean validating = false;
  private static Catalog staticCatalog = null;
  private Catalog catalog = null;
  
  public CatalogResolver()
  {
    initializeCatalogs(false);
  }
  
  public CatalogResolver(boolean paramBoolean)
  {
    initializeCatalogs(paramBoolean);
  }
  
  private void initializeCatalogs(boolean paramBoolean)
  {
    this.catalog = staticCatalog;
    if ((paramBoolean) || (this.catalog == null)) {
      try
      {
        String str = CatalogManager.catalogClassName();
        if (str == null) {
          this.catalog = new Catalog();
        } else {
          try
          {
            this.catalog = ((Catalog)Class.forName(str).newInstance());
          }
          catch (ClassNotFoundException localClassNotFoundException)
          {
            Debug.message(1, "Catalog class named '" + str + "' could not be found. Using default.");
            this.catalog = new Catalog();
          }
          catch (ClassCastException localClassCastException)
          {
            Debug.message(1, "Class named '" + str + "' is not a Catalog. Using default.");
            this.catalog = new Catalog();
          }
        }
        this.catalog.setupReaders();
        if (!paramBoolean) {
          this.catalog.loadSystemCatalogs();
        }
      }
      catch (Exception localException)
      {
        localException.printStackTrace();
      }
    }
    if ((!paramBoolean) && (this.catalog != null) && (CatalogManager.staticCatalog())) {
      staticCatalog = this.catalog;
    }
  }
  
  public Catalog getCatalog()
  {
    return this.catalog;
  }
  
  public String getResolvedEntity(String paramString1, String paramString2)
  {
    String str = null;
    if (this.catalog == null)
    {
      Debug.message(1, "Catalog resolution attempted with null catalog; ignored");
      return null;
    }
    if (paramString2 != null) {
      try
      {
        str = this.catalog.resolveSystem(paramString2);
      }
      catch (MalformedURLException localMalformedURLException1)
      {
        Debug.message(1, "Malformed URL exception trying to resolve", paramString1);
        str = null;
      }
      catch (IOException localIOException1)
      {
        Debug.message(1, "I/O exception trying to resolve", paramString1);
        str = null;
      }
    }
    if (str == null)
    {
      if (paramString1 != null) {
        try
        {
          str = this.catalog.resolvePublic(paramString1, paramString2);
        }
        catch (MalformedURLException localMalformedURLException2)
        {
          Debug.message(1, "Malformed URL exception trying to resolve", paramString1);
        }
        catch (IOException localIOException2)
        {
          Debug.message(1, "I/O exception trying to resolve", paramString1);
        }
      }
      if (str != null) {
        Debug.message(2, "Resolved public", paramString1, str);
      }
    }
    else
    {
      Debug.message(2, "Resolved system", paramString2, str);
    }
    return str;
  }
  
  public InputSource resolveEntity(String paramString1, String paramString2)
  {
    String str = getResolvedEntity(paramString1, paramString2);
    if (str != null) {
      try
      {
        InputSource localInputSource = new InputSource(str);
        localInputSource.setPublicId(paramString1);
        URL localURL = new URL(str);
        InputStream localInputStream = localURL.openStream();
        localInputSource.setByteStream(localInputStream);
        return localInputSource;
      }
      catch (Exception localException)
      {
        Debug.message(1, "Failed to create InputSource", str);
        return null;
      }
    }
    return null;
  }
  
  public Source resolve(String paramString1, String paramString2)
    throws TransformerException
  {
    String str1 = paramString1;
    String str2 = null;
    int i = paramString1.indexOf("#");
    if (i >= 0)
    {
      str1 = paramString1.substring(0, i);
      str2 = paramString1.substring(i + 1);
    }
    String str3 = null;
    try
    {
      str3 = this.catalog.resolveURI(paramString1);
    }
    catch (Exception localException) {}
    if (str3 == null) {
      try
      {
        URL localURL = null;
        if (paramString2 == null)
        {
          localURL = new URL(str1);
          str3 = localURL.toString();
        }
        else
        {
          localObject = new URL(paramString2);
          localURL = paramString1.length() == 0 ? localObject : new URL((URL)localObject, str1);
          str3 = localURL.toString();
        }
      }
      catch (MalformedURLException localMalformedURLException)
      {
        Object localObject = makeAbsolute(paramString2);
        if (!((String)localObject).equals(paramString2)) {
          return resolve(paramString1, (String)localObject);
        }
        throw new TransformerException("Malformed URL " + paramString1 + "(base " + paramString2 + ")", localMalformedURLException);
      }
    }
    Debug.message(2, "Resolved URI", paramString1, str3);
    SAXSource localSAXSource = new SAXSource();
    localSAXSource.setInputSource(new InputSource(str3));
    return localSAXSource;
  }
  
  private String makeAbsolute(String paramString)
  {
    if (paramString == null) {
      paramString = "";
    }
    try
    {
      URL localURL1 = new URL(paramString);
      return localURL1.toString();
    }
    catch (MalformedURLException localMalformedURLException1)
    {
      String str1 = System.getProperty("user.dir");
      String str2 = "";
      if (str1.endsWith("/")) {
        str2 = "file://" + str1 + paramString;
      } else {
        str2 = "file://" + str1 + "/" + paramString;
      }
      try
      {
        URL localURL2 = new URL(str2);
        return localURL2.toString();
      }
      catch (MalformedURLException localMalformedURLException2) {}
    }
    return paramString;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\tools\CatalogResolver.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */