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
        // 存储当前游戏的对局信息。
        const currentGameMessage = {'blue_dir': [], 'red_dir': [], 'result': []};
        const store = useStore()
        const socketUrl = `ws://127.0.0.1:8083/websocket/${store.state.user.token}`;
        let socket = null;
        // 标识未非回放画面
        store.commit("updateIsRecord", false);
        // const userId = store.state.user.id;

        onMounted(() => {
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
                        store.commit("start_timer", store, currentGameMessage)
                    }, 100);

                } else if (data.event === 'move') {
                    const gameObject = store.state.pk.gameMapObject;
                    const [blue, red] = gameObject.snakes;

                    currentGameMessage['blue_dir'].push(data.blue_direction)
                    currentGameMessage['red_dir'].push(data.red_direction)

                    blue.set_direction(data.blue_direction)
                    red.set_direction(data.red_direction)

                } else if (data.event === 'result') {
                    // console.log(data.event)
                    currentGameMessage['red_dir'].push(data.loser)

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