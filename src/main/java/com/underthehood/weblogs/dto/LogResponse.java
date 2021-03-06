/* ============================================================================
*
* FILE: LogResponse.java
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

import lombok.Data;

@Data
public class LogResponse implements Serializable{
  /**
   * 
   */
  private static final long serialVersionUID = 964397647213340381L;
  LogIngestionStatus status;
  String message;
  public LogResponse(LogIngestionStatus status, String message) {
    super();
    this.status = status;
    this.message = message;
  }
  public LogResponse() {
    super();
  }
}
