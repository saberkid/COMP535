package socs.network.node;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;
import socs.network.service.Client;
import socs.network.service.ClientHandler;
import socs.network.service.Server;
import socs.network.util.Configuration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import socs.network.util.WeightedGraph;
import socs.network.util.WeightedGraph.*;

import static socs.network.node.RouterStatus.TWO_WAY;


public class Router {



  protected LinkStateDatabase lsd;
  Server server;
  RouterDescription rd;


  //assuming that all routers are with 4 ports
  Link[] ports = new Link[4];
  Client[] clients = new Client[4];
  boolean isStarted = false;
  final Object lsa_lock = new Object();



  public LinkStateDatabase getLsd() {
    return lsd;
  }

  public RouterDescription getRd() {
    return rd;
  }

  public Router(Configuration config) {
    rd = new RouterDescription(config.PROCESS_IP, config.getShort("socs.network.router.port"), config.getString("socs.network.router.ip"));
    lsd = new LinkStateDatabase(rd);

  }
  public boolean addLink(Link link){
    for (int i = 0; i < ports.length; ++i) {
      if (ports[i] == null) {
        ports[i] = link;
        return true;
      }
    }
    return false;
  }
  /**
   * output the shortest path to the given destination ip
   * <p/>
   * format: source ip address  -> ip address -> ... -> destination ip
   *
   * @param destinationIP the ip adderss of the destination simulated service
   */

  private void processDetect(String destinationIP) {
    // implement detect command
    ArrayList<Vertex> nodes = new ArrayList<>();
    ArrayList<Edge> edges = new ArrayList<>();

    lsd._store.forEach((routerIp, lsa) -> nodes.add(new Vertex(routerIp)));
    lsd._store.forEach((routerIp, lsa) -> {
      Vertex source = WeightedGraph.findVertex(nodes, routerIp);
      lsa.links.forEach(ld -> {
        if (!ld.linkID.equals(routerIp)) {
          Vertex destination = WeightedGraph.findVertex(nodes, ld.linkID);
          //System.out.println(source.name+ destination.name+ld.tosMetrics);
          edges.add(new Edge(source.name, destination.name, ld.tosMetrics ));
        }
      });
    });

    WeightedGraph wGraph = new WeightedGraph(edges);
    wGraph.dijkstra(rd.getSimulatedIPAddress());

    //System.out.println(rd.getSimulatedIPAddress());
    boolean dIPfound = false;
    for (Vertex vertex: wGraph.getGraph().values()){
      //System.out.println("-> (" + vertex.dist + ")" + vertex.name );
      if(vertex.name.equals(destinationIP)){
        wGraph.printPath(destinationIP);
        dIPfound = true;
        break;
      }
    }
    if(!dIPfound)
      System.out.println("Unknown destination IP address!");

  }

  /**
   * disconnect with the service identified by the given destination ip address
   * Notice: this command should trigger the synchronization of database
   *
   * @param portNumber the port number which the link attaches at
   */
  private void processDisconnect(short portNumber) {

  }

  /**
   * attach the link to the remote service, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link
   * <p/>
   * NOTE: this command should not trigger link database synchronization
   */
  private void processAttach(String processIP, short processPort,
                             String simulatedIP, short weight) {
    RouterDescription attachedRouterDescription = new RouterDescription(processIP, processPort, simulatedIP);
    Link link = new Link(rd, attachedRouterDescription, weight);
    if (addLink(link))
      System.out.println("Link attached");
    else
      System.out.println("Ports are full");


  }

  /**
   * broadcast Hello to neighbors
   */
  private void processStart() {
    if (isStarted){
      System.out.println("Router already started");
      return;
    }
    for (int i = 0; i < ports.length; ++i) {
      if (!isConnected(i)){
      startConnection(i);
      }

    }
    isStarted = true;
  }

  private void startConnection(int i) {
    if (ports[i] == null)
      return;

    Link link = ports[i];
    RouterDescription remoteDescription = link.router1.simulatedIPAddress.equals(rd.simulatedIPAddress) ? link.router2 : link.router1;
    Client client = new Client(this, remoteDescription, link);
    clients[i] = client;
    client.getThreading().start();

  }


  /**
   * attach the link to the remote service, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link
   * <p/>
   * This command does trigger the link database synchronization
   */
  private void processConnect(String processIP, short processPort,
                              String simulatedIP, short weight) {

  }

  /**
   * output the neighbors of the routers
   */
  private void processNeighbors() {

    for (int i=0; i < 4; i++){
      if (ports[i] == null){
        continue;
      }
      Link link = ports[i];
      RouterDescription neighborRd = link.router1.simulatedIPAddress.equals(rd.simulatedIPAddress) ? link.router2 : link.router1;
      RouterStatus neighborStatus = neighborRd.getStatus();


      /*switch (neighborStatus){
        case INIT:
      }*/

      if (neighborStatus == TWO_WAY){
        System.out.println("IP Address of the neighbor" + (i+1) + ": " + neighborRd.getSimulatedIPAddress());
      }

    }
  }

  /**
   * disconnect with all neighbors and quit the program
   */
  private void processQuit() {

  }
  public void lsaUpdate(RouterDescription remoteRd, short weight){
    LinkDescription linkDescription = new LinkDescription(remoteRd.getSimulatedIPAddress(), remoteRd.getProcessPortNumber(), weight);
    LSA lsa = lsd._store.get(rd.simulatedIPAddress);
    boolean isLinkFound = false;

    for (int i = 0; i < lsa.links.size(); ++i) {
      LinkDescription ld = lsa.links.get(i);

      if (ld.linkID.equals(remoteRd.getSimulatedIPAddress())) {
        ld.tosMetrics = linkDescription.tosMetrics;
        isLinkFound = true;
      }
    }
    // if not found, lsaSeqNumber ++
    if (!isLinkFound) {
      lsa.links.add(linkDescription);
      ++lsa.lsaSeqNumber;
    }

  }
  /*
  synchronize LSA
   */
  public boolean synchronize(ArrayList<LSA> lsaArray) {
    boolean updated = false;
    synchronized (lsa_lock) {
      for (LSA lsa : lsaArray) {
        if (isLocalLsaOutdated(lsa)) {
          updated = true;
          lsd._store.put(lsa.linkStateID, lsa);
        }
      }
    }
    return updated;
  }
  /*
   synchronize and propagate the messages
    */
  public void synchronizeAndPropagate(ArrayList<LSA> lsaArray, String lduStarter, String excludedIp) {

    boolean canPropogate = synchronize(lsaArray);
    if(canPropogate){
      propagateLspToNbr(lduStarter, excludedIp);

    }


  }
  /*
   propagate with exceptions  && !clients[i].isConnectedWith(lduStarter)   && !clientHandlers[i].isConnectedWith(lduStarter)
    */
  public void propagateLspToNbr(String lduStarter, String excludedIp){
    for (int i = 0; i < clients.length; ++i) {
      if (clients[i] != null  &&  clients[i].getRemoteRd().status == RouterStatus.TWO_WAY && !clients[i].isConnectedWith(excludedIp)) {
        clients[i].propagate();
      }
    }
    ClientHandler[] clientHandlers = server.getClientHandlers();
    for (int i = 0; i < clientHandlers.length; ++i) {
      if (clientHandlers[i] != null  && clientHandlers[i].getRemoteRd().status == RouterStatus.TWO_WAY && !clientHandlers[i].isConnectedWith(excludedIp)) {
        clientHandlers[i].propagate();
      }
    }

  }

  private boolean isConnected(int i) {
    boolean isConnected = false;

    if (ports[i] != null) {
      String ip = ports[i].router2.simulatedIPAddress;
      ClientHandler[] clientHandlers = server.getClientHandlers();
      for (ClientHandler clientHandler: clientHandlers) {
        if (clientHandler != null && clientHandler.getRemoteRd().simulatedIPAddress.equals(ip)) {
          isConnected = true;
          break;
        }
      }
    }

    return isConnected;
  }
  /*
   check if the local LSA is outdated
    */
  private boolean isLocalLsaOutdated(LSA newLsa) {
    boolean isOutdated = true;
    if (lsd._store.containsKey(newLsa.linkStateID) && lsd._store.get(newLsa.linkStateID).lsaSeqNumber >= newLsa.lsaSeqNumber) {
      isOutdated = false;
    }
    return isOutdated;
  }
  public void terminal() {
    try {
      server = new Server(this);
      server.getThreading().start();


      InputStreamReader isReader = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isReader);
      System.out.print(">> ");
      String command = br.readLine();
      while (true) {
        if (command.startsWith("detect ")) {
          String[] cmdLine = command.split(" ");
          processDetect(cmdLine[1]);
        } else if (command.startsWith("disconnect ")) {
          String[] cmdLine = command.split(" ");
          processDisconnect(Short.parseShort(cmdLine[1]));
        } else if (command.startsWith("quit")) {
          processQuit();
        } else if (command.startsWith("attach ")) {
          String[] cmdLine = command.split(" ");
          processAttach(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("start")) {
          processStart();
        } else if (command.equals("connect ")) {
          String[] cmdLine = command.split(" ");
          processConnect(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("neighbors")) {
          //output neighbors
          processNeighbors();
        } else {
          //invalid command
          break;
        }
        System.out.print(">> ");
        command = br.readLine();
      }
      isReader.close();
      br.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
