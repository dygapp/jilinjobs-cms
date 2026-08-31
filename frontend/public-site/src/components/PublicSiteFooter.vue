<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { listPublicSiteConfig } from '../api/siteConfig'

const values = ref<Record<string, string>>({})
const publicSecurityIcon = '/static/footer/public-security-record.png'
const publicInstitutionBadge = '/static/footer/public-institution.png'
const wechatQr = '/static/footer/wechat-qr.png'
const copyrightText = computed(() =>
  values.value.FOOTER_COPYRIGHT || 'Copyright 版权所有 吉林省高等学校毕业生就业指导中心 All Rights Reserved',
)

onMounted(async () => {
  values.value = Object.fromEntries((await listPublicSiteConfig()).map(item => [item.key, item.value]))
})
</script>

<template>
  <footer class="site-footer">
    <div class="site-width site-footer-layout">
      <div class="site-footer-main">
        <p>办公地址：{{ values.CONTACT_ADDRESS }}，吉林省高等学校毕业生就业指导中心2楼一站式办公大厅，邮编：130033。</p>
        <p>公交线路：乘坐120路、227路、20路、130路、281路、154路、190路、125路公交北方市场下车，北方市场南门南行200米。</p>
        <p>业务咨询电话：{{ values.CONTACT_PHONE }}；办公时间：{{ values.OFFICE_HOURS }}</p>
        <p><strong>{{ copyrightText }}</strong></p>
        <p class="site-footer-records">
          <a
            class="public-security-record"
            href="https://www.beian.gov.cn/portal/registerSystemInfo?recordcode=22010702000243"
            target="_blank"
            rel="noopener noreferrer"
          >
            <img :src="publicSecurityIcon" alt="" width="20" height="20">
            <span>吉公网安备 22010702000243号</span>
          </a>
          <span>{{ values.ICP_NUMBER }}</span>
        </p>
      </div>

      <aside class="site-footer-badges" aria-label="网站官方信息">
        <div class="public-institution-badge">
          <img :src="publicInstitutionBadge" alt="事业单位">
        </div>
        <div class="wechat-entry">
          <img :src="wechatQr" alt="吉林省大学生就业创业微信公众号二维码">
          <span>吉林省大学生就业创业</span>
        </div>
      </aside>
    </div>
  </footer>
</template>
