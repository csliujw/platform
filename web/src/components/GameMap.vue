<template>
    <div class="gamemap" ref="parent">
        <canvas ref="canvas" tabindex="0"></canvas>
    </div>
</template>

<script>
import {GameMap} from "@/assets/scripts/GameMap";
import {onMounted, ref} from 'vue'
import {useStore} from "vuex";

export default {
    name: "GameMap",
    setup() {
        let parent = ref(null);
        let canvas = ref(null);
        const store = useStore();
        onMounted(() => {
            // 将游戏 gameMap 这个js对象存储在 pk 中，用于判断
            store.commit("updateGameMapObject", new GameMap(canvas.value.getContext('2d'), parent.value, store));
        });
        return {
            parent,
            canvas
        }
    }
}
</script>

<style scoped>
div.gamemap {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
}
</style>