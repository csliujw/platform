// 游戏地图
import {GameObject} from "@/assets/scripts/Game";
import {Wall} from "@/assets/scripts/Wall"
import {Snake} from "@/assets/scripts/Snake";

// 在游戏地图中创建蛇，创建地图，创建障碍物
export class GameMap extends GameObject {
    constructor(ctx, parent, store) { // ctx 是画布, parent 是画布的父元素
        super();
        this.ctx = ctx;
        this.parent = parent;
        this.L = 0; // 相对距离的长度
        // 因为浏览器会变,所以像素距离要用相对大小的距离
        this.store = store
        this.rows = 13;
        this.cols = 14;

        // 随机障碍物的个数
        this.walls = [];
        this.snakes = [ // 蓝色、红色
            new Snake({idx: 0, color: "#4876EC", r: this.rows - 2, c: 1}, this),
            new Snake({idx: 1, color: "#F94848", r: 1, c: this.cols - 2}, this),
        ];
    }

    create_walls() {
        const mapArray = this.store.state.pk.game_map;
        console.log(mapArray)
        for (let r = 0; r < this.rows; r++) {
            for (let c = 0; c < this.cols; c++) {
                if (mapArray[r][c]) {
                    this.walls.push(new Wall(r, c, this));
                }
            }
        }
    }

    send_message(direction) {
        if (direction >= 0) {
            this.store.state.pk.socket.send(JSON.stringify({
                event: "move",
                direction: direction
            }));
        }
    }

    add_listening_events() {
        console.log(this.store.state.record.is_record)
        if (this.store.state.record.is_record) {
            // 每 300 ms 更新下每个 蛇的操作。
            const [snackBlue, snackRed] = this.snakes;
            let k = 0;

            const a_step = this.store.state.record.asteps;
            const b_step = this.store.state.record.bsteps;
            const loser = this.store.state.record.record_loser;

            const interval_id = setInterval(() => {
                if (k >= a_step.length) {

                    if (loser === "all" || loser === 'Red') {
                        snackRed.status = 'die'
                        console.log("对局结束")
                        // 播放结束了，然后再清除 record 的记录
                        console.log("=========================")
                        console.log(this.store.state.record.is_record)
                        console.log("=========================")

                    }
                    if (loser === 'all' || loser === 'Blue') {
                        snackBlue.status = 'die';
                        console.log("对局结束")
                        console.log("=========================")
                        console.log(this.store.state.record.is_record)
                        console.log("=========================")
                    }
                    clearInterval(interval_id);
                } else {
                    snackBlue.set_direction(parseInt(a_step[k]));
                    snackRed.set_direction(parseInt(b_step[k]));
                }
                k++;
            }, 450);
            interval_id.valueOf();
        } else {
            this.ctx.canvas.focus();
            this.ctx.canvas.addEventListener("keydown", e => {
                let direction = -1;
                if (e.key === 'w') direction = 0;
                else if (e.key === 'd') direction = 1;
                else if (e.key === 's') direction = 2;
                else if (e.key === 'a') direction = 3;
                this.send_message(direction);
            });
        }
    }

    // 最开始的时候画一下墙就行.
    start() {
        this.create_walls()
        this.add_listening_events();
    }

    // 更新每个单元个的大小,因为浏览器的窗口大小可能会被用户拉来来去,变化.
    update_size() {
        this.L = parseInt(Math.min(this.parent.clientWidth / this.cols, this.parent.clientHeight / this.rows));
        this.ctx.canvas.width = this.L * this.cols;
        this.ctx.canvas.height = this.L * this.rows;
    }

    // 两条蛇都被 gameMap 持有了，所有在这里判断是否准备好了。如何判断是否准备好了。两条蛇都活着，且都发出了指令，那么就可以进行下一步了。
    check_ready() {

        for (const snack of this.snakes) {
            /**
             * 开始 都是 idel，都符合条件，然后只要都按下了 方向键，那就可以下一步了。
             */
            if (snack.status != 'idle') return false; // 开始，和每次走完后状态都会变为 idle。
            if (snack.direction == -1) return false; //  有蛇没动，说明没准备好。死亡的情况不会出现在这里
        }
        return true;
    }

    next_step() { // 让两条蛇进入下一回合
        for (const snack of this.snakes) {
            snack.next_step(); // 各自走各自的
        }
    }

    update() {
        this.update_size();
        if (this.check_ready()) {
            this.next_step();
        }
        this.render();
    }

    render() { // 渲染
        // console.log("执行了渲染")
        // 画出最基础的地图.
        const color_even = "#AAD751", color_odd = "#A2D149";
        for (let r = 0; r < this.rows; r++) {
            for (let c = 0; c < this.cols; c++) {
                if ((r + c) % 2 === 0) {
                    this.ctx.fillStyle = color_even;
                } else {
                    this.ctx.fillStyle = color_odd;
                }
                this.ctx.fillRect(c * this.L, r * this.L, this.L, this.L);
            }
        }
    }
}