package javax.mail.search;

import java.util.Locale;
import javax.mail.Message;

public final class HeaderTerm
  extends StringTerm
{
  protected String headerName;
  private static final long serialVersionUID = 8342514650333389122L;
  
  public HeaderTerm(String headerName, String pattern)
  {
    super(pattern);
    this.headerName = headerName;
  }
  
  public String getHeaderName()
  {
    return this.headerName;
  }
  
  public boolean match(Message msg)
  {
    String[] headers;
    try
    {
      headers = msg.getHeader(this.headerName);
    }
    catch (Exception e)
    {
      return false;
    }
    if (headers == null) {
      return false;
    }
    for (int i = 0; i < headers.length; i++) {
      if (super.match(headers[i])) {
        return true;
      }
    }
    return false;
  }
  
  public boolean equals(Object obj)
  {
    if (!(obj instanceof HeaderTerm)) {
      return false;
    }
    HeaderTerm ht = (HeaderTerm)obj;
    
    return (ht.headerName.equalsIgnoreCase(this.headerName)) && (super.equals(ht));
  }
  
  public int hashCode()
  {
    return this.headerName.toLowerCase(Locale.ENGLISH).hashCode() + super.hashCode();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\search\HeaderTerm.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */