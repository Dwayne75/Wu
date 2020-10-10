package org.apache.xml.resolver;

import java.util.Hashtable;
import java.util.Vector;

public class CatalogEntry
{
  protected static int nextEntry = 0;
  protected static Hashtable entryTypes = new Hashtable();
  protected static Vector entryArgs = new Vector();
  protected int entryType = 0;
  protected Vector args = null;
  
  public static int addEntryType(String paramString, int paramInt)
  {
    entryTypes.put(paramString, new Integer(nextEntry));
    entryArgs.add(nextEntry, new Integer(paramInt));
    nextEntry += 1;
    return nextEntry - 1;
  }
  
  public static int getEntryType(String paramString)
    throws CatalogException
  {
    if (!entryTypes.containsKey(paramString)) {
      throw new CatalogException(3);
    }
    Integer localInteger = (Integer)entryTypes.get(paramString);
    if (localInteger == null) {
      throw new CatalogException(3);
    }
    return localInteger.intValue();
  }
  
  public static int getEntryArgCount(String paramString)
    throws CatalogException
  {
    return getEntryArgCount(getEntryType(paramString));
  }
  
  public static int getEntryArgCount(int paramInt)
    throws CatalogException
  {
    try
    {
      Integer localInteger = (Integer)entryArgs.get(paramInt);
      return localInteger.intValue();
    }
    catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
    {
      throw new CatalogException(3);
    }
  }
  
  public CatalogEntry() {}
  
  public CatalogEntry(String paramString, Vector paramVector)
    throws CatalogException
  {
    Integer localInteger1 = (Integer)entryTypes.get(paramString);
    if (localInteger1 == null) {
      throw new CatalogException(3);
    }
    int i = localInteger1.intValue();
    try
    {
      Integer localInteger2 = (Integer)entryArgs.get(i);
      if (localInteger2.intValue() != paramVector.size()) {
        throw new CatalogException(2);
      }
    }
    catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
    {
      throw new CatalogException(3);
    }
    this.entryType = i;
    this.args = paramVector;
  }
  
  public CatalogEntry(int paramInt, Vector paramVector)
    throws CatalogException
  {
    try
    {
      Integer localInteger = (Integer)entryArgs.get(paramInt);
      if (localInteger.intValue() != paramVector.size()) {
        throw new CatalogException(2);
      }
    }
    catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
    {
      throw new CatalogException(3);
    }
    this.entryType = paramInt;
    this.args = paramVector;
  }
  
  public int getEntryType()
  {
    return this.entryType;
  }
  
  public String getEntryArg(int paramInt)
  {
    try
    {
      String str = (String)this.args.get(paramInt);
      return str;
    }
    catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException) {}
    return null;
  }
  
  public void setEntryArg(int paramInt, String paramString)
    throws ArrayIndexOutOfBoundsException
  {
    this.args.set(paramInt, paramString);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\CatalogEntry.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */