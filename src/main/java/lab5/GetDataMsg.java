package lab5;

import com.sun.tools.javac.util.Pair;

public class getDataMsg {
    private Pair<String, Integer> msg;
    public getDataMsg(Pair<String, Integer> msg){
        this.msg = msg;
    }

    public Pair<String, Integer> getMsg(){
        return msg;
    }
}
