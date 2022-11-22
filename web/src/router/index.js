import {createRouter, createWebHistory} from 'vue-router'
import PKIndex from "@/views/pk/PKIndex";
import RecordIndex from "@/views/record/RecordIndex";
import RanklistIndex from "@/views/ranklist/RanklistIndex";
import UserBotIndex from "@/views/user/bot/UserBotIndex"
import ErrorIndex from "@/views/error/ErrorIndex";
import UserAccountLogin from "@/views/user/UserAccountLogin";
import UserAccountRegister from "@/views/user/UserAccountRegister";
import store from '@/store/index'
import RecordContent from "@/views/record/RecordContent";

const routes = [
    {
        path: "/",
        name: "home",
        redirect: "/pk/",
        meta: { // 元信息，用户是否需要授权
            requireAuth: true
        }
    },
    {
        path: "/pk/",
        component: PKIndex,
        name: 'pk',
        meta: {
            requireAuth: true
        }
    },
    {
        path: "/record/",
        component: RecordIndex,
        name: 'record',
        meta: {
            requireAuth: true
        }
    },
    {
        path: "/ranklist/",
        component: RanklistIndex,
        name: 'ranklist',
        meta: {
            requireAuth: true
        }
    },
    {
        path: "/bot/",
        component: UserBotIndex,
        name: 'bot',
        meta: {
            requireAuth: true
        }
    },
    {
        path: "/404/",
        component: ErrorIndex,
        name: '404',
        meta: {
            requireAuth: false
        }
    },
    {
        path: '/login/',
        component: UserAccountLogin,
        name: 'login',
        meta: {
            requireAuth: false
        }
    },
    {
        path: '/register/',
        component: UserAccountRegister,
        name: 'register',
        meta: {
            requireAuth: false
        }
    },
    {
        path: '/record/:recordId/', //为路由添加参数
        component: RecordContent,
        name: 'video',
        meta: {
            requireAuth: true
        }
    },
    {
        path: '/:catchAll(.*)',
        redirect: '/404/',
        meta: {
            requireAuth: false
        }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// router 其作用之前执行的。查下具体的用法
router.beforeEach((to, from, next) => {
    // 需要授权且没有登录，则重定向到 login
    if (to.meta.requireAuth && !store.state.user.is_login) {
        next({name: "login"})
    } else {
        next();
    }
})

export default router
