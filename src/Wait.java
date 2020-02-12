import java.util.concurrent.TimeUnit;
public class Wait{
    public static void waitMilliSeconds(int time){
        try{TimeUnit.MILLISECONDS.sleep(time);}catch(Exception e){System.out.println(e);}
    }
}