<template>
  <div class="reminder-page">
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
      <el-button type="primary" :icon="Plus" @click="openCreate">新建提醒</el-button>
    </section>

    <section class="reminder-list glass-panel">
      <article v-for="reminder in reminders" :key="reminder.id" :class="['reminder-row', { overdue: reminder.overdue }]">
        <div :class="['reminder-kind', tagClass(reminder.type)]">
          <el-icon><component :is="typeIcon(reminder.type)" /></el-icon>
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
        <div class="reminder-time">
          <strong>{{ dayText(reminder.remindAt) }}</strong>
          <span>{{ timeText(reminder.remindAt) }}</span>
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
        <el-form-item label="提醒时间" prop="remindAt">
          <el-date-picker
            v-model="form.remindAt"
            value-format="YYYY-MM-DDTHH:mm:ss"
            format="YYYY-MM-DD HH:mm"
            type="datetime"
            style="width: 100%"
          />
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Calendar, Check, Delete, Edit, Flag, PhoneFilled, Plus, Refresh, Search } from '@element-plus/icons-vue'
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
const form = reactive({ contactId: null, type: 'FOLLOW_UP', remindAt: '', note: '' })
const rules = {
  contactId: [{ required: true, message: '请选择联系人', trigger: 'change' }],
  type: [{ required: true, message: '请选择提醒类型', trigger: 'change' }],
  remindAt: [{ required: true, message: '请选择提醒时间', trigger: 'change' }]
}

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
  Object.assign(form, { contactId: null, type: 'FOLLOW_UP', remindAt: '', note: '' })
  formVisible.value = true
}

function openEdit(reminder) {
  editing.value = reminder
  Object.assign(form, {
    contactId: reminder.contactId,
    type: reminder.type,
    remindAt: reminder.remindAt || (reminder.remindDate ? `${reminder.remindDate}T09:00:00` : ''),
    note: reminder.note || ''
  })
  formVisible.value = true
}

async function submit() {
  await formRef.value.validate()
  const payload = {
    contactId: form.contactId,
    type: form.type,
    remindDate: form.remindAt?.slice(0, 10),
    remindAt: normalizeRemindAt(form.remindAt),
    note: form.note
  }
  if (editing.value) {
    await api.put(`/reminders/${editing.value.id}`, payload)
  } else {
    await api.post('/reminders', payload)
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

function typeIcon(type) {
  if (type === 'BIRTHDAY') return Calendar
  if (type === 'FOLLOW_UP') return PhoneFilled
  if (type === 'ANNIVERSARY') return Flag
  return Bell
}

function tagClass(type) {
  return `type-${String(type || 'OTHER').toLowerCase()}`
}

function normalizeRemindAt(value) {
  if (!value) return value
  return value.length === 16 ? `${value}:00` : value
}

function reminderDateValue(value) {
  if (!value) return null
  const normalized = value.includes('T') ? value : `${value}T09:00:00`
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date
}

function dayText(dateText) {
  const date = reminderDateValue(dateText)
  if (!date) return '未定时间'
  const today = new Date()
  const dateDay = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const todayDay = new Date(today.getFullYear(), today.getMonth(), today.getDate())
  const diff = Math.round((dateDay - todayDay) / 86400000)
  if (diff < 0) return `逾期 ${Math.abs(diff)} 天`
  if (diff === 0) return '今天'
  if (diff === 1) return '明天'
  return `${diff} 天后`
}

function timeText(dateText) {
  const date = reminderDateValue(dateText)
  if (!date) return ''
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  })
}
</script>

<style scoped>
.reminder-page {
  display: grid;
  gap: 14px;
}

.reminder-tools,
.reminder-list {
  padding: 18px;
}

.reminder-main p,
.reminder-main small,
.reminder-date span,
.reminder-empty {
  color: var(--ios-text-muted);
}

.reminder-tools {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 150px 150px auto auto;
  gap: 10px;
  align-items: center;
}

.reminder-search :deep(.el-input__wrapper) {
  min-height: 42px;
  border-radius: 999px;
}

.reminder-list {
  display: grid;
  min-height: 420px;
  align-content: start;
  gap: 10px;
}

.reminder-row {
  display: grid;
  grid-template-columns: 50px minmax(0, 1fr) 128px auto;
  gap: 12px;
  align-items: center;
  border: 1px solid rgb(255 255 255 / 64%);
  border-radius: 24px;
  padding: 12px 14px;
  background:
    linear-gradient(135deg, rgb(255 255 255 / 78%), rgb(255 255 255 / 46%)),
    rgb(255 255 255 / 62%);
  box-shadow:
    0 12px 30px rgb(45 91 160 / 8%),
    inset 0 1px 0 rgb(255 255 255 / 86%);
}

.reminder-row.overdue {
  border-color: rgb(255 59 48 / 24%);
  background: rgb(255 245 244 / 76%);
}

.reminder-kind {
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  border: 1px solid rgb(255 255 255 / 74%);
  border-radius: 16px;
  color: #fff;
  background: linear-gradient(145deg, #0a84ff, #5e5ce6);
  box-shadow:
    0 14px 28px rgb(0 122 255 / 18%),
    inset 0 1px 0 rgb(255 255 255 / 42%);
}

.reminder-kind.type_birthday,
.reminder-kind.type-birthday {
  background: linear-gradient(145deg, #ff9f0a, #ff6b35);
}

.reminder-kind.type_anniversary,
.reminder-kind.type-anniversary {
  background: linear-gradient(145deg, #34c759, #00a878);
}

.reminder-kind.type_other,
.reminder-kind.type-other {
  background: linear-gradient(145deg, #8e8e93, #5e5ce6);
}

.reminder-time {
  justify-self: end;
  min-width: 108px;
  border-radius: 18px;
  padding: 9px 12px;
  color: var(--ios-text);
  text-align: right;
  background: rgb(255 255 255 / 62%);
}

.reminder-time strong {
  display: block;
  color: var(--ios-text);
  font-size: 15px;
}

.reminder-time span {
  display: block;
  margin-top: 3px;
  color: var(--ios-text-muted);
  font-size: 12px;
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
  gap: 2px;
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

  .reminder-time {
    justify-self: stretch;
    text-align: left;
  }

  .reminder-actions {
    justify-content: flex-start;
  }
}
</style>
