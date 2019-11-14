package lab5;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.*;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.util.ByteString;
import org.asynchttpclient.Response;
import scala.concurrent.Future;
import org.asynchttpclient.*;
import static org.asynchttpclient.Dsl.*;
import static org.asynchttpclient.Dsl.asyncHttpClient;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("start!");
        ActorSystem system = ActorSystem.create("routes");
        final Http http = Http.get(system);
        final ActorMaterializer materializer =
                ActorMaterializer.create(system);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = Flow.of(HttpRequest.class).map(
                request -> {
                    String url =  request.getUri().query().get("testUrl").get();
                    String count =  request.getUri().query().get("count").get();
                    AsyncHttpClient c = asyncHttpClient(config().setProxyServer(proxyServer("127.0.0.1", 38080)));
                    ListenableFuture<Response> whenResponse = asyncHttpClient().prepareGet("http://www.example.com/").execute();
                    Response response = whenResponse.get();
                    if(request.method() == HttpMethods.GET) {
                        if (request.getUri().path().equals("/")) {
                            return HttpResponse.create().withEntity(ContentTypes.TEXT_HTML_UTF8,
                                    ByteString.fromString(url + " " + count));
                        } else {
                            request.discardEntityBytes(materializer);
                            return HttpResponse.create().withStatus(StatusCodes.NOT_FOUND).withEntity("NO");
                        }
                    } else {
                        request.discardEntityBytes(materializer);
                        return HttpResponse.create().withStatus(StatusCodes.NOT_FOUND).withEntity("NO");

                    }
                }
        );
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
