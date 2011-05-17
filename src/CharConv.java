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
public class CharConv
{
    static int convertLsdCharToAscii ( int a_char )
    {
        if ( a_char >= 65 && a_char <= (65+25) )
        {
            //char
            return 'A' + a_char - 65;
        }
        if ( a_char >= 48 && a_char < 58 )
        {
            //decimal number
            return '0' + a_char - 48;
        }
        if ( 0 == a_char )
        {
            return 0;
        }
        return ' ';
    }
}
