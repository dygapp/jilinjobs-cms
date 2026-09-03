<script setup lang="ts">
import PartyBuildingHeader from '../shell/PartyBuildingHeader.vue'
import PartyBuildingFooter from '../shell/PartyBuildingFooter.vue'
</script>

<template>
  <div class="party-site-app" data-testid="party-building-site">
    <PartyBuildingHeader />
    <router-view />
    <PartyBuildingFooter />
  </div>
</template>
