<template>
  <main class="auth-page">
    <section class="auth-card glass-panel">
      <div class="auth-heading">
        <div class="brand-mark">CM</div>
        <p>Contact Master</p>
        <h1>电话通讯录管理系统</h1>
        <span>登录后管理联系人、分组、导入导出和回收站。</span>
      </div>

      <el-tabs v-model="mode" stretch>
        <el-tab-pane label="登录" name="login" />
        <el-tab-pane label="注册" name="register" />
      </el-tabs>

      <el-form :model="form" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" size="large" placeholder="请输入用户名" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" size="large" type="password" show-password placeholder="请输入密码" autocomplete="current-password" />
        </el-form-item>
        <el-form-item v-if="mode === 'register'" label="邮箱">
          <el-input v-model="form.email" size="large" placeholder="用于找回和展示，可选" inputmode="email" />
        </el-form-item>
        <el-button type="primary" size="large" class="submit" :loading="loading" @click="submit">
          {{ mode === 'login' ? '登录系统' : '注册并登录' }}
        </el-button>
      </el-form>
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api, unwrap } from '../api'

const emit = defineEmits(['authed'])
const mode = ref('login')
const loading = ref(false)
const form = ref({ username: '', password: '', email: '' })

async function submit() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const url = mode.value === 'login' ? '/auth/login' : '/auth/register'
    const data = unwrap(await api.post(url, form.value))
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data))
    ElMessage.success(mode.value === 'login' ? '登录成功' : '注册成功')
    emit('authed', data)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: grid;
  min-height: 100dvh;
  place-items: center;
  padding: 24px;
}

.auth-card {
  width: min(460px, 100%);
  padding: 30px;
}

.auth-heading {
  margin-bottom: 22px;
  text-align: center;
}

.brand-mark {
  display: grid;
  width: 58px;
  height: 58px;
  margin: 0 auto 14px;
  place-items: center;
  border: 1px solid rgb(255 255 255 / 78%);
  border-radius: 22px;
  color: #fff;
  font-weight: 850;
  background: linear-gradient(135deg, #007aff, #7c3aed);
  box-shadow: 0 16px 34px rgb(0 122 255 / 22%);
}

.auth-heading p {
  margin: 0 0 5px;
  color: var(--ios-blue);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  color: var(--ios-text);
  font-size: 26px;
}

.auth-heading span {
  display: block;
  margin-top: 8px;
  color: var(--ios-text-muted);
}

.submit {
  width: 100%;
  min-height: 46px;
  margin-top: 8px;
}
</style>
