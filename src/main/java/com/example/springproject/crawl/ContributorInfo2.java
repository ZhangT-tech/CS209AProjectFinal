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

public class ContributorInfo2 {
    static int count = 0;
    public  static Connection connection = null;
    public static Lock lock = new ReentrantLock();
    public static PreparedStatement contributor_statement = null;

    static List<Long> id = new ArrayList<>();
    static List<String> name = new ArrayList<>();
    static List<Long> contributions = new ArrayList<>();
    static List<String> avatar_url = new ArrayList<>();
    static List<String> html_url = new ArrayList<>();

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
            contributor_statement = connection.prepareStatement("insert ignore into contributor2 values(?,?,?,?,?)");
        } catch (SQLException e) {
            System.err.println("Insert Statement Fail");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }
    }

    public static void closeDB() throws SQLException{
        if (connection != null) {
            if (contributor_statement != null)
                contributor_statement.close();
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
        readByURL("https://api.github.com/repos/ShiqiYu/libfacedetection/contributors?page=1&per_page=100");
        System.out.println("Total Open Issue Amount is: " + count);
        System.out.println("Total id list is: " + id);
        System.out.println("Total name list is: " + name);
        System.out.println("Total contributions list is: " + contributions);
        System.out.println("Total avatar_url list is: " + avatar_url);
        System.out.println("Total html_url list is: " + html_url);

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
            System.out.println(jsonObject.get("id"));
            id.add((Long) jsonObject.get("id"));
            Long id = (Long) jsonObject.get("id");
            System.out.println(jsonObject.get("login"));
            name.add((String) jsonObject.get("login"));
            String name = (String) jsonObject.get("login");
            System.out.println(jsonObject.get("contributions"));
            contributions.add((Long) jsonObject.get("contributions"));
            Long contributions = (Long) jsonObject.get("contributions");
            System.out.println(jsonObject.get("avatar_url"));
            avatar_url.add((String) jsonObject.get("avatar_url"));
            String avatar_url = (String) jsonObject.get("avatar_url");
            System.out.println(jsonObject.get("html_url"));
            html_url.add((String) jsonObject.get("html_url"));
            String html_url = (String) jsonObject.get("html_url");
            if (connection != null) {
                contributor_statement.setLong(1, id);
                contributor_statement.setString(2, name);
                contributor_statement.setLong(3, contributions);
                contributor_statement.setString(4, avatar_url);
                contributor_statement.setString(5, html_url);
                contributor_statement.addBatch();
            }
            contributor_statement.executeBatch();


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

    public List<String> getName() {
        return name;
    }

    public List<String> getAvatar_url() {
        return avatar_url;
    }

    public List<String> getHtml_url() {
        return html_url;
    }

    public List<Long> getId() {
        return id;
    }

    public static List<Long> getContributions() {
        return contributions;
    }

}
