<template>
  <div class="login-page">
    <div class="login-box">
      <img src="/static/logo.png" alt="logo" class="logo" />
      <h1>云岚到家</h1>
      <form @submit.prevent="onLogin" class="form">
        <input v-model="form.phone" type="text" placeholder="手机号" required />
        <input v-model="form.password" type="password" placeholder="密码" required />
        <button type="submit" class="agree-btn">登录</button>
        <p class="tip">
          没有账号？
          <router-link to="/register">立即注册</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { loginByPassword } from '@/api/login'

const router = useRouter()
const form = reactive({ phone: '', password: '' })

const onLogin = async () => {
  try {
    const res = await loginByPassword(form)
    const token = res?.data?.token || res?.token
    const nickname = res?.data?.nickname || '用户'
    if (token) {
      localStorage.setItem('token', token)
      localStorage.setItem('nickName', nickname)
      router.push('/')
    } else {
      alert('登录失败')
    }
  } catch (e) {
    alert(e?.response?.data?.msg || e?.message || '登录失败')
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f8f8;
}
.login-box {
  background: #fff;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  width: 320px;
}
.logo {
  width: 120px;
  display: block;
  margin: 0 auto 16px;
}
h1 {
  text-align: center;
  font-size: 24px;
  margin-bottom: 24px;
  color: #151515;
}
.form input {
  width: 100%;
  padding: 12px 16px;
  margin-bottom: 12px;
  border: 1px solid #eee;
  border-radius: 8px;
  font-size: 14px;
}
.form button {
  width: 100%;
  padding: 12px;
  margin-top: 8px;
}
.tip {
  margin-top: 16px;
  text-align: center;
  font-size: 14px;
  color: #888;
}
.tip a {
  color: var(--essential-color-red);
}
</style>
