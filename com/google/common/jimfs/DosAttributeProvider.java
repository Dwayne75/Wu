package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import javax.annotation.Nullable;

final class DosAttributeProvider
  extends AttributeProvider
{
  private static final ImmutableSet<String> ATTRIBUTES = ImmutableSet.of("readonly", "hidden", "archive", "system");
  private static final ImmutableSet<String> INHERITED_VIEWS = ImmutableSet.of("basic", "owner");
  
  public String name()
  {
    return "dos";
  }
  
  public ImmutableSet<String> inherits()
  {
    return INHERITED_VIEWS;
  }
  
  public ImmutableSet<String> fixedAttributes()
  {
    return ATTRIBUTES;
  }
  
  public ImmutableMap<String, ?> defaultValues(Map<String, ?> userProvidedDefaults)
  {
    return ImmutableMap.of("dos:readonly", getDefaultValue("dos:readonly", userProvidedDefaults), "dos:hidden", getDefaultValue("dos:hidden", userProvidedDefaults), "dos:archive", getDefaultValue("dos:archive", userProvidedDefaults), "dos:system", getDefaultValue("dos:system", userProvidedDefaults));
  }
  
  private static Boolean getDefaultValue(String attribute, Map<String, ?> userProvidedDefaults)
  {
    Object userProvidedValue = userProvidedDefaults.get(attribute);
    if (userProvidedValue != null) {
      return (Boolean)checkType("dos", attribute, userProvidedValue, Boolean.class);
    }
    return Boolean.valueOf(false);
  }
  
  @Nullable
  public Object get(File file, String attribute)
  {
    if (ATTRIBUTES.contains(attribute)) {
      return file.getAttribute("dos", attribute);
    }
    return null;
  }
  
  public void set(File file, String view, String attribute, Object value, boolean create)
  {
    if (supports(attribute))
    {
      checkNotCreate(view, attribute, create);
      file.setAttribute("dos", attribute, checkType(view, attribute, value, Boolean.class));
    }
  }
  
  public Class<DosFileAttributeView> viewType()
  {
    return DosFileAttributeView.class;
  }
  
  public DosFileAttributeView view(FileLookup lookup, ImmutableMap<String, FileAttributeView> inheritedViews)
  {
    return new View(lookup, (BasicFileAttributeView)inheritedViews.get("basic"));
  }
  
  public Class<DosFileAttributes> attributesType()
  {
    return DosFileAttributes.class;
  }
  
  public DosFileAttributes readAttributes(File file)
  {
    return new Attributes(file);
  }
  
  private static final class View
    extends AbstractAttributeView
    implements DosFileAttributeView
  {
    private final BasicFileAttributeView basicView;
    
    public View(FileLookup lookup, BasicFileAttributeView basicView)
    {
      super();
      this.basicView = ((BasicFileAttributeView)Preconditions.checkNotNull(basicView));
    }
    
    public String name()
    {
      return "dos";
    }
    
    public DosFileAttributes readAttributes()
      throws IOException
    {
      return new DosAttributeProvider.Attributes(lookupFile());
    }
    
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime)
      throws IOException
    {
      this.basicView.setTimes(lastModifiedTime, lastAccessTime, createTime);
    }
    
    public void setReadOnly(boolean value)
      throws IOException
    {
      lookupFile().setAttribute("dos", "readonly", Boolean.valueOf(value));
    }
    
    public void setHidden(boolean value)
      throws IOException
    {
      lookupFile().setAttribute("dos", "hidden", Boolean.valueOf(value));
    }
    
    public void setSystem(boolean value)
      throws IOException
    {
      lookupFile().setAttribute("dos", "system", Boolean.valueOf(value));
    }
    
    public void setArchive(boolean value)
      throws IOException
    {
      lookupFile().setAttribute("dos", "archive", Boolean.valueOf(value));
    }
  }
  
  static class Attributes
    extends BasicAttributeProvider.Attributes
    implements DosFileAttributes
  {
    private final boolean readOnly;
    private final boolean hidden;
    private final boolean archive;
    private final boolean system;
    
    protected Attributes(File file)
    {
      super();
      this.readOnly = ((Boolean)file.getAttribute("dos", "readonly")).booleanValue();
      this.hidden = ((Boolean)file.getAttribute("dos", "hidden")).booleanValue();
      this.archive = ((Boolean)file.getAttribute("dos", "archive")).booleanValue();
      this.system = ((Boolean)file.getAttribute("dos", "system")).booleanValue();
    }
    
    public boolean isReadOnly()
    {
      return this.readOnly;
    }
    
    public boolean isHidden()
    {
      return this.hidden;
    }
    
    public boolean isArchive()
    {
      return this.archive;
    }
    
    public boolean isSystem()
    {
      return this.system;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\DosAttributeProvider.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */