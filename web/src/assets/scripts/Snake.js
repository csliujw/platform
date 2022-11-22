import {GameObject} from "@/assets/scripts/Game";
import {Cell} from "@/assets/scripts/Cell";

/**
 * 前10步每步变成一格子
 * 后面每3步再变长
 */
export class Snake extends GameObject {
    constructor(info, gamemap) {
        super();
        this.idx = info.idx;
        this.color = info.color;
        this.gamemap = gamemap; // 自己画自己
        this.cells = [new Cell(info.r, info.c)] // 存储蛇的身体，最开始就一个点。
        this.next_cell = null; // 下一步的位置
        this.speed = 3; // 蛇每秒走五个格子
        this.direction = -1; // -1 表示没有指令，0 1 2 3 分别表示上左下右
        this.status = "idle";// idle 表示停止， move 移动，die 死亡
        this.dr = [-1, 0, 1, 0];
        this.dc = [0, 1, 0, -1];

        this.step = 0; // 回合数
        this.eps = 1e-2; // 允许的误差

        this.eye_direction = 0;// 眼睛的方向 默认是上
        if (this.idx === 1) this.eye_direction = 2;// 左下角的蛇初始朝上，右上角的蛇朝下

        this.eye_dx = [  // 蛇眼睛不同方向的x的偏移量
            [-1, 1],
            [1, 1],
            [1, -1],
            [-1, -1],
        ];
        this.eye_dy = [  // 蛇眼睛不同方向的y的偏移量
            [-1, -1],
            [-1, 1],
            [1, 1],
            [1, -1],
        ];
    }

    start() {}

    set_direction(d) {
        this.direction = d;
    }

    next_step() {  // 将蛇的状态变为走下一步，下一步向那走呢？
        const dir = this.direction; // 走的位置
        this.next_cell = new Cell(this.cells[0].r + this.dr[dir], this.cells[0].c + this.dc[dir])
        this.eye_direction = dir;
        this.direction = -1;// 清空操作
        this.status = "move"; // 变为 move，这样在 update 的时候蛇发现自己可以走了，就移动，移动结束后就继续将状态变为 idle，静止。
        this.step++; // 用于判断蛇身体是否要变长。

        const k = this.cells.length;
        for (let i = k; i > 0; i--) {
            this.cells[i] = JSON.parse(JSON.stringify(this.cells[i - 1]));
        }
        if (!this.check_valid(this.next_cell)) {
            this.status = "die";
        }
    }

    check_valid(cell) { // 检测目标位置是否合法
        for (const wall of this.gamemap.walls) {
            if (wall.r === cell.r && wall.c === cell.c) return false;
        }

        for (const snake of this.gamemap.snakes) {
            let k = snake.cells.length;
            // 当蛇尾会前进的时候，蛇尾不要判断。
            if (!snake.check_tail_increasing()) {
                k--;
            }
            for (let i = 0; i < k; i++) {
                if (snake.cells[i].r === cell.r && snake.cells[i].c === cell.c) return false;
            }
        }
        return true;
    }

    // 检测当前回合蛇的身体是否需要变长
    check_tail_increasing() {
        if (this.step <= 10) return true;
        if (this.step % 3 === 1) return true;
        return false;
    }

    /**
     * 头部和尾部动，新头部往我们指定的方向动，尾巴向下一个节点动。
     * 蛇什么时候可以动呢？当两个蛇都发出指令了才可以动。
     */
    update_move() {
        const dx = this.next_cell.x - this.cells[0].x;
        const dy = this.next_cell.y - this.cells[0].y;
        const distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < this.eps) {
            this.cells[0] = this.next_cell; // 添加一个新的蛇头，那旧蛇头呢？
            this.status = "idle"; // 走完了，停下来
            this.next_cell = null;

            // 蛇不需要变长的话，就弹出蛇尾。
            if (!this.check_tail_increasing()) {
                this.cells.pop();
            }
        } else {
            const move_distance = this.speed * (this.timedelta / 1000) // 如果过去了 0.1 秒，则走 0.1 * 5 个格子
            this.cells[0].x += move_distance * dx / distance;
            this.cells[0].y += move_distance * dy / distance;

            // 走的时候也要判断蛇尾要不要走
            if (!this.check_tail_increasing()) {
                const k = this.cells.length;
                const tail = this.cells[k - 1], tail_target = this.cells[k - 2];
                const tail_dx = tail_target.x - tail.x;
                const tail_dy = tail_target.y - tail.y;
                tail.x += move_distance * tail_dx / distance;
                tail.y += move_distance * tail_dy / distance;
            }
        }
    }

    // 实际上是每一帧调用一次 update 方法
    update() {
        if (this.status === 'move') {
            this.update_move();
        }
        this.render();
    }

    // 只是一个绘画的操作
    render() {
        const L = this.gamemap.L;
        const ctx = this.gamemap.ctx;
        ctx.fillStyle = this.color;

        if (this.status === 'die') {
            ctx.fillStyle = "white";
        }
        // 画蛇的身体
        for (const cell of this.cells) {
            ctx.beginPath();
            ctx.arc(cell.x * L, cell.y * L, L / 2 * 0.8, 0, Math.PI * 2);
            ctx.fill();
        }

        // 补全蛇的身体，避免一个一个圈
        for (let i = 1; i < this.cells.length; i++) {
            const a = this.cells[i - 1], b = this.cells[i];
            if (Math.abs(a.x - b.x) < this.eps && Math.abs(a.y - b.y) < this.eps)
                continue;
            if (Math.abs(a.x - b.x) < this.eps) {
                ctx.fillRect((a.x - 0.4) * L, Math.min(a.y, b.y) * L, L * 0.8, Math.abs(a.y - b.y) * L);
            } else {
                ctx.fillRect(Math.min(a.x, b.x) * L, (a.y - 0.4) * L, Math.abs(a.x - b.x) * L, L * 0.8);
            }
        }

        // // 画蛇的眼睛
        ctx.fillStyle = "black";
        for (let i = 0; i < 2; i++) {
            const eye_x = (this.cells[0].x + this.eye_dx[this.eye_direction][i] * 0.15) * L;
            const eye_y = (this.cells[0].y + this.eye_dy[this.eye_direction][i] * 0.15) * L;

            ctx.beginPath();
            ctx.arc(eye_x, eye_y, L * 0.05, 0, Math.PI * 2);
            ctx.fill();
        }
    }
}