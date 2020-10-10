package com.sun.javaws.jnl;

import com.sun.deploy.config.Config;
import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.deploy.util.URLUtil;
import com.sun.deploy.xml.BadTokenException;
import com.sun.deploy.xml.XMLEncoding;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLParser;
import com.sun.javaws.Globals;
import com.sun.javaws.exceptions.BadFieldException;
import com.sun.javaws.exceptions.JNLParseException;
import com.sun.javaws.exceptions.MissingFieldException;
import com.sun.javaws.util.GeneralUtil;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class XMLFormat
{
  public static LaunchDesc parse(byte[] paramArrayOfByte)
    throws IOException, BadFieldException, MissingFieldException, JNLParseException
  {
    String str1;
    try
    {
      str1 = XMLEncoding.decodeXML(paramArrayOfByte);
    }
    catch (Exception localException1)
    {
      throw new JNLParseException(null, localException1, "exception determining encoding of jnlp file", 0);
    }
    XMLNode localXMLNode;
    try
    {
      localXMLNode = new XMLParser(str1).parse();
    }
    catch (BadTokenException localBadTokenException)
    {
      throw new JNLParseException(str1, localBadTokenException, "wrong kind of token found", localBadTokenException.getLine());
    }
    catch (Exception localException2)
    {
      throw new JNLParseException(str1, localException2, "exception parsing jnlp file", 0);
    }
    InformationDesc localInformationDesc = null;
    ResourcesDesc localResourcesDesc = null;
    ApplicationDesc localApplicationDesc = null;
    AppletDesc localAppletDesc = null;
    LibraryDesc localLibraryDesc = null;
    InstallerDesc localInstallerDesc = null;
    String str2 = null;
    if ((localXMLNode.getName().equals("player")) || (localXMLNode.getName().equals("viewer")))
    {
      str3 = XMLUtils.getAttribute(localXMLNode, null, "tab");
      return LaunchDescFactory.buildInternalLaunchDesc(localXMLNode.getName(), str1, str3);
    }
    if (!localXMLNode.getName().equals("jnlp")) {
      throw new MissingFieldException(str1, "<jnlp>");
    }
    String str3 = XMLUtils.getAttribute(localXMLNode, "", "spec", "1.0+");
    String str4 = XMLUtils.getAttribute(localXMLNode, "", "version");
    
    URL localURL1 = URLUtil.asPathURL(XMLUtils.getAttributeURL(str1, localXMLNode, "", "codebase"));
    
    URL localURL2 = XMLUtils.getAttributeURL(str1, localURL1, localXMLNode, "", "href");
    
    int i = 0;
    if (XMLUtils.isElementPath(localXMLNode, "<security><all-permissions>")) {
      i = 1;
    } else if (XMLUtils.isElementPath(localXMLNode, "<security><j2ee-application-client-permissions>")) {
      i = 2;
    }
    int j;
    if (XMLUtils.isElementPath(localXMLNode, "<application-desc>"))
    {
      j = 1;
      localApplicationDesc = buildApplicationDesc(str1, localXMLNode);
    }
    else if (XMLUtils.isElementPath(localXMLNode, "<component-desc>"))
    {
      j = 3;
      localLibraryDesc = buildLibraryDesc(str1, localXMLNode);
    }
    else if (XMLUtils.isElementPath(localXMLNode, "<installer-desc>"))
    {
      j = 4;
      localInstallerDesc = buildInstallerDesc(str1, localURL1, localXMLNode);
    }
    else if (XMLUtils.isElementPath(localXMLNode, "<applet-desc>"))
    {
      j = 2;
      localAppletDesc = buildAppletDesc(str1, localURL1, localXMLNode);
    }
    else
    {
      throw new MissingFieldException(str1, "<jnlp>(<application-desc>|<applet-desc>|<installer-desc>|<component-desc>)");
    }
    localInformationDesc = buildInformationDesc(str1, localURL1, localXMLNode);
    localResourcesDesc = buildResourcesDesc(str1, localURL1, localXMLNode, false);
    
    LaunchDesc localLaunchDesc = new LaunchDesc(str3, localURL1, localURL2, str4, localInformationDesc, i, localResourcesDesc, j, localApplicationDesc, localAppletDesc, localLibraryDesc, localInstallerDesc, str2, str1, paramArrayOfByte);
    
    Trace.println("returning LaunchDesc from XMLFormat.parse():\n" + localLaunchDesc, TraceLevel.TEMP);
    
    return localLaunchDesc;
  }
  
  private static InformationDesc combineInformationDesc(InformationDesc paramInformationDesc1, InformationDesc paramInformationDesc2)
  {
    if (paramInformationDesc1 == null) {
      return paramInformationDesc2;
    }
    if (paramInformationDesc2 == null) {
      return paramInformationDesc1;
    }
    String str1 = paramInformationDesc1.getTitle() != null ? paramInformationDesc1.getTitle() : paramInformationDesc2.getTitle();
    String str2 = paramInformationDesc1.getVendor() != null ? paramInformationDesc1.getVendor() : paramInformationDesc2.getVendor();
    URL localURL = paramInformationDesc1.getHome() != null ? paramInformationDesc1.getHome() : paramInformationDesc2.getHome();
    
    String[] arrayOfString = new String[4];
    for (int i = 0; i < arrayOfString.length; i++) {
      arrayOfString[i] = (paramInformationDesc1.getDescription(i) != null ? paramInformationDesc1.getDescription(i) : paramInformationDesc2.getDescription(i));
    }
    ArrayList localArrayList = new ArrayList();
    if (paramInformationDesc2.getIcons() != null) {
      localArrayList.addAll(Arrays.asList(paramInformationDesc2.getIcons()));
    }
    if (paramInformationDesc1.getIcons() != null) {
      localArrayList.addAll(Arrays.asList(paramInformationDesc1.getIcons()));
    }
    IconDesc[] arrayOfIconDesc = new IconDesc[localArrayList.size()];
    arrayOfIconDesc = (IconDesc[])localArrayList.toArray(arrayOfIconDesc);
    
    boolean bool = (paramInformationDesc1.supportsOfflineOperation()) || (paramInformationDesc1.supportsOfflineOperation());
    
    ShortcutDesc localShortcutDesc = paramInformationDesc1.getShortcut() != null ? paramInformationDesc1.getShortcut() : paramInformationDesc2.getShortcut();
    
    AssociationDesc[] arrayOfAssociationDesc = (AssociationDesc[])addArrays((Object[])paramInformationDesc1.getAssociations(), (Object[])paramInformationDesc2.getAssociations());
    
    RContentDesc[] arrayOfRContentDesc = (RContentDesc[])addArrays((Object[])paramInformationDesc1.getRelatedContent(), (Object[])paramInformationDesc2.getRelatedContent());
    
    return new InformationDesc(str1, str2, localURL, arrayOfString, arrayOfIconDesc, localShortcutDesc, arrayOfRContentDesc, arrayOfAssociationDesc, bool);
  }
  
  private static InformationDesc buildInformationDesc(String paramString, URL paramURL, XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    ArrayList localArrayList = new ArrayList();
    
    XMLUtils.visitElements(paramXMLNode, "<information>", new XMLUtils.ElementVisitor()
    {
      private final String val$source;
      private final URL val$codebase;
      private final ArrayList val$list;
      
      public void visitElement(XMLNode paramAnonymousXMLNode)
        throws BadFieldException, MissingFieldException
      {
        String[] arrayOfString1 = GeneralUtil.getStringList(XMLUtils.getAttribute(paramAnonymousXMLNode, "", "locale"));
        
        String[] arrayOfString2 = GeneralUtil.getStringList(XMLUtils.getAttribute(paramAnonymousXMLNode, "", "os", null));
        
        String[] arrayOfString3 = GeneralUtil.getStringList(XMLUtils.getAttribute(paramAnonymousXMLNode, "", "arch", null));
        
        String[] arrayOfString4 = GeneralUtil.getStringList(XMLUtils.getAttribute(paramAnonymousXMLNode, "", "locale", null));
        
        String[] arrayOfString5 = GeneralUtil.getStringList(XMLUtils.getAttribute(paramAnonymousXMLNode, "", "platform", null));
        if ((GeneralUtil.prefixMatchStringList(arrayOfString2, Config.getOSName())) && (GeneralUtil.prefixMatchStringList(arrayOfString3, Config.getOSArch())) && (GeneralUtil.prefixMatchStringList(arrayOfString5, Config.getOSPlatform())) && (XMLFormat.matchDefaultLocale(arrayOfString4)))
        {
          String str1 = XMLUtils.getElementContents(paramAnonymousXMLNode, "<title>");
          String str2 = XMLUtils.getElementContents(paramAnonymousXMLNode, "<vendor>");
          URL localURL = XMLUtils.getAttributeURL(this.val$source, this.val$codebase, paramAnonymousXMLNode, "<homepage>", "href");
          
          String[] arrayOfString6 = new String[4];
          
          arrayOfString6[0] = XMLUtils.getElementContentsWithAttribute(paramAnonymousXMLNode, "<description>", "kind", "", null);
          
          arrayOfString6[2] = XMLUtils.getElementContentsWithAttribute(paramAnonymousXMLNode, "<description>", "kind", "one-line", null);
          
          arrayOfString6[1] = XMLUtils.getElementContentsWithAttribute(paramAnonymousXMLNode, "<description>", "kind", "short", null);
          
          arrayOfString6[3] = XMLUtils.getElementContentsWithAttribute(paramAnonymousXMLNode, "<description>", "kind", "tooltip", null);
          
          IconDesc[] arrayOfIconDesc = XMLFormat.getIconDescs(this.val$source, this.val$codebase, paramAnonymousXMLNode);
          
          ShortcutDesc localShortcutDesc = XMLFormat.getShortcutDesc(paramAnonymousXMLNode);
          
          RContentDesc[] arrayOfRContentDesc = XMLFormat.getRContentDescs(this.val$source, this.val$codebase, paramAnonymousXMLNode);
          
          AssociationDesc[] arrayOfAssociationDesc = XMLFormat.getAssociationDesc(this.val$source, paramAnonymousXMLNode);
          
          this.val$list.add(new InformationDesc(str1, str2, localURL, arrayOfString6, arrayOfIconDesc, localShortcutDesc, arrayOfRContentDesc, arrayOfAssociationDesc, XMLUtils.isElementPath(paramAnonymousXMLNode, "<offline-allowed>")));
        }
      }
    });
    InformationDesc localInformationDesc1 = new InformationDesc(null, null, null, null, null, null, null, null, false);
    for (int i = 0; i < localArrayList.size(); i++)
    {
      InformationDesc localInformationDesc2 = (InformationDesc)localArrayList.get(i);
      localInformationDesc1 = combineInformationDesc(localInformationDesc2, localInformationDesc1);
    }
    if (localInformationDesc1.getTitle() == null) {
      throw new MissingFieldException(paramString, "<jnlp><information><title>");
    }
    if (localInformationDesc1.getVendor() == null) {
      throw new MissingFieldException(paramString, "<jnlp><information><vendor>");
    }
    return localInformationDesc1;
  }
  
  private static Object[] addArrays(Object[] paramArrayOfObject1, Object[] paramArrayOfObject2)
  {
    if (paramArrayOfObject1 == null) {
      return paramArrayOfObject2;
    }
    if (paramArrayOfObject2 == null) {
      return paramArrayOfObject1;
    }
    ArrayList localArrayList = new ArrayList();
    for (int i = 0; i < paramArrayOfObject1.length; localArrayList.add(paramArrayOfObject1[(i++)])) {}
    for (i = 0; i < paramArrayOfObject2.length; localArrayList.add(paramArrayOfObject2[(i++)])) {}
    return (Object[])localArrayList.toArray(paramArrayOfObject1);
  }
  
  public static boolean matchDefaultLocale(String[] paramArrayOfString)
  {
    return GeneralUtil.matchLocale(paramArrayOfString, Globals.getDefaultLocale());
  }
  
  static final ResourcesDesc buildResourcesDesc(String paramString, URL paramURL, XMLNode paramXMLNode, boolean paramBoolean)
    throws MissingFieldException, BadFieldException
  {
    ResourcesDesc localResourcesDesc = new ResourcesDesc();
    
    XMLUtils.visitElements(paramXMLNode, "<resources>", new XMLUtils.ElementVisitor()
    {
      private final String val$source;
      private final URL val$codebase;
      private final ResourcesDesc val$rdesc;
      private final boolean val$ignoreJres;
      
      public void visitElement(XMLNode paramAnonymousXMLNode)
        throws MissingFieldException, BadFieldException
      {
        String[] arrayOfString1 = GeneralUtil.getStringList(XMLUtils.getAttribute(paramAnonymousXMLNode, "", "os", null));
        String[] arrayOfString2 = GeneralUtil.getStringList(XMLUtils.getAttribute(paramAnonymousXMLNode, "", "arch", null));
        String[] arrayOfString3 = GeneralUtil.getStringList(XMLUtils.getAttribute(paramAnonymousXMLNode, "", "locale", null));
        if ((GeneralUtil.prefixMatchStringList(arrayOfString1, Config.getOSName())) && (GeneralUtil.prefixMatchStringList(arrayOfString2, Config.getOSArch())) && (XMLFormat.matchDefaultLocale(arrayOfString3))) {
          XMLUtils.visitChildrenElements(paramAnonymousXMLNode, new XMLUtils.ElementVisitor()
          {
            public void visitElement(XMLNode paramAnonymous2XMLNode)
              throws MissingFieldException, BadFieldException
            {
              XMLFormat.handleResourceElement(XMLFormat.2.this.val$source, XMLFormat.2.this.val$codebase, paramAnonymous2XMLNode, XMLFormat.2.this.val$rdesc, XMLFormat.2.this.val$ignoreJres);
            }
          });
        }
      }
    });
    return localResourcesDesc.isEmpty() ? null : localResourcesDesc;
  }
  
  private static IconDesc[] getIconDescs(String paramString, URL paramURL, XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    ArrayList localArrayList = new ArrayList();
    XMLUtils.visitElements(paramXMLNode, "<icon>", new XMLUtils.ElementVisitor()
    {
      private final String val$source;
      private final URL val$codebase;
      private final ArrayList val$answer;
      
      public void visitElement(XMLNode paramAnonymousXMLNode)
        throws MissingFieldException, BadFieldException
      {
        String str1 = XMLUtils.getAttribute(paramAnonymousXMLNode, "", "kind", "");
        URL localURL = XMLUtils.getRequiredURL(this.val$source, this.val$codebase, paramAnonymousXMLNode, "", "href");
        
        String str2 = XMLUtils.getAttribute(paramAnonymousXMLNode, "", "version", null);
        
        int i = XMLUtils.getIntAttribute(this.val$source, paramAnonymousXMLNode, "", "height", 0);
        
        int j = XMLUtils.getIntAttribute(this.val$source, paramAnonymousXMLNode, "", "width", 0);
        
        int k = XMLUtils.getIntAttribute(this.val$source, paramAnonymousXMLNode, "", "depth", 0);
        
        int m = 0;
        if (str1.equals("selected")) {
          m = 1;
        } else if (str1.equals("disabled")) {
          m = 2;
        } else if (str1.equals("rollover")) {
          m = 3;
        } else if (str1.equals("splash")) {
          m = 4;
        }
        this.val$answer.add(new IconDesc(localURL, str2, i, j, k, m));
      }
    });
    return (IconDesc[])localArrayList.toArray(new IconDesc[localArrayList.size()]);
  }
  
  private static ShortcutDesc getShortcutDesc(XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    ArrayList localArrayList = new ArrayList();
    
    XMLUtils.visitElements(paramXMLNode, "<shortcut>", new XMLUtils.ElementVisitor()
    {
      private final ArrayList val$shortcuts;
      
      public void visitElement(XMLNode paramAnonymousXMLNode)
        throws MissingFieldException, BadFieldException
      {
        String str1 = XMLUtils.getAttribute(paramAnonymousXMLNode, "", "online", "true");
        
        boolean bool1 = str1.equalsIgnoreCase("true");
        boolean bool2 = XMLUtils.isElementPath(paramAnonymousXMLNode, "<desktop>");
        
        boolean bool3 = XMLUtils.isElementPath(paramAnonymousXMLNode, "<menu>");
        
        String str2 = XMLUtils.getAttribute(paramAnonymousXMLNode, "<menu>", "submenu");
        
        this.val$shortcuts.add(new ShortcutDesc(bool1, bool2, bool3, str2));
      }
    });
    if (localArrayList.size() > 0) {
      return (ShortcutDesc)localArrayList.get(0);
    }
    return null;
  }
  
  private static AssociationDesc[] getAssociationDesc(String paramString, XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    ArrayList localArrayList = new ArrayList();
    XMLUtils.visitElements(paramXMLNode, "<association>", new XMLUtils.ElementVisitor()
    {
      private final String val$source;
      private final ArrayList val$answer;
      
      public void visitElement(XMLNode paramAnonymousXMLNode)
        throws MissingFieldException, BadFieldException
      {
        String str1 = XMLUtils.getRequiredAttribute(this.val$source, paramAnonymousXMLNode, "", "extensions");
        
        String str2 = XMLUtils.getRequiredAttribute(this.val$source, paramAnonymousXMLNode, "", "mime-type");
        
        this.val$answer.add(new AssociationDesc(str1, str2));
      }
    });
    return (AssociationDesc[])localArrayList.toArray(new AssociationDesc[localArrayList.size()]);
  }
  
  private static RContentDesc[] getRContentDescs(String paramString, URL paramURL, XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    ArrayList localArrayList = new ArrayList();
    XMLUtils.visitElements(paramXMLNode, "<related-content>", new XMLUtils.ElementVisitor()
    {
      private final String val$source;
      private final URL val$codebase;
      private final ArrayList val$answer;
      
      public void visitElement(XMLNode paramAnonymousXMLNode)
        throws MissingFieldException, BadFieldException
      {
        URL localURL1 = XMLUtils.getRequiredURL(this.val$source, this.val$codebase, paramAnonymousXMLNode, "", "href");
        
        String str1 = XMLUtils.getElementContents(paramAnonymousXMLNode, "<title>");
        String str2 = XMLUtils.getElementContents(paramAnonymousXMLNode, "<description>");
        
        URL localURL2 = XMLUtils.getAttributeURL(this.val$source, this.val$codebase, paramAnonymousXMLNode, "<icon>", "href");
        
        this.val$answer.add(new RContentDesc(localURL1, str1, str2, localURL2));
      }
    });
    return (RContentDesc[])localArrayList.toArray(new RContentDesc[localArrayList.size()]);
  }
  
  private static void handleResourceElement(String paramString, URL paramURL, XMLNode paramXMLNode, ResourcesDesc paramResourcesDesc, boolean paramBoolean)
    throws MissingFieldException, BadFieldException
  {
    String str1 = paramXMLNode.getName();
    Object localObject1;
    Object localObject2;
    String str2;
    String str4;
    Object localObject4;
    if ((str1.equals("jar")) || (str1.equals("nativelib")))
    {
      localObject1 = XMLUtils.getRequiredURL(paramString, paramURL, paramXMLNode, "", "href");
      localObject2 = XMLUtils.getAttribute(paramXMLNode, "", "version", null);
      str2 = XMLUtils.getAttribute(paramXMLNode, "", "download");
      String str3 = XMLUtils.getAttribute(paramXMLNode, "", "main");
      str4 = XMLUtils.getAttribute(paramXMLNode, "", "part");
      int i = XMLUtils.getIntAttribute(paramString, paramXMLNode, "", "size", 0);
      boolean bool2 = str1.equals("nativelib");
      boolean bool3 = false;
      boolean bool4 = false;
      if ("lazy".equalsIgnoreCase(str2)) {
        bool3 = true;
      }
      if ("true".equalsIgnoreCase(str3)) {
        bool4 = true;
      }
      localObject4 = new JARDesc((URL)localObject1, (String)localObject2, bool3, bool4, bool2, str4, i, paramResourcesDesc);
      
      paramResourcesDesc.addResource((ResourceType)localObject4);
    }
    else if (str1.equals("property"))
    {
      localObject1 = XMLUtils.getRequiredAttribute(paramString, paramXMLNode, "", "name");
      localObject2 = XMLUtils.getRequiredAttributeEmptyOK(paramString, paramXMLNode, "", "value");
      
      paramResourcesDesc.addResource(new PropertyDesc((String)localObject1, (String)localObject2));
    }
    else if (str1.equals("package"))
    {
      localObject1 = XMLUtils.getRequiredAttribute(paramString, paramXMLNode, "", "name");
      localObject2 = XMLUtils.getRequiredAttribute(paramString, paramXMLNode, "", "part");
      str2 = XMLUtils.getAttribute(paramXMLNode, "", "recursive", "false");
      boolean bool1 = "true".equals(str2);
      paramResourcesDesc.addResource(new PackageDesc((String)localObject1, (String)localObject2, bool1));
    }
    else
    {
      Object localObject3;
      if (str1.equals("extension"))
      {
        localObject1 = XMLUtils.getAttribute(paramXMLNode, "", "name");
        localObject2 = XMLUtils.getRequiredURL(paramString, paramURL, paramXMLNode, "", "href");
        str2 = XMLUtils.getAttribute(paramXMLNode, "", "version", null);
        
        localObject3 = getExtDownloadDescs(paramString, paramXMLNode);
        paramResourcesDesc.addResource(new ExtensionDesc((String)localObject1, (URL)localObject2, str2, (ExtDownloadDesc[])localObject3));
      }
      else if ((str1.equals("j2se")) && (!paramBoolean))
      {
        localObject1 = XMLUtils.getRequiredAttribute(paramString, paramXMLNode, "", "version");
        
        localObject2 = XMLUtils.getAttributeURL(paramString, paramURL, paramXMLNode, "", "href");
        
        str2 = XMLUtils.getAttribute(paramXMLNode, "", "initial-heap-size");
        
        localObject3 = XMLUtils.getAttribute(paramXMLNode, "", "max-heap-size");
        
        str4 = XMLUtils.getAttribute(paramXMLNode, "", "java-vm-args");
        
        long l1 = -1L;
        long l2 = -1L;
        l1 = GeneralUtil.heapValToLong(str2);
        l2 = GeneralUtil.heapValToLong((String)localObject3);
        
        localObject4 = buildResourcesDesc(paramString, paramURL, paramXMLNode, true);
        
        JREDesc localJREDesc = new JREDesc((String)localObject1, l1, l2, str4, (URL)localObject2, (ResourcesDesc)localObject4);
        
        paramResourcesDesc.addResource(localJREDesc);
      }
    }
  }
  
  private static ExtDownloadDesc[] getExtDownloadDescs(String paramString, XMLNode paramXMLNode)
    throws BadFieldException, MissingFieldException
  {
    ArrayList localArrayList = new ArrayList();
    
    XMLUtils.visitElements(paramXMLNode, "<ext-download>", new XMLUtils.ElementVisitor()
    {
      private final String val$source;
      private final ArrayList val$al;
      
      public void visitElement(XMLNode paramAnonymousXMLNode)
        throws MissingFieldException
      {
        String str1 = XMLUtils.getRequiredAttribute(this.val$source, paramAnonymousXMLNode, "", "ext-part");
        String str2 = XMLUtils.getAttribute(paramAnonymousXMLNode, "", "part");
        String str3 = XMLUtils.getAttribute(paramAnonymousXMLNode, "", "download", "eager");
        boolean bool = "lazy".equals(str3);
        this.val$al.add(new ExtDownloadDesc(str1, str2, bool));
      }
    });
    ExtDownloadDesc[] arrayOfExtDownloadDesc = new ExtDownloadDesc[localArrayList.size()];
    return (ExtDownloadDesc[])localArrayList.toArray(arrayOfExtDownloadDesc);
  }
  
  private static ApplicationDesc buildApplicationDesc(String paramString, XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    String str = XMLUtils.getAttribute(paramXMLNode, "<application-desc>", "main-class");
    
    ArrayList localArrayList = new ArrayList();
    XMLUtils.visitElements(paramXMLNode, "<application-desc><argument>", new XMLUtils.ElementVisitor()
    {
      private final String val$source;
      private final ArrayList val$al1;
      
      public void visitElement(XMLNode paramAnonymousXMLNode)
        throws MissingFieldException, BadFieldException
      {
        String str = XMLUtils.getElementContents(paramAnonymousXMLNode, "", null);
        if (str == null) {
          throw new BadFieldException(this.val$source, XMLUtils.getPathString(paramAnonymousXMLNode), "");
        }
        this.val$al1.add(str);
      }
    });
    String[] arrayOfString = new String[localArrayList.size()];
    arrayOfString = (String[])localArrayList.toArray(arrayOfString);
    
    return new ApplicationDesc(str, arrayOfString);
  }
  
  private static LibraryDesc buildLibraryDesc(String paramString, XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    String str = XMLUtils.getAttribute(paramXMLNode, "<component-desc>", "unique-id");
    
    return new LibraryDesc(str);
  }
  
  private static InstallerDesc buildInstallerDesc(String paramString, URL paramURL, XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    String str = XMLUtils.getAttribute(paramXMLNode, "<installer-desc>", "main-class");
    return new InstallerDesc(str);
  }
  
  private static AppletDesc buildAppletDesc(String paramString, URL paramURL, XMLNode paramXMLNode)
    throws MissingFieldException, BadFieldException
  {
    String str1 = XMLUtils.getRequiredAttribute(paramString, paramXMLNode, "<applet-desc>", "main-class");
    String str2 = XMLUtils.getRequiredAttribute(paramString, paramXMLNode, "<applet-desc>", "name");
    URL localURL = XMLUtils.getAttributeURL(paramString, paramURL, paramXMLNode, "<applet-desc>", "documentbase");
    int i = XMLUtils.getRequiredIntAttribute(paramString, paramXMLNode, "<applet-desc>", "width");
    int j = XMLUtils.getRequiredIntAttribute(paramString, paramXMLNode, "<applet-desc>", "height");
    if (i <= 0) {
      throw new BadFieldException(paramString, XMLUtils.getPathString(paramXMLNode) + "<applet-desc>width", new Integer(i).toString());
    }
    if (j <= 0) {
      throw new BadFieldException(paramString, XMLUtils.getPathString(paramXMLNode) + "<applet-desc>height", new Integer(j).toString());
    }
    Properties localProperties = new Properties();
    
    XMLUtils.visitElements(paramXMLNode, "<applet-desc><param>", new XMLUtils.ElementVisitor()
    {
      private final String val$source;
      private final Properties val$params;
      
      public void visitElement(XMLNode paramAnonymousXMLNode)
        throws MissingFieldException, BadFieldException
      {
        String str1 = XMLUtils.getRequiredAttribute(this.val$source, paramAnonymousXMLNode, "", "name");
        
        String str2 = XMLUtils.getRequiredAttributeEmptyOK(this.val$source, paramAnonymousXMLNode, "", "value");
        
        this.val$params.setProperty(str1, str2);
      }
    });
    return new AppletDesc(str2, str1, localURL, i, j, localProperties);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\jnl\XMLFormat.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */