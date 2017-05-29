import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
    private int tid;
    private int uid;

    public FindComent(CloseableHttpClient httpClient, int tid, int uid) throws IOException {
        this.httpClient = httpClient;
        this.url = "http://bbs.uestc.edu.cn/forum.php?mod=viewthread&tid=";
        this.commentUrlPre = "http://bbs.uestc.edu.cn/forum.php?mod=misc&action=commentmore&tid=1&pid=";
        this.commentUrlMid = "&page=";
        this.commentUrlSuf = "&inajax=1&ajaxtarget=comment";
        this.out = new PrintWriter("F:\\masterSpring\\riverside\\data.txt");
        this.tid = tid;
        this.uid = uid;
    }


    //找到当前页面的commentId
    public String findCommentIdByTid(int tid) {
        HttpGet httpGet = new HttpGet(url + tid);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            int times = 10;
            //若失败，则最多循环10次get
            while (httpResponse.getStatusLine().getStatusCode() != 200 && times > 0) {
                httpClient.execute(httpGet);
                times--;
            }
            try {
                HttpEntity httpEntity = httpResponse.getEntity();
                String response = EntityUtils.toString(httpEntity);
                Document document = Jsoup.parse(response);
                Elements elements = document.getElementsByClass("authicn");
                return elements.get(0).attr("id").substring(8);
            } finally {
                httpResponse.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    public void findLoop(int tid) {
        int page = 1;
        while (findCommentByUid( tid ,page))
            page++;
    }

    //根据commentId，进行ajaxget，按点评页数查找数据。
    public boolean findCommentByUid( int tid ,int page ) {
        boolean result = false;
        String commentId = findCommentIdByTid(tid);
        String commentUrl = commentUrlPre + commentId + commentUrlMid + page + commentUrlSuf;
        HttpGet httpGet = new HttpGet(commentUrl);
        try {

            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            int times = 10;
            while (httpResponse.getStatusLine().getStatusCode() != 200 && times > 0) {
                httpClient.execute(httpGet);
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
                    if (element.toString().contains(uid + ""))
                        System.out.print(element.text());
                    System.out.print(element.text());
                    output(url + tid + "   " + element.text());
                }
            } finally {
                httpResponse.close();
            }

        } catch (IOException e) {
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
}
