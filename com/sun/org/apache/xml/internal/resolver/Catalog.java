package com.sun.org.apache.xml.internal.resolver;

import com.sun.org.apache.xml.internal.resolver.helpers.Debug;
import com.sun.org.apache.xml.internal.resolver.helpers.FileURL;
import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;
import com.sun.org.apache.xml.internal.resolver.readers.CatalogReader;
import com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader;
import com.sun.org.apache.xml.internal.resolver.readers.TR9401CatalogReader;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
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
  public static final int SYSTEM_SUFFIX = CatalogEntry.addEntryType("SYSTEM_SUFFIX", 2);
  public static final int URI_SUFFIX = CatalogEntry.addEntryType("URI_SUFFIX", 2);
  protected URL base;
  protected URL catalogCwd;
  protected Vector catalogEntries = new Vector();
  protected boolean default_override = true;
  protected CatalogManager catalogManager = CatalogManager.getStaticManager();
  protected Vector catalogFiles = new Vector();
  protected Vector localCatalogFiles = new Vector();
  protected Vector catalogs = new Vector();
  protected Vector localDelegate = new Vector();
  protected Hashtable readerMap = new Hashtable();
  protected Vector readerArr = new Vector();
  
  public Catalog() {}
  
  public Catalog(CatalogManager manager)
  {
    this.catalogManager = manager;
  }
  
  public CatalogManager getCatalogManager()
  {
    return this.catalogManager;
  }
  
  public void setCatalogManager(CatalogManager manager)
  {
    this.catalogManager = manager;
  }
  
  public void setupReaders()
  {
    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setNamespaceAware(true);
    spf.setValidating(false);
    
    SAXCatalogReader saxReader = new SAXCatalogReader(spf);
    
    saxReader.setCatalogParser(null, "XMLCatalog", "com.sun.org.apache.xml.internal.resolver.readers.XCatalogReader");
    
    saxReader.setCatalogParser("urn:oasis:names:tc:entity:xmlns:xml:catalog", "catalog", "com.sun.org.apache.xml.internal.resolver.readers.OASISXMLCatalogReader");
    
    addReader("application/xml", saxReader);
    
    TR9401CatalogReader textReader = new TR9401CatalogReader();
    addReader("text/plain", textReader);
  }
  
  public void addReader(String mimeType, CatalogReader reader)
  {
    if (this.readerMap.containsKey(mimeType))
    {
      Integer pos = (Integer)this.readerMap.get(mimeType);
      this.readerArr.set(pos.intValue(), reader);
    }
    else
    {
      this.readerArr.add(reader);
      Integer pos = new Integer(this.readerArr.size() - 1);
      this.readerMap.put(mimeType, pos);
    }
  }
  
  protected void copyReaders(Catalog newCatalog)
  {
    Vector mapArr = new Vector(this.readerMap.size());
    for (int count = 0; count < this.readerMap.size(); count++) {
      mapArr.add(null);
    }
    Enumeration en = this.readerMap.keys();
    while (en.hasMoreElements())
    {
      String mimeType = (String)en.nextElement();
      Integer pos = (Integer)this.readerMap.get(mimeType);
      mapArr.set(pos.intValue(), mimeType);
    }
    for (int count = 0; count < mapArr.size(); count++)
    {
      String mimeType = (String)mapArr.get(count);
      Integer pos = (Integer)this.readerMap.get(mimeType);
      newCatalog.addReader(mimeType, (CatalogReader)this.readerArr.get(pos.intValue()));
    }
  }
  
  protected Catalog newCatalog()
  {
    String catalogClass = getClass().getName();
    try
    {
      Catalog c = (Catalog)Class.forName(catalogClass).newInstance();
      c.setCatalogManager(this.catalogManager);
      copyReaders(c);
      return c;
    }
    catch (ClassNotFoundException cnfe)
    {
      this.catalogManager.debug.message(1, "Class Not Found Exception: " + catalogClass);
    }
    catch (IllegalAccessException iae)
    {
      this.catalogManager.debug.message(1, "Illegal Access Exception: " + catalogClass);
    }
    catch (InstantiationException ie)
    {
      this.catalogManager.debug.message(1, "Instantiation Exception: " + catalogClass);
    }
    catch (ClassCastException cce)
    {
      this.catalogManager.debug.message(1, "Class Cast Exception: " + catalogClass);
    }
    catch (Exception e)
    {
      this.catalogManager.debug.message(1, "Other Exception: " + catalogClass);
    }
    Catalog c = new Catalog();
    c.setCatalogManager(this.catalogManager);
    copyReaders(c);
    return c;
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
    Vector catalogs = this.catalogManager.getCatalogFiles();
    if (catalogs != null) {
      for (int count = 0; count < catalogs.size(); count++) {
        this.catalogFiles.addElement(catalogs.elementAt(count));
      }
    }
    if (this.catalogFiles.size() > 0)
    {
      String catfile = (String)this.catalogFiles.lastElement();
      this.catalogFiles.removeElement(catfile);
      parseCatalog(catfile);
    }
  }
  
  public synchronized void parseCatalog(String fileName)
    throws MalformedURLException, IOException
  {
    this.default_override = this.catalogManager.getPreferPublic();
    this.catalogManager.debug.message(4, "Parse catalog: " + fileName);
    
    this.catalogFiles.addElement(fileName);
    
    parsePendingCatalogs();
  }
  
  public synchronized void parseCatalog(String mimeType, InputStream is)
    throws IOException, CatalogException
  {
    this.default_override = this.catalogManager.getPreferPublic();
    this.catalogManager.debug.message(4, "Parse " + mimeType + " catalog on input stream");
    
    CatalogReader reader = null;
    if (this.readerMap.containsKey(mimeType))
    {
      int arrayPos = ((Integer)this.readerMap.get(mimeType)).intValue();
      reader = (CatalogReader)this.readerArr.get(arrayPos);
    }
    if (reader == null)
    {
      String msg = "No CatalogReader for MIME type: " + mimeType;
      this.catalogManager.debug.message(2, msg);
      throw new CatalogException(6, msg);
    }
    reader.readCatalog(this, is);
    
    parsePendingCatalogs();
  }
  
  public synchronized void parseCatalog(URL aUrl)
    throws IOException
  {
    this.catalogCwd = aUrl;
    this.base = aUrl;
    
    this.default_override = this.catalogManager.getPreferPublic();
    this.catalogManager.debug.message(4, "Parse catalog: " + aUrl.toString());
    
    DataInputStream inStream = null;
    boolean parsed = false;
    for (int count = 0; (!parsed) && (count < this.readerArr.size()); count++)
    {
      CatalogReader reader = (CatalogReader)this.readerArr.get(count);
      try
      {
        inStream = new DataInputStream(aUrl.openStream());
      }
      catch (FileNotFoundException fnfe)
      {
        break;
      }
      try
      {
        reader.readCatalog(this, inStream);
        parsed = true;
      }
      catch (CatalogException ce)
      {
        if (ce.getExceptionType() != 7) {
          break label140;
        }
      }
      break;
      try
      {
        label140:
        inStream.close();
      }
      catch (IOException e) {}
    }
    if (parsed) {
      parsePendingCatalogs();
    }
  }
  
  protected synchronized void parsePendingCatalogs()
    throws MalformedURLException, IOException
  {
    if (!this.localCatalogFiles.isEmpty())
    {
      Vector newQueue = new Vector();
      Enumeration q = this.localCatalogFiles.elements();
      while (q.hasMoreElements()) {
        newQueue.addElement(q.nextElement());
      }
      for (int curCat = 0; curCat < this.catalogFiles.size(); curCat++)
      {
        String catfile = (String)this.catalogFiles.elementAt(curCat);
        newQueue.addElement(catfile);
      }
      this.catalogFiles = newQueue;
      this.localCatalogFiles.clear();
    }
    if ((this.catalogFiles.isEmpty()) && (!this.localDelegate.isEmpty()))
    {
      Enumeration e = this.localDelegate.elements();
      while (e.hasMoreElements()) {
        this.catalogEntries.addElement(e.nextElement());
      }
      this.localDelegate.clear();
    }
    while (!this.catalogFiles.isEmpty())
    {
      String catfile = (String)this.catalogFiles.elementAt(0);
      try
      {
        this.catalogFiles.remove(0);
      }
      catch (ArrayIndexOutOfBoundsException e) {}
      if ((this.catalogEntries.size() == 0) && (this.catalogs.size() == 0)) {
        try
        {
          parseCatalogFile(catfile);
        }
        catch (CatalogException ce)
        {
          System.out.println("FIXME: " + ce.toString());
        }
      } else {
        this.catalogs.addElement(catfile);
      }
      if (!this.localCatalogFiles.isEmpty())
      {
        Vector newQueue = new Vector();
        Enumeration q = this.localCatalogFiles.elements();
        while (q.hasMoreElements()) {
          newQueue.addElement(q.nextElement());
        }
        for (int curCat = 0; curCat < this.catalogFiles.size(); curCat++)
        {
          catfile = (String)this.catalogFiles.elementAt(curCat);
          newQueue.addElement(catfile);
        }
        this.catalogFiles = newQueue;
        this.localCatalogFiles.clear();
      }
      if (!this.localDelegate.isEmpty())
      {
        Enumeration e = this.localDelegate.elements();
        while (e.hasMoreElements()) {
          this.catalogEntries.addElement(e.nextElement());
        }
        this.localDelegate.clear();
      }
    }
    this.catalogFiles.clear();
  }
  
  protected synchronized void parseCatalogFile(String fileName)
    throws MalformedURLException, IOException, CatalogException
  {
    try
    {
      this.catalogCwd = FileURL.makeURL("basename");
    }
    catch (MalformedURLException e)
    {
      String userdir = System.getProperty("user.dir");
      userdir.replace('\\', '/');
      this.catalogManager.debug.message(1, "Malformed URL on cwd", userdir);
      this.catalogCwd = null;
    }
    try
    {
      this.base = new URL(this.catalogCwd, fixSlashes(fileName));
    }
    catch (MalformedURLException e)
    {
      try
      {
        this.base = new URL("file:" + fixSlashes(fileName));
      }
      catch (MalformedURLException e2)
      {
        this.catalogManager.debug.message(1, "Malformed URL on catalog filename", fixSlashes(fileName));
        
        this.base = null;
      }
    }
    this.catalogManager.debug.message(2, "Loading catalog", fileName);
    this.catalogManager.debug.message(4, "Default BASE", this.base.toString());
    
    fileName = this.base.toString();
    
    DataInputStream inStream = null;
    boolean parsed = false;
    boolean notFound = false;
    for (int count = 0; (!parsed) && (count < this.readerArr.size()); count++)
    {
      CatalogReader reader = (CatalogReader)this.readerArr.get(count);
      try
      {
        notFound = false;
        inStream = new DataInputStream(this.base.openStream());
      }
      catch (FileNotFoundException fnfe)
      {
        notFound = true;
        break;
      }
      try
      {
        reader.readCatalog(this, inStream);
        parsed = true;
      }
      catch (CatalogException ce)
      {
        if (ce.getExceptionType() != 7) {
          break label279;
        }
      }
      break;
      try
      {
        label279:
        inStream.close();
      }
      catch (IOException e) {}
    }
    if (!parsed) {
      if (notFound) {
        this.catalogManager.debug.message(3, "Catalog does not exist", fileName);
      } else {
        this.catalogManager.debug.message(1, "Failed to parse catalog", fileName);
      }
    }
  }
  
  public void addEntry(CatalogEntry entry)
  {
    int type = entry.getEntryType();
    if (type == BASE)
    {
      String value = entry.getEntryArg(0);
      URL newbase = null;
      if (this.base == null) {
        this.catalogManager.debug.message(5, "BASE CUR", "null");
      } else {
        this.catalogManager.debug.message(5, "BASE CUR", this.base.toString());
      }
      this.catalogManager.debug.message(4, "BASE STR", value);
      try
      {
        value = fixSlashes(value);
        newbase = new URL(this.base, value);
      }
      catch (MalformedURLException e)
      {
        try
        {
          newbase = new URL("file:" + value);
        }
        catch (MalformedURLException e2)
        {
          this.catalogManager.debug.message(1, "Malformed URL on base", value);
          newbase = null;
        }
      }
      if (newbase != null) {
        this.base = newbase;
      }
      this.catalogManager.debug.message(5, "BASE NEW", this.base.toString());
    }
    else if (type == CATALOG)
    {
      String fsi = makeAbsolute(entry.getEntryArg(0));
      
      this.catalogManager.debug.message(4, "CATALOG", fsi);
      
      this.localCatalogFiles.addElement(fsi);
    }
    else if (type == PUBLIC)
    {
      String publicid = PublicId.normalize(entry.getEntryArg(0));
      String systemid = makeAbsolute(normalizeURI(entry.getEntryArg(1)));
      
      entry.setEntryArg(0, publicid);
      entry.setEntryArg(1, systemid);
      
      this.catalogManager.debug.message(4, "PUBLIC", publicid, systemid);
      
      this.catalogEntries.addElement(entry);
    }
    else if (type == SYSTEM)
    {
      String systemid = normalizeURI(entry.getEntryArg(0));
      String fsi = makeAbsolute(normalizeURI(entry.getEntryArg(1)));
      
      entry.setEntryArg(1, fsi);
      
      this.catalogManager.debug.message(4, "SYSTEM", systemid, fsi);
      
      this.catalogEntries.addElement(entry);
    }
    else if (type == URI)
    {
      String uri = normalizeURI(entry.getEntryArg(0));
      String altURI = makeAbsolute(normalizeURI(entry.getEntryArg(1)));
      
      entry.setEntryArg(1, altURI);
      
      this.catalogManager.debug.message(4, "URI", uri, altURI);
      
      this.catalogEntries.addElement(entry);
    }
    else if (type == DOCUMENT)
    {
      String fsi = makeAbsolute(normalizeURI(entry.getEntryArg(0)));
      entry.setEntryArg(0, fsi);
      
      this.catalogManager.debug.message(4, "DOCUMENT", fsi);
      
      this.catalogEntries.addElement(entry);
    }
    else if (type == OVERRIDE)
    {
      this.catalogManager.debug.message(4, "OVERRIDE", entry.getEntryArg(0));
      
      this.catalogEntries.addElement(entry);
    }
    else if (type == SGMLDECL)
    {
      String fsi = makeAbsolute(normalizeURI(entry.getEntryArg(0)));
      entry.setEntryArg(0, fsi);
      
      this.catalogManager.debug.message(4, "SGMLDECL", fsi);
      
      this.catalogEntries.addElement(entry);
    }
    else if (type == DELEGATE_PUBLIC)
    {
      String ppi = PublicId.normalize(entry.getEntryArg(0));
      String fsi = makeAbsolute(normalizeURI(entry.getEntryArg(1)));
      
      entry.setEntryArg(0, ppi);
      entry.setEntryArg(1, fsi);
      
      this.catalogManager.debug.message(4, "DELEGATE_PUBLIC", ppi, fsi);
      
      addDelegate(entry);
    }
    else if (type == DELEGATE_SYSTEM)
    {
      String psi = normalizeURI(entry.getEntryArg(0));
      String fsi = makeAbsolute(normalizeURI(entry.getEntryArg(1)));
      
      entry.setEntryArg(0, psi);
      entry.setEntryArg(1, fsi);
      
      this.catalogManager.debug.message(4, "DELEGATE_SYSTEM", psi, fsi);
      
      addDelegate(entry);
    }
    else if (type == DELEGATE_URI)
    {
      String pui = normalizeURI(entry.getEntryArg(0));
      String fsi = makeAbsolute(normalizeURI(entry.getEntryArg(1)));
      
      entry.setEntryArg(0, pui);
      entry.setEntryArg(1, fsi);
      
      this.catalogManager.debug.message(4, "DELEGATE_URI", pui, fsi);
      
      addDelegate(entry);
    }
    else if (type == REWRITE_SYSTEM)
    {
      String psi = normalizeURI(entry.getEntryArg(0));
      String rpx = makeAbsolute(normalizeURI(entry.getEntryArg(1)));
      
      entry.setEntryArg(0, psi);
      entry.setEntryArg(1, rpx);
      
      this.catalogManager.debug.message(4, "REWRITE_SYSTEM", psi, rpx);
      
      this.catalogEntries.addElement(entry);
    }
    else if (type == REWRITE_URI)
    {
      String pui = normalizeURI(entry.getEntryArg(0));
      String upx = makeAbsolute(normalizeURI(entry.getEntryArg(1)));
      
      entry.setEntryArg(0, pui);
      entry.setEntryArg(1, upx);
      
      this.catalogManager.debug.message(4, "REWRITE_URI", pui, upx);
      
      this.catalogEntries.addElement(entry);
    }
    else if (type == SYSTEM_SUFFIX)
    {
      String pui = normalizeURI(entry.getEntryArg(0));
      String upx = makeAbsolute(normalizeURI(entry.getEntryArg(1)));
      
      entry.setEntryArg(0, pui);
      entry.setEntryArg(1, upx);
      
      this.catalogManager.debug.message(4, "SYSTEM_SUFFIX", pui, upx);
      
      this.catalogEntries.addElement(entry);
    }
    else if (type == URI_SUFFIX)
    {
      String pui = normalizeURI(entry.getEntryArg(0));
      String upx = makeAbsolute(normalizeURI(entry.getEntryArg(1)));
      
      entry.setEntryArg(0, pui);
      entry.setEntryArg(1, upx);
      
      this.catalogManager.debug.message(4, "URI_SUFFIX", pui, upx);
      
      this.catalogEntries.addElement(entry);
    }
    else if (type == DOCTYPE)
    {
      String fsi = makeAbsolute(normalizeURI(entry.getEntryArg(1)));
      entry.setEntryArg(1, fsi);
      
      this.catalogManager.debug.message(4, "DOCTYPE", entry.getEntryArg(0), fsi);
      
      this.catalogEntries.addElement(entry);
    }
    else if (type == DTDDECL)
    {
      String fpi = PublicId.normalize(entry.getEntryArg(0));
      entry.setEntryArg(0, fpi);
      String fsi = makeAbsolute(normalizeURI(entry.getEntryArg(1)));
      entry.setEntryArg(1, fsi);
      
      this.catalogManager.debug.message(4, "DTDDECL", fpi, fsi);
      
      this.catalogEntries.addElement(entry);
    }
    else if (type == ENTITY)
    {
      String fsi = makeAbsolute(normalizeURI(entry.getEntryArg(1)));
      entry.setEntryArg(1, fsi);
      
      this.catalogManager.debug.message(4, "ENTITY", entry.getEntryArg(0), fsi);
      
      this.catalogEntries.addElement(entry);
    }
    else if (type == LINKTYPE)
    {
      String fsi = makeAbsolute(normalizeURI(entry.getEntryArg(1)));
      entry.setEntryArg(1, fsi);
      
      this.catalogManager.debug.message(4, "LINKTYPE", entry.getEntryArg(0), fsi);
      
      this.catalogEntries.addElement(entry);
    }
    else if (type == NOTATION)
    {
      String fsi = makeAbsolute(normalizeURI(entry.getEntryArg(1)));
      entry.setEntryArg(1, fsi);
      
      this.catalogManager.debug.message(4, "NOTATION", entry.getEntryArg(0), fsi);
      
      this.catalogEntries.addElement(entry);
    }
    else
    {
      this.catalogEntries.addElement(entry);
    }
  }
  
  public void unknownEntry(Vector strings)
  {
    if ((strings != null) && (strings.size() > 0))
    {
      String keyword = (String)strings.elementAt(0);
      this.catalogManager.debug.message(2, "Unrecognized token parsing catalog", keyword);
    }
  }
  
  public void parseAllCatalogs()
    throws MalformedURLException, IOException
  {
    for (int catPos = 0; catPos < this.catalogs.size(); catPos++)
    {
      Catalog c = null;
      try
      {
        c = (Catalog)this.catalogs.elementAt(catPos);
      }
      catch (ClassCastException e)
      {
        String catfile = (String)this.catalogs.elementAt(catPos);
        c = newCatalog();
        
        c.parseCatalog(catfile);
        this.catalogs.setElementAt(c, catPos);
        c.parseAllCatalogs();
      }
    }
    Enumeration en = this.catalogEntries.elements();
    while (en.hasMoreElements())
    {
      CatalogEntry e = (CatalogEntry)en.nextElement();
      if ((e.getEntryType() == DELEGATE_PUBLIC) || (e.getEntryType() == DELEGATE_SYSTEM) || (e.getEntryType() == DELEGATE_URI))
      {
        Catalog dcat = newCatalog();
        dcat.parseCatalog(e.getEntryArg(1));
      }
    }
  }
  
  public String resolveDoctype(String entityName, String publicId, String systemId)
    throws MalformedURLException, IOException
  {
    String resolved = null;
    
    this.catalogManager.debug.message(3, "resolveDoctype(" + entityName + "," + publicId + "," + systemId + ")");
    
    systemId = normalizeURI(systemId);
    if ((publicId != null) && (publicId.startsWith("urn:publicid:"))) {
      publicId = PublicId.decodeURN(publicId);
    }
    if ((systemId != null) && (systemId.startsWith("urn:publicid:")))
    {
      systemId = PublicId.decodeURN(systemId);
      if ((publicId != null) && (!publicId.equals(systemId)))
      {
        this.catalogManager.debug.message(1, "urn:publicid: system identifier differs from public identifier; using public identifier");
        systemId = null;
      }
      else
      {
        publicId = systemId;
        systemId = null;
      }
    }
    if (systemId != null)
    {
      resolved = resolveLocalSystem(systemId);
      if (resolved != null) {
        return resolved;
      }
    }
    if (publicId != null)
    {
      resolved = resolveLocalPublic(DOCTYPE, entityName, publicId, systemId);
      if (resolved != null) {
        return resolved;
      }
    }
    boolean over = this.default_override;
    Enumeration en = this.catalogEntries.elements();
    while (en.hasMoreElements())
    {
      CatalogEntry e = (CatalogEntry)en.nextElement();
      if (e.getEntryType() == OVERRIDE) {
        over = e.getEntryArg(0).equalsIgnoreCase("YES");
      } else if ((e.getEntryType() == DOCTYPE) && (e.getEntryArg(0).equals(entityName))) {
        if ((over) || (systemId == null)) {
          return e.getEntryArg(1);
        }
      }
    }
    return resolveSubordinateCatalogs(DOCTYPE, entityName, publicId, systemId);
  }
  
  public String resolveDocument()
    throws MalformedURLException, IOException
  {
    this.catalogManager.debug.message(3, "resolveDocument");
    
    Enumeration en = this.catalogEntries.elements();
    while (en.hasMoreElements())
    {
      CatalogEntry e = (CatalogEntry)en.nextElement();
      if (e.getEntryType() == DOCUMENT) {
        return e.getEntryArg(0);
      }
    }
    return resolveSubordinateCatalogs(DOCUMENT, null, null, null);
  }
  
  public String resolveEntity(String entityName, String publicId, String systemId)
    throws MalformedURLException, IOException
  {
    String resolved = null;
    
    this.catalogManager.debug.message(3, "resolveEntity(" + entityName + "," + publicId + "," + systemId + ")");
    
    systemId = normalizeURI(systemId);
    if ((publicId != null) && (publicId.startsWith("urn:publicid:"))) {
      publicId = PublicId.decodeURN(publicId);
    }
    if ((systemId != null) && (systemId.startsWith("urn:publicid:")))
    {
      systemId = PublicId.decodeURN(systemId);
      if ((publicId != null) && (!publicId.equals(systemId)))
      {
        this.catalogManager.debug.message(1, "urn:publicid: system identifier differs from public identifier; using public identifier");
        systemId = null;
      }
      else
      {
        publicId = systemId;
        systemId = null;
      }
    }
    if (systemId != null)
    {
      resolved = resolveLocalSystem(systemId);
      if (resolved != null) {
        return resolved;
      }
    }
    if (publicId != null)
    {
      resolved = resolveLocalPublic(ENTITY, entityName, publicId, systemId);
      if (resolved != null) {
        return resolved;
      }
    }
    boolean over = this.default_override;
    Enumeration en = this.catalogEntries.elements();
    while (en.hasMoreElements())
    {
      CatalogEntry e = (CatalogEntry)en.nextElement();
      if (e.getEntryType() == OVERRIDE) {
        over = e.getEntryArg(0).equalsIgnoreCase("YES");
      } else if ((e.getEntryType() == ENTITY) && (e.getEntryArg(0).equals(entityName))) {
        if ((over) || (systemId == null)) {
          return e.getEntryArg(1);
        }
      }
    }
    return resolveSubordinateCatalogs(ENTITY, entityName, publicId, systemId);
  }
  
  public String resolveNotation(String notationName, String publicId, String systemId)
    throws MalformedURLException, IOException
  {
    String resolved = null;
    
    this.catalogManager.debug.message(3, "resolveNotation(" + notationName + "," + publicId + "," + systemId + ")");
    
    systemId = normalizeURI(systemId);
    if ((publicId != null) && (publicId.startsWith("urn:publicid:"))) {
      publicId = PublicId.decodeURN(publicId);
    }
    if ((systemId != null) && (systemId.startsWith("urn:publicid:")))
    {
      systemId = PublicId.decodeURN(systemId);
      if ((publicId != null) && (!publicId.equals(systemId)))
      {
        this.catalogManager.debug.message(1, "urn:publicid: system identifier differs from public identifier; using public identifier");
        systemId = null;
      }
      else
      {
        publicId = systemId;
        systemId = null;
      }
    }
    if (systemId != null)
    {
      resolved = resolveLocalSystem(systemId);
      if (resolved != null) {
        return resolved;
      }
    }
    if (publicId != null)
    {
      resolved = resolveLocalPublic(NOTATION, notationName, publicId, systemId);
      if (resolved != null) {
        return resolved;
      }
    }
    boolean over = this.default_override;
    Enumeration en = this.catalogEntries.elements();
    while (en.hasMoreElements())
    {
      CatalogEntry e = (CatalogEntry)en.nextElement();
      if (e.getEntryType() == OVERRIDE) {
        over = e.getEntryArg(0).equalsIgnoreCase("YES");
      } else if ((e.getEntryType() == NOTATION) && (e.getEntryArg(0).equals(notationName))) {
        if ((over) || (systemId == null)) {
          return e.getEntryArg(1);
        }
      }
    }
    return resolveSubordinateCatalogs(NOTATION, notationName, publicId, systemId);
  }
  
  public String resolvePublic(String publicId, String systemId)
    throws MalformedURLException, IOException
  {
    this.catalogManager.debug.message(3, "resolvePublic(" + publicId + "," + systemId + ")");
    
    systemId = normalizeURI(systemId);
    if ((publicId != null) && (publicId.startsWith("urn:publicid:"))) {
      publicId = PublicId.decodeURN(publicId);
    }
    if ((systemId != null) && (systemId.startsWith("urn:publicid:")))
    {
      systemId = PublicId.decodeURN(systemId);
      if ((publicId != null) && (!publicId.equals(systemId)))
      {
        this.catalogManager.debug.message(1, "urn:publicid: system identifier differs from public identifier; using public identifier");
        systemId = null;
      }
      else
      {
        publicId = systemId;
        systemId = null;
      }
    }
    if (systemId != null)
    {
      String resolved = resolveLocalSystem(systemId);
      if (resolved != null) {
        return resolved;
      }
    }
    String resolved = resolveLocalPublic(PUBLIC, null, publicId, systemId);
    if (resolved != null) {
      return resolved;
    }
    return resolveSubordinateCatalogs(PUBLIC, null, publicId, systemId);
  }
  
  protected synchronized String resolveLocalPublic(int entityType, String entityName, String publicId, String systemId)
    throws MalformedURLException, IOException
  {
    publicId = PublicId.normalize(publicId);
    if (systemId != null)
    {
      String resolved = resolveLocalSystem(systemId);
      if (resolved != null) {
        return resolved;
      }
    }
    boolean over = this.default_override;
    Enumeration en = this.catalogEntries.elements();
    while (en.hasMoreElements())
    {
      CatalogEntry e = (CatalogEntry)en.nextElement();
      if (e.getEntryType() == OVERRIDE) {
        over = e.getEntryArg(0).equalsIgnoreCase("YES");
      } else if ((e.getEntryType() == PUBLIC) && (e.getEntryArg(0).equals(publicId))) {
        if ((over) || (systemId == null)) {
          return e.getEntryArg(1);
        }
      }
    }
    over = this.default_override;
    en = this.catalogEntries.elements();
    Vector delCats = new Vector();
    while (en.hasMoreElements())
    {
      CatalogEntry e = (CatalogEntry)en.nextElement();
      if (e.getEntryType() == OVERRIDE)
      {
        over = e.getEntryArg(0).equalsIgnoreCase("YES");
      }
      else if ((e.getEntryType() == DELEGATE_PUBLIC) && ((over) || (systemId == null)))
      {
        String p = e.getEntryArg(0);
        if ((p.length() <= publicId.length()) && (p.equals(publicId.substring(0, p.length())))) {
          delCats.addElement(e.getEntryArg(1));
        }
      }
    }
    if (delCats.size() > 0)
    {
      Enumeration enCats = delCats.elements();
      if (this.catalogManager.debug.getDebug() > 1)
      {
        this.catalogManager.debug.message(2, "Switching to delegated catalog(s):");
        while (enCats.hasMoreElements())
        {
          String delegatedCatalog = (String)enCats.nextElement();
          this.catalogManager.debug.message(2, "\t" + delegatedCatalog);
        }
      }
      Catalog dcat = newCatalog();
      
      enCats = delCats.elements();
      while (enCats.hasMoreElements())
      {
        String delegatedCatalog = (String)enCats.nextElement();
        dcat.parseCatalog(delegatedCatalog);
      }
      return dcat.resolvePublic(publicId, null);
    }
    return null;
  }
  
  public String resolveSystem(String systemId)
    throws MalformedURLException, IOException
  {
    this.catalogManager.debug.message(3, "resolveSystem(" + systemId + ")");
    
    systemId = normalizeURI(systemId);
    if ((systemId != null) && (systemId.startsWith("urn:publicid:")))
    {
      systemId = PublicId.decodeURN(systemId);
      return resolvePublic(systemId, null);
    }
    if (systemId != null)
    {
      String resolved = resolveLocalSystem(systemId);
      if (resolved != null) {
        return resolved;
      }
    }
    return resolveSubordinateCatalogs(SYSTEM, null, null, systemId);
  }
  
  protected String resolveLocalSystem(String systemId)
    throws MalformedURLException, IOException
  {
    String osname = System.getProperty("os.name");
    boolean windows = osname.indexOf("Windows") >= 0;
    Enumeration en = this.catalogEntries.elements();
    while (en.hasMoreElements())
    {
      CatalogEntry e = (CatalogEntry)en.nextElement();
      if ((e.getEntryType() == SYSTEM) && ((e.getEntryArg(0).equals(systemId)) || ((windows) && (e.getEntryArg(0).equalsIgnoreCase(systemId))))) {
        return e.getEntryArg(1);
      }
    }
    en = this.catalogEntries.elements();
    String startString = null;
    String prefix = null;
    while (en.hasMoreElements())
    {
      CatalogEntry e = (CatalogEntry)en.nextElement();
      if (e.getEntryType() == REWRITE_SYSTEM)
      {
        String p = e.getEntryArg(0);
        if ((p.length() <= systemId.length()) && (p.equals(systemId.substring(0, p.length())))) {
          if ((startString == null) || (p.length() > startString.length()))
          {
            startString = p;
            prefix = e.getEntryArg(1);
          }
        }
      }
    }
    if (prefix != null) {
      return prefix + systemId.substring(startString.length());
    }
    en = this.catalogEntries.elements();
    String suffixString = null;
    String suffixURI = null;
    while (en.hasMoreElements())
    {
      CatalogEntry e = (CatalogEntry)en.nextElement();
      if (e.getEntryType() == SYSTEM_SUFFIX)
      {
        String p = e.getEntryArg(0);
        if ((p.length() <= systemId.length()) && (systemId.endsWith(p))) {
          if ((suffixString == null) || (p.length() > suffixString.length()))
          {
            suffixString = p;
            suffixURI = e.getEntryArg(1);
          }
        }
      }
    }
    if (suffixURI != null) {
      return suffixURI;
    }
    en = this.catalogEntries.elements();
    Vector delCats = new Vector();
    while (en.hasMoreElements())
    {
      CatalogEntry e = (CatalogEntry)en.nextElement();
      if (e.getEntryType() == DELEGATE_SYSTEM)
      {
        String p = e.getEntryArg(0);
        if ((p.length() <= systemId.length()) && (p.equals(systemId.substring(0, p.length())))) {
          delCats.addElement(e.getEntryArg(1));
        }
      }
    }
    if (delCats.size() > 0)
    {
      Enumeration enCats = delCats.elements();
      if (this.catalogManager.debug.getDebug() > 1)
      {
        this.catalogManager.debug.message(2, "Switching to delegated catalog(s):");
        while (enCats.hasMoreElements())
        {
          String delegatedCatalog = (String)enCats.nextElement();
          this.catalogManager.debug.message(2, "\t" + delegatedCatalog);
        }
      }
      Catalog dcat = newCatalog();
      
      enCats = delCats.elements();
      while (enCats.hasMoreElements())
      {
        String delegatedCatalog = (String)enCats.nextElement();
        dcat.parseCatalog(delegatedCatalog);
      }
      return dcat.resolveSystem(systemId);
    }
    return null;
  }
  
  public String resolveURI(String uri)
    throws MalformedURLException, IOException
  {
    this.catalogManager.debug.message(3, "resolveURI(" + uri + ")");
    
    uri = normalizeURI(uri);
    if ((uri != null) && (uri.startsWith("urn:publicid:")))
    {
      uri = PublicId.decodeURN(uri);
      return resolvePublic(uri, null);
    }
    if (uri != null)
    {
      String resolved = resolveLocalURI(uri);
      if (resolved != null) {
        return resolved;
      }
    }
    return resolveSubordinateCatalogs(URI, null, null, uri);
  }
  
  protected String resolveLocalURI(String uri)
    throws MalformedURLException, IOException
  {
    Enumeration en = this.catalogEntries.elements();
    while (en.hasMoreElements())
    {
      CatalogEntry e = (CatalogEntry)en.nextElement();
      if ((e.getEntryType() == URI) && (e.getEntryArg(0).equals(uri))) {
        return e.getEntryArg(1);
      }
    }
    en = this.catalogEntries.elements();
    String startString = null;
    String prefix = null;
    while (en.hasMoreElements())
    {
      CatalogEntry e = (CatalogEntry)en.nextElement();
      if (e.getEntryType() == REWRITE_URI)
      {
        String p = e.getEntryArg(0);
        if ((p.length() <= uri.length()) && (p.equals(uri.substring(0, p.length())))) {
          if ((startString == null) || (p.length() > startString.length()))
          {
            startString = p;
            prefix = e.getEntryArg(1);
          }
        }
      }
    }
    if (prefix != null) {
      return prefix + uri.substring(startString.length());
    }
    en = this.catalogEntries.elements();
    String suffixString = null;
    String suffixURI = null;
    while (en.hasMoreElements())
    {
      CatalogEntry e = (CatalogEntry)en.nextElement();
      if (e.getEntryType() == URI_SUFFIX)
      {
        String p = e.getEntryArg(0);
        if ((p.length() <= uri.length()) && (uri.endsWith(p))) {
          if ((suffixString == null) || (p.length() > suffixString.length()))
          {
            suffixString = p;
            suffixURI = e.getEntryArg(1);
          }
        }
      }
    }
    if (suffixURI != null) {
      return suffixURI;
    }
    en = this.catalogEntries.elements();
    Vector delCats = new Vector();
    while (en.hasMoreElements())
    {
      CatalogEntry e = (CatalogEntry)en.nextElement();
      if (e.getEntryType() == DELEGATE_URI)
      {
        String p = e.getEntryArg(0);
        if ((p.length() <= uri.length()) && (p.equals(uri.substring(0, p.length())))) {
          delCats.addElement(e.getEntryArg(1));
        }
      }
    }
    if (delCats.size() > 0)
    {
      Enumeration enCats = delCats.elements();
      if (this.catalogManager.debug.getDebug() > 1)
      {
        this.catalogManager.debug.message(2, "Switching to delegated catalog(s):");
        while (enCats.hasMoreElements())
        {
          String delegatedCatalog = (String)enCats.nextElement();
          this.catalogManager.debug.message(2, "\t" + delegatedCatalog);
        }
      }
      Catalog dcat = newCatalog();
      
      enCats = delCats.elements();
      while (enCats.hasMoreElements())
      {
        String delegatedCatalog = (String)enCats.nextElement();
        dcat.parseCatalog(delegatedCatalog);
      }
      return dcat.resolveURI(uri);
    }
    return null;
  }
  
  protected synchronized String resolveSubordinateCatalogs(int entityType, String entityName, String publicId, String systemId)
    throws MalformedURLException, IOException
  {
    for (int catPos = 0; catPos < this.catalogs.size(); catPos++)
    {
      Catalog c = null;
      try
      {
        c = (Catalog)this.catalogs.elementAt(catPos);
      }
      catch (ClassCastException e)
      {
        String catfile = (String)this.catalogs.elementAt(catPos);
        c = newCatalog();
        try
        {
          c.parseCatalog(catfile);
        }
        catch (MalformedURLException mue)
        {
          this.catalogManager.debug.message(1, "Malformed Catalog URL", catfile);
        }
        catch (FileNotFoundException fnfe)
        {
          this.catalogManager.debug.message(1, "Failed to load catalog, file not found", catfile);
        }
        catch (IOException ioe)
        {
          this.catalogManager.debug.message(1, "Failed to load catalog, I/O error", catfile);
        }
        this.catalogs.setElementAt(c, catPos);
      }
      String resolved = null;
      if (entityType == DOCTYPE) {
        resolved = c.resolveDoctype(entityName, publicId, systemId);
      } else if (entityType == DOCUMENT) {
        resolved = c.resolveDocument();
      } else if (entityType == ENTITY) {
        resolved = c.resolveEntity(entityName, publicId, systemId);
      } else if (entityType == NOTATION) {
        resolved = c.resolveNotation(entityName, publicId, systemId);
      } else if (entityType == PUBLIC) {
        resolved = c.resolvePublic(publicId, systemId);
      } else if (entityType == SYSTEM) {
        resolved = c.resolveSystem(systemId);
      } else if (entityType == URI) {
        resolved = c.resolveURI(systemId);
      }
      if (resolved != null) {
        return resolved;
      }
    }
    return null;
  }
  
  protected String fixSlashes(String sysid)
  {
    return sysid.replace('\\', '/');
  }
  
  protected String makeAbsolute(String sysid)
  {
    URL local = null;
    
    sysid = fixSlashes(sysid);
    try
    {
      local = new URL(this.base, sysid);
    }
    catch (MalformedURLException e)
    {
      this.catalogManager.debug.message(1, "Malformed URL on system identifier", sysid);
    }
    if (local != null) {
      return local.toString();
    }
    return sysid;
  }
  
  protected String normalizeURI(String uriref)
  {
    String newRef = "";
    if (uriref == null) {
      return null;
    }
    byte[] bytes;
    try
    {
      bytes = uriref.getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException uee)
    {
      this.catalogManager.debug.message(1, "UTF-8 is an unsupported encoding!?");
      return uriref;
    }
    for (int count = 0; count < bytes.length; count++)
    {
      int ch = bytes[count] & 0xFF;
      if ((ch <= 32) || (ch > 127) || (ch == 34) || (ch == 60) || (ch == 62) || (ch == 92) || (ch == 94) || (ch == 96) || (ch == 123) || (ch == 124) || (ch == 125) || (ch == 127)) {
        newRef = newRef + encodedByte(ch);
      } else {
        newRef = newRef + (char)bytes[count];
      }
    }
    return newRef;
  }
  
  protected String encodedByte(int b)
  {
    String hex = Integer.toHexString(b).toUpperCase();
    if (hex.length() < 2) {
      return "%0" + hex;
    }
    return "%" + hex;
  }
  
  protected void addDelegate(CatalogEntry entry)
  {
    int pos = 0;
    String partial = entry.getEntryArg(0);
    
    Enumeration local = this.localDelegate.elements();
    while (local.hasMoreElements())
    {
      CatalogEntry dpe = (CatalogEntry)local.nextElement();
      String dp = dpe.getEntryArg(0);
      if (dp.equals(partial)) {
        return;
      }
      if (dp.length() > partial.length()) {
        pos++;
      }
      if (dp.length() < partial.length()) {
        break;
      }
    }
    if (this.localDelegate.size() == 0) {
      this.localDelegate.addElement(entry);
    } else {
      this.localDelegate.insertElementAt(entry, pos);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\org\apache\xml\internal\resolver\Catalog.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */