/* ============================================================================
*
* FILE: LogRequest.java
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
package com.underthehood.weblogs.dto;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.cassandra.repository.MapId;
import org.springframework.data.cassandra.repository.support.BasicMapId;

import com.underthehood.weblogs.utils.CommonHelper;

import lombok.Data;

@Data
public class LogRequest implements Serializable{

  /**
   * 
   */
  private static final long serialVersionUID = -251827285602328542L;
  public MapId toMapId()
  {
     return new BasicMapId()
         .with("appId", getApplicationId())
         .with("bucket", new SimpleDateFormat(CommonHelper.TIMEBUCKET_DATEFORMAT).format(new Date(getTimestamp())));
   }
  
  public LogRequest() {
    super();
  }
  public LogRequest(String applicationId, String logText) {
    super();
    this.applicationId = applicationId;
    this.logText = logText;
  }
  @NotEmpty
  private String applicationId;//not null
  private String level = "INFO";
  private String logText;
  private long timestamp = -1;
  private String executionId;
}
