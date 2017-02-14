package uk.co.harrymartland.nonblockinghttp;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping("/long/poll")
public class LongPoll {

    private static final Logger LOG = LoggerFactory.getLogger(LongPoll.class);

    private List<DeferredResult<String>> blockingResults = new LinkedList<>();

    private CountDownLatch startSignal = new CountDownLatch(1);
    private String blockedResult;

    @RequestMapping("/trigger/{event}")
    public String trigger(@PathVariable("event") String event) {
        LOG.info("Triggering long poll requests");

        for (DeferredResult<String> blockingResult : blockingResults) {
            blockingResult.setResult(event);
        }

        blockedResult = event;
        startSignal.countDown();
        startSignal = new CountDownLatch(1);

        LOG.info("Finished triggering long poll requests");
        return event;
    }

    @RequestMapping("non/blocking")
    public DeferredResult<String> nonBlocking(DeferredResult<String> deferredResult) {
        LOG.info("Registered non blocking request");
        blockingResults.add(deferredResult);
        LOG.info("Returning non blocking request");
        return deferredResult;
    }

    @RequestMapping("blocking")
    public String blocking() throws InterruptedException {
        LOG.info("Registered blocking request");
        startSignal.await();
        LOG.info("Returning blocking request");
        return blockedResult;
    }
}
