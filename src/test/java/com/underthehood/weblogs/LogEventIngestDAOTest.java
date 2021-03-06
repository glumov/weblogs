/* ============================================================================
*
* FILE: LogEventDAOTest.java
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
package com.underthehood.weblogs;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.cassandra.repository.support.BasicMapId;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.underthehood.weblogs.dao.LogEventIngestionDAO;
import com.underthehood.weblogs.dao.LogEventRepository;
import com.underthehood.weblogs.domain.LogEvent;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class LogEventIngestDAOTest {
  
  @Autowired
  private LogEventRepository repo;
  
  LogEvent event;
  List<LogEvent> requests;
  
  @After
  public void delete()
  {
    
    try 
    {
      if (event != null) {
        repo.delete(new BasicMapId().with("appId", appId).with("bucket",
            event.getId().getBucket()));
      }
      if(requests != null){
        for(LogEvent l : requests)
        {
          repo.delete(new BasicMapId().with("appId", appId).with("bucket", l.getId().getBucket()));
        }
      }
      
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    
  }
  
  final int batchSize = 10;
  final String appId = "applicationId";
  
  @Autowired
  private LogEventIngestionDAO iDao;
  
  @Test
  public void testInsertLogEvents()
  {
    requests = new ArrayList<>(batchSize);
    for(int i=0; i<batchSize; i++)
    {
      event = new LogEvent();
      event.setLogText("This is some bla blaah bla logging at info level");
      event.getId().setAppId(appId);
      
      requests.add(event);
    }
    try {
      iDao.ingestEntitiesAsync(requests);
      
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    
  }
  
   
  
  
  @Test
  public void testInsertLogEvent()
  {
    event = new LogEvent();
    event.setLogText("This is some bla blaah bla logging at info level");
    event.getId().setAppId(appId);
    try {
      iDao.insert(event);
      
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    
  }

}
