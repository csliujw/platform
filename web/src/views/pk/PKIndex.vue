<template>
    <PlayGround v-if="$store.state.pk.status === 'playing'"/>
    <MatchGround v-if="$store.state.pk.status === 'matching'"/>
    <div class="alert alert-danger shows" role="alert" style="width: 50%; position: absolute;left: 25%;top: 10%;text-align: center" v-show="display">网络连接异常！请稍后刷新重试！
    </div>
    <ResultBoard v-if="$store.state.pk.loser != 'none'"/>
</template>

<script>
import PlayGround from "@/components/PlayGround";
import MatchGround from "@/components/MatchGround";
import {useStore} from 'vuex';
import {onMounted, onUnmounted, ref} from 'vue'
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
        let display = ref(false);
        const store = useStore()
        const socketUrl = `ws://127.0.0.1:8083/websocket/${store.state.user.token}`;
        let socket = null;
        // 标识为非回放画面
        store.commit("updateIsRecord", false);

        onMounted(() => {
            store.commit("updateOpponent", {
                username: "???",
                photo: "https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png",
            })

            socket = new WebSocket(socketUrl);

            socket.onopen = () => {
                store.commit("updateSocket", socket);
                // 初始胡游戏的对局信息（用户的历史路径）
                // 开启 socket 是重置游戏信息，发送页面且换，重开游戏时需要清理现在的游戏数据
                store.commit("initGameMessage");
            };

            socket.onerror = () => {
                console.log("error")
                display.value = true;
            };

            socket.onmessage = msg => {
                const data = JSON.parse(msg.data);
                if (data.event === 'start-matching') {
                    console.log(data.opponent_username);
                    store.commit("updateOpponent", {
                        username: data.opponent_username,
                        photo: data.opponent_photo,
                    });

                    // 开启定时器
                    if (data.game.start_timer) {
                        store.commit("updateIsStartTimer", data.game.start_timer)
                        store.commit("start_timer", 450);
                    }
                    store.commit("updateGameMap", data.game);
                    store.commit("updatePlayGameInfo", data.game);

                    setTimeout(() => {
                        store.commit("updateStatus", "playing");
                    }, 100);

                } else if (data.event === 'move') {
                    if (!store.state.pk.is_start_timer) {
                        // 不开启定时器，则说明是手动控制的，直接设置即可。
                        console.log("手动控制的");
                        const gameObject = store.state.pk.gameMapObject;
                        const [blue, red] = gameObject.snakes;
                        blue.set_direction(data.blue_direction)
                        red.set_direction(data.red_direction)
                    } else {
                        // 如果启用了定时器，则将后端的数据暂存前端的数组中，然后在定时器中消费。直到对局正常结束，清除数组中的信息和定时器的标记。
                        const gameMessage = store.state.pk.gameMessage;
                        gameMessage['blue_dir'].push(data.blue_direction)
                        gameMessage['red_dir'].push(data.red_direction)
                    }

                } else if (data.event === 'result') {
                    if (!store.state.pk.is_start_timer) {
                        const game = store.state.pk.gameMapObject;
                        const [blue, red] = game.snakes;
                        if (data.loser === 'all' || data.loser === 'Blue') {
                            blue.status = 'die';
                        }
                        if (data.loser === 'all' || data.loser === 'Red') {
                            red.status = 'die';
                        }
                        store.commit("updateLoser", data.loser);
                    } else {
                        const gameMessage = store.state.pk.gameMessage;
                        gameMessage['result'].push(data.loser)
                    }
                }
            }
            socket.onclose = () => {
                // socket 断开连接，清除所有对局状态
                store.commit("end_timer");
                store.commit("updateIsStartTimer", false);
                store.commit("cleanGameMessage");
            }
        });
        // 且换页面的时候，对局默认结束，关闭 websocket
        onUnmounted(() => {
            // that.refs.match_child.state.socket.close();
            // console.log(MatchGround.socket);
            socket.close();

            store.commit("updateStatus", "matching");
            store.commit("updateLoser", 'none');
            store.commit("end_timer"); // 页面且换出去的时候需要清理定时器，定理定时器状态，清理游戏记录（来自后端的移动记录）
            store.commit("updateIsStartTimer", false);
            store.commit("cleanGameMessage");
        });
        return {
            display
        }
    }

}
</script>

<style scoped>
.shows {
    display: block;
}
</style>