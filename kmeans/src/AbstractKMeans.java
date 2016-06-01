
public abstract class AbstractKMeans {
	
	public static final int K = 256;
	
	public int [][][] process(int [][][] data) {
		
		return data;
	}
	
	/**
	 * 取得初始的 256 個點
	 * @param data 整張圖
	 * @return 挑種的點
	 */
	private int [][] getInitialColors(int [][][] data) {
		if (data == null) return null;
		int height = data.length, width = data[0].length;
		int [][] colors = new int [K][];
		for (int i = 0; i < K; i++) {
			int random_h = (int) (Math.random() * height);
			int random_w = (int) (Math.random() * width);
			colors[i] = data[random_h][random_w];
		}
		return colors;
	}
	
	/**
	 * 取得分群中心
	 * @param data 分群 [群集][屬於該群的每個點][RGB]
	 * @return 分群中心 [群集][RGB]
	 */
	private int [][] getClusterCenter(int [][][] data) {
		if (data == null) return null;
		if (data.length != K) System.out.println("Debug>> 發現分群數不為預期的 K");
		
		int [][] centers = new int [data.length][3];
		for (int i = 0; i < data.length; i++) {
			int sum_R = 0, sum_G = 0, sum_B = 0;
			for (int [] rgb : data[i]) {
				sum_R += rgb[0];
				sum_G += rgb[1];
				sum_B += rgb[2];
			}
			centers[i][0] = sum_R / data[i].length;
			centers[i][1] = sum_G / data[i].length;
			centers[i][2] = sum_B / data[i].length;
		}
		return centers;
	}
}
