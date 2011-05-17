/**
 * <p>Title: LSDManager</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class LSDManager
{

    public LSDManager()
    {
        Frame l_frame = new Frame();
        //Validate frames that have preset sizes
        //Pack frames that have useful preferred size info, e.g. from their layout
        l_frame.validate();
        l_frame.setLocation(200,200);
        l_frame.setVisible(true);

        try
        {
            jbInit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        LSDManager lsdmanager = new LSDManager();
    }

    private void jbInit() throws Exception {
    }
}
