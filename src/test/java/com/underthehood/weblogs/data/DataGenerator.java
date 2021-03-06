/* ============================================================================
*
* FILE: DataGenerator.java
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
package com.underthehood.weblogs.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.NavigableSet;
import java.util.Random;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.underthehood.weblogs.Application;
import com.underthehood.weblogs.dao.LogEventIngestionDAO;
import com.underthehood.weblogs.dao.LogEventRepository;
import com.underthehood.weblogs.domain.LogEvent;
import com.underthehood.weblogs.dto.LogRequest;
import com.underthehood.weblogs.service.ILoggingService;
import com.underthehood.weblogs.utils.TimeuuidGenerator;

import ch.qos.logback.core.helpers.ThrowableToStringArray;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class DataGenerator {

  final String LOG_PREFIX = "This is a log prefix with offset- ";
  @Autowired
  private LogEventIngestionDAO logDao;
  @Autowired
  private LogEventRepository repo;
  @Autowired
  private ILoggingService logService;
  
  private LogEvent event;
  private int logsPerPeriod = 10;
  private int logPeriod = 10;
  private String appId = "7DAYSAPP";
  @Test
  public void dummy()
  {
    
  }
  
  //@After
  public void delete()
  {
    if(event != null)
    {
      try {
        repo.delete(event);
      } catch (Exception e) {
        Assert.fail(e.getMessage());
      }
    }
  }
  
  private static void mRaiseEx(int i) throws Exception
  {
    if(i == 0)
      throw new Exception("This is a deep exception stack trace!");
    mRaiseEx(--i);
  }
  final String EXEC_START = "EXEC_START:";
  final String EXEC_END = "EXEC_END:";
  
  @Test
  public void generateExecutionTimedData()
  {
    List<LogRequest> requests = new ArrayList<>();
    
    final long execStartTime = System.currentTimeMillis();
    final long execDur = 5;
    LogRequest request = null;
    for(int i=0; i<logsPerPeriod; i++)
    {
      request = new LogRequest();
      request.setLogText(EXEC_START+" This is some bla blaah bla logging at info level");
      request.setExecutionId(i+"");
      request.setApplicationId(appId);
      request.setTimestamp(execStartTime+i);
      requests.add(request);
      
    }
       
    try 
    {
      logService.ingestLoggingRequests(requests);
      Thread.sleep(1000);
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    
    requests.clear();
    
    final long execEndTime = request.getTimestamp();
    
    for(int i=0; i<logsPerPeriod; i++)
    {
      request = new LogRequest();
      request.setLogText(EXEC_END+" This is some bla blaah bla logging at info level");
      request.setExecutionId(i+"");
      request.setApplicationId(appId);
      request.setTimestamp(execEndTime + (i*execDur));
      requests.add(request);
    }
    
    try 
    {
      logService.ingestLoggingRequests(requests);
      Thread.sleep(1000);
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }
  //@Test
  public void generateXDaysData()
  {
    
    List<LogEvent> entities = new ArrayList<>();
    Calendar date = new GregorianCalendar();
    Random r = new Random();
    try 
    {
      for (int i = 0; i < logPeriod; i++) {
        date.add(Calendar.DATE, -1);
        date.set(Calendar.HOUR_OF_DAY, 0);
        int ec = r.nextInt(logsPerPeriod);
        
        System.out.println("----- DataGenerator.generateXDaysData(): "+" Day: " + date.get(Calendar.DATE) + " Errors: "+ec + " -----");
        
        for (int j = 0; j < logsPerPeriod; j++) {
          date.add(Calendar.HOUR_OF_DAY, 1);
          event = new LogEvent();
          event.setLogText(LOG_PREFIX + " Day: " + date.get(Calendar.DATE)
              + " Hour: " + date.get(Calendar.HOUR_OF_DAY));
          event.getId().setAppId(appId);
          event.getId().setTimestamp(TimeuuidGenerator.minTimeUUID(date.getTimeInMillis()));
          if (j < ec) 
          {
            event.setLevel("ERROR");
            try {
              mRaiseEx(10);
            } catch (Exception e) {
              StringBuilder s = new StringBuilder(
                  LOG_PREFIX + " Day: " + date.get(Calendar.DATE) + " Hour: "
                      + date.get(Calendar.HOUR_OF_DAY));
              for (String str : ThrowableToStringArray.convert(e)) {
                s.append(str).append("\n");
              }
              event.setLogText(s.toString());
            }
          }

          entities.add(event);
        }

      }

      logDao.ingestEntitiesAsync(entities);

    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }
}
