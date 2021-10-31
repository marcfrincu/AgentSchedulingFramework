package utils.io;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import settings.SystemSettings;

/**
 * Class responsible with IO operations between the Hadoop Distributed File System
 * and the local machine
 * @author Marc Frincu
 * @since 2010 (last modification Feb 4th 2013: tested HDFS, added logging, updated core to 1.0.4)
 */
public class HDFS {

	private static Logger logger = Logger.getLogger(HDFS.class
			.getPackage().getName());

    Configuration config = null;
    FileSystem hadoopFileSystem = null;
    Path hadoopDirectory = null;
    
    /**
     * Default constructor
     */
    public HDFS() {
    	this.config = new Configuration();    	

    	//this.config.addResource(new Path("/usr/local/hadoop/conf/core-site.xml"));
    	//this.config.addResource(new Path("/usr/local/hadoop/conf/hdfs-site.xml"));
    	
        config.set("fs.default.name", SystemSettings.getSystemSettings().getHadoop_host() + ":"
        		+ SystemSettings.getSystemSettings().getHadoop_port());
       
        config.set("hadoop.job.ugi", 
        			SystemSettings.getSystemSettings().getHadoop_user_credentials());
        
        try {
			this.hadoopFileSystem = FileSystem.get(this.config);
		} catch (IOException e) {
			HDFS.logger.fatal("Could not bind to Hadoop FS. HDFS message: " + e.getMessage());
			System.exit(0);
		}

		this.hadoopDirectory = new Path(this.hadoopFileSystem.getWorkingDirectory() + "/" 
				+ SystemSettings.getSystemSettings().getHadoop_dir());
        
    }
    
    /**
     * Creates a directory on the Hadoop FS inside the default working directory
     * @param dirName the directory name
     * @throws IOException
     */
    public void makeDirectory(String dirName) throws IOException {
    	
    	this.hadoopFileSystem.mkdirs(new Path(this.hadoopFileSystem.getWorkingDirectory() + "/" 
    			+ dirName));
    }

    /**
     * Copies a file to the specified directory inside the default working directory
     * @param src the source file including relative path to the default home directory
     * @param dst the destination directory. If the directory does not exist it needs
     * to be created with the <i>makeDirectory</i> method
     * @param override true if the file should replace an existing one with the same name
     * @return true if the file has been copied, false otherwise
     * @throws IOException
     */
    public boolean copyToHDFS(String src, String dst, boolean override) throws IOException {
    	Path source = new Path(src);
    	
    	if (this.hadoopFileSystem.exists(new Path(this.hadoopFileSystem.getWorkingDirectory() 
    											+ "/" + 
    											dst)) && !override) {
    		HDFS.logger.info("File already exists on HDFS. Skipping.");
    		return false;
    	}
    	
    	final String from = System.getProperty("user.home") + 
								System.getProperty("file.separator") +
								SystemSettings.getSystemSettings().getLocal_dir() + 
								System.getProperty("file.separator") + 
								source;
    	final String to = this.hadoopFileSystem.getWorkingDirectory() + 
			"/" + 
				dst;
    	this.hadoopFileSystem.copyFromLocalFile(new Path(from), 
    			 								new Path(to));
    	
    	HDFS.logger.info("Copied local file: " + from + " to HDFS: " + to); 
    	return true;
    	 
    }
    
    /**
     * Copies a file from the default Hadoop FS working directory to the
     * specified local location  
     * @param src the relative path of the remote file, including sub-directories
     * @param dst the destination path including filename. It should be relative to the
     * default home directory specified in the <i>system.properties</i> file
     * @return true if the file has been successfully copied, false otherwise
     * @throws IOException
     */
    public boolean copyFromHDFS(String src, String dst) throws IOException {
    	final String from = this.hadoopFileSystem.getWorkingDirectory() + "/" + src;
    	Path source = new Path(from); 

    	if (!this.hadoopFileSystem.exists(source)) {
    		HDFS.logger.error("HDFS file: " + this.hadoopFileSystem.getWorkingDirectory() + "/" + src + " does not exist.");
    		return false;
    	}
    	    	
    	FSDataInputStream in = this.hadoopFileSystem.open(source);

		final String to = System.getProperty("user.home") + 
							System.getProperty("file.separator") +
							SystemSettings.getSystemSettings().getLocal_dir() + 
							System.getProperty("file.separator") + 
							dst;
    	FileOutputStream out = new FileOutputStream(to);

    	while (in.available() > 0) {
    		out.write(in.readByte());
    	}

    	out.close();
    	in.close();

    	HDFS.logger.info("Copied HDFS file: " + from + " to local: " + to);
    	return true;
    }    
}

