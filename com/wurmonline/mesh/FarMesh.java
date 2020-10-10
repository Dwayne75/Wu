package com.wurmonline.mesh;

public class FarMesh
  extends Mesh
{
  public FarMesh(int width, int height, int meshWidth)
  {
    super(width, height, meshWidth);
    generateEmpty(true);
  }
  
  public void setFarTiles(int xStart, int yStart, int w, int h, short[][] tiles)
  {
    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++)
      {
        Node node = getNode(x + xStart, y + yStart);
        node.setHeight(Tiles.shortHeightToFloat(tiles[x][y]));
      }
    }
    calculateNormals(xStart - 1, yStart - 1, xStart + w + 2, yStart + h + 2);
    processData(xStart - 1, yStart - 1, xStart + w + 2, yStart + h + 2);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\mesh\FarMesh.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */