# campus-learning-platform 项目长期记忆

## 项目定位
毕业设计：校园智能学习辅导平台。本质是 RAG（检索增强生成）学习辅导系统，不是训练模型。
三端角色：学生 / 教师 / 管理员。核心闭环：教师上传课程文档 → 解析切分 → 向量化 → 学生提问 → 检索 → 大模型回答并展示引用来源 → 教师查看学习记录。
第一版明确不做：模型微调、知识图谱、微服务、Redis、RabbitMQ、智能组卷、学习计划生成。

## 技术栈（既定）
- 后端：Java 17 + Spring Boot 4.1.1 + Maven + Spring MVC + MyBatis-Plus 3.5.17（Boot4 专用 starter）+ MySQL 8 + spring-security-crypto(BCrypt)。待接入：Spring Security + JWT、Spring AI、Knife4j。
- 前端（未开始）：Vue 3 + Vite + JavaScript + Vue Router + Pinia + Axios + Element Plus + ECharts。
- 基础设施：Docker / Docker Compose（MySQL 容器 `campus-mysql`，库 `campus_learning`），Qdrant 待引入。
- 工具：IDEA Community + VS Code + DBeaver Community + Git/GitHub。
- 仓库：git@github.com:PepsicoX/campus-learning-platform.git，主开发分支 `feature/project-init`。

## 已记录的技术陷阱
- Spring Boot 4 中 Web starter 名为 `spring-boot-starter-webmvc`（非 `web`）。
- MyBatis-Plus 3.5.17 的 `IService` / `ServiceImpl` 包路径为 `com.baomidou.mybatisplus.spring.service[.impl]`，3.x 的 `extension.service` 已失效。
- 数据库密码走环境变量 `DB_PASSWORD`，在 IDEA 运行配置中注入，不提交明文。
- DBeaver 连本地 MySQL 容器需设 `allowPublicKeyRetrieval=true`；`useSSL=false` 仅限本地。

## 用户偏好（本项目）
- 要求新手式引导：先讲作用 → 给代码 → 运行验证 → 解释结果 → Git 提交，一次只做一小步。
- 不喜欢长篇回答，多次要求"只要告诉我第一步干什么"。
- 文档风格正式书面，避免 AI 痕迹。

## 关键文档
- `docs/01-项目思路与技术栈.md` — 项目定位、技术栈清单、数据库设计、进度与路线图（2026-09-11 由 Codex 对话记录整理生成）。
