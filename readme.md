[请观看清水河畔帖子](http://bbs.uestc.edu.cn/forum.php?mod=viewthread&tid=1665546)
##### 河畔的点评是使用ajax请求获得的，该请求需要一个tid相关的commentId,
##### 所以请求comment（点评）内容前要解析tid页面，找出commentId，然后再进行ajax请求，
##### 并解析数据。请求返回然一串xml（怨念颇深，为啥不是Json ！！！）通过对该xml解析即可获得该主题帖的点评信息。
##### 然而这串xml是被![CDATA[ xml ]] 包含的，所以Jsoup无法解析，需要把提取出![CDATA[]]中的xml然后再拿去给Jsoup进行解析。
#####（还要记得如果遇到多页点评，要每一页都找）为了提高效率，使用了支持并发的httpclient，然后使用了线程池进行并发请求。
##### 但是还是遇到了问题，httpclient在execute get请求的时候，会卡死，既不会返回请求超时也不会返回响应超时（设置了请求时的超时时间的），
##### 所以一旦卡死，还是需要人为重启，从当前到达的tid继续进行查找。