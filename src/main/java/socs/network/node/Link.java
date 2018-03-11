package socs.network.node;

public class Link {

  RouterDescription router1;
  RouterDescription router2;
  short weight;

  public short getWeight() {
    return weight;
  }

  public Link(RouterDescription r1, RouterDescription r2, short linkWeight ) {
    router1 = r1;
    router2 = r2;
    weight = linkWeight;
  }
}
