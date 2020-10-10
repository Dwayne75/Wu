package com.sun.javaws.exceptions;

import com.sun.deploy.resources.ResourceManager;
import java.net.URL;

public class JARSigningException
  extends DownloadException
{
  private int _code;
  private String _missingEntry;
  public static final int MULTIPLE_CERTIFICATES = 0;
  public static final int MULTIPLE_SIGNERS = 1;
  public static final int BAD_SIGNING = 2;
  public static final int UNSIGNED_FILE = 3;
  public static final int MISSING_ENTRY = 4;
  
  public JARSigningException(URL paramURL, String paramString, int paramInt)
  {
    super(null, paramURL, paramString, null);
    this._code = paramInt;
  }
  
  public JARSigningException(URL paramURL, String paramString1, int paramInt, String paramString2)
  {
    super(null, paramURL, paramString1, null);
    this._code = paramInt;
    this._missingEntry = paramString2;
  }
  
  public JARSigningException(URL paramURL, String paramString, int paramInt, Exception paramException)
  {
    super(null, paramURL, paramString, paramException);
    this._code = paramInt;
  }
  
  public String getRealMessage()
  {
    switch (this._code)
    {
    case 0: 
      return ResourceManager.getString("launch.error.jarsigning-multicerts", getResourceString());
    case 1: 
      return ResourceManager.getString("launch.error.jarsigning-multisigners", getResourceString());
    case 2: 
      return ResourceManager.getString("launch.error.jarsigning-badsigning", getResourceString());
    case 3: 
      return ResourceManager.getString("launch.error.jarsigning-unsignedfile", getResourceString());
    case 4: 
      return ResourceManager.getString("launch.error.jarsigning-missingentry", getResourceString()) + "\n" + ResourceManager.getString("launch.error.jarsigning-missingentryname", this._missingEntry);
    }
    return "<error>";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\exceptions\JARSigningException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */