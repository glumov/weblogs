/* ============================================================================
*
* FILE: DashboardController.java
*
* MODULE DESCRIPTION:
* See class description
*
* Copyright (C) 2015 by
* ERICSSON
*
* The program may be used and/or copied only with the written
* permission from Ericsson Inc, or in accordance with
* the terms and conditions stipulated in the agreement/contract
* under which the program has been supplied.
*
* All rights reserved
*
* ============================================================================
*/
package com.ericsson.weblogs.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import com.ericsson.weblogs.dto.LogEventDTO;
import com.ericsson.weblogs.dto.QueryRequest;
import com.ericsson.weblogs.dto.QueryResponse;
import com.ericsson.weblogs.service.ILoggingService;
import com.ericsson.weblogs.service.ServiceException;
import com.ericsson.weblogs.utils.CommonHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class DashboardController {

  @Autowired
  private ILoggingService logService;
  static final String DATE_PICKER_FORMAT = "MM/dd/yyyy";

  @RequestMapping(value = "/logsearch")
  public @ResponseBody QueryResponse fetchLogs(@RequestParam int start,//from datatables
      @RequestParam int length,//from datatables
      @RequestParam(value = "p_appid") String appId,
      @RequestParam(value = "p_level", required = false) String level,
      @RequestParam(value = "p_dtrange") String dateRange,
      @RequestParam(value = "p_term", required = false) String searchTerm,
      @RequestParam(value = "p_frowid", required = false) String firstRow,
      @RequestParam(value = "p_lrowid", required = false) String lastRow,
      @RequestParam(value = "p_prev", required = false) boolean isPrev,
      @RequestParam(value = "p_refresh") boolean autoRefresh, Model model) {

    int pageIdx = start/length;
    log.debug("pageIdx: "+pageIdx);
    int pageSize = length;
    
    QueryResponse qr = new QueryResponse();
    QueryRequest req = new QueryRequest();
    req.setAppId(appId);
    req.setSearchTerm(searchTerm);
    req.setFetchSize(pageSize);
    req.setFetchMarkUUID(isPrev ? firstRow : lastRow);
    req.setFetchPrev(isPrev);
    req.setLevel(level);
    
    // 11/19/2015 - 11/19/2015
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_PICKER_FORMAT);
    StringTokenizer st = new StringTokenizer(dateRange);
    try 
    {
      if(st.hasMoreTokens())
        req.setFromDate(sdf.parse(st.nextToken()));
      if(st.hasMoreTokens())
        st.nextToken();
      if(st.hasMoreTokens())
        req.setTillDate(sdf.parse(st.nextToken()));
      
    } catch (ParseException e) {
      log.error("Date parsing error ", e);
      qr.setError("Internal server error!");
    }
    log.debug("Got request: " + req);

    try 
    {
      qr = logService.fetchLogsBetweenDates(req);
      SimpleDateFormat format = new SimpleDateFormat(CommonHelper.ISO8601_TS_MILLIS);
      format.setTimeZone(TimeZone.getDefault());
      for(LogEventDTO dto : qr.getLogs())
      {
        String html = HtmlUtils.htmlEscape(dto.getLogText());
        html = html.replaceAll("(\r\n|\n)", "<br />");
        html = html.replaceAll("(\t)", "&nbsp;&nbsp;&nbsp;&nbsp;");
        dto.setLogText(html);
        dto.setTimestampText(format.format(dto.getTimestamp()));
      }
      
    } catch (ServiceException e) {
      log.error("", e);
      qr.setError(e.getMessage());
    }

    log.debug("qr.getFirstRowUUID(): "+qr.getFirstRowUUID());
    log.debug("qr.getLastRowUUID(): "+qr.getLastRowUUID());
    return qr;
  }
}
