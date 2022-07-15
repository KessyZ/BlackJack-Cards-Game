package finalProject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import javax.swing.*;

public class Server extends JFrame implements Runnable{
	ObjectOutputStream toPlayer = null;
	DataInputStream fromPlayer = null;
	private JTextArea ta;
	private int clientall=0;
	private int clientNo = 0;
	private int bustNum = 0; // record the number of busted player
	private int sendNum = 0;
	private int [] cards = new int[53];
	private int [] sum = new int[5]; // bust:-1, else record sum of cards value 
	private int [] blackjack = new int[5]; // 1: ace, 2: J/Q/K ,3: blackjack!
	private int [] dealerhand = new int [10];
	private int [][] playershand = new int [5][10];
	ServerSocket serverSocket;
	
	
	public Server() {
		ta = new JTextArea(10,10);
		JScrollPane sp = new JScrollPane(ta);
		this.add(sp);
		this.setTitle("Dealer");
		this.setSize(400,400);
		Thread t = new Thread(this);
		t.start();
	}
	
	@Override
	  public void run() {
		  try {
	        // Create a server socket
	        ServerSocket serverSocket = new ServerSocket(8000);
	        ta.append("MultiThreadServer started at "   + new Date() + '\n');
	        for(int i=1;i<=52;i++) cards[i] = 1;
	        for(int i=0;i<=3;i++) sum[i] = 0;//calculate sum of hand cards in player
	        dealerhand[0] = 0;

	        while (true) {
	          // Listen for a new connection request
	          Socket socket = serverSocket.accept(); // get connected with socket(client)
	    
	          // Increment clientNo
	          clientNo++;
	          
	          ta.append("Starting thread for client " + clientNo +
	              " at " + new Date() + '\n');
	            // Find the client's host name, and IP address
	          InetAddress inetAddress = socket.getInetAddress();
	          ta.append("Client " + clientNo + "'s IP Address is "
	              + inetAddress.getHostAddress() + "\n");
	          
	          // Create and start a new thread for the connection
	          new Thread(new Handle(socket, clientNo,cards)).start();
	          // everytime find connection request, start a  new thread
	        }
	      }
	      catch(IOException ex) {
	        System.err.println(ex);
	      }
		    
	  }
	  class Handle implements Runnable {
		    private Socket socket; // A connected socket
		    private int clientNum;
		    private int[] cards;
		    
		    /** Construct a thread */
		    public Handle(Socket socket, int clientNum, int[] cards) {
		      this.socket = socket;
		      this.clientNum = clientNum;
		      this.cards = cards; /// hit和initial的时候 要synchronize!
		    }

		    /** Run a thread */
		    public void run() {
		      try {
		        // Create data input and output streams// 
		        DataInputStream fromClient = new DataInputStream(
		          socket.getInputStream());
		        DataOutputStream toClient = new DataOutputStream(
			            socket.getOutputStream());
			    toClient.writeInt(clientNum);

		        // Continuously serve the client
			    int gameOver = 1;// mark end of game in one thread
		        while (true) {
		          // Receive from the client
		          int msg = fromClient.readInt();
		          int card1,card2,card3,card4;
		          //msg 1: start , initial 2 cards
		          if(msg==1 && gameOver==1) {// initial
		        	  gameOver = 0;// start
		        	  card1 = getCard(cards,clientNum);
		        	  toClient.writeInt(card1);
		        	  card2 = getCard(cards,clientNum);
		        	  toClient.writeInt(card2);
		        	  sum[clientNum] += getCardValue(card1,clientNum)+getCardValue(card2,clientNum);
		        	  System.out.println("start: send card "+card1+", card "+card2+" to player "+clientNum);
		        	  ta.append("start: send card "+card1+", card "+card2+" to player "+clientNum+ '\n');
		        	  if(dealerhand[0]==0) { // when dealer have no card
		        		  card3 = getCard(cards,0);
		        		  card4 = getCard(cards,0);   // dealer's hole card
			        	  dealerhand[0] = card3;
			        	  dealerhand[1] = card4;
			        	  dealerhand[2] = -1;// end
			        	  sum[0] += getCardValue(card3,0)+getCardValue(card4,0);
		        	  }
		        	  else {// dealer already has cards
		        		  card3 = dealerhand[0]; card4 = dealerhand[1];		        	  
		        	  }		        		  
		        		  
		        	  toClient.writeInt(card3); // dealers revealing card
		        	  toClient.writeInt(card4);
		        	  ta.append("Initial: dealer get card "+card3+", "+card4+'\n');
		        	  
		        	  //other players' initial card
		        	  for(int i=1;i<=3;i++) {
		        		  if(i!=clientNum) { // could del
		        			  while(playershand[i][1]<=0) {
		        				  toClient.writeInt(-100);// wait for other player!
		        			  }
		        			  System.out.println("others "+i+": "+playershand[i][0]+", "+playershand[i][1]);
		        			  toClient.writeInt(playershand[i][0]); // first 2 card of other player
		        			  toClient.writeInt(playershand[i][1]);
		        			  playershand[i][2] = -1;//end
		        		  }
		        	  }
		        	  if(blackjack[clientNum]==3||blackjack[0]==3) {
			    			clientall++; gameOver = 1;
			    			//break;
			    		}  
		          }
		          //msg 2 hit: random 1 cards
		          else if(msg==2 && gameOver==0) { // after start ,hit
		        	  card1 = getCard(cards,clientNum);
		        	  sum[clientNum] += getCardValue(card1,clientNum);
		        	  toClient.writeInt(card1); 
		        	  ta.append("send card "+card1+" to player"+'\n');
		        	  if(sum[clientNum]>21) {//bust
		        		  ta.append("player"+clientNum+" BUST! Stop send card"+'\n');
		        		  sum[clientNum] = -1;
		        		  bustNum++;
		        		  clientall++;
		        		  gameOver = 1;
		        		 // break;
		        	  }
		          }
		          //stand: stop
		          else if(msg==3  && gameOver==0) {
		        	  ta.append("player"+clientNum+" stand, hold "+sum[clientNum]+'\n');
		        	  clientall++;
		        	  gameOver = 1;
		        	  //break;
		          }
		          else {
		        	  ta.append("Wrong behavior!");
		          }
		          
		          if(gameOver==1) {

		        	  while(clientall<=2) {// some players hasn't finished
		        			toClient.writeInt(-100);// wait for other player done!
				     }
				      // if all bust, dealer doesn't need to getcard
		        	  if(clientall==3) { // all players finish dealing 
		        		  System.out.println("all players dealing over, dealer's turn");
		        		  synchronized(sum) {// only need to get card once
		        			  if(bustNum<3 && sum[0]<17) {// all bust ? else dealer get more card till >17
		        				  int i=2;
				        		  for(i=2;sum[0]<17;i++) {
				        			  int card = getCard(cards,0);
				        			  sum[0] += getCardValue(card,0);
				        			  dealerhand[i] = card;
				        			}
				        		  dealerhand[i] = -1;// end
				       		  }
				       	  }
				       // dealer already get enough card & send
		        		
		        	   if(true) {// bust no need to send other cards
		        		   for(int i=1;i<10;i++) {// first card already record
		        			   toClient.writeInt(dealerhand[i]);
		        			   System.out.println("dealer get&send "+ dealerhand[i]);
		        			   if(dealerhand[i]==-1) break; // send -1 end
				       		}	        	
		        		   if(sum[0]>21) ta.append("Dealer BUST!");
		        		   else ta.append("Dealer hold "+sum[0]+'\n');
				        	
		        		   //send other players' card
		        		   for(int i=1;i<=3;i++) {
		        			   if(i!=clientNum) { 
		        				   for(int j=2;playershand[i][j]!=-1;j++) {
		        					   toClient.writeInt(playershand[i][j]);
		        					   System.out.println("send "+playershand[i][j]+" from player"+i+" to "+clientNum);
		        				   }
		        				   toClient.writeInt(-1); //mark the end
		        			   }     		
		        		   }
		        	   }
				        sendNum ++;// already send all cards to player
				        if(sendNum==3) clearCard(clientNum);  // players game over
				     }		        	         	  
		          }		          
		        }		        
		      }
		      catch(IOException ex) {
		        ex.printStackTrace();
		      }
		  }
	}
	public synchronized int getCard(int [] cards, int clientNum) { // randomly get one cards from rest of cards
		Random r = new Random();
		int i = r.nextInt(52);
		while(cards[i]==0) {
			i = r.nextInt(52);
		}
		cards[i] = 0;
		int j=0;
		while(playershand[clientNum][j]>0) j++; 
		playershand[clientNum][j] = i;
		playershand[clientNum][j+1] = -1;// mark the end
		return i;
	}
	public int getCardValue(int card, int clientNum) {
		int val = card%13;
		if(val==0||val>10) blackjack[clientNum] += 2;// JQK
		else if(val==1) blackjack[clientNum] += 1; //ace
			
		if(val==0 || val>=10) return 10;// 10,JQK
		else return val;
		
	}
	public void clearCard(int clientNum) {
		clientall = 0;
		bustNum = 0;
		sendNum = 0;
		for(int i=1;i<=52;i++) cards[i] = 1;
		for(int i=0;i<=3;i++) {
			 sum[i] = 0;
		     blackjack[i] = 0;
		     playershand[i] = new int [10];
		}
        dealerhand = new int [10];
        dealerhand[0] = 0;
	}
	public static void main(String[] args) {
		Server s = new Server();
		s.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    s.setVisible(true);

	}

}
