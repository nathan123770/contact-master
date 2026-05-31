<template>
  <main class="auth-page">
    <section class="auth-card">
      <div class="auth-heading">
        <div class="brand-mark">CM</div>
        <h1>电话通讯录管理系统</h1>
        <p>登录后管理联系人、分组、导入导出和回收站</p>
      </div>

      <el-tabs v-model="mode" stretch>
        <el-tab-pane label="登录" name="login" />
        <el-tab-pane label="注册" name="register" />
      </el-tabs>

      <el-form :model="form" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" size="large" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" size="large" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item v-if="mode === 'register'" label="邮箱">
          <el-input v-model="form.email" size="large" placeholder="用于找回和展示，可选" />
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
  min-height: 100vh;
  place-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #eef6ff, #f7f9fc 45%, #eef2ff);
}

.auth-card {
  width: min(440px, 100%);
  padding: 28px;
  border: 1px solid #e5eaf4;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 16px 40px rgb(15 23 42 / 10%);
}

.auth-heading {
  margin-bottom: 20px;
  text-align: center;
}

.brand-mark {
  display: grid;
  width: 52px;
  height: 52px;
  margin: 0 auto 14px;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  font-weight: 800;
  background: #2563eb;
}

h1 {
  margin: 0;
  font-size: 24px;
}

p {
  margin: 8px 0 0;
  color: #6b7280;
}

.submit {
  width: 100%;
  margin-top: 8px;
}
</style>
