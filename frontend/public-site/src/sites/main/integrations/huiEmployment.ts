const HUI_EMPLOYMENT_BASE_URL = 'https://student.hjiuye.com'
const HUI_EMPLOYMENT_PAGE_BASE_URL = `${HUI_EMPLOYMENT_BASE_URL}/moreActivities/2685`

export const HUI_EMPLOYMENT_HOME_TARGETS = Object.freeze({
  calendar: {
    title: '就业日历',
    url: `${HUI_EMPLOYMENT_BASE_URL}/calendarEd/2685`,
  },
  latestRecruitment: {
    title: '最新招聘',
    url: `${HUI_EMPLOYMENT_BASE_URL}/college/2685/4`,
  },
  liveCourses: {
    title: '直播课程',
    url: `${HUI_EMPLOYMENT_BASE_URL}/college/2685/5`,
  },
} as const)

export const HUI_EMPLOYMENT_PAGE_TARGETS = Object.freeze({
  HUI_EMPLOYMENT_POSITIONS: {
    title: '在招职位',
    url: `${HUI_EMPLOYMENT_PAGE_BASE_URL}/4/22`,
    height: 1300,
  },
  HUI_EMPLOYMENT_RECRUITMENT: {
    title: '招聘简章',
    url: `${HUI_EMPLOYMENT_PAGE_BASE_URL}/4/23`,
    height: 1300,
  },
  HUI_EMPLOYMENT_JOB_FAIR: {
    title: '双选会',
    url: `${HUI_EMPLOYMENT_PAGE_BASE_URL}/4/24`,
    height: 1300,
  },
  HUI_EMPLOYMENT_PRESENTATION: {
    title: '现场宣讲',
    url: `${HUI_EMPLOYMENT_PAGE_BASE_URL}/4/25`,
    height: 1300,
  },
  HUI_EMPLOYMENT_JILIN: {
    title: '留省就业',
    url: `${HUI_EMPLOYMENT_PAGE_BASE_URL}/4/30`,
    height: 1300,
  },
  HUI_EMPLOYMENT_LIVE_COURSES: {
    title: '直播课程',
    url: `${HUI_EMPLOYMENT_PAGE_BASE_URL}/5/27`,
    height: 1250,
  },
} as const)

export type HuiEmploymentPageRendererKey = keyof typeof HUI_EMPLOYMENT_PAGE_TARGETS

export function isHuiEmploymentPageRenderer(rendererKey: string): rendererKey is HuiEmploymentPageRendererKey {
  return rendererKey in HUI_EMPLOYMENT_PAGE_TARGETS
}

export function resolveHuiEmploymentPageTarget(rendererKey: string) {
  return isHuiEmploymentPageRenderer(rendererKey) ? HUI_EMPLOYMENT_PAGE_TARGETS[rendererKey] : null
}
