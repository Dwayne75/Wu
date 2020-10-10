package com.google.common.jimfs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

final class FileFactory
{
  private final AtomicInteger idGenerator = new AtomicInteger();
  private final HeapDisk disk;
  
  public FileFactory(HeapDisk disk)
  {
    this.disk = ((HeapDisk)Preconditions.checkNotNull(disk));
  }
  
  private int nextFileId()
  {
    return this.idGenerator.getAndIncrement();
  }
  
  public Directory createDirectory()
  {
    return Directory.create(nextFileId());
  }
  
  public Directory createRootDirectory(Name name)
  {
    return Directory.createRoot(nextFileId(), name);
  }
  
  @VisibleForTesting
  RegularFile createRegularFile()
  {
    return RegularFile.create(nextFileId(), this.disk);
  }
  
  @VisibleForTesting
  SymbolicLink createSymbolicLink(JimfsPath target)
  {
    return SymbolicLink.create(nextFileId(), target);
  }
  
  public File copyWithoutContent(File file)
    throws IOException
  {
    return file.copyWithoutContent(nextFileId());
  }
  
  private final Supplier<Directory> directorySupplier = new DirectorySupplier(null);
  private final Supplier<RegularFile> regularFileSupplier = new RegularFileSupplier(null);
  
  public Supplier<Directory> directoryCreator()
  {
    return this.directorySupplier;
  }
  
  public Supplier<RegularFile> regularFileCreator()
  {
    return this.regularFileSupplier;
  }
  
  public Supplier<SymbolicLink> symbolicLinkCreator(JimfsPath target)
  {
    return new SymbolicLinkSupplier(target);
  }
  
  private final class DirectorySupplier
    implements Supplier<Directory>
  {
    private DirectorySupplier() {}
    
    public Directory get()
    {
      return FileFactory.this.createDirectory();
    }
  }
  
  private final class RegularFileSupplier
    implements Supplier<RegularFile>
  {
    private RegularFileSupplier() {}
    
    public RegularFile get()
    {
      return FileFactory.this.createRegularFile();
    }
  }
  
  private final class SymbolicLinkSupplier
    implements Supplier<SymbolicLink>
  {
    private final JimfsPath target;
    
    protected SymbolicLinkSupplier(JimfsPath target)
    {
      this.target = ((JimfsPath)Preconditions.checkNotNull(target));
    }
    
    public SymbolicLink get()
    {
      return FileFactory.this.createSymbolicLink(this.target);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\FileFactory.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */