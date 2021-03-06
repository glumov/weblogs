/* ============================================================================
*
* FILE: DashboardController.java
*
* MODULE DESCRIPTION:
* See class description
*
* Copyright (C) 2015 by
* 
*
* The program may be used and/or copied only with the written
* permission from  or in accordance with
* the terms and conditions stipulated in the agreement/contract
* under which the program has been supplied.
*
* All rights reserved
*
* ============================================================================
*/
package com.underthehood.weblogs.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.underthehood.weblogs.dto.LogEventDTO;
import com.underthehood.weblogs.dto.QueryRequest;
import com.underthehood.weblogs.dto.QueryResponse;
import com.underthehood.weblogs.dto.SliceQueryRequest;
import com.underthehood.weblogs.dto.viz.ChartJSDataset;
import com.underthehood.weblogs.dto.viz.ChartJSResponse;
import com.underthehood.weblogs.service.ILogAggregationService;
import com.underthehood.weblogs.service.ILoggingService;
import com.underthehood.weblogs.service.ServiceException;
import com.underthehood.weblogs.utils.CommonHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class DashboardController {

  @Autowired
  private ILogAggregationService logMetrics;
  @Autowired
  private ILoggingService logService;
  
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
    SimpleDateFormat sdf = new SimpleDateFormat(CommonHelper.DATE_PICKER_FORMAT);
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
        if(StringUtils.hasText(searchTerm))
        {
          html = CommonHelper.highlightMatchedTerm(html, searchTerm, true);
        }
        dto.setLogText(html);
        dto.setTimestampText(format.format(dto.getTimestamp()));
      }
      
    } catch (ServiceException e) {
      log.error("Service exception ["+e.getMessage()+"]");
      log.debug("", e);
    }
    catch (Exception e) {
      log.error("", e);
      throw e;
    }

    log.debug("qr.getFirstRowUUID(): "+qr.getFirstRowUUID());
    log.debug("qr.getLastRowUUID(): "+qr.getLastRowUUID());
    return qr;
  }
  
  @RequestMapping(value = "/exectrend")
  public @ResponseBody ChartJSResponse fetchExecTimingTrends(
      @RequestParam(value = "p_appid") String appId,
      @RequestParam(value = "p_level", defaultValue = "INFO") String level,
      @RequestParam(value = "p_dtrange") String dateRange,
      @RequestParam(value = "p_term_1") String searchTerm,
      @RequestParam(value = "p_term_2") String searchTermEnd,
      Model model) {

   
    Map<String, Map<Date, Long>> qr;
    ChartJSResponse resp = new ChartJSResponse();
    SliceQueryRequest req = new SliceQueryRequest();
    req.setAppId(appId);
    req.setSearchTerm(searchTerm);
    req.setFetchSize(50);
    req.setLevel(level);
    req.setSearchTermEnd(searchTermEnd);
    
    // 11/19/2015 - 11/19/2015
    SimpleDateFormat sdf = new SimpleDateFormat(CommonHelper.DATE_PICKER_FORMAT);
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
      resp.setError("Internal server error!");
    }
    log.debug("Got request: " + req);

    try 
    {
      sdf.applyPattern(CommonHelper.LOG_EXEC_TIMING_FORMAT);
      qr = logMetrics.countExecutionTimings(req);
      
      if (qr != null && !qr.isEmpty()) 
      {
        TreeMap<Date, Long> ordered = new TreeMap<>();
        for (Map<Date, Long> e : qr.values()) {
          ordered.putAll(e);
        }
        for (Entry<Date, Long> e : ordered.entrySet()) {
          resp.getLabels().add(sdf.format(e.getKey()));
        }
        Collection<String> values = Collections2.transform(ordered.values(),
            new Function<Long, String>() {

              @Override
              public String apply(Long input) {
                return String.valueOf(input);
              }
            });
        resp.getDatasets().add(new ChartJSDataset(values));
      }
      
    } catch (ServiceException e) {
      log.error("Service exception ["+e.getMessage()+"]");
      log.debug("", e);
      resp.setError(e.getMessage());
    }
    catch (Exception e) {
      log.error("", e);
      throw e;
    }

    return resp;
  }
  
  @RequestMapping(value = "/logtrend")
  public @ResponseBody ChartJSResponse fetchLogTrends(
      @RequestParam(value = "p_appid") String appId,
      @RequestParam(value = "p_level", required = false) String level,
      @RequestParam(value = "p_dtrange") String dateRange,
      @RequestParam(value = "p_term", required = false) String searchTerm,
      @RequestParam(value = "p_freq", defaultValue = CommonHelper.LOG_TREND_HOURLY) String freq,
      Model model) {

   
    Map<String, Long> qr;
    ChartJSResponse resp = new ChartJSResponse();
    QueryRequest req = new QueryRequest();
    req.setAppId(appId);
    req.setSearchTerm(searchTerm);
    req.setFetchSize(500);
    req.setLevel(level);
    
    // 11/19/2015 - 11/19/2015
    SimpleDateFormat sdf = new SimpleDateFormat(CommonHelper.DATE_PICKER_FORMAT);
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
      resp.setError("Internal server error!");
    }
    log.debug("Got request: " + req);

    try 
    {
      if(CommonHelper.LOG_TREND_DAILY.equals(freq))
        qr = logMetrics.countDailyLogsByLevel(req);
      else if(CommonHelper.LOG_TREND_HOURLY.equals(freq))
        qr = logMetrics.countHourlyLogsByLevel(req);
      else
        throw new ServiceException("Invalid frequency specified- "+freq);
      
      resp.getLabels().addAll(qr.keySet());
            
      Collection<String> values = Collections2.transform(qr.values(), new Function<Long, String>() {

        @Override
        public String apply(Long input) {
          return String.valueOf(input);
        }
      });
      
      resp.getDatasets().add(new ChartJSDataset(values));
      
    } catch (ServiceException e) {
      log.error("Service exception ["+e.getMessage()+"]");
      log.debug("", e);
      resp.setError(e.getMessage());
    }
    catch (Exception e) {
      log.error("", e);
      throw e;
    }

    return resp;
  }
}
