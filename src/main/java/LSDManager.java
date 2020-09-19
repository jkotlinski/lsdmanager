import javax.swing.UIManager;

public class LSDManager
{

	public static void main(String[] args)
	{
		//Try to set local system theme, default to cross-platform if fails.
		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel(
						UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		//Validate frames that have preset sizes
		//Pack frames that have useful preferred size info, e.g. from their layout
		Frame frame = new Frame();
		frame.validate();
		frame.setLocation(200,200);
		frame.setVisible(true);
	}

}