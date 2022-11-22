<template>
    <div style="text-align: center;color:orange">{{ color }}</div>
    <div class="playground">
        <GameMap></GameMap>
    </div>
</template>

<script>
import GameMap from "@/components/GameMap";
import {useStore} from "vuex";

export default {
    name: "PlayGround",
    components: {GameMap},
    setup() {
        let color = "";
        const store = useStore();
        const userId = store.state.user.id;
        const blueId = store.state.pk.blue_id;
        const redId = store.state.pk.red_id;
        if (userId == blueId) {
            color = "我方为蓝方";
        } else if(userId == redId){
            color = "我方为红方";
        }else{
            color = "他人对局"
        }
        return {
            color
        }
    }
}
</script>

<style scoped>
div.playground {
    width: 65vw;
    height: 70vh;
    background-color: rgba(255, 255, 255, 0.3);
    margin: 0 auto;
    margin-top: 2vh;
    border-radius: 0.5vh;
}
</style>