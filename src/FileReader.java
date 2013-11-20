import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class FileReader {

	private static PrintWriter resultFile;
	private static final String FILE_TEXT_EXT = ".ts";
	
	public FileReader() {
		
	}
	
    public static void main(String[] args) {
    	try {
        	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        	String pathString = null, fileResultString = null;
        	GenericExtFilter filter = new GenericExtFilter(FILE_TEXT_EXT);
        	
			// Get path from user
        	System.out.print("Enter path to diretory: ");
			pathString = reader.readLine();
			File dir = new File(pathString);
			
			while(dir.isDirectory() == false) {
				System.out.print("Not a directory!");
	        	System.out.print("Enter path to diretory: ");
				pathString = reader.readLine();
				dir = new File(pathString);
			}
			
			File[] fileList = dir.listFiles(filter);
    	
			resultFile = new PrintWriter("result.txt");
			resultFile.println("File name\tFirst PAT byte\tVideo PID\tFirst IDR key\tFirst IDR byte");
			
			for(File f : fileList) {
				boolean firstPATSwhown = false;
				fileResultString = f.getName();
				
	    		// Read file in to a byte array
				byte[] data = Files.readAllBytes(f.toPath());
				
				int i = 0, pid = 0;
				byte[] tspackage = new byte[188];
				Map<Integer, ArrayList<MPEG2TSPacket>> packageMap = new HashMap<Integer, ArrayList<MPEG2TSPacket>>();
				Map<Integer, ArrayList<Integer>> indexMap = new HashMap<Integer, ArrayList<Integer>>();
				H264Reader h264Reader = new H264Reader();
				
				while(i < data.length) {
					if(data[i] == 0x47) {
						System.arraycopy(data, i, tspackage, 0, 188);
						MPEG2TSPacket tempPackage = new MPEG2TSPacket(tspackage);
						pid = tempPackage.getPID();
						
						if(!firstPATSwhown && pid == 0) {
							fileResultString += "\t" + i;
							firstPATSwhown = true;
						}
						
						if(packageMap.get(pid) == null) {
							packageMap.put(pid, new ArrayList<MPEG2TSPacket>());
							indexMap.put(pid, new ArrayList<Integer>());
						} 
						
						packageMap.get(pid).add(tempPackage);
						indexMap.get(pid).add(i);
						
						i += 188;
					} else {
						i++;
					}
				}

				int videoPID = 0, temp = 0;
				Set<Integer> keys = packageMap.keySet();
				
				for(Integer key : keys) {
					if(temp <= packageMap.get(key).size()) {
						temp = packageMap.get(key).size();
						videoPID = key;
					}
				}
				
				fileResultString += "\t" + videoPID;
				
				if(videoPID == 0) {
					System.out.println("WARNING: Most PAT packages in file");
				}
				
				int continuityCounter, lastIndex = -1;
				boolean error = false;
				
				// Loop thru all packages in "videoPID" PID
				for(int j = 0; j < packageMap.get(videoPID).size(); j++) {
					// Check if package has payload data
					if(packageMap.get(videoPID).get(j).hasPayloadData()) {
						// Get continuityCounter to keep track of the order of the payload
						continuityCounter = packageMap.get(videoPID).get(j).getContinuityCounter();
						// If continuityCounter have increased with 1 seen last package
						if(continuityCounter == lastIndex +1 || lastIndex == -1) {
							lastIndex = continuityCounter;
							h264Reader.addData(indexMap.get(videoPID).get(j), packageMap.get(videoPID).get(j).getPayloadData());
						} else { 
							System.out.println("PID: " + Integer.toHexString(videoPID) + ". Package: " + j + ". Out of order.");
							error = true;
							break;
						}
						
						if(lastIndex == 15) {
							lastIndex = -1;
						}
					}
				}
				
				if(!error) {
					h264Reader.checkForIDR();
					
					fileResultString += "\t" + h264Reader.getFirstIDRKeyNr();
					fileResultString += "\t" + h264Reader.getFirstIDRByteNr();
				}
				resultFile.println(fileResultString);
				System.out.println("File " + f.getName() + " done.");
			}
			System.out.println("All files done!");
	    	
	    	
    	} catch (IOException e) {
			System.out.println(e.toString());
		}
    	resultFile.close();
    }
    
    public static byte[] byteArrayAppend(byte[] array1, byte[] array2) {
    	byte[] newArray = new byte[array1.length + array2.length];
    	System.arraycopy(array1, 0, newArray, 0, array1.length);
    	System.arraycopy(array2, 0, newArray, array1.length, array2.length);
    	return newArray;
    }
    
}

