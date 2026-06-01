<template>
  <el-drawer :model-value="modelValue" title="联系人详情" size="430px" @close="$emit('update:modelValue', false)">
    <div v-if="contact" class="detail">
      <section class="detail-hero">
        <div class="detail-avatar ios-avatar">
          <img v-if="contact.avatarData" :src="contact.avatarData" alt="联系人头像" />
          <span v-else>{{ contact.name?.slice(0, 1) }}</span>
        </div>
        <h2>{{ contact.name }}</h2>
        <p>{{ contact.position || '未填写职位' }}<span v-if="contact.company"> · {{ contact.company }}</span></p>
        <el-tag v-if="contact.favorite" type="warning">收藏联系人</el-tag>
      </section>

      <section class="detail-card">
        <DetailItem label="手机号" :value="contact.phone" />
        <DetailItem label="邮箱" :value="contact.email || '未填写'" />
        <DetailItem label="分组" :value="contact.groupName || '未分组'" />
      </section>

      <section class="detail-card">
        <DetailItem label="公司" :value="contact.company || '未填写'" />
        <DetailItem label="职位" :value="contact.position || '未填写'" />
        <DetailItem label="生日" :value="contact.birthday || '未填写'" />
        <DetailItem label="地址" :value="contact.address || '未填写'" />
      </section>

      <section class="detail-card">
        <DetailItem label="备注" :value="contact.remark || '无'" />
      </section>
    </div>
  </el-drawer>
</template>

<script setup>
import { defineComponent, h } from 'vue'

defineProps({
  modelValue: Boolean,
  contact: Object
})
defineEmits(['update:modelValue'])

const DetailItem = defineComponent({
  name: 'DetailItem',
  props: {
    label: { type: String, required: true },
    value: { type: String, default: '' }
  },
  setup(props) {
    return () =>
      h('div', { class: 'detail-item' }, [
        h('span', props.label),
        h('strong', props.value)
      ])
  }
})
</script>

<style scoped>
.detail {
  display: grid;
  gap: 14px;
}

.detail-hero,
.detail-card {
  border: 1px solid rgb(255 255 255 / 70%);
  border-radius: 24px;
  background: rgb(255 255 255 / 58%);
  box-shadow: inset 0 1px 0 rgb(255 255 255 / 86%);
}

.detail-hero {
  display: grid;
  justify-items: center;
  padding: 24px 18px 20px;
  text-align: center;
}

.detail-avatar {
  width: 94px;
  height: 94px;
  border-radius: 32px;
  font-size: 34px;
}

h2 {
  margin: 14px 0 6px;
  color: var(--ios-text);
  font-size: 26px;
}

p {
  margin: 0 0 12px;
  color: var(--ios-text-muted);
}

.detail-card {
  overflow: hidden;
  padding: 4px 16px;
}

.detail-item {
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
  padding: 14px 0;
}

.detail-item + .detail-item {
  border-top: 1px solid rgb(177 199 231 / 30%);
}

.detail-item span {
  color: var(--ios-text-muted);
  font-size: 13px;
}

.detail-item strong {
  min-width: 0;
  color: var(--ios-text);
  font-weight: 680;
  overflow-wrap: anywhere;
}
</style>
