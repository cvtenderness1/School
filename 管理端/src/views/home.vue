<template>
  <div class="welcome-page">
    <h1 class="welcome-title">欢迎进入校园跑腿系统</h1>
    <p class="welcome-subtitle">高效便捷 · 服务校园</p>
  </div>
</template>

<script setup>
</script>

<style scoped>
.welcome-page {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-image: url("@/assets/img_1.png");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.welcome-title {
  font-size: 42px;
  font-weight: 700;
  margin: 0 0 16px 0;
  background: linear-gradient(90deg, #ff9500, #ff6b00);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.welcome-subtitle {
  font-size: 18px;
  color: #140306;
  margin: 0;
  font-weight: 500;
}
</style>