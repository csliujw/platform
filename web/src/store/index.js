import {createStore} from 'vuex'
import ModuleUser from "./user"
import ModulePK from "./pk"
import ModuleRecord from './record'
// store 里面的是全局变量
export default createStore({
    state: {},
    getters: {},
    mutations: {},
    actions: {},
    modules: {
        user: ModuleUser, // 将写好的数据导入到 index.js 中，全局可用。
        pk: ModulePK,
        record: ModuleRecord
    }
})
