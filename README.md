# 电话通讯录管理系统

一个面向课程设计的 Web 版电话通讯录管理系统，采用 Spring Boot + Vue 3 + MySQL 架构。

## 功能

- 用户注册、登录、退出、修改密码
- 联系人新增、编辑、删除、批量删除、详情查看
- 分组联系人一体化管理：左侧分组展开联系人，右侧展示当前分组联系人列表
- 分组新增、重命名、删除，收藏联系人、搜索筛选
- CSV 导入导出、CSV 模板下载、导入失败明细展示
- 导入和新增时校验必填项、手机号格式、重复手机号
- 回收站软删除、恢复、彻底删除
- 首页统计、最近新增、收藏快捷列表、近 7 天生日提醒

## 项目结构

```text
backend/   Spring Boot 后端接口
frontend/  Vue 3 + Element Plus 前端界面
docs/      数据库和接口说明
```

## MySQL 配置

默认数据库连接如下：

```text
数据库：contact_master
账号：root
密码：root
端口：3306
```

后端配置位于 `backend/src/main/resources/application.yml`。应用启动时使用
`createDatabaseIfNotExist=true` 自动创建数据库；如果你的 MySQL 用户没有创建数据库权限，可以先手动执行：

```sql
CREATE DATABASE IF NOT EXISTS contact_master
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

## 后端启动

```bash
cd backend
mvn spring-boot:run
```

后端接口地址为 `http://localhost:8080`。

## 前端启动

PowerShell 中建议使用 `npm.cmd`，避免系统执行策略拦截 `npm.ps1`：

```bash
cd frontend
npm.cmd install
npm.cmd run dev
```

前端默认访问 `http://localhost:5173`，接口代理到 `http://localhost:8080`。

## CSV 导入格式

支持字段顺序：

```csv
name,phone,email,group,company,position,address,birthday,remark,favorite
张三,13800138000,zhangsan@example.com,默认分组,示例公司,工程师,上海市,2000-06-03,重要客户,Y
```

其中 `name` 和 `phone` 必填；`favorite` 可填 `Y` 或 `true`。

## 演示建议

1. 注册并登录用户。
2. 在通讯录左侧展开默认分组，查看分组下的联系人。
3. 新增联系人，设置所属分组、生日和收藏。
4. 点击左侧分组切换联系人列表，并使用搜索、收藏筛选。
5. 下载 CSV 模板并导入，展示重复手机号校验和失败明细。
6. 删除联系人后进入回收站，再恢复或彻底删除。
