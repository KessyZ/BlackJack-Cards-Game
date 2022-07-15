package finalProject;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class ClientJoin extends JFrame{
	
	DataInputStream input = null;
	JButton btn;
	Socket socket = null;
	int clientId;
	int[] cards = new int [10];
	
	ClientJoin(){
		super("Client");
		JPanel joinPanel = new JPanel();
		ImagePanel imagePanel = new ImagePanel("cards\\bj.jpg");
		joinPanel.add(imagePanel);
		JPanel btnPanel = new JPanel();
		btn = new JButton("Join the Game !");
		btn.setFont(new Font("Arial", Font.BOLD, 22));
		btn.setBackground(new Color(255,255,255));
		btn.setBounds(350, 250, 250, 50);
		btn.setLocation(290, 350);
		btnPanel.setLayout(null);
		btnPanel.add(btn);
		this.getContentPane().add(btnPanel);
		this.getLayeredPane().add(imagePanel,Integer.MIN_VALUE);
		((JPanel)this.getContentPane()).setOpaque(false);
		//this.add(btnPanel,BorderLayout.CENTER);
		setSize(800, 510);
		
		btn.addActionListener(new JoinListener());
	}
	class JoinListener implements ActionListener { 

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				socket = new Socket("localhost", 8000);// connection request to target host, target port
				input = new DataInputStream(socket.getInputStream());
				clientId = input.readInt();
				
				Client c = new Client(clientId,socket);
				c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				c.setVisible(true);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
	}
	

	public static void main(String[] args) {
		ClientJoin cj = new ClientJoin();
		cj.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    cj.setVisible(true);

	}

}
