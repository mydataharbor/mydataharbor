<template>
  <div id="app">
    <router-view v-if="isRouterAlice"/>
  </div>
</template>

<script>
export default {
  name: 'App',
  provide(){
    return {
      reload: this.reload
    }
  },
  data(){
    return {
      isRouterAlice: true
    }
  },
  methods: {
    reload(){
      this.isRouterAlice = false
      this.$nextTick(function(){
        this.isRouterAlice = true
      })
    }
  }
}
</script>

<style>
@import "./assets/css/main.css";
</style>
