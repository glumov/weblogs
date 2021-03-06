/* ============================================================================
*
* FILE: LoginController.java
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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Controller
public class LoginController {

  @RequestMapping(value = { "/welcome" })
  public String welcome(Model model) {
    String view = "fragments/logsearch";
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    model.addAttribute("username", auth.getName());
    log.info("View - {} \t For user - {}", view, auth.getName());
    return view;
  }
  @RequestMapping(value = { "/", "/signin" })
  public String goToSignOrHome() {
    String view = "index";

    Object principal = SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();

    log.info("View - {} \t For user - {}", view, principal);
    return view;
  }
}
