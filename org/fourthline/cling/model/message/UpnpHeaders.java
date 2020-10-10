package org.fourthline.cling.model.message;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.message.header.UpnpHeader.Type;
import org.seamless.http.Headers;

public class UpnpHeaders
  extends Headers
{
  private static final Logger log = Logger.getLogger(UpnpHeaders.class.getName());
  protected Map<UpnpHeader.Type, List<UpnpHeader>> parsedHeaders;
  
  public UpnpHeaders() {}
  
  public UpnpHeaders(Map<String, List<String>> headers)
  {
    super(headers);
  }
  
  public UpnpHeaders(ByteArrayInputStream inputStream)
  {
    super(inputStream);
  }
  
  public UpnpHeaders(boolean normalizeHeaders)
  {
    super(normalizeHeaders);
  }
  
  protected void parseHeaders()
  {
    this.parsedHeaders = new LinkedHashMap();
    if (log.isLoggable(Level.FINE)) {
      log.fine("Parsing all HTTP headers for known UPnP headers: " + size());
    }
    for (Map.Entry<String, List<String>> entry : entrySet()) {
      if (entry.getKey() != null)
      {
        type = UpnpHeader.Type.getByHttpName((String)entry.getKey());
        if (type == null)
        {
          if (log.isLoggable(Level.FINE)) {
            log.fine("Ignoring non-UPNP HTTP header: " + (String)entry.getKey());
          }
        }
        else {
          for (String value : (List)entry.getValue())
          {
            UpnpHeader upnpHeader = UpnpHeader.newInstance(type, value);
            if ((upnpHeader == null) || (upnpHeader.getValue() == null))
            {
              if (log.isLoggable(Level.FINE)) {
                log.fine("Ignoring known but irrelevant header (value violates the UDA specification?) '" + type
                
                  .getHttpName() + "': " + value);
              }
            }
            else {
              addParsedValue(type, upnpHeader);
            }
          }
        }
      }
    }
    UpnpHeader.Type type;
  }
  
  protected void addParsedValue(UpnpHeader.Type type, UpnpHeader value)
  {
    if (log.isLoggable(Level.FINE)) {
      log.fine("Adding parsed header: " + value);
    }
    List<UpnpHeader> list = (List)this.parsedHeaders.get(type);
    if (list == null)
    {
      list = new LinkedList();
      this.parsedHeaders.put(type, list);
    }
    list.add(value);
  }
  
  public List<String> put(String key, List<String> values)
  {
    this.parsedHeaders = null;
    return super.put(key, values);
  }
  
  public void add(String key, String value)
  {
    this.parsedHeaders = null;
    super.add(key, value);
  }
  
  public List<String> remove(Object key)
  {
    this.parsedHeaders = null;
    return super.remove(key);
  }
  
  public void clear()
  {
    this.parsedHeaders = null;
    super.clear();
  }
  
  public boolean containsKey(UpnpHeader.Type type)
  {
    if (this.parsedHeaders == null) {
      parseHeaders();
    }
    return this.parsedHeaders.containsKey(type);
  }
  
  public List<UpnpHeader> get(UpnpHeader.Type type)
  {
    if (this.parsedHeaders == null) {
      parseHeaders();
    }
    return (List)this.parsedHeaders.get(type);
  }
  
  public void add(UpnpHeader.Type type, UpnpHeader value)
  {
    super.add(type.getHttpName(), value.getString());
    if (this.parsedHeaders != null) {
      addParsedValue(type, value);
    }
  }
  
  public void remove(UpnpHeader.Type type)
  {
    super.remove(type.getHttpName());
    if (this.parsedHeaders != null) {
      this.parsedHeaders.remove(type);
    }
  }
  
  public UpnpHeader[] getAsArray(UpnpHeader.Type type)
  {
    if (this.parsedHeaders == null) {
      parseHeaders();
    }
    return this.parsedHeaders.get(type) != null ? (UpnpHeader[])((List)this.parsedHeaders.get(type)).toArray(new UpnpHeader[((List)this.parsedHeaders.get(type)).size()]) : new UpnpHeader[0];
  }
  
  public UpnpHeader getFirstHeader(UpnpHeader.Type type)
  {
    return getAsArray(type).length > 0 ? getAsArray(type)[0] : null;
  }
  
  public <H extends UpnpHeader> H getFirstHeader(UpnpHeader.Type type, Class<H> subtype)
  {
    UpnpHeader[] headers = getAsArray(type);
    if (headers.length == 0) {
      return null;
    }
    for (UpnpHeader header : headers) {
      if (subtype.isAssignableFrom(header.getClass())) {
        return header;
      }
    }
    return null;
  }
  
  public String getFirstHeaderString(UpnpHeader.Type type)
  {
    UpnpHeader header = getFirstHeader(type);
    return header != null ? header.getString() : null;
  }
  
  public void log()
  {
    if (log.isLoggable(Level.FINE))
    {
      log.fine("############################ RAW HEADERS ###########################");
      for (Map.Entry<String, List<String>> entry : entrySet())
      {
        log.fine("=== NAME : " + (String)entry.getKey());
        for (String v : (List)entry.getValue()) {
          log.fine("VALUE: " + v);
        }
      }
      if ((this.parsedHeaders != null) && (this.parsedHeaders.size() > 0))
      {
        log.fine("########################## PARSED HEADERS ##########################");
        for (Map.Entry<UpnpHeader.Type, List<UpnpHeader>> entry : this.parsedHeaders.entrySet())
        {
          log.fine("=== TYPE: " + entry.getKey());
          for (UpnpHeader upnpHeader : (List)entry.getValue()) {
            log.fine("HEADER: " + upnpHeader);
          }
        }
      }
      log.fine("####################################################################");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\UpnpHeaders.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */