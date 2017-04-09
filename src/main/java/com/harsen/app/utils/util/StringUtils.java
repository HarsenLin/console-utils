package com.harsen.app.utils.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符处理工具
 * Created by HarsenLin on 2016/6/14.
 */
public class StringUtils {
    /**
     * 根据变量替换字符串
     * @param value 值(abc-${val1}-${val2}.txt)
     * @param valMap 变量集
     * @return String
     */
    public static String replace(String value, Map<String, String> valMap){
        return replace(value, valMap, Pattern.compile("(\\$\\{)([a-zA-Z0-9_\\.]*)(\\})"));
    }

    /**
     * 根据变量替换字符串
     * @param value 值(abc-${val1}-${val2}.txt)
     * @param valMap 变量集
     * @param pattern 变量配置正则：Pattern.compile("(\\$\\{)([a-zA-Z0-9_\\.]*)(\\})")
     * @return String
     */
    public static String replace(String value, Map<String, String> valMap, Pattern pattern){
        Matcher matcher = pattern.matcher(value);
        StringBuffer sb = new StringBuffer();
        String tempVal;
        while(matcher.find() && matcher.groupCount() == 3) {
            tempVal = valMap.get(matcher.group(2));
            // 存在变量则替换，不存在则跳过保持原值
            if (null != tempVal){
                matcher.appendReplacement(sb, tempVal);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
