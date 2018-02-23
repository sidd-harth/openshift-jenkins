package com.openshift.jenkins;

import org.springframework.stereotype.Component;

@Component
public class CheckService {

String response = "Yeah, This service is deployed & it is running!!!!!!!!!!!!!!";
    public String check() {
        return response;
    }
}
