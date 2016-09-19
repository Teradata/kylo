package com.thinkbiganalytics.datalake.authorization;

import static org.apache.hadoop.fs.permission.AclEntryScope.ACCESS;
import static org.apache.hadoop.fs.permission.AclEntryType.USER;
import static org.apache.hadoop.fs.permission.FsAction.READ_WRITE;



import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.AclEntry;
import org.apache.hadoop.fs.permission.AclEntryScope;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.hdfs.DistributedFileSystem;

import com.google.common.collect.Lists;

public class SentryHDFSPolicy {

	//Cluster cluster;
	static FileSystem fileSystem;
	public static void main (String args[]) throws Exception
	{
		//Cluster cluster = new lus
		
		
		Configuration conf = new Configuration();
		conf.addResource(new Path("/etc/hadoop/conf/core-site.xml"));
		conf.addResource(new Path("/etc/hadoop/conf/hdfs-site.xml")); 
		Path path = new Path("/user/test1");
		System.out.println("--------------Step 0.5--------------------");
		fileSystem = path.getFileSystem(conf);
		
				
		System.out.println("--------------Step 1--------------------");
		 Path dirPath = new Path("/user/");
		  DistributedFileSystem fs;// = fs1;
		 System.out.println("fs content ---- " + fileSystem.toString());
		 
		  //DistributedFileSystem fs = cluster.getFileSystem();
		  System.out.println("--------------------Step 2-------------------");
		
		 // List<AclEntry> aclSpec = Lists.newArrayList(aclEntry(ACCESS, USER, "foo", READ_WRITE));
		  //fs.setAcl(dirPath, aclSpec);
		  
		  System.out.println("Step 3");
		  AclEntry e = new AclEntry.Builder().setName("shashi") 
			        .setPermission(FsAction.ALL).setScope(AclEntryScope.ACCESS).setType(USER).build();
		  System.out.println("Step 4");
		// fs.modifyAclEntries(dirPath, Lists.newArrayList(e)); 
		 
		 // fileSystem.
		  
		  fileSystem.modifyAclEntries(dirPath, Lists.newArrayList(e));
		  //fs.create(filePath).close();
		  //fs.mkdirs(subdirPath);

		  System.out.println("--------------------Step 5-------------------");
		  
		  fileSystem.close();
		  
		  
		  System.out.println("--------------------Step 6-------------------");
	}
	
}
