package org.fourthline.cling.model.meta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.fourthline.cling.model.Validatable;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.types.BinHexDatatype;
import org.seamless.util.MimeType;
import org.seamless.util.URIUtil;
import org.seamless.util.io.IO;

public class Icon
  implements Validatable
{
  private static final Logger log = Logger.getLogger(StateVariable.class.getName());
  private final MimeType mimeType;
  private final int width;
  private final int height;
  private final int depth;
  private final URI uri;
  private final byte[] data;
  private Device device;
  
  public Icon(String mimeType, int width, int height, int depth, URI uri)
  {
    this((mimeType != null) && (mimeType.length() > 0) ? MimeType.valueOf(mimeType) : null, width, height, depth, uri, null);
  }
  
  public Icon(String mimeType, int width, int height, int depth, URL url)
    throws IOException
  {
    this(mimeType, width, height, depth, new File(URIUtil.toURI(url)));
  }
  
  public Icon(String mimeType, int width, int height, int depth, File file)
    throws IOException
  {
    this(mimeType, width, height, depth, file.getName(), IO.readBytes(file));
  }
  
  public Icon(String mimeType, int width, int height, int depth, String uniqueName, InputStream is)
    throws IOException
  {
    this(mimeType, width, height, depth, uniqueName, IO.readBytes(is));
  }
  
  public Icon(String mimeType, int width, int height, int depth, String uniqueName, byte[] data)
  {
    this((mimeType != null) && (mimeType.length() > 0) ? MimeType.valueOf(mimeType) : null, width, height, depth, URI.create(uniqueName), data);
  }
  
  public Icon(String mimeType, int width, int height, int depth, String uniqueName, String binHexEncoded)
  {
    this(mimeType, width, height, depth, uniqueName, 
    
      !binHexEncoded.equals("") ? new BinHexDatatype().valueOf(binHexEncoded) : null);
  }
  
  protected Icon(MimeType mimeType, int width, int height, int depth, URI uri, byte[] data)
  {
    this.mimeType = mimeType;
    this.width = width;
    this.height = height;
    this.depth = depth;
    this.uri = uri;
    this.data = data;
  }
  
  public MimeType getMimeType()
  {
    return this.mimeType;
  }
  
  public int getWidth()
  {
    return this.width;
  }
  
  public int getHeight()
  {
    return this.height;
  }
  
  public int getDepth()
  {
    return this.depth;
  }
  
  public URI getUri()
  {
    return this.uri;
  }
  
  public byte[] getData()
  {
    return this.data;
  }
  
  public Device getDevice()
  {
    return this.device;
  }
  
  void setDevice(Device device)
  {
    if (this.device != null) {
      throw new IllegalStateException("Final value has been set already, model is immutable");
    }
    this.device = device;
  }
  
  public List<ValidationError> validate()
  {
    List<ValidationError> errors = new ArrayList();
    if (getMimeType() == null)
    {
      log.warning("UPnP specification violation of: " + getDevice());
      log.warning("Invalid icon, missing mime type: " + this);
    }
    if (getWidth() == 0)
    {
      log.warning("UPnP specification violation of: " + getDevice());
      log.warning("Invalid icon, missing width: " + this);
    }
    if (getHeight() == 0)
    {
      log.warning("UPnP specification violation of: " + getDevice());
      log.warning("Invalid icon, missing height: " + this);
    }
    if (getDepth() == 0)
    {
      log.warning("UPnP specification violation of: " + getDevice());
      log.warning("Invalid icon, missing bitmap depth: " + this);
    }
    if (getUri() == null) {
      errors.add(new ValidationError(
        getClass(), "uri", "URL is required"));
    } else {
      try
      {
        URL testURI = getUri().toURL();
        if (testURI == null) {
          throw new MalformedURLException();
        }
      }
      catch (MalformedURLException ex)
      {
        errors.add(new ValidationError(
          getClass(), "uri", "URL must be valid: " + ex
          
          .getMessage()));
      }
      catch (IllegalArgumentException localIllegalArgumentException) {}
    }
    return errors;
  }
  
  public Icon deepCopy()
  {
    return new Icon(getMimeType(), getWidth(), getHeight(), getDepth(), getUri(), getData());
  }
  
  public String toString()
  {
    return "Icon(" + getWidth() + "x" + getHeight() + ", MIME: " + getMimeType() + ") " + getUri();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\meta\Icon.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */