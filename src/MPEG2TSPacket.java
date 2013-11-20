
public class MPEG2TSPacket {
	
	public static final int PID_FOR_PAT = 0x0000;
	
	private static final byte TRANSPORTERRORINDICATOR_BITMASK = (byte) 0x80;
	private static final byte PAYLOADUNITSTARTINDICATOR_BITMASK = (byte) 0x40;
	private static final byte TRANSPORTPRIORITY_BITMASK = (byte) 0x20;
	private static final byte PID_FIRSTBYTE_BITMASK = (byte) 0x1F;
	private static final byte SCRAMBLINGCONTROL_BITMASK = (byte) 0xC0;
	private static final byte ADAPTATIONFIELDEXIST_BITMASK = (byte) 0x30;
	private static final byte CONTINUITYCOUNTER_BITMASK = (byte) 0x0F;
	
	private byte packetHeader[];
	
	private byte syncByte; 
	// Should be 0x47
	
	private boolean transportErrorIndicator; 
	// Set by demodulator if can't correct errors in the stream, to tell the demultiplexer that the packet has an uncorrectable error
	
	private boolean payloadUnitStartIndicator; 
	// 1 means start of PES data or PSI otherwise zero only.
	
	private boolean transportPriority; 
	// 1 means higher priority than other packets with the same PID.
	
	private int pid; 
	// Packet ID or Program ID depending on what you prefer
	
	private byte scramblingControl;
	/* '00' = Not scrambled.   The following per DVB spec:[11]   
	 * '01' = Reserved for future use,   
	 * '10' = Scrambled with even key,   
	 * '11' = Scrambled with odd key */
	
	private byte adaptationFieldExist;
	/* 01 = no adaptation fields, payload only
	 * 10 = adaptation field only
	 * 11 = adaptation field and payload */
	
	private byte continuityCounter;
	// Incremented only when a payload is present (i.e., adaptation field exist is 01 or 11)
	
	private AdaptationField adaptationField;
	
	private byte payloadData[];
	
	// Constructor for whole packet
	public MPEG2TSPacket(byte tsPacket[]) {
		//byte[] tempField;
		packetHeader = new byte[4];
		System.arraycopy(tsPacket, 0, packetHeader, 0, 4);
		
		readHeader();
		
		if(adaptationFieldExist == 0b01) { // no adaptation fields, payload only
			payloadData = new byte[tsPacket.length -4];
			System.arraycopy(tsPacket, 4, payloadData, 0, tsPacket.length -4);
		} else if(adaptationFieldExist == 0b10) { // adaptation field only
			//tempField = new byte[tsPacket.length -4];
			//System.arraycopy(tsPacket, 4, tempField, 0, tsPacket.length -4);
			//adaptationField = new AdaptationField(tempField);
		} else if(adaptationFieldExist == 0b11) { // adaptation field and payload
			int adaptationFieldLength = (tsPacket[4] & 0xFF);
			int payloadLength = tsPacket.length - adaptationFieldLength - 4;
			
			//tempField = new byte[adaptationFieldLength];
			payloadData = new byte[payloadLength];
			
			//System.arraycopy(tsPacket, 4, tempField, 0, adaptationFieldLength);
			//adaptationField = new AdaptationField(tempField);
			System.arraycopy(tsPacket, adaptationFieldLength +4, payloadData, 0, payloadLength);			
		}
	}
	
	// Constructor for packet parts
	public MPEG2TSPacket(byte packetHeader[], AdaptationField adaptationField, byte payloadData[]) {
		this.packetHeader = packetHeader;
		this.adaptationField = adaptationField;
		this.payloadData = payloadData;
		
		readHeader();
	}
	
	// Read the packetHeader and unpacks its values
	private void readHeader() {
		syncByte = packetHeader[0];
		
		transportErrorIndicator = (packetHeader[1] & TRANSPORTERRORINDICATOR_BITMASK) == 1;

		payloadUnitStartIndicator = (packetHeader[1] & PAYLOADUNITSTARTINDICATOR_BITMASK) == 1;

		transportPriority = (packetHeader[1] & TRANSPORTPRIORITY_BITMASK) == 1;
		
		int pid2 = ((packetHeader[1] & PID_FIRSTBYTE_BITMASK) << 8) + packetHeader[2];
		pid = (256*(packetHeader[1]&0x1f)+(packetHeader[2]&0xff));
		
		byte scramblingControl2 = (byte) ((byte) (packetHeader[3] & SCRAMBLINGCONTROL_BITMASK) >>> 6);
		scramblingControl = (byte) ((packetHeader[3]>>6)&0x03);
		
		byte adaptationFieldExist2 = (byte) ((byte) (packetHeader[3] & ADAPTATIONFIELDEXIST_BITMASK) >>> 4);
		adaptationFieldExist=(byte) ((packetHeader[3]>>4)&0x03);
		
		boolean piddif = pid2 != pid;
		boolean scramdif = scramblingControl != scramblingControl2;
		boolean adapdif = adaptationFieldExist != adaptationFieldExist2;

		if(piddif || scramdif || adapdif) {
			System.out.println("Dif!");
		}
		
		continuityCounter = (byte) (packetHeader[3] & CONTINUITYCOUNTER_BITMASK);
		
	}
	
	public int getPID() {
		return pid;
	}
	
	public boolean getPayloadUnitStartIndicator() {
		return payloadUnitStartIndicator;
	}
	
	public boolean getTransportPriority() {
		return transportPriority;
	}
	
	public byte getScramblingControl() {
		return scramblingControl;
	}
	
	public byte getContinuityCounter() {
		return continuityCounter;
	}
	
	public byte[] getPacketHeader() {
		return packetHeader;
	}
	
	public AdaptationField getAdaptationField() {
		return adaptationField;
	}
	
	public byte[] getPayloadData() {
		return payloadData;
	}
	
	public int getPayloadSize() {
		return payloadData.length;
	}
	
	public boolean isPAT() {
		return pid == 0x0000;
	}
	
	public boolean hasError() {
		boolean error = false;
		
		if(transportErrorIndicator)
			error = true;
		
		if(syncByte != 0x47)
			error = true;
		
		return error;
	}
	
	public boolean hasPayloadData () {
		return payloadData.length > 0;
	}
	
	public void printHeader() {
		System.out.println("syncByte: " + Integer.toHexString(syncByte));
		System.out.println("transportErrorIndicator: " + Boolean.toString(transportErrorIndicator));
		System.out.println("payloadUnitStartIndicator: " + Boolean.toString(payloadUnitStartIndicator));
		System.out.println("transportPriority: " + Boolean.toString(transportPriority));
		System.out.println("pid: " + Integer.toHexString(pid));
		System.out.println("scramblingControl" + Integer.toBinaryString(scramblingControl));
		System.out.println("adaptationFieldExist" + Integer.toBinaryString(adaptationFieldExist));
		System.out.println("continuityCounter" + continuityCounter);
	}
	
	public String toString() {
		String string;
		
		string = Integer.toHexString(syncByte) + "\t" + Boolean.toString(transportErrorIndicator) + "\t" + Boolean.toString(payloadUnitStartIndicator) + 
			"\t" + Boolean.toString(transportPriority) + "\t" + Integer.toHexString(pid) + "\t" + Integer.toBinaryString(scramblingControl) + 
			"\t" + Integer.toBinaryString(adaptationFieldExist) + "\t" + continuityCounter;
		
		if(adaptationFieldExist > 1) {
			string += "\t" + adaptationField.toString();
		}
		
		return string;
	}
}

