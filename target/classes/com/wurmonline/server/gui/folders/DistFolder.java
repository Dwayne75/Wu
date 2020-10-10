package com.wurmonline.server.gui.folders;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class DistFolder
  extends Folder
{
  private static final Logger logger = Logger.getLogger(PresetFolder.class.getName());
  
  public DistFolder(Path path)
  {
    super(path);
  }
  
  @Nullable
  public static DistFolder fromPath(Path path)
  {
    if (path == null) {
      return null;
    }
    if (!Files.isDirectory(path, new LinkOption[0])) {
      return null;
    }
    for (DistEntity entity : DistEntity.values()) {
      if ((entity.isRequired()) && (!entity.existsIn(path)))
      {
        logger.warning("Dist folder missing " + entity.filename());
        return null;
      }
    }
    return new DistFolder(path);
  }
  
  public final Path getPathFor(DistEntity entity)
  {
    return getPath().resolve(entity.filename());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\gui\folders\DistFolder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */