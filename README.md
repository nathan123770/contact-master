# 电话通讯录管理系统

一个面向课程设计的 Web 版电话通讯录管理系统，采用 Spring Boot + Vue 3 + Element Plus + MySQL 架构。

## 功能简介

- 用户注册、登录、退出、修改密码
- 联系人新增、编辑、删除、批量删除、详情查看
- 分组管理、收藏联系人、搜索筛选
- CSV 导入导出、模板下载、导入失败明细展示
- 回收站软删除、恢复、彻底删除
- 首页统计、最近新增、收藏快捷列表、生日提醒
- 联系人提醒中心，支持回访、生日、纪念日和其他提醒
- 演示联系人头像使用前端静态资源 URL，例如 `/avatars/seed-contacts/avatar-01.jpg`

## 项目结构

```text
backend/                         Spring Boot 后端接口
frontend/                        Vue 3 + Element Plus 前端
frontend/public/avatars/         演示头像静态资源，打包时会进入前端 dist
database/contact_master_demo.sql MySQL 建表和演示数据
docs/                            接口和数据库说明
scripts/seed-contacts.ps1        本机已有数据库的演示联系人重置脚本
```

## 演示账号

导入 `database/contact_master_demo.sql` 后可直接登录：

```text
用户名：admin
密码：123456
```

## 数据库初始化

默认数据库配置：

```text
数据库：contact_master
账号：root
密码：root
端口：3306
```

初始化方式：

```bash
mysql -uroot -proot --default-character-set=utf8mb4 < database/contact_master_demo.sql
```

如果 PowerShell 不方便使用重定向，也可以进入 MySQL 后执行：

```sql
source C:/你的项目路径/contact-master/database/contact_master_demo.sql
```

数据库文件会创建 `contact_master`，并写入 1 个演示用户、4 个默认分组和 30 个演示联系人。联系人头像字段存的是 URL 路径，不是 base64；图片文件位于 `frontend/public/avatars/seed-contacts/`，前端打包时会一起复制。

## 后端启动

确认 MySQL 已启动，并且 `backend/src/main/resources/application.yml` 中的账号密码与本机一致。

```bash
cd backend
mvn spring-boot:run
```

后端接口地址为：

```text
http://localhost:8080
```

## 前端启动

PowerShell 中建议使用 `npm.cmd`，避免系统执行策略拦截 `npm.ps1`。

```bash
cd frontend
npm.cmd install
npm.cmd run dev
```

前端默认访问：

```text
http://localhost:5173
```

开发环境下 Vite 会把 `/api` 代理到 `http://localhost:8080`。

## 前端打包

```bash
cd frontend
npm.cmd run build
```

打包产物在 `frontend/dist/`。`frontend/public/avatars/seed-contacts/` 下的 `avatar-01.jpg` 到 `avatar-30.jpg` 会被复制到 dist 中，因此数据库里的 `/avatars/seed-contacts/avatar-xx.jpg` 可以正常访问。

## 重新写入演示联系人

如果本机数据库已经存在，但只想重置 30 个演示联系人，可以运行：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\seed-contacts.ps1
```

脚本会删除当前用户下手机号 `13926060001` 到 `13926060030` 的旧演示联系人，然后重新插入干净数据。默认 `UserId = 1`，对应 `database/contact_master_demo.sql` 里的 `admin` 用户；如需指定其他用户：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\seed-contacts.ps1 -UserId 1
```

## CSV 导入格式

支持字段顺序：

```csv
name,phone,email,group,company,position,address,birthday,remark,favorite
张三,13800138000,zhangsan@example.com,默认分组,示例公司,工程师,上海市,2000-06-03,重要客户,Y
```

其中 `name` 和 `phone` 必填；`favorite` 可填 `Y` 或 `true`。
