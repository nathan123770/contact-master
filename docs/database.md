# 数据库设计

数据库：`contact_master`

字符集建议：`utf8mb4`

## users

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| username | varchar | 用户名，唯一 |
| password_hash | varchar | 密码哈希 |
| email | varchar | 邮箱 |
| created_at | datetime | 创建时间 |

## contact_groups

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| user_id | bigint | 所属用户 |
| name | varchar | 分组名称 |
| created_at | datetime | 创建时间 |

同一用户下分组名称唯一。

## contacts

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| user_id | bigint | 所属用户 |
| group_id | bigint | 所属分组 |
| name | varchar | 姓名 |
| phone | varchar | 手机号 |
| email | varchar | 邮箱 |
| company | varchar | 公司 |
| position | varchar | 职位 |
| address | varchar | 地址 |
| birthday | date | 生日 |
| remark | varchar | 备注 |
| favorite | boolean | 是否收藏 |
| deleted | boolean | 是否进入回收站 |
| deleted_at | datetime | 删除时间 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

业务规则：同一用户下未进入回收站的联系人手机号不能重复。

## import_records

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| user_id | bigint | 所属用户 |
| file_name | varchar | 文件名 |
| success_count | int | 成功数量 |
| failure_count | int | 失败数量 |
| created_at | datetime | 导入时间 |
