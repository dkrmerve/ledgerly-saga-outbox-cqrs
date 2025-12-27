package com.example.ledgerly.infra.payment;

import com.example.ledgerly.infra.payment.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Profile("!test & !stub") // test/stub harici profillerde aktif
public class HttpMasterpassClient implements MasterpassClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String bearerToken;

    public HttpMasterpassClient(
            RestTemplate restTemplate,
            @Value("${ledgerly.masterpass.baseUrl:http://masterpass-mock}") String baseUrl,
            @Value("${ledgerly.masterpass.bearerToken:FIXED_DEMO_TOKEN}") String bearerToken
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.bearerToken = bearerToken;
    }

    @Override
    public MasterpassAuthorizeResponse authorize(MasterpassAuthorizeRequest request) {
        return exchange("/authorize", request, MasterpassAuthorizeResponse.class);
    }

    @Override
    public MasterpassCaptureResponse capture(MasterpassCaptureRequest request) {
        return exchange("/capture", request, MasterpassCaptureResponse.class);
    }

    @Override
    public MasterpassVoidResponse voidAuth(MasterpassVoidRequest request) {
        return exchange("/void", request, MasterpassVoidResponse.class);
    }

    private <T> T exchange(String path, Object body, Class<T> responseType) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setBearerAuth(bearerToken);

        ResponseEntity<T> res = restTemplate.exchange(
                baseUrl + path,
                HttpMethod.POST,
                new HttpEntity<>(body, h),
                responseType
        );

        return res.getBody();
    }
}
