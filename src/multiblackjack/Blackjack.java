package multiblackjack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Blackjack extends Thread {

  static final int DURATION_WAIT_FOR_GAMERS = 15000; // in millisecs
  private final ArrayList<Client> potentialClients;
  
  public Blackjack(ArrayList<Client> potentialClients) {
    this.potentialClients = potentialClients;
  }
  
  @Override
  public void run() {
    ArrayList<Client> clients = new ArrayList<>();
    
    waitFirstCheckIn();

    try {
      sleep(DURATION_WAIT_FOR_GAMERS);
    } catch (InterruptedException ex) {
      Logger.getLogger(Blackjack.class.getName()).log(Level.SEVERE, null, ex);
    }

    moveNamedClientsIntoGame(potentialClients, clients);
    
    for (Client client : clients) {
      client.setName(client.receive());
      client.setState(ClientState.IN_GAME);
      initializeHand(client);
    }

    MultiBlackjack.startNewGame();
    
    Boolean anybodyInGame;
    do {
      anybodyInGame = false;
      for (Client client : clients) {
        if (client.getState() == ClientState.IN_GAME) {
          if (client.receive().equals("hit")) {  // receive client command: hit/stick
            addCardToHandAndSetState(client);
          } else { // cmd.equals("stick"
            client.setState(ClientState.STICK);
          }
        }
        switch (client.getState()) {
          case IN_GAME:
          case STICK:
            client.send(Integer.toString(client.getInHand()));
            break;
          case BUST:
            client.send(ClientState.BUST.getValue());
            break;
        }
        anybodyInGame = (anybodyInGame || (client.getState() == ClientState.IN_GAME));
      }
    } while (anybodyInGame);

    String winnerName = evaluateWinner(clients);
      for (Iterator<Client> it = clients.iterator(); it.hasNext(); ) {
      it.next().send(winnerName);
    }
  }
  
  private void waitFirstCheckIn() {
    Boolean someoneCheckedIn = false;
    
    while (!someoneCheckedIn) {
      synchronized(potentialClients) {
        for (Client client : potentialClients) {
          if (client.getSc().hasNextLine()) {
            someoneCheckedIn = true;
            break;
          }
        }
      }
    }
  }
  
  private void moveNamedClientsIntoGame(ArrayList<Client> pcs, ArrayList<Client> cs){
    synchronized(pcs) {
      for (Client client : pcs) {
        if (client.getSc().hasNextLine()) {
          cs.add(client);
        }
      }
      pcs.removeAll(cs);
    }
  }
    
  private static void initializeHand(Client client) {
    int cardValueDrawn = drawCard() + drawCard();
    client.setInHand(cardValueDrawn);
    client.send(Integer.toString(cardValueDrawn));
  }
  
  private static void addCardToHandAndSetState(Client client) {
    int cardValueDrawn = drawCard();
    int inHand = client.getInHand() + cardValueDrawn;
    client.setInHand(inHand);
    client.send(Integer.toString(cardValueDrawn));
    if (inHand > 21) {
      client.setState(ClientState.BUST);
    }
  }
  
  private static String evaluateWinner(ArrayList<Client> clients) {
    String name = "Dealer/None";
    int maxInHand = 0;
        for (Iterator<Client> it = clients.iterator(); it.hasNext(); ) {
      Client client = it.next();
      if (client.getState() != ClientState.BUST && client.getInHand() > maxInHand) {
        maxInHand = client.getInHand();
        name = client.getName();
      }
    }
    return name;
  }

  private static int drawCard() {
    //return (int)(2.0 + 9 * Math.random());
    return (new Random()).nextInt(9) + 2;
  }
}
