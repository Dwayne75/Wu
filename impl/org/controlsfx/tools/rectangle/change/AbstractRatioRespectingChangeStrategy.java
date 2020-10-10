package impl.org.controlsfx.tools.rectangle.change;

abstract class AbstractRatioRespectingChangeStrategy
  extends AbstractBeginEndCheckingChangeStrategy
{
  private final boolean ratioFixed;
  private final double ratio;
  
  protected AbstractRatioRespectingChangeStrategy(boolean ratioFixed, double ratio)
  {
    this.ratioFixed = ratioFixed;
    this.ratio = ratio;
  }
  
  protected final boolean isRatioFixed()
  {
    return this.ratioFixed;
  }
  
  protected final double getRatio()
  {
    if (!this.ratioFixed) {
      throw new IllegalStateException("The ratio is not fixed.");
    }
    return this.ratio;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\tools\rectangle\change\AbstractRatioRespectingChangeStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */