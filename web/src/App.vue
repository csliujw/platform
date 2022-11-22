<template>
  <NavBar/>
  <!--  geng'ju-->
  <router-view/>
</template>

<script>

import NavBar from "@/components/NavBar";
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap'

export default {
  name: "App",
  components: {
    NavBar
  },
}
</script>

<style>
body {
  background-image: url("./assets/background.png");
  background-size: 100%;
}
</style>