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

public class Web
{
	static int portNumber;
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
        
    public static void doParse()
    {

    }

    public static void doHTTP()
    {

    }

    public static void main(String[] args)
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
				byte[] requestedHost = new byte[8196]; // Creating byte array for Host Name
				client_in.read(requestedHost, 0, requestedHost.length);
				requestedHost = trimBytes(requestedHost);
				System.out.println("\tREQ: " + new String(requestedHost) + "\n");
				client.close();
			}
		}catch (IOException | NumberFormatException e) {
			System.out.println("Some error occured while listening on port " + portNumber);
			System.out.println("[P01 DNS - Error]: "+e.getMessage());
			System.exit(1);
		}
    }
}