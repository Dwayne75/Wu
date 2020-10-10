package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;

final class AttributeService
{
  private static final String ALL_ATTRIBUTES = "*";
  private final ImmutableMap<String, AttributeProvider> providersByName;
  private final ImmutableMap<Class<?>, AttributeProvider> providersByViewType;
  private final ImmutableMap<Class<?>, AttributeProvider> providersByAttributesType;
  private final ImmutableList<FileAttribute<?>> defaultValues;
  
  public AttributeService(Configuration configuration)
  {
    this(getProviders(configuration), configuration.defaultAttributeValues);
  }
  
  public AttributeService(Iterable<? extends AttributeProvider> providers, Map<String, ?> userProvidedDefaults)
  {
    ImmutableMap.Builder<String, AttributeProvider> byViewNameBuilder = ImmutableMap.builder();
    ImmutableMap.Builder<Class<?>, AttributeProvider> byViewTypeBuilder = ImmutableMap.builder();
    ImmutableMap.Builder<Class<?>, AttributeProvider> byAttributesTypeBuilder = ImmutableMap.builder();
    
    ImmutableList.Builder<FileAttribute<?>> defaultAttributesBuilder = ImmutableList.builder();
    for (AttributeProvider provider : providers)
    {
      byViewNameBuilder.put(provider.name(), provider);
      byViewTypeBuilder.put(provider.viewType(), provider);
      if (provider.attributesType() != null) {
        byAttributesTypeBuilder.put(provider.attributesType(), provider);
      }
      for (Map.Entry<String, ?> entry : provider.defaultValues(userProvidedDefaults).entrySet()) {
        defaultAttributesBuilder.add(new SimpleFileAttribute((String)entry.getKey(), entry.getValue()));
      }
    }
    this.providersByName = byViewNameBuilder.build();
    this.providersByViewType = byViewTypeBuilder.build();
    this.providersByAttributesType = byAttributesTypeBuilder.build();
    this.defaultValues = defaultAttributesBuilder.build();
  }
  
  private static Iterable<AttributeProvider> getProviders(Configuration configuration)
  {
    Map<String, AttributeProvider> result = new HashMap();
    for (AttributeProvider provider : configuration.attributeProviders) {
      result.put(provider.name(), provider);
    }
    for (String view : configuration.attributeViews) {
      addStandardProvider(result, view);
    }
    addMissingProviders(result);
    
    return Collections.unmodifiableCollection(result.values());
  }
  
  private static void addMissingProviders(Map<String, AttributeProvider> providers)
  {
    Set<String> missingViews = new HashSet();
    for (AttributeProvider provider : providers.values()) {
      for (String inheritedView : provider.inherits()) {
        if (!providers.containsKey(inheritedView)) {
          missingViews.add(inheritedView);
        }
      }
    }
    if (missingViews.isEmpty()) {
      return;
    }
    for (String view : missingViews) {
      addStandardProvider(providers, view);
    }
    addMissingProviders(providers);
  }
  
  private static void addStandardProvider(Map<String, AttributeProvider> result, String view)
  {
    AttributeProvider provider = StandardAttributeProviders.get(view);
    if (provider == null)
    {
      if (!result.containsKey(view)) {
        throw new IllegalStateException("no provider found for attribute view '" + view + "'");
      }
    }
    else {
      result.put(provider.name(), provider);
    }
  }
  
  public ImmutableSet<String> supportedFileAttributeViews()
  {
    return this.providersByName.keySet();
  }
  
  public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type)
  {
    return this.providersByViewType.containsKey(type);
  }
  
  public void setInitialAttributes(File file, FileAttribute<?>... attrs)
  {
    for (int i = 0; i < this.defaultValues.size(); i++)
    {
      FileAttribute<?> attribute = (FileAttribute)this.defaultValues.get(i);
      
      int separatorIndex = attribute.name().indexOf(':');
      String view = attribute.name().substring(0, separatorIndex);
      String attr = attribute.name().substring(separatorIndex + 1);
      file.setAttribute(view, attr, attribute.value());
    }
    for (FileAttribute<?> attr : attrs) {
      setAttribute(file, attr.name(), attr.value(), true);
    }
  }
  
  public void copyAttributes(File file, File copy, AttributeCopyOption copyOption)
  {
    switch (copyOption)
    {
    case ALL: 
      file.copyAttributes(copy);
      break;
    case BASIC: 
      file.copyBasicAttributes(copy);
      break;
    }
  }
  
  public Object getAttribute(File file, String attribute)
  {
    String view = getViewName(attribute);
    String attr = getSingleAttribute(attribute);
    return getAttribute(file, view, attr);
  }
  
  public Object getAttribute(File file, String view, String attribute)
  {
    Object value = getAttributeInternal(file, view, attribute);
    if (value == null) {
      throw new IllegalArgumentException("invalid attribute for view '" + view + "': " + attribute);
    }
    return value;
  }
  
  @Nullable
  private Object getAttributeInternal(File file, String view, String attribute)
  {
    AttributeProvider provider = (AttributeProvider)this.providersByName.get(view);
    if (provider == null) {
      return null;
    }
    Object value = provider.get(file, attribute);
    if (value == null) {
      for (String inheritedView : provider.inherits())
      {
        value = getAttributeInternal(file, inheritedView, attribute);
        if (value != null) {
          break;
        }
      }
    }
    return value;
  }
  
  public void setAttribute(File file, String attribute, Object value, boolean create)
  {
    String view = getViewName(attribute);
    String attr = getSingleAttribute(attribute);
    setAttributeInternal(file, view, attr, value, create);
  }
  
  private void setAttributeInternal(File file, String view, String attribute, Object value, boolean create)
  {
    AttributeProvider provider = (AttributeProvider)this.providersByName.get(view);
    if (provider != null)
    {
      if (provider.supports(attribute))
      {
        provider.set(file, view, attribute, value, create);
        return;
      }
      for (String inheritedView : provider.inherits())
      {
        AttributeProvider inheritedProvider = (AttributeProvider)this.providersByName.get(inheritedView);
        if (inheritedProvider.supports(attribute))
        {
          inheritedProvider.set(file, view, attribute, value, create);
          return;
        }
      }
    }
    throw new IllegalArgumentException("cannot set attribute '" + view + ":" + attribute + "'");
  }
  
  @Nullable
  public <V extends FileAttributeView> V getFileAttributeView(FileLookup lookup, Class<V> type)
  {
    AttributeProvider provider = (AttributeProvider)this.providersByViewType.get(type);
    if (provider != null) {
      return provider.view(lookup, createInheritedViews(lookup, provider));
    }
    return null;
  }
  
  private ImmutableMap<String, FileAttributeView> createInheritedViews(FileLookup lookup, AttributeProvider provider)
  {
    if (provider.inherits().isEmpty()) {
      return ImmutableMap.of();
    }
    Map<String, FileAttributeView> inheritedViews = new HashMap();
    createInheritedViews(lookup, provider, inheritedViews);
    return ImmutableMap.copyOf(inheritedViews);
  }
  
  private void createInheritedViews(FileLookup lookup, AttributeProvider provider, Map<String, FileAttributeView> inheritedViews)
  {
    for (String inherited : provider.inherits()) {
      if (!inheritedViews.containsKey(inherited))
      {
        AttributeProvider inheritedProvider = (AttributeProvider)this.providersByName.get(inherited);
        FileAttributeView inheritedView = getFileAttributeView(lookup, inheritedProvider.viewType(), inheritedViews);
        
        inheritedViews.put(inherited, inheritedView);
      }
    }
  }
  
  private FileAttributeView getFileAttributeView(FileLookup lookup, Class<? extends FileAttributeView> viewType, Map<String, FileAttributeView> inheritedViews)
  {
    AttributeProvider provider = (AttributeProvider)this.providersByViewType.get(viewType);
    createInheritedViews(lookup, provider, inheritedViews);
    return provider.view(lookup, ImmutableMap.copyOf(inheritedViews));
  }
  
  public ImmutableMap<String, Object> readAttributes(File file, String attributes)
  {
    String view = getViewName(attributes);
    List<String> attrs = getAttributeNames(attributes);
    if ((attrs.size() > 1) && (attrs.contains("*"))) {
      throw new IllegalArgumentException("invalid attributes: " + attributes);
    }
    Map<String, Object> result = new HashMap();
    if ((attrs.size() == 1) && (attrs.contains("*")))
    {
      AttributeProvider provider = (AttributeProvider)this.providersByName.get(view);
      readAll(file, provider, result);
      for (String inheritedView : provider.inherits())
      {
        AttributeProvider inheritedProvider = (AttributeProvider)this.providersByName.get(inheritedView);
        readAll(file, inheritedProvider, result);
      }
    }
    else
    {
      for (String attr : attrs) {
        result.put(attr, getAttribute(file, view, attr));
      }
    }
    return ImmutableMap.copyOf(result);
  }
  
  private static void readAll(File file, AttributeProvider provider, Map<String, Object> map)
  {
    for (String attribute : provider.attributes(file))
    {
      Object value = provider.get(file, attribute);
      if (value != null) {
        map.put(attribute, value);
      }
    }
  }
  
  public <A extends BasicFileAttributes> A readAttributes(File file, Class<A> type)
  {
    AttributeProvider provider = (AttributeProvider)this.providersByAttributesType.get(type);
    if (provider != null) {
      return provider.readAttributes(file);
    }
    throw new UnsupportedOperationException("unsupported attributes type: " + type);
  }
  
  private static String getViewName(String attribute)
  {
    int separatorIndex = attribute.indexOf(':');
    if (separatorIndex == -1) {
      return "basic";
    }
    if ((separatorIndex == 0) || (separatorIndex == attribute.length() - 1) || (attribute.indexOf(':', separatorIndex + 1) != -1)) {
      throw new IllegalArgumentException("illegal attribute format: " + attribute);
    }
    return attribute.substring(0, separatorIndex);
  }
  
  private static final Splitter ATTRIBUTE_SPLITTER = Splitter.on(',');
  
  private static ImmutableList<String> getAttributeNames(String attributes)
  {
    int separatorIndex = attributes.indexOf(':');
    String attributesPart = attributes.substring(separatorIndex + 1);
    
    return ImmutableList.copyOf(ATTRIBUTE_SPLITTER.split(attributesPart));
  }
  
  private static String getSingleAttribute(String attribute)
  {
    ImmutableList<String> attributeNames = getAttributeNames(attribute);
    if ((attributeNames.size() != 1) || ("*".equals(attributeNames.get(0)))) {
      throw new IllegalArgumentException("must specify a single attribute: " + attribute);
    }
    return (String)attributeNames.get(0);
  }
  
  private static final class SimpleFileAttribute<T>
    implements FileAttribute<T>
  {
    private final String name;
    private final T value;
    
    SimpleFileAttribute(String name, T value)
    {
      this.name = ((String)Preconditions.checkNotNull(name));
      this.value = Preconditions.checkNotNull(value);
    }
    
    public String name()
    {
      return this.name;
    }
    
    public T value()
    {
      return (T)this.value;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\AttributeService.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */