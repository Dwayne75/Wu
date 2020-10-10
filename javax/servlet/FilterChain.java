package javax.servlet;

import java.io.IOException;

public abstract interface FilterChain
{
  public abstract void doFilter(ServletRequest paramServletRequest, ServletResponse paramServletResponse)
    throws IOException, ServletException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\FilterChain.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */