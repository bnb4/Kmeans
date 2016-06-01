import javax.swing.JFrame;

public class Main {
	public static void main(String[] args) {
		JFrame frame = new ImageGui();
		frame.setSize(1500, 1500);
		frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
