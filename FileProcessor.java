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
import org.apache.commons.net.ftp.FTPFile;

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
    	else if (args[0].equals("-unzip"))
    		unzip(args[1], args[2]);
    	else if (args[0].equals("-download"))
    		download(args[1],Integer.parseInt(args[2]),args[3],args[4],args[5],args[6]);
    	else if (args[0].equals("-processCSV"))
    		newCSVWithFilteredRows(args[1]);
    	else if (args[0].equals("-createDir"))
    		createDirectoryIfNotExist(args[1]);
    	
    	else
    		System.out.println("Unknown command. use -help to show command list");
    	}
    
    public static void showHelp(){
    	/*
    	 * prints all available commands of the program and an explanation
    	 */
    	System.out.println("-output: downloads a specific zip-file, unzip its CSV in /backup/ process the CSV, so all lines with ID over 6500000 are deleted and store it as <date>.csv.\n Paramter: Path to destination of download.");
    	System.out.println();
    	System.out.println("-unzip: unzips a file. Parameters: String location of zip-File; String destination of content of zip-file");
    	System.out.println();
    	System.out.println("-download: download a FTP file. Parameters: String server; Int port; String user; String password; String path to store; String file to download");
    	System.out.println();
    	System.out.println("-processCSV: creates a new CSV, with missing rows where first cell is lower than 650000. Parameters: String location of CSV file");
    	System.out.println();
    	System.out.println("-createDir: creates a directory if not exists. Parameter: String complete Path of directory");
    	System.out.println();
    	System.out.println("-about: shows information about the programm");
    	System.out.println();
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
    	 * @param path destination path to download
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
    	newCSVWithFilteredRows(combinePaths(backupPath,file));
    }
    
    public static int printProgBar(int percent, int alreadyPrinted, int max){
    	/*
    	 * prints # as a progress bar. if finished, print 100%.
    	 * 
    	 * @param percent the current percentage the progress file should display
    	 * @param alreadyPrinted counter, how much # are printed. has to be maintained
    	 * @param max defines the maximal characters of the progress bar
    	 * @return the updated alreadyPrinted counter
    	 */
    	
    	if(percent>= 100){
    		percent = 100;
    	}
    	
    	while (percent > (alreadyPrinted/max)*100){
        	System.out.print("#");
        	alreadyPrinted++;
    	}
    	
    	if(percent>= 100){
    		System.out.println(" 100%");
    	}
		

    	return alreadyPrinted;
    }
    
    public static void newCSVWithFilteredRows(String path) {
    	/*
    	 * all rows which cell in the first coloumn are lower than 6500000, are not copied to the new file
    	 * store the new file as <date>.csv in the same folder
    	 * 
    	 * @param path path to original csv
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
        		if (!firstCell.matches("^[0-9]*$")){
        			writer.append(reader.readLine());
        			writer.flush();
        		}
        		//if cell contains a number, it must be bigger or equal than 6500000 to be copied
        		else if (Integer.parseInt(firstCell) >= 6500000){
    				writer.append(line + "\n");
    				writer.flush();
    			}
    		} 
    		System.out.println("created new CSV: " + newpath);
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
    	 * @param path Path including to creating directory
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
    	 * @param path1 first part of path
    	 * @param path2 second part of path
    	 */
        return new File(path1, path2).getPath();
    }
    
    public static void unzip(String source, String dest) {
    	/*
    	 * unzips a file
    	 * 
    	 * @param source path to the zip file
    	 * @param dest path where the files in the zip should be put
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
    	 * downloads a specific file from a ftp server
    	 * 
    	 * @param server the ftp server, where the files to download are
    	 * @param user user name to log in
    	 * @param pass password to log in
    	 * @param path the destination for the downloaded file
    	 * @param file the name of the downloaded file
    	 */
    	System.out.println("downloading file...");
        FTPClient ftpClient = new FTPClient();
        File downloadFile = new File(path,file);
        try {
 
        	//connect and login to FTP-Server
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            
            //get the file size of the file to download
            FTPFile serverFile = ftpClient.mlistFile(file);
            long serverFileSize = serverFile.getSize();
            long localFileSize = 0;
            //a variable for the progressbar printing
            int alreadyPrinted = 0;
            
            //choose the maximal characters of the progress bar
            int maxProgressBarCharacters = 30;
            
            //choose a package size which enables reasonable printing in the progressbar (one can argue about this, because little packages need longer. But in this case we want a fancy progress bar :))
            int packageSize = (int)serverFileSize/maxProgressBarCharacters;
            
            //but not too big or too small
            if (packageSize > 4096) {
            	packageSize = 4096;
            } else if (packageSize < 512){
            	packageSize = 512;
            }
            
            //download file via buffer
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
            InputStream inputStream = ftpClient.retrieveFileStream(file);
            byte[] bytesArray = new byte[packageSize];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                outputStream.write(bytesArray, 0, bytesRead);
                localFileSize += packageSize;
                alreadyPrinted = printProgBar((int)(((localFileSize/(double)serverFileSize))*100), alreadyPrinted, maxProgressBarCharacters);
            }
 
            System.out.println("\ndownloaded: "+ downloadFile.getName());
            
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