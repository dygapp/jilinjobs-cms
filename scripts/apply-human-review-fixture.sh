#!/usr/bin/env bash
set -euo pipefail

base="${REVIEW_BASE_URL:-http://127.0.0.1:5173}"
columns="$(curl --fail --silent "$base/api/admin/columns")"
notice_id="$(echo "$columns" | jq -r '.[] | select(.alias == "notice") | .id' | head -1)"
employment_id="$(echo "$columns" | jq -r '.[] | select(.alias == "employment-news") | .id' | head -1)"
recruitment_id="$(echo "$columns" | jq -r '.[] | select(.alias == "recruitment-announcement") | .id' | head -1)"
test -n "$notice_id"
test -n "$employment_id"
test -n "$recruitment_id"

create_article() {
  local column_id="$1" title="$2" published="$3" source="${4:-Human Admin Review Fixture}"
  local payload article_id
  payload="$(jq -n --argjson columnId "$column_id" --arg title "$title" --arg published "$published" --arg source "$source"     '{columnId:$columnId,title:$title,bodyHtml:"<p>人工评审示例内容，用于验证编辑、发布与公开展示闭环。</p>",source:$source,publishDate:$published,pinned:false,recommended:false,sortOrder:0,coverResourceId:null,bodyImageResourceIds:[],attachmentResourceIds:[]}')"
  article_id="$(curl --fail --silent -H 'Content-Type: application/json' -d "$payload" "$base/api/admin/articles" | jq -r '.id')"
  curl --fail --silent -X POST "$base/api/admin/articles/$article_id/publish" >/dev/null
}

create_external_article() {
  local column_id="$1" title="$2" published="$3" url="$4" source="${5:-外部招聘信息源}"
  local payload article_id
  payload="$(jq -n --argjson columnId "$column_id" --arg title "$title" --arg published "$published" --arg url "$url" --arg source "$source"     '{columnId:$columnId,title:$title,bodyHtml:"",source:$source,articleType:"EXTERNAL_LINK",externalUrl:$url,publishDate:$published,pinned:false,recommended:false,sortOrder:0,coverResourceId:null,bodyImageResourceIds:[],attachmentResourceIds:[]}')"
  article_id="$(curl --fail --silent -H 'Content-Type: application/json' -d "$payload" "$base/api/admin/articles" | jq -r '.id')"
  curl --fail --silent -X POST "$base/api/admin/articles/$article_id/publish" >/dev/null
}

create_article "$notice_id" '管理端人工评审：就业服务工作通知' '2026-08-28'
create_article "$notice_id" '管理端人工评审：毕业生就业手续办理提示' '2026-08-26'
create_article "$employment_id" '管理端人工评审：访企拓岗促就业专项行动' '2026-08-27'
create_external_article "$recruitment_id" '管理端人工评审：高校毕业生专场招聘公告' '2026-08-28' 'https://example.com/review/recruitment-1'
create_external_article "$recruitment_id" '管理端人工评审：重点单位招聘公告' '2026-08-24' 'https://example.com/review/recruitment-2'

home_list_id="$(curl --fail --silent "$base/api/admin/lists" | jq --exit-status '.[] | select(.code == "HOME_CAROUSEL") | .id')"
for position in 1 2; do
  payload="$(jq -n --argjson position "$position"     '{sourceType:"LINK",articleId:null,title:("主站轮播人工评审示例 " + ($position|tostring)),subtitle:null,url:"/column/notice",imagePath:(if $position == 1 then "/static/home/carousel-01.jpg" else "/static/home/recruitment-campaign.png" end),imageResourceId:null,openMode:"_self",sortOrder:(-100 + $position),enabled:true,extraJson:null}')"
  curl --fail --silent -H 'Content-Type: application/json' -d "$payload" "$base/api/admin/lists/$home_list_id/items"     | jq --exit-status '.id > 0' >/dev/null
done
