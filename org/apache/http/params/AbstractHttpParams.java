package org.apache.http.params;

import java.util.Set;

public abstract class AbstractHttpParams
  implements HttpParams, HttpParamsNames
{
  public long getLongParameter(String name, long defaultValue)
  {
    Object param = getParameter(name);
    if (param == null) {
      return defaultValue;
    }
    return ((Long)param).longValue();
  }
  
  public HttpParams setLongParameter(String name, long value)
  {
    setParameter(name, new Long(value));
    return this;
  }
  
  public int getIntParameter(String name, int defaultValue)
  {
    Object param = getParameter(name);
    if (param == null) {
      return defaultValue;
    }
    return ((Integer)param).intValue();
  }
  
  public HttpParams setIntParameter(String name, int value)
  {
    setParameter(name, new Integer(value));
    return this;
  }
  
  public double getDoubleParameter(String name, double defaultValue)
  {
    Object param = getParameter(name);
    if (param == null) {
      return defaultValue;
    }
    return ((Double)param).doubleValue();
  }
  
  public HttpParams setDoubleParameter(String name, double value)
  {
    setParameter(name, new Double(value));
    return this;
  }
  
  public boolean getBooleanParameter(String name, boolean defaultValue)
  {
    Object param = getParameter(name);
    if (param == null) {
      return defaultValue;
    }
    return ((Boolean)param).booleanValue();
  }
  
  public HttpParams setBooleanParameter(String name, boolean value)
  {
    setParameter(name, value ? Boolean.TRUE : Boolean.FALSE);
    return this;
  }
  
  public boolean isParameterTrue(String name)
  {
    return getBooleanParameter(name, false);
  }
  
  public boolean isParameterFalse(String name)
  {
    return !getBooleanParameter(name, false);
  }
  
  public Set<String> getNames()
  {
    throw new UnsupportedOperationException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\params\AbstractHttpParams.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */