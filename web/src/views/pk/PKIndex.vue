<template>
    <PlayGround v-if="$store.state.pk.status === 'playing'"/>
    <MatchGround v-if="$store.state.pk.status === 'matching'"/>
    <ResultBoard v-if="$store.state.pk.loser != 'none'"/>
</template>

<script>
import PlayGround from "@/components/PlayGround";
import MatchGround from "@/components/MatchGround";
import {useStore} from 'vuex';
import {onMounted, onUnmounted} from 'vue'
import ResultBoard from '@/components/ResultBoard.vue'

export default {
    name: "PKIndex.vue",
    components: {
        PlayGround,
        MatchGround,
        ResultBoard
    },
    // setup 方法中无法获得 this,应为它先于 vue 对象创建？
    setup() {
        const store = useStore()
        const socketUrl = `ws://127.0.0.1:8083/websocket/${store.state.user.token}`;
        let socket = null;
        // 這個真的有必要嗎？有影响吗？
        store.commit("updateIsRecord", false);
        // const userId = store.state.user.id;
        onMounted(() => {

            console.log("==================")
            console.log(store.state.record.is_record)
            store.commit("updateOpponent", {
                username: "???",
                photo: "https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png",
            })
            socket = new WebSocket(socketUrl);
            socket.onopen = () => {
                console.log("connected");
                store.commit("updateSocket", socket);
            };
            socket.onmessage = msg => {
                const data = JSON.parse(msg.data);
                if (data.event === 'start-matching') {
                    console.log(data.opponent_username);
                    store.commit("updateOpponent", {
                        username: data.opponent_username,
                        photo: data.opponent_photo,
                    });
                    store.commit("updateGameMap", data.game);
                    store.commit("updatePlayGameInfo", data.game);
                    setTimeout(() => {
                        store.commit("updateStatus", "playing");
                    }, 200);

                } else if (data.event === 'move') {
                    const gameObject = store.state.pk.gameMapObject;
                    const [blue, red] = gameObject.snakes;
                    console.log("都移动了，更新了位置信息")
                    console.log(data.blue_direction, data.red_direction);
                    blue.set_direction(data.blue_direction)
                    red.set_direction(data.red_direction)
                } else if (data.event === 'result') {
                    console.log(data.event)
                    const game = store.state.pk.gameMapObject;
                    const [blue, red] = game.snakes;
                    if (data.loser === 'all' || data.loser === 'Blue') {
                        blue.status = 'die';
                    }
                    if (data.loser === 'all' || data.loser === 'Red') {
                        red.status = 'die';
                    }
                    store.commit("updateLoser", data.loser);
                }

            }
            socket.onclose = () => {
                console.log("disconnected")
            }
        });
        // 且换页面的时候，对局默认结束，关闭 websocket
        onUnmounted(() => {
            // that.refs.match_child.state.socket.close();
            // console.log(MatchGround.socket);
            socket.close();
            store.commit("updateStatus", "matching");
            store.commit("updateLoser", 'none');
        });
    }

}
</script>

<style scoped>
</style>