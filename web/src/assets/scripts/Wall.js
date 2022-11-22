import {GameObject} from "@/assets/scripts/Game"

// 都是自己画自己,符合游戏开发的逻辑
export class Wall extends GameObject {
    constructor(r, c, gamemap) {
        super();
        this.r = r;
        this.c = c;
        this.gamemap = gamemap;
        this.color = '#B37226';
        this.walls = []
    }

    update() {
        this.render();
    }

    render() {
        const L = this.gamemap.L; // 每个单元格的长度
        const ctx = this.gamemap.ctx;
        ctx.fillStyle = this.color;
        ctx.fillRect(this.c * L, this.r * L, L, L);
    }
}