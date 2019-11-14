package lab5;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;

import java.util.HashMap;
import java.util.Map;

public class Storage extends AbstractActor {

    private HashMap<String, Map<Integer, Integer>> data = new HashMap<>();

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create().
                match(GetDataMsg.class, msg -> {
                    if (data.containsKey(msg.getUrl()) && data.get(msg.getUrl()).containsKey(msg.getValue())) {
                        getSender().tell(data.get(msg.getUrl()).get(msg.getUrl()), ActorRef.noSender());
                    } else {
                        getSender().tell(-1, ActorRef.noSender());
                    }
                }).match(PutDataMsg.class, msg ->{
                    data.put(msg.getUrl(),new HashMap<>(msg.getReqestNumber(), msg.getTime()));
        })
                .build();
    }
}
