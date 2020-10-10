package com.sun.dtdparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class Resolver
  implements EntityResolver
{
  private boolean ignoringMIME;
  private Hashtable id2uri;
  private Hashtable id2resource;
  private Hashtable id2loader;
  private static final String[] types = { "application/xml", "text/xml", "text/plain", "text/html", "application/x-netcdf", "content/unknown" };
  
  public static InputSource createInputSource(String contentType, InputStream stream, boolean checkType, String scheme)
    throws IOException
  {
    String charset = null;
    if (contentType != null)
    {
      contentType = contentType.toLowerCase();
      int index = contentType.indexOf(';');
      if (index != -1)
      {
        String attributes = contentType.substring(index + 1);
        contentType = contentType.substring(0, index);
        
        index = attributes.indexOf("charset");
        if (index != -1)
        {
          attributes = attributes.substring(index + 7);
          if ((index = attributes.indexOf(';')) != -1) {
            attributes = attributes.substring(0, index);
          }
          if ((index = attributes.indexOf('=')) != -1)
          {
            attributes = attributes.substring(index + 1);
            if ((index = attributes.indexOf('(')) != -1) {
              attributes = attributes.substring(0, index);
            }
            if ((index = attributes.indexOf('"')) != -1)
            {
              attributes = attributes.substring(index + 1);
              attributes = attributes.substring(0, attributes.indexOf('"'));
            }
            charset = attributes.trim();
          }
        }
      }
      if (checkType)
      {
        boolean isOK = false;
        for (int i = 0; i < types.length; i++) {
          if (types[i].equals(contentType))
          {
            isOK = true;
            break;
          }
        }
        if (!isOK) {
          throw new IOException("Not XML: " + contentType);
        }
      }
      if (charset == null)
      {
        contentType = contentType.trim();
        if ((contentType.startsWith("text/")) && 
          (!"file".equalsIgnoreCase(scheme))) {
          charset = "US-ASCII";
        }
      }
    }
    InputSource retval = new InputSource(XmlReader.createReader(stream, charset));
    retval.setByteStream(stream);
    retval.setEncoding(charset);
    return retval;
  }
  
  public static InputSource createInputSource(URL uri, boolean checkType)
    throws IOException
  {
    URLConnection conn = uri.openConnection();
    InputSource retval;
    InputSource retval;
    if (checkType)
    {
      String contentType = conn.getContentType();
      retval = createInputSource(contentType, conn.getInputStream(), false, uri.getProtocol());
    }
    else
    {
      retval = new InputSource(XmlReader.createReader(conn.getInputStream()));
    }
    retval.setSystemId(conn.getURL().toString());
    return retval;
  }
  
  public static InputSource createInputSource(File file)
    throws IOException
  {
    InputSource retval = new InputSource(XmlReader.createReader(new FileInputStream(file)));
    
    String path = file.getAbsolutePath();
    if (File.separatorChar != '/') {
      path = path.replace(File.separatorChar, '/');
    }
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    if ((!path.endsWith("/")) && (file.isDirectory())) {
      path = path + "/";
    }
    retval.setSystemId("file:" + path);
    return retval;
  }
  
  public InputSource resolveEntity(String name, String uri)
    throws IOException
  {
    String mappedURI = name2uri(name);
    InputStream stream;
    InputSource retval;
    InputSource retval;
    if ((mappedURI == null) && ((stream = mapResource(name)) != null))
    {
      uri = "java:resource:" + (String)this.id2resource.get(name);
      retval = new InputSource(XmlReader.createReader(stream));
    }
    else
    {
      if (mappedURI != null) {
        uri = mappedURI;
      } else if (uri == null) {
        return null;
      }
      URL url = new URL(uri);
      URLConnection conn = url.openConnection();
      uri = conn.getURL().toString();
      InputSource retval;
      if (this.ignoringMIME)
      {
        retval = new InputSource(XmlReader.createReader(conn.getInputStream()));
      }
      else
      {
        String contentType = conn.getContentType();
        retval = createInputSource(contentType, conn.getInputStream(), false, url.getProtocol());
      }
    }
    retval.setSystemId(uri);
    retval.setPublicId(name);
    return retval;
  }
  
  public boolean isIgnoringMIME()
  {
    return this.ignoringMIME;
  }
  
  public void setIgnoringMIME(boolean value)
  {
    this.ignoringMIME = value;
  }
  
  private String name2uri(String publicId)
  {
    if ((publicId == null) || (this.id2uri == null)) {
      return null;
    }
    return (String)this.id2uri.get(publicId);
  }
  
  public void registerCatalogEntry(String publicId, String uri)
  {
    if (this.id2uri == null) {
      this.id2uri = new Hashtable(17);
    }
    this.id2uri.put(publicId, uri);
  }
  
  private InputStream mapResource(String publicId)
  {
    if ((publicId == null) || (this.id2resource == null)) {
      return null;
    }
    String resourceName = (String)this.id2resource.get(publicId);
    ClassLoader loader = null;
    if (resourceName == null) {
      return null;
    }
    if (this.id2loader != null) {
      loader = (ClassLoader)this.id2loader.get(publicId);
    }
    if (loader == null) {
      return ClassLoader.getSystemResourceAsStream(resourceName);
    }
    return loader.getResourceAsStream(resourceName);
  }
  
  public void registerCatalogEntry(String publicId, String resourceName, ClassLoader loader)
  {
    if (this.id2resource == null) {
      this.id2resource = new Hashtable(17);
    }
    this.id2resource.put(publicId, resourceName);
    if (loader != null)
    {
      if (this.id2loader == null) {
        this.id2loader = new Hashtable(17);
      }
      this.id2loader.put(publicId, loader);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\dtdparser\Resolver.class
 * Java compiler version: 3 (47.0)
 * JD-Core Version:       0.7.1
 */