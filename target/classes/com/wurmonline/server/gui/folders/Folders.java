package com.wurmonline.server.gui.folders;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public enum Folders
{
  INSTANCE;
  
  private static final Logger logger = Logger.getLogger(Folders.class.getName());
  private HashMap<String, GameFolder> gameFolders = new HashMap();
  private HashMap<String, PresetFolder> presets = new HashMap();
  private GameFolder current;
  private DistFolder dist;
  private Path distPath = Paths.get(System.getProperty("wurm.distRoot", "./dist"), new String[0]);
  private Path gamesPath = Paths.get(System.getProperty("wurm.gameFolderRoot", "."), new String[0]);
  private Path presetsPath = Paths.get(System.getProperty("wurm.presetsRoot", "./presets"), new String[0]);
  
  private Folders() {}
  
  public static Folders getInstance()
  {
    return INSTANCE;
  }
  
  public static ArrayList<GameFolder> getGameFolders()
  {
    ArrayList<GameFolder> gameFolders = new ArrayList();
    gameFolders.addAll(getInstance().gameFolders.values());
    return gameFolders;
  }
  
  @Nullable
  public static GameFolder getGameFolder(String folderName)
  {
    return (GameFolder)getInstance().gameFolders.get(folderName);
  }
  
  public static boolean setCurrent(GameFolder gameFolder)
  {
    if (getInstance().current != null) {
      if (!getInstance().current.setCurrent(false)) {
        return false;
      }
    }
    getInstance().current = gameFolder;
    if (!gameFolder.setCurrent(true)) {
      return false;
    }
    logger.info("Current game folder: " + gameFolder.getName());
    return true;
  }
  
  public static void clear()
  {
    getInstance().gameFolders.clear();
    getInstance().current = null;
    logger.info("Game folders cleared.");
  }
  
  public static GameFolder getCurrent()
  {
    return getInstance().current;
  }
  
  public static boolean loadGames()
  {
    return loadGamesFrom(getInstance().gamesPath);
  }
  
  public static boolean loadGamesFrom(Path parent)
  {
    if (!getInstance().gameFolders.isEmpty()) {
      getInstance().gameFolders = new HashMap();
    }
    try
    {
      DirectoryStream<Path> directoryStream = Files.newDirectoryStream(parent);Throwable localThrowable4 = null;
      try
      {
        for (Path path : directoryStream)
        {
          GameFolder gameFolder = GameFolder.fromPath(path);
          if (gameFolder != null)
          {
            getInstance().gameFolders.put(gameFolder.getName(), gameFolder);
            if (gameFolder.isCurrent()) {
              if (getInstance().current == null) {
                getInstance().current = gameFolder;
              } else if (!gameFolder.setCurrent(false)) {
                return false;
              }
            }
          }
        }
      }
      catch (Throwable localThrowable6)
      {
        localThrowable4 = localThrowable6;throw localThrowable6;
      }
      finally
      {
        if (directoryStream != null) {
          if (localThrowable4 != null) {
            try
            {
              directoryStream.close();
            }
            catch (Throwable localThrowable3)
            {
              localThrowable4.addSuppressed(localThrowable3);
            }
          } else {
            directoryStream.close();
          }
        }
      }
    }
    catch (IOException ex)
    {
      logger.warning("IOException while reading game folders");
      ex.printStackTrace();
      return false;
    }
    return true;
  }
  
  public static boolean loadDist()
  {
    getInstance().dist = DistFolder.fromPath(getInstance().distPath);
    return getInstance().dist != null;
  }
  
  public static DistFolder getDist()
  {
    if (getInstance().dist == null) {
      if (!loadDist())
      {
        logger.warning("Unable to load 'dist' folder, please run Steam validation");
        
        return new DistFolder(getInstance().distPath);
      }
    }
    return getInstance().dist;
  }
  
  public static boolean loadPresets()
  {
    if (!getInstance().presets.isEmpty()) {
      getInstance().presets = new HashMap();
    }
    if (getInstance().dist == null) {
      if (!loadDist())
      {
        logger.warning("Unable to load 'dist' folder, please run Steam validation");
        return false;
      }
    }
    if (!loadPresetsFrom(getInstance().dist.getPath()))
    {
      logger.warning("Unable to load presets from 'dist', please run Steam validation");
      return false;
    }
    if (!Files.exists(getInstance().presetsPath, new LinkOption[0])) {
      try
      {
        Files.createDirectory(getInstance().presetsPath, new FileAttribute[0]);
      }
      catch (IOException ex)
      {
        logger.warning("Could not create presets folder");
        return false;
      }
    }
    return loadPresetsFrom(getInstance().presetsPath);
  }
  
  private static boolean loadPresetsFrom(Path parent)
  {
    try
    {
      DirectoryStream<Path> directoryStream = Files.newDirectoryStream(parent);Throwable localThrowable3 = null;
      try
      {
        for (Path path : directoryStream)
        {
          PresetFolder folder = PresetFolder.fromPath(path);
          if (folder != null) {
            getInstance().presets.put(folder.getName(), folder);
          }
        }
      }
      catch (Throwable localThrowable5)
      {
        localThrowable3 = localThrowable5;throw localThrowable5;
      }
      finally
      {
        if (directoryStream != null) {
          if (localThrowable3 != null) {
            try
            {
              directoryStream.close();
            }
            catch (Throwable localThrowable2)
            {
              localThrowable3.addSuppressed(localThrowable2);
            }
          } else {
            directoryStream.close();
          }
        }
      }
    }
    catch (IOException ex)
    {
      logger.warning("IOException while reading game folders");
      ex.printStackTrace();
      return false;
    }
    return true;
  }
  
  public static Path getGamesPath()
  {
    return getInstance().gamesPath;
  }
  
  public static void addGame(GameFolder folder)
  {
    getInstance().gameFolders.put(folder.getName(), folder);
  }
  
  public static void removeGame(GameFolder folder)
  {
    getInstance().gameFolders.remove(folder.getName());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\gui\folders\Folders.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */