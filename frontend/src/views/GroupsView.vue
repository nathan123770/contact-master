<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">分组管理</h2>
      <el-button type="primary" @click="openCreate">新增分组</el-button>
    </div>

    <section class="panel">
      <el-table :data="groups">
        <el-table-column prop="name" label="分组名称" min-width="160" />
        <el-table-column prop="contactCount" label="联系人数量" width="140" />
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row)">重命名</el-button>
            <el-button text type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="visible" :title="editing ? '重命名分组' : '新增分组'" width="420px">
      <el-form label-position="top">
        <el-form-item label="分组名称">
          <el-input v-model="name" maxlength="40" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api, unwrap } from '../api'

const groups = ref([])
const visible = ref(false)
const editing = ref(null)
const name = ref('')

onMounted(load)

async function load() {
  groups.value = unwrap(await api.get('/groups'))
}

function openCreate() {
  editing.value = null
  name.value = ''
  visible.value = true
}

function openEdit(row) {
  editing.value = row
  name.value = row.name
  visible.value = true
}

async function save() {
  if (!name.value.trim()) {
    ElMessage.warning('请输入分组名称')
    return
  }
  if (editing.value) {
    await api.put(`/groups/${editing.value.id}`, { name: name.value })
  } else {
    await api.post('/groups', { name: name.value })
  }
  ElMessage.success('保存成功')
  visible.value = false
  await load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除分组「${row.name}」？`, '删除确认', { type: 'warning' })
  await api.delete(`/groups/${row.id}`)
  ElMessage.success('删除成功')
  await load()
}
</script>
