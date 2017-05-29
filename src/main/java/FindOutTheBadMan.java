/**
 * Created by Claudius on 2017/5/29.
 */
public class FindOutTheBadMan implements  Runnable {
    private  FindComent findComent;
    public  FindOutTheBadMan (FindComent findComent){
        this.findComent = findComent;
    }
    @Override
    public void run() {
        while (findComent.getTid() > 1528100)
        findComent.findLoop(findComent.reduce());
    }
}
