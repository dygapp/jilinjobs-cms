<script setup lang="ts">
import type { Component } from 'vue'

withDefaults(defineProps<{
  label: string
  icon: Component
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  disabled?: boolean
  loading?: boolean
  testid?: string
  href?: string
  target?: string
}>(), {
  type: 'primary',
  disabled: false,
  loading: false,
  target: '_blank',
})

defineEmits<{ click: [event: MouseEvent] }>()
</script>

<template>
  <el-tooltip :content="label" placement="top" :show-after="250">
    <span class="admin-icon-action-wrap">
      <el-link
        v-if="href"
        class="admin-icon-link"
        :aria-label="label"
        :data-testid="testid"
        :href="href"
        :target="target"
        :type="type"
        :disabled="disabled"
      >
        <el-icon><component :is="icon" /></el-icon>
      </el-link>
      <el-button
        v-else
        class="admin-icon-action"
        :aria-label="label"
        :data-testid="testid"
        :type="type"
        link
        :disabled="disabled"
        :loading="loading"
        @click="$emit('click', $event)"
      >
        <el-icon v-if="!loading"><component :is="icon" /></el-icon>
      </el-button>
    </span>
  </el-tooltip>
</template>
