
/***********************************************
 * CMPE 207, Spring 2016                       *
 *                                             *
 **********************************************/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Calendar;

public class AnalyticsServer {
	AnalyticsServer(Socket SocketOfClient)
	{
	File file = new File("ClientDetails.txt");
    if(!file.exists())
    		{
					try {
						file.createNewFile();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
				}
	   FileWriter fileWritter;
	try {
		
		fileWritter = new FileWriter(file.getName(),true);
		BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		bufferWritter.write("Details of connected Clients are:\n" + "  ");
		bufferWritter.write("Port number: "+SocketOfClient.getPort() + "\n " + "  ");
		Calendar calendar = Calendar.getInstance();
		bufferWritter.write("The inet address: "+SocketOfClient.getInetAddress() + "\n"+ "    " );
		bufferWritter.write("Remote Socket Address: "+ SocketOfClient.getRemoteSocketAddress() + "\n" + "      ");
		bufferWritter.write("Local Socket Address: "+ SocketOfClient.getLocalSocketAddress() + "\n"+ "        ");
	    java.sql.Timestamp ClientTimeStamp = new java.sql.Timestamp(calendar.getTime().getTime());
	    bufferWritter.write("Date and Time of streaming the video: " + ClientTimeStamp + "\n\n");
		bufferWritter.close();
	} catch (IOException e1) {
		
		e1.printStackTrace();
	}
	}
	  
}

