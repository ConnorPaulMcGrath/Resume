import javax.swing.SwingUtilities;

/**
 * Launcher class for the CBIR and readImage classes in the 
 * Connor_McGrath_490B_HW1.jar file.
 * 
 * @author Connor Paul McGrath
 */

public class Launcher {
	public static void main(String[] args){
		//get intensity and color code information
		new readImage();
		
		//initialize and run the CBIR GUI
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				CBIR app = new CBIR();
				app.setVisible(true);
			}
		});
	}
}
