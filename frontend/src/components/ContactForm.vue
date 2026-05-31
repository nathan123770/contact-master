<template>
  <el-dialog :model-value="modelValue" :title="isEdit ? '编辑联系人' : '新增联系人'" width="720px" @close="$emit('update:modelValue', false)">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="姓名" prop="name"><el-input v-model="form.name" placeholder="请输入姓名" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" placeholder="请输入手机号" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" placeholder="name@example.com" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="分组">
            <el-select v-model="form.groupId" clearable placeholder="请选择分组" style="width: 100%">
              <el-option v-for="group in groups" :key="group.id" :label="group.name" :value="group.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="公司"><el-input v-model="form.company" placeholder="请输入公司" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="职位"><el-input v-model="form.position" placeholder="请输入职位" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="生日"><el-date-picker v-model="form.birthday" value-format="YYYY-MM-DD" type="date" style="width: 100%" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="收藏"><el-switch v-model="form.favorite" active-text="收藏" inactive-text="普通" /></el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="地址"><el-input v-model="form.address" placeholder="请输入地址" /></el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" placeholder="补充说明" /></el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'

const props = defineProps({
  modelValue: Boolean,
  contact: Object,
  initialGroupId: Number,
  groups: { type: Array, default: () => [] }
})
const emit = defineEmits(['update:modelValue', 'saved'])

const formRef = ref()
const isEdit = computed(() => Boolean(props.contact?.id))
const blank = () => ({
  groupId: props.initialGroupId || null,
  name: '',
  phone: '',
  email: '',
  company: '',
  position: '',
  address: '',
  birthday: '',
  remark: '',
  favorite: false
})
const form = reactive(blank())
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

watch(
  () => props.modelValue,
  (visible) => {
    if (!visible) return
    Object.assign(form, blank(), props.contact?.id ? props.contact : {})
  }
)

async function submit() {
  await formRef.value.validate()
  if (isEdit.value) {
    await api.put(`/contacts/${props.contact.id}`, form)
  } else {
    await api.post('/contacts', form)
  }
  ElMessage.success('保存成功')
  emit('update:modelValue', false)
  emit('saved')
}
</script>
