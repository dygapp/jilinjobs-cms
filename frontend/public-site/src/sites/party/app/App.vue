<script setup lang="ts">
import PartyHeader from '../shell/PartyHeader.vue'
import PartyFooter from '../shell/PartyFooter.vue'
</script>

<template>
  <div class="party-site-app" data-testid="party-site">
    <PartyHeader />
    <router-view />
    <PartyFooter />
  </div>
</template>
