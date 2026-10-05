package com.campus.campus_server.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.campus.campus_server.entity.KnowledgeDocument;
import com.campus.campus_server.mapper.KnowledgeDocumentMapper;
import com.campus.campus_server.service.KnowledgeDocumentService;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeDocumentServiceImpl
        extends ServiceImpl<KnowledgeDocumentMapper, KnowledgeDocument>
        implements KnowledgeDocumentService {
}