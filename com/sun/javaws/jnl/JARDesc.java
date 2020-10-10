package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.javaws.Globals;
import java.net.URL;

public class JARDesc
  implements ResourceType
{
  private URL _location;
  private String _version;
  private int _size;
  private boolean _isNativeLib;
  private boolean _isLazyDownload;
  private boolean _isMainFile;
  private String _part;
  private ResourcesDesc _parent;
  
  public JARDesc(URL paramURL, String paramString1, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, String paramString2, int paramInt, ResourcesDesc paramResourcesDesc)
  {
    this._location = paramURL;
    this._version = paramString1;
    this._isLazyDownload = ((paramBoolean1) && (!this._isMainFile) && (!Globals.isImportMode()));
    
    this._isNativeLib = paramBoolean3;
    this._isMainFile = paramBoolean2;
    this._part = paramString2;
    this._size = paramInt;
    this._parent = paramResourcesDesc;
  }
  
  public boolean isNativeLib()
  {
    return this._isNativeLib;
  }
  
  public boolean isJavaFile()
  {
    return !this._isNativeLib;
  }
  
  public URL getLocation()
  {
    return this._location;
  }
  
  public String getVersion()
  {
    return this._version;
  }
  
  public boolean isLazyDownload()
  {
    return this._isLazyDownload;
  }
  
  public boolean isMainJarFile()
  {
    return this._isMainFile;
  }
  
  public String getPartName()
  {
    return this._part;
  }
  
  public int getSize()
  {
    return this._size;
  }
  
  public ResourcesDesc getParent()
  {
    return this._parent;
  }
  
  public void visit(ResourceVisitor paramResourceVisitor)
  {
    paramResourceVisitor.visitJARDesc(this);
  }
  
  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("href", this._location);
    localXMLAttributeBuilder.add("version", this._version);
    localXMLAttributeBuilder.add("part", this._part);
    localXMLAttributeBuilder.add("download", isLazyDownload() ? "lazy" : "eager");
    localXMLAttributeBuilder.add("main", isMainJarFile() ? "true" : "false");
    String str = this._isNativeLib ? "nativelib" : "jar";
    return new XMLNode("jar", localXMLAttributeBuilder.getAttributeList());
  }
  
  public String toString()
  {
    return "JARDesc[" + this._location + ":" + this._version;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\jnl\JARDesc.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */