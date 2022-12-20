package com.example.springproject.crawl;
import com.example.springproject.domain.Contributor;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
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

public class ReleaseInfo2 {
    static int count = 0;
    public  static Connection connection = null;
    public static Lock lock = new ReentrantLock();
    public static PreparedStatement release_statement = null;

    static List<String> assets_url = new ArrayList<>();
    static List<String> created_at = new ArrayList<>();
    static List<String> published_at = new ArrayList<>();
    static List<String> name = new ArrayList<>();

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
            release_statement = connection.prepareStatement("insert ignore into release2 values(?,?,?,?)");
        } catch (SQLException e) {
            System.err.println("Insert Statement Fail");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }
    }

    public static void closeDB() throws SQLException{
        if (connection != null) {
            if (release_statement != null)
                release_statement.close();
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

        readByURL("https://api.github.com/repos/ShiqiYu/libfacedetection/releases?state=open&page=1&per_page=100");
        System.out.println("Total Open Issue Amount is: " + count);
        System.out.println("Total Assets_url list is: " + assets_url);
        System.out.println("Total created at list is: " + created_at);
        System.out.println("Total published at list is: " + published_at);
        System.out.println("Total name list is: " + name);

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
            System.out.println(jsonObject.get("assets_url"));
            assets_url.add((String) jsonObject.get("assets_url"));
            String assets_url = (String) jsonObject.get("assets_url");
            System.out.println(jsonObject.get("created_at"));
            created_at.add((String) jsonObject.get("created_at"));
            java.sql.Date created_at = new java.sql.Date(com.example.springproject.common.DateUtil.StrConvertDate((String) jsonObject.get("created_at")).getTime());
            System.out.println(jsonObject.get("published_at"));
            published_at.add((String) jsonObject.get("published_at"));
            Date published_at = new java.sql.Date(com.example.springproject.common.DateUtil.StrConvertDate((String) jsonObject.get("published_at")).getTime());
            System.out.println(jsonObject.get("name"));
            name.add((String) jsonObject.get("name"));
            String name = (String) jsonObject.get("name");
            if (connection != null) {
                release_statement.setString(1, assets_url);
                release_statement.setDate(2, created_at);
                release_statement.setDate(3, published_at);
                release_statement.setString(4, name);
                release_statement.addBatch();
            }
            release_statement.executeBatch();


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

    public List<String> getAssets_url() {
        return assets_url;
    }

    public List<String> getCreated_at() {
        return created_at;
    }

    public List<String> getPublished_at() {
        return published_at;
    }

}
