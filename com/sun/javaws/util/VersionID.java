package com.sun.javaws.util;

import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import java.util.ArrayList;
import java.util.Arrays;

public class VersionID
  implements Comparable
{
  private String[] _tuple;
  private boolean _usePrefixMatch;
  private boolean _useGreaterThan;
  private boolean _isCompound;
  private VersionID _rest;
  
  public VersionID(String paramString)
  {
    this._usePrefixMatch = false;
    this._useGreaterThan = false;
    this._isCompound = false;
    if ((paramString == null) && (paramString.length() == 0))
    {
      this._tuple = new String[0];
      return;
    }
    int i = paramString.indexOf("&");
    Object localObject;
    if (i >= 0)
    {
      this._isCompound = true;
      localObject = new VersionID(paramString.substring(0, i));
      this._rest = new VersionID(paramString.substring(i + 1));
      this._tuple = ((VersionID)localObject)._tuple;
      this._usePrefixMatch = ((VersionID)localObject)._usePrefixMatch;
      this._useGreaterThan = ((VersionID)localObject)._useGreaterThan;
    }
    else
    {
      if (paramString.endsWith("+"))
      {
        this._useGreaterThan = true;
        paramString = paramString.substring(0, paramString.length() - 1);
      }
      else if (paramString.endsWith("*"))
      {
        this._usePrefixMatch = true;
        paramString = paramString.substring(0, paramString.length() - 1);
      }
      localObject = new ArrayList();
      int j = 0;
      for (int k = 0; k < paramString.length(); k++) {
        if (".-_".indexOf(paramString.charAt(k)) != -1)
        {
          if (j < k)
          {
            String str = paramString.substring(j, k);
            ((ArrayList)localObject).add(str);
          }
          j = k + 1;
        }
      }
      if (j < paramString.length()) {
        ((ArrayList)localObject).add(paramString.substring(j, paramString.length()));
      }
      this._tuple = new String[((ArrayList)localObject).size()];
      this._tuple = ((String[])((ArrayList)localObject).toArray(this._tuple));
    }
    Trace.println("Created version ID: " + this, TraceLevel.NETWORK);
  }
  
  public boolean isSimpleVersion()
  {
    return (!this._useGreaterThan) && (!this._usePrefixMatch) && (!this._isCompound);
  }
  
  public boolean match(VersionID paramVersionID)
  {
    if ((this._isCompound) && 
      (!this._rest.match(paramVersionID))) {
      return false;
    }
    return this._useGreaterThan ? paramVersionID.isGreaterThanOrEqualTuple(this) : this._usePrefixMatch ? isPrefixMatchTuple(paramVersionID) : matchTuple(paramVersionID);
  }
  
  public boolean equals(Object paramObject)
  {
    if (matchTuple(paramObject))
    {
      VersionID localVersionID = (VersionID)paramObject;
      if (((this._rest == null) || (this._rest.equals(localVersionID._rest))) && 
        (this._useGreaterThan == localVersionID._useGreaterThan) && (this._usePrefixMatch == localVersionID._usePrefixMatch)) {
        return true;
      }
    }
    return false;
  }
  
  private boolean matchTuple(Object paramObject)
  {
    if ((paramObject == null) || (!(paramObject instanceof VersionID))) {
      return false;
    }
    VersionID localVersionID = (VersionID)paramObject;
    
    String[] arrayOfString1 = normalize(this._tuple, localVersionID._tuple.length);
    String[] arrayOfString2 = normalize(localVersionID._tuple, this._tuple.length);
    for (int i = 0; i < arrayOfString1.length; i++)
    {
      Object localObject1 = getValueAsObject(arrayOfString1[i]);
      Object localObject2 = getValueAsObject(arrayOfString2[i]);
      if (!localObject1.equals(localObject2)) {
        return false;
      }
    }
    return true;
  }
  
  private Object getValueAsObject(String paramString)
  {
    if ((paramString.length() > 0) && (paramString.charAt(0) != '-')) {
      try
      {
        return Integer.valueOf(paramString);
      }
      catch (NumberFormatException localNumberFormatException) {}
    }
    return paramString;
  }
  
  public boolean isGreaterThan(VersionID paramVersionID)
  {
    return isGreaterThanOrEqualHelper(paramVersionID, false, true);
  }
  
  public boolean isGreaterThanOrEqual(VersionID paramVersionID)
  {
    return isGreaterThanOrEqualHelper(paramVersionID, true, true);
  }
  
  private boolean isGreaterThanOrEqualTuple(VersionID paramVersionID)
  {
    return isGreaterThanOrEqualHelper(paramVersionID, true, false);
  }
  
  private boolean isGreaterThanOrEqualHelper(VersionID paramVersionID, boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((paramBoolean2) && (this._isCompound) && 
      (!this._rest.isGreaterThanOrEqualHelper(paramVersionID, paramBoolean1, true))) {
      return false;
    }
    String[] arrayOfString1 = normalize(this._tuple, paramVersionID._tuple.length);
    String[] arrayOfString2 = normalize(paramVersionID._tuple, this._tuple.length);
    for (int i = 0; i < arrayOfString1.length; i++)
    {
      Object localObject1 = getValueAsObject(arrayOfString1[i]);
      Object localObject2 = getValueAsObject(arrayOfString2[i]);
      if (!localObject1.equals(localObject2))
      {
        if (((localObject1 instanceof Integer)) && ((localObject2 instanceof Integer))) {
          return ((Integer)localObject1).intValue() > ((Integer)localObject2).intValue();
        }
        String str1 = arrayOfString1[i].toString();
        String str2 = arrayOfString2[i].toString();
        return str1.compareTo(str2) > 0;
      }
    }
    return paramBoolean1;
  }
  
  private boolean isPrefixMatchTuple(VersionID paramVersionID)
  {
    String[] arrayOfString = normalize(paramVersionID._tuple, this._tuple.length);
    for (int i = 0; i < this._tuple.length; i++)
    {
      String str1 = this._tuple[i];
      String str2 = arrayOfString[i];
      if (!str1.equals(str2)) {
        return false;
      }
    }
    return true;
  }
  
  private String[] normalize(String[] paramArrayOfString, int paramInt)
  {
    if (paramArrayOfString.length < paramInt)
    {
      String[] arrayOfString = new String[paramInt];
      System.arraycopy(paramArrayOfString, 0, arrayOfString, 0, paramArrayOfString.length);
      Arrays.fill(arrayOfString, paramArrayOfString.length, arrayOfString.length, "0");
      return arrayOfString;
    }
    return paramArrayOfString;
  }
  
  public int compareTo(Object paramObject)
  {
    if ((paramObject == null) || (!(paramObject instanceof VersionID))) {
      return -1;
    }
    VersionID localVersionID = (VersionID)paramObject;
    return isGreaterThanOrEqual(localVersionID) ? 1 : equals(localVersionID) ? 0 : -1;
  }
  
  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i < this._tuple.length - 1; i++)
    {
      localStringBuffer.append(this._tuple[i]);
      localStringBuffer.append('.');
    }
    if (this._tuple.length > 0) {
      localStringBuffer.append(this._tuple[(this._tuple.length - 1)]);
    }
    if (this._useGreaterThan) {
      localStringBuffer.append('+');
    }
    if (this._usePrefixMatch) {
      localStringBuffer.append('*');
    }
    if (this._isCompound)
    {
      localStringBuffer.append("&");localStringBuffer.append(this._rest);
    }
    return localStringBuffer.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\util\VersionID.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */