// 存储用户的全局信息
import $ from 'jquery'

export default {
    state: {
        id: "",
        username: "",
        photo: "",
        token: "",
        is_login: false
    },
    getters: {},
    mutations: { // 用 commit 執行
        // 一般用于修改数据的
        updateUser(state, user) {
            state.id = user.id;
            state.username = user.username;
            state.photo = user.photo;
            state.is_login = user.is_login;
        },
        updateToken(state, token) {
            state.token = token;
        },
        logout(state) {
            state.id = "";
            state.token = "";
            state.username = "";
            state.photo = "";
            state.is_login = false;
        }
    },
    actions: { // 用 dispatch 执行
        login(context, data) {
            console.log("执行了 user 的 login 方法吗")
            $.ajax({
                url: "http://127.0.0.1:8080/user/account/token/",
                type: "post",
                data: {
                    username: data.username,
                    password: data.password
                },
                success(resp) {
                    // action 里调用 mutations 要用 commit
                    if (resp.resp_message === 'success') {
                        localStorage.setItem("jwt_token", resp.token);
                        context.commit('updateToken', resp.token);
                        data.success(resp);
                    } else {
                        data.error(resp);
                    }
                },
                error(resp) {
                    data.error(resp); // 這個 data 哪裏來的？我们传参的 data 里面包含 success 方法和 error 方法
                },
            });
        },
        getInfo(context, data) {
            $.ajax({
                url: "http://127.0.0.1:8080/user/account/info/",
                type: "get",
                headers: {
                    Authorization: "voucher" + context.state.token
                },
                success(resp) {
                    // action 里调用 mutations 要用 commit
                    if (resp.resp_message === 'success') {
                        context.commit('updateUser', {
                            ...resp,
                            is_login: true,
                        });
                        data.success(resp);
                    } else {
                        data.error(resp);
                    }
                },
                error(resp) {
                    data.error(resp); // 這個 data 哪裏來的？我们传参的 data 里面包含 success 方法和 error 方法
                },
            });
        },
        logout(context) {
            localStorage.removeItem("jwt_token");
            context.commit("logout");
        }
    },
    modules: {}
}
