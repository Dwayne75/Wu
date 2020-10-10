package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttribute;
import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import com.sun.deploy.xml.XMLable;
import java.net.URL;

public class InformationDesc
  implements XMLable
{
  private String _title;
  private String _vendor;
  private URL _home;
  private String[] _descriptions;
  private IconDesc[] _icons;
  private ShortcutDesc _shortcutHints;
  private AssociationDesc[] _associations;
  private RContentDesc[] _relatedContent;
  private boolean _supportOfflineOperation;
  public static final int DESC_DEFAULT = 0;
  public static final int DESC_SHORT = 1;
  public static final int DESC_ONELINE = 2;
  public static final int DESC_TOOLTIP = 3;
  public static final int NOF_DESC = 4;
  public static final int ICON_SIZE_SMALL = 0;
  public static final int ICON_SIZE_MEDIUM = 1;
  public static final int ICON_SIZE_LARGE = 2;
  
  public InformationDesc(String paramString1, String paramString2, URL paramURL, String[] paramArrayOfString, IconDesc[] paramArrayOfIconDesc, ShortcutDesc paramShortcutDesc, RContentDesc[] paramArrayOfRContentDesc, AssociationDesc[] paramArrayOfAssociationDesc, boolean paramBoolean)
  {
    this._title = paramString1;
    this._vendor = paramString2;
    this._home = paramURL;
    if (paramArrayOfString == null) {
      paramArrayOfString = new String[4];
    }
    this._descriptions = paramArrayOfString;
    this._icons = paramArrayOfIconDesc;
    this._shortcutHints = paramShortcutDesc;
    this._associations = paramArrayOfAssociationDesc;
    this._relatedContent = paramArrayOfRContentDesc;
    this._supportOfflineOperation = paramBoolean;
  }
  
  public String getTitle()
  {
    return this._title;
  }
  
  public String getVendor()
  {
    return this._vendor;
  }
  
  public URL getHome()
  {
    return this._home;
  }
  
  public boolean supportsOfflineOperation()
  {
    return this._supportOfflineOperation;
  }
  
  public IconDesc[] getIcons()
  {
    return this._icons;
  }
  
  public ShortcutDesc getShortcut()
  {
    return this._shortcutHints;
  }
  
  public AssociationDesc[] getAssociations()
  {
    return this._associations;
  }
  
  public RContentDesc[] getRelatedContent()
  {
    return this._relatedContent;
  }
  
  public String getDescription(int paramInt)
  {
    return this._descriptions[paramInt];
  }
  
  public IconDesc getIconLocation(int paramInt1, int paramInt2)
  {
    int i = 0;
    int j = 0;
    switch (paramInt1)
    {
    case 0: 
      i = j = 16; break;
    case 1: 
      i = j = 32; break;
    case 2: 
      i = j = 64;
    }
    Object localObject = null;
    long l1 = 0L;
    for (int k = 0; k < this._icons.length; k++)
    {
      IconDesc localIconDesc = this._icons[k];
      if (localIconDesc.getKind() == paramInt2)
      {
        if ((localIconDesc.getHeight() == i) && (localIconDesc.getWidth() == j)) {
          return localIconDesc;
        }
        if ((localIconDesc.getHeight() == 0) && (localIconDesc.getWidth() == 0))
        {
          if (localObject == null) {
            localObject = localIconDesc;
          }
        }
        else
        {
          long l2 = Math.abs(localIconDesc.getHeight() * localIconDesc.getWidth() - i * j);
          if ((l1 == 0L) || (l2 < l1))
          {
            l1 = l2;
            localObject = localIconDesc;
          }
        }
      }
    }
    return (IconDesc)localObject;
  }
  
  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("information", localXMLAttributeBuilder.getAttributeList());
    localXMLNodeBuilder.add("title", this._title);
    localXMLNodeBuilder.add("vendor", this._vendor);
    localXMLNodeBuilder.add(new XMLNode("homepage", new XMLAttribute("href", this._home != null ? this._home.toString() : null), null, null));
    
    localXMLNodeBuilder.add(getDescriptionNode(0, ""));
    localXMLNodeBuilder.add(getDescriptionNode(1, "short"));
    localXMLNodeBuilder.add(getDescriptionNode(2, "one-line"));
    localXMLNodeBuilder.add(getDescriptionNode(3, "tooltip"));
    int i;
    if (this._icons != null) {
      for (i = 0; i < this._icons.length; i++) {
        localXMLNodeBuilder.add(this._icons[i]);
      }
    }
    if (this._shortcutHints != null) {
      localXMLNodeBuilder.add(this._shortcutHints);
    }
    if (this._associations != null) {
      for (i = 0; i < this._associations.length; i++) {
        localXMLNodeBuilder.add(this._associations[i]);
      }
    }
    if (this._relatedContent != null) {
      for (i = 0; i < this._relatedContent.length; i++) {
        localXMLNodeBuilder.add(this._relatedContent[i]);
      }
    }
    if (this._supportOfflineOperation) {
      localXMLNodeBuilder.add(new XMLNode("offline-allowed", null));
    }
    return localXMLNodeBuilder.getNode();
  }
  
  private XMLNode getDescriptionNode(int paramInt, String paramString)
  {
    String str = this._descriptions[paramInt];
    if (str == null) {
      return null;
    }
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("kind", paramString);
    return new XMLNode("description", localXMLAttributeBuilder.getAttributeList(), new XMLNode(str), null);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\jnl\InformationDesc.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */