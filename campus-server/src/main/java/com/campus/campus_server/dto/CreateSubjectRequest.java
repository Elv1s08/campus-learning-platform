package com.campus.campus_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSubjectRequest {

    @NotBlank(message = "学科编号不能为空")
    @Size(max = 50, message = "学科编号不能超过50个字符")
    private String subjectCode;

    @NotBlank(message = "学科名称不能为空")
    @Size(max = 100, message = "学科名称不能超过100个字符")
    private String subjectName;

    private String description;
}