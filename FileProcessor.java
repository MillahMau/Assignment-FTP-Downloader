import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
    	String server = "beel.org";
        int port = 21;
        String user = "f00b72a7";
        String pass = "WtdyYSkrvzT5hErd";
    	String path = args[0];
    	
    	String backuppath = combine(path, "backup"); 
    	createBackupIfNecessary(backuppath);
    	
    	download(server, port, user, pass, backuppath);
    	unzip("C:\\Users\\Sophie\\workspace\\FTP Downloader and Processor\\temp\\stuff.zip", "C:\\Users\\Sophie\\workspace\\FTP Downloader and Processor\\temp");
    }

    public static void createBackupIfNecessary(String path){
    	File dir = new File(path);
    	String name = dir.getName();
    	System.out.println(name);
    	
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
    	System.out.println("unzipping file:" + source);
        try {
             ZipFile zipFile = new ZipFile(source);
             zipFile.extractAll(dest);
             System.out.println("unzipped file to:" + dest);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }
    
    public static void download(String server, int port, String user, String pass, String path) {
    	System.out.println("downloading file...");
        FTPClient ftpClient = new FTPClient();
        try {
 
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
 
            
            String remoteFile = "FTP File Downloader and Processor example file.zip";
            File downloadFile = new File(path + "/FTP File Downloader");
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
            InputStream inputStream = ftpClient.retrieveFileStream(remoteFile);
            byte[] bytesArray = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                outputStream.write(bytesArray, 0, bytesRead);
            }
 
            boolean success = ftpClient.completePendingCommand();
            if (success) {
                System.out.println("File has been downloaded successfully.");
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
    }
}
