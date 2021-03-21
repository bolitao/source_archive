package xyz.bolitao.simpsearchdemo.crawler.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author boli
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JdContent {
    private String title;
    private String image;
    private String price;
}
