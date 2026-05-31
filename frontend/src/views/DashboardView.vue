<template>
  <div>
    <div class="stats-grid">
      <div v-for="item in statsCards" :key="item.label" class="stat-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </div>

    <div class="dashboard-grid">
      <section class="panel">
        <div class="panel-header">
          <strong>最近新增</strong>
          <el-button text type="primary" @click="$emit('openContacts')">查看全部</el-button>
        </div>
        <div class="panel-body list">
          <ContactLine v-for="contact in recent" :key="contact.id" :contact="contact" />
          <el-empty v-if="!recent.length" description="还没有联系人" />
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <strong>近 7 天生日</strong>
          <el-tag type="warning">{{ birthdays.length }} 人</el-tag>
        </div>
        <div class="panel-body list">
          <ContactLine v-for="contact in birthdays" :key="contact.id" :contact="contact" birthday />
          <el-empty v-if="!birthdays.length" description="暂无生日提醒" />
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <strong>收藏联系人</strong>
          <el-tag type="primary">{{ favorites.length }} 人</el-tag>
        </div>
        <div class="panel-body list">
          <ContactLine v-for="contact in favorites" :key="contact.id" :contact="contact" />
          <el-empty v-if="!favorites.length" description="暂无收藏联系人" />
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { api, unwrap } from '../api'
import ContactLine from '../components/ContactLine.vue'

defineEmits(['openContacts'])

const stats = ref({ totalContacts: 0, favoriteContacts: 0, groupCount: 0, recycleBinCount: 0, birthdayCount: 0 })
const recent = ref([])
const birthdays = ref([])
const favorites = ref([])

const statsCards = computed(() => [
  { label: '联系人总数', value: stats.value.totalContacts },
  { label: '收藏联系人', value: stats.value.favoriteContacts },
  { label: '分组数量', value: stats.value.groupCount },
  { label: '回收站', value: stats.value.recycleBinCount },
  { label: '生日提醒', value: stats.value.birthdayCount }
])

onMounted(load)

async function load() {
  const [s, r, b, f] = await Promise.all([
    api.get('/dashboard/statistics'),
    api.get('/dashboard/recent'),
    api.get('/dashboard/birthdays'),
    api.get('/dashboard/favorites')
  ])
  stats.value = unwrap(s)
  recent.value = unwrap(r)
  birthdays.value = unwrap(b)
  favorites.value = unwrap(f)
}
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(140px, 1fr));
  gap: 14px;
  margin-bottom: 18px;
}

.stat-card {
  padding: 18px;
  border: 1px solid #e6eaf2;
  border-radius: 8px;
  background: #fff;
}

.stat-card span {
  color: #6b7280;
  font-size: 14px;
}

.stat-card strong {
  display: block;
  margin-top: 10px;
  color: #1d4ed8;
  font-size: 28px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.dashboard-grid .panel:first-child {
  grid-row: span 2;
}

.list {
  display: grid;
  gap: 10px;
}

@media (max-width: 980px) {
  .stats-grid,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
