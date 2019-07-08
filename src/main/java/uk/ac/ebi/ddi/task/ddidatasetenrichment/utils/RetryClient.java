package uk.ac.ebi.ddi.task.ddidatasetenrichment.utils;

import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.backoff.ExponentialRandomBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Collections;

public class RetryClient {

    private static final int RETRIES = 5;
    private RetryTemplate retryTemplate = new RetryTemplate();

    public RetryClient() {
        SimpleRetryPolicy policy =
                new SimpleRetryPolicy(RETRIES, Collections.singletonMap(Exception.class, true));
        retryTemplate.setRetryPolicy(policy);
        ExponentialRandomBackOffPolicy backOffPolicy = new ExponentialRandomBackOffPolicy();
        backOffPolicy.setInitialInterval(2000);
        backOffPolicy.setMultiplier(2.0);
        retryTemplate.setBackOffPolicy(backOffPolicy);
    }

    protected RetryTemplate getRetryTemplate() {
        return retryTemplate;
    }

    private <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback, int count) throws E {
        try {
            return retryTemplate.execute(retryCallback);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS && count > 0) {
                return execute(retryCallback, count - 1);
            }
            throw e;
        } catch (ResourceAccessException e) {
            if (count > 0) {
                return execute(retryCallback, count - 1);
            }
            throw e;
        }
    }

    public final <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback) throws E {
        return execute(retryCallback, RETRIES);
    }
}

