package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Lists;
import java.nio.file.CopyOption;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Set;

final class Options
{
  public static final ImmutableSet<LinkOption> NOFOLLOW_LINKS = ImmutableSet.of(LinkOption.NOFOLLOW_LINKS);
  public static final ImmutableSet<LinkOption> FOLLOW_LINKS = ImmutableSet.of();
  private static final ImmutableSet<OpenOption> DEFAULT_READ = ImmutableSet.of(StandardOpenOption.READ);
  private static final ImmutableSet<OpenOption> DEFAULT_READ_NOFOLLOW_LINKS = ImmutableSet.of(StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
  private static final ImmutableSet<OpenOption> DEFAULT_WRITE = ImmutableSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
  
  public static ImmutableSet<LinkOption> getLinkOptions(LinkOption... options)
  {
    return options.length == 0 ? FOLLOW_LINKS : NOFOLLOW_LINKS;
  }
  
  public static ImmutableSet<OpenOption> getOptionsForChannel(Set<? extends OpenOption> options)
  {
    if (options.isEmpty()) {
      return DEFAULT_READ;
    }
    boolean append = options.contains(StandardOpenOption.APPEND);
    boolean write = (append) || (options.contains(StandardOpenOption.WRITE));
    boolean read = (!write) || (options.contains(StandardOpenOption.READ));
    if (read)
    {
      if (append) {
        throw new UnsupportedOperationException("'READ' + 'APPEND' not allowed");
      }
      if (!write) {
        return options.contains(LinkOption.NOFOLLOW_LINKS) ? DEFAULT_READ_NOFOLLOW_LINKS : DEFAULT_READ;
      }
    }
    if (options.contains(StandardOpenOption.WRITE)) {
      return ImmutableSet.copyOf(options);
    }
    return new ImmutableSet.Builder().add(StandardOpenOption.WRITE).addAll(options).build();
  }
  
  public static ImmutableSet<OpenOption> getOptionsForInputStream(OpenOption... options)
  {
    boolean nofollowLinks = false;
    for (OpenOption option : options) {
      if (Preconditions.checkNotNull(option) != StandardOpenOption.READ) {
        if (option == LinkOption.NOFOLLOW_LINKS) {
          nofollowLinks = true;
        } else {
          throw new UnsupportedOperationException("'" + option + "' not allowed");
        }
      }
    }
    return nofollowLinks ? NOFOLLOW_LINKS : FOLLOW_LINKS;
  }
  
  public static ImmutableSet<OpenOption> getOptionsForOutputStream(OpenOption... options)
  {
    if (options.length == 0) {
      return DEFAULT_WRITE;
    }
    ImmutableSet<OpenOption> result = ImmutableSet.copyOf(options);
    if (result.contains(StandardOpenOption.READ)) {
      throw new UnsupportedOperationException("'READ' not allowed");
    }
    return result;
  }
  
  public static ImmutableSet<CopyOption> getMoveOptions(CopyOption... options)
  {
    return ImmutableSet.copyOf(Lists.asList(LinkOption.NOFOLLOW_LINKS, options));
  }
  
  public static ImmutableSet<CopyOption> getCopyOptions(CopyOption... options)
  {
    ImmutableSet<CopyOption> result = ImmutableSet.copyOf(options);
    if (result.contains(StandardCopyOption.ATOMIC_MOVE)) {
      throw new UnsupportedOperationException("'ATOMIC_MOVE' not allowed");
    }
    return result;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\Options.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */