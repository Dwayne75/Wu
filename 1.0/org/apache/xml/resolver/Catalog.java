package org.apache.xml.resolver;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.xml.parsers.SAXParserFactory;
import org.apache.xml.resolver.helpers.Debug;
import org.apache.xml.resolver.helpers.PublicId;
import org.apache.xml.resolver.readers.CatalogReader;
import org.apache.xml.resolver.readers.SAXCatalogReader;
import org.apache.xml.resolver.readers.TR9401CatalogReader;

public class Catalog
{
  public static final int BASE = CatalogEntry.addEntryType("BASE", 1);
  public static final int CATALOG = CatalogEntry.addEntryType("CATALOG", 1);
  public static final int DOCUMENT = CatalogEntry.addEntryType("DOCUMENT", 1);
  public static final int OVERRIDE = CatalogEntry.addEntryType("OVERRIDE", 1);
  public static final int SGMLDECL = CatalogEntry.addEntryType("SGMLDECL", 1);
  public static final int DELEGATE_PUBLIC = CatalogEntry.addEntryType("DELEGATE_PUBLIC", 2);
  public static final int DELEGATE_SYSTEM = CatalogEntry.addEntryType("DELEGATE_SYSTEM", 2);
  public static final int DELEGATE_URI = CatalogEntry.addEntryType("DELEGATE_URI", 2);
  public static final int DOCTYPE = CatalogEntry.addEntryType("DOCTYPE", 2);
  public static final int DTDDECL = CatalogEntry.addEntryType("DTDDECL", 2);
  public static final int ENTITY = CatalogEntry.addEntryType("ENTITY", 2);
  public static final int LINKTYPE = CatalogEntry.addEntryType("LINKTYPE", 2);
  public static final int NOTATION = CatalogEntry.addEntryType("NOTATION", 2);
  public static final int PUBLIC = CatalogEntry.addEntryType("PUBLIC", 2);
  public static final int SYSTEM = CatalogEntry.addEntryType("SYSTEM", 2);
  public static final int URI = CatalogEntry.addEntryType("URI", 2);
  public static final int REWRITE_SYSTEM = CatalogEntry.addEntryType("REWRITE_SYSTEM", 2);
  public static final int REWRITE_URI = CatalogEntry.addEntryType("REWRITE_URI", 2);
  protected URL base;
  protected URL catalogCwd;
  protected Vector catalogEntries = new Vector();
  protected boolean default_override = true;
  protected Vector catalogFiles = new Vector();
  protected Vector localCatalogFiles = new Vector();
  protected Vector catalogs = new Vector();
  protected Vector localDelegate = new Vector();
  protected Hashtable readerMap = new Hashtable();
  protected Vector readerArr = new Vector();
  
  public void setupReaders()
  {
    SAXParserFactory localSAXParserFactory = SAXParserFactory.newInstance();
    localSAXParserFactory.setNamespaceAware(true);
    localSAXParserFactory.setValidating(false);
    SAXCatalogReader localSAXCatalogReader = new SAXCatalogReader(localSAXParserFactory);
    localSAXCatalogReader.setCatalogParser(null, "XMLCatalog", "org.apache.xml.resolver.readers.XCatalogReader");
    localSAXCatalogReader.setCatalogParser("urn:oasis:names:tc:entity:xmlns:xml:catalog", "catalog", "org.apache.xml.resolver.readers.OASISXMLCatalogReader");
    addReader("application/xml", localSAXCatalogReader);
    TR9401CatalogReader localTR9401CatalogReader = new TR9401CatalogReader();
    addReader("text/plain", localTR9401CatalogReader);
  }
  
  public void addReader(String paramString, CatalogReader paramCatalogReader)
  {
    Integer localInteger;
    if (this.readerMap.contains(paramString))
    {
      localInteger = (Integer)this.readerMap.get(paramString);
      this.readerArr.set(localInteger.intValue(), paramCatalogReader);
    }
    else
    {
      this.readerArr.add(paramCatalogReader);
      localInteger = new Integer(this.readerArr.size() - 1);
      this.readerMap.put(paramString, localInteger);
    }
  }
  
  protected void copyReaders(Catalog paramCatalog)
  {
    Vector localVector = new Vector(this.readerMap.size());
    for (int i = 0; i < this.readerMap.size(); i++) {
      localVector.add(null);
    }
    Enumeration localEnumeration = this.readerMap.keys();
    Object localObject;
    while (localEnumeration.hasMoreElements())
    {
      String str = (String)localEnumeration.nextElement();
      localObject = (Integer)this.readerMap.get(str);
      localVector.set(((Integer)localObject).intValue(), str);
    }
    for (int j = 0; j < localVector.size(); j++)
    {
      localObject = (String)localVector.get(j);
      Integer localInteger = (Integer)this.readerMap.get(localObject);
      paramCatalog.addReader((String)localObject, (CatalogReader)this.readerArr.get(localInteger.intValue()));
    }
  }
  
  protected Catalog newCatalog()
  {
    String str = getClass().getName();
    try
    {
      Catalog localCatalog1 = (Catalog)Class.forName(str).newInstance();
      copyReaders(localCatalog1);
      return localCatalog1;
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      Debug.message(1, "Class Not Found Exception: " + str);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      Debug.message(1, "Illegal Access Exception: " + str);
    }
    catch (InstantiationException localInstantiationException)
    {
      Debug.message(1, "Instantiation Exception: " + str);
    }
    catch (ClassCastException localClassCastException)
    {
      Debug.message(1, "Class Cast Exception: " + str);
    }
    catch (Exception localException)
    {
      Debug.message(1, "Other Exception: " + str);
    }
    Catalog localCatalog2 = new Catalog();
    copyReaders(localCatalog2);
    return localCatalog2;
  }
  
  public String getCurrentBase()
  {
    return this.base.toString();
  }
  
  public String getDefaultOverride()
  {
    if (this.default_override) {
      return "yes";
    }
    return "no";
  }
  
  public void loadSystemCatalogs()
    throws MalformedURLException, IOException
  {
    Vector localVector = CatalogManager.catalogFiles();
    if (localVector != null) {
      for (int i = 0; i < localVector.size(); i++) {
        this.catalogFiles.addElement(localVector.elementAt(i));
      }
    }
    if (this.catalogFiles.size() > 0)
    {
      String str = (String)this.catalogFiles.lastElement();
      this.catalogFiles.removeElement(str);
      parseCatalog(str);
    }
  }
  
  public synchronized void parseCatalog(String paramString)
    throws MalformedURLException, IOException
  {
    Debug.message(4, "Parse catalog: " + paramString);
    this.catalogFiles.addElement(paramString);
    parsePendingCatalogs();
  }
  
  public synchronized void parseCatalog(String paramString, InputStream paramInputStream)
    throws IOException, CatalogException
  {
    Debug.message(4, "Parse " + paramString + " catalog on input stream");
    CatalogReader localCatalogReader = null;
    if (this.readerMap.containsKey(paramString))
    {
      int i = ((Integer)this.readerMap.get(paramString)).intValue();
      localCatalogReader = (CatalogReader)this.readerArr.get(i);
    }
    if (localCatalogReader == null)
    {
      String str = "No CatalogReader for MIME type: " + paramString;
      Debug.message(2, str);
      throw new CatalogException(6, str);
    }
    localCatalogReader.readCatalog(this, paramInputStream);
    parsePendingCatalogs();
  }
  
  protected synchronized void parsePendingCatalogs()
    throws MalformedURLException, IOException
  {
    Object localObject1;
    if (!this.localCatalogFiles.isEmpty())
    {
      localObject1 = new Vector();
      Enumeration localEnumeration1 = this.localCatalogFiles.elements();
      while (localEnumeration1.hasMoreElements()) {
        ((Vector)localObject1).addElement(localEnumeration1.nextElement());
      }
      for (int i = 0; i < this.catalogFiles.size(); i++)
      {
        String str = (String)this.catalogFiles.elementAt(i);
        ((Vector)localObject1).addElement(str);
      }
      this.catalogFiles = ((Vector)localObject1);
      this.localCatalogFiles.clear();
    }
    if ((this.catalogFiles.isEmpty()) && (!this.localDelegate.isEmpty()))
    {
      localObject1 = this.localDelegate.elements();
      while (((Enumeration)localObject1).hasMoreElements()) {
        this.catalogEntries.addElement(((Enumeration)localObject1).nextElement());
      }
      this.localDelegate.clear();
    }
    while (!this.catalogFiles.isEmpty())
    {
      localObject1 = (String)this.catalogFiles.elementAt(0);
      try
      {
        this.catalogFiles.remove(0);
      }
      catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException) {}
      if ((this.catalogEntries.size() == 0) && (this.catalogs.size() == 0)) {
        try
        {
          parseCatalogFile((String)localObject1);
        }
        catch (CatalogException localCatalogException)
        {
          System.out.println("FIXME: " + localCatalogException.toString());
        }
      } else {
        this.catalogs.addElement(localObject1);
      }
      Object localObject2;
      if (!this.localCatalogFiles.isEmpty())
      {
        localObject2 = new Vector();
        Enumeration localEnumeration2 = this.localCatalogFiles.elements();
        while (localEnumeration2.hasMoreElements()) {
          ((Vector)localObject2).addElement(localEnumeration2.nextElement());
        }
        for (int j = 0; j < this.catalogFiles.size(); j++)
        {
          localObject1 = (String)this.catalogFiles.elementAt(j);
          ((Vector)localObject2).addElement(localObject1);
        }
        this.catalogFiles = ((Vector)localObject2);
        this.localCatalogFiles.clear();
      }
      if (!this.localDelegate.isEmpty())
      {
        localObject2 = this.localDelegate.elements();
        while (((Enumeration)localObject2).hasMoreElements()) {
          this.catalogEntries.addElement(((Enumeration)localObject2).nextElement());
        }
        this.localDelegate.clear();
      }
    }
    this.catalogFiles.clear();
  }
  
  protected synchronized void parseCatalogFile(String paramString)
    throws MalformedURLException, IOException, CatalogException
  {
    try
    {
      String str1 = fixSlashes(System.getProperty("user.dir"));
      this.catalogCwd = new URL("file:" + str1 + "/basename");
    }
    catch (MalformedURLException localMalformedURLException1)
    {
      String str2 = fixSlashes(System.getProperty("user.dir"));
      Debug.message(1, "Malformed URL on cwd", str2);
      this.catalogCwd = null;
    }
    try
    {
      this.base = new URL(this.catalogCwd, fixSlashes(paramString));
    }
    catch (MalformedURLException localMalformedURLException2)
    {
      try
      {
        this.base = new URL("file:" + fixSlashes(paramString));
      }
      catch (MalformedURLException localMalformedURLException3)
      {
        Debug.message(1, "Malformed URL on catalog filename", fixSlashes(paramString));
        this.base = null;
      }
    }
    Debug.message(2, "Loading catalog", paramString);
    Debug.message(4, "Default BASE", this.base.toString());
    paramString = this.base.toString();
    DataInputStream localDataInputStream = null;
    int i = 0;
    int j = 0;
    for (int k = 0; (i == 0) && (k < this.readerArr.size()); k++)
    {
      CatalogReader localCatalogReader = (CatalogReader)this.readerArr.get(k);
      try
      {
        j = 0;
        localDataInputStream = new DataInputStream(this.base.openStream());
      }
      catch (FileNotFoundException localFileNotFoundException)
      {
        j = 1;
        break;
      }
      try
      {
        localCatalogReader.readCatalog(this, localDataInputStream);
        i = 1;
      }
      catch (CatalogException localCatalogException)
      {
        if (localCatalogException.getExceptionType() != 7) {
          break label267;
        }
      }
      break;
      try
      {
        label267:
        localDataInputStream.close();
      }
      catch (IOException localIOException) {}
    }
    if (i == 0) {
      if (j != 0) {
        Debug.message(3, "Catalog does not exist", paramString);
      } else {
        Debug.message(1, "Failed to parse catalog", paramString);
      }
    }
  }
  
  public void addEntry(CatalogEntry paramCatalogEntry)
  {
    int i = paramCatalogEntry.getEntryType();
    String str;
    Object localObject;
    if (i == BASE)
    {
      str = paramCatalogEntry.getEntryArg(0);
      localObject = null;
      Debug.message(5, "BASE CUR", this.base.toString());
      Debug.message(4, "BASE STR", str);
      try
      {
        str = fixSlashes(str);
        localObject = new URL(this.base, str);
      }
      catch (MalformedURLException localMalformedURLException1)
      {
        try
        {
          localObject = new URL("file:" + str);
        }
        catch (MalformedURLException localMalformedURLException2)
        {
          Debug.message(1, "Malformed URL on base", str);
          localObject = null;
        }
      }
      if (localObject != null) {
        this.base = ((URL)localObject);
      }
      Debug.message(5, "BASE NEW", this.base.toString());
    }
    else if (i == CATALOG)
    {
      str = makeAbsolute(paramCatalogEntry.getEntryArg(0));
      Debug.message(4, "CATALOG", str);
      this.localCatalogFiles.addElement(str);
    }
    else if (i == PUBLIC)
    {
      str = PublicId.normalize(paramCatalogEntry.getEntryArg(0));
      localObject = makeAbsolute(normalizeURI(paramCatalogEntry.getEntryArg(1)));
      paramCatalogEntry.setEntryArg(0, str);
      paramCatalogEntry.setEntryArg(1, (String)localObject);
      Debug.message(4, "PUBLIC", str, (String)localObject);
      this.catalogEntries.addElement(paramCatalogEntry);
    }
    else if (i == SYSTEM)
    {
      str = normalizeURI(paramCatalogEntry.getEntryArg(0));
      localObject = makeAbsolute(normalizeURI(paramCatalogEntry.getEntryArg(1)));
      paramCatalogEntry.setEntryArg(1, (String)localObject);
      Debug.message(4, "SYSTEM", str, (String)localObject);
      this.catalogEntries.addElement(paramCatalogEntry);
    }
    else if (i == URI)
    {
      str = normalizeURI(paramCatalogEntry.getEntryArg(0));
      localObject = makeAbsolute(normalizeURI(paramCatalogEntry.getEntryArg(1)));
      paramCatalogEntry.setEntryArg(1, (String)localObject);
      Debug.message(4, "URI", str, (String)localObject);
      this.catalogEntries.addElement(paramCatalogEntry);
    }
    else if (i == DOCUMENT)
    {
      str = makeAbsolute(normalizeURI(paramCatalogEntry.getEntryArg(0)));
      paramCatalogEntry.setEntryArg(0, str);
      Debug.message(4, "DOCUMENT", str);
      this.catalogEntries.addElement(paramCatalogEntry);
    }
    else if (i == OVERRIDE)
    {
      Debug.message(4, "OVERRIDE", paramCatalogEntry.getEntryArg(0));
      this.catalogEntries.addElement(paramCatalogEntry);
    }
    else if (i == SGMLDECL)
    {
      str = makeAbsolute(normalizeURI(paramCatalogEntry.getEntryArg(0)));
      paramCatalogEntry.setEntryArg(0, str);
      Debug.message(4, "SGMLDECL", str);
      this.catalogEntries.addElement(paramCatalogEntry);
    }
    else if (i == DELEGATE_PUBLIC)
    {
      str = PublicId.normalize(paramCatalogEntry.getEntryArg(0));
      localObject = makeAbsolute(normalizeURI(paramCatalogEntry.getEntryArg(1)));
      paramCatalogEntry.setEntryArg(0, str);
      paramCatalogEntry.setEntryArg(1, (String)localObject);
      Debug.message(4, "DELEGATE_PUBLIC", str, (String)localObject);
      addDelegate(paramCatalogEntry);
    }
    else if (i == DELEGATE_SYSTEM)
    {
      str = normalizeURI(paramCatalogEntry.getEntryArg(0));
      localObject = makeAbsolute(normalizeURI(paramCatalogEntry.getEntryArg(1)));
      paramCatalogEntry.setEntryArg(0, str);
      paramCatalogEntry.setEntryArg(1, (String)localObject);
      Debug.message(4, "DELEGATE_SYSTEM", str, (String)localObject);
      addDelegate(paramCatalogEntry);
    }
    else if (i == DELEGATE_URI)
    {
      str = normalizeURI(paramCatalogEntry.getEntryArg(0));
      localObject = makeAbsolute(normalizeURI(paramCatalogEntry.getEntryArg(1)));
      paramCatalogEntry.setEntryArg(0, str);
      paramCatalogEntry.setEntryArg(1, (String)localObject);
      Debug.message(4, "DELEGATE_URI", str, (String)localObject);
      addDelegate(paramCatalogEntry);
    }
    else if (i == REWRITE_SYSTEM)
    {
      str = normalizeURI(paramCatalogEntry.getEntryArg(0));
      localObject = makeAbsolute(normalizeURI(paramCatalogEntry.getEntryArg(1)));
      paramCatalogEntry.setEntryArg(0, str);
      paramCatalogEntry.setEntryArg(1, (String)localObject);
      Debug.message(4, "REWRITE_SYSTEM", str, (String)localObject);
      this.catalogEntries.addElement(paramCatalogEntry);
    }
    else if (i == REWRITE_URI)
    {
      str = normalizeURI(paramCatalogEntry.getEntryArg(0));
      localObject = makeAbsolute(normalizeURI(paramCatalogEntry.getEntryArg(1)));
      paramCatalogEntry.setEntryArg(0, str);
      paramCatalogEntry.setEntryArg(1, (String)localObject);
      Debug.message(4, "REWRITE_URI", str, (String)localObject);
      this.catalogEntries.addElement(paramCatalogEntry);
    }
    else if (i == DOCTYPE)
    {
      str = makeAbsolute(normalizeURI(paramCatalogEntry.getEntryArg(1)));
      paramCatalogEntry.setEntryArg(1, str);
      Debug.message(4, "DOCTYPE", paramCatalogEntry.getEntryArg(0), str);
      this.catalogEntries.addElement(paramCatalogEntry);
    }
    else if (i == DTDDECL)
    {
      str = PublicId.normalize(paramCatalogEntry.getEntryArg(0));
      paramCatalogEntry.setEntryArg(0, str);
      localObject = makeAbsolute(normalizeURI(paramCatalogEntry.getEntryArg(1)));
      paramCatalogEntry.setEntryArg(1, (String)localObject);
      Debug.message(4, "DTDDECL", str, (String)localObject);
      this.catalogEntries.addElement(paramCatalogEntry);
    }
    else if (i == ENTITY)
    {
      str = makeAbsolute(normalizeURI(paramCatalogEntry.getEntryArg(1)));
      paramCatalogEntry.setEntryArg(1, str);
      Debug.message(4, "ENTITY", paramCatalogEntry.getEntryArg(0), str);
      this.catalogEntries.addElement(paramCatalogEntry);
    }
    else if (i == LINKTYPE)
    {
      str = makeAbsolute(normalizeURI(paramCatalogEntry.getEntryArg(1)));
      paramCatalogEntry.setEntryArg(1, str);
      Debug.message(4, "LINKTYPE", paramCatalogEntry.getEntryArg(0), str);
      this.catalogEntries.addElement(paramCatalogEntry);
    }
    else if (i == NOTATION)
    {
      str = makeAbsolute(normalizeURI(paramCatalogEntry.getEntryArg(1)));
      paramCatalogEntry.setEntryArg(1, str);
      Debug.message(4, "NOTATION", paramCatalogEntry.getEntryArg(0), str);
      this.catalogEntries.addElement(paramCatalogEntry);
    }
    else
    {
      this.catalogEntries.addElement(paramCatalogEntry);
    }
  }
  
  public void unknownEntry(Vector paramVector)
  {
    if ((paramVector != null) && (paramVector.size() > 0))
    {
      String str = (String)paramVector.elementAt(0);
      Debug.message(2, "Unrecognized token parsing catalog", str);
    }
  }
  
  public void parseAllCatalogs()
    throws MalformedURLException, IOException
  {
    Object localObject2;
    for (int i = 0; i < this.catalogs.size(); i++)
    {
      localObject1 = null;
      try
      {
        localObject1 = (Catalog)this.catalogs.elementAt(i);
      }
      catch (ClassCastException localClassCastException)
      {
        localObject2 = (String)this.catalogs.elementAt(i);
        localObject1 = newCatalog();
        ((Catalog)localObject1).parseCatalog((String)localObject2);
        this.catalogs.setElementAt(localObject1, i);
        ((Catalog)localObject1).parseAllCatalogs();
      }
    }
    Object localObject1 = this.catalogEntries.elements();
    while (((Enumeration)localObject1).hasMoreElements())
    {
      CatalogEntry localCatalogEntry = (CatalogEntry)((Enumeration)localObject1).nextElement();
      if ((localCatalogEntry.getEntryType() == DELEGATE_PUBLIC) || (localCatalogEntry.getEntryType() == DELEGATE_SYSTEM) || (localCatalogEntry.getEntryType() == DELEGATE_URI))
      {
        localObject2 = newCatalog();
        ((Catalog)localObject2).parseCatalog(localCatalogEntry.getEntryArg(1));
      }
    }
  }
  
  public String resolveDoctype(String paramString1, String paramString2, String paramString3)
    throws MalformedURLException, IOException
  {
    String str = null;
    Debug.message(3, "resolveDoctype(" + paramString1 + "," + paramString2 + "," + paramString3 + ")");
    paramString3 = normalizeURI(paramString3);
    if ((paramString2 != null) && (paramString2.startsWith("urn:publicid:"))) {
      paramString2 = PublicId.decodeURN(paramString2);
    }
    if ((paramString3 != null) && (paramString3.startsWith("urn:publicid:")))
    {
      paramString3 = PublicId.decodeURN(paramString3);
      if ((paramString2 != null) && (!paramString2.equals(paramString3)))
      {
        Debug.message(1, "urn:publicid: system identifier differs from public identifier; using public identifier");
        paramString3 = null;
      }
      else
      {
        paramString2 = paramString3;
        paramString3 = null;
      }
    }
    if (paramString3 != null)
    {
      str = resolveLocalSystem(paramString3);
      if (str != null) {
        return str;
      }
    }
    if (paramString2 != null)
    {
      str = resolveLocalPublic(DOCTYPE, paramString1, paramString2, paramString3);
      if (str != null) {
        return str;
      }
    }
    boolean bool = this.default_override;
    Enumeration localEnumeration = this.catalogEntries.elements();
    while (localEnumeration.hasMoreElements())
    {
      CatalogEntry localCatalogEntry = (CatalogEntry)localEnumeration.nextElement();
      if (localCatalogEntry.getEntryType() == OVERRIDE) {
        bool = localCatalogEntry.getEntryArg(0).equalsIgnoreCase("YES");
      } else if ((localCatalogEntry.getEntryType() == DOCTYPE) && (localCatalogEntry.getEntryArg(0).equals(paramString1)) && ((bool) || (paramString3 == null))) {
        return localCatalogEntry.getEntryArg(1);
      }
    }
    return resolveSubordinateCatalogs(DOCTYPE, paramString1, paramString2, paramString3);
  }
  
  public String resolveDocument()
    throws MalformedURLException, IOException
  {
    Debug.message(3, "resolveDocument");
    Enumeration localEnumeration = this.catalogEntries.elements();
    while (localEnumeration.hasMoreElements())
    {
      CatalogEntry localCatalogEntry = (CatalogEntry)localEnumeration.nextElement();
      if (localCatalogEntry.getEntryType() == DOCUMENT) {
        return localCatalogEntry.getEntryArg(1);
      }
    }
    return resolveSubordinateCatalogs(DOCUMENT, null, null, null);
  }
  
  public String resolveEntity(String paramString1, String paramString2, String paramString3)
    throws MalformedURLException, IOException
  {
    String str = null;
    Debug.message(3, "resolveEntity(" + paramString1 + "," + paramString2 + "," + paramString3 + ")");
    paramString3 = normalizeURI(paramString3);
    if ((paramString2 != null) && (paramString2.startsWith("urn:publicid:"))) {
      paramString2 = PublicId.decodeURN(paramString2);
    }
    if ((paramString3 != null) && (paramString3.startsWith("urn:publicid:")))
    {
      paramString3 = PublicId.decodeURN(paramString3);
      if ((paramString2 != null) && (!paramString2.equals(paramString3)))
      {
        Debug.message(1, "urn:publicid: system identifier differs from public identifier; using public identifier");
        paramString3 = null;
      }
      else
      {
        paramString2 = paramString3;
        paramString3 = null;
      }
    }
    if (paramString3 != null)
    {
      str = resolveLocalSystem(paramString3);
      if (str != null) {
        return str;
      }
    }
    if (paramString2 != null)
    {
      str = resolveLocalPublic(ENTITY, paramString1, paramString2, paramString3);
      if (str != null) {
        return str;
      }
    }
    boolean bool = this.default_override;
    Enumeration localEnumeration = this.catalogEntries.elements();
    while (localEnumeration.hasMoreElements())
    {
      CatalogEntry localCatalogEntry = (CatalogEntry)localEnumeration.nextElement();
      if (localCatalogEntry.getEntryType() == OVERRIDE) {
        bool = localCatalogEntry.getEntryArg(0).equalsIgnoreCase("YES");
      } else if ((localCatalogEntry.getEntryType() == ENTITY) && (localCatalogEntry.getEntryArg(0).equals(paramString1)) && ((bool) || (paramString3 == null))) {
        return localCatalogEntry.getEntryArg(1);
      }
    }
    return resolveSubordinateCatalogs(ENTITY, paramString1, paramString2, paramString3);
  }
  
  public String resolveNotation(String paramString1, String paramString2, String paramString3)
    throws MalformedURLException, IOException
  {
    String str = null;
    Debug.message(3, "resolveNotation(" + paramString1 + "," + paramString2 + "," + paramString3 + ")");
    paramString3 = normalizeURI(paramString3);
    if ((paramString2 != null) && (paramString2.startsWith("urn:publicid:"))) {
      paramString2 = PublicId.decodeURN(paramString2);
    }
    if ((paramString3 != null) && (paramString3.startsWith("urn:publicid:")))
    {
      paramString3 = PublicId.decodeURN(paramString3);
      if ((paramString2 != null) && (!paramString2.equals(paramString3)))
      {
        Debug.message(1, "urn:publicid: system identifier differs from public identifier; using public identifier");
        paramString3 = null;
      }
      else
      {
        paramString2 = paramString3;
        paramString3 = null;
      }
    }
    if (paramString3 != null)
    {
      str = resolveLocalSystem(paramString3);
      if (str != null) {
        return str;
      }
    }
    if (paramString2 != null)
    {
      str = resolveLocalPublic(NOTATION, paramString1, paramString2, paramString3);
      if (str != null) {
        return str;
      }
    }
    boolean bool = this.default_override;
    Enumeration localEnumeration = this.catalogEntries.elements();
    while (localEnumeration.hasMoreElements())
    {
      CatalogEntry localCatalogEntry = (CatalogEntry)localEnumeration.nextElement();
      if (localCatalogEntry.getEntryType() == OVERRIDE) {
        bool = localCatalogEntry.getEntryArg(0).equalsIgnoreCase("YES");
      } else if ((localCatalogEntry.getEntryType() == NOTATION) && (localCatalogEntry.getEntryArg(0).equals(paramString1)) && ((bool) || (paramString3 == null))) {
        return localCatalogEntry.getEntryArg(1);
      }
    }
    return resolveSubordinateCatalogs(NOTATION, paramString1, paramString2, paramString3);
  }
  
  public String resolvePublic(String paramString1, String paramString2)
    throws MalformedURLException, IOException
  {
    Debug.message(3, "resolvePublic(" + paramString1 + "," + paramString2 + ")");
    paramString2 = normalizeURI(paramString2);
    if ((paramString1 != null) && (paramString1.startsWith("urn:publicid:"))) {
      paramString1 = PublicId.decodeURN(paramString1);
    }
    if ((paramString2 != null) && (paramString2.startsWith("urn:publicid:")))
    {
      paramString2 = PublicId.decodeURN(paramString2);
      if ((paramString1 != null) && (!paramString1.equals(paramString2)))
      {
        Debug.message(1, "urn:publicid: system identifier differs from public identifier; using public identifier");
        paramString2 = null;
      }
      else
      {
        paramString1 = paramString2;
        paramString2 = null;
      }
    }
    if (paramString2 != null)
    {
      str = resolveLocalSystem(paramString2);
      if (str != null) {
        return str;
      }
    }
    String str = resolveLocalPublic(PUBLIC, null, paramString1, paramString2);
    if (str != null) {
      return str;
    }
    return resolveSubordinateCatalogs(PUBLIC, null, paramString1, paramString2);
  }
  
  protected synchronized String resolveLocalPublic(int paramInt, String paramString1, String paramString2, String paramString3)
    throws MalformedURLException, IOException
  {
    paramString2 = PublicId.normalize(paramString2);
    if (paramString3 != null)
    {
      String str1 = resolveLocalSystem(paramString3);
      if (str1 != null) {
        return str1;
      }
    }
    boolean bool = this.default_override;
    Enumeration localEnumeration = this.catalogEntries.elements();
    while (localEnumeration.hasMoreElements())
    {
      localObject1 = (CatalogEntry)localEnumeration.nextElement();
      if (((CatalogEntry)localObject1).getEntryType() == OVERRIDE) {
        bool = ((CatalogEntry)localObject1).getEntryArg(0).equalsIgnoreCase("YES");
      } else if ((((CatalogEntry)localObject1).getEntryType() == PUBLIC) && (((CatalogEntry)localObject1).getEntryArg(0).equals(paramString2)) && ((bool) || (paramString3 == null))) {
        return ((CatalogEntry)localObject1).getEntryArg(1);
      }
    }
    bool = this.default_override;
    localEnumeration = this.catalogEntries.elements();
    Object localObject1 = new Vector();
    Object localObject2;
    Object localObject3;
    while (localEnumeration.hasMoreElements())
    {
      localObject2 = (CatalogEntry)localEnumeration.nextElement();
      if (((CatalogEntry)localObject2).getEntryType() == OVERRIDE)
      {
        bool = ((CatalogEntry)localObject2).getEntryArg(0).equalsIgnoreCase("YES");
      }
      else if ((((CatalogEntry)localObject2).getEntryType() == DELEGATE_PUBLIC) && ((bool) || (paramString3 == null)))
      {
        localObject3 = ((CatalogEntry)localObject2).getEntryArg(0);
        if ((((String)localObject3).length() <= paramString2.length()) && (((String)localObject3).equals(paramString2.substring(0, ((String)localObject3).length())))) {
          ((Vector)localObject1).addElement(((CatalogEntry)localObject2).getEntryArg(1));
        }
      }
    }
    if (((Vector)localObject1).size() > 0)
    {
      localObject2 = ((Vector)localObject1).elements();
      if (Debug.getDebug() > 1)
      {
        Debug.message(2, "Switching to delegated catalog(s):");
        while (((Enumeration)localObject2).hasMoreElements())
        {
          localObject3 = (String)((Enumeration)localObject2).nextElement();
          Debug.message(2, "\t" + (String)localObject3);
        }
      }
      localObject3 = newCatalog();
      localObject2 = ((Vector)localObject1).elements();
      while (((Enumeration)localObject2).hasMoreElements())
      {
        String str2 = (String)((Enumeration)localObject2).nextElement();
        ((Catalog)localObject3).parseCatalog(str2);
      }
      return ((Catalog)localObject3).resolvePublic(paramString2, null);
    }
    return null;
  }
  
  public String resolveSystem(String paramString)
    throws MalformedURLException, IOException
  {
    Debug.message(3, "resolveSystem(" + paramString + ")");
    paramString = normalizeURI(paramString);
    if ((paramString != null) && (paramString.startsWith("urn:publicid:")))
    {
      paramString = PublicId.decodeURN(paramString);
      return resolvePublic(paramString, null);
    }
    if (paramString != null)
    {
      String str = resolveLocalSystem(paramString);
      if (str != null) {
        return str;
      }
    }
    return resolveSubordinateCatalogs(SYSTEM, null, null, paramString);
  }
  
  protected String resolveLocalSystem(String paramString)
    throws MalformedURLException, IOException
  {
    String str1 = System.getProperty("os.name");
    int i = str1.indexOf("Windows") >= 0 ? 1 : 0;
    Enumeration localEnumeration = this.catalogEntries.elements();
    while (localEnumeration.hasMoreElements())
    {
      localObject1 = (CatalogEntry)localEnumeration.nextElement();
      if ((((CatalogEntry)localObject1).getEntryType() == SYSTEM) && ((((CatalogEntry)localObject1).getEntryArg(0).equals(paramString)) || ((i != 0) && (((CatalogEntry)localObject1).getEntryArg(0).equalsIgnoreCase(paramString))))) {
        return ((CatalogEntry)localObject1).getEntryArg(1);
      }
    }
    localEnumeration = this.catalogEntries.elements();
    Object localObject1 = null;
    String str2 = null;
    Object localObject3;
    while (localEnumeration.hasMoreElements())
    {
      localObject2 = (CatalogEntry)localEnumeration.nextElement();
      if (((CatalogEntry)localObject2).getEntryType() == REWRITE_SYSTEM)
      {
        localObject3 = ((CatalogEntry)localObject2).getEntryArg(0);
        if ((((String)localObject3).length() <= paramString.length()) && (((String)localObject3).equals(paramString.substring(0, ((String)localObject3).length()))) && ((localObject1 == null) || (((String)localObject3).length() > ((String)localObject1).length())))
        {
          localObject1 = localObject3;
          str2 = ((CatalogEntry)localObject2).getEntryArg(1);
        }
      }
      if (str2 != null) {
        return str2 + paramString.substring(((String)localObject1).length());
      }
    }
    localEnumeration = this.catalogEntries.elements();
    Object localObject2 = new Vector();
    Object localObject4;
    while (localEnumeration.hasMoreElements())
    {
      localObject3 = (CatalogEntry)localEnumeration.nextElement();
      if (((CatalogEntry)localObject3).getEntryType() == DELEGATE_SYSTEM)
      {
        localObject4 = ((CatalogEntry)localObject3).getEntryArg(0);
        if ((((String)localObject4).length() <= paramString.length()) && (((String)localObject4).equals(paramString.substring(0, ((String)localObject4).length())))) {
          ((Vector)localObject2).addElement(((CatalogEntry)localObject3).getEntryArg(1));
        }
      }
    }
    if (((Vector)localObject2).size() > 0)
    {
      localObject3 = ((Vector)localObject2).elements();
      if (Debug.getDebug() > 1)
      {
        Debug.message(2, "Switching to delegated catalog(s):");
        while (((Enumeration)localObject3).hasMoreElements())
        {
          localObject4 = (String)((Enumeration)localObject3).nextElement();
          Debug.message(2, "\t" + (String)localObject4);
        }
      }
      localObject4 = newCatalog();
      localObject3 = ((Vector)localObject2).elements();
      while (((Enumeration)localObject3).hasMoreElements())
      {
        String str3 = (String)((Enumeration)localObject3).nextElement();
        ((Catalog)localObject4).parseCatalog(str3);
      }
      return ((Catalog)localObject4).resolveSystem(paramString);
    }
    return null;
  }
  
  public String resolveURI(String paramString)
    throws MalformedURLException, IOException
  {
    Debug.message(3, "resolveURI(" + paramString + ")");
    paramString = normalizeURI(paramString);
    if ((paramString != null) && (paramString.startsWith("urn:publicid:")))
    {
      paramString = PublicId.decodeURN(paramString);
      return resolvePublic(paramString, null);
    }
    if (paramString != null)
    {
      String str = resolveLocalURI(paramString);
      if (str != null) {
        return str;
      }
    }
    return resolveSubordinateCatalogs(URI, null, null, paramString);
  }
  
  protected String resolveLocalURI(String paramString)
    throws MalformedURLException, IOException
  {
    Enumeration localEnumeration = this.catalogEntries.elements();
    while (localEnumeration.hasMoreElements())
    {
      localObject1 = (CatalogEntry)localEnumeration.nextElement();
      if ((((CatalogEntry)localObject1).getEntryType() == URI) && (((CatalogEntry)localObject1).getEntryArg(0).equals(paramString))) {
        return ((CatalogEntry)localObject1).getEntryArg(1);
      }
    }
    localEnumeration = this.catalogEntries.elements();
    Object localObject1 = null;
    String str1 = null;
    Object localObject3;
    while (localEnumeration.hasMoreElements())
    {
      localObject2 = (CatalogEntry)localEnumeration.nextElement();
      if (((CatalogEntry)localObject2).getEntryType() == REWRITE_URI)
      {
        localObject3 = ((CatalogEntry)localObject2).getEntryArg(0);
        if ((((String)localObject3).length() <= paramString.length()) && (((String)localObject3).equals(paramString.substring(0, ((String)localObject3).length()))) && ((localObject1 == null) || (((String)localObject3).length() > ((String)localObject1).length())))
        {
          localObject1 = localObject3;
          str1 = ((CatalogEntry)localObject2).getEntryArg(1);
        }
      }
      if (str1 != null) {
        return str1 + paramString.substring(((String)localObject1).length());
      }
    }
    localEnumeration = this.catalogEntries.elements();
    Object localObject2 = new Vector();
    Object localObject4;
    while (localEnumeration.hasMoreElements())
    {
      localObject3 = (CatalogEntry)localEnumeration.nextElement();
      if (((CatalogEntry)localObject3).getEntryType() == DELEGATE_URI)
      {
        localObject4 = ((CatalogEntry)localObject3).getEntryArg(0);
        if ((((String)localObject4).length() <= paramString.length()) && (((String)localObject4).equals(paramString.substring(0, ((String)localObject4).length())))) {
          ((Vector)localObject2).addElement(((CatalogEntry)localObject3).getEntryArg(1));
        }
      }
    }
    if (((Vector)localObject2).size() > 0)
    {
      localObject3 = ((Vector)localObject2).elements();
      if (Debug.getDebug() > 1)
      {
        Debug.message(2, "Switching to delegated catalog(s):");
        while (((Enumeration)localObject3).hasMoreElements())
        {
          localObject4 = (String)((Enumeration)localObject3).nextElement();
          Debug.message(2, "\t" + (String)localObject4);
        }
      }
      localObject4 = newCatalog();
      localObject3 = ((Vector)localObject2).elements();
      while (((Enumeration)localObject3).hasMoreElements())
      {
        String str2 = (String)((Enumeration)localObject3).nextElement();
        ((Catalog)localObject4).parseCatalog(str2);
      }
      return ((Catalog)localObject4).resolveURI(paramString);
    }
    return null;
  }
  
  protected synchronized String resolveSubordinateCatalogs(int paramInt, String paramString1, String paramString2, String paramString3)
    throws MalformedURLException, IOException
  {
    for (int i = 0; i < this.catalogs.size(); i++)
    {
      Catalog localCatalog = null;
      try
      {
        localCatalog = (Catalog)this.catalogs.elementAt(i);
      }
      catch (ClassCastException localClassCastException)
      {
        String str2 = (String)this.catalogs.elementAt(i);
        localCatalog = newCatalog();
        try
        {
          localCatalog.parseCatalog(str2);
        }
        catch (MalformedURLException localMalformedURLException)
        {
          Debug.message(1, "Malformed Catalog URL", str2);
        }
        catch (FileNotFoundException localFileNotFoundException)
        {
          Debug.message(1, "Failed to load catalog, file not found", str2);
        }
        catch (IOException localIOException)
        {
          Debug.message(1, "Failed to load catalog, I/O error", str2);
        }
        this.catalogs.setElementAt(localCatalog, i);
      }
      String str1 = null;
      if (paramInt == DOCTYPE) {
        str1 = localCatalog.resolveDoctype(paramString1, paramString2, paramString3);
      } else if (paramInt == DOCUMENT) {
        str1 = localCatalog.resolveDocument();
      } else if (paramInt == ENTITY) {
        str1 = localCatalog.resolveEntity(paramString1, paramString2, paramString3);
      } else if (paramInt == NOTATION) {
        str1 = localCatalog.resolveNotation(paramString1, paramString2, paramString3);
      } else if (paramInt == PUBLIC) {
        str1 = localCatalog.resolvePublic(paramString2, paramString3);
      } else if (paramInt == SYSTEM) {
        str1 = localCatalog.resolveSystem(paramString3);
      } else if (paramInt == URI) {
        str1 = localCatalog.resolveURI(paramString3);
      }
      if (str1 != null) {
        return str1;
      }
    }
    return null;
  }
  
  protected String fixSlashes(String paramString)
  {
    return paramString.replace('\\', '/');
  }
  
  protected String makeAbsolute(String paramString)
  {
    URL localURL = null;
    paramString = fixSlashes(paramString);
    try
    {
      localURL = new URL(this.base, paramString);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      Debug.message(1, "Malformed URL on system identifier", paramString);
    }
    if (localURL != null) {
      return localURL.toString();
    }
    return paramString;
  }
  
  protected String normalizeURI(String paramString)
  {
    String str = "";
    if (paramString == null) {
      return null;
    }
    byte[] arrayOfByte;
    try
    {
      arrayOfByte = paramString.getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      Debug.message(1, "UTF-8 is an unsupported encoding!?");
      return paramString;
    }
    for (int i = 0; i < arrayOfByte.length; i++)
    {
      int j = arrayOfByte[i] & 0xFF;
      if ((j <= 32) || (j > 127) || (j == 34) || (j == 60) || (j == 62) || (j == 92) || (j == 94) || (j == 96) || (j == 123) || (j == 124) || (j == 125) || (j == 127)) {
        str = str + encodedByte(j);
      } else {
        str = str + (char)arrayOfByte[i];
      }
    }
    return str;
  }
  
  protected String encodedByte(int paramInt)
  {
    String str = Integer.toHexString(paramInt).toUpperCase();
    if (str.length() < 2) {
      return "%0" + str;
    }
    return "%" + str;
  }
  
  protected void addDelegate(CatalogEntry paramCatalogEntry)
  {
    int i = 0;
    String str1 = paramCatalogEntry.getEntryArg(0);
    Enumeration localEnumeration = this.localDelegate.elements();
    while (localEnumeration.hasMoreElements())
    {
      CatalogEntry localCatalogEntry = (CatalogEntry)localEnumeration.nextElement();
      String str2 = localCatalogEntry.getEntryArg(0);
      if (str2.equals(str1)) {
        return;
      }
      if (str2.length() > str1.length()) {
        i++;
      }
      if (str2.length() < str1.length()) {
        break;
      }
    }
    if (this.localDelegate.size() == 0) {
      this.localDelegate.addElement(paramCatalogEntry);
    } else {
      this.localDelegate.insertElementAt(paramCatalogEntry, i);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\org\apache\xml\resolver\Catalog.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */