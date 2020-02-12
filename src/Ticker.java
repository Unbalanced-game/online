import java.util.Timer;
import java.util.TimerTask;
public class Ticker {
    long duration = 1234;
    Timer timer;
    GameManager gm;

    public Ticker(int duration, GameManager gm){
        this.duration=duration;
        this.gm=gm;
    }
    
    public void startTimer(){
        timer = new Timer();
        startNow();
    }
    
    public void startNow(){
        timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    doStuff();
                }
            }, 0, duration);
    }
    
    public void stop(){
        timer.cancel();
    }

    public void doStuff(){
        gm.tickerEvent();
    }
}