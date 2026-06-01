<template>
  <div class="reminder-page">
    <header class="reminder-hero glass-panel">
      <div>
        <p>Reminder Center</p>
        <h2>提醒</h2>
        <span>{{ page.total }} 条提醒 · {{ pendingCountText }}</span>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新建提醒</el-button>
    </header>

    <section class="reminder-tools glass-panel">
      <el-input
        v-model="query.keyword"
        class="reminder-search"
        placeholder="搜索联系人、手机号或备注"
        clearable
        :prefix-icon="Search"
        @keyup.enter="reloadFirstPage"
      />
      <el-select v-model="query.status" placeholder="状态" @change="reloadFirstPage">
        <el-option label="未完成" value="pending" />
        <el-option label="已完成" value="completed" />
        <el-option label="逾期" value="overdue" />
        <el-option label="今日" value="today" />
      </el-select>
      <el-select v-model="query.type" clearable placeholder="类型" @change="reloadFirstPage">
        <el-option v-for="item in reminderTypes" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button :icon="Refresh" @click="reloadFirstPage">刷新</el-button>
    </section>

    <section class="reminder-list glass-panel">
      <article v-for="reminder in reminders" :key="reminder.id" :class="['reminder-row', { overdue: reminder.overdue }]">
        <div class="reminder-date">
          <strong>{{ dayText(reminder.remindDate) }}</strong>
          <span>{{ reminder.remindDate }}</span>
        </div>
        <div class="reminder-main">
          <div class="reminder-title">
            <strong>{{ reminder.contactName || '未知联系人' }}</strong>
            <el-tag :type="tagType(reminder.type)" size="small">{{ typeLabel(reminder.type) }}</el-tag>
            <el-tag v-if="reminder.overdue" type="danger" size="small">逾期</el-tag>
            <el-tag v-if="reminder.completed" type="success" size="small">已完成</el-tag>
          </div>
          <p>{{ reminder.contactPhone || '未填写手机号' }}</p>
          <small>{{ reminder.note || '暂无备注' }}</small>
        </div>
        <div class="reminder-actions">
          <el-button link type="primary" :icon="Edit" @click="openEdit(reminder)">编辑</el-button>
          <el-button link :type="reminder.completed ? 'warning' : 'success'" :icon="Check" @click="toggleComplete(reminder)">
            {{ reminder.completed ? '取消完成' : '完成' }}
          </el-button>
          <el-button link type="danger" :icon="Delete" @click="remove(reminder)">删除</el-button>
        </div>
      </article>

      <div v-if="!reminders.length" class="reminder-empty">
        <strong>暂无提醒</strong>
        <p>为联系人添加回访、纪念日或其他提醒后，会显示在这里。</p>
        <el-button type="primary" @click="openCreate">新建提醒</el-button>
      </div>

      <el-pagination
        v-if="page.total > page.size"
        class="reminder-pagination"
        background
        layout="prev, pager, next"
        :current-page="page.page"
        :page-size="page.size"
        :total="page.total"
        @current-change="changePage"
      />
    </section>

    <el-dialog v-model="formVisible" :title="editing ? '编辑提醒' : '新建提醒'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="联系人" prop="contactId">
          <el-select v-model="form.contactId" filterable placeholder="请选择联系人" style="width: 100%">
            <el-option
              v-for="contact in contacts"
              :key="contact.id"
              :label="`${contact.name} · ${contact.phone}`"
              :value="contact.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" style="width: 100%">
            <el-option v-for="item in reminderTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒日期" prop="remindDate">
          <el-date-picker v-model="form.remindDate" value-format="YYYY-MM-DD" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.note" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { api, unwrap } from '../api'

const reminderTypes = [
  { value: 'BIRTHDAY', label: '生日' },
  { value: 'FOLLOW_UP', label: '回访' },
  { value: 'ANNIVERSARY', label: '纪念日' },
  { value: 'OTHER', label: '其他' }
]

const reminders = ref([])
const contacts = ref([])
const page = ref({ total: 0, page: 1, size: 20, totalPages: 0 })
const query = reactive({ keyword: '', status: 'pending', type: '' })
const formVisible = ref(false)
const editing = ref(null)
const formRef = ref()
const form = reactive({ contactId: null, type: 'FOLLOW_UP', remindDate: '', note: '' })
const rules = {
  contactId: [{ required: true, message: '请选择联系人', trigger: 'change' }],
  type: [{ required: true, message: '请选择提醒类型', trigger: 'change' }],
  remindDate: [{ required: true, message: '请选择提醒日期', trigger: 'change' }]
}

const pendingCountText = computed(() => (query.status === 'pending' ? '默认显示未完成' : '当前筛选结果'))

onMounted(async () => {
  await Promise.all([loadContacts(), load()])
})

async function load() {
  const data = unwrap(await api.get('/reminders', {
    params: {
      page: page.value.page,
      size: page.value.size,
      keyword: query.keyword,
      status: query.status,
      type: query.type || undefined
    }
  }))
  reminders.value = data.items
  page.value = data
}

async function changePage(nextPage) {
  page.value = { ...page.value, page: nextPage }
  await load()
}

async function reloadFirstPage() {
  page.value = { ...page.value, page: 1 }
  await load()
}

async function loadContacts() {
  const data = unwrap(await api.get('/contacts', { params: { page: 1, size: 1000 } }))
  contacts.value = data.items
}

function openCreate() {
  editing.value = null
  Object.assign(form, { contactId: null, type: 'FOLLOW_UP', remindDate: '', note: '' })
  formVisible.value = true
}

function openEdit(reminder) {
  editing.value = reminder
  Object.assign(form, {
    contactId: reminder.contactId,
    type: reminder.type,
    remindDate: reminder.remindDate,
    note: reminder.note || ''
  })
  formVisible.value = true
}

async function submit() {
  await formRef.value.validate()
  if (editing.value) {
    await api.put(`/reminders/${editing.value.id}`, form)
  } else {
    await api.post('/reminders', form)
  }
  ElMessage.success('提醒已保存')
  formVisible.value = false
  await load()
}

async function toggleComplete(reminder) {
  await api.put(`/reminders/${reminder.id}/complete`, { completed: !reminder.completed })
  ElMessage.success(reminder.completed ? '已取消完成' : '已标记完成')
  await load()
}

async function remove(reminder) {
  await ElMessageBox.confirm(`确认删除 ${reminder.contactName || '该联系人'} 的提醒？`, '删除确认', { type: 'warning' })
  await api.delete(`/reminders/${reminder.id}`)
  ElMessage.success('提醒已删除')
  await load()
}

function typeLabel(type) {
  return reminderTypes.find((item) => item.value === type)?.label || '其他'
}

function tagType(type) {
  if (type === 'BIRTHDAY') return 'warning'
  if (type === 'FOLLOW_UP') return 'primary'
  if (type === 'ANNIVERSARY') return 'success'
  return 'info'
}

function dayText(dateText) {
  const today = new Date()
  const date = new Date(`${dateText}T00:00:00`)
  const diff = Math.round((date - new Date(today.getFullYear(), today.getMonth(), today.getDate())) / 86400000)
  if (diff < 0) return `逾期 ${Math.abs(diff)} 天`
  if (diff === 0) return '今天'
  if (diff === 1) return '明天'
  return `${diff} 天后`
}
</script>

<style scoped>
.reminder-page {
  display: grid;
  gap: 14px;
}

.reminder-hero,
.reminder-tools,
.reminder-list {
  padding: 18px;
}

.reminder-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.reminder-hero p,
.reminder-hero h2,
.reminder-hero span {
  margin: 0;
}

.reminder-hero p {
  color: var(--ios-blue);
  font-size: 12px;
  font-weight: 800;
}

.reminder-hero h2 {
  color: var(--ios-text);
  font-size: 28px;
}

.reminder-hero span,
.reminder-main p,
.reminder-main small,
.reminder-date span,
.reminder-empty {
  color: var(--ios-text-muted);
}

.reminder-tools {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 150px 150px auto;
  gap: 10px;
  align-items: center;
}

.reminder-search :deep(.el-input__wrapper) {
  min-height: 42px;
  border-radius: 999px;
}

.reminder-list {
  display: grid;
  min-height: 480px;
  gap: 10px;
}

.reminder-row {
  display: grid;
  grid-template-columns: 128px minmax(0, 1fr) auto;
  gap: 14px;
  align-items: center;
  border: 1px solid rgb(255 255 255 / 64%);
  border-radius: 22px;
  padding: 14px;
  background: rgb(255 255 255 / 66%);
  box-shadow: 0 10px 26px rgb(45 91 160 / 7%);
}

.reminder-row.overdue {
  border-color: rgb(255 59 48 / 24%);
  background: rgb(255 245 244 / 76%);
}

.reminder-date strong {
  display: block;
  color: var(--ios-text);
  font-size: 18px;
}

.reminder-title {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.reminder-title strong {
  color: var(--ios-text);
  font-size: 17px;
}

.reminder-main p,
.reminder-main small {
  display: block;
  margin: 5px 0 0;
}

.reminder-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.reminder-empty {
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  min-height: 320px;
  text-align: center;
}

.reminder-empty strong {
  color: var(--ios-text);
  font-size: 20px;
}

.reminder-empty p {
  margin: 0;
}

.reminder-pagination {
  justify-self: center;
  margin-top: 6px;
}

@media (max-width: 900px) {
  .reminder-tools,
  .reminder-row {
    grid-template-columns: 1fr;
  }

  .reminder-actions {
    justify-content: flex-start;
  }
}
</style>
