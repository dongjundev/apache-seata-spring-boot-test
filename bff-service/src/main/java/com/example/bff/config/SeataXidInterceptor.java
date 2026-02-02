package com.example.bff.config;

import org.apache.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class SeataXidInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SeataXidInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                         ClientHttpRequestExecution execution) throws IOException {
        String xid = RootContext.getXID();
        if (xid != null) {
            request.getHeaders().add(RootContext.KEY_XID, xid);
            log.debug("Propagating Seata XID: {} to {}", xid, request.getURI());
        }
        return execution.execute(request, body);
    }
}
