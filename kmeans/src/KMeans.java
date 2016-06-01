import java.util.*;

public class KMeans {
	
	public static final int K = 32;
	
	private static final KMeans kmeans = new KMeans();
	public static final KMeans getInstance() {return kmeans;}
	
	public int [][][] process(int [][][] rdata, int max) {
		
		int [][][] data = new int[rdata.length][rdata[0].length][3];
		for (int i = 0; i < rdata.length; i++)
			for (int j = 0; j < rdata[i].length; j++)
				for (int k = 0; k < 3; k++)
					data[i][j][k] = rdata[i][j][k];
		
		System.out.println("Begin process for " + max + " iterations.");
		int [][][] groups = getFirstGroup(data);
		
		while (max-- > 0) {
			System.out.println("Left " + (max+1) + " iteration.");
			int [][][] nextGroups = getNextGroup(groups);
			if (isEquals(groups, nextGroups)) {
				System.out.println("The same group, exit.");
				break;
			}
			
			groups = nextGroups;
		}
		
		// 取得最後分群的最終顏色
		int [][] colors = getClusterCenter(groups);
		
		Map<int[], int[]> mapColor = new HashMap<>();
		for (int group_idx = 0; group_idx < K; group_idx++) {
			for (int [] color : groups[group_idx])
				mapColor.put(color, colors[group_idx]);
		}
			
		for (int h = 0; h < data.length; h++)
			for (int w = 0; w < data[h].length; w++){
				@SuppressWarnings("unused")
				int [] from = data[h][w];
				int [] to = mapColor.get(data[h][w]);
				/*System.out.println(from[0]+","+from[1]+","+from[2] + ">>" +
						to[0]+","+to[1]+","+to[2]);*/
				data[h][w] = to;
			}
				
		System.out.println("Finish.");
		return data;
	}
	
	private boolean isEquals(int [][][] a, int [][][] b) {
		if (a == null || b == null) return false;

		if (a.length != b.length) return false;
		for (int i = 0; i < a.length; i++) {
			if (a[i].length != b[i].length) return false;
			for (int j = 0; j < a[i].length; j++) {
				if (a[i][j].length != b[i][j].length) return false;
				for (int k = 0; k < a[i][j].length; k++) {
					if (a[i][j][k] != b[i][j][k]) return false;
				}
			}
		}
			
		return true;
	}
	
	/**
	 * 取得初始的 K 個點
	 * @param data 整張圖
	 * @return 挑種的點
	 */
	private int [][] getInitialColors(int [][][] data) {
		if (data == null) return null;
		int height = data.length, width = data[0].length;
		
		Set<int []> colors = new HashSet<>();
		
		while (colors.size() < K) {
			int random_h = (int) (Math.random() * height);
			int random_w = (int) (Math.random() * width);
			colors.add(data[random_h][random_w]);
		}
		return colors.toArray(new int[K][]);
	}
	
	/**
	 * 取得分群中心
	 * @param data 分群 [群集][屬於該群的每個顏色][RGB]
	 * @return 分群中心 [群集][RGB]
	 */
	private int [][] getClusterCenter(int [][][] data) {
		if (data == null) return null;
		if (data.length != K) System.out.println("Warning >> 發現分群數不為預期的 K");
		
		int [][] centers = new int [data.length][3];
		for (int i = 0; i < data.length; i++) {
			if (data[i].length == 0) {
				centers[i] = null;
				continue;
			}
			
			int sum_R = 0, sum_G = 0, sum_B = 0;
			//System.out.println(i);
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
	
	/**
	 * 取得下一輪分群
	 * @param data 前一輪分群 [群集][屬於該群的每個顏色][RGB]
	 * @return 下一輪分群 [群集][屬於該群的每個顏色][RGB]
	 */
	private int [][][] getNextGroup(int [][][] data) {
		
		if (data == null) return null;	
		int [][] colors = getClusterCenter(data);
		
		for (int i = 0; i < colors.length; i++)
			if (colors[i] == null) {
				int [][] temp;
				while ((temp = data[(int) (Math.random()*data.length)]).length == 0);
				colors[i] = temp[(int) (Math.random()*temp.length)];
			}
		
		return getGroup(data, colors);
	}
	
	
	
	/**
	 * 取得第一輪分群
	 * @param data 整張圖 [Height][Wigth][RGB]
	 * @return 第一輪分群 [群集][屬於該群的每個顏色][RGB]
	 */
	private int [][][] getFirstGroup(int [][][] data) {
		
		if (data == null) return null;
		int [][] colors = getInitialColors(data);
		
		return getGroup(data, colors);
	}
	
	private int [][][] getGroup(int [][][] data, int [][] center) {
		
		int [][][] nextGroup = new int[K][][];
		List<List<int[]>> temp = new ArrayList<>(K);
		for (int i = 0; i < K; i++) 
			temp.add(new ArrayList<int[]>());
		
		for (int g = 0, groupCount = data.length; g < groupCount; g++)
			for (int c = 0, colorsCount = data[g].length; c < colorsCount; c++) {
				int min = Integer.MAX_VALUE;
				int min_group_idx = -1;
				for (int idx = 0; idx < center.length; idx++) {
					int tmp_sum = 0;
					for (int i = 0; i < 3; i++)
						tmp_sum += Math.pow(data[g][c][i] - center[idx][i], 2);
					int dis = (int) Math.pow(tmp_sum, 0.5);
					if (dis < min) {
						min_group_idx = idx;
						min = dis;
					}
				}
				temp.get(min_group_idx).add(data[g][c]);
			}
		
		for (int i = 0; i < K; i++) 
			nextGroup[i] = temp.get(i).toArray(new int [temp.get(i).size()][]);
		
		return nextGroup;
	}
	
}
