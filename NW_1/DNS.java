import java.io.IOException;
import java.net.UnknownHostException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class DNS{
    public static char[] ByteToCharConv(byte[] buff) {
        char[] charbuff = new char[buff.length];
        for(int i=0; i<charbuff.length; i++){
            charbuff[i] = (char)buff[i];
        }
        return charbuff;
    }
    public static InetAddress[] dns(char[] c) throws UnknownHostException{
		byte[] bytearr = new byte[c.length];
		for(int i=0;i<c.length;i++){
            bytearr[i]= (byte) c[i];
            // System.out.println(bytearr[i]);
        }
		InetAddress[] ipadd = InetAddress.getAllByName(new String(bytearr));
		return ipadd;
	  }
    public static void main(String[] args) throws IOException{
        if(args.length != 1){
            System.out.println("Usage: java DNS <port number>");
            System.exit(1);
        }
        int portNo = Integer.parseInt(args[0]);
        System.out.println("DNS Server listening on socket " + portNo);
        int servingReq = 0;
        ServerSocket ss = null;
        try
        {
            ss = new ServerSocket();
            ss.bind(new InetSocketAddress(portNo));
        } catch (IOException e) {
            System.out.println("Some error occured while listing on port " + portNo);
			System.out.println("[P01 DNS - Error]: "+e.getMessage());
        }
        while(true)
        {
            Socket cs = null;
            InputStream input = null;
            OutputStream output = null;
            try{
                cs = ss.accept();
                input = cs.getInputStream();
    			output = cs.getOutputStream();
                System.out.println("(" + ++servingReq + ") Client connected on port " + portNo + ". Servicing requests.");
                byte[] bBuff = new byte[100];
                input.read(bBuff);
                int newlen = 0;
                for(int i=0;i<=bBuff.length;i++)
                {
                    if(bBuff[i]==13)
                    {
                        newlen = i;
                        break;
                    }
                }
                byte[] bBuffCSize = new byte[newlen];
                for(int i=0;i<bBuffCSize.length;i++)
                {
                    bBuffCSize[i] = bBuff[i];
                }
                char [] cBuff = ByteToCharConv(bBuffCSize);
                System.out.print("\tREQ: ");
                System.out.print(cBuff);
                System.out.println("\n");
                InetAddress[] ipaddressArr = dns(cBuff);
                for(InetAddress host:ipaddressArr)
                {
                    byte[] bytesArr = (host.getHostAddress()).getBytes();
                    output.write(bytesArr);
                    output.write('\n');
                }
                Socket host_sock = new Socket(new String(cBuff), 80);
                byte[] host_ip = host_sock.getInetAddress().getHostAddress().getBytes();
                byte[] out = "Preffered IP: ".getBytes();
                output.write(out);
                output.write(host_ip);
                output.write('\n');
                cs.close();
            } catch (Exception e) {
            System.out.println("Exception caught when trying to listen on port " + portNo + " or listening for a connection");
            System.out.println("[P01 DNS - Error]: "+e.getMessage());
            }
        }        
    }
}

