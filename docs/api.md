# 接口说明

所有登录后接口都需要请求头：

```text
Authorization: Bearer <token>
```

统一响应格式：

```json
{
  "success": true,
  "message": "操作成功",
  "data": {}
}
```

分页响应格式：

```json
{
  "items": [],
  "total": 0,
  "page": 1,
  "size": 10,
  "totalPages": 0
}
```

## 认证

- `POST /api/auth/register` 注册
- `POST /api/auth/login` 登录
- `POST /api/auth/change-password` 修改密码

## 联系人

- `GET /api/contacts` 分页查询联系人，参数：`keyword`, `groupId`, `favorite`, `page`, `size`
- `POST /api/contacts` 新增联系人
- `PUT /api/contacts/{id}` 编辑联系人
- `DELETE /api/contacts/{id}` 删除到回收站
- `DELETE /api/contacts/batch` 批量删除到回收站
- `PUT /api/contacts/{id}/favorite` 收藏/取消收藏
- `POST /api/contacts/import` CSV 导入
- `GET /api/contacts/export` CSV 导出

## 回收站

- `GET /api/contacts/recycle-bin` 查询回收站
- `PUT /api/contacts/{id}/restore` 恢复联系人
- `DELETE /api/contacts/{id}/permanent` 彻底删除

## 分组

- `GET /api/groups` 查询分组
- `POST /api/groups` 新增分组
- `PUT /api/groups/{id}` 修改分组
- `DELETE /api/groups/{id}` 删除分组

## 首页

- `GET /api/dashboard/statistics` 统计数据
- `GET /api/dashboard/recent` 最近新增联系人
- `GET /api/dashboard/favorites` 收藏联系人
- `GET /api/dashboard/birthdays` 近 7 天生日提醒
