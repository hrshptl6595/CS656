import java.io.FileInputStream;

public class Test
{
    public static void main(String[] args) throws Exception
    {
        FileInputStream fin = new FileInputStream(args[0]);
        System.out.println("Total file size to read : " + fin.available());

        byte[] content = new byte[fin.available()];
        int x;
        int i = 0;
        while((x = fin.read()) != -1)
        {
            content[i] = (byte)x;
            i++;
        }
        // for(int i=0; i<=fin.available(); i++)
        // {
        //     int x;
        //     if((x = fin.read()) != -1)
        //     {
                
        //     }
        // }
        System.out.println(new String(content));
    }
}