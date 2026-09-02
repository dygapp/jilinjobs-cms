import { inject, provide, ref, type Ref } from 'vue'
import { listPublicNavigations, type PublicNavigation } from '../api/navigation'
import { listPublicSiteConfig } from '../api/siteConfig'

export const MAIN_SITE_CONFIG_KEYS = {
  SITE_NAME: 'SITE_NAME',
  PLATFORM_LOGO_ICON_PATH: 'PLATFORM_LOGO_ICON_PATH',
  PLATFORM_LOGO_TEXT_PATH: 'PLATFORM_LOGO_TEXT_PATH',
  LEGACY_LOGO_PATH: 'LOGO_PATH',
  HEADER_BANNER_PATH: 'HEADER_BANNER_PATH',
  CONTACT_ADDRESS: 'CONTACT_ADDRESS',
  CONTACT_PHONE: 'CONTACT_PHONE',
  OFFICE_HOURS: 'OFFICE_HOURS',
  FOOTER_COPYRIGHT: 'FOOTER_COPYRIGHT',
  ICP_NUMBER: 'ICP_NUMBER',
  CAROUSEL_INTERVAL_SECONDS: 'HOME_CAROUSEL_INTERVAL_SECONDS',
} as const

type MainSiteContext = {
  navigation: Ref<PublicNavigation[]>
  config: Ref<Record<string, string>>
  loading: Ref<boolean>
  error: Ref<string>
  ready: Promise<void>
}

const mainSiteContextKey = Symbol('main-site-context')

function createMainSiteContext(): MainSiteContext {
  const navigation = ref<PublicNavigation[]>([])
  const config = ref<Record<string, string>>({})
  const loading = ref(true)
  const error = ref('')

  const ready = Promise.all([listPublicNavigations(), listPublicSiteConfig()])
    .then(([navigationItems, properties]) => {
      navigation.value = navigationItems
      config.value = Object.fromEntries(properties.map(item => [item.key, item.value]))
    })
    .catch(cause => {
      error.value = cause instanceof Error ? cause.message : '站点公共配置加载失败'
    })
    .finally(() => {
      loading.value = false
    })

  return { navigation, config, loading, error, ready }
}

export function provideMainSiteContext() {
  const context = createMainSiteContext()
  provide(mainSiteContextKey, context)
  return context
}

export function useMainSiteContext() {
  const context = inject<MainSiteContext>(mainSiteContextKey)
  if (!context) throw new Error('Main Site Context 尚未初始化')
  return context
}
