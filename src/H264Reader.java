import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


public class H264Reader {

	Map<Integer, byte[]> dataMap;
	Queue<Integer> keyQueue;
	private int firstIDRKey;
	private int firstIDRByte;
	private static final byte TYPE_BITMASK = 0b111110;
	
	public H264Reader() {
		dataMap = new HashMap<Integer, byte[]>();
		keyQueue = new LinkedList<Integer>();
	}
	
	public void addData(Integer key, byte[] data) {
		dataMap.put(key, data);
		keyQueue.add(key);
	}
	
	
	public void checkForIDR() {
		int i = 0, startKey = -1;
		byte [] data;
		byte nalUnitType;
		firstIDRKey = -1;
		firstIDRByte = -1;

		startKey = keyQueue.remove();
		
		while(firstIDRKey == -1) {
			data = dataMap.get(startKey);
			i = 0;
				
			while(i < data.length -3) {
				if(data[i]==0x00 && data[i+1]==0x00 && data[i+2]==0x01) {

					// Get NAL Unit Type
					nalUnitType = data[i+3];
					
					// Check if its a IDR (type = 5)
					if(((nalUnitType & TYPE_BITMASK) >>> 3) == 0x5) {
						if(firstIDRKey == -1) {
							firstIDRKey = startKey;
							firstIDRByte = i;
						}
					}
	
					i += 4; 
				} else {
					i++;
				}
			}
			startKey = keyQueue.remove();
		}
	}
	
	public int getFirstIDRByteNr() {
		return firstIDRByte;
	}
	
	public int getFirstIDRKeyNr() {
		return firstIDRKey;
	}
	
}
