<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listPublicSiteConfig } from '../api/siteConfig'

const values = ref<Record<string, string>>({})
onMounted(async () => {
  values.value = Object.fromEntries((await listPublicSiteConfig()).map(item => [item.key, item.value]))
})
</script>

<template>
  <footer class="site-footer">
    <div class="site-width">
      <p>办公地址：{{ values.CONTACT_ADDRESS }}，吉林省高等学校毕业生就业指导中心2楼一站式办公大厅，邮编：130033。</p>
      <p>公交线路：乘坐120路、227路、20路、130路、281路、154路、190路、125路公交北方市场下车，北方市场南门南行200米。</p>
      <p>业务咨询电话：{{ values.CONTACT_PHONE }}；办公时间：{{ values.OFFICE_HOURS }}</p>
      <p><strong>{{ values.FOOTER_COPYRIGHT }}</strong></p>
      <p><a href="https://www.beian.gov.cn/portal/registerSystemInfo?recordcode=22010702000243" target="_blank" rel="noopener noreferrer">吉公网安备 22010702000243号</a>　{{ values.ICP_NUMBER }}</p>
    </div>
  </footer>
</template>
