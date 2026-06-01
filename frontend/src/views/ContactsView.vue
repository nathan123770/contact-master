<template>
  <div class="ios-contact-shell">
    <header class="ios-contact-hero glass-panel">
      <div class="ios-contact-title">
        <div class="ios-profile-avatar ios-avatar">{{ userInitial }}</div>
        <div>
          <p>联系人中心</p>
          <h2>通讯录</h2>
          <span>{{ allContacts.length }} 位联系人 · {{ groups.length }} 个分组</span>
        </div>
      </div>
      <div class="ios-contact-primary-actions">
        <el-button :icon="Refresh" circle aria-label="刷新联系人" @click="loadAll" />
        <el-button type="primary" :icon="Plus" circle aria-label="新增联系人" @click="openCreate" />
      </div>
    </header>

    <section class="ios-contact-tools glass-panel">
      <el-input
        v-model="query.keyword"
        class="ios-search"
        placeholder="搜索姓名、手机、邮箱、公司"
        clearable
        :prefix-icon="Search"
      />
      <div class="ios-tool-buttons">
        <el-button :icon="Plus" @click="openGroupCreate">新建分组</el-button>
        <el-upload :show-file-list="false" accept=".csv" :http-request="importCsv">
          <el-button :icon="Upload">导入 CSV</el-button>
        </el-upload>
        <el-button :icon="Download" @click="downloadTemplate">模板</el-button>
        <el-button :icon="Download" @click="exportCsv">导出</el-button>
        <el-button type="danger" plain :disabled="!selectedIds.length" :icon="Delete" @click="batchDelete">
          批量删除 {{ selectedIds.length || '' }}
        </el-button>
      </div>
    </section>

    <nav class="ios-contact-tabs glass-panel" aria-label="通讯录分类">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        :class="['ios-tab', { active: activeTab === tab.key }]"
        type="button"
        @click="activeTab = tab.key"
      >
        <strong>{{ tab.label }}</strong>
        <span>{{ tab.count }}</span>
      </button>
    </nav>

    <main class="ios-contact-body glass-panel">
      <template v-if="activeTab === 'groups'">
        <section v-for="section in groupSections" :key="section.id" class="ios-group-section">
          <button class="ios-group-row" type="button" @click="toggleGroup(section.id)" @contextmenu.prevent="openGroupMenu($event, section.group)">
            <span :class="['ios-disclosure', { open: isExpanded(section.id) }]"></span>
            <span class="ios-group-name">{{ section.name }}</span>
            <span class="ios-group-count">{{ section.visibleContacts.length }}/{{ section.totalCount }}</span>
          </button>

          <div class="ios-group-hint">
            <span>右键分组可重命名或删除</span>
          </div>

          <div v-if="isExpanded(section.id)" class="ios-contact-list">
            <ContactRow
              v-for="contact in section.visibleContacts"
              :key="contact.id"
              :contact="contact"
              :selected="isSelected(contact.id)"
              @toggle-selected="toggleSelected(contact.id, $event)"
              @detail="showDetail(contact)"
              @edit="openEdit(contact)"
              @favorite="toggleFavorite(contact)"
              @remove="remove(contact)"
            />
            <p v-if="!section.visibleContacts.length" class="ios-empty-inline">该分组暂无匹配联系人</p>
          </div>
        </section>

        <section v-if="ungroupedSection.totalCount" class="ios-group-section">
          <button class="ios-group-row" type="button" @click="toggleGroup(UNGROUPED_KEY)">
            <span :class="['ios-disclosure', { open: isExpanded(UNGROUPED_KEY) }]"></span>
            <span class="ios-group-name">未分组</span>
            <span class="ios-group-count">{{ ungroupedSection.visibleContacts.length }}/{{ ungroupedSection.totalCount }}</span>
          </button>
          <div v-if="isExpanded(UNGROUPED_KEY)" class="ios-contact-list">
            <ContactRow
              v-for="contact in ungroupedSection.visibleContacts"
              :key="contact.id"
              :contact="contact"
              :selected="isSelected(contact.id)"
              @toggle-selected="toggleSelected(contact.id, $event)"
              @detail="showDetail(contact)"
              @edit="openEdit(contact)"
              @favorite="toggleFavorite(contact)"
              @remove="remove(contact)"
            />
            <p v-if="!ungroupedSection.visibleContacts.length" class="ios-empty-inline">暂无匹配联系人</p>
          </div>
        </section>
      </template>

      <section v-else class="ios-flat-list">
        <ContactRow
          v-for="contact in currentTabContacts"
          :key="contact.id"
          :contact="contact"
          :selected="isSelected(contact.id)"
          @toggle-selected="toggleSelected(contact.id, $event)"
          @detail="showDetail(contact)"
          @edit="openEdit(contact)"
          @favorite="toggleFavorite(contact)"
          @remove="remove(contact)"
        />
        <div v-if="!currentTabContacts.length" class="ios-empty-state">
          <strong>{{ emptyTitle }}</strong>
          <p>换个关键词试试，或添加一位新联系人。</p>
          <el-button type="primary" @click="openCreate">添加联系人</el-button>
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

    <div v-if="groupMenu.visible" class="ios-context-menu" :style="groupMenuStyle" @click.stop>
      <button type="button" @click="renameFromMenu">重命名</button>
      <button type="button" class="danger" @click="deleteFromMenu">删除</button>
    </div>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElButton, ElCheckbox, ElMessage, ElMessageBox, ElTag } from 'element-plus'
import { Delete, Download, Edit, Plus, Refresh, Search, Star, Upload } from '@element-plus/icons-vue'
import { api, unwrap } from '../api'
import ContactForm from '../components/ContactForm.vue'
import ContactDetail from '../components/ContactDetail.vue'

const UNGROUPED_KEY = 'ungrouped'

const ContactRow = defineComponent({
  name: 'ContactRow',
  props: {
    contact: { type: Object, required: true },
    selected: { type: Boolean, default: false }
  },
  emits: ['toggle-selected', 'detail', 'edit', 'favorite', 'remove'],
  setup(props, { emit }) {
    return () =>
      h('article', { class: 'ios-contact-row', onClick: () => emit('detail') }, [
        h(ElCheckbox, {
          class: 'ios-contact-check',
          modelValue: props.selected,
          'aria-label': `选择 ${props.contact.name || '联系人'}`,
          'onUpdate:modelValue': (value) => emit('toggle-selected', value),
          onClick: (event) => event.stopPropagation()
        }),
        h('div', { class: 'ios-row-avatar ios-avatar' }, [
          props.contact.avatarData
            ? h('img', { src: props.contact.avatarData, alt: `${props.contact.name || '联系人'}头像` })
            : h('span', props.contact.name?.slice(0, 1) || '?')
        ]),
        h('div', { class: 'ios-contact-info' }, [
          h('div', { class: 'ios-contact-name-line' }, [
            h('strong', props.contact.name),
            props.contact.favorite ? h(ElTag, { size: 'small', type: 'warning' }, () => '收藏') : null
          ]),
          h('small', [props.contact.phone || '未填写手机号', props.contact.company ? ` · ${props.contact.company}` : ''])
        ]),
        h('div', { class: 'ios-row-actions' }, [
          h(
            ElButton,
            {
              link: true,
              type: props.contact.favorite ? 'warning' : 'info',
              icon: Star,
              'aria-label': props.contact.favorite ? '取消收藏' : '收藏',
              onClick: (event) => {
                event.stopPropagation()
                emit('favorite')
              }
            },
            () => (props.contact.favorite ? '取消' : '收藏')
          ),
          h(ElButton, {
            link: true,
            type: 'primary',
            icon: Edit,
            'aria-label': '编辑联系人',
            onClick: (event) => {
              event.stopPropagation()
              emit('edit')
            }
          }),
          h(ElButton, {
            link: true,
            type: 'danger',
            icon: Delete,
            'aria-label': '删除联系人',
            onClick: (event) => {
              event.stopPropagation()
              emit('remove')
            }
          })
        ])
      ])
  }
})

const allContacts = ref([])
const groups = ref([])
const selectedIdSet = ref(new Set())
const formVisible = ref(false)
const detailVisible = ref(false)
const importVisible = ref(false)
const groupVisible = ref(false)
const groupEditing = ref(null)
const groupMenu = ref({ visible: false, x: 0, y: 0, group: null })
const groupName = ref('')
const importResult = ref({ successCount: 0, failureCount: 0, failures: [] })
const editing = ref(null)
const initialGroupId = ref(null)
const detail = ref(null)
const activeTab = ref('groups')
const expandedGroupIds = ref(new Set([UNGROUPED_KEY]))
const query = ref({ keyword: '' })

const userInitial = computed(() => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  return user?.username?.slice(0, 1)?.toUpperCase() || 'CM'
})

const normalizedKeyword = computed(() => query.value.keyword.trim().toLowerCase())
const filteredContacts = computed(() => allContacts.value.filter(matchesKeyword))
const favoriteContacts = computed(() => allContacts.value.filter((contact) => contact.favorite))
const rawUngroupedContacts = computed(() => allContacts.value.filter((contact) => !contact.groupId))

const tabs = computed(() => [
  { key: 'groups', label: '分组', count: groups.value.length },
  { key: 'friends', label: '全部', count: allContacts.value.length },
  { key: 'favorites', label: '收藏', count: favoriteContacts.value.length },
  { key: 'ungrouped', label: '未分组', count: rawUngroupedContacts.value.length }
])

const groupSections = computed(() =>
  groups.value.map((group) => {
    const groupContacts = allContacts.value.filter((contact) => contact.groupId === group.id)
    return {
      id: group.id,
      group,
      name: group.name,
      totalCount: groupContacts.length,
      visibleContacts: groupContacts.filter(matchesKeyword)
    }
  })
)

const ungroupedSection = computed(() => ({
  totalCount: rawUngroupedContacts.value.length,
  visibleContacts: rawUngroupedContacts.value.filter(matchesKeyword)
}))

const currentTabContacts = computed(() => {
  if (activeTab.value === 'favorites') return favoriteContacts.value.filter(matchesKeyword)
  if (activeTab.value === 'ungrouped') return rawUngroupedContacts.value.filter(matchesKeyword)
  return filteredContacts.value
})

const emptyTitle = computed(() => {
  if (activeTab.value === 'favorites') return '还没有收藏联系人'
  if (activeTab.value === 'ungrouped') return '没有未分组联系人'
  return '没有匹配的联系人'
})

const selectedIds = computed(() => Array.from(selectedIdSet.value))
const groupMenuStyle = computed(() => ({
  left: `${groupMenu.value.x}px`,
  top: `${groupMenu.value.y}px`
}))

onMounted(() => {
  loadAll()
  window.addEventListener('click', closeGroupMenu)
  window.addEventListener('keydown', handleGlobalKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('click', closeGroupMenu)
  window.removeEventListener('keydown', handleGlobalKeydown)
})

async function loadAll() {
  await loadGroups()
  await refreshContacts()
}

async function refreshContacts() {
  const data = unwrap(await api.get('/contacts', { params: { page: 1, size: 1000 } }))
  allContacts.value = data.items
  pruneSelection()
}

async function loadGroups() {
  groups.value = unwrap(await api.get('/groups'))
  expandedGroupIds.value = new Set([...expandedGroupIds.value, ...groups.value.map((group) => group.id)])
}

function matchesKeyword(contact) {
  if (!normalizedKeyword.value) return true
  const target = [contact.name, contact.phone, contact.email, contact.groupName, contact.company, contact.position]
    .filter(Boolean)
    .join(' ')
    .toLowerCase()
  return target.includes(normalizedKeyword.value)
}

function isExpanded(groupId) {
  return expandedGroupIds.value.has(groupId)
}

function toggleGroup(groupId) {
  const next = new Set(expandedGroupIds.value)
  if (next.has(groupId)) next.delete(groupId)
  else next.add(groupId)
  expandedGroupIds.value = next
}

function isSelected(contactId) {
  return selectedIdSet.value.has(contactId)
}

function toggleSelected(contactId, checked) {
  const next = new Set(selectedIdSet.value)
  if (checked) next.add(contactId)
  else next.delete(contactId)
  selectedIdSet.value = next
}

function pruneSelection() {
  const validIds = new Set(allContacts.value.map((contact) => contact.id))
  selectedIdSet.value = new Set(selectedIds.value.filter((id) => validIds.has(id)))
}

function openCreate() {
  editing.value = null
  initialGroupId.value = null
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
  await loadAll()
}

async function toggleFavorite(row) {
  await api.put(`/contacts/${row.id}/favorite`)
  ElMessage.success('收藏状态已更新')
  await refreshContacts()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除联系人“${row.name}”？删除后可在回收站恢复。`, '删除确认', { type: 'warning' })
  await api.delete(`/contacts/${row.id}`)
  ElMessage.success('已移入回收站')
  await refreshContacts()
}

async function batchDelete() {
  await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 位联系人？`, '批量删除确认', { type: 'warning' })
  await api.delete('/contacts/batch', { data: { ids: selectedIds.value } })
  ElMessage.success('已批量移入回收站')
  selectedIdSet.value = new Set()
  await refreshContacts()
}

function openGroupCreate() {
  groupEditing.value = null
  groupName.value = ''
  groupVisible.value = true
}

function openGroupEdit(group) {
  closeGroupMenu()
  groupEditing.value = group
  groupName.value = group.name
  groupVisible.value = true
}

function openGroupMenu(event, group) {
  groupMenu.value = {
    visible: true,
    x: Math.min(event.clientX, window.innerWidth - 150),
    y: Math.min(event.clientY, window.innerHeight - 104),
    group
  }
}

function closeGroupMenu() {
  groupMenu.value = { visible: false, x: 0, y: 0, group: null }
}

function handleGlobalKeydown(event) {
  if (event.key === 'Escape') closeGroupMenu()
}

function renameFromMenu() {
  if (!groupMenu.value.group) return
  openGroupEdit(groupMenu.value.group)
}

async function deleteFromMenu() {
  if (!groupMenu.value.group) return
  const group = groupMenu.value.group
  closeGroupMenu()
  await removeGroup(group)
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
  await loadAll()
}

async function removeGroup(group) {
  await ElMessageBox.confirm(`确认删除分组“${group.name}”？分组下有联系人时不能删除。`, '删除确认', { type: 'warning' })
  await api.delete(`/groups/${group.id}`)
  ElMessage.success('分组已删除')
  await loadAll()
}

async function importCsv({ file }) {
  const form = new FormData()
  form.append('file', file)
  const result = unwrap(await api.post('/contacts/import', form))
  importResult.value = result
  importVisible.value = true
  ElMessage.success(`导入完成：成功 ${result.successCount} 条，失败 ${result.failureCount} 条`)
  await loadAll()
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

<style scoped>
.ios-contact-shell {
  display: grid;
  gap: 14px;
}

.ios-contact-hero,
.ios-contact-tools,
.ios-contact-tabs,
.ios-contact-body {
  position: relative;
}

.ios-contact-hero {
  display: flex;
  min-height: 96px;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 22px;
}

.ios-contact-title {
  display: flex;
  min-width: 0;
  gap: 14px;
  align-items: center;
}

.ios-profile-avatar {
  width: 58px;
  height: 58px;
  border-radius: 21px;
  font-size: 19px;
}

.ios-contact-title p,
.ios-contact-title h2,
.ios-contact-title span {
  margin: 0;
}

.ios-contact-title p {
  color: var(--ios-blue);
  font-size: 12px;
  font-weight: 800;
}

.ios-contact-title h2 {
  color: var(--ios-text);
  font-size: 28px;
  line-height: 1.1;
}

.ios-contact-title span {
  display: block;
  margin-top: 5px;
  color: var(--ios-text-muted);
  font-size: 13px;
}

.ios-contact-primary-actions {
  display: flex;
  gap: 10px;
}

.ios-contact-tools {
  display: grid;
  grid-template-columns: minmax(240px, 420px) minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  padding: 14px;
}

.ios-search :deep(.el-input__wrapper) {
  min-height: 44px;
  border-radius: 999px;
}

.ios-tool-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

.ios-contact-tabs {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  padding: 8px;
}

.ios-tab {
  display: grid;
  min-height: 56px;
  border: 0;
  border-radius: 20px;
  padding: 8px 10px;
  color: var(--ios-text-muted);
  text-align: left;
  background: transparent;
  cursor: pointer;
}

.ios-tab strong,
.ios-tab span {
  display: block;
}

.ios-tab strong {
  font-size: 14px;
}

.ios-tab span {
  color: var(--ios-text-subtle);
  font-size: 12px;
}

.ios-tab.active {
  color: var(--ios-text);
  background: rgb(255 255 255 / 76%);
  box-shadow: inset 0 1px 0 rgb(255 255 255 / 92%), 0 10px 24px rgb(45 91 160 / 10%);
}

.ios-contact-body {
  min-height: 520px;
  padding: 10px;
}

.ios-group-section {
  overflow: hidden;
  border-radius: 24px;
  background: rgb(255 255 255 / 48%);
  box-shadow: inset 0 1px 0 rgb(255 255 255 / 76%);
}

.ios-group-section + .ios-group-section {
  margin-top: 10px;
}

.ios-group-row {
  display: grid;
  width: 100%;
  min-height: 56px;
  grid-template-columns: 20px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  border: 0;
  padding: 0 16px;
  color: var(--ios-text);
  font: inherit;
  font-size: 17px;
  font-weight: 720;
  text-align: left;
  background: transparent;
  cursor: pointer;
}

.ios-group-row:hover,
.ios-contact-row:hover {
  background: rgb(255 255 255 / 62%);
}

.ios-disclosure {
  width: 9px;
  height: 9px;
  border-right: 2px solid var(--ios-text-muted);
  border-bottom: 2px solid var(--ios-text-muted);
  transform: rotate(-45deg);
}

.ios-disclosure.open {
  transform: rotate(45deg);
}

.ios-group-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ios-group-count,
.ios-contact-info small,
.ios-empty-inline,
.ios-empty-state {
  color: var(--ios-text-muted);
}

.ios-group-count {
  font-size: 13px;
  font-weight: 650;
}

.ios-group-hint {
  margin: -6px 16px 10px 48px;
  color: var(--ios-text-subtle);
  font-size: 12px;
  opacity: 0;
  pointer-events: none;
}

.ios-group-section:hover .ios-group-hint {
  opacity: 1;
}

.ios-contact-list,
.ios-flat-list {
  display: grid;
  gap: 8px;
}

.ios-contact-row {
  display: grid;
  min-height: 70px;
  grid-template-columns: 28px 46px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  margin: 0 8px 8px;
  border: 1px solid rgb(255 255 255 / 64%);
  border-radius: 22px;
  padding: 10px 12px;
  background: rgb(255 255 255 / 66%);
  box-shadow: 0 10px 26px rgb(45 91 160 / 7%);
  cursor: pointer;
}

.ios-flat-list .ios-contact-row {
  margin: 0;
}

.ios-contact-check {
  min-width: 20px;
}

.ios-row-avatar {
  width: 46px;
  height: 46px;
  border-radius: 999px;
  font-size: 18px;
}

.ios-row-avatar img {
  border-radius: 999px;
}

.ios-context-menu {
  position: fixed;
  z-index: 1000;
  display: grid;
  min-width: 132px;
  overflow: hidden;
  border: 1px solid rgb(255 255 255 / 74%);
  border-radius: 16px;
  padding: 6px;
  background: rgb(255 255 255 / 84%);
  box-shadow: 0 18px 44px rgb(45 91 160 / 18%);
  backdrop-filter: var(--ios-blur);
  -webkit-backdrop-filter: var(--ios-blur);
}

.ios-context-menu button {
  min-height: 36px;
  border: 0;
  border-radius: 11px;
  padding: 0 12px;
  color: var(--ios-text);
  font: inherit;
  font-size: 14px;
  font-weight: 650;
  text-align: left;
  background: transparent;
  cursor: pointer;
}

.ios-context-menu button:hover {
  background: rgb(0 122 255 / 10%);
}

.ios-context-menu button.danger {
  color: var(--ios-red);
}

.ios-contact-info {
  min-width: 0;
}

.ios-contact-name-line {
  display: flex;
  min-width: 0;
  gap: 8px;
  align-items: center;
}

.ios-contact-name-line strong {
  overflow: hidden;
  color: var(--ios-text);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ios-contact-info small {
  display: block;
  overflow: hidden;
  margin-top: 4px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ios-row-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
  justify-content: flex-end;
}

.ios-empty-inline {
  margin: 4px 18px 16px 48px;
}

.ios-empty-state {
  display: grid;
  min-height: 280px;
  place-items: center;
  align-content: center;
  gap: 10px;
  padding: 24px;
  text-align: center;
}

.ios-empty-state strong {
  color: var(--ios-text);
  font-size: 20px;
}

.ios-empty-state p {
  margin: 0;
}

.failure-table {
  margin-top: 14px;
}

@media (max-width: 900px) {
  .ios-contact-tools {
    grid-template-columns: 1fr;
  }

  .ios-tool-buttons {
    justify-content: flex-start;
  }
}

@media (max-width: 760px) {
  .ios-contact-hero {
    min-height: 82px;
    padding: 16px;
  }

  .ios-profile-avatar {
    width: 50px;
    height: 50px;
  }

  .ios-contact-title h2 {
    font-size: 24px;
  }

  .ios-contact-tabs {
    overflow-x: auto;
    grid-template-columns: repeat(4, minmax(104px, 1fr));
    scrollbar-width: none;
  }

  .ios-contact-tabs::-webkit-scrollbar {
    display: none;
  }

  .ios-tool-buttons .el-button,
  .ios-tool-buttons :deep(.el-upload) {
    flex: 1 1 auto;
  }

  .ios-contact-row,
  .ios-flat-list .ios-contact-row {
    grid-template-columns: 24px 42px minmax(0, 1fr);
  }

  .ios-row-avatar {
    width: 42px;
    height: 42px;
  }

  .ios-row-actions {
    grid-column: 3;
    justify-content: flex-start;
  }
}
</style>
