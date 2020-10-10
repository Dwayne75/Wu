package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nullable;

public abstract class AttributeProvider
{
  public abstract String name();
  
  public ImmutableSet<String> inherits()
  {
    return ImmutableSet.of();
  }
  
  public abstract Class<? extends FileAttributeView> viewType();
  
  public abstract FileAttributeView view(FileLookup paramFileLookup, ImmutableMap<String, FileAttributeView> paramImmutableMap);
  
  public ImmutableMap<String, ?> defaultValues(Map<String, ?> userDefaults)
  {
    return ImmutableMap.of();
  }
  
  public abstract ImmutableSet<String> fixedAttributes();
  
  public boolean supports(String attribute)
  {
    return fixedAttributes().contains(attribute);
  }
  
  public ImmutableSet<String> attributes(File file)
  {
    return fixedAttributes();
  }
  
  @Nullable
  public abstract Object get(File paramFile, String paramString);
  
  public abstract void set(File paramFile, String paramString1, String paramString2, Object paramObject, boolean paramBoolean);
  
  @Nullable
  public Class<? extends BasicFileAttributes> attributesType()
  {
    return null;
  }
  
  public BasicFileAttributes readAttributes(File file)
  {
    throw new UnsupportedOperationException();
  }
  
  protected static IllegalArgumentException unsettable(String view, String attribute)
  {
    throw new IllegalArgumentException("cannot set attribute '" + view + ":" + attribute + "'");
  }
  
  protected static void checkNotCreate(String view, String attribute, boolean create)
  {
    if (create) {
      throw new UnsupportedOperationException("cannot set attribute '" + view + ":" + attribute + "' during file creation");
    }
  }
  
  protected static <T> T checkType(String view, String attribute, Object value, Class<T> type)
  {
    Preconditions.checkNotNull(value);
    if (type.isInstance(value)) {
      return (T)type.cast(value);
    }
    throw invalidType(view, attribute, value, new Class[] { type });
  }
  
  protected static IllegalArgumentException invalidType(String view, String attribute, Object value, Class<?>... expectedTypes)
  {
    Object expected = "one of " + Arrays.toString(expectedTypes);
    
    throw new IllegalArgumentException("invalid type " + value.getClass() + " for attribute '" + view + ":" + attribute + "': expected " + expected);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\AttributeProvider.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */