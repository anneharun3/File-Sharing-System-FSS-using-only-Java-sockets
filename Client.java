/**
 * This class implements Client fuctions that initiates a server connection
 * @author Harun Anne(G01102695)
 */
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Client {
	protected static Socket serverSocket;	
	protected static DataInputStream dis;
	protected static DataOutputStream dos;		
	private static void ClientOperations(String[] args) throws IOException {
		switch(args[0].toLowerCase()) {
		case "upload":
			if (args.length == 3) 
				Upload(args[1],args[2]);
			else {
            	System.out.println("Please Enter Valid Command.");
            	return;
            }
			break;
		case "dir":
			if (args.length == 2)
				ListDirectory(args[1]);
			else {
				System.out.println("Enter a Valid Command.");
            	return;	
			}
			break;
		case "shutdown":
			ShutDown();
			break;
		case "mkdir":
			if (args.length == 2)
				MakeDirectory(args[1]);
			else {
				System.out.println("Enter a Valid Command.");
            	return;
			}
			break;
		case "rmdir":
			if (args.length == 2)
				RemoveDirectory(args[1]);
			else {
				System.out.println("Enter a Valid Command.");
            	return;
			}
			break;
		case "rm":
			if (args.length == 2)
			RemoveFile(args[1]);
			else {
			System.out.println("Enter a Valid Command.");
            return;
			}
			break;
		case "download":
			if (args.length == 3)
				Download(args[1], args[2]);
			else {
				System.out.println("Enter a Valid Command.");
            	return;		
			}
			break;
		default: {
            System.out.println("Enter a Valid Command.");
            try {
            	CloseAllStreams();    
            } catch (IOException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
            break;
			}
		}
	}
		
	//Method uploads file to server
	protected static void UploadFile(String file,long serverFileSize) throws IOException{
		FileInputStream fis = new FileInputStream(file);
		fis.skip(serverFileSize);		
		byte[] buffer = new byte[1024];
		while(fis.read(buffer) > 0){
			dos.write(buffer);
		}
		fis.close();
	}

	//Method is used to connect to server
	private static void connectToServer(String host, int port) {
		try {
			serverSocket = new Socket(host, port);			
		}catch(Exception e) {
			System.out.println("Cannot Connect to Server");
		}
	}
	
	//Method sets up Input and Output streams to read and write file data
	protected static void InitiateStreams() throws IOException {
		dos = new DataOutputStream(serverSocket.getOutputStream());
		dis = new DataInputStream(serverSocket.getInputStream());   
	}
	
	//Method closes all connections and flush 
	protected static void CloseAllStreams() throws IOException {
		dos.flush(); dos.close(); dis.close();
	}
	
	//Method writes the bytes to a file when downloading from server
	protected static void writeBytesToFile(long fileSize, String fileName,long buploAded) throws IOException {		
		FileOutputStream fos;
		if(buploAded == 0)
            fos = new FileOutputStream(fileName, false);		
        else
            fos = new FileOutputStream(fileName, true);           
        try {
	        byte[] buffer = new byte[1024];
	        int read = 0;
	    	long totalRead = buploAded;
	    	int remaining = (int) fileSize;
	    	//To determine the percentage remaining to download file
	        while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
	    		totalRead += read;
	    		remaining -= read;
	    		System.out.print("\rDownloading file - "+ (int)((double)(totalRead)/fileSize * 100)+"% complete");
	    		fos.write(buffer, 0, read);
	    	}            
        }catch (Exception e){
        	System.out.println("ERROR: " + e.getMessage());
        }finally {
        	fos.flush();  fos.close();
        }
	}
	
	//This Method performs client shutdown
	private static void ShutDown() throws IOException {
		try {
			InitiateStreams();
			dos.writeUTF("shutdown");
			serverSocket.close();
			System.out.println("Server/Client Closed");
		}catch(Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}finally {
			CloseAllStreams();
		}		
	}	
	
	//Method performs download from server
	private static void Download(String serverPath, String ClientPath) throws IOException{	
		String Serverpath = serverPath;		
		String DownloadPath = ClientPath;
		System.out.println("Location of File on Server: " + serverPath + " Download Location: " + DownloadPath);
		try {
			InitiateStreams();
			dos.writeUTF("download");
			dos.writeUTF(Serverpath);
			
			if(dis.readBoolean()){
				long fileSize = dis.readLong();
				long buploAded = 0;
				buploAded = new File(DownloadPath).length();
				dos.writeLong(buploAded);
				writeBytesToFile(fileSize,DownloadPath,buploAded);
			}else
				System.out.println("\nFile does not exist on server");
		}catch(Exception e){
			System.out.println("ERROR: " + e.getMessage());
		}finally{
			CloseAllStreams();
		}
	}
	
	//Method performs file upload to server
	private static void Upload(String file,String destn) throws IOException {
		String destination = destn;
		System.out.println("File Location: " + file + " Destination: " + destination);
		long contentLengthInBytes = new File(file).length();
		try {
			InitiateStreams();
			dos.writeUTF("upload");
			dos.writeUTF(destination);
			dos.writeLong(contentLengthInBytes);
			long serverFileSize = dis.readLong(); 
			UploadFile(file,serverFileSize);
			System.out.println("\nFile upload complete");
		}
        catch (FileNotFoundException e) {
            System.out.println("ERROR: " + e.getMessage());
        } 
        catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }finally {
        	CloseAllStreams();
		}		
	}

	//Method creates directory in the path specified by user
	private static void MakeDirectory(String dir) throws IOException {
		String zd = dir;
		System.out.print("Creating directory: " + zd);		
		try {
			InitiateStreams();
			dos.writeUTF("mkdir");
			dos.writeUTF(zd);
            if (!dis.readBoolean())             
                System.out.println("\n Directory could not be created on the server.");            
            else
            	System.out.println("\nDirectory created successfully");           
		}catch(Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}finally {
			CloseAllStreams();
		}		
	}

	//Method removal of directory specified by user
	private static void RemoveDirectory(String dir) throws IOException {
		String zd = dir;
		System.out.print("Remove directory: " + zd);
		try {
			InitiateStreams();
			dos.writeUTF("rmdir");
			dos.writeUTF(zd);			
			if (!dis.readBoolean())             
                System.out.println("\nDirectory could not be removed from the server");     
			else
				System.out.println("\nDirectory removal complete");
		}catch(Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}finally {
			CloseAllStreams();
		}
	}

	//Method removes of the file specified by the user
	private static void RemoveFile(String removeFile) throws IOException {
		String file = removeFile;
		System.out.print("Deleting file: " + file);
		try {
			InitiateStreams();
			dos.writeUTF("rm");
			dos.writeUTF(file);
			if (!dis.readBoolean())             
                System.out.println("\nFile could not be removed from the server");
			else
				System.out.println("\nFile removal complete");
		}catch(Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}finally {
			CloseAllStreams();
		}
	}	
	
	//Method lists all the directories and files in the path given by user
	private static void ListDirectory(String dir) throws IOException{
		String sdir = dir;
		System.out.println("Getting Server Directory: " + sdir);
		try{
			InitiateStreams();
			dos.writeUTF("dir");
			dos.writeUTF(sdir);
			if (!dis.readBoolean())             
                System.out.println("\nCould not find directory on server");
			else{
				System.out.println("\n"+dis.readUTF().trim());
				System.out.println("\nDirectory listing complete");
			}				
		}catch(Exception e){
			System.out.println("ERROR: " + e.getMessage());
		}finally{
			CloseAllStreams();
		}
	}

	public static void main(String[] args) throws IOException {	
		String hname = System.getenv("PA1_SERVER");
		if (hname != null) {
	           try {
				String[] vars = System.getenv("PA1_SERVER").split(":");		
				connectToServer(vars[0],Integer.parseInt(vars[1]));
						
			}catch(Exception e) {
				System.out.println("ERROR: Cannot connect to Server" + e.getMessage());
			}
			ClientOperations(args);
		}
		else
			System.out.println("PA1_SERVER not set.");
	}
}