import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	
	private static final long serialVersionUID = 3503354214955001924L;

	private int [][][] data;
	
	public void paintComponent(Graphics g, int data [][][]){
		 this.data = data;
		 paintComponent(g);
	 }
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawImage(g);
	}
	
	/**
	 * 畫出圖像 (優化)
	 * @param g 畫筆
	 */
	private void drawImage(Graphics g) {
		if (data == null) return;
		
		int width = data[0].length, height = data.length;
		
		BufferedImage imageOut = 
			    new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		int [] outImage = new int [width * height];
		
		for (int y = 0; y < data.length; y++)
	    	for (int x = 0; x < data[y].length; x++)
	    		outImage[y * width + x] = 
	    			(data[y][x][0]<<16) + (data[y][x][1]<<8) + (data[y][x][2]);
		
		imageOut.setRGB(0, 0, width, height, outImage, 0, width);
		g.drawImage(imageOut, 0, 0, null);
		
		/*for (int y=0; y< data.length; y++){
	    	for (int x = 0; x < data[y].length; x++){
	    		int red = data[y][x][0];
	    		int green = data[y][x][1];
	            int blue =  data[y][x][2];
	            g.setColor(new Color(red, green, blue));
	    	    g.drawLine(x, y, x, y);		
	    	} 
	  	}*/
	} 
	
	public Color getColorByPoint(int x, int y) {
		return new Color(data[y][x][0], data[y][x][1], data[y][x][2]);
	}
}
