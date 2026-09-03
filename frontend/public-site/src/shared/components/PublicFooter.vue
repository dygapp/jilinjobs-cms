<script setup lang="ts">
import { computed } from 'vue'
import '../styles/public-shell.css'

const props = withDefaults(defineProps<{
  config: Record<string, string>
  theme: 'main' | 'party'
  testId?: string
}>(), {
  testId: undefined,
})

const publicSecurityIcon = '/static/footer/public-security-record.png'
const publicInstitutionBadge = '/static/footer/public-institution.png'
const wechatQr = '/static/footer/wechat-qr.png'
const defaultCopyright = 'Copyright 版权所有 吉林省高等学校毕业生就业指导中心 All Rights Reserved'

const address = computed(() => props.config.CONTACT_ADDRESS || '')
const phone = computed(() => props.config.CONTACT_PHONE || '')
const officeHours = computed(() => props.config.OFFICE_HOURS || '')
const copyright = computed(() => props.config.FOOTER_COPYRIGHT || defaultCopyright)
const icp = computed(() => props.config.ICP_NUMBER || '')
const legacyClass = computed(() => props.theme === 'main' ? 'site-footer' : 'party-footer')
</script>

<template>
  <footer
    class="shared-public-footer"
    :class="[legacyClass, `shared-public-footer--${theme}`]"
    data-component="public-footer"
    :data-theme="theme"
    :data-testid="testId"
  >
    <div class="shared-public-shell-width shared-public-footer-layout site-footer-layout">
      <div class="shared-public-footer-main">
        <p>办公地址：{{ address }}，吉林省高等学校毕业生就业指导中心2楼一站式办公大厅，邮编：130033。</p>
        <p>公交线路：乘坐120路、227路、20路、130路、281路、154路、190路、125路公交北方市场下车，北方市场南门南行200米。</p>
        <p>业务咨询电话：{{ phone }}；办公时间：{{ officeHours }}</p>
        <p><strong>{{ copyright }}</strong></p>
        <p class="shared-public-footer-records">
          <a
            class="shared-public-security-record public-security-record"
            href="https://www.beian.gov.cn/portal/registerSystemInfo?recordcode=22010702000243"
            target="_blank"
            rel="noopener noreferrer"
          >
            <img :src="publicSecurityIcon" alt="" width="20" height="20">
            <span>吉公网安备 22010702000243号</span>
          </a>
          <span>{{ icp }}</span>
        </p>
      </div>

      <aside class="shared-public-footer-badges" aria-label="网站官方信息">
        <div class="shared-public-institution-badge public-institution-badge">
          <img :src="publicInstitutionBadge" alt="事业单位">
        </div>
        <div class="shared-public-wechat-entry wechat-entry">
          <img :src="wechatQr" alt="吉林省大学生就业创业微信公众号二维码">
          <span>吉林省大学生就业创业</span>
        </div>
      </aside>
    </div>
  </footer>
</template>
