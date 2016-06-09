import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ImageGui extends JFrame implements MouseMotionListener, MouseListener {

	private static final long serialVersionUID = 4355249614879154117L;
	
	JPanel cotrolPanelMain = new JPanel();
	JPanel cotrolPanelShow = new JPanel();;
	JPanel colorPanel = new JPanel();
	JPanel nowColor = new JPanel();
	
	ImagePanel imagePanel;
	ImagePanel imagePanel2;

	JButton btnShow = new JButton("顯示");
	JButton btnDither = new JButton("降色");
	JTextField kTextField = new JTextField("64", 6);
	
	JSlider slider;
	JLabel lbIteration = new JLabel("Iteration : ");
	JLabel lbNow = new JLabel("10");
	JLabel lbK = new JLabel("K : ");
	
	JLabel[] colorLabels;
	
	int[][][] data;
	int height;
	int width;
	BufferedImage img = null;
	int iteration;
	boolean isDithered = false;
	JLabel nowHighlightLabel;
	
	ImageGui() {
		setBounds(0, 0, 1300, 700);
		getContentPane().setLayout(null);
	    setTitle("K-means application to colored image's dithering");
		
		try {
			img = ImageIO.read(new File("image/Munich.png"));
			//img = ImageIO.read(new File("image/F16.png"));
		} catch (IOException e) {
			System.out.println("IO exception");
		}
		
		height = img.getHeight();
		width = img.getWidth();
		data = new int[height][width][3]; 
		
		for (int y = 0; y < height; y++){
	    	for (int x = 0; x < width; x++){
	    		int rgb = img.getRGB(x, y);
	    		data[y][x][0] = Util.getR(rgb);
	    		data[y][x][1] = Util.getG(rgb);
	    		data[y][x][2] = Util.getB(rgb);
	    	}
	    }
		
		cotrolPanelMain = new JPanel();
		cotrolPanelMain.setLayout(new GridLayout(7, 1));
		cotrolPanelShow.add(btnShow);
		cotrolPanelShow.add(btnDither);
		cotrolPanelShow.add(lbK);
		cotrolPanelShow.add(kTextField);
		cotrolPanelShow.add(lbIteration);
		cotrolPanelMain.setOpaque(false); // fix: 背景設為透明
		
		slider = new JSlider(0, 200, 10);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing(50);
		slider.setMinorTickSpacing(10);
		
		iteration = slider.getValue();
		cotrolPanelShow.add(slider);
		cotrolPanelShow.add(lbNow);
		cotrolPanelMain.add(cotrolPanelShow);
		cotrolPanelMain.setBounds(0, 0, 1200,350);
		getContentPane().add(cotrolPanelMain);
		
	    imagePanel = new ImagePanel();
	    imagePanel.setBounds(20, 100, 620, 450);
	    getContentPane().add(imagePanel);
	    
	    imagePanel2 = new ImagePanel();
	    imagePanel2.setBounds(630, 100, 1230, 450);
	    imagePanel2.addMouseMotionListener(this);
	    imagePanel2.addMouseListener(this);
	    getContentPane().add(imagePanel2);
	    	    
	    colorPanel = new JPanel();
	    colorPanel.setLayout(new FlowLayout(10));
	    colorPanel.setBounds(20, 550, 1140, 150);
	    getContentPane().add(colorPanel);
	    
	    nowColor = new JPanel();
	    nowColor.setBounds(1180, 550, 50, 50);
	    getContentPane().add(nowColor);

	    btnShow.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent arg0) {
				Graphics g = imagePanel.getGraphics();
				imagePanel.paintComponent(g, data);
			}
	    });   
		    
	    slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				iteration = slider.getValue();
				lbNow.setText(String.valueOf(slider.getValue()));
				System.out.println("iteration = " + iteration);
			}
		});
	  
	    btnDither.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent arg0) {
				colorPanel.removeAll();
				colorPanel.repaint();
				Graphics g = imagePanel2.getGraphics();
				int k = Integer.parseInt(kTextField.getText());
				
	    		if (k > 0) {
	    			int[][][] dataDither = KMeans.getInstance().process(data, slider.getValue(), k);
	    			int[][] finalColor = KMeans.getInstance().getFinalColors();
	    			imagePanel2.paintComponent(g, dataDither);
	    			colorLabels = new JLabel[k];
	    			createColorsLabel(finalColor);
	    			showDitherColor();
	    			isDithered = true;
	    		}
			}
	    });   
	}
	
	private void createColorsLabel(int[][] finalColor) {
		for (int i = 0; i < finalColor.length; i++) {
			JLabel label = new JLabel();
			label.setBackground(new Color(finalColor[i][0], finalColor[i][1], finalColor[i][2]));
			label.setPreferredSize(new Dimension(20, 20));
			label.setOpaque(true);
			
			colorLabels[i] = label;
		}
	}
	
	private void showDitherColor() {
		for (int i = 0; i < colorLabels.length; i++) {
			colorPanel.add(colorLabels[i]);
		}
		this.getContentPane().revalidate();
	}

	@Override
	public void mouseDragged(MouseEvent e) {}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(nowHighlightLabel != null) {
			nowHighlightLabel.setBorder(null);
			nowHighlightLabel = null;
			nowColor.setBackground(null);
		}
		
		if (isDithered && e.getX() < width && e.getY() < height) {
			Color targetColor = imagePanel2.getColorByPoint(e.getX(), e.getY());
			for (JLabel jLabel : colorLabels) {
				if (jLabel.getBackground().equals(targetColor)) {
					nowHighlightLabel = jLabel;
					jLabel.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
					nowColor.setBackground(targetColor);
					break;
				}
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {
		if(nowHighlightLabel != null) {
			nowHighlightLabel.setBorder(null);
			nowHighlightLabel = null;
			nowColor.setBackground(null);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}
}