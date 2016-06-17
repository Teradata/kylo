package com.thinkbiganalytics.feedmgr.rest.support;

import com.google.common.base.CaseFormat;

import org.apache.commons.lang3.StringUtils;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 * Convert a Name into a System name replacing control chars, lowercase, spaces to underscore, CamelCase to underscore
 * Examples:
 *  MYFeedName  = my_feed_name
 *  myFeedName = my_feed_name
 *  MY_TESTFeedName = my_test_feed_name
 */
public class SystemNamingService {

  public static String generateSystemName(String name){
    String[] controlChars = {"\"","'","!","@","#","$","%","^","&","*","(",")"};
    //first trim it
    String systemName = name.trim().replaceAll(" +", "_");
    int i = 0;

    StringBuilder s = new StringBuilder();
    CharacterIterator itr = new StringCharacterIterator(systemName);
    for (char c = itr.first(); c != CharacterIterator.DONE; c = itr.next()) {
      if (Character.isUpperCase(c)) {
        //if it is upper, not at the start and not at the end check to see if i am surrounded by upper then lower it.
        if (i > 0 && i != systemName.length() - 1) {
          char prevChar = systemName.charAt(i - 1);
          char nextChar = systemName.charAt(i + 1);

          if (Character.isUpperCase(prevChar) && (Character.isUpperCase(nextChar) || '_' == nextChar || '-' == nextChar)) {
            char lowerChar = Character.toLowerCase(systemName.charAt(i));
            s.append(lowerChar);
          } else {
            s.append(c);
          }
        } else if (i > 0 && i == systemName.length() - 1) {
          char prevChar = systemName.charAt(i - 1);
          if (Character.isUpperCase(prevChar)) {
            char lowerChar = Character.toLowerCase(systemName.charAt(i));
            s.append(lowerChar);
          } else {
            s.append(c);
          }
        } else {
          s.append(c);
        }
      } else {
        s.append(c);
      }

      i++;
    }

    systemName = s.toString();
    systemName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, systemName);
    systemName = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_UNDERSCORE, systemName);
    systemName = StringUtils.replace(systemName, "__", "_");
    for(String controlChar: controlChars){
      systemName = StringUtils.remove(systemName, controlChar);
    }
    return systemName;
  }

}
