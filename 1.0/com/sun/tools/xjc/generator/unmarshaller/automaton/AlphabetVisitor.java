package com.sun.tools.xjc.generator.unmarshaller.automaton;

public abstract interface AlphabetVisitor
{
  public abstract void onEnterElement(Alphabet.EnterElement paramEnterElement);
  
  public abstract void onLeaveElement(Alphabet.LeaveElement paramLeaveElement);
  
  public abstract void onEnterAttribute(Alphabet.EnterAttribute paramEnterAttribute);
  
  public abstract void onLeaveAttribute(Alphabet.LeaveAttribute paramLeaveAttribute);
  
  public abstract void onInterleave(Alphabet.Interleave paramInterleave);
  
  public abstract void onChild(Alphabet.Child paramChild);
  
  public abstract void onSuper(Alphabet.SuperClass paramSuperClass);
  
  public abstract void onDispatch(Alphabet.Dispatch paramDispatch);
  
  public abstract void onExternal(Alphabet.External paramExternal);
  
  public abstract void onBoundText(Alphabet.BoundText paramBoundText);
  
  public abstract void onIgnoredText(Alphabet.IgnoredText paramIgnoredText);
  
  public abstract void onEverythingElse(Alphabet.EverythingElse paramEverythingElse);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\automaton\AlphabetVisitor.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */