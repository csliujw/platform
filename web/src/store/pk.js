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
    },
    getters: {},
    mutations: {
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
            state.loser = loser;
        },
        updatePlayGameInfo(state, info) {
            state.blue_id = info.blue_id;
            state.blue_sx = info.blue_sx;
            state.blue_sy = info.blue_sy;

            state.red_id = info.red_id;
            state.red_sx = info.red_sx;
            state.red_sy = info.red_sy;
        }
    },
    actions: {},
    modules: {}
}