package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.List;

final class UserDefinedAttributeProvider
  extends AttributeProvider
{
  public String name()
  {
    return "user";
  }
  
  public ImmutableSet<String> fixedAttributes()
  {
    return ImmutableSet.of();
  }
  
  public boolean supports(String attribute)
  {
    return true;
  }
  
  public ImmutableSet<String> attributes(File file)
  {
    return userDefinedAttributes(file);
  }
  
  private static ImmutableSet<String> userDefinedAttributes(File file)
  {
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    for (String attribute : file.getAttributeNames("user")) {
      builder.add(attribute);
    }
    return builder.build();
  }
  
  public Object get(File file, String attribute)
  {
    Object value = file.getAttribute("user", attribute);
    if ((value instanceof byte[]))
    {
      byte[] bytes = (byte[])value;
      return bytes.clone();
    }
    return null;
  }
  
  public void set(File file, String view, String attribute, Object value, boolean create)
  {
    Preconditions.checkNotNull(value);
    checkNotCreate(view, attribute, create);
    byte[] bytes;
    if ((value instanceof byte[]))
    {
      bytes = (byte[])((byte[])value).clone();
    }
    else if ((value instanceof ByteBuffer))
    {
      ByteBuffer buffer = (ByteBuffer)value;
      byte[] bytes = new byte[buffer.remaining()];
      buffer.get(bytes);
    }
    else
    {
      throw invalidType(view, attribute, value, new Class[] { byte[].class, ByteBuffer.class });
    }
    byte[] bytes;
    file.setAttribute("user", attribute, bytes);
  }
  
  public Class<UserDefinedFileAttributeView> viewType()
  {
    return UserDefinedFileAttributeView.class;
  }
  
  public UserDefinedFileAttributeView view(FileLookup lookup, ImmutableMap<String, FileAttributeView> inheritedViews)
  {
    return new View(lookup);
  }
  
  private static class View
    extends AbstractAttributeView
    implements UserDefinedFileAttributeView
  {
    public View(FileLookup lookup)
    {
      super();
    }
    
    public String name()
    {
      return "user";
    }
    
    public List<String> list()
      throws IOException
    {
      return UserDefinedAttributeProvider.userDefinedAttributes(lookupFile()).asList();
    }
    
    private byte[] getStoredBytes(String name)
      throws IOException
    {
      byte[] bytes = (byte[])lookupFile().getAttribute(name(), name);
      if (bytes == null) {
        throw new IllegalArgumentException("attribute '" + name() + ":" + name + "' is not set");
      }
      return bytes;
    }
    
    public int size(String name)
      throws IOException
    {
      return getStoredBytes(name).length;
    }
    
    public int read(String name, ByteBuffer dst)
      throws IOException
    {
      byte[] bytes = getStoredBytes(name);
      dst.put(bytes);
      return bytes.length;
    }
    
    public int write(String name, ByteBuffer src)
      throws IOException
    {
      byte[] bytes = new byte[src.remaining()];
      src.get(bytes);
      lookupFile().setAttribute(name(), name, bytes);
      return bytes.length;
    }
    
    public void delete(String name)
      throws IOException
    {
      lookupFile().deleteAttribute(name(), name);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\UserDefinedAttributeProvider.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */