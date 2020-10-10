package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

public final class Configuration
{
  final PathType pathType;
  final ImmutableSet<PathNormalization> nameDisplayNormalization;
  final ImmutableSet<PathNormalization> nameCanonicalNormalization;
  final boolean pathEqualityUsesCanonicalForm;
  final int blockSize;
  final long maxSize;
  final long maxCacheSize;
  final ImmutableSet<String> attributeViews;
  final ImmutableSet<AttributeProvider> attributeProviders;
  final ImmutableMap<String, Object> defaultAttributeValues;
  final WatchServiceConfiguration watchServiceConfig;
  final ImmutableSet<String> roots;
  final String workingDirectory;
  final ImmutableSet<Feature> supportedFeatures;
  
  public static Configuration unix()
  {
    return UnixHolder.UNIX;
  }
  
  private static final class UnixHolder
  {
    private static final Configuration UNIX = Configuration.builder(PathType.unix()).setRoots("/", new String[0]).setWorkingDirectory("/work").setAttributeViews("basic", new String[0]).setSupportedFeatures(new Feature[] { Feature.LINKS, Feature.SYMBOLIC_LINKS, Feature.SECURE_DIRECTORY_STREAM, Feature.FILE_CHANNEL }).build();
  }
  
  public static Configuration osX()
  {
    return OsxHolder.OS_X;
  }
  
  private static final class OsxHolder
  {
    private static final Configuration OS_X = Configuration.unix().toBuilder().setNameDisplayNormalization(PathNormalization.NFC, new PathNormalization[0]).setNameCanonicalNormalization(PathNormalization.NFD, new PathNormalization[] { PathNormalization.CASE_FOLD_ASCII }).setSupportedFeatures(new Feature[] { Feature.LINKS, Feature.SYMBOLIC_LINKS, Feature.FILE_CHANNEL }).build();
  }
  
  public static Configuration windows()
  {
    return WindowsHolder.WINDOWS;
  }
  
  private static final class WindowsHolder
  {
    private static final Configuration WINDOWS = Configuration.builder(PathType.windows()).setRoots("C:\\", new String[0]).setWorkingDirectory("C:\\work").setNameCanonicalNormalization(PathNormalization.CASE_FOLD_ASCII, new PathNormalization[0]).setPathEqualityUsesCanonicalForm(true).setAttributeViews("basic", new String[0]).setSupportedFeatures(new Feature[] { Feature.LINKS, Feature.SYMBOLIC_LINKS, Feature.FILE_CHANNEL }).build();
  }
  
  public static Configuration forCurrentPlatform()
  {
    String os = System.getProperty("os.name");
    if (os.contains("Windows")) {
      return windows();
    }
    if (os.contains("OS X")) {
      return osX();
    }
    return unix();
  }
  
  public static Builder builder(PathType pathType)
  {
    return new Builder(pathType, null);
  }
  
  private Configuration(Builder builder)
  {
    this.pathType = builder.pathType;
    this.nameDisplayNormalization = builder.nameDisplayNormalization;
    this.nameCanonicalNormalization = builder.nameCanonicalNormalization;
    this.pathEqualityUsesCanonicalForm = builder.pathEqualityUsesCanonicalForm;
    this.blockSize = builder.blockSize;
    this.maxSize = builder.maxSize;
    this.maxCacheSize = builder.maxCacheSize;
    this.attributeViews = builder.attributeViews;
    this.attributeProviders = (builder.attributeProviders == null ? ImmutableSet.of() : ImmutableSet.copyOf(builder.attributeProviders));
    
    this.defaultAttributeValues = (builder.defaultAttributeValues == null ? ImmutableMap.of() : ImmutableMap.copyOf(builder.defaultAttributeValues));
    
    this.watchServiceConfig = builder.watchServiceConfig;
    this.roots = builder.roots;
    this.workingDirectory = builder.workingDirectory;
    this.supportedFeatures = builder.supportedFeatures;
  }
  
  public Builder toBuilder()
  {
    return new Builder(this, null);
  }
  
  public static final class Builder
  {
    public static final int DEFAULT_BLOCK_SIZE = 8192;
    public static final long DEFAULT_MAX_SIZE = 4294967296L;
    public static final long DEFAULT_MAX_CACHE_SIZE = -1L;
    private final PathType pathType;
    private ImmutableSet<PathNormalization> nameDisplayNormalization = ImmutableSet.of();
    private ImmutableSet<PathNormalization> nameCanonicalNormalization = ImmutableSet.of();
    private boolean pathEqualityUsesCanonicalForm = false;
    private int blockSize = 8192;
    private long maxSize = 4294967296L;
    private long maxCacheSize = -1L;
    private ImmutableSet<String> attributeViews = ImmutableSet.of();
    private Set<AttributeProvider> attributeProviders = null;
    private Map<String, Object> defaultAttributeValues;
    private WatchServiceConfiguration watchServiceConfig = WatchServiceConfiguration.DEFAULT;
    private ImmutableSet<String> roots = ImmutableSet.of();
    private String workingDirectory;
    private ImmutableSet<Feature> supportedFeatures = ImmutableSet.of();
    
    private Builder(PathType pathType)
    {
      this.pathType = ((PathType)Preconditions.checkNotNull(pathType));
    }
    
    private Builder(Configuration configuration)
    {
      this.pathType = configuration.pathType;
      this.nameDisplayNormalization = configuration.nameDisplayNormalization;
      this.nameCanonicalNormalization = configuration.nameCanonicalNormalization;
      this.pathEqualityUsesCanonicalForm = configuration.pathEqualityUsesCanonicalForm;
      this.blockSize = configuration.blockSize;
      this.maxSize = configuration.maxSize;
      this.maxCacheSize = configuration.maxCacheSize;
      this.attributeViews = configuration.attributeViews;
      this.attributeProviders = (configuration.attributeProviders.isEmpty() ? null : new HashSet(configuration.attributeProviders));
      
      this.defaultAttributeValues = (configuration.defaultAttributeValues.isEmpty() ? null : new HashMap(configuration.defaultAttributeValues));
      
      this.watchServiceConfig = configuration.watchServiceConfig;
      this.roots = configuration.roots;
      this.workingDirectory = configuration.workingDirectory;
      this.supportedFeatures = configuration.supportedFeatures;
    }
    
    public Builder setNameDisplayNormalization(PathNormalization first, PathNormalization... more)
    {
      this.nameDisplayNormalization = checkNormalizations(Lists.asList(first, more));
      return this;
    }
    
    public Builder setNameCanonicalNormalization(PathNormalization first, PathNormalization... more)
    {
      this.nameCanonicalNormalization = checkNormalizations(Lists.asList(first, more));
      return this;
    }
    
    private ImmutableSet<PathNormalization> checkNormalizations(List<PathNormalization> normalizations)
    {
      PathNormalization none = null;
      PathNormalization normalization = null;
      PathNormalization caseFold = null;
      for (PathNormalization n : normalizations)
      {
        Preconditions.checkNotNull(n);
        checkNormalizationNotSet(n, none);
        switch (Configuration.1.$SwitchMap$com$google$common$jimfs$PathNormalization[n.ordinal()])
        {
        case 1: 
          none = n;
          break;
        case 2: 
        case 3: 
          checkNormalizationNotSet(n, normalization);
          normalization = n;
          break;
        case 4: 
        case 5: 
          checkNormalizationNotSet(n, caseFold);
          caseFold = n;
          break;
        default: 
          throw new AssertionError();
        }
      }
      if (none != null) {
        return ImmutableSet.of();
      }
      return Sets.immutableEnumSet(normalizations);
    }
    
    private static void checkNormalizationNotSet(PathNormalization n, @Nullable PathNormalization set)
    {
      if (set != null) {
        throw new IllegalArgumentException("can't set normalization " + n + ": normalization " + set + " already set");
      }
    }
    
    public Builder setPathEqualityUsesCanonicalForm(boolean useCanonicalForm)
    {
      this.pathEqualityUsesCanonicalForm = useCanonicalForm;
      return this;
    }
    
    public Builder setBlockSize(int blockSize)
    {
      Preconditions.checkArgument(blockSize > 0, "blockSize (%s) must be positive", new Object[] { Integer.valueOf(blockSize) });
      this.blockSize = blockSize;
      return this;
    }
    
    public Builder setMaxSize(long maxSize)
    {
      Preconditions.checkArgument(maxSize > 0L, "maxSize (%s) must be positive", new Object[] { Long.valueOf(maxSize) });
      this.maxSize = maxSize;
      return this;
    }
    
    public Builder setMaxCacheSize(long maxCacheSize)
    {
      Preconditions.checkArgument(maxCacheSize >= 0L, "maxCacheSize (%s) may not be negative", new Object[] { Long.valueOf(maxCacheSize) });
      this.maxCacheSize = maxCacheSize;
      return this;
    }
    
    public Builder setAttributeViews(String first, String... more)
    {
      this.attributeViews = ImmutableSet.copyOf(Lists.asList(first, more));
      return this;
    }
    
    public Builder addAttributeProvider(AttributeProvider provider)
    {
      Preconditions.checkNotNull(provider);
      if (this.attributeProviders == null) {
        this.attributeProviders = new HashSet();
      }
      this.attributeProviders.add(provider);
      return this;
    }
    
    public Builder setDefaultAttributeValue(String attribute, Object value)
    {
      Preconditions.checkArgument(ATTRIBUTE_PATTERN.matcher(attribute).matches(), "attribute (%s) must be of the form \"view:attribute\"", new Object[] { attribute });
      
      Preconditions.checkNotNull(value);
      if (this.defaultAttributeValues == null) {
        this.defaultAttributeValues = new HashMap();
      }
      this.defaultAttributeValues.put(attribute, value);
      return this;
    }
    
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("[^:]+:[^:]+");
    
    public Builder setRoots(String first, String... more)
    {
      List<String> roots = Lists.asList(first, more);
      for (String root : roots)
      {
        PathType.ParseResult parseResult = this.pathType.parsePath(root);
        Preconditions.checkArgument(parseResult.isRoot(), "invalid root: %s", new Object[] { root });
      }
      this.roots = ImmutableSet.copyOf(roots);
      return this;
    }
    
    public Builder setWorkingDirectory(String workingDirectory)
    {
      PathType.ParseResult parseResult = this.pathType.parsePath(workingDirectory);
      Preconditions.checkArgument(parseResult.isAbsolute(), "working directory must be an absolute path: %s", new Object[] { workingDirectory });
      
      this.workingDirectory = ((String)Preconditions.checkNotNull(workingDirectory));
      return this;
    }
    
    public Builder setSupportedFeatures(Feature... features)
    {
      this.supportedFeatures = Sets.immutableEnumSet(Arrays.asList(features));
      return this;
    }
    
    public Builder setWatchServiceConfiguration(WatchServiceConfiguration config)
    {
      this.watchServiceConfig = ((WatchServiceConfiguration)Preconditions.checkNotNull(config));
      return this;
    }
    
    public Configuration build()
    {
      return new Configuration(this, null);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\Configuration.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */