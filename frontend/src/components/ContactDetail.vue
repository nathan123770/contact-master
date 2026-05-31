<template>
  <el-drawer :model-value="modelValue" title="联系人详情" size="420px" @close="$emit('update:modelValue', false)">
    <div v-if="contact" class="detail">
      <div class="avatar">{{ contact.name?.slice(0, 1) }}</div>
      <h2>{{ contact.name }}</h2>
      <el-tag v-if="contact.favorite" type="warning">收藏联系人</el-tag>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="手机号">{{ contact.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ contact.email || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="分组">{{ contact.groupName || '未分组' }}</el-descriptions-item>
        <el-descriptions-item label="公司">{{ contact.company || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="职位">{{ contact.position || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ contact.address || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="生日">{{ contact.birthday || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ contact.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </el-drawer>
</template>

<script setup>
defineProps({
  modelValue: Boolean,
  contact: Object
})
defineEmits(['update:modelValue'])
</script>

<style scoped>
.detail {
  text-align: center;
}

.avatar {
  display: grid;
  width: 72px;
  height: 72px;
  margin: 0 auto 12px;
  place-items: center;
  border-radius: 8px;
  color: #1d4ed8;
  font-size: 28px;
  font-weight: 800;
  background: #eaf2ff;
}

h2 {
  margin: 0 0 10px;
}

.el-descriptions {
  margin-top: 18px;
  text-align: left;
}
</style>
