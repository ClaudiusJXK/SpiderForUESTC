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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Claudius on 2017/5/28.
 */
public class FindComent {
    private CloseableHttpClient httpClient;
    private String url;
    private String commentUrlPre;
    private String commentUrlSuf;
    private String commentUrlMid;
    private PrintWriter out;
    private RequestConfig requestConfig;

    private int tid;
    private int uid;

    /**
     *
     * @param httpClient   保存有cookie信息的httpclient
     * @param tid          从tid开始往后查找
     * @param uid          需要查找的uid
     * @param file         保存的文件
     * @throws IOException
     */
    public FindComent(CloseableHttpClient httpClient, int tid, int uid , String file) throws IOException {
        this.httpClient = httpClient;
        this.url = "http://bbs.uestc.edu.cn/forum.php?mod=viewthread&tid=";
        this.commentUrlPre = "http://bbs.uestc.edu.cn/forum.php?mod=misc&action=commentmore&tid=1&pid=";
        this.commentUrlMid = "&page=";
        this.commentUrlSuf = "&inajax=1&ajaxtarget=comment";
        this.out = new PrintWriter(new BufferedWriter(new FileWriter(file,true)));
        this.tid = tid;
        this.uid = uid;
        this.requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).build();
    }


    /**
     * 根据帖子tid，找到该主题帖的commentId
     * @param tid
     * @return
     */
    public String findCommentIdByTid(int tid) {
        HttpGet httpGet = new HttpGet(url + tid);
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
                if (elements.size() != 0){
                    Element element = elements.get(0);
                    if (element != null) {
                        String id = element.attr("id");
                        if ( id != null)
                            return  id.substring(8);
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
     * @param tid
     */
    public void findLoop(int tid) {
        int page = 1;
        String commentId = findCommentIdByTid(tid);
        if (commentId == null)
            return;
        while (findCommentByUid( commentId,tid ,page))
            page++;
        System.out.println(tid);
    }

    /**
     * 如果返回true，则还有下一页点评。
     * @param tid
     * @param page   第几页点评
     * @return
     */
    public boolean findCommentByUid( String commentId ,int tid ,int page ) {
        boolean result = false;
        String commentUrl = commentUrlPre + commentId + commentUrlMid + page + commentUrlSuf;
        HttpGet httpGet = new HttpGet(commentUrl);
        httpGet.setConfig(requestConfig);
        try {

            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            int times = 10;
            while (httpResponse.getStatusLine().getStatusCode() != 200 && times > 0) {
                httpResponse.close();
                httpResponse =  httpClient.execute(httpGet);
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
                    if (element.toString().contains(uid + "")){
                        System.out.print(element.text());
                        output(url + tid + "   " + element.text());
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

    //使用同一个printer进行存储。
    public synchronized void output(String text) {
        out.println(text);
        out.flush();
    }

    public synchronized int reduce() {
        this.tid--;
        return  this.tid + 1;
    }

    public int getTid(){
        return  tid;
    }

    public void close(){
        out.close();
    }
}
