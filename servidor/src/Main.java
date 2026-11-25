public class Main
{
    public static void main(String[] args)
    {
        try
        {
            ApiServer.Iniciar(8080);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}