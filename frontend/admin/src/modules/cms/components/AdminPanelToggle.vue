<script setup lang="ts">
import { Expand, Fold } from '@element-plus/icons-vue'

const props = defineProps<{
  collapsed: boolean
  label: string
}>()

defineEmits<{ toggle: [] }>()
</script>

<template>
  <el-tooltip :content="`${props.collapsed ? '展开' : '收起'}${props.label}`" placement="top" :show-after="250">
    <el-button
      class="admin-panel-toggle"
      text
      circle
      :aria-label="`${props.collapsed ? '展开' : '收起'}${props.label}`"
      :icon="props.collapsed ? Expand : Fold"
      @click="$emit('toggle')"
    />
  </el-tooltip>
</template>

<style scoped>
.admin-panel-toggle{flex:none}
</style>
