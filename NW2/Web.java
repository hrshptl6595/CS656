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
import java.util.Arrays;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
public class Web {
    static final int MAX_REQUEST_LENGTH = 65535;
    static final int MAX_URI_LENGTH = 2048;
    static final byte CARRIAGE_RETURN = 13;
    static final byte NULL_ASCII = 0;
    static final int HTTP_PORT = 80;
    static final int MAX_RESPONSE_BUFFER = 1024*1024;
    static final int HTTP_BAD_REQUEST_STATUS_CODE = 400;
    static final byte[] HTTP_BAD_REQUEST_STATUS = "Bad Request".getBytes(); 
    static final int HTTP_FORBIDDEN_STATUS_CODE = 403;
    static final byte[] HTTP_FORBIDDEN_STATUS = "Forbidden".getBytes();
    static final int HTTP_NOT_FOUND_STATUS_CODE = 404;
    static final byte[] HTTP_NOT_FOUND_STATUS = "Not Found".getBytes();
    
    public static int ConvCharToInt(char[] charArray) { // Used to parse Port Number into Int Format
        int num = 0;
        for (int i = 0; i < charArray.length; i++) {
            num = num * 10 + (charArray[i] - '0');
        }
        return num;
    }
    public static boolean byteArrContains(byte[] needle, byte[] haystack) {
        for (int i = 0; i < haystack.length - needle.length + 1; i++) {
            boolean found = true;
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return found;
        }
        return false;
    }
    public static byte[] trimBytes(byte[] byteArr) { // Used to trim the byte array to get correct array size
        int newlen = 0;
        for (int i = 0; i < byteArr.length; i++) {
            if (byteArr[i] == CARRIAGE_RETURN || byteArr[i] == NULL_ASCII) {
                newlen = i;
                break;
            }
        }
        byte[] trimByteArray = new byte[newlen];
        for (int i = 0; i < trimByteArray.length; i++) trimByteArray[i] = byteArr[i];
        return trimByteArray;
    }
    // Used to Calculate Round trip time of one IP Address from the IP List
    public static int rtt(byte[] req_ip) throws IOException {
        try {
        	Socket s = new Socket();
        	InetSocketAddress ipAdd = new InetSocketAddress(InetAddress.getByAddress(req_ip), HTTP_PORT);
            long start_time = System.nanoTime();
        	s.connect(ipAdd);
            long finish_time = System.nanoTime();
            return (int)(finish_time - start_time);
        } catch (IOException e) {
            return 0;
        }
    }
    public static byte[] dns(byte[] host, OutputStream client_out) throws Exception{ // Main DNS method
        int min_time = 0;
        byte[] emptyByteArray = new byte[0];
        InetAddress preferredIP = null;
        try {
            InetAddress[] ip_list = InetAddress.getAllByName(new String(host)); // Get list of IP Addresses
            for (InetAddress hostIP: ip_list) { // Iteration throughout the IP List
                int roundTT = rtt(hostIP.getAddress()); // Calculate Round Trip Time
                if (min_time == 0 || min_time > roundTT) // If Rtt of IP is less than the previous IP Rtt
                {
                    min_time = roundTT;
                    preferredIP = hostIP; // Update the Preferred IP
                }
            }
        } catch (IOException e) {
            System.out.println("[P02 Proxy - Error]: " + e.getMessage());
        }
        if(preferredIP == null){
            System.out.println("\t\tNO IP ADDRESS FOUND");
            return emptyByteArray;
        }else{
            return preferredIP.getHostAddress().getBytes();
        }
    }
    /*
     * Will parse the Request Object to get the document path and will also check for blocked hosts
     */
    public static byte[] doParse(byte[] request) throws Exception {
        byte[] httpVerb = new byte[20];
        byte[] requestURI = new byte[MAX_URI_LENGTH];
        byte[] protocol = new byte[20];
        int chunkNum = 1;
        int start = 0, end = 0;
        for (int i = 0; i < request.length; i++) {
            if (request[i] == 32 || i == request.length - 1) {
                end = (i == request.length - 1 ? i : i - 1);
                int k = 0;
                switch (chunkNum) {
                    case 1:
                        for (int j = start; j <= end; j++) httpVerb[k++] = request[j];
                        break;
                    case 2:
                    	if ((end-start) > MAX_URI_LENGTH) { throw new Exception("Request URI Too Long"); }
                        for (int j = start; j <= end; j++) requestURI[k++] = request[j];
                        break;
                    case 3:
                        for (int j = start; j <= end; j++) protocol[k++] = request[j];
                        break;
                }
                start = end + 1;
                chunkNum++;
            }
        }
        if (!byteArrContains("GET".getBytes(), httpVerb)) {
            throw new Exception("Unable to parse the request");
        }
        if(byteArrContains(":".getBytes(), getHostOrPath(requestURI, false))) {
        	throw new Exception("Bad Port");
        }
        return requestURI;
    }
    
    public static void sendHTTPError(int statusCode, byte[] status, byte[] errorMessage, OutputStream client_out) {
    	try {
    		byte[] httpHeader = responseBuilder(("HTTP/1.1 "+ statusCode +" ").getBytes(), status);
    		httpHeader = responseBuilder(httpHeader, "\r\nContent-Type: text/html\r\nConnection: Keep-Alive\r\n\r\n".getBytes());
    		
    		byte[] httpBody = responseBuilder(("<h1>" + statusCode + " ").getBytes(), status);
    		httpBody = responseBuilder(httpBody, "</h1><h4>Error: ".getBytes());
    		httpBody = responseBuilder(httpBody, errorMessage);
    		httpBody = responseBuilder(httpBody, "</h4>".getBytes());
	    	
    		client_out.write(httpHeader);
    		client_out.write(httpBody);
    	} catch (IOException e) {
			System.out.println("[P02 Proxy Error]: " + e.getMessage());
		}
    }
    
