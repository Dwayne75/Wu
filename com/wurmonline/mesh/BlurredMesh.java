package com.wurmonline.mesh;

public final class BlurredMesh
  extends Mesh
{
  private final Mesh mesh;
  private final int factor;
  private Mesh parent;
  private int factorPow;
  
  public BlurredMesh(Mesh mesh, int factor)
  {
    super(mesh.getWidth() / factor, mesh.getHeight() / factor, mesh.getMeshWidth() * factor);
    this.factor = factor;
    if (factor == 1) {
      this.factorPow = 0;
    } else if (factor == 2) {
      this.factorPow = 1;
    } else if (factor == 4) {
      this.factorPow = 2;
    } else if (factor == 8) {
      this.factorPow = 3;
    } else if (factor == 16) {
      this.factorPow = 4;
    } else if (factor == 32) {
      this.factorPow = 5;
    } else if (factor == 64) {
      this.factorPow = 6;
    } else if (factor == 128) {
      this.factorPow = 7;
    } else if (factor == 256) {
      this.factorPow = 8;
    } else {
      throw new IllegalArgumentException("Factor has to be 2^n");
    }
    this.mesh = mesh;
    this.parent = mesh;
  }
  
  public void setParent(Mesh parent)
  {
    this.parent = parent;
  }
  
  public Mesh getParent()
  {
    return this.parent;
  }
  
  public float getTextureScale()
  {
    return this.mesh.getTextureScale() * this.factor;
  }
  
  public Node getNode(int x, int y)
  {
    return this.mesh.getNode(x << this.factorPow, y << this.factorPow);
  }
  
  public int getBlurFactor()
  {
    return this.factor;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\mesh\BlurredMesh.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */