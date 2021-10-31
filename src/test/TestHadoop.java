package test;

import java.io.IOException;

import settings.SystemSettings;
import utils.io.HDFS;

/**
 * Tests the connection and facilities of the Hadoop FS
 * @author Marc Frincu
 *
 */
public class TestHadoop {
	public static void main(String args[]) throws IOException {
		SystemSettings settings = SystemSettings.getSystemSettings();
		settings.loadProperties("settings/system.properties");
		
		HDFS hdfs = new HDFS();
		
		//hdfs.makeDirectory("subdir");
		System.out.println(hdfs.copyToHDFS("silk/rulesDMECT.silk", "a.silk", true));
		hdfs.copyFromHDFS("a.silk", "retrieve.hadoop");
	}
}
