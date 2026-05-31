<template>
  <AuthView v-if="!token" @authed="handleAuthed" />
  <el-container v-else class="app-shell">
    <el-aside class="sidebar" width="236px">
      <div class="brand">
        <div class="brand-mark">CM</div>
        <div>
          <strong>电话通讯录</strong>
          <span>Contact Master</span>
        </div>
      </div>
      <el-menu :default-active="active" class="menu" @select="active = $event">
        <el-menu-item index="dashboard"><el-icon><DataLine /></el-icon><span>首页统计</span></el-menu-item>
        <el-menu-item index="contacts"><el-icon><UserFilled /></el-icon><span>通讯录</span></el-menu-item>
        <el-menu-item index="recycle"><el-icon><Delete /></el-icon><span>回收站</span></el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div>
          <h1>{{ pageTitle }}</h1>
          <p>按分组浏览联系人，统一完成新增、搜索、导入导出和回收站管理</p>
        </div>
        <el-dropdown>
          <el-button>
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
      </el-header>

      <el-main class="content">
        <DashboardView v-if="active === 'dashboard'" @openContacts="active = 'contacts'" />
        <ContactsView v-else-if="active === 'contacts'" />
        <RecycleBinView v-else />
      </el-main>
    </el-container>
  </el-container>

  <el-dialog v-model="passwordVisible" title="修改密码" width="420px">
    <el-form :model="passwordForm" label-position="top">
      <el-form-item label="原密码"><el-input v-model="passwordForm.oldPassword" type="password" show-password /></el-form-item>
      <el-form-item label="新密码"><el-input v-model="passwordForm.newPassword" type="password" show-password /></el-form-item>
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

const titles = {
  dashboard: '首页统计',
  contacts: '通讯录',
  recycle: '回收站'
}

const pageTitle = computed(() => titles[active.value])

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
  min-height: 100vh;
}

.sidebar {
  border-right: 1px solid #e6eaf2;
  background: #fff;
}

.brand {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 20px;
}

.brand-mark {
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  font-weight: 800;
  background: #2563eb;
}

.brand span {
  display: block;
  margin-top: 2px;
  color: #6b7280;
  font-size: 12px;
}

.menu {
  border-right: 0;
}

.topbar {
  display: flex;
  height: 76px;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6eaf2;
  background: #fff;
}

.topbar h1 {
  margin: 0;
  font-size: 20px;
}

.topbar p {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.content {
  padding: 22px;
  background: #f5f7fb;
}

@media (max-width: 760px) {
  .app-shell {
    display: block;
  }

  .sidebar {
    width: 100% !important;
  }

  .brand {
    padding: 14px 16px;
  }

  .menu {
    display: flex;
    overflow-x: auto;
  }

  .menu :deep(.el-menu-item) {
    flex: 0 0 auto;
  }

  .topbar {
    height: auto;
    gap: 12px;
    padding: 14px 16px;
  }

  .content {
    padding: 14px;
  }
}
</style>
