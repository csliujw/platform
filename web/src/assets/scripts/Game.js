const GAME_OBJECT = [] // 存放游戏对象的
export class GameObject {
    constructor() {
        GAME_OBJECT.push(this);
        this.has_called_start = false; // 是否执行过 start
        this.timedelta = 0; // 上一帧和下一帧的时间间隔,确保一秒内走的像素值一样.
    }

    // 初始化资源
    start() {
    }

    // 除了第一帧,每帧执行一次
    update() {
    }

    // 删除之前执行的操作
    before_destroy() {
    }

    // 删除对象
    destroy() {
        this.before_destroy();
        for (let index in GAME_OBJECT) {
            const cur = GAME_OBJECT[index];
            if (cur === this) {
                GAME_OBJECT.splice(index);
                break;
            }
        }
    }
}

let last_timestamp; // 上一帧执行的时刻
const step = (timestamp) => { // 怎么传递的 timestamp 这个参数?
    for(let obj of GAME_OBJECT){
        if(!obj.has_called_start){
            obj.has_called_start = true;
            obj.start();
        }else{
            obj.timedelta = timestamp - last_timestamp;
            obj.update() // 每一帧更新一次,让动作看起来流畅
        }
    }
    last_timestamp = timestamp
    requestAnimationFrame(step)
}
requestAnimationFrame(step)