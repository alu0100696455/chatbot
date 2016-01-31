

import javax.swing.JFrame;




/**
 * Clase principal para la ejecución del Chatbsot
 *
 */

class Principal {
	private static final int WIDTH = 400;
	private static final int HEIGHT = 200;
	
	public static void main(String[] args) throws Exception {
		JFrame frame = new JFrame();
		Chatbot applet = new Chatbot();
		//applet.setVisible(true);
	    frame.add(applet);
		frame.setTitle("Gamebot");
	    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
	    frame.setSize(WIDTH, HEIGHT);
	    frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}