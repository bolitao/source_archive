package xyz.bolitao.simpsearchdemo.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import xyz.bolitao.simpsearchdemo.crawler.entity.JdContent;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author boli
 */
@Component
public class HtmlParseUtil {
    public static void main(String[] args) throws IOException {
        new HtmlParseUtil().parseJd("高效").forEach(System.out::println);
    }

    public List<JdContent> parseJd(String keyword) throws IOException {
        ArrayList<JdContent> jdContents = new ArrayList<>();

        String url = "https://search.jd.com/Search?keyword=" + keyword + "&enc=utf-8";
        Document document = Jsoup.parse(new URL(url), 30000);
        Element jGoodsList = document.getElementById("J_goodsList");
        Elements elementsByTag = jGoodsList.getElementsByTag("li");
        for (Element element : elementsByTag) {
            String img = element.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = element.getElementsByClass("p-price").eq(0).text();
            String title = element.getElementsByClass("p-name").eq(0).text();
            jdContents.add(new JdContent(title, img, price));
        }
        return jdContents;
    }
}
