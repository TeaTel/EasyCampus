package com.campus.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class PostQueryDTO {

    private Long boardId;

    private String postType;

    private String keyword;

    private Long userId;

    private List<Long> userIds;

    private String status;

    private String tag;

    /** 校区标签 */
    private String campusTag;

    /** 是否为广告 */
    private Boolean isAd;

    /** 排序方式: time_desc(默认), hot, likes */
    private String sortBy = "time_desc";

    private Integer page = 1;

    private Integer size = 20;

    public int getOffset() {
        return (page - 1) * size;
    }
}
