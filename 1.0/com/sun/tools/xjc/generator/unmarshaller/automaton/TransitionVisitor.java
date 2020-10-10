package com.sun.tools.xjc.generator.unmarshaller.automaton;

public abstract interface TransitionVisitor
{
  public abstract void onEnterElement(Alphabet.EnterElement paramEnterElement, State paramState);
  
  public abstract void onLeaveElement(Alphabet.LeaveElement paramLeaveElement, State paramState);
  
  public abstract void onEnterAttribute(Alphabet.EnterAttribute paramEnterAttribute, State paramState);
  
  public abstract void onLeaveAttribute(Alphabet.LeaveAttribute paramLeaveAttribute, State paramState);
  
  public abstract void onInterleave(Alphabet.Interleave paramInterleave, State paramState);
  
  public abstract void onChild(Alphabet.Child paramChild, State paramState);
  
  public abstract void onDispatch(Alphabet.Dispatch paramDispatch, State paramState);
  
  public abstract void onSuper(Alphabet.SuperClass paramSuperClass, State paramState);
  
  public abstract void onExternal(Alphabet.External paramExternal, State paramState);
  
  public abstract void onBoundText(Alphabet.BoundText paramBoundText, State paramState);
  
  public abstract void onIgnoredText(Alphabet.IgnoredText paramIgnoredText, State paramState);
  
  public abstract void onEverythingElse(Alphabet.EverythingElse paramEverythingElse, State paramState);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\automaton\TransitionVisitor.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */