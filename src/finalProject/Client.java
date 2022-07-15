package finalProject;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import javax.swing.*;


public class Client extends JFrame{
	DataOutputStream toDealer = null;
	DataInputStream fromDealer = null;
	Socket socket2 = null;
	JButton hit,stand,start;
	JLabel dealer;
	JLabel [] player = new JLabel[4], pointLabel = new JLabel [4];
	JPanel cardsPanel0 = new JPanel(new GridLayout(1,6));// for dealer
	JPanel [] statusPanel = new JPanel[5], playerPanel = new JPanel[5],controlPanel = new JPanel[5], 
			imagePanel = new JPanel[5], cardsPanel = new JPanel[5];
	int clientId2;
	int [] sum = new int[4];
	int [] blackjack = new int [4];
	int [] points = new int [4];
	
	Client(int clientId,Socket socket){
		super("Client");
		clientId2 = clientId;
		socket2 = socket;
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel topPanel = new JPanel(new BorderLayout());
		JPanel mainPlayerPanel = new JPanel(new GridLayout(1,3));
		topPanel.setPreferredSize(new Dimension(900,200));
		mainPlayerPanel.setPreferredSize(new Dimension(900,400));
		dealer = new JLabel("     Dealer  ready ~ ");
		dealer.setFont(new Font("Arial", Font.ITALIC, 18));
		dealer.setPreferredSize(new Dimension(300,50));
		player[0] = dealer;
		cardsPanel[0] = cardsPanel0;
		cardsPanel[0].setBackground(new Color(0,100,50));
		
		topPanel.add(dealer,BorderLayout.NORTH);
		topPanel.add(cardsPanel[0],BorderLayout.CENTER);
		for(int i=1;i<=3;i++) {
			if(i==clientId2) player[i] = new JLabel("<html>player"+i+" ready?<br> Click 'start' & wait for other players~<html> ");		
			else player[i] = new JLabel("Waiting for player"+i);
			player[i].setFont(new Font("Arial", Font.ROMAN_BASELINE, 16));
			player[i].setPreferredSize(new Dimension(300,35));
			statusPanel[i]= new JPanel(new GridLayout(2,1));
			statusPanel[i].add(player[i]);
			pointLabel[i] = new JLabel("Points: 0");
			pointLabel[i].setFont(new Font("Arial", Font.ROMAN_BASELINE, 16));
			pointLabel[i].setPreferredSize(new Dimension(300,30));
			statusPanel[i].add(pointLabel[i]);
			playerPanel[i] = new JPanel(new BorderLayout());
			playerPanel[i].add(statusPanel[i],BorderLayout.NORTH);
			cardsPanel[i] = new JPanel(new GridLayout(2,3));
			cardsPanel[i].setBackground(new Color(0,100,50));
			//
			//playerPanel[i].add(pointPanel,BorderLayout.NORTH);
			playerPanel[i].add(cardsPanel[i],BorderLayout.CENTER);			
		}
		// buttons
		hit = new JButton("Hit");
		hit.setFont(new Font("Arial", Font.ITALIC, 18));
		hit.setBackground(new Color(200,250,255));
		stand = new JButton("Stand");
		stand.setFont(new Font("Arial", Font.ITALIC, 18));
		stand.setBackground(new Color(200,250,255));
		start = new JButton("Start");
		start.setFont(new Font("Arial", Font.ITALIC, 18));
		start.setBackground(new Color(200,250,255));
		hit.addActionListener(new HitListener());
		stand.addActionListener(new StandListener());
		start.addActionListener(new StartListener());
		if(clientId>0 && clientId<=3) {
			controlPanel[clientId] = new JPanel();
			controlPanel[clientId].add(start);
			controlPanel[clientId].add(hit);
			controlPanel[clientId].add(stand);
			controlPanel[clientId].setBackground(new Color(0,100,50));
			playerPanel[clientId].add(cardsPanel[clientId],BorderLayout.CENTER);
			playerPanel[clientId].add(controlPanel[clientId],BorderLayout.SOUTH);
		}
		else System.out.print("Bad ClientId");
		for(int i=1;i<=3;i++)
			mainPlayerPanel.add(playerPanel[i]);
		
		mainPanel.add(topPanel,BorderLayout.NORTH);
		mainPanel.add(mainPlayerPanel,BorderLayout.SOUTH);
		this.add(mainPanel);
		setSize(1200,650);
		for(int j=0;j<=3;j++) {sum[j] = 0; blackjack[j]=0; points[j]=0;}
		
	}
	
	class StartListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				System.out.println("to "+socket2.getPort()+" start");
				clearCard(clientId2);
				DataInputStream fromDealer = new DataInputStream(socket2.getInputStream());
				DataOutputStream toDealer = new DataOutputStream(socket2.getOutputStream());
				toDealer.writeInt(1); 
				toDealer.flush();
				 // initial 2 cards
				System.out.println("player"+clientId2+" send 1:initial");
				int card1,card2,card3,card4;
				do{
					card1 = fromDealer.readInt();// for player				
				}while(card1<0);
				card2 = fromDealer.readInt();//for player
				card3 = fromDealer.readInt(); //for dealer
				card4 = fromDealer.readInt(); //for dealer
				System.out.println("initial get:"+card1+' '+card2+' '+card3+' '+card4);
				
				sum[clientId2] += getCardValue(card1,clientId2,1)+getCardValue(card2,clientId2,1);
				sum[0] += getCardValue(card3,0,1);//only record first card of dealer
				getCardValue(card4,0,1); // to check blackjack
				ImagePanel imagePanel1 = new ImagePanel("cards\\"+card1+".png");
				ImagePanel imagePanel2 = new ImagePanel("cards\\"+card2+".png");
				ImagePanel imagePanel3 = new ImagePanel("cards\\"+card3+".png");

				cardsPanel[0].add(imagePanel3);
				cardsPanel[0].updateUI();
				cardsPanel[clientId2].add(imagePanel1);
				cardsPanel[clientId2].add(imagePanel2);
				cardsPanel[clientId2].updateUI();
							
				System.out.println("Initial: player"+clientId2+" get cards "+card1+','+card2+", "
						+ "dealer get card "+card3);
				judgeBJ(clientId2,0); // judge self could change dealer
				
				//anyone blackjack, reveal the hole card of dealer
				if(blackjack[0]==3 || blackjack[clientId2]==3) {
					ImagePanel imagePanel4 = new ImagePanel("cards\\"+card4+".png");
					cardsPanel[0].add(imagePanel4);// changed!
					cardsPanel[0].updateUI();
				}
				else{ // both not blackjack then start!
					player[clientId2].setText("    Hit or Stand ?");
				}
				// other players' card
				int other ;
				for(int i=1;i<=3;i++) {
					if(i!=clientId2) {
						do{
							other = fromDealer.readInt();
						}while(other==-100);// wait until other players start
						
						int other2 = fromDealer.readInt();
						player[i].setText("    Got 2 cards~");
						System.out.println("Others: player"+clientId2+" get : "+i+" : "+other+" "+other2);
						sum[i] += getCardValue(other,i,1)+getCardValue(other2,i,1);
						ImagePanel otherImg1 = new ImagePanel("cards\\"+other+".png");
						ImagePanel otherImg2 = new ImagePanel("cards\\"+other2+".png");
						cardsPanel[i].add(otherImg1);// changed!
						cardsPanel[i].updateUI();
						cardsPanel[i].add(otherImg2);// changed!
						cardsPanel[i].updateUI();
						judgeBJ(i,1);// judge other player
					}
				}
				
				///if anyone bj, stop and transfer other cards
				if(blackjack[clientId2]==3||blackjack[clientId2]==3) {//bj
				System.out.println("player"+clientId2+" blackjack & get others cards");										
				///dealer's & other players cards
				transferOtherCard();						
				//judge after all other players done
				for(int id=1;id<=3;id++) 
					Judge(sum,id,1);//other player	
				}
				
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
		}
		
	}
	class HitListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				//!ObjectOutputStream getOutputStream
				fromDealer = new DataInputStream(socket2.getInputStream());
				toDealer = new DataOutputStream(socket2.getOutputStream());
				toDealer.writeInt(2); 
				 // initial 2 cards
				System.out.println("player"+clientId2+" send 2: hit");
				toDealer.flush();
				int card1 = fromDealer.readInt();
				sum[clientId2] += getCardValue(card1,clientId2,0);
				ImagePanel imagePanel = new ImagePanel("cards\\"+card1+".png");			
				cardsPanel[clientId2].add(imagePanel);
				cardsPanel[clientId2].updateUI(); ///!
				if(sum[clientId2]>21) {// only for bust
					Judge(sum,clientId2,0);// self judge
					
					//dealer's & other players cards
					transferOtherCard();
					
					//judge after all other players done
					for(int id=1;id<=3;id++) 
						if(id!=clientId2) Judge(sum,id,1);// judge others
				}

				System.out.println("player"+clientId2+" get cards "+card1);
				///dealer's & other players cards
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
	}
	class StandListener implements ActionListener { // none bust stand ??wrong??
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				fromDealer = new DataInputStream(socket2.getInputStream());
				toDealer = new DataOutputStream(socket2.getOutputStream());
				toDealer.writeInt(3); 
				System.out.println("player"+clientId2+" send 3: stand");
				toDealer.flush();
				
				///dealer's & other players cards
				transferOtherCard();
				
				//judge after all other players done
				for(int id=1;id<=3;id++) 
					if(id==clientId2) Judge(sum,id,0);//self
					else Judge(sum,id,1);//other player			
					
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
	}
	public int getCardValue(int card,int clientId, int bj) { // from card id to card value
		int val = card%13;
		if(bj==1) {// check blackjack
			if(val==0||val>10) blackjack[clientId] += 2;
			else if(val==1) blackjack[clientId] += 1;
		}
		if(val==0 || val>=10) return 10;
		else return val;
		
	}
	
	public void transferOtherCard() {
		System.out.println("transfering cards");
		int card;
		try {
			card = fromDealer.readInt();
			while(card==-100) {// wait for other player done
				card = fromDealer.readInt();
			}
			while(card!=-1) {// dealer
				sum[0] += getCardValue(card,0,0);//from second card of dealer
				System.out.println("dealer get "+ card );
				ImagePanel imagePanel0 = new ImagePanel("cards\\"+card+".png");
				cardsPanel0.add(imagePanel0);
				cardsPanel0.updateUI();
				card = fromDealer.readInt();
			}
			
			/// other players
			for(int i=1;i<=3;i++){// other 2 player
				if(i!=clientId2) {
					int other = fromDealer.readInt();
					while(other!=-1){
						System.out.println("get from "+i+": card "+other);
						sum[i] += getCardValue(other,i,0);
						ImagePanel otherImg1 = new ImagePanel("cards\\"+other+".png");
						cardsPanel[i].add(otherImg1);// changed!
						cardsPanel[i].updateUI();
						other = fromDealer.readInt();
						System.out.println("sum of "+i+": "+sum[i]);
					}
					System.out.println("transfer other cards end");
				}
			}			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void Judge(int [] sum, int clientId, int other) {// after stand compare their value
		//judge
		System.out.println("Judging for "+clientId);
		if(blackjack[clientId]==3||blackjack[0]==3) {
			System.out.println("dealer blackjack");
			return; //already judged by judgeBJ before
		}
		else if(sum[clientId]==-1 || sum[clientId]>21) {
			player[clientId].setText("Hold "+sum[clientId]+" BUST! LOSE the Game.:(");
			points[clientId] -= 10;
			pointLabel[clientId].setText("Points: "+points[clientId]);
			setResImg(clientId,-1);
			System.out.println("player"+clientId+" bust "+"other? "+other);
			if(other==0) {
				dealer.setText("<html>Dealer WIN! :)<br>"
						+ "Click 'start' & wait for other players~ </html>");
				setResImg(0,1);
			}
		}
		else if(sum[0]>21) {
			if(other==0) {
				dealer.setText("<html>Dealer hold "+sum[0]+" BUST! LOSE...:("
						+ "<br>Click 'start' & wait for other players~ </html>");
				setResImg(0,-1);
			}
			player[clientId].setText("Congratulations! player"+clientId+" hold "+sum[clientId]+" WIN ! :)");
			points[clientId] += 10;
			pointLabel[clientId].setText("Points: "+points[clientId]);
			setResImg(clientId,1);
		}
		else {
			if(sum[0]<sum[clientId]) {
				setResImg(clientId,1);
				player[clientId].setText("Congratulations! player"+clientId+" hold "+sum[clientId]+" WIN ! :)");
				points[clientId] += 10;
				pointLabel[clientId].setText("Points: "+points[clientId]);
				if(other==0) {
					setResImg(0,-1);
					dealer.setText("<html>Dealer hold "+sum[0]+" LOSE...:(<br>"
							+ "Click 'start' & wait for other players~ </html>");
				}
			}
			else if(sum[0]==sum[clientId]) {
				setResImg(clientId,0);
				player[clientId].setText("player"+clientId+" hold "+sum[clientId]+" TIE ~");
				if(other==0) {
					dealer.setText("<html>Dealer hold "+sum[0]+" too, TIE~<br>"
							+ "Click 'start' & wait for other players~ </html>");
					setResImg(0,0);
				}
			}
			else if(sum[0]>sum[clientId]){
				setResImg(clientId,-1);
				player[clientId].setText("Sad..player"+clientId+" hold "+sum[clientId]+" LOSE :(");
				points[clientId] -= 10;
				pointLabel[clientId].setText("Points: "+points[clientId]);
				if(other==0) {
					dealer.setText("<html>Dealer hold "+sum[0]+" WIN ! :)<br>"
							+ "Click 'start' & wait for other players~ </html>");
					setResImg(0,1);
				}
			}
		}
	}
	public void judgeBJ(int clientId2, int other) {// judge whether got blackjack
		if(blackjack[clientId2]==3&&blackjack[0]!=3) {
			player[clientId2].setText("<html>BlackJack! WIN! :) <br>"
					+ "Wait for other players, then click 'start' again ~</html>");
			setResImg(clientId2,3);
			points[clientId2] += 20;
			pointLabel[clientId2].setText("Points: "+points[clientId2]);
			if(other==0) {
				player[0].setText("Sad...LOSE..:("); setResImg(0,-1);
			}
		}
		else if(blackjack[0]==3 && blackjack[clientId2]!=3) {
			if(other!=1) {
				player[0].setText("BlackJack! WIN! :)");
				setResImg(0,3);
			}
			player[clientId2].setText("<html>Sad...LOSE..:( <br>"
					+ "Wait for other players, then click 'start' again ~</html>");
			setResImg(clientId2,-1);
			points[clientId2] -= 20;
			pointLabel[clientId2].setText("Points: "+points[clientId2]);
			
		}
		else if(blackjack[0]==3 && blackjack[clientId2]==3) {
			if(other!=1) {
				player[0].setText("BlackJack! TIE! :)"); setResImg(0,0);
			}
			player[clientId2].setText("<html>BlackJack! TIE! :)<br>"
					+ "Wait for other players, then click 'start' again ~</html>");
			points[clientId2] += 10;
			pointLabel[clientId2].setText("Points: "+points[clientId2]);
			setResImg(clientId2,0);
		}
	}
	public void setResImg(int clientId, int result) {
		ImagePanel imagePanel;
		if(result==1) {
			imagePanel = new ImagePanel("cards\\win.jpg");
		}
		else if(result==0) {
			imagePanel = new ImagePanel("cards\\tie.jpg");
		}
		else if(result==-1){
			imagePanel = new ImagePanel("cards\\lose.png");
		}
		else {
			imagePanel = new ImagePanel("cards\\bj.png");
		}
		cardsPanel[clientId].add(imagePanel);
		cardsPanel[clientId].updateUI();
	}
	public void clearCard(int clientId) {

        for(int i=0;i<=3;i++) {
        	sum[i] = 0;
        	blackjack[i] = 0;
        	if(i==0) {
        		dealer.setText("Dealer ready!~");
        		cardsPanel[0].removeAll();
        		cardsPanel[0].updateUI();
        	}
        	else {
        		if(i==clientId) player[i].setText("    player"+i+" get ready & wait for other players~ ");		
        		else player[i].setText("    Waiting for player"+i);
        		cardsPanel[i].removeAll();
        		cardsPanel[i].updateUI();
        	}
        }
	}
	public static void main(String[] args) {
		//Client c =new Client(1);
		//c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    //c.setVisible(true);

	}

}
