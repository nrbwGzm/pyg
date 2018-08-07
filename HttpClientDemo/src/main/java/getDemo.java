import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URI;

public class getDemo {
    public static void main(String[] args) throws Exception {
        //1. HttpClients工具类创建默认http客户端 (相当于浏览器)
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //2.使用get请求,不带参数
//        HttpGet httpGet = new HttpGet("http://www.baidu.com/");
        //2.1 使用get请求,带参数(相当于在百度搜索java)
        // 方式一:
        // HttpGet httpGet = new HttpGet("http://www.baidu.com/s?wd=java");
        //方式二:
        HttpGet httpGet = new HttpGet();
            // 创建一个带参数的URI
        URI uri = new URIBuilder("http://www.baidu.com/s").setParameter("wd", "java").build();
        httpGet.setURI(uri);
        //3.访问百度    CloseableHttpResponse : 可关闭的Http响应
        CloseableHttpResponse closeableHttpResponse = httpClient.execute(httpGet);//execute : 执行
        //4. if ( http响应.获取状态行.获取状态代码==200 )
        if (closeableHttpResponse.getStatusLine().getStatusCode()==200){
            //获取http响应的内容
            HttpEntity entity = closeableHttpResponse.getEntity();
            //将响应的内容转换为字符串
            String responseData = EntityUtils.toString(entity, "UTF-8");
            //在控制台打印获取到的内容
            System.out.println(responseData);
        }
        //5.关闭资源
        closeableHttpResponse.close();//关闭http响应
        httpClient.close();//关闭http客户端
    }
}
