<template>
  <div class="line">
    <div class="contact-line-avatar">
      <img v-if="contact.avatarData" :src="contact.avatarData" alt="联系人头像" />
      <span v-else>{{ contact.name?.slice(0, 1) }}</span>
    </div>
    <div class="line-copy">
      <strong>{{ contact.name }}</strong>
      <p>{{ birthday ? contact.birthday || '未填写生日' : contact.phone || '未填写手机号' }}</p>
    </div>
    <el-tag v-if="contact.groupName" size="small">{{ contact.groupName }}</el-tag>
  </div>
</template>

<script setup>
defineProps({
  contact: { type: Object, required: true },
  birthday: { type: Boolean, default: false }
})
</script>

<style scoped>
.line {
  display: flex;
  gap: 12px;
  align-items: center;
  min-height: 64px;
  border: 1px solid rgb(255 255 255 / 64%);
  border-radius: 20px;
  padding: 10px;
  background: rgb(255 255 255 / 56%);
}

.contact-line-avatar {
  width: 42px;
  height: 42px;
  border-radius: 16px;
}

.line-copy {
  min-width: 0;
  flex: 1;
}

.line-copy strong,
.line-copy p {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.line-copy strong {
  color: var(--ios-text);
}

p {
  margin: 4px 0 0;
  color: var(--ios-text-muted);
  font-size: 13px;
}
</style>
