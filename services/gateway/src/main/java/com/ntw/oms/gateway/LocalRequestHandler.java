//////////////////////////////////////////////////////////////////////////////
// Copyright 2020 Anurag Yadav (anurag.yadav@newtechways.com)               //
//                                                                          //
// Licensed under the Apache License, Version 2.0 (the "License");          //
// you may not use this file except in compliance with the License.         //
// You may obtain a copy of the License at                                  //
//                                                                          //
//     http://www.apache.org/licenses/LICENSE-2.0                           //
//                                                                          //
// Unless required by applicable law or agreed to in writing, software      //
// distributed under the License is distributed on an "AS IS" BASIS,        //
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. //
// See the License for the specific language governing permissions and      //
// limitations under the License.                                           //
//////////////////////////////////////////////////////////////////////////////

package com.ntw.oms.gateway;

import com.google.gson.Gson;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by anurag on 21/08/19.
 */
@RestController
public class LocalRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(LocalRequestHandler.class);
    private static String HOSTNAME = "UNKNOWN";

    static {
        try {
            HOSTNAME = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.error("Unable to get Hostname");
            logger.error(e.getMessage(),e);
        }
    }

    @GetMapping(path = "/status", produces = MediaType.TEXT_PLAIN_VALUE)
    public static void getServiceStatus() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Calcutta"));
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("serviceId", "GatewaySvc");
        statusMap.put("serviceHost", HOSTNAME);
        statusMap.put("serviceTime", dateFormat.format(cal.getTime()));
        String status = (new Gson()).toJson(statusMap);
        RequestContext requestContext = RequestContext.getCurrentContext();
        try {
            PrintWriter pw = requestContext.getResponse().getWriter();
            pw.println(status);
            requestContext.setResponseStatusCode(HttpStatus.OK.value());
        } catch (IOException e) {
            logger.error("Unable to write system status response");
            logger.error("Exception Message: ", e);
        }
    }

}
