import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * 
 * @author Sophie
 *
 */
public class FileProcessor {
    public static void main(String[] args) {
    	if (args[0].equals("-output"))
    		startProgram(args[1]);
    	else if (args[0].equals("-help"))
    		showHelp();
    	else if (args[0].equals("-about"))
    		showAbout();
    	else
    		System.out.println("Unknown command. use -help to show command list");
    	}
    
    public static void showHelp(){
    	/*
    	 * prints all available commands of the program and an explanation
    	 */
    	System.out.println("-output: downloads a specific zip-file, unzip its CSV in /backup/ process the CSV, so all lines with ID over 6500000 are deleted and store it as <date>.csv.\n Paramter: Path to destination of download.");
    	System.out.println("-about: shows information about the programm");
    	System.out.println("-help: shows all available parameters");
    }
    
    public static void showAbout() {
    	/*
    	 * prints about information
    	 */
    	System.out.println("© 2016, Sophie Siebert, https://github.com/MillahMau/Assignment-FTP-Downloader.git");
    }
    
    public static void startProgram(String path){
    	/*
    	 * starts the main Program:
    	 * creates a backup-folder, if not present.
    	 * downloads a file with
    	 * Server: beel.org
    	 * port: 21
    	 * pass: WtdyYSkrvzT5hErd
    	 * file: FTP File Downloader and Processor example file.zip
    	 * 
    	 * to the given destination in the backup folder. 
    	 * unzip the file.  
    	 * the contained CSV-file is processed:
    	 * a new file is created, where all rows, which cells in the first coloumn are lower than 6500000 are not copied.
    	 * store the new file as <date>.csv
    	 * 
    	 * 
    	 * Parameter: destination path to download
    	 */
    	
    	//the given parameters for the connection
    	String server = "beel.org";
        int port = 21;
        String user = "f00b72a7";
        String pass = "WtdyYSkrvzT5hErd";
        
    	String file = "FTP File Downloader and Processor example file.zip";
    	String backupPath = combinePaths(path, "backup"); 

    	createDirectoryIfNotExist(backupPath);
    	
    	download(server, port, user, pass, backupPath, file);
    	unzip(combinePaths(backupPath,file), backupPath);
    	newcCSVWithFilteredRows("C:/Users/Sophie/workspace/FTP Downloader and Processor/temp/backup/FTP File Downloader and Processor example file.csv");
    }
    
    public static void newCSVWithFilteredRows(String path) {
    	/*
    	 * all rows which cell in the first coloumn are lower than 6500000, are not copied to the new file
    	 * store the new file as <date>.csv in the same folder
    	 * 
    	 * Parameter: path to original csv
    	 * 
    	 */
    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	Date date = new Date();
    	
    	BufferedReader reader;
    	String line = null;
    	//creates the new file with the name <date>.csv at the same location of the old file
    	String newpath = path.substring(0, path.lastIndexOf('/')) +"/" +dateFormat.format(date) + ".csv";
    	FileWriter writer = null;
    	System.out.println("create new CSV: "+ newpath);
    	
    	try {
    		writer = new FileWriter(newpath, true);
    		reader = new BufferedReader(new FileReader(path));
 
    		//copies the rows to the new file, if they meet certain requirements
    		while ((line = reader.readLine()) != null) {
    			//get only the first cell
    			String firstCell = line.split(";")[0];
    			
    			//if cell contains no pure number, copy complete row (for eg headers)
        		if (!firstCell.matches("^[0-9]$")){
        			writer.append(reader.readLine());
        			writer.flush();
        		}
        		//if cell contains a number, it must be bigger or equal than 6500000 to be copied
        		else if (Integer.parseInt(firstCell) >= 6500000){
    				writer.append(line + "\n");
    				writer.flush();
    			}
        		
        		System.out.println("created new CSV: " + newpath);
    		} 
    	}catch (FileNotFoundException e) {
    		System.out.println("File not Found!");
    	} catch (NumberFormatException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }

    public static void createDirectoryIfNotExist(String path){
    	/*
    	 * creates a directory-folder, if not present.
    	 * 
    	 * Parameter: Path including to creating directory
    	 */
    	File dir = new File(path);
    	String name = dir.getName();
    	
    	//create directory, if it not exists
    	if (!dir.exists()) {
    	    System.out.println("creating directory: " + name);

    	    try{
    	        dir.mkdir();
    	        System.out.println("created directory: " + name);
    	    } 
    	    catch(SecurityException se){
    	    	System.out.println("failed to create directory: " + name);
    	    	se.printStackTrace();
    	    }        
    	}
    }
    
    public static String combinePaths (String path1, String path2) {
    	/*
    	 * combines to pathes to one
    	 * 
    	 * Parameters: Two Pathes to combine
    	 */
        return new File(path1, path2).getPath();
    }
    
    public static void unzip(String source, String dest) {
    	/*
    	 * unzips a file
    	 * 
    	 * Parameters: 
    	 * source: path to the zip file
    	 * dest: path where the files in the zip should be put
    	 */
    	System.out.println("unzipping file: " + source);
        try {
             ZipFile zipFile = new ZipFile(source);
             zipFile.extractAll(dest);
             System.out.println("unzipped files to: " + dest);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }
    
    public static void download(String server, int port, String user, String pass, String path, String file) {
    	/*
    	 * downloads a file from a ftp server
    	 * 
    	 * Parameters:
    	 * server: the ftp server, where the files to download are
    	 * user: user name to log in
    	 * pass: password to log in
    	 * path: the destination of the downloaded file
    	 * file: the local name of the downloaded file
    	 */
    	System.out.println("downloading file...");
        FTPClient ftpClient = new FTPClient();
        File downloadFile = new File(path,file);
        try {
 
        	//conncet and login to FTP-Server
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            
            //download file via buffer
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
            InputStream inputStream = ftpClient.retrieveFileStream(file);
            byte[] bytesArray = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                outputStream.write(bytesArray, 0, bytesRead);
            }
 
            boolean success = ftpClient.completePendingCommand();
            if (success) {
                System.out.println("downloaded: "+ downloadFile.getName());
            }
            outputStream.close();
            inputStream.close();
 
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
            	//close connection
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}