package lab5;

import javafx.util.Pair;

public class GetDataMsg {
    private Pair<String, Integer> msg;
    public GetDataMsg(Pair<String, Integer> msg){
        this.msg = msg;
    }

    public Pair<String, Integer> getMsg(){
        return msg;
    }
}
