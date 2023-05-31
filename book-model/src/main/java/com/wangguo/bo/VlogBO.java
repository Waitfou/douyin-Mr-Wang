package com.wangguo.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Vlog的业务对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VlogBO {
    private String id;
    private String vlogerId; // 作者的id
    private String url;
    private String cover; // vlog封面
    private String title; // 标题
    private Integer width;
    private Integer height;
    private Integer likeCounts;
    private Integer commentCounts;
}
