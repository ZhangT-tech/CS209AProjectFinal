package com.example.springproject.crawl;
import com.example.springproject.domain.Contributor;
import org.apache.ibatis.jdbc.SQL;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CommitsInfo2 {
    static int count = 0;
    public  static Connection connection = null;
    public static Lock lock = new ReentrantLock();
    public static PreparedStatement commits_statement = null;

    static List<String> message = new ArrayList<>();
    static List<String> date = new ArrayList<>();

    public static void openDB(String database) throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            System.err.println("Can't find Mysql Driver, please check the path");
            System.exit(1);
        }
        try {
            connection = Druid.getConnection();
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }

        try {
            commits_statement = connection.prepareStatement("insert ignore into commits2 values(?,?)");
        } catch (SQLException e) {
            System.err.println("Insert Statement Fail");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }
    }

    public static void closeDB() throws SQLException{
        if (connection != null) {
            if (commits_statement != null)
                commits_statement.close();
            Druid.closeAll(connection);
        }
    }

    public static void main(String[] args) throws IOException, ParseException, SQLException, java.text.ParseException {
        lock.lock();
        try {
            openDB("repo");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        readByURL("https://api.github.com/repos/ShiqiYu/libfacedetection/commits?page=1&per_page=100");
        readByURL("https://api.github.com/repos/ShiqiYu/libfacedetection/commits?page=2&per_page=100");
        readByURL("https://api.github.com/repos/ShiqiYu/libfacedetection/commits?page=3&per_page=100");
        readByURL("https://api.github.com/repos/ShiqiYu/libfacedetection/commits?page=4&per_page=100");
        readByURL("https://api.github.com/repos/ShiqiYu/libfacedetection/commits?page=5&per_page=100");
        readByURL("https://api.github.com/repos/ShiqiYu/libfacedetection/commits?page=6&per_page=100");

        System.out.println("Total Open Issue Amount is: " + count);
        System.out.println("Total Message list is: " + message);
        System.out.println("Total Date list is: " + date);

        commit();
        closeDB();
        lock.unlock();

    }



    public static void readByURL(String url) throws IOException, ParseException, SQLException, java.text.ParseException {
        URL u = new URL(url);
        URLConnection connection = u.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;


        int code = httpURLConnection.getResponseCode();
        if (code != 200)
            return;

        InputStream inputStream = httpURLConnection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null)
            stringBuilder.append(line);

        bufferedReader.close();
        JSONArray jsonArray = (JSONArray) new JSONParser().parse(stringBuilder.toString());

        for (int i = 0; i  < jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            int order = i + 1;
            System.out.println("=================" + order + "th ? ==============");


            JSONObject commits = (JSONObject) jsonObject.get("commit");
            System.out.println(commits.get("message"));
            message.add((String) commits.get("message"));
            String message = (String) commits.get("message");

            JSONObject committer = (JSONObject) commits.get("committer");
            System.out.println(committer.get("date"));
            java.sql.Date post_date_add = new java.sql.Date(com.example.springproject.common.DateUtil.StrConvertDate((String) committer.get("date")).getTime());
            date.add((String) committer.get("date"));

            if (connection != null) {
                commits_statement.setString(1, message);
                commits_statement.setDate(2, post_date_add);
                commits_statement.addBatch();
            }
            commits_statement.executeBatch();


            count++;
        }
    }

    public static void commit() throws SQLException {
        try {
            connection.commit();
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
        } try {
            connection.rollback();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        closeDB();
        System.exit(1);
    }

    public int getCount() {
        return count;
    }

    public List<String> getMessage() {
        return message;
    }

    public List<String> getDate() {
        return date;
    }

}
