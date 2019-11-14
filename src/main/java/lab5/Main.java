package lab5;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.*;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.sun.tools.javac.util.Pair;
import org.asynchttpclient.Response;
import scala.concurrent.Future;
import org.asynchttpclient.*;
import scala.util.Try;

import static org.asynchttpclient.Dsl.*;
import static org.asynchttpclient.Dsl.asyncHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;

public class Main {
    private static ActorRef controlActor;

    public static void main(String[] args) throws IOException {
        System.out.println("start!");
        ActorSystem system = ActorSystem.create("routes");
        final Http http = Http.get(system);
        final ActorMaterializer materializer =
                ActorMaterializer.create(system);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = Flow.of(HttpRequest.class).map(
                request -> {
                    if(request.method() == HttpMethods.GET) {
                        if (request.getUri().path().equals("/")) {
                            String url =  request.getUri().query().get("testUrl").get();
                            int count =  Integer.parseInt(request.getUri().query().get("count").get());
                            Pair<String, Integer> data = new Pair<>(url,count);
                            try {
                                Source<Pair<String, Integer>, NotUsed> source = Source.from(Collections.singleton(data));
                                Flow<Pair<String, Integer>, HttpResponse, NotUsed> flow = Flow.<Pair<String, Integer>>create()
                                        .map(pair -> new Pair<>(HttpRequest.create().withUri(pair.fst), pair.snd)).
                                                mapAsync(1, pair -> {
                                                    Flow<Pair<HttpRequest, Long>, Pair<Try<HttpResponse>, Long>, NotUsed> httpClient =
                                                            http.superPool(materializer);
                                                    Sink<Pair<Try<HttpResponse>, Long>, CompletionStage<Integer>> fold = Sink.fold(0,
                                                            (accumulator, element) -> {
                                                                int responseTime = (int) (System.currentTimeMillis() - element.snd);
                                                                return accumulator + responseTime;
                                                            });
                                                    return;
                                                    Source.from(Collections.singleton(pair)).toMat(Flow.<Pair<HttpRequest, Integer>>create().
                                                            mapConcat(p -> Collections.nCopies(p.snd, p.fst)).
                                                            map(request2 -> new Pair<>(request2, System.currentTimeMillis())).via(httpClient).toMat(
                                                                    fold, Keep.right()), Keep.right()).run(materializer);
                                                }).map(sum -> {
                                                    Double middleValue = (double)sum/(double)count;
                                        })
                            }
                        } else {
                            request.discardEntityBytes(materializer);
                            return HttpResponse.create().withStatus(StatusCodes.NOT_FOUND).withEntity("NO");
                        }
                    } else {
                        request.discardEntityBytes(materializer);
                        return HttpResponse.create().withStatus(StatusCodes.NOT_FOUND).withEntity("NO");

                    }
                }
        )
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost("localhost", 8080),
                materializer
        );
        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read();
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());
    }
}
