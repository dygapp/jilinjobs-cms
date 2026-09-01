import { cmsAdminModule } from '../modules/cms/module'

export const adminModules = [cmsAdminModule]
export const adminNavigationSections = adminModules.flatMap(module => module.navigationSections)
export const adminModuleRoutes = adminModules.flatMap(module => module.routes)
