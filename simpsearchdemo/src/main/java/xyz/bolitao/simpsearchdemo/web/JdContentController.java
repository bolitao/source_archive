package xyz.bolitao.simpsearchdemo.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import xyz.bolitao.simpsearchdemo.service.JdContentService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author boli
 */
@RestController
public class JdContentController {
    private final JdContentService jdContentService;

    @Autowired
    public JdContentController(JdContentService jdContentService) {
        this.jdContentService = jdContentService;
    }

    @GetMapping(value = "/parse/{keyword}")
    public Boolean parse(@PathVariable("keyword") String keyword) throws IOException {
        return jdContentService.parseContent(keyword);
    }

    @GetMapping(value = "/search/{keyword}/{currentPage}/{pageSize}")
    public List<Map<String, Object>> search(@PathVariable("keyword") String keyword,
                                            @PathVariable("currentPage") int currentPage,
                                            @PathVariable("pageSize") int pageSize) throws IOException {
        return jdContentService.searchPageable(keyword, currentPage, pageSize);
    }

    @GetMapping(value = "/highlightSearch/{keyword}/{currentPage}/{pageSize}")
    public List<Map<String, Object>> highlightSearch(@PathVariable("keyword") String keyword,
                                            @PathVariable("currentPage") int currentPage,
                                            @PathVariable("pageSize") int pageSize) throws IOException {
        return jdContentService.highlightSearchPageable(keyword, currentPage, pageSize);
    }
}
