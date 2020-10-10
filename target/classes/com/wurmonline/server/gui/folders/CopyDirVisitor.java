package com.wurmonline.server.gui.folders;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;

public class CopyDirVisitor
  extends SimpleFileVisitor<Path>
{
  private final Path fromPath;
  private final Path toPath;
  private final CopyOption copyOption;
  
  public CopyDirVisitor(Path fromPath, Path toPath, CopyOption copyOption)
  {
    this.fromPath = fromPath;
    this.toPath = toPath;
    this.copyOption = copyOption;
  }
  
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
    throws IOException
  {
    Path target = this.toPath.resolve(this.fromPath.relativize(dir));
    if (!Files.exists(target, new LinkOption[0])) {
      Files.createDirectory(target, new FileAttribute[0]);
    }
    return FileVisitResult.CONTINUE;
  }
  
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
    throws IOException
  {
    Files.copy(file, this.toPath.resolve(this.fromPath.relativize(file)), new CopyOption[] { this.copyOption });
    return FileVisitResult.CONTINUE;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\gui\folders\CopyDirVisitor.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */