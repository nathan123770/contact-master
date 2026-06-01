<template>
  <div class="dashboard-page">
    <div class="stats-grid">
      <div v-for="item in statsCards" :key="item.label" class="stat-card glass-panel">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.hint }}</p>
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
  { label: '联系人总数', value: stats.value.totalContacts, hint: '全部可用联系人' },
  { label: '收藏联系人', value: stats.value.favoriteContacts, hint: '重点关系' },
  { label: '分组数量', value: stats.value.groupCount, hint: '通讯录结构' },
  { label: '回收站', value: stats.value.recycleBinCount, hint: '可恢复联系人' },
  { label: '生日提醒', value: stats.value.birthdayCount, hint: '近 7 天' }
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
.dashboard-page {
  display: grid;
  gap: 18px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(140px, 1fr));
  gap: 14px;
}

.stat-card {
  padding: 18px;
}

.stat-card span {
  color: var(--ios-text-muted);
  font-size: 13px;
  font-weight: 700;
}

.stat-card strong {
  display: block;
  margin-top: 10px;
  color: var(--ios-blue);
  font-size: 32px;
  line-height: 1;
}

.stat-card p {
  margin: 10px 0 0;
  color: var(--ios-text-subtle);
  font-size: 12px;
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

@media (max-width: 1100px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 980px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .dashboard-grid .panel:first-child {
    grid-row: auto;
  }
}

@media (max-width: 560px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
