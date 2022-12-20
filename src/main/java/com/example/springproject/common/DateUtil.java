package com.example.springproject.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 处理日期相关的操作
 */
public class DateUtil {

    /**
     * 把日期格式转换成年月日(yyyy-MM-dd)的字符串
     */
    public static String dateConvertStr(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    public static Date StrConvertDate(String str) throws ParseException {
        str=str.substring(0,10);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date=sdf.parse(str);
        return date;
    }

    public static float DateDiff(String d1,String d2)throws ParseException{
        d1=d1.replace("T"," ").replace("Z","");
        d2=d2.replace("T"," ").replace("Z","");
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1=s.parse(d1);
        Date date2=s.parse(d2);
        float t = (float)100.0/Math.round(((float)date1.getTime() - (float)date2.getTime())/((float)1000*60*60));
        return t;
    }

    public static void main(String[] args) throws ParseException {
//        String d1="2013-05-17T21:41:56Z";
//        String d2="2013-05-12T21:42:56Z";
//        System.out.println( DateUtil.DateDiff(d1,d2));
        String hh="                .addUrl(\"https://api.github.com/search/repositories?q=language:java+created:\")";
        for(int i=2012;i<=2022;i++){
            for(int j=1;j<=12;j++){
                if(j>=10){
                    System.out.println("                .addUrl(\"https://api.github.com/search/repositories?q=language:java+created:"+i+"-"+j+".."+i+"-"+j+"\")");
                }else {
                    System.out.println("                .addUrl(\"https://api.github.com/search/repositories?q=language:java+created:"+i+"-0"+j+".."+i+"-0"+j+"\")");
                }
            }
        }
    }
}
