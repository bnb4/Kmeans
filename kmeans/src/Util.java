public class Util {
	
	final static int checkPixelBounds(int value){
		if (value >255) return 255;
		if (value <0) return 0;
		return value;
 	} 
	
	//get red channel from colorspace (4 bytes)
	final static int getR(int rgb){
		  return checkPixelBounds((rgb & 0x00ff0000)>>>16);	
    }

	//get green channel from colorspace (4 bytes)
	final static int getG(int rgb){
	  return checkPixelBounds((rgb & 0x0000ff00)>>>8);
	}
	
	//get blue channel from colorspace (4 bytes)
	final static int getB(int rgb){
		  return  checkPixelBounds(rgb & 0x000000ff);
	}

	/**
	 * 複製三維整數陣列
	 * @param rdata 傳入陣列
	 * @return 複製的陣列
	 */
	final static int [][][] clone(int [][][] rdata) {
		int [][][] data = new int[rdata.length][rdata[0].length][3];
		for (int i = 0; i < rdata.length; i++)
			for (int j = 0; j < rdata[i].length; j++)
				for (int k = 0; k < 3; k++)
					data[i][j][k] = rdata[i][j][k];
		return data;
	}
}