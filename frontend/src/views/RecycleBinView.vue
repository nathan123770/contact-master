<template>
  <div class="recycle-page">
    <div class="toolbar">
      <div>
        <h2 class="page-title">回收站</h2>
        <span class="muted">这里展示软删除的联系人，可恢复或彻底删除。</span>
      </div>
    </div>

    <section class="panel">
      <el-table class="desktop-table" :data="contacts">
        <el-table-column prop="name" label="姓名" min-width="140" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="groupName" label="分组" width="120" />
        <el-table-column prop="deletedAt" label="删除时间" min-width="180" />
        <el-table-column label="操作" width="190">
          <template #default="{ row }">
            <el-button text type="primary" @click="restore(row)">恢复</el-button>
            <el-button text type="danger" @click="permanentDelete(row)">彻底删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mobile-card-list panel-body">
        <el-card v-for="row in contacts" :key="row.id" shadow="never">
          <strong>{{ row.name }}</strong>
          <p>{{ row.phone }} · {{ row.deletedAt }}</p>
          <div class="mobile-actions">
            <el-button size="small" type="primary" @click="restore(row)">恢复</el-button>
            <el-button size="small" type="danger" plain @click="permanentDelete(row)">彻底删除</el-button>
          </div>
        </el-card>
        <el-empty v-if="!contacts.length" description="回收站为空" />
      </div>

      <div class="pagination">
        <el-pagination
          layout="prev, pager, next, total"
          :total="total"
          :page-size="query.size"
          v-model:current-page="query.page"
          @current-change="load"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api, unwrap } from '../api'

const contacts = ref([])
const total = ref(0)
const query = ref({ page: 1, size: 10 })

onMounted(load)

async function load() {
  const data = unwrap(await api.get('/contacts/recycle-bin', { params: query.value }))
  contacts.value = data.items
  total.value = data.total
}

async function restore(row) {
  await api.put(`/contacts/${row.id}/restore`)
  ElMessage.success('恢复成功')
  await load()
}

async function permanentDelete(row) {
  await ElMessageBox.confirm(`彻底删除后无法恢复，确认删除“${row.name}”？`, '彻底删除确认', { type: 'warning' })
  await api.delete(`/contacts/${row.id}/permanent`)
  ElMessage.success('已彻底删除')
  await load()
}
</script>

<style scoped>
.recycle-page {
  display: grid;
  gap: 14px;
}

.mobile-card-list p {
  margin: 8px 0 0;
  color: var(--ios-text-muted);
}

.mobile-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}
</style>
