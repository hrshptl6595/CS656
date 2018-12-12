import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDP {
	
	static final int MIN_ARGS = 3;
	static final int MAX_ARGS = 6;
	static final int PACKET_SIZE_INDEX = 3;
	static final int TRANSMISSION_DELAY_INDEX = 4;
	static final int BUFFER_SIZE_INDEX = 5;
	static final int DEFAULT_PACKET_SIZE = 512;
	static final int DEFAULT_TRANSMISSION_DELAY = 2;
	static final int DEFAULT_BUFFER_SIZE = 256;
	
	public static byte[] getFileContent(byte[] filePath) {
		File file = new File(new String(filePath));
		FileInputStream fis = null;
		byte[] emptyArr = {};
		try{
			fis = new FileInputStream(file);
			int fileSize = fis.available();
			byte[] fileContent = new byte[fileSize];
			fis.read(fileContent, 0, fileSize);
			return fileContent;
		} catch (IOException e) {
			System.out.println("[P03 UDP Error]: " + e.getMessage());
			System.exit(1);
		} finally {
			try {
				if(fis != null)
					fis.close();
			} catch (IOException e) {
				System.out.println("[P03 UDP Error]: " + e.getMessage());
				System.exit(1);
			}
		}
		return emptyArr;
	}
	
	public static void udpPacketRadioTransmitter(byte[] data, byte[] host, int port, int packetSize, int transmissionDelay, int bufferSize) throws IOException, InterruptedException {
		DatagramSocket socket = new DatagramSocket();
		int bytesAvailable = data.length;
		while(bytesAvailable != 0) {
			System.out.println(bytesAvailable);
			packetSize = bytesAvailable < packetSize ? bytesAvailable : packetSize;
			byte[] packet = new byte[packetSize];
			for (int i = 0; i < packetSize; i++) {
				packet[i] = data[data.length - bytesAvailable + i];
			}
			Thread.sleep(transmissionDelay);
			socket.send(new DatagramPacket(packet, packetSize, InetAddress.getByName(new String(host)), port));
			bytesAvailable -= packetSize;
		}
		socket.close();
	}
	
	public static void main(String[] args) {
		if (!(args.length >= 3 && args.length <= 6)) {
            System.err.println("Usage: java UDP <host> <port number> <file name> <p1 | optional> <p2 | optional> <p3 | optional>");
            System.exit(1);
        }
		byte[] host = args[0].getBytes();
		int portNumber = Integer.parseInt(args[1]);
		byte[] filePath = args[2].getBytes();
		int packetSize = args.length >= PACKET_SIZE_INDEX+1 ? Integer.parseInt(args[PACKET_SIZE_INDEX]) : DEFAULT_PACKET_SIZE;
		int transmissionDelay = args.length >= TRANSMISSION_DELAY_INDEX+1 ? Integer.parseInt(args[TRANSMISSION_DELAY_INDEX]) : DEFAULT_TRANSMISSION_DELAY;
		int bufferSize = args.length >= BUFFER_SIZE_INDEX+1 ? Integer.parseInt(args[BUFFER_SIZE_INDEX]) : DEFAULT_BUFFER_SIZE;
		
		byte[] fileContent = getFileContent(filePath);
		if(fileContent.length != 0) {
			try {
				udpPacketRadioTransmitter(fileContent, host, portNumber, packetSize, transmissionDelay, bufferSize);
			} catch (IOException | InterruptedException e) {
				System.out.println("[P03 UDP Error]: " + e.getMessage());
				System.exit(1);
			}
		}
	}
}