/**
 * This class implements Client fuctions that initiates a server connection
 * @author Harun Anne(G01102695)
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;

public class Server {
	protected static DataInputStream dis;
	protected static DataOutputStream dos;
	protected static Socket s;
	private static ServerSocket ssoc;
	private static boolean isRun = true;	
	
	//Method selects operation that server needs to perform
	private static void ServerCommands(String args) throws IOException {
		switch(args.toLowerCase()) {
		case "rmdir":
			RemoveDirectory();
			break;
		case "download":
			Download();
			break;
		case "dir":
			ListDirectory();
			break;
		case "rm":
			RemoveFile();
			break;
		case "shutdown":
			ShutDown();
			break;
		case "upload":
			Upload();
			break;
		case "mkdir":
			MakeDirectory();
			break;
		default: System.out.println("Enter Valid Command");
		}
	}

	//Method is used to write bytes to a file 
	protected static void writeBytesToFile(long fileSize, String fileName,long buploAded) throws IOException {		
		FileOutputStream fos;
		if(buploAded == 0)
            fos = new FileOutputStream(fileName, false);		
        else
            fos = new FileOutputStream(fileName, true);          
        try {
        	int rx = (int) fileSize;
        	int read = 0;
	    	long tn = buploAded;
	        byte[] cz = new byte[1024];
	        while((read = dis.read(cz, 0, Math.min(cz.length, rx))) > 0) {
	    		tn += read;rx -= read;
	    		System.out.println("\r Uploading File - "+ (int)((double)(tn)/fileSize * 100)+"% complete");
	    		fos.write(cz,0,read);
	    	}            
        }catch (Exception e) {
        	System.out.println("ERROR: " + e.getMessage());
        }finally {
        	fos.flush();  fos.close();
        }
	}
	
	//Method allows server connections
	private Server(int port) {
		try {
			ssoc = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("ERROR: "+ e.getMessage());
		}
	}

	//Method is used to set up Input and Output streams 
	protected static void InitStreams() throws IOException {
		dos = new DataOutputStream(s.getOutputStream());
		dis = new DataInputStream(s.getInputStream());   
	}
	
	//This method is used to close all connections and flush 
	protected static void CloseStreams() throws IOException {
		dos.flush(); dos.close(); dis.close();
	}
	
	//This method is used to check server run to accept connections from multiple clients
	private void run() throws IOException {		
		while (isRun) {	
			try {
				s = ssoc.accept();
				InitStreams();
				String argument = dis.readUTF();							
				ServerCommands(argument);		
			} catch (IOException e) {
				System.out.println("ERROR: " + e.getMessage());
			}finally {
				CloseStreams();
			}
		}
	}
	
	//This method uploads file to server
	private static void Upload() throws IOException {		
		String fx = dis.readUTF().trim();			
		long fileSize = dis.readLong();		
		 try {
			 long buploAded = 0;
			 buploAded = new File(fx).length();
			 dos.writeLong(buploAded); 
	         writeBytesToFile(fileSize, fx, buploAded);	
	    }catch (Exception e){
	         System.out.println("ERROR: " + e.getMessage());
	    }	
	}
	
	//Method makes directories to server
	private static void MakeDirectory() throws IOException {
		String path = dis.readUTF().trim();
		dos.writeBoolean(new File(path).mkdir());
	}
	
	//Method removes directories from server
	private static void RemoveDirectory() throws IOException {
		String f = dis.readUTF().trim();
        if (!(new File(f).exists()) || !(new File(f).isDirectory())) {        
        	dos.writeBoolean(false);
        }           
        else {
        	dos.writeBoolean(new File(f).delete());
        	}       
	}
	
	//Method removes file from server
	private static void RemoveFile() throws IOException {
		String f = dis.readUTF().trim();
		if (!(new File(f).exists()) || (new File(f).isDirectory())) 
			dos.writeBoolean(false);
		else
			dos.writeBoolean(new File(f).delete());	
	}
	
	//Method lists directories from server
	private static void ListDirectory() throws IOException{
		String d = dis.readUTF().trim();
		if (!(new File(d).exists()) || !(new File(d).isDirectory())){
			dos.writeBoolean(false);
		}else{
			dos.writeBoolean(true);
			File folder = new File(d); 
			File[] lff = folder.listFiles();			
			dos.writeUTF(Arrays.toString(lff));
		}
	}
	
	//Method shutdowns server 
	private static void ShutDown() throws IOException{
		CloseStreams();
		ssoc.close();
		isRun = false;
	}
	
	//Method uploads file to server
	protected static void UploadFile(String f,long serverFileSize) throws IOException{
		FileInputStream fis = new FileInputStream(f);
		fis.skip(serverFileSize);		
		byte[] cz = new byte[1024];
		while(fis.read(cz) > 0)
			dos.write(cz);
		fis.close();
	}
	
	//Method downloads file from server
	private static void Download() throws IOException{
		try {
			String fa = dis.readUTF().trim();		
			if(!(new File(fa).exists())){
				dos.writeBoolean(false);
			}else {
				dos.writeBoolean(true);
				long contentLengthInBytes = new File(fa).length();
				dos.writeLong(contentLengthInBytes);
				long clientFileSize = dis.readLong(); 
				System.out.println(clientFileSize);
				UploadFile(fa,clientFileSize);	
				System.out.println("\nFile download Complete");
			}
		}catch(Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}		
	}	

	public static void main(String[] args) throws IOException {		
		if(args.length !=2)
			System.out.println("ERROR: Enter 2 arguements");
		if(args[0].equalsIgnoreCase("start")) {
			Server f = new Server(Integer.parseInt(args[1]));
			f.run();
		}else
			System.out.println("Enter a Valid Command");
	}
}