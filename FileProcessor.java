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
    	System.out.println("-output: downloads a specific zip-file, unzip its CSV in \\backup\\ process the CSV, so all lines with ID over 6500000 are deleted and store it as <date>.csv.\n Paramter: Path to destination of download.");
    	System.out.println("-about: shows information about the programm");
    	System.out.println("-help: shows all available parameters");
    	
    }
    
    public static void showAbout() {
    	System.out.println("© 2016, Sophie Siebert, https://github.com/MillahMau/Assignment-FTP-Downloader.git");
    }
    
    public static void startProgram(String path){
    	String server = "beel.org";
        int port = 21;
        String user = "f00b72a7";
        String pass = "WtdyYSkrvzT5hErd";
    	String file = "FTP File Downloader and Processor example file.zip";
    	
    	String backuppath = combine(path, "backup"); 
    	//createBackupIfNecessary(backuppath);
    	
    	//String downloaded = download(server, port, user, pass, backuppath, file);
    	//unzip(combine(backuppath,downloaded), backuppath);
    	deleteRowsInCSV("C:\\Users\\Sophie\\workspace\\FTP Downloader and Processor\\temp\\backup\\FTP File Downloader and Processor example file.csv");
    }
    
    public static void deleteRowsInCSV(String path) {
    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	Date date = new Date();
    	
    	BufferedReader reader;
    	String line = null;
    	String newpath = path.substring(0, path.lastIndexOf('\\')) +"\\" +dateFormat.format(date) + ".csv";
    	FileWriter writer = null;
    	
    	try {
			writer = new FileWriter(newpath, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
		try {
			reader = new BufferedReader(new FileReader(path));
			writer.append(reader.readLine());
			writer.flush();
			
			while ((line = reader.readLine()) != null) {
	    		String firstCell = line.split(";")[0];
	    		if (Integer.parseInt(firstCell)<= 6500000){
	    			writer.append(line + "\n");
	    			writer.flush();
	    		}
	    	}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public static void createBackupIfNecessary(String path){
    	File dir = new File(path);
    	String name = dir.getName();
    	
    	if (!dir.exists()) {
    	    System.out.println("creating directory: " + name);
    	    boolean result = false;

    	    try{
    	        dir.mkdir();
    	        result = true;
    	    } 
    	    catch(SecurityException se){
    	        se.printStackTrace();
    	    }        
    	    if(result) {    
    	        System.out.println("created directory: " + name);  
    	    }
    	}
    }
    
    public static String combine (String path1, String path2)
    {
        File file1 = new File(path1);
        File file2 = new File(file1, path2);
        return file2.getPath();
    }
    
    public static void unzip(String source, String dest) {
    	System.out.println("unzipping file: " + source);
        try {
             ZipFile zipFile = new ZipFile(source);
             zipFile.extractAll(dest);
             System.out.println("unzipped file to: " + dest);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }
    
    public static String download(String server, int port, String user, String pass, String path, String file) {
    	System.out.println("downloading file...");
        FTPClient ftpClient = new FTPClient();
        File downloadFile = new File(path,file);
        try {
 
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
 
            
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
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return downloadFile.getName();
    }
}