package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import com.sun.deploy.xml.XMLable;
import com.sun.javaws.exceptions.JNLPSigningException;
import java.net.URL;

public class LaunchDesc
  implements XMLable
{
  private String _specVersion;
  private String _version;
  private URL _home;
  private URL _codebase;
  private InformationDesc _information;
  private int _securiyModel;
  private ResourcesDesc _resources;
  private int _launchType;
  private ApplicationDesc _applicationDesc;
  private AppletDesc _appletDesc;
  private LibraryDesc _libraryDesc;
  private InstallerDesc _installerDesc;
  private String _internalCommand;
  private String _source;
  private boolean _propsSet = false;
  private byte[] _bits;
  public static final int SANDBOX_SECURITY = 0;
  public static final int ALLPERMISSIONS_SECURITY = 1;
  public static final int J2EE_APP_CLIENT_SECURITY = 2;
  public static final int APPLICATION_DESC_TYPE = 1;
  public static final int APPLET_DESC_TYPE = 2;
  public static final int LIBRARY_DESC_TYPE = 3;
  public static final int INSTALLER_DESC_TYPE = 4;
  public static final int INTERNAL_TYPE = 5;
  
  public LaunchDesc(String paramString1, URL paramURL1, URL paramURL2, String paramString2, InformationDesc paramInformationDesc, int paramInt1, ResourcesDesc paramResourcesDesc, int paramInt2, ApplicationDesc paramApplicationDesc, AppletDesc paramAppletDesc, LibraryDesc paramLibraryDesc, InstallerDesc paramInstallerDesc, String paramString3, String paramString4, byte[] paramArrayOfByte)
  {
    this._specVersion = paramString1;
    this._version = paramString2;
    this._codebase = paramURL1;
    this._home = paramURL2;
    this._information = paramInformationDesc;
    this._securiyModel = paramInt1;
    this._resources = paramResourcesDesc;
    this._launchType = paramInt2;
    this._applicationDesc = paramApplicationDesc;
    this._appletDesc = paramAppletDesc;
    this._libraryDesc = paramLibraryDesc;
    this._installerDesc = paramInstallerDesc;
    this._internalCommand = paramString3;
    this._source = paramString4;
    this._bits = paramArrayOfByte;
    if (this._resources != null) {
      this._resources.setParent(this);
    }
  }
  
  public String getSpecVersion()
  {
    return this._specVersion;
  }
  
  public synchronized URL getCodebase()
  {
    return this._codebase;
  }
  
  public byte[] getBytes()
  {
    return this._bits;
  }
  
  public synchronized URL getLocation()
  {
    return this._home;
  }
  
  public synchronized URL getCanonicalHome()
  {
    if ((this._home == null) && (this._resources != null))
    {
      JARDesc localJARDesc = this._resources.getMainJar(true);
      return localJARDesc != null ? localJARDesc.getLocation() : null;
    }
    return this._home;
  }
  
  public InformationDesc getInformation()
  {
    return this._information;
  }
  
  public String getInternalCommand()
  {
    return this._internalCommand;
  }
  
  public int getSecurityModel()
  {
    return this._securiyModel;
  }
  
  public ResourcesDesc getResources()
  {
    return this._resources;
  }
  
  public boolean arePropsSet()
  {
    return this._propsSet;
  }
  
  public void setPropsSet(boolean paramBoolean)
  {
    this._propsSet = paramBoolean;
  }
  
  public int getLaunchType()
  {
    return this._launchType;
  }
  
  public ApplicationDesc getApplicationDescriptor()
  {
    return this._applicationDesc;
  }
  
  public AppletDesc getAppletDescriptor()
  {
    return this._appletDesc;
  }
  
  public InstallerDesc getInstallerDescriptor()
  {
    return this._installerDesc;
  }
  
  public boolean isApplication()
  {
    return this._launchType == 1;
  }
  
  public boolean isApplet()
  {
    return this._launchType == 2;
  }
  
  public boolean isLibrary()
  {
    return this._launchType == 3;
  }
  
  public boolean isInstaller()
  {
    return this._launchType == 4;
  }
  
  public boolean isApplicationDescriptor()
  {
    return (isApplication()) || (isApplet());
  }
  
  public boolean isHttps()
  {
    return this._codebase.getProtocol().equals("https");
  }
  
  public String getSource()
  {
    return this._source;
  }
  
  public void checkSigning(LaunchDesc paramLaunchDesc)
    throws JNLPSigningException
  {
    if (!paramLaunchDesc.getSource().equals(getSource())) {
      throw new JNLPSigningException(this, paramLaunchDesc.getSource());
    }
  }
  
  public boolean isJRESpecified()
  {
    boolean[] arrayOfBoolean1 = new boolean[1];
    boolean[] arrayOfBoolean2 = new boolean[1];
    if (getResources() != null) {
      getResources().visit(new ResourceVisitor()
      {
        private final boolean[] val$needJre;
        private final boolean[] val$hasJre;
        
        public void visitJARDesc(JARDesc paramAnonymousJARDesc)
        {
          this.val$needJre[0] = true;
        }
        
        public void visitPropertyDesc(PropertyDesc paramAnonymousPropertyDesc) {}
        
        public void visitPackageDesc(PackageDesc paramAnonymousPackageDesc) {}
        
        public void visitExtensionDesc(ExtensionDesc paramAnonymousExtensionDesc)
        {
          this.val$needJre[0] = true;
        }
        
        public void visitJREDesc(JREDesc paramAnonymousJREDesc)
        {
          this.val$hasJre[0] = true;
        }
      });
    }
    if ((this._launchType == 1) || (this._launchType == 2)) {
      arrayOfBoolean2[0] = true;
    }
    return (arrayOfBoolean1[0] != 0) || (arrayOfBoolean2[0] == 0);
  }
  
  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("spec", this._specVersion);
    localXMLAttributeBuilder.add("codebase", this._codebase);
    localXMLAttributeBuilder.add("version", this._version);
    localXMLAttributeBuilder.add("href", this._home);
    
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("jnlp", localXMLAttributeBuilder.getAttributeList());
    localXMLNodeBuilder.add(this._information);
    if (this._securiyModel == 1) {
      localXMLNodeBuilder.add(new XMLNode("security", null, new XMLNode("all-permissions", null), null));
    } else if (this._securiyModel == 2) {
      localXMLNodeBuilder.add(new XMLNode("security", null, new XMLNode("j2ee-application-client-permissions", null), null));
    }
    localXMLNodeBuilder.add(this._resources);
    localXMLNodeBuilder.add(this._applicationDesc);
    localXMLNodeBuilder.add(this._appletDesc);
    localXMLNodeBuilder.add(this._libraryDesc);
    localXMLNodeBuilder.add(this._installerDesc);
    return localXMLNodeBuilder.getNode();
  }
  
  public String toString()
  {
    return asXML().toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\jnl\LaunchDesc.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */