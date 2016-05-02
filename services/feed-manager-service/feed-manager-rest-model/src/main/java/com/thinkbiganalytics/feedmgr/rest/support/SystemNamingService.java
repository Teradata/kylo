package com.thinkbiganalytics.feedmgr.rest.support;

import com.google.common.base.CaseFormat;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by sr186054 on 4/29/16.
 */
public class SystemNamingService {

  public static String generateSystemName(String name){
    String[] controlChars = {"\"","'","!","@","#","$","%","^","&","*","(",")"};
    //lower and remove double spaces
    String  systemName = name.toLowerCase().trim().replaceAll(" +","_");
    systemName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, systemName);
    systemName = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_UNDERSCORE, systemName);
    for(String controlChar: controlChars){
      systemName = StringUtils.remove(systemName, controlChar);
    }
    return systemName;
  }

}
