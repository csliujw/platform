export default {
    state: {
        status: "matching", // matching 匹配中,playing 对战
        socket: null, // websocket
        opponent_username: "",
        opponent_photo: "",
        game_map: null, // 后端生成游戏地图
        blue_id: 0,
        blue_sx: 0,
        blue_sy: 0,
        red_id: 0,
        red_sx: 0,
        red_sy: 0,
        gameMapObject: null, // 存儲的是對象，不是數組
        loser: "none", // none all a b
        timer: null,
        gameMessage: {'blue_dir': [], 'red_dir': [], 'result': []},
        is_start_timer: false, // 如果是 bot 代碼，则对战的时候定时器的刷新频率减低，如果是人操控，则定时器的刷新频率提高
    },
    getters: {},
    mutations: {
        updateIsStartTimer(state, is_start_timer) {
            state.is_start_timer = is_start_timer;
        },
        initGameMessage(state) {
            state.gameMessage = {'blue_dir': [], 'red_dir': [], 'result': []};
        },
        cleanGameMessage(state) {
            state.gameMessage = {'blue_dir': [], 'red_dir': [], 'result': []};
        },
        // 非异步操作写在此处
        updateSocket(state, socket) {
            state.socket = socket;
        },
        updateOpponent(state, opponent) {
            state.opponent_username = opponent.username;
            state.opponent_photo = opponent.photo;
        },
        updateStatus(state, status) {
            state.status = status;
        },
        updateGameMap(state, game) {
            state.game_map = game.gamemap;
        },
        updateGameMapObject(state, gameMapObject) {
            state.gameMapObject = gameMapObject;
        },
        updateLoser(state, loser) {
            console.log("updateLoser")
            state.loser = loser;
        },
        updatePlayGameInfo(state, info) {
            state.blue_id = info.blue_id;
            state.blue_sx = info.blue_sx;
            state.blue_sy = info.blue_sy;

            state.red_id = info.red_id;
            state.red_sx = info.red_sx;
            state.red_sy = info.red_sy;
        },
        start_timer(state, times) {
            if (state.timer != null) return;
            console.log("开启定时器")
            state.timer = setInterval(() => {
                console.log("run")
                let result = null;
                const gameObject = state.gameMapObject;
                const [blue, red] = gameObject.snakes;
                // 有对局信息则设置对局信息内容
                if (state.gameMessage['blue_dir'].length > blue.step && state.gameMessage['red_dir'].length > red.step) {
                    let blue_dir = state.gameMessage['blue_dir'][blue.step]
                    let red_dir = state.gameMessage['red_dir'][red.step]
                    blue.set_direction(blue_dir)
                    red.set_direction(red_dir)
                }
                if (state.gameMessage['blue_dir'].length === blue.step && state.gameMessage['red_dir'].length === red.step && state.gameMessage['result'].length !== 0) {
                    setTimeout(() => {
                        if (result == null) {
                            result = state.gameMessage['result'].shift();
                        }
                        if (result === 'all' || result === 'Blue') {
                            blue.status = 'die';
                            state.loser = result;
                        }
                        if (result === 'all' || result === 'Red') {
                            red.status = 'die';
                            state.loser = result;
                        }
                        clearInterval(state.timer);
                        state.timer = null;
                        state.gameMessage = {'blue_dir': [], 'red_dir': [], 'result': []};
                        console.log("清楚定時器")
                    }, times - 100)
                }

            }, times);
        },
        end_timer(state) {
            console.log("executor end_timer")
            clearInterval(state.timer);
            state.timer = null;
        }
    },
    actions: {},
    modules: {}
}