<template>
    <ContentFiled>
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
                        <input v-model="password" type="password" class="form-control" id="password"
                               placeholder="请输入密码">
                    </div>
                    <div class="mb-3">
                        <label for="password" class="form-label">确认密码</label>
                        <input v-model="confirmPassword" type="password" class="form-control" id="confirmPassword"
                               placeholder="请输入密码">
                    </div>
                    <div class="mb-3">
                        <label for="password" class="form-label">头像链接</label>
                        <input v-model="photo" type="text" class="form-control" id="photo" placeholder="请输入头像的外部链接">
                    </div>
                    <div class="mb-3" style="color:red">
                        {{ resp_message }}
                    </div>
                    <button type="button" class="btn btn-primary btn-lg  btn-block" @click="register">注册</button>
                </form>
            </div>
        </div>

    </ContentFiled>
</template>

<script>
import ContentFiled from "@/components/ContentFiled";
import {ref} from 'vue'
import $ from 'jquery'
import router from "@/router";
import {useStore} from "vuex";

export default {
    components: {
        ContentFiled
    },
    setup() {
        const store = useStore();
        let username = ref('')
        let password = ref('')
        let confirmPassword = ref('')
        let photo = ref('')
        let resp_message = ref('')

        const login = ()=>{
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
                        },error(resp){
                            console.log("未知错误");
                            console.log(resp)
                        }
                    })
                    router.push({name: 'home'})
                    console.log(resp)
                },
                error() {
                    resp_message.value = "用户名或密码错误";
                },
            });
        }
        const register = () => {
            console.log("執行了注冊")
            $.ajax(
                {
                    url: 'http://127.0.0.1:8080/user/account/register/',
                    type: 'post',
                    data: {
                        username: username.value,
                        password: password.value,
                        confirmPassword: confirmPassword.value,
                        photo: photo.value
                    },
                    success(resp) {
                        resp_message.value = resp.resp_message
                        if(resp_message.value === '注冊成功'){
                            setTimeout(login,1000);
                        }
                    }, error(resp) {
                        resp_message.value = resp.resp_message
                        console.log("error")
                    }
                }
            )
        }
        return {
            username,
            password,
            confirmPassword,
            photo,
            resp_message,
            register
        }
    }
}
</script>

<style scoped>

</style>
