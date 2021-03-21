package xyz.bolitao.simpsearchdemo.service;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.bolitao.simpsearchdemo.crawler.HtmlParseUtil;
import xyz.bolitao.simpsearchdemo.crawler.entity.JdContent;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author boli
 */
@Service
@Slf4j
public class JdContentService {
    private final RestHighLevelClient restHighLevelClientt;

    @Autowired
    public JdContentService(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClientt = restHighLevelClient;
    }

    public Boolean parseContent(String keyword) throws IOException {
        List<JdContent> contents = new HtmlParseUtil().parseJd(keyword);
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2m");

        for (JdContent content : contents) {
            bulkRequest.add(
                    new IndexRequest("goods")
                            .source(JSON.toJSONString(content), XContentType.JSON));
        }

        BulkResponse bulk = restHighLevelClientt.bulk(bulkRequest, RequestOptions.DEFAULT);
        return bulk.hasFailures();
    }

    public List<Map<String, Object>> searchPageable(String keyword, Integer currentPage, Integer pageSize) throws IOException {
        if (currentPage <= 1) {
            currentPage = 1;
        }
        SearchRequest searchRequest = new SearchRequest("goods");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 分页
        searchSourceBuilder.from(currentPage);
        searchSourceBuilder.size(pageSize);

        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
        searchSourceBuilder.query(termQueryBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClientt.search(searchRequest, RequestOptions.DEFAULT);


        ArrayList<Map<String, Object>> ret = new ArrayList<>();
        for (SearchHit searchHit : searchResponse.getHits().getHits()) {
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();

            ret.add(searchHit.getSourceAsMap());
        }
        return ret;
    }

    public List<Map<String, Object>> highlightSearchPageable(String keyword, Integer currentPage, Integer pageSize) throws IOException {
        if (currentPage <= 1) {
            currentPage = 1;
        }
        SearchRequest searchRequest = new SearchRequest("goods");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 分页
        searchSourceBuilder.from(currentPage);
        searchSourceBuilder.size(pageSize);

        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
        searchSourceBuilder.query(termQueryBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // highlight
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClientt.search(searchRequest, RequestOptions.DEFAULT);


        ArrayList<Map<String, Object>> ret = new ArrayList<>();
        for (SearchHit searchHit : searchResponse.getHits().getHits()) {
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();

            if (title != null) {
                Text[] fragments = title.fragments();
                String n_title = "";
                for (Text fragment : fragments) {
                    n_title += fragment;
                }
                sourceAsMap.put("title", n_title);
            }

            ret.add(sourceAsMap);
        }
        return ret;
    }
}
