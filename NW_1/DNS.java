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

public class DNS {
	public static char[] ConvByteToChar(byte[] byteArray){
		char[] charArray = (new String(byteArray)).toCharArray();
		return charArray;
	}
	public static int ConvCharToInt(char[] charArray){
		int num = 0;
		for(int i=0; i<charArray.length; i++){
			num = num*10 + (charArray[i] - '0');
		}
		return num;
	}
	public static InetAddress[] dns(char[] url) throws UnknownHostException	{
		InetAddress[] ip_list = InetAddress.getAllByName(new String(url));
		return ip_list;
	}
	public static byte[] trimBytes(byte[] byteArr){
		int newlen = 0;
		for(int i=0;i<=byteArr.length;i++){
			if(byteArr[i]==13){
				newlen = i;
				break;
			}
		}
		byte[] trimByteArray = new byte[newlen];
		for(int i=0;i<trimByteArray.length;i++){
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
			char[] p_num = args[0].toCharArray();
			portNumber = ConvCharToInt(p_num);
			System.out.println("DNS Server listening on socket " + portNumber);
			int servingRequest = 0;
			ServerSocket server = null;
			server = new ServerSocket();
			server.bind(new InetSocketAddress(portNumber));
			while (true) {
				Socket client = null;
				OutputStream client_out = null;
				InputStream client_in = null;
					client = server.accept();
					client_out = client.getOutputStream();
					client_in = client.getInputStream();
					System.out.println("(" + ++servingRequest + ") Incoming client connection from [" + client.getInetAddress().getHostAddress() + ":" + client.getPort() + "] to me [" + server.getInetAddress().getHostAddress() + ":" + server.getLocalPort() + "]");
					byte[] requestedHost = new byte[100];
					client_in.read(requestedHost);
					requestedHost = trimBytes(requestedHost);
					System.out.println("\tREQ: " + new String(requestedHost));
					InetAddress[] ip_list = dns(ConvByteToChar(requestedHost));
					int min_time = 0;
					long start_time, finish_time = 0;
					byte[] pref_ip_out = null;
					for(InetAddress host:ip_list){
						byte[] ip = host.getHostAddress().getBytes();
						byte[] ip_out = "IP: ".getBytes();
						client_out.write(ip_out);
						client_out.write(ip);
						client_out.write('\n');
						start_time = System.currentTimeMillis();
						if(host.isReachable(5000) == true){
							finish_time = System.currentTimeMillis();
							if(min_time == 0 || min_time>((int)(finish_time - start_time))){
								min_time = (int)(finish_time - start_time);
								pref_ip_out = ip;
								client_out.write('\\');
								client_out.write(ip);
								client_out.write('\n');
							}
						}
					}
					byte[] output = "Preferred IP: ".getBytes();
					client_out.write(output);
					client_out.write(pref_ip_out);
					client_out.write('\n');
					client.close();
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