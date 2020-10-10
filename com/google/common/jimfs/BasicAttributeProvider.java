package com.google.common.jimfs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import javax.annotation.Nullable;

final class BasicAttributeProvider
  extends AttributeProvider
{
  private static final ImmutableSet<String> ATTRIBUTES = ImmutableSet.of("size", "fileKey", "isDirectory", "isRegularFile", "isSymbolicLink", "isOther", new String[] { "creationTime", "lastAccessTime", "lastModifiedTime" });
  
  public String name()
  {
    return "basic";
  }
  
  public ImmutableSet<String> fixedAttributes()
  {
    return ATTRIBUTES;
  }
  
  public Object get(File file, String attribute)
  {
    switch (attribute)
    {
    case "size": 
      return Long.valueOf(file.size());
    case "fileKey": 
      return Integer.valueOf(file.id());
    case "isDirectory": 
      return Boolean.valueOf(file.isDirectory());
    case "isRegularFile": 
      return Boolean.valueOf(file.isRegularFile());
    case "isSymbolicLink": 
      return Boolean.valueOf(file.isSymbolicLink());
    case "isOther": 
      return Boolean.valueOf((!file.isDirectory()) && (!file.isRegularFile()) && (!file.isSymbolicLink()));
    case "creationTime": 
      return FileTime.fromMillis(file.getCreationTime());
    case "lastAccessTime": 
      return FileTime.fromMillis(file.getLastAccessTime());
    case "lastModifiedTime": 
      return FileTime.fromMillis(file.getLastModifiedTime());
    }
    return null;
  }
  
  public void set(File file, String view, String attribute, Object value, boolean create)
  {
    switch (attribute)
    {
    case "creationTime": 
      checkNotCreate(view, attribute, create);
      file.setCreationTime(((FileTime)checkType(view, attribute, value, FileTime.class)).toMillis());
      break;
    case "lastAccessTime": 
      checkNotCreate(view, attribute, create);
      file.setLastAccessTime(((FileTime)checkType(view, attribute, value, FileTime.class)).toMillis());
      break;
    case "lastModifiedTime": 
      checkNotCreate(view, attribute, create);
      file.setLastModifiedTime(((FileTime)checkType(view, attribute, value, FileTime.class)).toMillis());
      break;
    case "size": 
    case "fileKey": 
    case "isDirectory": 
    case "isRegularFile": 
    case "isSymbolicLink": 
    case "isOther": 
      throw unsettable(view, attribute);
    }
  }
  
  public Class<BasicFileAttributeView> viewType()
  {
    return BasicFileAttributeView.class;
  }
  
  public BasicFileAttributeView view(FileLookup lookup, ImmutableMap<String, FileAttributeView> inheritedViews)
  {
    return new View(lookup);
  }
  
  public Class<BasicFileAttributes> attributesType()
  {
    return BasicFileAttributes.class;
  }
  
  public BasicFileAttributes readAttributes(File file)
  {
    return new Attributes(file);
  }
  
  private static final class View
    extends AbstractAttributeView
    implements BasicFileAttributeView
  {
    protected View(FileLookup lookup)
    {
      super();
    }
    
    public String name()
    {
      return "basic";
    }
    
    public BasicFileAttributes readAttributes()
      throws IOException
    {
      return new BasicAttributeProvider.Attributes(lookupFile());
    }
    
    public void setTimes(@Nullable FileTime lastModifiedTime, @Nullable FileTime lastAccessTime, @Nullable FileTime createTime)
      throws IOException
    {
      File file = lookupFile();
      if (lastModifiedTime != null) {
        file.setLastModifiedTime(lastModifiedTime.toMillis());
      }
      if (lastAccessTime != null) {
        file.setLastAccessTime(lastAccessTime.toMillis());
      }
      if (createTime != null) {
        file.setCreationTime(createTime.toMillis());
      }
    }
  }
  
  static class Attributes
    implements BasicFileAttributes
  {
    private final FileTime lastModifiedTime;
    private final FileTime lastAccessTime;
    private final FileTime creationTime;
    private final boolean regularFile;
    private final boolean directory;
    private final boolean symbolicLink;
    private final long size;
    private final Object fileKey;
    
    protected Attributes(File file)
    {
      this.lastModifiedTime = FileTime.fromMillis(file.getLastModifiedTime());
      this.lastAccessTime = FileTime.fromMillis(file.getLastAccessTime());
      this.creationTime = FileTime.fromMillis(file.getCreationTime());
      this.regularFile = file.isRegularFile();
      this.directory = file.isDirectory();
      this.symbolicLink = file.isSymbolicLink();
      this.size = file.size();
      this.fileKey = Integer.valueOf(file.id());
    }
    
    public FileTime lastModifiedTime()
    {
      return this.lastModifiedTime;
    }
    
    public FileTime lastAccessTime()
    {
      return this.lastAccessTime;
    }
    
    public FileTime creationTime()
    {
      return this.creationTime;
    }
    
    public boolean isRegularFile()
    {
      return this.regularFile;
    }
    
    public boolean isDirectory()
    {
      return this.directory;
    }
    
    public boolean isSymbolicLink()
    {
      return this.symbolicLink;
    }
    
    public boolean isOther()
    {
      return false;
    }
    
    public long size()
    {
      return this.size;
    }
    
    public Object fileKey()
    {
      return this.fileKey;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\BasicAttributeProvider.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */