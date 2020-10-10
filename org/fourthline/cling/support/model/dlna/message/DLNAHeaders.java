package org.fourthline.cling.support.model.dlna.message;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.support.model.dlna.message.header.DLNAHeader;
import org.fourthline.cling.support.model.dlna.message.header.DLNAHeader.Type;

public class DLNAHeaders
  extends UpnpHeaders
{
  private static final Logger log = Logger.getLogger(DLNAHeaders.class.getName());
  protected Map<DLNAHeader.Type, List<UpnpHeader>> parsedDLNAHeaders;
  
  public DLNAHeaders() {}
  
  public DLNAHeaders(Map<String, List<String>> headers)
  {
    super(headers);
  }
  
  public DLNAHeaders(ByteArrayInputStream inputStream)
  {
    super(inputStream);
  }
  
  protected void parseHeaders()
  {
    if (this.parsedHeaders == null) {
      super.parseHeaders();
    }
    this.parsedDLNAHeaders = new LinkedHashMap();
    log.log(Level.FINE, "Parsing all HTTP headers for known UPnP headers: {0}", Integer.valueOf(size()));
    for (Map.Entry<String, List<String>> entry : entrySet()) {
      if (entry.getKey() != null)
      {
        type = DLNAHeader.Type.getByHttpName((String)entry.getKey());
        if (type == null) {
          log.log(Level.FINE, "Ignoring non-UPNP HTTP header: {0}", entry.getKey());
        } else {
          for (String value : (List)entry.getValue())
          {
            UpnpHeader upnpHeader = DLNAHeader.newInstance(type, value);
            if ((upnpHeader == null) || (upnpHeader.getValue() == null)) {
              log.log(Level.FINE, "Ignoring known but non-parsable header (value violates the UDA specification?) '{0}': {1}", new Object[] { type.getHttpName(), value });
            } else {
              addParsedValue(type, upnpHeader);
            }
          }
        }
      }
    }
    DLNAHeader.Type type;
  }
  
  protected void addParsedValue(DLNAHeader.Type type, UpnpHeader value)
  {
    log.log(Level.FINE, "Adding parsed header: {0}", value);
    List<UpnpHeader> list = (List)this.parsedDLNAHeaders.get(type);
    if (list == null)
    {
      list = new LinkedList();
      this.parsedDLNAHeaders.put(type, list);
    }
    list.add(value);
  }
  
  public List<String> put(String key, List<String> values)
  {
    this.parsedDLNAHeaders = null;
    return super.put(key, values);
  }
  
  public void add(String key, String value)
  {
    this.parsedDLNAHeaders = null;
    super.add(key, value);
  }
  
  public List<String> remove(Object key)
  {
    this.parsedDLNAHeaders = null;
    return super.remove(key);
  }
  
  public void clear()
  {
    this.parsedDLNAHeaders = null;
    super.clear();
  }
  
  public boolean containsKey(DLNAHeader.Type type)
  {
    if (this.parsedDLNAHeaders == null) {
      parseHeaders();
    }
    return this.parsedDLNAHeaders.containsKey(type);
  }
  
  public List<UpnpHeader> get(DLNAHeader.Type type)
  {
    if (this.parsedDLNAHeaders == null) {
      parseHeaders();
    }
    return (List)this.parsedDLNAHeaders.get(type);
  }
  
  public void add(DLNAHeader.Type type, UpnpHeader value)
  {
    super.add(type.getHttpName(), value.getString());
    if (this.parsedDLNAHeaders != null) {
      addParsedValue(type, value);
    }
  }
  
  public void remove(DLNAHeader.Type type)
  {
    super.remove(type.getHttpName());
    if (this.parsedDLNAHeaders != null) {
      this.parsedDLNAHeaders.remove(type);
    }
  }
  
  public UpnpHeader[] getAsArray(DLNAHeader.Type type)
  {
    if (this.parsedDLNAHeaders == null) {
      parseHeaders();
    }
    return this.parsedDLNAHeaders.get(type) != null ? (UpnpHeader[])((List)this.parsedDLNAHeaders.get(type)).toArray(new UpnpHeader[((List)this.parsedDLNAHeaders.get(type)).size()]) : new UpnpHeader[0];
  }
  
  public UpnpHeader getFirstHeader(DLNAHeader.Type type)
  {
    return getAsArray(type).length > 0 ? getAsArray(type)[0] : null;
  }
  
  public <H extends UpnpHeader> H getFirstHeader(DLNAHeader.Type type, Class<H> subtype)
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
  
  public void log()
  {
    if (log.isLoggable(Level.FINE))
    {
      super.log();
      if ((this.parsedDLNAHeaders != null) && (this.parsedDLNAHeaders.size() > 0))
      {
        log.fine("########################## PARSED DLNA HEADERS ##########################");
        for (Map.Entry<DLNAHeader.Type, List<UpnpHeader>> entry : this.parsedDLNAHeaders.entrySet())
        {
          log.log(Level.FINE, "=== TYPE: {0}", entry.getKey());
          for (UpnpHeader upnpHeader : (List)entry.getValue()) {
            log.log(Level.FINE, "HEADER: {0}", upnpHeader);
          }
        }
      }
      log.fine("####################################################################");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\DLNAHeaders.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */