package com.campus.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdCreateDTO {

    @NotBlank(message = "广告标题不能为空")
    @Size(min = 2, max = 200, message = "标题长度为2-200个字符")
    private String title;

    @NotBlank(message = "广告内容不能为空")
    @Size(min = 1, max = 10000, message = "内容长度不能超过10000个字符")
    private String content;

    private String tags;

    private String packageId;

    /** 内容类型：SHOWCASE(默认) 或 ACTIVITY */
    private String postType;
}
