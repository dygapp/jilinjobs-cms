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
const publicSecurityIcon = '/static/footer/public-security-record.png'
const publicInstitutionBadge = '/static/footer/public-institution.png'
const wechatQr = '/static/footer/wechat-qr.png'

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
    <div class="party-width party-footer-layout">
      <div class="party-footer-main">
        <p>办公地址：{{ address }}，吉林省高等学校毕业生就业指导中心2楼一站式办公大厅，邮编：130033。</p>
        <p>公交线路：乘坐120路、227路、20路、130路、281路、154路、190路、125路公交北方市场下车，北方市场南门南行200米。</p>
        <p>业务咨询电话：{{ phone }}；办公时间：{{ officeHours }}</p>
        <p><strong>{{ copyright }}</strong></p>
        <p class="party-footer-records">
          <a
            class="party-public-security-record"
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

      <aside class="party-footer-badges" aria-label="网站官方信息">
        <div class="party-public-institution-badge">
          <img :src="publicInstitutionBadge" alt="事业单位">
        </div>
        <div class="party-wechat-entry">
          <img :src="wechatQr" alt="吉林省大学生就业创业微信公众号二维码">
          <span>吉林省大学生就业创业</span>
        </div>
      </aside>
    </div>
  </footer>
</template>

<style scoped>
.party-footer-layout {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 48px;
  width: min(1200px, calc(100% - 32px));
  padding: 27px 0;
  text-align: left;
  font-size: 12px;
  line-height: 1.75;
}

.party-footer-main {
  min-width: 0;
  flex: 1;
}

.party-footer-main p {
  margin: 2px 0;
}

.party-footer-main strong {
  font-weight: 700;
}

.party-footer-records {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.party-public-security-record {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #fff;
  text-decoration: none;
}

.party-public-security-record img {
  display: block;
  width: 20px;
  height: 20px;
  flex: none;
  object-fit: contain;
}

.party-footer-badges {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 26px;
  flex: 0 0 auto;
  min-width: 250px;
}

.party-public-institution-badge {
  display: flex;
  align-items: center;
  justify-content: center;
}

.party-public-institution-badge img {
  display: block;
  width: 96px;
  height: 96px;
  object-fit: contain;
}

.party-wechat-entry {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  color: #fff;
  font-size: 12px;
  line-height: 1.4;
  white-space: nowrap;
}

.party-wechat-entry img {
  display: block;
  width: 92px;
  height: 92px;
  object-fit: contain;
  background: #fff;
  padding: 3px;
}

@media (max-width: 900px) {
  .party-footer-layout {
    align-items: flex-start;
    gap: 24px;
  }

  .party-footer-badges {
    min-width: 190px;
    gap: 14px;
  }

  .party-public-institution-badge img,
  .party-wechat-entry img {
    width: 76px;
    height: 76px;
  }

  .party-wechat-entry span {
    display: none;
  }
}

@media (max-width: 760px) {
  .party-footer-layout {
    display: block;
    width: calc(100% - 24px);
  }

  .party-footer-badges {
    justify-content: flex-start;
    margin-top: 16px;
    min-width: 0;
  }

  .party-footer-records {
    gap: 8px 12px;
  }
}
</style>
