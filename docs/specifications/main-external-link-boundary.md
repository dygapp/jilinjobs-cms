# Main External-link Ownership & Behavior Specification

## Authority

- GitHub Issue #60 / E1
- `docs/requirements/main-external-link-boundary.md`
- `docs/project/main-site-formal-content-plan.md`
- `docs/specifications/public-site.md`

## Status

- Specification: **READY**
- Technical Planning: **NOT REQUIRED**
- Current Ready Execution Unit: **NONE**

## 1. Surface matrix

| Surface | Business owner | Public target | Opening owner | Migration owner |
|---|---|---|---|---|
| EXTERNAL_LINK Article | Article | `externalUrl` | current Article public behavior | E3 Article canonical unit |
| Navigation LINK | NavigationItem | `targetUrl` / projected `href` | Navigation `openMode` | Site Package if stable structure |
| CmsList LINK | CmsListItem | item URL | ListItem `openMode` | E3 when historical member |
| CmsList ARTICLE | CmsListItem placement + Article target | Article canonical target | current Article/List projection | E3 list placement + referenced Article |
| Advertisement | Advertisement | advertisement URL | Advertisement `openMode` | later migration only if source evidence requires |
| fixed integration | engineering Site/Public code | fixed target | engineering contract | no historical migration unless reclassified |

The same URL may legitimately appear in multiple surfaces when business placement differs; URL equality is not an identity rule.

## 2. Main Public behavior

### 2.1 Article

- Main Column and homepage Article aggregation classify `articleType=EXTERNAL_LINK` + valid `externalUrl` as an external target.
- External Article opens the source directly; current new-window behavior remains accepted.
- INTERNAL Article uses `/article/{id}`.
- No external Article detail duplication is introduced.

### 2.2 Navigation

- same-entry internal paths remain Router navigation;
- external URL and cross-entry target use document navigation;
- current `newWindow` projection controls `_blank`; new-window anchors keep `noopener noreferrer`;
- `/party/**` remains a cross-entry route, not an external business URL.

### 2.3 CmsList

- LINK item uses item-owned URL/title/openMode;
- ARTICLE item resolves the current Article target;
- ARTICLE + INTERNAL resolves the consuming Site canonical article route;
- ARTICLE + EXTERNAL_LINK resolves the Article external source URL;
- list placement does not transfer Article ownership.

### 2.4 Advertisement / fixed integration

- Advertisement uses its existing URL/openMode projection;
- fixed NCSS and other accepted engineering seams remain fixed unless future operational-maintenance evidence requires a CMS owner.

## 3. Migration classification

E3 source discovery must classify each discovered external target by **source business role**, not by URL string:

1. content record in a Column → Article canonical record with `EXTERNAL_LINK`;
2. historical operational member of stable list → ListItem canonical record;
3. stable navigation structure → Site Package Navigation, not historical dataset;
4. Fresh Site initial ordinary default → bootstrap, not historical dataset;
5. fixed integration → engineering asset;
6. unresolved role → source discovery unresolved evidence; do not silently promote.

## 4. No-gap audit

Current Repository inspection already demonstrates the required runtime primitives:

- `PublicColumnPage.vue` directly renders EXTERNAL_LINK Article anchors;
- Main home uses the same EXTERNAL_LINK semantics for news/recruitment aggregation;
- `PublicNavigation.vue` separates internal Router navigation from external/cross-entry document navigation;
- Main home CmsList carousel and Advertisement paths already honor their current target/open-mode semantics;
- Site Package stable Navigation/List definitions and Generic Article/List migration capabilities already have distinct ownership.

No additional schema, API, Admin form, frontend component or migration engine capability is required solely to satisfy E1.

## 5. Verification / closure

E1 Planning integration must verify:

- Requirement/Specification do not contradict `information-publishing.md` or `public-site.md`;
- source inspection still matches the matrix above;
- no E1-only implementation diff is required;
- E2/E3 explicitly consume this classification boundary.

If those checks remain true at final planning head, `slice-work` result for E1 is:

**NO CANDIDATE EXECUTION UNIT — E1 closes as Planning / Authority convergence.**

Any future request for external badges, leave-site confirmation, health checking, a global link object or per-Article opening controls is a new Requirement and must not be smuggled into E1.
