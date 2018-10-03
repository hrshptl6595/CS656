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
	public static char[] ConvByteToChar(byte[] byteArray)
	{
		char[] charArray = (new String(byteArray)).toCharArray();
		return charArray;
	}
	public static InetAddress[] dns(char[] url) throws UnknownHostException
	{
		InetAddress[] ip_list = InetAddress.getAllByName(new String(url));
		return ip_list;
	}
	public static byte[] trimBytes(byte[] byteArr)
	{
		int newlen = 0;
		for(int i=0;i<=byteArr.length;i++)
		{
			if(byteArr[i]==13)
			{
				newlen = i;
				break;
			}
		}
		byte[] trimByteArray = new byte[newlen];
		for(int i=0;i<trimByteArray.length;i++)
		{
			trimByteArray[i] = byteArr[i];
		}
		return trimByteArray;
	}
	static int portNumber;
	public static void main(String[] args) throws IOException {
		if(args.length != 1) {
			System.err.println("Usage: java DNS <port number>");
			System.exit(1);
		}
		try {
			portNumber = Integer.parseInt(args[0]);
			System.out.println("DNS Server listening on socket " + portNumber);
			int servingRequest = 0;
			ServerSocket ss = null;
		// try {
			ss = new ServerSocket();
			ss.bind(new InetSocketAddress(portNumber));
		// } catch (IOException e) {
		// 	System.out.println("Some error occured while listening on port " + portNumber);
		// 	System.out.println("[P01 DNS - Error]: "+e.getMessage());
		// 	System.exit(1);
		// }
			while (true) {
				Socket cs = null;
				OutputStream out = null;
				InputStream in = null;
				// try {
					cs = ss.accept();
					out = cs.getOutputStream();
					in = cs.getInputStream();
					System.out.println("(" + ++servingRequest + ") Incoming client connection from [" + cs.getInetAddress().getHostAddress() + ":" + cs.getPort() + "] to me [" + ss.getInetAddress().getHostAddress() + ":" + ss.getLocalPort() + "]");
					byte[] requestedHost = new byte[100];
					in.read(requestedHost);
					requestedHost = trimBytes(requestedHost);
					System.out.println("\tREQ: " + new String(requestedHost));
					//char[] host_url = ConvByteToChar(requestedHost);
					InetAddress[] ip_list = dns(ConvByteToChar(requestedHost));
					for(InetAddress host:ip_list)
					{
						byte[] ip = host.getHostAddress().getBytes();
						out.write('I');
						out.write('P');
						out.write(':');
						out.write(ip);
						out.write('\n');
					}
					byte[] output = "Preferred IP: ".getBytes();
					Socket req_host = new Socket(new String(ConvByteToChar(requestedHost)), 80);
					byte[] pref_ip = req_host.getInetAddress().getHostAddress().getBytes();
					out.write(output);
					out.write(pref_ip);
					out.write('\n');
					cs.close();
			} 
		}catch (IOException e) {
			System.out.println("Some error occured while listening on port " + portNumber);
			System.out.println("[P01 DNS - Error]: "+e.getMessage());
			System.exit(1);
		} catch (NumberFormatException e){
			System.out.println("Usage: java DNS <port number> should be in Integer Format");
			System.out.println("[P01 DNS - Error]: "+e.getMessage());
			System.exit(1);
		}
	}
}