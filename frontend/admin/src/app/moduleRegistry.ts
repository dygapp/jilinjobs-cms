import type { RouteRecordRaw } from 'vue-router'
import type { AdminModule } from './adminModule'
import { cmsAdminModule } from '../modules/cms/module'

const modules: AdminModule[] = [cmsAdminModule]

function registerPath(path: string, routePaths: Set<string>) {
  if (routePaths.has(path)) {
    throw new Error(`Admin module route path duplicated: ${path}`)
  }
  routePaths.add(path)
}

function validateAdminModules(values: AdminModule[]) {
  const moduleIds = new Set<string>()
  const routePaths = new Set<string>()
  const defaultModules = values.filter(module => module.defaultEntry)

  if (defaultModules.length !== 1) {
    throw new Error(`Admin module registry requires exactly one default entry, received ${defaultModules.length}`)
  }

  for (const module of values) {
    if (!/^[a-z][a-z0-9-]*$/.test(module.id)) {
      throw new Error(`Admin module id must use lowercase kebab-case: ${module.id}`)
    }
    if (moduleIds.has(module.id)) {
      throw new Error(`Admin module id duplicated: ${module.id}`)
    }
    moduleIds.add(module.id)

    const namespace = `/${module.id}/`
    const canonicalPaths = new Set(module.routes.map(route => route.path))

    if (!module.landingRoute.startsWith(namespace) || !canonicalPaths.has(module.landingRoute)) {
      throw new Error(`Admin module landing route must be a canonical route in ${namespace}: ${module.landingRoute}`)
    }

    for (const route of module.routes) {
      if (!route.path.startsWith(namespace)) {
        throw new Error(`Admin module canonical route must stay in ${namespace}: ${route.path}`)
      }
      registerPath(route.path, routePaths)
    }

    for (const section of module.navigationSections) {
      for (const item of section.items) {
        if (!item.to.startsWith(namespace) || !canonicalPaths.has(item.to)) {
          throw new Error(`Admin module navigation must point to a canonical route in ${namespace}: ${item.to}`)
        }
      }
    }

    for (const route of module.compatibilityRoutes ?? []) {
      registerPath(route.path, routePaths)
    }
  }
}

validateAdminModules(modules)

export const adminModules: readonly AdminModule[] = modules
export const adminDefaultRoute = modules.find(module => module.defaultEntry)!.landingRoute
export const adminNavigationSections = modules.flatMap(module => module.navigationSections)
export const adminModuleRoutes: RouteRecordRaw[] = modules.flatMap(module => [
  ...module.routes,
  ...(module.compatibilityRoutes ?? []),
])
