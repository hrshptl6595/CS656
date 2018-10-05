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

public class DNS1 {
	
	public static int ConvCharToInt(char[] charArray) {
		int num = 0;
		for(int i=0; i<charArray.length; i++) {
			num = num*10 + (charArray[i] - '0');
		}
		return num;
	}

	public static byte[] trimBytes(byte[] byteArr) {
		int newlen = 0;
		for(int i=0;i<=byteArr.length;i++) {
			if(byteArr[i] == 13) {		// 13 is the ASCII value for carriage return(CR)
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
	
	public static void dns(byte[] host, OutputStream out) {
		try {
			InetAddress preferredIP = null;
			try {
				InetAddress[] ip_list = InetAddress.getAllByName(new String(host));
				for(InetAddress hostIP:ip_list) {
					out.write((" IP: " + hostIP.getHostAddress() + "\n").getBytes());
					if(preferredIP == null && hostIP.isReachable(1000)) {
						preferredIP = hostIP;
					}	
				}
				/*Socket req_host = new Socket(new String(host), 80);
				byte[] pref_ip = req_host.getInetAddress().getHostAddress().getBytes();*/
				out.write(" Preferred IP: ".getBytes());
				//out.write(pref_ip);
				out.write(preferredIP.getHostAddress().getBytes());
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
	public static void main(String[] args) throws IOException {
		if(args.length != 1) {
			System.err.println("Usage: java DNS <port number>");
			System.exit(1);
		}
		try {
			char[] p_num = args[0].toCharArray();
			portNumber = ConvCharToInt(p_num);
			System.out.println("DNS Server listening on socket " + portNumber);
			int servingRequest = 0;
			ServerSocket ss = new ServerSocket();
			ss.bind(new InetSocketAddress(portNumber));
			while (true) {
				Socket cs = ss.accept();
				InputStream in = cs.getInputStream();
				OutputStream out = cs.getOutputStream();
				System.out.println("(" + ++servingRequest + ") Incoming client connection from [" + cs.getInetAddress().getHostAddress() + ":" + cs.getPort() + "] to me [" + ss.getInetAddress().getHostAddress() + ":" + ss.getLocalPort() + "]");
				byte[] requestedHost = new byte[1024];
				in.read(requestedHost, 0, requestedHost.length);
				requestedHost = trimBytes(requestedHost);
				System.out.println("\tREQ: " + new String(requestedHost));
				dns(requestedHost, out);
				cs.close();
			} 
		}catch (IOException | NumberFormatException e) {
			System.out.println("Some error occured while listening on port " + portNumber);
			System.out.println("[P01 DNS - Error]: "+e.getMessage());
			System.exit(1);
		}
	}
}