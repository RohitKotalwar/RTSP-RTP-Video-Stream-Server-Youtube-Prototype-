package multicastClient1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.*;
import java.net.*;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MulticastReceiver1 {
	public static void main(String[] args) {

		   Frame f= new Frame();	  
			  
		    MulticastSocket multicastSocket = null;
		    DatagramPacket inPacket = null;
		    byte[] inBuf = new byte[65000];
		    try {
		     
		      multicastSocket = new MulticastSocket(8888);
		      InetAddress address = InetAddress.getByName("224.2.2.3");
		      multicastSocket.joinGroup(address);
		      JFrame frame = new JFrame("Display Image");
		      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		      JPanel panel = (JPanel)frame.getContentPane();
		      JLabel label = new JLabel();
		      while (true) 
		      {
		    	  inPacket = new DatagramPacket(inBuf, inBuf.length);
		    	  multicastSocket.receive(inPacket);
		    	  RTPDepacketizer rtp_packet = new RTPDepacketizer(inPacket.getData(), inPacket.getLength());
		    	  String msg = new String(inBuf, 0, inPacket.getLength());
		    	 
		    	  int payload_length = rtp_packet.getpayload_length();
		    	  byte [] payload = new byte[payload_length];
		    	  rtp_packet.getpayload(payload); 
		    	  Toolkit toolkit = Toolkit.getDefaultToolkit();
		    	  Image image = toolkit.createImage(payload, 0, payload.length);
		    	 
		    	  f.icon = new ImageIcon(image);
		    	  f.iconLabel.setIcon(f.icon);	
		        
		      }
		      
		      
		    } 
		    
		    catch (IOException ioe) 
		    {
		      System.out.println(ioe);
		    }
		  }
		}



		class Frame
		{
			
			JFrame f = new JFrame("Client1");
			JButton setupButton = new JButton("Setup");
			JButton playButton = new JButton("Play");
			JButton pauseButton = new JButton("Pause");
			JButton tearButton = new JButton("Teardown");
			JPanel mainPanel = new JPanel();
			JPanel buttonPanel = new JPanel();
			JLabel iconLabel = new JLabel();
			ImageIcon icon;
			Frame()
			{
				 
				  buttonPanel.setLayout(new GridLayout(1,0));
				  iconLabel.setIcon(null);
				  mainPanel.setLayout(null);
				  mainPanel.add(iconLabel);
				  mainPanel.add(buttonPanel);
				  iconLabel.setBounds(0,0,380,280);
				  buttonPanel.setBounds(0,280,380,50);
				  f.getContentPane().add(mainPanel, BorderLayout.CENTER);
				  iconLabel.setBackground(Color.pink);
				  iconLabel.setOpaque(true);
				  f.setSize(new Dimension(390,370));
				  f.setVisible(true);
			}
		}




class RTPDepacketizer {

	static int SizeOfHeader = 12;
	public int Version;
	public int Padding;
	public int Extension;
	public int CC;
	public int Marker;
	public int PayloadType;
	public int SequenceNumber;
	public int TimeStamp;
	public int SyncSource;
	public byte[] header;
	public int payload_size;
	public byte[] payload;


public RTPDepacketizer(int PType, int Framenb, int Time, byte[] data,
int data_length) {

	Version = 2;
	Padding = 0;
	Extension = 0;
	CC = 0;
	Marker = 0;
	SyncSource = 0;
	SequenceNumber = Framenb;
	TimeStamp = Time;
	PayloadType = PType;
	header = new byte[SizeOfHeader];

	header[1] = (byte) ((Marker << 7) | PayloadType);
	header[2] = (byte) (SequenceNumber >> 8);
	header[3] = (byte) (SequenceNumber);

	for (int i = 0; i < 4; i++)
		header[7 - i] = (byte) (TimeStamp >> (8 * i));

	for (int i = 0; i < 4; i++)
		header[11 - i] = (byte) (SyncSource >> (8 * i));

	payload_size = data_length;
	payload = new byte[data_length];
	payload = data;

}


public RTPDepacketizer(byte[] packet, int packet_size) {

	Version = 2;
	Padding = 0;
	Extension = 0;
	CC = 0;
	Marker = 0;
	SyncSource = 0;


	if (packet_size >= SizeOfHeader) {
		header = new byte[SizeOfHeader];
		for (int i = 0; i < SizeOfHeader; i++)
			header[i] = packet[i];


		payload_size = packet_size - SizeOfHeader;
		payload = new byte[payload_size];
		for (int i = SizeOfHeader; i < packet_size; i++)
			payload[i - SizeOfHeader] = packet[i];


		PayloadType = header[1] & 127;
SequenceNumber = unsigned_int(header[3]) + 256
* unsigned_int(header[2]);
TimeStamp = unsigned_int(header[7]) + 256 * unsigned_int(header[6])
+ 65536 * unsigned_int(header[5]) + 16777216
* unsigned_int(header[4]);
}
}


public int getpayload(byte[] data) {

for (int i = 0; i < payload_size; i++)
data[i] = payload[i];
return (payload_size);

}

public int getpayload_length() {
return (payload_size);
}


public int getlength() {
return (payload_size + SizeOfHeader);
}


public int getpacket(byte[] packet) {

	for (int i = 0; i < SizeOfHeader; i++)
		packet[i] = header[i];
	for (int i = 0; i < payload_size; i++)
		packet[i + SizeOfHeader] = payload[i];
	return (payload_size + SizeOfHeader);
}


public int gettimestamp() {
	return (TimeStamp);
}


public int getsequencenumber() {
return (SequenceNumber);
}


public int getpayloadtype() {
	return (PayloadType);
}


public void printheader() {

	for (int i = 0; i < (SizeOfHeader - 4); i++) {
		for (int j = 7; j >= 0; j--)
			if (((1 << j) & header[i]) != 0)
				System.out.print("1");
			else
				System.out.print("0");
		System.out.print(" ");
	}

	System.out.println();
}


static int unsigned_int(int nb) {
	if (nb >= 0)
		return (nb);
	else
		return (256 + nb);
	}

}
