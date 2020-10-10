package com.sun.tools.jxc.apt;

import com.sun.tools.xjc.BadCommandLineException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Options
{
  public String classpath = System.getenv("CLASSPATH");
  public File targetDir = null;
  public File episodeFile = null;
  public final List<String> arguments = new ArrayList();
  
  public void parseArguments(String[] args)
    throws BadCommandLineException
  {
    for (int i = 0; i < args.length; i++) {
      if (args[i].charAt(0) == '-')
      {
        int j = parseArgument(args, i);
        if (j == 0) {
          throw new BadCommandLineException(Messages.UNRECOGNIZED_PARAMETER.format(new Object[] { args[i] }));
        }
        i += j;
      }
      else
      {
        this.arguments.add(args[i]);
      }
    }
  }
  
  private int parseArgument(String[] args, int i)
    throws BadCommandLineException
  {
    if (args[i].equals("-d"))
    {
      if (i == args.length - 1) {
        throw new BadCommandLineException(Messages.OPERAND_MISSING.format(new Object[] { args[i] }));
      }
      this.targetDir = new File(args[(++i)]);
      if (!this.targetDir.exists()) {
        throw new BadCommandLineException(Messages.NON_EXISTENT_FILE.format(new Object[] { this.targetDir }));
      }
      return 1;
    }
    if (args[i].equals("-episode"))
    {
      if (i == args.length - 1) {
        throw new BadCommandLineException(Messages.OPERAND_MISSING.format(new Object[] { args[i] }));
      }
      this.episodeFile = new File(args[(++i)]);
      return 1;
    }
    if ((args[i].equals("-cp")) || (args[i].equals("-classpath")))
    {
      if (i == args.length - 1) {
        throw new BadCommandLineException(Messages.OPERAND_MISSING.format(new Object[] { args[i] }));
      }
      this.classpath = args[(++i)];
      
      return 1;
    }
    return 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\jxc\apt\Options.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */