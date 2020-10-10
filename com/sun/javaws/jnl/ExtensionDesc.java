package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import java.net.URL;
import java.util.HashSet;

public class ExtensionDesc
  implements ResourceType
{
  private String _name;
  private URL _location;
  private String _version;
  private boolean _isInstaller;
  private ExtDownloadDesc[] _extDownloadDescs;
  private LaunchDesc _extensionDefDesc;
  
  public ExtensionDesc(String paramString1, URL paramURL, String paramString2, ExtDownloadDesc[] paramArrayOfExtDownloadDesc)
  {
    this._location = paramURL;
    this._version = paramString2;
    this._name = paramString1;
    if (paramArrayOfExtDownloadDesc == null) {
      paramArrayOfExtDownloadDesc = new ExtDownloadDesc[0];
    }
    this._extDownloadDescs = paramArrayOfExtDownloadDesc;
    this._extensionDefDesc = null;
    this._isInstaller = false;
  }
  
  public void setInstaller(boolean paramBoolean)
  {
    this._isInstaller = paramBoolean;
  }
  
  public boolean isInstaller()
  {
    return this._isInstaller;
  }
  
  public String getVersion()
  {
    return this._version;
  }
  
  public URL getLocation()
  {
    return this._location;
  }
  
  public String getName()
  {
    return this._name;
  }
  
  ExtDownloadDesc[] getExtDownloadDescs()
  {
    return this._extDownloadDescs;
  }
  
  public LaunchDesc getExtensionDesc()
  {
    return this._extensionDefDesc;
  }
  
  public void setExtensionDesc(LaunchDesc paramLaunchDesc)
  {
    this._extensionDefDesc = paramLaunchDesc;
  }
  
  ResourcesDesc getExtensionResources()
  {
    return this._extensionDefDesc.getResources();
  }
  
  HashSet getExtensionPackages(HashSet paramHashSet, boolean paramBoolean)
  {
    HashSet localHashSet = new HashSet();
    for (int i = 0; i < this._extDownloadDescs.length; i++)
    {
      ExtDownloadDesc localExtDownloadDesc = this._extDownloadDescs[i];
      
      int j = (paramBoolean) && (!localExtDownloadDesc.isLazy()) ? 1 : 0;
      if ((j != 0) || ((paramHashSet != null) && (paramHashSet.contains(localExtDownloadDesc.getPart())))) {
        localHashSet.add(localExtDownloadDesc.getExtensionPart());
      }
    }
    return localHashSet;
  }
  
  public void visit(ResourceVisitor paramResourceVisitor)
  {
    paramResourceVisitor.visitExtensionDesc(this);
  }
  
  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("href", this._location);
    localXMLAttributeBuilder.add("version", this._version);
    localXMLAttributeBuilder.add("name", this._name);
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("extension", localXMLAttributeBuilder.getAttributeList());
    for (int i = 0; i < this._extDownloadDescs.length; i++) {
      localXMLNodeBuilder.add(this._extDownloadDescs[i]);
    }
    return localXMLNodeBuilder.getNode();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\jnl\ExtensionDesc.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */