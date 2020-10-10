package org.apache.xml.resolver;

public class CatalogException
  extends Exception
{
  public static final int WRAPPER = 1;
  public static final int INVALID_ENTRY = 2;
  public static final int INVALID_ENTRY_TYPE = 3;
  public static final int NO_XML_PARSER = 4;
  public static final int UNKNOWN_FORMAT = 5;
  public static final int UNPARSEABLE = 6;
  public static final int PARSE_FAILED = 7;
  private Exception exception = null;
  private int exceptionType = 0;
  
  public CatalogException(int paramInt, String paramString)
  {
    super(paramString);
    this.exceptionType = paramInt;
    this.exception = null;
  }
  
  public CatalogException(int paramInt)
  {
    super("Catalog Exception " + paramInt);
    this.exceptionType = paramInt;
    this.exception = null;
  }
  
  public CatalogException(Exception paramException)
  {
    this.exceptionType = 1;
    this.exception = paramException;
  }
  
  public CatalogException(String paramString, Exception paramException)
  {
    super(paramString);
    this.exceptionType = 1;
    this.exception = paramException;
  }
  
  public String getMessage()
  {
    String str = super.getMessage();
    if ((str == null) && (this.exception != null)) {
      return this.exception.getMessage();
    }
    return str;
  }
  
  public Exception getException()
  {
    return this.exception;
  }
  
  public int getExceptionType()
  {
    return this.exceptionType;
  }
  
  public String toString()
  {
    if (this.exception != null) {
      return this.exception.toString();
    }
    return super.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\CatalogException.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */