
public class AdaptationField {
	private byte adaptationFieldLength;
	private boolean discontinuityIndicator;
	private boolean randomAccessIndicator;
	private boolean elementaryStreamPriorityIndicator;
	private boolean pcrFlag;
	private boolean opcrFlag;
	private boolean splicingPointFlag;
	private boolean transportPrivateDataFlag;
	private boolean adaptationFieldExtensionFlag;
	private long pcr;
	private long opcr;
	private byte splicingCountdown;
	
	public AdaptationField(byte[] data) {
		if(data.length > 2) {
			adaptationFieldLength = data[0];
			discontinuityIndicator = (data[1] & 0x80) == 1;
			randomAccessIndicator = (data[1] & 0x40) == 1;
			elementaryStreamPriorityIndicator = (data[1] & 0x20) == 1;
			pcrFlag = (data[1] & 0x10) == 1;
			opcrFlag = (data[1] & 0x08) == 1;
			splicingPointFlag = (data[1] & 0x04) == 1;
			transportPrivateDataFlag = (data[1] & 0x02) == 1;
			adaptationFieldExtensionFlag = (data[1] & 0x01) == 1;
			
			int i = 2;
			pcr = 0;
			if(pcrFlag) {
				for(int i2 = 0; i2 < 6; i2++) {
					pcr += ((long) data[i] & 0xffL) << (8 * i);
				}
				i += 6;
			}
			
			opcr = 0;
			if(opcrFlag) {
				for(int i2 = 0; i2 < 6; i2++) {
					opcr += ((long) data[i] & 0xffL) << (8 * i);
				}
				i += 6;
			}
			
			if(splicingPointFlag) {
				splicingCountdown = data[i];
			} else {
				splicingCountdown = 0;
			}
		} else {
			System.out.println("Error: data to short. Is " + data.length);
		}
		
	}
	
	public String toString() {
		String string;
		
		string = adaptationFieldLength + "\t" + Boolean.toString(discontinuityIndicator) + "\t" + Boolean.toString(randomAccessIndicator) + 
			"\t" + Boolean.toString(elementaryStreamPriorityIndicator) + "\t" + Boolean.toString(pcrFlag) + "\t" + Boolean.toString(opcrFlag) + 
			"\t" + Boolean.toString(splicingPointFlag) + "\t" + Boolean.toString(transportPrivateDataFlag) + 
			"\t" + Boolean.toString(adaptationFieldExtensionFlag) + "\t" + pcr + "\t" + opcr + "\t" + splicingCountdown;
		
		return string;
	}

}
