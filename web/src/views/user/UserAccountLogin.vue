<template>
  <ContentFiled v-if="show_content">
    <div class="row justify-content-md-center">
      <div class="col-3">
        <!--阻止冒泡默认行为？-->
        <form @submit.prevent="login">
          <div class="mb-3">
            <label for="username" class="form-label">用户名</label>
            <input v-model="username" type="text" class="form-control" id="username" placeholder="请输入用户名">
          </div>
          <div class="mb-3">
            <label for="password" class="form-label">密码</label>
            <input v-model="password" type="password" class="form-control" id="password" placeholder="请输入密码">
          </div>
          <div class="error-message">{{ resp_message }}</div>
          <button type="submit" class="btn btn-primary">提交</button>
        </form>
      </div>
    </div>

  </ContentFiled>
</template>

<script>
import ContentFiled from "@/components/ContentFiled";
import {useStore} from 'vuex'
import {ref} from 'vue'
import router from "@/router/index";

export default {
  components: {
    ContentFiled
  },
  setup() {
    const store = useStore();
    let username = ref('');
    let password = ref('');
    let resp_message = ref('');

    let show_content = ref(false)
    const jwt_token = localStorage.getItem("jwt_token");
    if (jwt_token) {
      store.commit("updateToken", jwt_token);
      store.dispatch("getInfo", {
        success() {
          // 如果 jwt_token 还可以用，就尝试用 jwt token 获取信息，如果可以获取到，就更新到内存，然后在跳转到首页。
          router.push({name: 'home'})
        }, error() {
          show_content.value = true
        }
      })
    } else {
      // 如果 jwt 过期了或者是没有jwt，那么就要登录，設置為 true 顯示登錄頁面
      show_content.value = true
    }
    const login = () => {
      resp_message.value = "";
      /**
       * 登录，登录成功后保存用户信息，然后在跳转。
       */
      store.dispatch("login", {
        username: username.value,
        password: password.value,
        success(resp) {
          // 成功则跳转到主页面
          store.dispatch("getInfo", {
            success(resp) {
              if (resp.resp_message === 'success') {
                router.push({name: 'home'})
              }
            },
            error() {
              resp_message.value = "用户名或密码错误";
            },
          })
          router.push({name: 'home'})
          console.log(resp)
        },
        error() {
          resp_message.value = "用户名或密码错误";
        },
      });
    }

    return {
      username,
      password,
      resp_message,
      login,
      show_content
    }
  }
}
</script>

<style scoped>

button {
  width: 100%;
}

div.error-message {
  color: red;
}
</style>
