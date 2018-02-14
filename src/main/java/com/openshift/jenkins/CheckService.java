package com.openshift.jenkins;

import org.springframework.stereotype.Component;

@Component
public class CheckService {


    public String check() {
        return "Yeah, This service is deployed & it is running...";
    }
}
