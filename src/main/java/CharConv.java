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
