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
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * download a zip file from a ftp server. Unzip it and processes its containing CSV file
 * 
 * @author Millah 
 * @since 2016
 */
public class FileProcessor {
    public static void main(String[] args) {
    	switch(args[0]){ 
        case "-output": 
        	startProgram(args[1]);
            break; 
        case "-help": 
        	showHelp();
            break; 
        case "-about": 
        	showAbout();
            break; 
        case "-unzip": 
        	unzip(args[1], args[2]); 
            break; 
        case "-download": 
        	download(args[1],Integer.parseInt(args[2]),args[3],args[4],args[5],args[6]); 
            break; 
        case "-processCSV": 
        	newCSVWithFilteredRows(args[1]); 
            break; 
        case "-createDir": 
        	createDirectoryIfNotExist(args[1]);
            break; 
        default: 
        	System.out.println("Unknown command. use -help to show command list"); 
        } 
    }

    public static void showHelp(){
    	/**
    	 * prints all available commands of the program and an explanation
    	 */
    	System.out.println(
                    "NAME: FileProcessor\n\n"
                +   "\n"
                +   "Parameters:\n"
                +   "    -output FILE:\n"
                +   "        downloads a specific zip file, unzip its CSV in /backup/, copy the\n"
    			+   "        CSV as <date>.csv. Only the lines where the ID in the first cell is\n"
    			+   "        bigger or equal than 6500000 are copied and stored.\n"
    			+   "        Paramters:\n"
    			+   "            FILE: Path to destination of download.\n\n"
    			+   "    -unzip FILE FOLDER:\n"
    			+   "        unzips the file to the folder \n"
    			+   "        Parameters: \n"
    			+   "            FILE: Location of the zip file \n"
    			+   "            FOLDER: Destination to extract the content of the zipp file to\n\n"
	    		+   "    -download SERVER PORT USER PW FOLDER FILE\n"
	    		+   "        download a FTP file.\n"
	    		+   "        Parameters:\n"
	    		+   "            SERVER: server IP or name to download from \n"
	    		+   "            PORT: port ftp server listens on\n"
	    		+   "            USER: username\n"
	    		+   "            PW: password\n"
	    		+   "            FOLDER: path where the downloaded file should be stored\n"
	    		+   "            FILE: file, which has to be downloaded\n\n"
	    		+   "    -processCSV FILE\n"
	    		+   "        copies a CSV. The new CSV is named <date>.csv and only have the rows\n"
	    		+   "        copied, which first cells are bigger or equal than 6500000.\n"
	    		+   "        Parameters:\n"
	    		+   "            FILE: location of original CSV file\n\n"
	    		+   "    -createDir FOLDER:\n"
	    		+   "        creates a directory if it does not exist, yet.\n"
	    		+   "        Parameter:\n"
	    		+   "            FOLDER: complete Path of directory\n\n"
	    		+   "    -about:\n"
	    		+   "        shows information about the programm\n\n"
	    		+   "    -help:\n"
	    		+   "        shows this help");
    }
    
    public static void showAbout() {
    	/**
    	 * prints about information
    	 */
    	System.out.println("© 2016, Millah, https://github.com/MillahMau/Assignment-FTP-Downloader.git");
    }
    
    public static void startProgram(String path){
    	/**
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
    	 * @param path destination path to download
    	 */
    	
    	//the given parameters for the connection
    	String server = "beel.org";
        int port = 21;
        String user = "f00b72a7";
        String pass = "WtdyYSkrvzT5hErd";
        
    	String file = "FTP File Downloader and Processor example file.zip";
    	String unpackedFile = "FTP File Downloader and Processor example file.csv";
    	String backupPath = combinePaths(path, "backup"); 

    	createDirectoryIfNotExist(backupPath);
    	
    	download(server, port, user, pass, backupPath, file);
    	unzip(combinePaths(backupPath,file), backupPath);
    	newCSVWithFilteredRows(combinePaths(backupPath,unpackedFile));
    }
    
    public static int printProgBar(int percent, int alreadyPrinted, int max){
    	/**
    	 * prints # as a progress bar. if finished, print 100%.
    	 * 
    	 * @param percent the current percentage the progress file should display
    	 * @param alreadyPrinted counter, how much # are printed. has to be maintained
    	 * @param max defines the maximal characters of the progress bar
    	 * @return the updated alreadyPrinted counter
    	 */
    	
    	//prevents to many #
    	if(percent>= 100){
    		percent = 100;
    	}
    	
    	//compare already printed percent with wanted percent. Prints #, until wanted percent is reached
    	while (percent > (alreadyPrinted/max)*100){
        	System.out.print("#");
        	alreadyPrinted++;
    	}
    	
    	//indicates complete download
    	if(percent>= 100){
    		System.out.println(" 100%");
    	}
		
    	return alreadyPrinted;
    }
    
    public static void newCSVWithFilteredRows(String path) {
    	/**
    	 * all rows, which cells in the first coloumn are lower than 6500000, are not copied to the new file
    	 * store the new file as <date>.csv in the same folder
    	 * 
    	 * @param path path to original csv
    	 * 
    	 * @exception FileNotFoundException
    	 * @exception NumberFormatException
    	 * @exception IOException
    	 */
    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	Date date = new Date();
    	
    	BufferedReader reader;
    	String line = null;
    	
    	//creates the new file with the name <date>.csv at the same location of the old file
    	String newpath = "";
    	if (path.contains(File.separator))
    		newpath = path.substring(0, path.lastIndexOf(File.separator)) + File.separator;
    	newpath += dateFormat.format(date) + ".csv";
    	
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
    		System.out.println("File not Found! Check file path");
    	} catch (NumberFormatException e) {
    		e.printStackTrace();
    		System.out.println("Formatting Error in CSV. Check the content of the cells in the first coloumn");
    	} catch (IOException e) {
    		e.printStackTrace();
    		System.out.println("Couldn't open/read/write file. Check permissions or if the file is already in use.");
    	}
    }

    public static void createDirectoryIfNotExist(String path){
    	/**
    	 * creates a directory-folder, if not present.
    	 * 
    	 * @param path Path including to creating directory
    	 * @exception SecurityException
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
    	    	System.out.println("Couldn't create the file. Check permissions.");
    	    	se.printStackTrace();
    	    }        
    	}
    }
    
    public static String combinePaths (String path1, String path2) {
    	/**
    	 * combines to paths to one
    	 * 
    	 * @param path1 first part of path
    	 * @param path2 second part of path
    	 */
        return Paths.get(path1, path2).toString();
    }
    
    public static void unzip(String source, String dest) {
    	/**
    	 * unzips a file to a folder
    	 * 
    	 * @param source path to the zip file
    	 * @param dest path where the files in the zip should be put
    	 * @exception ZipException
    	 */
    	System.out.println("unzipping file: " + source);
        try {
             ZipFile zipFile = new ZipFile(source);
             zipFile.extractAll(dest);
             System.out.println("unzipped files to: " + dest);
        } catch (ZipException e) {
        	System.out.println("can't unzip file");
            e.printStackTrace();
        }
    }
    
    public static void download(String server, int port, String user, String pass, String path, String file) {
    	/**
    	 * downloads a specific file from a ftp server
    	 * 
    	 * @param server the ftp server, where the files to download are
    	 * @param user user name to log in
    	 * @param pass password to log in
    	 * @param path the destination for the downloaded file
    	 * @param file the name of the downloaded file
    	 * 
    	 * @exception IOException
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
            
            //a variable for the progress bar printing
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
            
            //download file via streams
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
            InputStream inputStream = ftpClient.retrieveFileStream(file);
            byte[] bytesArray = new byte[packageSize];
            int bytesRead = -1;
            
            //download package wise
            while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                outputStream.write(bytesArray, 0, bytesRead);
                //calculations for progress bar
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
