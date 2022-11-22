<template>
    <PlayGround/>
</template>

<script>
import PlayGround from "@/components/PlayGround";
import {onUnmounted} from "vue";
import {useStore} from "vuex";

export default {
    name: "RecordContent.vue",
    components: {
        PlayGround,
    },
    // setup 方法中无法获得 this,应为它先于 vue 对象创建？
    setup() {
        onUnmounted(()=>{
            const store = useStore();
            // store.state.pk.loser != 'none'
            store.commit("updateLoser",'none');
            console.log(store.state.pk.loser)
            console.log("unmounted RecordContent")
        })
    }

}
</script>

<style scoped>
</style>