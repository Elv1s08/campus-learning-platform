package com.campus.campus_server.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.campus.campus_server.entity.KnowledgeChunk;
import com.campus.campus_server.mapper.KnowledgeChunkMapper;
import com.campus.campus_server.service.KnowledgeChunkService;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeChunkServiceImpl
        extends ServiceImpl<KnowledgeChunkMapper, KnowledgeChunk>
        implements KnowledgeChunkService {
}