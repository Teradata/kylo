package com.thinkbiganalytics.datalake.authorization;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;

public class ListPathTest {

	static FileSystem fileSystem;
	public static void main(String[] args) throws IOException, URISyntaxException {
	    Configuration conf = new Configuration();
	    conf.addResource(new Path("/etc/hadoop/conf/core-site.xml"));
		conf.addResource(new Path("/etc/hadoop/conf/hdfs-site.xml")); 
		
	    Path path = new Path("/model.db/customers");
	    
		fileSystem = path.getFileSystem(conf);

		listAllDir(fileSystem ,path);
		//RemoteIterator<LocatedFileStatus> fileStatusListIterator = fileSystem.listFiles(path, true);
		//while(fileStatusListIterator.hasNext())
	//	{
		
		//	LocatedFileStatus status = fileStatusListIterator.next();
	   // FileSystem fs = FileSystem.get(new URI("hdfs://localhost:9000/"), conf);
	    
		 fileSystem.close();
	    }

	   

	
	public static void listAllDir(FileSystem fileSytem ,Path path) throws FileNotFoundException, IOException
	
	{
	    FileStatus[] fileStatus = fileSystem.listStatus(path);

		for(FileStatus status : fileStatus){
	        
	    if(status.isDirectory())
	    {
	    	System.out.println("Found Folder -- > " +status.getPath().toString());
	       	listAllDir(fileSytem,status.getPath());
	    }
	    else
	    {
	    	System.out.println("Found File -- > " + status.getPath().toString());
	    
	    }
		
	}
}
}