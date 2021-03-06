/* ============================================================================
*
* FILE: LogEventDTO.java
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

import java.util.Date;

import com.underthehood.weblogs.domain.LogEvent;

import lombok.Data;

@Data
public class LogEventDTO {

  public LogEventDTO(LogEvent domain) {

    this(domain.getId().getAppId(), 
        domain.getLogText(),
        domain.getId().getTimestampAsLong());
    setLevel(domain.getLevel());
    setExecId(domain.getExecId());
  }

  public LogEventDTO() {
    super();
  }

  public LogEventDTO(String applicationId, String logText,
      Date timestamp) {
    super();
    this.applicationId = applicationId;
    this.logText = logText;
    this.timestamp = timestamp;
  }

  public LogEventDTO(String appId, String logText2, long unixTimestamp) {
    this(appId, logText2, new Date(unixTimestamp));
  }
  
  private String level;
  private String applicationId, logText;
  private Date timestamp;
  private String timestampText;
  private String execId;
}
