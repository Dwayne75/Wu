package com.wurmonline.server.utils;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class SimpleArgumentParser
{
  private final HashMap<String, String> assignedOptions = new HashMap();
  private final HashSet<String> flagOptions = new HashSet();
  private final HashSet<String> unknownOptions = new HashSet();
  
  public SimpleArgumentParser(String[] args, Set<String> allowedOptions)
  {
    for (String arg : args)
    {
      arg = arg.trim();
      if (!arg.isEmpty()) {
        if (!arg.contains("WurmServerLauncher"))
        {
          int assignmentIdx = arg.indexOf('=');
          if (assignmentIdx > 0)
          {
            String option = arg.substring(0, assignmentIdx).toLowerCase(Locale.ENGLISH);
            if (!allowedOptions.contains(option)) {
              System.err.println("Unknown parameter: " + option);
            } else if (assignmentIdx >= arg.length()) {
              this.assignedOptions.put(option, "");
            } else {
              this.assignedOptions.put(option, arg.substring(assignmentIdx + 1));
            }
          }
          else if (allowedOptions.contains(arg))
          {
            this.flagOptions.add(arg.toLowerCase(Locale.ENGLISH));
          }
          else
          {
            System.err.println("Unknown parameter: " + arg);
            this.unknownOptions.add(arg);
          }
        }
      }
    }
  }
  
  public boolean hasOption(String option)
  {
    return (this.flagOptions.contains(option)) || (this.assignedOptions.containsKey(option));
  }
  
  public boolean hasFlag(String option)
  {
    return this.flagOptions.contains(option);
  }
  
  public String getOptionValue(String option)
  {
    return (String)this.assignedOptions.get(option);
  }
  
  public boolean hasUnknownOptions()
  {
    return !this.unknownOptions.isEmpty();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\utils\SimpleArgumentParser.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */