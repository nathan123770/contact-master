<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑联系人' : '新建联系人'"
    width="760px"
    class="ios-contact-dialog"
    @close="$emit('update:modelValue', false)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <section class="form-section avatar-uploader">
        <button class="avatar-preview ios-avatar" type="button" aria-label="上传头像" @click="fileInput?.click()">
          <img v-if="form.avatarData" :src="form.avatarData" alt="联系人头像" />
          <span v-else>{{ avatarInitial }}</span>
        </button>
        <div>
          <strong>联系人头像</strong>
          <p>选择本地图片，保存后会跟随联系人资料保留。</p>
          <div class="avatar-actions">
            <el-button size="small" @click="fileInput?.click()">上传头像</el-button>
            <el-button size="small" :disabled="!form.avatarData" @click="form.avatarData = ''">移除</el-button>
          </div>
          <input ref="fileInput" class="visually-hidden-file" type="file" accept="image/*" @change="handleAvatarChange" />
        </div>
      </section>

      <section class="form-section">
        <h3>基础信息</h3>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" inputmode="tel" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="name@example.com" inputmode="email" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分组">
              <el-select v-model="form.groupId" clearable placeholder="请选择分组" style="width: 100%">
                <el-option v-for="group in groups" :key="group.id" :label="group.name" :value="group.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </section>

      <section class="form-section">
        <h3>工作与提醒</h3>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="公司">
              <el-input v-model="form.company" placeholder="请输入公司" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位">
              <el-input v-model="form.position" placeholder="请输入职位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生日">
              <el-date-picker v-model="form.birthday" value-format="YYYY-MM-DD" type="date" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收藏">
              <el-switch v-model="form.favorite" active-text="收藏" inactive-text="普通" />
            </el-form-item>
          </el-col>
        </el-row>
      </section>

      <section class="form-section">
        <h3>补充信息</h3>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="补充说明" />
        </el-form-item>
      </section>
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
const fileInput = ref()
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
  avatarData: '',
  favorite: false
})
const form = reactive(blank())
const avatarInitial = computed(() => form.name?.trim()?.slice(0, 1) || '人')
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

async function handleAvatarChange(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.size > 3 * 1024 * 1024) {
    ElMessage.warning('头像图片请小于 3MB')
    return
  }
  form.avatarData = await imageToAvatarData(file)
}

function imageToAvatarData(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onerror = reject
    reader.onload = () => {
      const img = new Image()
      img.onerror = reject
      img.onload = () => {
        const size = 320
        const canvas = document.createElement('canvas')
        const context = canvas.getContext('2d')
        canvas.width = size
        canvas.height = size
        const scale = Math.max(size / img.width, size / img.height)
        const width = img.width * scale
        const height = img.height * scale
        context.drawImage(img, (size - width) / 2, (size - height) / 2, width, height)
        resolve(canvas.toDataURL('image/jpeg', 0.86))
      }
      img.src = reader.result
    }
    reader.readAsDataURL(file)
  })
}

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

<style scoped>
.form-section {
  border: 1px solid rgb(255 255 255 / 70%);
  border-radius: 22px;
  padding: 16px;
  background: rgb(255 255 255 / 54%);
  box-shadow: inset 0 1px 0 rgb(255 255 255 / 84%);
}

.form-section + .form-section {
  margin-top: 14px;
}

.form-section h3 {
  margin: 0 0 14px;
  color: var(--ios-text);
  font-size: 16px;
}

.avatar-uploader {
  display: flex;
  gap: 16px;
  align-items: center;
}

.avatar-preview {
  width: 86px;
  height: 86px;
  aspect-ratio: 1;
  border-radius: 50%;
  padding: 0;
  font-size: 30px;
  cursor: pointer;
}

.avatar-preview img {
  border-radius: 50%;
}

.avatar-uploader p {
  margin: 4px 0 10px;
  color: var(--ios-text-muted);
  font-size: 13px;
}

.avatar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.visually-hidden-file {
  display: none;
}

@media (max-width: 760px) {
  .avatar-uploader {
    align-items: flex-start;
  }

  :deep(.el-col) {
    max-width: 100%;
    flex: 0 0 100%;
  }
}
</style>
