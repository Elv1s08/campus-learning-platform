package com.campus.campus_server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("knowledge_document")
public class KnowledgeDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long subjectId;
    private Long uploadedBy;
    private String fileName;
    private String storagePath;
    private String fileType;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}