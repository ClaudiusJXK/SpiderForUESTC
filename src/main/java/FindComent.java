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

/**
 * Created by Claudius on 2017/5/28.
 */
public class FindComent {
    private CloseableHttpClient httpClient;
    private String url;

    public FindComent(CloseableHttpClient httpClient, String url) {
        this.httpClient = httpClient;
        this.url = url;
    }

    public void findByTid(int tid) {
        HttpGet httpGet = new HttpGet(url + tid);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            String response = EntityUtils.toString(httpEntity);
            Document document = Jsoup.parse(response);
            Elements commentElements = document.getElementsByClass("pstl");
            for (Element element : commentElements) {
                Elements hrefElements = element.getElementsByAttribute("href");
                if(hrefElements.get(0).attr("href").contains("151616"))
                    System.out.println(element.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
