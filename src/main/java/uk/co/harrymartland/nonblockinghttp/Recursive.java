package uk.co.harrymartland.nonblockinghttp;

import static java.util.Objects.isNull;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.client.HttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping("/recursive")
public class Recursive {

    private static final Logger LOG = LoggerFactory.getLogger(Recursive.class);

    @Autowired
    private HttpAsyncClient httpAsyncClient;

    @RequestMapping("/non/blocking/{count}")
    public DeferredResult<String> nonBlocking(@PathVariable("count") final int count, final DeferredResult<String> result) {
        LOG.info("Received recursive none blocking: {}", count);
        if (count == 0) {
            result.setResult("0");
        } else {
            String url = "http://localhost:8080/recursive/non/blocking/" + (count - 1);
            sendNextRequest(url).handle((body, e) -> {
                if (isNull(e)) {
                    LOG.info("Finished recursive none blocking: {}", count);
                    return result.setResult(body);
                } else {
                    LOG.error("Non blocking, exception thrown", e);
                    return result.setResult("Failed " + url + ": " + e.getMessage());
                }
            });
        }
        return result;
    }

    @RequestMapping("/blocking/{count}")
    public String blocking(@PathVariable("count") final int count) {
        LOG.info("received recursive blocking: {}", count);
        if (count == 0) {
            return "0";
        } else {
            try {
                String response = sendNextRequest("http://localhost:8080/recursive/blocking/" + (count - 1)).get();
                LOG.info("received recursive blocking: {}", count);
                return response;
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Blocking, exception thrown", e);
                return "Exception, " + count + ": " + e.getMessage();
            }
        }
    }

    private CompletableFuture<String> sendNextRequest(String url) {

        CompletableFuture<String> future = new CompletableFuture<>();

        httpAsyncClient.execute(new HttpGet(url), new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse httpResponse) {
                try {
                    future.complete(IOUtils.toString(httpResponse.getEntity().getContent(), Charset.defaultCharset()));
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void failed(Exception e) {
                future.completeExceptionally(e);
            }

            @Override
            public void cancelled() {
                future.completeExceptionally(new Exception("Cancellation"));
            }
        });

        return future;
    }

}
