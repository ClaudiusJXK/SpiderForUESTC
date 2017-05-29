import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Claudius on 2017/5/27.
 */
public class Login {
    private CloseableHttpClient httpClient;

    public static void main(String[] args) throws  IOException {
        Login login = new Login();
        login.post("Claudius", "9876543211");  //提交表单进行登录
        FindComent findComent = new FindComent(login.httpClient,1665311,1);
        findComent.findLoop();
        login.shoutDown();
    }


    public Login() {
        httpClient = HttpClients.createDefault();
    }

    /**
     * 用来关闭httpclient;
     */
    public void shoutDown() {
        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用来检测登陆是否成功，随便进入一个页面看返回结果
     */
    public void testLogin() {
        HttpGet httpGet = new HttpGet("http://bbs.uestc.edu.cn/forum.php?mod=viewthread&tid=1665256");
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            try {
                HttpEntity entity = httpResponse.getEntity();
                System.out.print(EntityUtils.toString(entity));
            }finally {
                httpResponse.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * post表单用来进行模拟登陆
     *
     * @param username
     * @param password
     */
    public void post(String username, String password) {
        String url = "http://bbs.uestc.edu.cn/member.php?mod=logging&action=login&loginsubmit=yes&loginhash=";
        String[] hash = get().split(" ");
        url = url + hash[0] + "&inajax=1";
        HttpPost httpPost = new HttpPost(url);

        //需要提交的表单数据
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("formhash", hash[1]));
        formparams.add(new BasicNameValuePair("referer", "http://bbs.uestc.edu.cn/"));
        formparams.add(new BasicNameValuePair("loginfield", "username"));
        formparams.add(new BasicNameValuePair("username", username));
        formparams.add(new BasicNameValuePair("password", password));
        formparams.add(new BasicNameValuePair("questionid", "0"));
        formparams.add(new BasicNameValuePair("answer", ""));

        try {
            UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(uefEntity);
            CloseableHttpResponse response = httpClient.execute(httpPost);

            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    System.out.println("--------------------------------------");
                    System.out.println("Response content: " + EntityUtils.toString(entity, "UTF-8"));
                    System.out.println("--------------------------------------");
                }
            } finally {
                response.close();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 用来返回login页面的loginhash与formhash
     *
     * @return
     */
    public String get() {
        try {
            HttpGet httpGet = new HttpGet("http://bbs.uestc.edu.cn/member.php?mod=logging&action=login");
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            try {
                HttpEntity httpEntity = httpResponse.getEntity();
                Document document = Jsoup.parse(EntityUtils.toString(httpEntity, "UTF-8"));
                Elements elements = document.select("form");
                Element element = elements.get(0);
                String action = element.attr("action");
                int index = action.indexOf("loginhash");
                String loginhash = action.substring(index + 10, index + 15);
                Elements inputs = element.select("input");
                String formhash = inputs.get(0).attr("value");
                return loginhash + " " + formhash;

            } finally {
                httpResponse.close();
            }
        } catch (IOException e) {
            System.out.print("getMethod wrong" + e.getMessage());
        }
        return null;
    }
}
