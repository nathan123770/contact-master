<template>
  <div class="line">
    <div class="avatar">{{ contact.name?.slice(0, 1) }}</div>
    <div>
      <strong>{{ contact.name }}</strong>
      <p>{{ birthday ? contact.birthday : contact.phone }}</p>
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
  gap: 10px;
  align-items: center;
  padding: 10px;
  border: 1px solid #eef1f6;
  border-radius: 8px;
}

.avatar {
  display: grid;
  width: 38px;
  height: 38px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 8px;
  color: #1d4ed8;
  font-weight: 800;
  background: #eaf2ff;
}

.line div:nth-child(2) {
  min-width: 0;
  flex: 1;
}

p {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 13px;
}
</style>
