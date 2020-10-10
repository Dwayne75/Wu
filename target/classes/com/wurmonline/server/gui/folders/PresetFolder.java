package com.wurmonline.server.gui.folders;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class PresetFolder
  extends Folder
{
  private static final Logger logger = Logger.getLogger(PresetFolder.class.getName());
  private boolean original;
  
  public PresetFolder(Path path, boolean original)
  {
    this(path);
    this.original = original;
  }
  
  public PresetFolder(Path path)
  {
    super(path);
  }
  
  @Nullable
  public static PresetFolder fromPath(Path path)
  {
    if (path == null) {
      return null;
    }
    if (!Files.isDirectory(path, new LinkOption[0])) {
      return null;
    }
    for (PresetEntity entity : PresetEntity.values()) {
      if ((entity.isRequired()) && (!entity.existsIn(path))) {
        return null;
      }
    }
    return new PresetFolder(path, PresetEntity.OriginalDir.existsIn(path));
  }
  
  public final String getError()
  {
    for (PresetEntity entity : ) {
      if ((entity.isRequired()) && (!entity.existsIn(this))) {
        return "Preset folder missing: " + entity.filename();
      }
    }
    return "";
  }
  
  public boolean delete()
  {
    return (!this.original) && (super.delete());
  }
  
  public boolean isOriginal()
  {
    return this.original;
  }
  
  public boolean copyTo(Path path)
  {
    if ((!Files.exists(path, new LinkOption[0])) || (!exists())) {
      return false;
    }
    for (PresetEntity entity : PresetEntity.values()) {
      if ((entity != PresetEntity.OriginalDir) && (entity.existsIn(this.path))) {
        try
        {
          if (Files.isDirectory(this.path.resolve(entity.filename()), new LinkOption[0])) {
            Files.walkFileTree(this.path.resolve(entity.filename()), new CopyDirVisitor(this.path.resolve(entity.filename()), path.resolve(entity.filename()), StandardCopyOption.REPLACE_EXISTING));
          } else {
            Files.copy(this.path.resolve(entity.filename()), path.resolve(entity.filename()), new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
          }
        }
        catch (IOException e)
        {
          logger.warning("Unable to copy " + entity.filename() + " from " + this.path.toString() + " to " + path.toString());
          e.printStackTrace();
          return false;
        }
      }
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\gui\folders\PresetFolder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */