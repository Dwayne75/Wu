package com.sun.tools.xjc.generator.unmarshaller.automaton;

public class AbstractTransitionVisitorImpl
  implements TransitionVisitor
{
  public void onEnterElement(Alphabet.EnterElement a, State to)
  {
    onNamed(a, to);
  }
  
  public void onLeaveElement(Alphabet.LeaveElement a, State to)
  {
    onNamed(a, to);
  }
  
  public void onEnterAttribute(Alphabet.EnterAttribute a, State to)
  {
    onNamed(a, to);
  }
  
  public void onLeaveAttribute(Alphabet.LeaveAttribute a, State to)
  {
    onNamed(a, to);
  }
  
  protected void onNamed(Alphabet.Named a, State to)
  {
    onAlphabet(a, to);
  }
  
  public void onInterleave(Alphabet.Interleave a, State to)
  {
    onRef(a, to);
  }
  
  public void onChild(Alphabet.Child a, State to)
  {
    onRef(a, to);
  }
  
  public void onDispatch(Alphabet.Dispatch a, State to)
  {
    onAlphabet(a, to);
  }
  
  public void onSuper(Alphabet.SuperClass a, State to)
  {
    onRef(a, to);
  }
  
  public void onExternal(Alphabet.External a, State to)
  {
    onRef(a, to);
  }
  
  protected void onRef(Alphabet.Reference a, State to)
  {
    onAlphabet(a, to);
  }
  
  public void onBoundText(Alphabet.BoundText a, State to)
  {
    onText(a, to);
  }
  
  public void onIgnoredText(Alphabet.IgnoredText a, State to)
  {
    onText(a, to);
  }
  
  protected void onText(Alphabet.Text a, State to)
  {
    onAlphabet(a, to);
  }
  
  public void onEverythingElse(Alphabet.EverythingElse a, State to)
  {
    onAlphabet(a, to);
  }
  
  protected void onAlphabet(Alphabet a, State to) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\automaton\AbstractTransitionVisitorImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */