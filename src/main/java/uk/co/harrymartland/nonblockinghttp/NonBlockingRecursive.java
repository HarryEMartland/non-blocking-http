package uk.co.harrymartland.nonblockinghttp;

import java.io.IOException;
import java.nio.charset.Charset;
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
public class NonBlockingRecursive {

    private static final Logger LOG = LoggerFactory.getLogger(NonBlockingRecursive.class);

    @Autowired
    private HttpAsyncClient httpAsyncClient;

    @RequestMapping("{count}")
    public DeferredResult<String> recurse(@PathVariable("count") final int count, final DeferredResult<String> result) {
        if (count == 0) {
            result.setResult("0");
        } else {
            sendNextRequest(count, result);
        }
        return result;
    }

    private void sendNextRequest(@PathVariable("count") final int count, final DeferredResult<String> result) {
        httpAsyncClient.execute(new HttpGet("http://localhost:8080/recursive/"+(count-1)), new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse httpResponse) {
                try {
                    result.setResult(IOUtils.toString(httpResponse.getEntity().getContent(), Charset.defaultCharset()));
                } catch (IOException e) {
                    LOG.error("Error retrieving http entity",e);
                    result.setResult("Exception, "+count+": "+e.getMessage());
                }
            }

            @Override
            public void failed(Exception e) {
                result.setResult("failed " + count + ": " +e.getMessage());
            }

            @Override
            public void cancelled() {
                result.setResult("cancelled: " + count);
            }
        });
    }

}
