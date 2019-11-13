package lab5;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.stream.ActorMaterializer;

public class Main {
    System.out.println("start!");
    ActorSystem system = ActorSystem.create("routes");
    final Http http = Http.get(system);
    final ActorMaterializer materializer =
            ActorMaterializer.create(system);
}
