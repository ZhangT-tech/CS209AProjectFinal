package com.example.springproject.web;

//import com.example.springproject.crawl.OpenIssueInfo;
import com.example.springproject.crawl.Druid;
import com.example.springproject.domain.Repo;
import com.example.springproject.service.RepoService;
import com.example.springproject.service.RepoServiceImpl;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;


@RestController
@RequestMapping("/repo")
public class RepoController {

    /**
     * 读取json文件，返回json串
     * @param fileName
     * @return
     */
    public static String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);

            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8);
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }

            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Autowired
    private RepoService repoService;

    @GetMapping("/getInfo1")
    public Repo getInfo_1(){
        repoService=new RepoServiceImpl();
        return repoService.findInfo();
    }
    @GetMapping("/getInfo4")
    public String get_info3(){
        return "666";
    }

    @GetMapping("/getInfo2")
    public ArrayList<String> getInfo_2() throws IOException {

        ArrayList<String> arrayList = new ArrayList<>();

        String json0 = "",json1="";

        try {
            BufferedReader in = new BufferedReader(new FileReader("src/temp.json"));
            String str;
            while ((str = in.readLine()) != null) {
                System.out.println(str);
                json0 = json0.concat(str);
            }

            in=new BufferedReader(new FileReader("src/MostDeveloper.json"));
            while ((str=in.readLine())!=null){
//            for(int i=1;i<=21;++i){
//                str=in.readLine();
                System.out.println(str);
                json1=json1.concat(str);
            }
//            str.concat("}]");

        } catch (IOException e) {
            System.out.println(e);
        }

        Object document = Configuration.defaultConfiguration().jsonProvider().parse(json0);

        arrayList.add(""+JsonPath.read(document, "$.repo"));
        arrayList.add(""+JsonPath.read(document, "$.developers"));
        arrayList.add(""+JsonPath.read(document, "$.most_active_developer.login"));
        arrayList.add(""+JsonPath.read(document, "$.open_issues"));
        arrayList.add(""+JsonPath.read(document, "$.close_issues"));


        Object document1 = Configuration.defaultConfiguration().jsonProvider().parse(json1);
        arrayList.add(""+JsonPath.read(document1, "$.id"));
        arrayList.add(""+JsonPath.read(document1, "$.login"));
        arrayList.add(""+JsonPath.read(document1, "$.contributions"));
        arrayList.add(""+JsonPath.read(document1, "$.avatar_url"));
        arrayList.add(""+JsonPath.read(document1, "$.html_url"));

        System.out.println(Arrays.toString(arrayList.toArray()));

        return arrayList;
    }

    @GetMapping("/getInfo3")
    public ArrayList<String>[] getInfo_3() throws SQLException, IOException, ParseException {
        ArrayList<String>[] arrayList=new ArrayList[10];
        arrayList[0]=new ArrayList<>();//Contributors Num;openIssue num;closedIssue num;Commits num;
                                        //均值，极值，方差
        arrayList[1]=new ArrayList<>();//Contributors Info
        arrayList[2]=new ArrayList<>();//[2][0]-Releases-Num; Releases
        arrayList[3]=new ArrayList<>();//Commits between Releases
        arrayList[4]=new ArrayList<>();//Time distribution of the number of submissions

        Connection connection = Druid.getConnection();
        Statement statement=connection.createStatement();
        String sql="select count(id) from contributor;";
        ResultSet result=statement.executeQuery(sql);
        result.next();
        arrayList[0].add(result.getString(1));

        sql="select count(number) from openissue;";
        result=statement.executeQuery(sql);
        result.next();
        arrayList[0].add(result.getString(1));

        sql="select count(number) from closedissue;";
        result=statement.executeQuery(sql);
        result.next();
        arrayList[0].add(result.getString(1));

        sql="select count(message) from commits;";
        result=statement.executeQuery(sql);
        result.next();
        arrayList[0].add(result.getString(1));

        sql="select 24*sum(datediff(updated_at,created_at))/count(number) from closedissue;";//平均值,单位：小时
        result=statement.executeQuery(sql);
        result.next();
        arrayList[0].add(result.getString(1));
        sql="select max(ok)-min(ok) from (select datediff(updated_at,created_at)ok from closedissue)nb;";//极值，单位：天
        result=statement.executeQuery(sql);
        result.next();
        arrayList[0].add(result.getString(1));
        sql="select sum(pow((datediff(updated_at,created_at)-161.4),2))/count(number) from closedissue;";//方差，单位：天
        result=statement.executeQuery(sql);
        result.next();
        arrayList[0].add(result.getString(1));

        sql="select count(assets_url) from releases;";
        result=statement.executeQuery(sql);
        result.next();
        arrayList[2].add(result.getString(1));


        sql="select * from contributor limit 10;";
        result=statement.executeQuery(sql);
//        while (result.next()){
        for(int i=1;i<=10;++i){
            result.next();
            String id=result.getString(1);
            String name=result.getString(2);
            String contributions=result.getString(3);
            String avatar_url=result.getString(4);
            String html_url=result.getString(5);

            arrayList[1].add(id);
            arrayList[1].add(name);
            arrayList[1].add(contributions);
            arrayList[1].add(avatar_url);
            arrayList[1].add(html_url);
        }


        sql="select * from releases limit 15;";
        result=statement.executeQuery(sql);
//        while (result.next()){
        for(int i=1;i<=15;++i){
            result.next();
            String assets_url=result.getString(1);
            String created_at=result.getString(2);
            String published_at=result.getString(3);
            String name=result.getString(4);

            arrayList[2].add(assets_url);//[2][] start form 1
            arrayList[2].add(created_at);
            arrayList[2].add(published_at);
            arrayList[2].add(name);
        }

//        while (result.next()){
        for(int i=2;i<=15;++i){
            result.next();
            String  upTime= arrayList[2].get(4*(i-2)+3);
            String downTime=arrayList[2].get(4*(i-1)+3);

            sql="select count(message) from commits where post_date " +
                    "between str_to_date('"+downTime+"', '%Y-%m-%d %H') and str_to_date('"+upTime+"', '%Y-%m-%d %H');";

            System.out.println(downTime);
            System.out.println(upTime);
            System.out.println(sql);

            result=statement.executeQuery(sql);
            result.next();
            int count=result.getInt(1);
            arrayList[3].add(String.valueOf(count));
        }

        sql="select count(message) from commits where post_date between str_to_date('2017-01-01','%Y-%m-%d %H') and str_to_date('2018-01-01','%Y-%m-%d %H');";
        result=statement.executeQuery(sql);
        result.next();
        arrayList[4].add(result.getString(1));
        sql="select count(message) from commits where post_date between str_to_date('2018-01-01','%Y-%m-%d %H') and str_to_date('2019-01-01','%Y-%m-%d %H');";
        result=statement.executeQuery(sql);
        result.next();
        arrayList[4].add(result.getString(1));
        sql="select count(message) from commits where post_date between str_to_date('2019-01-01','%Y-%m-%d %H') and str_to_date('2020-01-01','%Y-%m-%d %H');";
        result=statement.executeQuery(sql);
        result.next();
        arrayList[4].add(result.getString(1));
        sql="select count(message) from commits where post_date between str_to_date('2020-01-01','%Y-%m-%d %H') and str_to_date('2021-01-01','%Y-%m-%d %H');";
        result=statement.executeQuery(sql);
        result.next();
        arrayList[4].add(result.getString(1));



        result.close();
        statement.close();
        Druid.closeAll(connection);
        return arrayList;
    }

}
