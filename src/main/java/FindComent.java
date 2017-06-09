import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;

/**
 * Created by Claudius on 2017/5/28.
 */
public class FindComent {
    private CloseableHttpClient httpClient;
    private RequestConfig requestConfig;
    private Connection connection;
    private Properties properties;

    private int tid;
    private int uid;

    /**
     * @param httpClient 保存有cookie信息的httpclient
     * @param tid        从tid开始往后查找
     * @param uid        需要查找的uid
     * @throws IOException
     */
    public FindComent(CloseableHttpClient httpClient, int tid, int uid) throws IOException, SQLException {
        this.httpClient = httpClient;
        this.tid = tid;
        this.uid = uid;
        this.requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).build();
        this.connection = getConnection();
    }

    /**
     * 默认构造函数
     */
    public FindComent() {
    }

    /**
     * 加载属性文件，并返回connection
     *
     * @return
     * @throws IOException
     * @throws SQLException
     */
    public Connection getConnection() throws IOException, SQLException {
        this.properties = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get("props.properties"))) {
            properties.load(in);
        }
        String drivers = properties.getProperty("jdbc.drivers");
        //加载mysql driver
        if (drivers != null)
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                System.out.println("找不到驱动程序类 ，加载驱动失败！");
                e.printStackTrace();
            }
        return DriverManager.getConnection(properties.getProperty("databaseUrl"), properties.getProperty("username"),
                properties.getProperty("password"));
    }

    /**
     * 根据帖子tid，找到该主题帖的commentId
     *
     * @param tid
     * @return
     */
    public String findCommentIdByTid(int tid) {
        HttpGet httpGet = new HttpGet(properties.getProperty("url") + tid);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
        httpGet.setConfig(requestConfig);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            int times = 10;
            //若失败，则最多循环10次get
            while (httpResponse.getStatusLine().getStatusCode() != 200 && times > 0) {
                httpResponse.close();
                httpResponse = httpClient.execute(httpGet);
                times--;
            }
            try {
                HttpEntity httpEntity = httpResponse.getEntity();
                String response = EntityUtils.toString(httpEntity);
                Document document = Jsoup.parse(response);
                Elements elements = document.getElementsByClass("authicn");
                if (elements.size() != 0) {
                    Element element = elements.get(0);
                    if (element != null) {
                        String id = element.attr("id");
                        if (id != null)
                            return id.substring(8);
                    }

                }

            } finally {
                httpResponse.close();
            }

        } catch (IOException e) {
            httpGet.releaseConnection();
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 如果有多页点评，则循环
     *
     * @param tid
     */
    public void findLoop(int tid) {
        int page = 1;
        String commentId = findCommentIdByTid(tid);
        if (commentId == null)
            return;
        while (findCommentByUid(commentId, tid, page))
            page++;
        System.out.println(tid);
    }

    /**
     * 如果返回true，则还有下一页点评。
     *
     * @param tid
     * @param page 第几页点评
     * @return
     */
    public boolean findCommentByUid(String commentId, int tid, int page) {
        boolean result = false;
        String commentUrl = properties.getProperty("commentUrlPre") + commentId + properties.getProperty("commentUrlMid") +
                page + properties.getProperty("commentUrlSuf");
        HttpGet httpGet = new HttpGet(commentUrl);
        httpGet.setConfig(requestConfig);
        try {

            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            int times = 10;
            while (httpResponse.getStatusLine().getStatusCode() != 200 && times > 0) {
                httpResponse.close();
                httpResponse = httpClient.execute(httpGet);
                times--;
            }
            try {
                HttpEntity httpEntity = httpResponse.getEntity();
                String response = EntityUtils.toString(httpEntity);
                if (response.contains("下一页"))
                    result = true;
                if (!response.contains("" + uid))
                    return result;
                int length = response.length();
                response = response.substring(55, length - 10);
                Document document = Jsoup.parse(response);
                Elements elements = document.getElementsByClass("psti");
                for (Element element : elements) {
                    if (element.toString().contains(uid + "")) {
                        System.out.print(element.text());
                        insert(tid,element.text());
                    }

                }
            } finally {
                httpResponse.close();
            }

        } catch (IOException e) {
            httpGet.releaseConnection();
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 持久化到数据库中
     * @param tid
     * @param text
     */
    public void insert(int tid ,String text) {
        try {
            PreparedStatement statement = connection.prepareStatement("insert into comment  VALUES  (?,?)");
            try {
                statement.setInt(1,tid);
                statement.setString(2,text);
                statement.executeUpdate();

            }finally {
                statement.close();
            }
        }catch (SQLException e){
            e.printStackTrace();
            System.err.println(tid + text);
        }
    }

    public synchronized int reduce() {
        this.tid--;
        return this.tid + 1;
    }

    public int getTid() {
        return tid;
    }
}
