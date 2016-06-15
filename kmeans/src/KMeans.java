import java.util.*;

public class KMeans {
	
	// Singleton pettern
	private static final KMeans kmeans = new KMeans();
	public static final KMeans getInstance() {return kmeans;}
	private static int[][] finalColors = null;
	
	private double boxSideLength;
	private Map<String, ArrayList<int[]>> boxsData;
	private Map<String, ArrayList<int[]>> boxsCenter;
	
	// 設定預設的分群數量
	private int K = 64;
	
	/**
	 * 外部呼叫方法，包含整個流程
	 * @param rdata 傳入圖片
	 * @param max 最多執行次數
	 * @return 回傳圖片
	 */
	public int [][][] process(int [][][] rdata, int max, int k) {
		
		System.out.println("Start: " + Calendar.getInstance().getTime());
		
		// 設定要跑的 k數量
		this.K = k;
		
		// 複製陣列來執行 (防止串改到原始資料 陣列資料為  Call by reference.)
		int [][][] data = Util.clone(rdata);
		
		// 分格子
		int boxSideNum = (int)(Math.pow(K, 1/3));
		boxSideLength = 256 / boxSideNum;
		boxsData = new HashMap<>();
		
		for(int h = 0; h < data.length; h++) {
			for(int w = 0; w < data[h].length; w++) {
				String rString = (int)(data[h][w][0] / boxSideLength) + "";
				String gString = (int)(data[h][w][1] / boxSideLength) + "";
				String bString = (int)(data[h][w][2] / boxSideLength) + "";
				String key = rString + gString + bString;
				if (!boxsData.containsKey(key)) {
					boxsData.put(key, new ArrayList<int[]>());
				}
				boxsData.get(key).add(data[h][w]);
			}
		}
		
		// 取得初始分群
		System.out.println("Begin process for " + max + " iterations.");
		int [][][] groups = getFirstGroup(data);
		
		// 開始重複執行分群
		while (max-- > 0) {
			System.out.println("Left " + (max+1) + " iteration(s).");
			int [][][] nextGroups = getNextGroup(groups);
			
			// 若群集沒有改變，跳出分群執行
			if (groups.equals(nextGroups)) {
				System.out.println("The clusters are stable, break.");
				break;
			}
			
			groups = nextGroups;
		}
		
		System.out.println("Finish: " + Calendar.getInstance().getTime());
		
		// 取得最後分群的最終顏色
		int [][] colors = getClusterCenter(groups);
		finalColors = colors;
		
		// 將顏色結果對應，方便後面處理圖像
		Map<int[], int[]> mapColor = new HashMap<>();
		for (int group_idx = 0; group_idx < K; group_idx++)
			for (int [] color : groups[group_idx])
				mapColor.put(color, colors[group_idx]);
			
		// 處理圖像成對應的顏色
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
	
	/**
	 * 取得初始的 K 個點
	 * @param data 整張圖
	 * @return 挑種的點
	 */
	private int [][] getInitialColors(int [][][] data) {
		if (data == null) return null;
		int height = data.length, width = data[0].length;
		
		Set<int []> colors = new HashSet<>();
		
		// 取到不重複的 K個點 (若不同點數值相同則仍會被選到)
		while (colors.size() < K) {
			int random_h = (int) (Math.random() * height);
			int random_w = (int) (Math.random() * width);
			colors.add(data[random_h][random_w]);
			
			putCenterBox(data[random_h][random_w]);
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
			
			// 若某個群沒有任何顏色，則直接填入不處理，後面程序會在重新指派新的中心
			if (data[i].length == 0) {
				centers[i] = null;
				continue;
			}
			
			// 加總該群所有數值方便計算平均
			int sum_R = 0, sum_G = 0, sum_B = 0;
			for (int [] rgb : data[i]) {
				sum_R += rgb[0];
				sum_G += rgb[1];
				sum_B += rgb[2];
			}
			
			// 儲存平均
			centers[i][0] = sum_R / data[i].length;
			centers[i][1] = sum_G / data[i].length;
			centers[i][2] = sum_B / data[i].length;
			
			putCenterBox(centers[i]);
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
		
		boxsCenter = new HashMap<>();
		int [][] colors = getClusterCenter(data);
		
		// 若某個群是 null，則隨機指派新的中心點
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
		
		boxsCenter = new HashMap<>();
		int [][] colors = getInitialColors(data);
		
		return getGroup(data, colors);
	}
	
	/**
	 * 分類每個顏色應該在哪個分群之中
	 * @param data 上一輪分群
	 * @param center 新的中心點
	 * @return 下一輪分群
	 */
	private int [][][] getGroup(int [][][] data, int [][] center) {	
		// 儲存結構
		int [][][] nextGroup = new int[K][][];
		List<List<int[]>> temp = new ArrayList<>(K);
		
		// 初始化暫存結構
		for (int i = 0; i < K; i++) 
			temp.add(new ArrayList<int[]>());
		
		// 開始找尋每個顏色最近的中央點並歸類
		/*for (int g = 0, groupCount = data.length; g < groupCount; g++)
			for (int c = 0, colorsCount = data[g].length; c < colorsCount; c++) {
				int min = Integer.MAX_VALUE;
				int min_group_idx = -1;
				
				// 找出最近的點
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
			}*/
		
		for (String key : boxsData.keySet()) {
			ArrayList<int[]> list = boxsData.get(key);
			for (int[] pointData : list) {
				int x = key.charAt(0) - '0';
				int y = key.charAt(1) - '0';
				int z = key.charAt(2) - '0';
				
				int index = 0;
				int min = Integer.MAX_VALUE;
				int[] minPoint = new int[3];
				while(true) {
					for(int i = index * -1; i < index; i++) {
						for(int j = index * -1; j < index; j++) {
							for(int k = index * -1; k < index; k++) {
								String tx = (x + i) + "";
								String ty = (y + j) + "";
								String tz = (z + k) + "";
								String target = tx + ty + tz;
								if (boxsCenter.containsKey(target)) {
									for (int[] pointCenter : boxsCenter.get(target)) {
										int tmpSum = 0;
										tmpSum += Math.pow(pointCenter[0] - pointData[0], 2);
										tmpSum += Math.pow(pointCenter[1] - pointData[1], 2);
										tmpSum += Math.pow(pointCenter[2] - pointData[2], 2);

										if (tmpSum < min) {
											min = tmpSum;
											minPoint = pointCenter;
										}
									}
								}
							}
						}
					}
					if (min != Integer.MAX_VALUE) {
						for (int centerId = 0; centerId < K; centerId++){
							if (center[centerId].equals(minPoint)) {
								temp.get(centerId).add(pointData);
								break;
							}
						}
						break;
					}
					index++;
				}
			}
		}
		
		// 將暫存結構存入儲存結構
		for (int i = 0; i < K; i++) 
			nextGroup[i] = temp.get(i).toArray(new int [temp.get(i).size()][]);
		
		return nextGroup;
	}
	
	public int[][] getFinalColors() {
		return finalColors;
	}
	
	public void putCenterBox(int[] pointRGB) {
		String rString = (int)(pointRGB[0] / boxSideLength) + "";
		String gString = (int)(pointRGB[1] / boxSideLength) + "";
		String bString = (int)(pointRGB[2] / boxSideLength) + "";
		String key = rString + gString + bString;
		if (!boxsCenter.containsKey(key)) {
			boxsCenter.put(key, new ArrayList<int[]>());
		}
		boxsCenter.get(key).add(pointRGB);
	}
}
