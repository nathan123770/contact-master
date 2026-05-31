<template>
  <div class="contact-book-layout">
    <aside class="contact-book-sidebar">
      <div class="contact-book-header">
        <div>
          <strong>通讯录分组</strong>
          <p>{{ allContacts.length }} 位联系人</p>
        </div>
        <el-button size="small" type="primary" @click="openGroupCreate">新建</el-button>
      </div>

      <div class="contact-book-search">
        <el-input v-model="query.keyword" placeholder="搜索联系人" clearable @keyup.enter="load" @clear="load" />
      </div>

      <div class="contact-book-list">
        <div
          :class="['contact-book-group', { active: query.groupId === null }]"
          role="button"
          tabindex="0"
          @click="selectGroup(null)"
        >
          <span class="contact-book-dot all"></span>
          <span class="contact-book-group-name">全部联系人</span>
          <span class="contact-book-count">{{ allContacts.length }}</span>
        </div>

        <div v-for="group in groups" :key="group.id" class="contact-book-section">
          <div
            :class="['contact-book-group', { active: query.groupId === group.id }]"
            role="button"
            tabindex="0"
            @click="selectGroup(group.id)"
          >
            <span class="contact-book-dot"></span>
            <span class="contact-book-group-name">{{ group.name }}</span>
            <span class="contact-book-count">{{ contactsByGroup[group.id]?.length || 0 }}</span>
          </div>

          <div class="contact-book-actions">
            <el-button link type="primary" size="small" @click="openGroupEdit(group)">重命名</el-button>
            <el-button link type="danger" size="small" @click="removeGroup(group)">删除</el-button>
          </div>

          <div
            v-for="contact in contactsByGroup[group.id]"
            :key="contact.id"
            class="contact-book-person"
            role="button"
            tabindex="0"
            @click="showDetail(contact)"
          >
            <span class="contact-book-avatar">{{ contact.name?.slice(0, 1) }}</span>
            <span class="contact-book-person-text">
              <strong>{{ contact.name }}</strong>
              <small>{{ contact.phone }}</small>
            </span>
          </div>
        </div>

        <div v-if="ungroupedContacts.length" class="contact-book-section">
          <div
            :class="['contact-book-group', { active: query.groupId === 0 }]"
            role="button"
            tabindex="0"
            @click="selectGroup(0)"
          >
            <span class="contact-book-dot muted"></span>
            <span class="contact-book-group-name">未分组</span>
            <span class="contact-book-count">{{ ungroupedContacts.length }}</span>
          </div>

          <div
            v-for="contact in ungroupedContacts"
            :key="contact.id"
            class="contact-book-person"
            role="button"
            tabindex="0"
            @click="showDetail(contact)"
          >
            <span class="contact-book-avatar">{{ contact.name?.slice(0, 1) }}</span>
            <span class="contact-book-person-text">
              <strong>{{ contact.name }}</strong>
              <small>{{ contact.phone }}</small>
            </span>
          </div>
        </div>
      </div>
    </aside>

    <main class="contact-book-main">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="query.favorite" placeholder="收藏状态" clearable style="width: 140px" @change="load">
            <el-option label="只看收藏" :value="true" />
            <el-option label="非收藏" :value="false" />
          </el-select>
          <el-button @click="load">刷新</el-button>
        </div>
        <div class="toolbar-right">
          <el-upload :show-file-list="false" accept=".csv" :http-request="importCsv">
            <el-button>导入 CSV</el-button>
          </el-upload>
          <el-button @click="downloadTemplate">下载模板</el-button>
          <el-button @click="exportCsv">导出 CSV</el-button>
          <el-button type="danger" plain :disabled="!selected.length" @click="batchDelete">批量删除</el-button>
          <el-button type="primary" @click="openCreate">添加联系人</el-button>
        </div>
      </div>

      <section class="panel">
        <div class="contact-list-header">
          <div>
            <h2>{{ currentGroupName }}</h2>
            <p>当前显示 {{ total }} 位联系人</p>
          </div>
          <el-tag v-if="query.favorite === true" type="warning">收藏筛选</el-tag>
        </div>

        <el-table class="desktop-table" :data="contacts" @selection-change="selected = $event" row-key="id">
          <el-table-column type="selection" width="44" />
          <el-table-column label="姓名" min-width="150">
            <template #default="{ row }">
              <span class="contact-name">
                {{ row.name }}
                <el-tag v-if="row.favorite" size="small" type="warning">收藏</el-tag>
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" min-width="130" />
          <el-table-column prop="email" label="邮箱" min-width="170" />
          <el-table-column prop="groupName" label="分组" width="110" />
          <el-table-column prop="company" label="公司" min-width="140" />
          <el-table-column label="操作" width="260" fixed="right">
            <template #default="{ row }">
              <el-button text type="primary" @click="showDetail(row)">详情</el-button>
              <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button text type="warning" @click="toggleFavorite(row)">{{ row.favorite ? '取消收藏' : '收藏' }}</el-button>
              <el-button text type="danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="mobile-card-list panel-body">
          <el-card v-for="row in contacts" :key="row.id" shadow="never">
            <div class="mobile-card-head">
              <strong>{{ row.name }}</strong>
              <el-tag v-if="row.favorite" size="small" type="warning">收藏</el-tag>
            </div>
            <p>{{ row.phone }}</p>
            <p>{{ row.company || '未填写公司' }} · {{ row.groupName || '未分组' }}</p>
            <div class="mobile-actions">
              <el-button size="small" @click="showDetail(row)">详情</el-button>
              <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="remove(row)">删除</el-button>
            </div>
          </el-card>
        </div>

        <div class="pagination">
          <el-pagination layout="prev, pager, next, total" :total="total" :page-size="query.size" v-model:current-page="query.page" @current-change="load" />
        </div>
      </section>
    </main>

    <ContactForm v-model="formVisible" :contact="editing" :initial-group-id="initialGroupId" :groups="groups" @saved="afterSaved" />
    <ContactDetail v-model="detailVisible" :contact="detail" />

    <el-dialog v-model="groupVisible" :title="groupEditing ? '重命名分组' : '新增分组'" width="420px">
      <el-form label-position="top">
        <el-form-item label="分组名称">
          <el-input v-model="groupName" maxlength="40" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="groupVisible = false">取消</el-button>
        <el-button type="primary" @click="saveGroup">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importVisible" title="导入结果" width="760px">
      <el-alert
        :title="`成功 ${importResult.successCount} 条，失败 ${importResult.failureCount} 条`"
        :type="importResult.failureCount ? 'warning' : 'success'"
        show-icon
        :closable="false"
      />
      <el-table v-if="importResult.failures?.length" :data="importResult.failures" class="failure-table">
        <el-table-column prop="row" label="行号" width="80" />
        <el-table-column prop="reason" label="失败原因" width="180" />
        <el-table-column prop="raw" label="原始内容" min-width="260" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api, unwrap } from '../api'
import ContactForm from '../components/ContactForm.vue'
import ContactDetail from '../components/ContactDetail.vue'

const contacts = ref([])
const allContacts = ref([])
const total = ref(0)
const groups = ref([])
const selected = ref([])
const formVisible = ref(false)
const detailVisible = ref(false)
const importVisible = ref(false)
const groupVisible = ref(false)
const groupEditing = ref(null)
const groupName = ref('')
const importResult = ref({ successCount: 0, failureCount: 0, failures: [] })
const editing = ref(null)
const initialGroupId = ref(null)
const detail = ref(null)
const query = ref({ keyword: '', groupId: null, favorite: null, page: 1, size: 10 })

const contactsByGroup = computed(() => {
  const result = {}
  for (const group of groups.value) result[group.id] = []
  for (const contact of allContacts.value) {
    if (contact.groupId && result[contact.groupId]) result[contact.groupId].push(contact)
  }
  return result
})

const ungroupedContacts = computed(() => allContacts.value.filter((contact) => !contact.groupId))

const currentGroupName = computed(() => {
  if (query.value.groupId === null) return '全部联系人'
  if (query.value.groupId === 0) return '未分组'
  return groups.value.find((group) => group.id === query.value.groupId)?.name || '联系人'
})

onMounted(async () => {
  await loadGroups()
  await load()
})

async function load() {
  if (query.value.groupId === 0) {
    const data = unwrap(await api.get('/contacts', { params: { keyword: query.value.keyword, favorite: query.value.favorite, page: 1, size: 1000 } }))
    allContacts.value = data.items
    contacts.value = data.items.filter((item) => !item.groupId)
    total.value = contacts.value.length
    return
  }
  const data = unwrap(await api.get('/contacts', { params: query.value }))
  contacts.value = data.items
  total.value = data.total
  await refreshGroupContacts()
}

async function refreshGroupContacts() {
  const data = unwrap(await api.get('/contacts', { params: { page: 1, size: 1000 } }))
  allContacts.value = data.items
}

async function loadGroups() {
  groups.value = unwrap(await api.get('/groups'))
}

async function selectGroup(groupId) {
  query.value.groupId = groupId
  query.value.page = 1
  await load()
}

function openCreate() {
  editing.value = null
  initialGroupId.value = query.value.groupId && query.value.groupId !== 0 ? query.value.groupId : null
  formVisible.value = true
}

function openEdit(row) {
  editing.value = { ...row }
  initialGroupId.value = null
  formVisible.value = true
}

function showDetail(row) {
  detail.value = row
  detailVisible.value = true
}

async function afterSaved() {
  await loadGroups()
  await load()
}

async function toggleFavorite(row) {
  await api.put(`/contacts/${row.id}/favorite`)
  ElMessage.success('收藏状态已更新')
  await load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除联系人「${row.name}」？删除后可在回收站恢复。`, '删除确认', { type: 'warning' })
  await api.delete(`/contacts/${row.id}`)
  ElMessage.success('已移入回收站')
  await load()
}

async function batchDelete() {
  await ElMessageBox.confirm(`确认删除选中的 ${selected.value.length} 位联系人？`, '批量删除确认', { type: 'warning' })
  await api.delete('/contacts/batch', { data: { ids: selected.value.map((item) => item.id) } })
  ElMessage.success('已批量移入回收站')
  await load()
}

function openGroupCreate() {
  groupEditing.value = null
  groupName.value = ''
  groupVisible.value = true
}

function openGroupEdit(group) {
  groupEditing.value = group
  groupName.value = group.name
  groupVisible.value = true
}

async function saveGroup() {
  const name = groupName.value.trim()
  if (!name) {
    ElMessage.warning('请输入分组名称')
    return
  }
  if (groupEditing.value) {
    await api.put(`/groups/${groupEditing.value.id}`, { name })
  } else {
    await api.post('/groups', { name })
  }
  ElMessage.success('分组已保存')
  groupVisible.value = false
  await loadGroups()
  await load()
}

async function removeGroup(group) {
  await ElMessageBox.confirm(`确认删除分组「${group.name}」？该分组下有联系人时不能删除。`, '删除确认', { type: 'warning' })
  await api.delete(`/groups/${group.id}`)
  ElMessage.success('分组已删除')
  if (query.value.groupId === group.id) query.value.groupId = null
  await loadGroups()
  await load()
}

async function importCsv({ file }) {
  const form = new FormData()
  form.append('file', file)
  const result = unwrap(await api.post('/contacts/import', form))
  importResult.value = result
  importVisible.value = true
  ElMessage.success(`导入完成：成功 ${result.successCount} 条，失败 ${result.failureCount} 条`)
  await loadGroups()
  await load()
}

function downloadTemplate() {
  const header = 'name,phone,email,group,company,position,address,birthday,remark,favorite\n'
  const sample = '张三,13800138000,zhangsan@example.com,默认分组,示例公司,工程师,上海市,2000-06-03,重要客户,Y\n'
  downloadBlob(new Blob([header + sample], { type: 'text/csv;charset=utf-8' }), 'contacts-template.csv')
}

async function exportCsv() {
  const response = await api.get('/contacts/export', {
    params: { keyword: query.value.keyword },
    responseType: 'blob'
  })
  downloadBlob(response.data, 'contacts.csv')
}

function downloadBlob(blob, fileName) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.click()
  URL.revokeObjectURL(url)
}
</script>
