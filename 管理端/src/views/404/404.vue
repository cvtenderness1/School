<template>
  <div class="not-found-container">
    <div class="error-box">
      <h1>404</h1>
      <p>页面不存在</p>
    </div>
    <el-button type="success" @click="goHome">返回主页</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()

const goHome = () => {
  router.push('/admin/home')
}
</script>

<style scoped>
.not-found-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 80vh; /* 垂直居中 */
  gap: 20px;
}
.error-box {
  width: 200px;
  padding: 30px;
  margin: 0 auto;
  border: 2px solid saddlebrown;
  background-color: #ac122d;
  color: white;
  text-align: center;
  border-radius: 8px;
}
.error-box h1 {
  margin: 0 0 10px;
  font-size: 48px;
}
.error-box p {
  margin: 0;
  font-size: 18px;
}
</style>