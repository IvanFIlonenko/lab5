package lab5;

import com.sun.tools.javac.util.Pair;
import scala.Int;
import sun.rmi.server.InactiveGroupException;

public class PairMsg {
    Pair<String, Integer> msgPair;

    public PairMsg(Pair<String, Integer> pair) {
        this.msgPair = pair;
    }
}
