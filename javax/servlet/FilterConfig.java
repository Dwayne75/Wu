package javax.servlet;

import java.util.Enumeration;

public abstract interface FilterConfig
{
  public abstract String getFilterName();
  
  public abstract ServletContext getServletContext();
  
  public abstract String getInitParameter(String paramString);
  
  public abstract Enumeration<String> getInitParameterNames();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\FilterConfig.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */