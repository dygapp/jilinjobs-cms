<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { listPublicSiteConfig, type SiteConfigItem } from '../../../shared/api/siteConfig'

const items = ref<SiteConfigItem[]>([])
const config = computed(() => Object.fromEntries(items.value.filter(item => item.enabled).map(item => [item.key, item.value])))
const address = computed(() => config.value.CONTACT_ADDRESS || '')
const phone = computed(() => config.value.CONTACT_PHONE || '')
const officeHours = computed(() => config.value.OFFICE_HOURS || '')
const copyright = computed(() => config.value.FOOTER_COPYRIGHT || 'Copyright 版权所有 吉林省高等学校毕业生就业指导中心 All Rights Reserved')
const icp = computed(() => config.value.ICP_NUMBER || '')

onMounted(async () => {
  try {
    items.value = await listPublicSiteConfig()
  } catch {
    items.value = []
  }
})
</script>

<template>
  <footer class="party-footer" data-testid="party-building-footer">
    <div class="party-width party-footer-inner">
      <p>办公地址：{{ address }}，吉林省高等学校毕业生就业指导中心2楼一站式办公大厅，邮编：130033。</p>
      <p>业务咨询电话：{{ phone }}；办公时间：{{ officeHours }}</p>
      <p><strong>{{ copyright }}</strong></p>
      <p class="party-footer-records">
        <a href="https://www.beian.gov.cn/portal/registerSystemInfo?recordcode=22010702000243" target="_blank" rel="noopener noreferrer">吉公网安备 22010702000243号</a>
        <span>{{ icp }}</span>
      </p>
    </div>
  </footer>
</template>
