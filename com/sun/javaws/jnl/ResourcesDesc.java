package com.sun.javaws.jnl;

import com.sun.deploy.config.JREInfo;
import com.sun.deploy.util.Trace;
import com.sun.deploy.util.URLUtil;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import com.sun.javaws.cache.DiskCacheEntry;
import com.sun.javaws.cache.DownloadProtocol;
import com.sun.javaws.util.VersionString;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

public class ResourcesDesc
  implements ResourceType
{
  private ArrayList _list = null;
  private LaunchDesc _parent = null;
  
  public ResourcesDesc()
  {
    this._list = new ArrayList();
  }
  
  public LaunchDesc getParent()
  {
    return this._parent;
  }
  
  void setParent(LaunchDesc paramLaunchDesc)
  {
    this._parent = paramLaunchDesc;
    for (int i = 0; i < this._list.size(); i++)
    {
      Object localObject = this._list.get(i);
      if ((localObject instanceof JREDesc))
      {
        JREDesc localJREDesc = (JREDesc)localObject;
        if (localJREDesc.getNestedResources() != null) {
          localJREDesc.getNestedResources().setParent(paramLaunchDesc);
        }
      }
    }
  }
  
  void addResource(ResourceType paramResourceType)
  {
    if (paramResourceType != null) {
      this._list.add(paramResourceType);
    }
  }
  
  boolean isEmpty()
  {
    return this._list.isEmpty();
  }
  
  public JREDesc getSelectedJRE()
  {
    for (int i = 0; i < this._list.size(); i++)
    {
      Object localObject = this._list.get(i);
      if (((localObject instanceof JREDesc)) && (((JREDesc)localObject).isSelected())) {
        return (JREDesc)localObject;
      }
    }
    return null;
  }
  
  public JARDesc[] getLocalJarDescs()
  {
    ArrayList localArrayList = new ArrayList(this._list.size());
    for (int i = 0; i < this._list.size(); i++)
    {
      Object localObject = this._list.get(i);
      if ((localObject instanceof JARDesc)) {
        localArrayList.add(localObject);
      }
    }
    return toJARDescArray(localArrayList);
  }
  
  public ExtensionDesc[] getExtensionDescs()
  {
    ArrayList localArrayList = new ArrayList();
    ExtensionDesc[] arrayOfExtensionDesc = new ExtensionDesc[0];
    visit(new ResourceVisitor()
    {
      private final ArrayList val$l;
      
      public void visitJARDesc(JARDesc paramAnonymousJARDesc) {}
      
      public void visitPropertyDesc(PropertyDesc paramAnonymousPropertyDesc) {}
      
      public void visitExtensionDesc(ExtensionDesc paramAnonymousExtensionDesc)
      {
        this.val$l.add(paramAnonymousExtensionDesc);
      }
      
      public void visitJREDesc(JREDesc paramAnonymousJREDesc) {}
      
      public void visitPackageDesc(PackageDesc paramAnonymousPackageDesc) {}
    });
    return (ExtensionDesc[])localArrayList.toArray(arrayOfExtensionDesc);
  }
  
  public JARDesc[] getEagerOrAllJarDescs(boolean paramBoolean)
  {
    HashSet localHashSet = new HashSet();
    if (!paramBoolean) {
      visit(new ResourceVisitor()
      {
        private final HashSet val$eagerParts;
        
        public void visitJARDesc(JARDesc paramAnonymousJARDesc)
        {
          if ((!paramAnonymousJARDesc.isLazyDownload()) && (paramAnonymousJARDesc.getPartName() != null)) {
            this.val$eagerParts.add(paramAnonymousJARDesc.getPartName());
          }
        }
        
        public void visitPropertyDesc(PropertyDesc paramAnonymousPropertyDesc) {}
        
        public void visitExtensionDesc(ExtensionDesc paramAnonymousExtensionDesc) {}
        
        public void visitJREDesc(JREDesc paramAnonymousJREDesc) {}
        
        public void visitPackageDesc(PackageDesc paramAnonymousPackageDesc) {}
      });
    }
    ArrayList localArrayList = new ArrayList();
    addJarsToList(localArrayList, localHashSet, paramBoolean, true);
    return toJARDescArray(localArrayList);
  }
  
  private void addJarsToList(ArrayList paramArrayList, HashSet paramHashSet, boolean paramBoolean1, boolean paramBoolean2)
  {
    visit(new ResourceVisitor()
    {
      private final boolean val$includeAll;
      private final boolean val$includeEager;
      private final HashSet val$includeParts;
      private final ArrayList val$list;
      
      public void visitJARDesc(JARDesc paramAnonymousJARDesc)
      {
        if ((this.val$includeAll) || ((this.val$includeEager) && (!paramAnonymousJARDesc.isLazyDownload())) || (this.val$includeParts.contains(paramAnonymousJARDesc.getPartName()))) {
          this.val$list.add(paramAnonymousJARDesc);
        }
      }
      
      public void visitPropertyDesc(PropertyDesc paramAnonymousPropertyDesc) {}
      
      public void visitExtensionDesc(ExtensionDesc paramAnonymousExtensionDesc)
      {
        HashSet localHashSet = paramAnonymousExtensionDesc.getExtensionPackages(this.val$includeParts, this.val$includeEager);
        Object localObject;
        if (paramAnonymousExtensionDesc.getExtensionDesc() == null)
        {
          localObject = JREInfo.getKnownPlatforms();
          DiskCacheEntry localDiskCacheEntry = null;
          try
          {
            localDiskCacheEntry = DownloadProtocol.getCachedExtension(paramAnonymousExtensionDesc.getLocation(), paramAnonymousExtensionDesc.getVersion(), (String)localObject);
            if ((localDiskCacheEntry != null) && (localDiskCacheEntry.getFile() != null))
            {
              LaunchDesc localLaunchDesc = LaunchDescFactory.buildDescriptor(localDiskCacheEntry.getFile());
              paramAnonymousExtensionDesc.setExtensionDesc(localLaunchDesc);
            }
          }
          catch (Exception localException)
          {
            Trace.ignoredException(localException);
          }
        }
        if (paramAnonymousExtensionDesc.getExtensionDesc() != null)
        {
          localObject = paramAnonymousExtensionDesc.getExtensionDesc().getResources();
          if (localObject != null) {
            ((ResourcesDesc)localObject).addJarsToList(this.val$list, localHashSet, this.val$includeAll, this.val$includeEager);
          }
        }
      }
      
      public void visitJREDesc(JREDesc paramAnonymousJREDesc)
      {
        if (paramAnonymousJREDesc.isSelected())
        {
          ResourcesDesc localResourcesDesc1 = paramAnonymousJREDesc.getNestedResources();
          if (localResourcesDesc1 != null) {
            localResourcesDesc1.addJarsToList(this.val$list, this.val$includeParts, this.val$includeAll, this.val$includeEager);
          }
          if (paramAnonymousJREDesc.getExtensionDesc() != null)
          {
            ResourcesDesc localResourcesDesc2 = paramAnonymousJREDesc.getExtensionDesc().getResources();
            if (localResourcesDesc2 != null) {
              localResourcesDesc2.addJarsToList(this.val$list, new HashSet(), this.val$includeAll, this.val$includeEager);
            }
          }
        }
      }
      
      public void visitPackageDesc(PackageDesc paramAnonymousPackageDesc) {}
    });
  }
  
  public JARDesc[] getPartJars(String[] paramArrayOfString)
  {
    HashSet localHashSet = new HashSet();
    for (int i = 0; i < paramArrayOfString.length; i++) {
      localHashSet.add(paramArrayOfString[i]);
    }
    ArrayList localArrayList = new ArrayList();
    addJarsToList(localArrayList, localHashSet, false, false);
    return toJARDescArray(localArrayList);
  }
  
  public JARDesc[] getPartJars(String paramString)
  {
    return getPartJars(new String[] { paramString });
  }
  
  public JARDesc[] getResource(URL paramURL, String paramString)
  {
    VersionString localVersionString = paramString != null ? new VersionString(paramString) : null;
    JARDesc[] arrayOfJARDesc = new JARDesc[1];
    
    visit(new ResourceVisitor()
    {
      private final URL val$location;
      private final VersionString val$vs;
      private final JARDesc[] val$resources;
      
      public void visitJARDesc(JARDesc paramAnonymousJARDesc)
      {
        if (URLUtil.equals(paramAnonymousJARDesc.getLocation(), this.val$location)) {
          if (this.val$vs == null) {
            this.val$resources[0] = paramAnonymousJARDesc;
          } else if (this.val$vs.contains(paramAnonymousJARDesc.getVersion())) {
            this.val$resources[0] = paramAnonymousJARDesc;
          }
        }
      }
      
      public void visitPropertyDesc(PropertyDesc paramAnonymousPropertyDesc) {}
      
      public void visitExtensionDesc(ExtensionDesc paramAnonymousExtensionDesc) {}
      
      public void visitJREDesc(JREDesc paramAnonymousJREDesc) {}
      
      public void visitPackageDesc(PackageDesc paramAnonymousPackageDesc) {}
    });
    if (arrayOfJARDesc[0] == null) {
      return null;
    }
    if (arrayOfJARDesc[0].getPartName() != null) {
      return getPartJars(arrayOfJARDesc[0].getPartName());
    }
    return arrayOfJARDesc;
  }
  
  public JARDesc[] getExtensionPart(URL paramURL, String paramString, String[] paramArrayOfString)
  {
    ExtensionDesc localExtensionDesc = findExtension(paramURL, paramString);
    if (localExtensionDesc == null) {
      return null;
    }
    ResourcesDesc localResourcesDesc = localExtensionDesc.getExtensionResources();
    if (localResourcesDesc == null) {
      return null;
    }
    return localResourcesDesc.getPartJars(paramArrayOfString);
  }
  
  private ExtensionDesc findExtension(URL paramURL, String paramString)
  {
    ExtensionDesc[] arrayOfExtensionDesc = new ExtensionDesc[1];
    
    visit(new ResourceVisitor()
    {
      private final ExtensionDesc[] val$ea;
      private final URL val$location;
      private final String val$version;
      
      public void visitJARDesc(JARDesc paramAnonymousJARDesc) {}
      
      public void visitPropertyDesc(PropertyDesc paramAnonymousPropertyDesc) {}
      
      public void visitExtensionDesc(ExtensionDesc paramAnonymousExtensionDesc)
      {
        if (this.val$ea[0] == null) {
          if ((URLUtil.equals(paramAnonymousExtensionDesc.getLocation(), this.val$location)) && ((this.val$version == null) || (new VersionString(this.val$version).contains(paramAnonymousExtensionDesc.getVersion()))))
          {
            this.val$ea[0] = paramAnonymousExtensionDesc;
          }
          else
          {
            LaunchDesc localLaunchDesc = paramAnonymousExtensionDesc.getExtensionDesc();
            if ((localLaunchDesc != null) && (localLaunchDesc.getResources() != null)) {
              this.val$ea[0] = localLaunchDesc.getResources().findExtension(this.val$location, this.val$version);
            }
          }
        }
      }
      
      public void visitJREDesc(JREDesc paramAnonymousJREDesc) {}
      
      public void visitPackageDesc(PackageDesc paramAnonymousPackageDesc) {}
    });
    return arrayOfExtensionDesc[0];
  }
  
  public JARDesc getMainJar(boolean paramBoolean)
  {
    JARDesc[] arrayOfJARDesc = new JARDesc[2];
    visit(new ResourceVisitor()
    {
      private final JARDesc[] val$results;
      
      public void visitJARDesc(JARDesc paramAnonymousJARDesc)
      {
        if (paramAnonymousJARDesc.isJavaFile())
        {
          if (this.val$results[0] == null) {
            this.val$results[0] = paramAnonymousJARDesc;
          }
          if (paramAnonymousJARDesc.isMainJarFile()) {
            this.val$results[1] = paramAnonymousJARDesc;
          }
        }
      }
      
      public void visitPropertyDesc(PropertyDesc paramAnonymousPropertyDesc) {}
      
      public void visitExtensionDesc(ExtensionDesc paramAnonymousExtensionDesc) {}
      
      public void visitJREDesc(JREDesc paramAnonymousJREDesc) {}
      
      public void visitPackageDesc(PackageDesc paramAnonymousPackageDesc) {}
    });
    JARDesc localJARDesc1 = arrayOfJARDesc[0];
    JARDesc localJARDesc2 = arrayOfJARDesc[1];
    return (localJARDesc2 != null) && (paramBoolean) ? localJARDesc2 : localJARDesc1;
  }
  
  public JARDesc[] getPart(String paramString)
  {
    ArrayList localArrayList = new ArrayList();
    visit(new ResourceVisitor()
    {
      private final String val$name;
      private final ArrayList val$l;
      
      public void visitJARDesc(JARDesc paramAnonymousJARDesc)
      {
        if (this.val$name.equals(paramAnonymousJARDesc.getPartName())) {
          this.val$l.add(paramAnonymousJARDesc);
        }
      }
      
      public void visitPropertyDesc(PropertyDesc paramAnonymousPropertyDesc) {}
      
      public void visitExtensionDesc(ExtensionDesc paramAnonymousExtensionDesc) {}
      
      public void visitJREDesc(JREDesc paramAnonymousJREDesc) {}
      
      public void visitPackageDesc(PackageDesc paramAnonymousPackageDesc) {}
    });
    return toJARDescArray(localArrayList);
  }
  
  public JARDesc[] getExtensionPart(URL paramURL, String paramString1, String paramString2)
  {
    JARDesc[][] arrayOfJARDesc = new JARDesc[1][];
    visit(new ResourceVisitor()
    {
      private final URL val$url;
      private final String val$version;
      private final JARDesc[][] val$jdss;
      private final String val$part;
      
      public void visitJARDesc(JARDesc paramAnonymousJARDesc) {}
      
      public void visitPropertyDesc(PropertyDesc paramAnonymousPropertyDesc) {}
      
      public void visitExtensionDesc(ExtensionDesc paramAnonymousExtensionDesc)
      {
        if (URLUtil.equals(paramAnonymousExtensionDesc.getLocation(), this.val$url)) {
          if (this.val$version == null)
          {
            if ((paramAnonymousExtensionDesc.getVersion() == null) && (paramAnonymousExtensionDesc.getExtensionResources() != null)) {
              this.val$jdss[0] = paramAnonymousExtensionDesc.getExtensionResources().getPart(this.val$part);
            }
          }
          else if ((this.val$version.equals(paramAnonymousExtensionDesc.getVersion())) && 
            (paramAnonymousExtensionDesc.getExtensionResources() != null)) {
            this.val$jdss[0] = paramAnonymousExtensionDesc.getExtensionResources().getPart(this.val$part);
          }
        }
      }
      
      public void visitJREDesc(JREDesc paramAnonymousJREDesc) {}
      
      public void visitPackageDesc(PackageDesc paramAnonymousPackageDesc) {}
    });
    return arrayOfJARDesc[0];
  }
  
  private JARDesc[] toJARDescArray(ArrayList paramArrayList)
  {
    JARDesc[] arrayOfJARDesc = new JARDesc[paramArrayList.size()];
    return (JARDesc[])paramArrayList.toArray(arrayOfJARDesc);
  }
  
  public Properties getResourceProperties()
  {
    Properties localProperties = new Properties();
    visit(new ResourceVisitor()
    {
      private final Properties val$props;
      
      public void visitPropertyDesc(PropertyDesc paramAnonymousPropertyDesc)
      {
        this.val$props.setProperty(paramAnonymousPropertyDesc.getKey(), paramAnonymousPropertyDesc.getValue());
      }
      
      public void visitJARDesc(JARDesc paramAnonymousJARDesc) {}
      
      public void visitExtensionDesc(ExtensionDesc paramAnonymousExtensionDesc) {}
      
      public void visitJREDesc(JREDesc paramAnonymousJREDesc) {}
      
      public void visitPackageDesc(PackageDesc paramAnonymousPackageDesc) {}
    });
    return localProperties;
  }
  
  public static class PackageInformation
  {
    private LaunchDesc _launchDesc;
    private String _part;
    
    PackageInformation(LaunchDesc paramLaunchDesc, String paramString)
    {
      this._launchDesc = paramLaunchDesc;
      this._part = paramString;
    }
    
    public LaunchDesc getLaunchDesc()
    {
      return this._launchDesc;
    }
    
    public String getPart()
    {
      return this._part;
    }
  }
  
  public PackageInformation getPackageInformation(String paramString)
  {
    paramString = paramString.replace('/', '.');
    if (paramString.endsWith(".class")) {
      paramString = paramString.substring(0, paramString.length() - 6);
    }
    return visitPackageElements(getParent(), paramString);
  }
  
  public boolean isPackagePart(String paramString)
  {
    boolean[] arrayOfBoolean = { false };
    
    visit(new ResourceVisitor()
    {
      private final boolean[] val$result;
      private final String val$part;
      
      public void visitJARDesc(JARDesc paramAnonymousJARDesc) {}
      
      public void visitPropertyDesc(PropertyDesc paramAnonymousPropertyDesc) {}
      
      public void visitJREDesc(JREDesc paramAnonymousJREDesc) {}
      
      public void visitExtensionDesc(ExtensionDesc paramAnonymousExtensionDesc)
      {
        if (!paramAnonymousExtensionDesc.isInstaller())
        {
          LaunchDesc localLaunchDesc = paramAnonymousExtensionDesc.getExtensionDesc();
          if ((this.val$result[0] == 0) && (localLaunchDesc.isLibrary()) && (localLaunchDesc.getResources() != null)) {
            this.val$result[0] = localLaunchDesc.getResources().isPackagePart(this.val$part);
          }
        }
      }
      
      public void visitPackageDesc(PackageDesc paramAnonymousPackageDesc)
      {
        if (paramAnonymousPackageDesc.getPart().equals(this.val$part)) {
          this.val$result[0] = true;
        }
      }
    });
    return arrayOfBoolean[0];
  }
  
  private static PackageInformation visitPackageElements(LaunchDesc paramLaunchDesc, String paramString)
  {
    PackageInformation[] arrayOfPackageInformation = new PackageInformation[1];
    
    paramLaunchDesc.getResources().visit(new ResourceVisitor()
    {
      private final ResourcesDesc.PackageInformation[] val$result;
      private final String val$name;
      private final LaunchDesc val$ld;
      
      public void visitJARDesc(JARDesc paramAnonymousJARDesc) {}
      
      public void visitPropertyDesc(PropertyDesc paramAnonymousPropertyDesc) {}
      
      public void visitJREDesc(JREDesc paramAnonymousJREDesc) {}
      
      public void visitExtensionDesc(ExtensionDesc paramAnonymousExtensionDesc)
      {
        if (!paramAnonymousExtensionDesc.isInstaller())
        {
          LaunchDesc localLaunchDesc = paramAnonymousExtensionDesc.getExtensionDesc();
          if ((this.val$result[0] == null) && (localLaunchDesc.isLibrary()) && (localLaunchDesc.getResources() != null)) {
            this.val$result[0] = ResourcesDesc.visitPackageElements(localLaunchDesc, this.val$name);
          }
        }
      }
      
      public void visitPackageDesc(PackageDesc paramAnonymousPackageDesc)
      {
        if ((this.val$result[0] == null) && (paramAnonymousPackageDesc.match(this.val$name))) {
          this.val$result[0] = new ResourcesDesc.PackageInformation(this.val$ld, paramAnonymousPackageDesc.getPart());
        }
      }
    });
    return arrayOfPackageInformation[0];
  }
  
  public void visit(ResourceVisitor paramResourceVisitor)
  {
    for (int i = 0; i < this._list.size(); i++)
    {
      ResourceType localResourceType = (ResourceType)this._list.get(i);
      localResourceType.visit(paramResourceVisitor);
    }
  }
  
  public XMLNode asXML()
  {
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("resources", null);
    for (int i = 0; i < this._list.size(); i++)
    {
      ResourceType localResourceType = (ResourceType)this._list.get(i);
      localXMLNodeBuilder.add(localResourceType);
    }
    return localXMLNodeBuilder.getNode();
  }
  
  public void addNested(ResourcesDesc paramResourcesDesc)
  {
    if (paramResourcesDesc != null) {
      paramResourcesDesc.visit(new ResourceVisitor()
      {
        public void visitJARDesc(JARDesc paramAnonymousJARDesc)
        {
          ResourcesDesc.this._list.add(paramAnonymousJARDesc);
        }
        
        public void visitPropertyDesc(PropertyDesc paramAnonymousPropertyDesc)
        {
          ResourcesDesc.this._list.add(paramAnonymousPropertyDesc);
        }
        
        public void visitExtensionDesc(ExtensionDesc paramAnonymousExtensionDesc)
        {
          ResourcesDesc.this._list.add(paramAnonymousExtensionDesc);
        }
        
        public void visitJREDesc(JREDesc paramAnonymousJREDesc) {}
        
        public void visitPackageDesc(PackageDesc paramAnonymousPackageDesc) {}
      });
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\jnl\ResourcesDesc.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */