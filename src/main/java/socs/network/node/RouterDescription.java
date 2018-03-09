package socs.network.node;

public class RouterDescription {
  //used to socket communication
  String processIPAddress;
  short processPortNumber;
  //used to identify the service in the simulated network space
  String simulatedIPAddress;



  //status of the service
  RouterStatus status;

  public short getProcessPortNumber() {
    return processPortNumber;
  }

  public String getProcessIPAddress() {return processIPAddress;}

  public String getSimulatedIPAddress() {return simulatedIPAddress;}

  public RouterStatus getStatus() {return status;}

  public RouterDescription(String processIPAddress, short processPortNumber, String simulatedIPAddress) {
    this.processIPAddress = processIPAddress;
    this.processPortNumber = processPortNumber;
    this.simulatedIPAddress = simulatedIPAddress;
  }

  public void setStatus(RouterStatus status) {
    this.status = status;
    System.out.println("set " + simulatedIPAddress + " state to " + status + ";");
  }

}
