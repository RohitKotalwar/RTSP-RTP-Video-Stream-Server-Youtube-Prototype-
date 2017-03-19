
/* ------------------
   Client
   usage: java Client [Server hostname] [Server RTSP listening port] [Video file requested]
      //ClientPORT = 4000;  
   ---------------------- */

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

public class Client{

  //GUI
  //----
  JFrame f = new JFrame("Client");
  //JButton setupButton = new JButton("SETUP");
  JButton playButton = new JButton("PLAY");
  JButton pauseButton = new JButton("PAUSE");
  JButton tearButton = new JButton("TERMINATE");
  JPanel mainPanel = new JPanel();
  JPanel buttonPanel = new JPanel();
  static JLabel iconLabel = new JLabel();
  static ImageIcon icon;


  //RTP variables:
  //----------------
  DatagramPacket rcvdp; //UDP packet received from the server
  DatagramSocket RTPsocket; //socket to be used to send and receive UDP packets
  static int RTP_RCV_PORT = 25000; //port where the client will receive the RTP packets
  
  Timer timer; //timer used to receive data from the UDP socket
  byte[] buf; //buffer used to store data received from the server 
 
  //RTSP variables
  //----------------
  //rtsp states 
  final static int INIT = 0;
  final static int SESS_START = 1;
  final static int PLAYING = 2;
  final static int PAUSED = 3;
  final static int TERMINATED = 4;
  static int state; //RTSP state == INIT or READY or PLAYING
  Socket RTSPsocket; //socket used to send/receive RTSP messages
  //input and output stream filters
  static BufferedReader RTSPBufferedReader;
  static BufferedWriter RTSPBufferedWriter;
  static String VideoFileName; //video file to request to the server
  int RTSPSeqNb = 0; //Sequence number of RTSP messages within the session
  int RTSPid = 0; //ID of the RTSP session (given by the RTSP Server)

  final static String CRLF = "\r\n";

  //Video constants:
  //------------------
  static int MJPEG_TYPE = 26; //RTP payload type for MJPEG video
 
  //--------------------------
  //Constructor
  //--------------------------
  public Client() {

    //Frame
    f.addWindowListener(new WindowAdapter() {
       public void windowClosing(WindowEvent e) {
	 System.exit(0);
       }
    });

    //Buttons
    buttonPanel.setLayout(new GridLayout(1,0));
    //buttonPanel.add(setupButton);
    buttonPanel.add(playButton);
    buttonPanel.add(pauseButton);
    buttonPanel.add(tearButton);
    //setupButton.addActionListener(new setupButtonListener());
    playButton.addActionListener(new playButtonListener());
    pauseButton.addActionListener(new pauseButtonListener());
    tearButton.addActionListener(new tearButtonListener());

    //Image display label
    iconLabel.setIcon(null);
    
    //frame layout
    mainPanel.setLayout(null);
    mainPanel.add(iconLabel);
    mainPanel.add(buttonPanel);
    iconLabel.setBounds(0,0,380,280);
    buttonPanel.setBounds(0,280,380,50);
    iconLabel.setBackground(Color.pink);
    iconLabel.setOpaque(true);

    f.getContentPane().add(mainPanel, BorderLayout.CENTER);
    f.setSize(new Dimension(390,370));
    f.setVisible(true);

    //init timer
    //--------------------------

    //allocate enough memory for the buffer used to receive data from the server
    buf = new byte[15000];    
  }

  public static void main(String argv[]) throws Exception
  {
    int i=0;
    int ClientPORT = 0;

    
    System.out.println("Enter Server IP Address: ");
    String ServerHost = System.console().readLine();
    InetAddress ServerIPAddr = InetAddress.getByName(ServerHost);

    System.out.println("Enter Server Port: ");
    int RTSP_server_port  = Integer.parseInt(System.console().readLine());


    Client theClient = new Client();
    
    theClient.RTSPsocket = new Socket(ServerIPAddr, RTSP_server_port);

    RTSPBufferedReader = new BufferedReader(new InputStreamReader(theClient.RTSPsocket.getInputStream()) );
    RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(theClient.RTSPsocket.getOutputStream()) );

    state = INIT;

    String message = RTSPBufferedReader.readLine();
    System.out.println("Client Port is : " +message);
    ClientPORT = Integer.parseInt(message);

    DatagramSocket DataSocket = null;
    DatagramPacket inPacket = null;
    DataSocket = new DatagramSocket(ClientPORT);
    InetAddress address = InetAddress.getByName("127.0.0.1");
    byte[] inBuf = new byte[65000];


    while(true){
		inPacket = new DatagramPacket(inBuf, inBuf.length);
                DataSocket.receive(inPacket);
                i++;
                RTPDepacketizer rtp_depacketizer = new RTPDepacketizer(inPacket.getData(), inPacket.getLength());
                String msg = new String(inBuf, 0, inPacket.getLength());

                System.out.println("Length of Packet From " + inPacket.getAddress() + ":" + inPacket.getLength());
                System.out.println("Data of Packet From " + inPacket.getAddress() + ":"+ inPacket.getData());
                System.out.println("Number of Packets received: "+i);
                System.out.println();
                int payload_length = rtp_depacketizer.getpayload_length();
                byte [] payload = new byte[payload_length];
                rtp_depacketizer.getpayload(payload);

                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Image image = toolkit.createImage(payload, 0, payload.length);


                icon = new ImageIcon(image);
                iconLabel.setIcon(icon);


    }
  }


  class setupButtonListener implements ActionListener{
    public void actionPerformed(ActionEvent e){
      System.out.println("Setup Button pressed !"); 
      if (state == INIT) 
	{
	  try{  
		System.out.println("INFO: SENT MESSAGE: SETUP");
		RTSPBufferedWriter.write("SETUP "+VideoFileName+" RTSP/1.0"+"\r\n");
		RTSPBufferedWriter.write("CSeq: 200"+"\r\n");
		RTSPBufferedWriter.write("Transport: RTP/UDP;unicast;client_port=7000\r\n");
                RTSPBufferedWriter.flush();


	    	System.out.println("INFO: RECEIVED RESPONSE: ");
                String respLine = RTSPBufferedReader.readLine();
             	String cseqLine = RTSPBufferedReader.readLine();
             	String sessIDLine = RTSPBufferedReader.readLine();
	  
             	System.out.println("INFO: "+respLine);  
             	System.out.println("INFO: "+cseqLine);  
             	System.out.println("INFO: "+sessIDLine);  
          	
	  	state = SESS_START;
	  	System.out.println("INFO: RTSP SESSION STATE: SETUP DONE");
         } catch (Exception se) {
              System.out.println("Socket exception: "+se);
              System.exit(0);
         }
	}
    }
  }
  
  //Handler for Play button
  //-----------------------
  class playButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e){
      System.out.println("Play Button pressed !"); 
      if ((state == INIT) || (state == PAUSED)) {
          try{
                System.out.println("INFO: SENT MESSAGE: SETUP");
                RTSPBufferedWriter.write("PLAY RTSP/1.0"+"\r\n");
                RTSPBufferedWriter.write("CSeq: 201"+"\r\n");
		RTSPBufferedWriter.write("Transport: RTP/UDP;unicast;client_port=7000\r\n");
                RTSPBufferedWriter.write("SessionID: 1"+"\r\n");
                RTSPBufferedWriter.flush();


                System.out.println("INFO: RECEIVED RESPONSE: ");
                String respLine = RTSPBufferedReader.readLine();
                String cseqLine = RTSPBufferedReader.readLine();
                String sessIDLine = RTSPBufferedReader.readLine();

                System.out.println("INFO: "+respLine);
                System.out.println("INFO: "+cseqLine);
                System.out.println("INFO: "+sessIDLine);
                state = PLAYING;
                System.out.println("INFO: RTSP SESSION STATE: PLAYING");
         } catch (Exception se) {
              System.out.println("Socket exception: "+se);
              System.exit(0);
         }
      }
    }
  }

  //Handler for Pause button
  class pauseButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e){
      System.out.println("Pause Button pressed !");
      if (state == PLAYING) {
          try{
                System.out.println("INFO: SENT MESSAGE: PAUSE");
                RTSPBufferedWriter.write("PAUSE RTSP/1.0"+"\r\n");
                RTSPBufferedWriter.write("CSeq: 202"+"\r\n");
                RTSPBufferedWriter.write("Transport: RTP/UDP;unicast;client_port=7000\r\n");
                RTSPBufferedWriter.write("SessionID: 1"+"\r\n");
                RTSPBufferedWriter.flush();


                System.out.println("INFO: RECEIVED RESPONSE: ");
                String respLine = RTSPBufferedReader.readLine();
                String cseqLine = RTSPBufferedReader.readLine();
                String sessIDLine = RTSPBufferedReader.readLine();

                System.out.println("INFO: "+respLine);
                System.out.println("INFO: "+cseqLine);
                System.out.println("INFO: "+sessIDLine);
                state = PAUSED;
                System.out.println("INFO: RTSP SESSION STATE: PAUSED");
         } catch (Exception se) {
              System.out.println("Socket exception: "+se);
              System.exit(0);
         }
      }
    }
  }




  //Handler for Terminate button
  //-----------------------
  class tearButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e){

      System.out.println("Teardown Button pressed !");  

        try{
                System.out.println("INFO: SENT MESSAGE: TEARDOWN");
                RTSPBufferedWriter.write("TEARDOWN  RTSP/1.0"+"\r\n");
                RTSPBufferedWriter.write("CSeq: 203"+"\r\n");
		RTSPBufferedWriter.write("Transport: RTP/UDP;unicast;client_port=7000\r\n");
                RTSPBufferedWriter.write("SessionID: 1"+"\r\n");
                RTSPBufferedWriter.flush();


                System.out.println("INFO: RECEIVED RESPONSE: ");
                String respLine = RTSPBufferedReader.readLine();
                String cseqLine = RTSPBufferedReader.readLine();
                String sessIDLine = RTSPBufferedReader.readLine();

                System.out.println("INFO: "+respLine);
                System.out.println("INFO: "+cseqLine);
                System.out.println("INFO: "+sessIDLine);
                state = TERMINATED;
                System.out.println("INFO: RTSP SESSION STATE: TERMINATED");
         } catch (Exception se) {
              System.out.println("Socket exception: "+se);
              System.exit(0);
         }

    }
  }

}
 
 class RTPDepacketizer {

        public int SizeOfHeader = 12;
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


public int unsigned_int(int nb) {
        if (nb >= 0)
                return (nb);
        else
                return (256 + nb);
        }

} 


