import { getPublicColumnByAlias, type PublicColumn } from '../../../shared/api/columns'

export const PARTY_PARENT_ALIAS = 'party-building' as const
export const PARTY_HOME_CAROUSEL_CODE = 'PARTY_HOME_CAROUSEL' as const
export const PARTY_COLUMN_ALIASES = ['party-voice', 'party-work', 'party-rules', 'party-study'] as const
export type PartyColumnAlias = typeof PARTY_COLUMN_ALIASES[number]

let columnsPromise: Promise<Record<PartyColumnAlias, PublicColumn>> | null = null

export function isPartyColumnAlias(value: unknown): value is PartyColumnAlias {
  return typeof value === 'string' && (PARTY_COLUMN_ALIASES as readonly string[]).includes(value)
}

export function loadPartyColumns(): Promise<Record<PartyColumnAlias, PublicColumn>> {
  if (!columnsPromise) {
    columnsPromise = Promise.all(PARTY_COLUMN_ALIASES.map(alias => getPublicColumnByAlias(alias)))
      .then(columns => Object.fromEntries(columns.map(column => [column.alias, column])) as Record<PartyColumnAlias, PublicColumn>)
      .catch(error => {
        columnsPromise = null
        throw error
      })
  }
  return columnsPromise
}

export async function getPartyColumn(alias: PartyColumnAlias): Promise<PublicColumn> {
  return (await loadPartyColumns())[alias]
}
