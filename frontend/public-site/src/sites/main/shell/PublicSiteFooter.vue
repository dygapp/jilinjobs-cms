<script setup lang="ts">
import { useMainSiteContext } from '../app/siteContext'
import SharedPublicFooter from '../../../shared/components/PublicFooter.vue'

const { config } = useMainSiteContext()
</script>

<template>
  <SharedPublicFooter :config="config" theme="main" />
</template>
