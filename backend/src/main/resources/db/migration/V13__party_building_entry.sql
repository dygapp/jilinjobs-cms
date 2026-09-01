UPDATE cms_navigation
SET target_type = 'LINK',
    target_column_id = NULL,
    target_page_id = NULL,
    target_url = '/party/',
    open_mode = 'SAME_WINDOW'
WHERE parent_id IS NULL
  AND position = 'MAIN'
  AND name = '中心党建'
  AND target_type = 'PLACEHOLDER';
