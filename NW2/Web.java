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
    
    public static void dns(byte[] host, OutputStream out) // DNS method
	{
		try {
			int min_time = 0; 
			InetAddress preferredIP = null; 
			try {
				InetAddress[] ip_list = InetAddress.getAllByName(new String(host)); // Get list of IP Addresses
				for(InetAddress hostIP:ip_list) { // Iteration throughout the IP List
					out.write((" IP: " + hostIP.getHostAddress() + "\n").getBytes()); // Send IP List to Client
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
    }
    
    public static void doParse()
    {

    }

    public static void doHTTP()
    {

    }

    public static void main(String[] args)
    {

    }
}