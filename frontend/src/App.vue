<template>
  <AuthView v-if="!token" @authed="handleAuthed" />
  <div v-else class="app-shell">
    <aside class="app-sidebar glass-panel">
      <div class="brand">
        <div class="brand-mark">CM</div>
        <div>
          <strong>电话通讯录</strong>
          <span>Contact Master</span>
        </div>
      </div>

      <nav class="app-nav" aria-label="主导航">
        <button
          v-for="item in navItems"
          :key="item.key"
          :class="['app-nav-item', { active: active === item.key }]"
          type="button"
          @click="active = item.key"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </button>
      </nav>
    </aside>

    <section class="app-workspace">
      <header class="topbar glass-panel">
        <div>
          <p class="eyebrow">{{ pageMeta.eyebrow }}</p>
          <h1>{{ pageMeta.title }}</h1>
          <span>{{ pageMeta.description }}</span>
        </div>
        <el-dropdown>
          <el-button class="account-button">
            {{ user?.username || '用户' }}
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="passwordVisible = true">修改密码</el-dropdown-item>
              <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <main class="content">
        <DashboardView v-if="active === 'dashboard'" @openContacts="active = 'contacts'" />
        <ContactsView v-else-if="active === 'contacts'" />
        <RecycleBinView v-else />
      </main>
    </section>

    <nav class="mobile-nav glass-panel" aria-label="移动端主导航">
      <button
        v-for="item in navItems"
        :key="item.key"
        :class="['mobile-nav-item', { active: active === item.key }]"
        type="button"
        @click="active = item.key"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </button>
    </nav>
  </div>

  <el-dialog v-model="passwordVisible" title="修改密码" width="420px">
    <el-form :model="passwordForm" label-position="top">
      <el-form-item label="原密码">
        <el-input v-model="passwordForm.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="passwordForm.newPassword" type="password" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="passwordVisible = false">取消</el-button>
      <el-button type="primary" @click="changePassword">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ArrowDown, DataLine, Delete, UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { api } from './api'
import AuthView from './views/AuthView.vue'
import DashboardView from './views/DashboardView.vue'
import ContactsView from './views/ContactsView.vue'
import RecycleBinView from './views/RecycleBinView.vue'

const token = ref(localStorage.getItem('token'))
const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
const active = ref('dashboard')
const passwordVisible = ref(false)
const passwordForm = ref({ oldPassword: '', newPassword: '' })

const navItems = [
  { key: 'dashboard', label: '概览', icon: DataLine },
  { key: 'contacts', label: '通讯录', icon: UserFilled },
  { key: 'recycle', label: '回收站', icon: Delete }
]

const pageInfo = {
  dashboard: {
    title: '今日概览',
    eyebrow: 'Contacts Overview',
    description: '用轻量卡片掌握联系人、收藏、生日提醒和最近新增。'
  },
  contacts: {
    title: '通讯录',
    eyebrow: 'iOS 26 Contacts',
    description: '按分组、收藏和未分组快速浏览，支持导入导出与批量管理。'
  },
  recycle: {
    title: '回收站',
    eyebrow: 'Recently Deleted',
    description: '查看软删除联系人，按需恢复或彻底删除。'
  }
}

const pageMeta = computed(() => pageInfo[active.value])

function handleAuthed(payload) {
  token.value = payload.token
  user.value = payload
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  token.value = null
  user.value = null
}

async function changePassword() {
  await api.post('/auth/change-password', passwordForm.value)
  ElMessage.success('密码修改成功')
  passwordVisible.value = false
  passwordForm.value = { oldPassword: '', newPassword: '' }
}
</script>

<style scoped>
.app-shell {
  display: grid;
  min-height: 100dvh;
  grid-template-columns: 248px minmax(0, 1fr);
  gap: 18px;
  padding: 18px;
}

.app-sidebar {
  position: sticky;
  top: 18px;
  height: calc(100dvh - 36px);
  padding: 18px;
}

.brand {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 6px 4px 22px;
}

.brand-mark {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border: 1px solid rgb(255 255 255 / 78%);
  border-radius: 18px;
  color: #fff;
  font-weight: 850;
  background: linear-gradient(135deg, #007aff, #7c3aed);
  box-shadow: 0 16px 34px rgb(0 122 255 / 22%);
}

.brand strong,
.brand span {
  display: block;
}

.brand strong {
  color: var(--ios-text);
  font-size: 17px;
}

.brand span {
  margin-top: 2px;
  color: var(--ios-text-muted);
  font-size: 12px;
}

.app-nav {
  display: grid;
  gap: 8px;
}

.app-nav-item,
.mobile-nav-item {
  display: flex;
  align-items: center;
  border: 0;
  color: var(--ios-text-muted);
  background: transparent;
  cursor: pointer;
}

.app-nav-item {
  min-height: 48px;
  gap: 12px;
  border-radius: 18px;
  padding: 0 14px;
  font-size: 15px;
  font-weight: 700;
}

.app-nav-item:hover,
.app-nav-item.active {
  color: var(--ios-text);
  background: rgb(255 255 255 / 72%);
  box-shadow: inset 0 1px 0 rgb(255 255 255 / 86%), 0 10px 24px rgb(45 91 160 / 10%);
}

.app-workspace {
  min-width: 0;
}

.topbar {
  display: flex;
  min-height: 92px;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 18px 22px;
}

.eyebrow {
  margin: 0 0 4px;
  color: var(--ios-blue);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.topbar h1 {
  margin: 0;
  color: var(--ios-text);
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 0;
}

.topbar span {
  display: block;
  margin-top: 5px;
  color: var(--ios-text-muted);
  font-size: 14px;
}

.account-button {
  min-height: 42px;
  border-color: rgb(255 255 255 / 72%);
  background: rgb(255 255 255 / 68%);
}

.content {
  padding: 18px 0 0;
}

.mobile-nav {
  display: none;
}

@media (max-width: 900px) {
  .app-shell {
    display: block;
    padding: 12px 12px 92px;
  }

  .app-sidebar {
    display: none;
  }

  .topbar {
    min-height: auto;
    align-items: flex-start;
    padding: 16px;
  }

  .topbar h1 {
    font-size: 24px;
  }

  .topbar span {
    font-size: 13px;
  }

  .account-button {
    min-width: 44px;
  }

  .mobile-nav {
    position: fixed;
    right: 12px;
    bottom: 12px;
    left: 12px;
    z-index: 20;
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 6px;
    padding: 8px;
    border-radius: 24px;
  }

  .mobile-nav-item {
    min-height: 52px;
    flex-direction: column;
    justify-content: center;
    gap: 3px;
    border-radius: 18px;
    font-size: 12px;
    font-weight: 700;
  }

  .mobile-nav-item.active {
    color: var(--ios-text);
    background: rgb(255 255 255 / 74%);
    box-shadow: inset 0 1px 0 rgb(255 255 255 / 90%);
  }
}

@media (max-width: 520px) {
  .topbar {
    gap: 10px;
  }

  .topbar span {
    display: none;
  }
}
</style>
