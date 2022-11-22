<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
      <a class="navbar-brand" href="#">对战平台</a>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarText"
              aria-controls="navbarText" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarText">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0">
          <li class="nav-item" @click="a">
            <router-link :class="route_name=='pk'?'nav-link newactive' : ' nav-link ' " :to="{name:'pk'}">对战
            </router-link>
          </li>
          <li class="nav-item">
            <router-link :class="route_name=='record'?'nav-link newactive' : ' nav-link '" :to="{name:'record'}">对局列表
            </router-link>
          </li>
          <li class="nav-item">
            <router-link :class="route_name=='ranklist'?'nav-link newactive' : ' nav-link '" :to="{name:'ranklist'}">
              排行榜
            </router-link>
          </li>
        </ul>

        <ul class="navbar-nav" v-if="$store.state.user.is_login">
          <li class="nav-item dropdown">
            <a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button" data-bs-toggle="dropdown"
               aria-expanded="false">
              {{ $store.state.user.username }}
            </a>
            <ul class="dropdown-menu" aria-labelledby="navbarDropdown">
              <router-link class="dropdown-item" :to="{name:'bot'}">我的Bot</router-link>
              <li>
                <hr class="dropdown-divider">
              </li>
              <li><a class="dropdown-item" href="#" @click="logout">退出</a></li>
            </ul>
          </li>
        </ul>
        <ul class="navbar-nav" v-else>
          <li class="nav-item">
            <router-link class="nav-link" :to="{name: 'login'}" role="button">
              登录
            </router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" :to="{name: 'register'}" role="button">
              注册
            </router-link>
          </li>
        </ul>


      </div>
    </div>
  </nav>
</template>
<script>

import {useRoute} from 'vue-router'
import {computed} from "vue";
import {useStore} from "vuex";
import router from "@/router"; // 实时计算函数

export default {
  name: "NavBAr",
  setup() {
    const route = useRoute()
    const store = useStore()
    let route_name = computed(() => route.name)

    const logout = () => {
      console.log("执行了 logout 吗")
      store.dispatch("logout");
      router.push({name: 'login'})
    }
    return {
      route_name,
      logout
    }
  }
}
</script>
<style scoped>
.newactive {
  color: orangered !important;
}
</style>