    public static byte[] getHostOrPath(byte[] requestURI, boolean needPath) {
        byte[] protocol = {
            requestURI[0],
            requestURI[1],
            requestURI[2],
            requestURI[3],
            requestURI[4],
            requestURI[5]
        };
        int start = 0;
        if (byteArrContains("http:".getBytes(), protocol)) {
            start = protocol.length + 2;
        }
        byte[] host = new byte[MAX_URI_LENGTH];
        byte[] path = new byte[MAX_URI_LENGTH];
        int hostIndex = 0, pathIndex = 0;
        boolean encounteredSlash = false;
        for (int i = start; i < requestURI.length; i++) {
            if (requestURI[i] == 47 && !encounteredSlash) {
                encounteredSlash = true;
            }
            if (encounteredSlash) {
                path[pathIndex++] = requestURI[i];
            } else {
                host[hostIndex++] = requestURI[i];
            }
        }
        return needPath ? trimBytes(path) : trimBytes(host);
    }
    public static byte[] responseBuilder(byte[] previousArr, byte[] newArr) {
        byte[] response = new byte[previousArr.length + newArr.length];
        for (int i = 0; i < response.length; i++) {
            response[i] = i < previousArr.length ? previousArr[i] : newArr[i - previousArr.length];
        }
        return response;
    }
    public static void doHTTP(byte[] hostIP, byte[] requestObject, OutputStream client_out) throws IOException {
    	try {
	    	Socket s = new Socket();
	        s.connect(new InetSocketAddress(InetAddress.getByName(new String(hostIP)), HTTP_PORT));
	    	s.getOutputStream().write(requestObject);
	    	InputStream hostInputStream = s.getInputStream();
	    	int readCount;
	    	byte[] readBytes = new byte[MAX_RESPONSE_BUFFER];
	    	while ((readCount = hostInputStream.read(readBytes, 0, readBytes.length)) != -1) {
				client_out.write(readBytes, 0, readCount);
			}
			client_out.flush();
	    } catch (IOException e) {
	    	System.out.println("[P02 Proxy Error]: " + e.getMessage());
		}
    	return;
    }

    public static byte[] getBlackListedHosts(byte[] fname) throws IOException {
        FileInputStream fin = new FileInputStream(new String(fname));
        byte[] content = new byte[fin.available()];
        int x;
        int i = 0;
        while((x = fin.read()) != -1) {
            content[i] = (byte)x;
            i++;
        }
        return content;
    }
    
    static int portNumber;
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java Web <port number> <blacklist.txt>");
            System.exit(1);
        }
        try {
            byte[] file_name = args[1].getBytes();
            byte[] blacklist_urls = getBlackListedHosts(file_name);
            char[] p_num = args[0].toCharArray(); // Converting the String Format into a Char array 
            portNumber = ConvCharToInt(p_num); // Conver the Char array to get the Port Number in Integer Format
            System.out.println("Web Proxy program by (hsp52@njit.edu) listening on port (" + portNumber + ")");
            int servingRequest = 0;
            ServerSocket server = new ServerSocket(); // Make a Server Socket
            server.bind(new InetSocketAddress(portNumber)); // Bind Socket with Port Number
            // server.bind(server.getLocalSocketAddress(), portNumber);
            while (true) { // Printing Server Side Information
                Socket client = server.accept(); // Make a Server Socket
                InputStream client_in = client.getInputStream();
                OutputStream client_out = client.getOutputStream();
                byte[] requestObject = new byte[MAX_REQUEST_LENGTH]; // Creating byte array for request object
                client_in.read(requestObject, 0, MAX_REQUEST_LENGTH);
                byte[] requestURI = new byte[MAX_URI_LENGTH];
                byte[] request = trimBytes(requestObject);
                System.out.println("\t(" + ++servingRequest + ") REQ: " + new String(request));
                try {
                    requestURI = doParse(request);
                    byte[] host = getHostOrPath(requestURI, false);
                    if(byteArrContains(host, blacklist_urls)) {
                    	byte[] errorMessage = "You are restricted to view this content!".getBytes();
                    	sendHTTPError(HTTP_FORBIDDEN_STATUS_CODE, HTTP_FORBIDDEN_STATUS, errorMessage, client_out);
                    } else {
	                    byte[] hostIP = dns(host, client_out);
	                    if(hostIP.length != 0) {
		                    long sTime = System.nanoTime();
		                    doHTTP(hostIP, requestObject, client_out);
		                    long eTime = System.nanoTime();
		                    System.out.println("Service Time: " + ((eTime - sTime) / 1000000) + " ms" );
	                    }else {
	                    	byte[] errorMessage = "Requested resource not found!".getBytes();
	                    	sendHTTPError(HTTP_NOT_FOUND_STATUS_CODE, HTTP_NOT_FOUND_STATUS, errorMessage, client_out);
	                    }
                    }
                } catch (Exception e) {
                    //System.out.println("[P02 Proxy - Error]: " + e.getMessage());
                	sendHTTPError(HTTP_BAD_REQUEST_STATUS_CODE, HTTP_BAD_REQUEST_STATUS, e.getMessage().getBytes(), client_out);
                }
                client_out.close();
                client.close();
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Some error occured while listening on port " + portNumber);
            System.out.println("[P02 Proxy - Error]: " + e.getMessage());
            System.exit(1);
        }
    }
}