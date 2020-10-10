package com.wurmonline.server.concurrency;

import java.util.ArrayList;
import java.util.List;

public class GenericThreadPoolWithListTester
{
  public static void main(String[] args)
  {
    try
    {
      Thread.sleep(1000L);
    }
    catch (InterruptedException e1)
    {
      e1.printStackTrace();
    }
    long lLastID = 10000L;
    List<Pollable> lInputList = new ArrayList(10010);
    for (long j = 0L; j < 10000L; j += 1L) {
      lInputList.add(new GenericThreadPoolWithListTester.1());
    }
    for (int lNumberOfTasks = 1; lNumberOfTasks < 50; lNumberOfTasks++) {
      GenericThreadPoolWithList.multiThreadedPoll(lInputList, lNumberOfTasks);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\concurrency\GenericThreadPoolWithListTester.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */