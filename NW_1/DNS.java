
/*
 * CS656-003 Group M6
 * Harsh Patel(hsp52), Justin Ayoor(ja573), Vishal Singh(vas27),
 * Rajeev Chanchlani(rnc26), Meghashyam Senapthi(ms2727), Gaurav Daxini(gnd6)
 */

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class DNS
{
	public static int ConvCharToInt(char[] charArray) // Used to parse Port Number into Int Format
	{
		int num = 0;
		for(int i=0; i<charArray.length; i++) {
			num = num*10 + (charArray[i] - '0');
		}
		return num;
	}

	public static byte[] trimBytes(byte[] byteArr) // Used to trim the byte array to get correct array size
	{
		int newlen = 0;
		for(int i=0;i<=byteArr.length;i++) {
			if(byteArr[i] == 13) {		// 13 is the ASCII value for Enter
				newlen = i;
				break;
			}
		}
		byte[] trimByteArray = new byte[newlen];
		for(int i=0;i<trimByteArray.length;i++) {
			trimByteArray[i] = byteArr[i];
		}
		return trimByteArray;
	}

	// Used to Calculate Round trip time of one IP Address from the IP List
	public static int rtt(byte[] req_ip) throws UnknownHostException
	{
		long start_time = System.nanoTime();
		try{
			InetAddress obj = InetAddress.getByAddress(req_ip);
			long finish_time = System.nanoTime();
			return (int)(finish_time - start_time);
		}catch (UnknownHostException e)
		{
			return 0;
		}		

	}

	public static void dns(byte[] host, OutputStream out) // Main DNS method
	{
		try {
			int min_time = 0; 
			InetAddress preferredIP = null; 
			try {
				InetAddress[] ip_list = InetAddress.getAllByName(new String(host)); // Get list of IP Addresses
				for(InetAddress hostIP:ip_list) { // Iteration throughout the IP List
					out.write((" IP: " + hostIP.getHostAddress() + "\n").getBytes()); // Send IP List to Client
					int roundTT = rtt(hostIP.getAddress()); // Calculate Round Trip Time
					if(min_time == 0 || min_time>roundTT) // If Rtt of IP is less than the previous IP Rtt
					{
						min_time = roundTT;
						preferredIP = hostIP; // Update the Preferred IP
					}
				}
				out.write(" Preferred IP: ".getBytes()); 
				out.write((preferredIP.getHostAddress() + "\n").getBytes()); // Send Preferred IP to Client
			} catch (UnknownHostException e) {
				out.write(("NO IP ADDRESS FOUND\n").getBytes());
			}
		} catch (IOException | NumberFormatException e) {
			System.out.println("Some error occured while listening on port " + portNumber);
			System.out.println("[P01 DNS - Error]: "+e.getMessage());
			System.exit(1);
		}
		return;
	}

	static int portNumber;

	public static void main(String[] args) throws IOException
	{
		if(args.length != 1) {
			System.err.println("Usage: java DNS <port number>");
			System.exit(1);
		}
		try {
			char[] p_num = args[0].toCharArray(); // Converting the String Format into a Char array 
			portNumber = ConvCharToInt(p_num); // Conver the Char array to get the Port Number in Integer Format
			System.out.println("DNS Server listening on socket " + portNumber);
			int servingRequest = 0;
			ServerSocket server = new ServerSocket(); // Make a Server Socket
			server.bind(new InetSocketAddress(portNumber)); // Bind Socket with Port Number
			while (true) { // Printing Server Side Information
				Socket client = server.accept(); // Make a Server Socket
				InputStream client_in = client.getInputStream(); 
				OutputStream client_out = client.getOutputStream();
				System.out.println("(" + ++servingRequest + ") Incoming client connection from [" + client.getInetAddress().getHostAddress() + ":" + client.getPort() + "] to me [" + InetAddress.getLocalHost().getHostAddress() + ":" + server.getLocalPort() + "]");
				byte[] requestedHost = new byte[1024]; // Creating byte array for Host Name
				client_in.read(requestedHost, 0, requestedHost.length); //
				requestedHost = trimBytes(requestedHost);
				System.out.println("\tREQ: " + new String(requestedHost));
				dns(requestedHost, client_out);
				client.close();
			}
		}catch (IOException | NumberFormatException e) {
			System.out.println("Some error occured while listening on port " + portNumber);
			System.out.println("[P01 DNS - Error]: "+e.getMessage());
			System.exit(1);
		}
	}
}