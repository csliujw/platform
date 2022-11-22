<template>
  <div class="container" style="margin-top: 3vh">
    <div class="card">
      <div class="card-body">
        <slot></slot>
      </div>
    </div>
  </div>
</template>
<script>
// 未来填充的内容都渲染到 slot 里面
// PK ranklist 都可以用到的一个公共的卡片区域,因此提取为一个组件
export default {
  name: "ContentFiled"
}
</script>

<style scoped>

</style>