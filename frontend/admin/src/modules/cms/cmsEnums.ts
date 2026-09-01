export const contentImagePolicyOptions = [
  { value: 'NONE', label: '无' },
  { value: 'OPTIONAL', label: '可选' },
  { value: 'REQUIRED', label: '必填' },
] as const

export type ContentImagePolicy = typeof contentImagePolicyOptions[number]['value']

export function contentImagePolicyLabel(policy: ContentImagePolicy) {
  return contentImagePolicyOptions.find(option => option.value === policy)?.label ?? policy
}
