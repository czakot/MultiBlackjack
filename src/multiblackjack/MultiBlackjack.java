package multiblackjack;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiBlackjack {

  private static final int PORT = 2121;
  private static final ArrayList<Client> clients = new ArrayList<>();
  
  public static void main(String[] args) {
    
    try
      (
        ServerSocket multiBlackjack = new ServerSocket(PORT);
      )
    {
      Socket s;
      
      while (true) {
        s = multiBlackjack.accept();
        synchronized(clients) {
          clients.add(new Client(s));
        }
      }
    } catch (IOException ex) {
      Logger.getLogger(MultiBlackjack.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public static void startNewGame() {
    Blackjack blackjack = new Blackjack(clients);
    blackjack.start();
  }
}
