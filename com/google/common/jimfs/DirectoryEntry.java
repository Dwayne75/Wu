package com.google.common.jimfs;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.NotLinkException;
import java.nio.file.Path;
import java.util.Objects;
import javax.annotation.Nullable;

final class DirectoryEntry
{
  private final Directory directory;
  private final Name name;
  @Nullable
  private final File file;
  @Nullable
  DirectoryEntry next;
  
  DirectoryEntry(Directory directory, Name name, @Nullable File file)
  {
    this.directory = ((Directory)Preconditions.checkNotNull(directory));
    this.name = ((Name)Preconditions.checkNotNull(name));
    this.file = file;
  }
  
  public boolean exists()
  {
    return this.file != null;
  }
  
  public DirectoryEntry requireExists(Path pathForException)
    throws NoSuchFileException
  {
    if (!exists()) {
      throw new NoSuchFileException(pathForException.toString());
    }
    return this;
  }
  
  public DirectoryEntry requireDoesNotExist(Path pathForException)
    throws FileAlreadyExistsException
  {
    if (exists()) {
      throw new FileAlreadyExistsException(pathForException.toString());
    }
    return this;
  }
  
  public DirectoryEntry requireDirectory(Path pathForException)
    throws NoSuchFileException, NotDirectoryException
  {
    requireExists(pathForException);
    if (!file().isDirectory()) {
      throw new NotDirectoryException(pathForException.toString());
    }
    return this;
  }
  
  public DirectoryEntry requireSymbolicLink(Path pathForException)
    throws NoSuchFileException, NotLinkException
  {
    requireExists(pathForException);
    if (!file().isSymbolicLink()) {
      throw new NotLinkException(pathForException.toString());
    }
    return this;
  }
  
  public Directory directory()
  {
    return this.directory;
  }
  
  public Name name()
  {
    return this.name;
  }
  
  public File file()
  {
    Preconditions.checkState(exists());
    return this.file;
  }
  
  @Nullable
  public File fileOrNull()
  {
    return this.file;
  }
  
  public boolean equals(Object obj)
  {
    if ((obj instanceof DirectoryEntry))
    {
      DirectoryEntry other = (DirectoryEntry)obj;
      return (this.directory.equals(other.directory)) && (this.name.equals(other.name)) && (Objects.equals(this.file, other.file));
    }
    return false;
  }
  
  public int hashCode()
  {
    return Objects.hash(new Object[] { this.directory, this.name, this.file });
  }
  
  public String toString()
  {
    return MoreObjects.toStringHelper(this).add("directory", this.directory).add("name", this.name).add("file", this.file).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\DirectoryEntry.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */