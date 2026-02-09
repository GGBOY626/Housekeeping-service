<template>
  <div class="register-page">
    <div class="register-box">
      <img src="/static/logo.png" alt="logo" class="logo" />
      <h1>注册</h1>
      <form @submit.prevent="onRegister" class="form">
        <input v-model="form.phone" type="text" placeholder="手机号" required />
        <input v-model="form.password" type="password" placeholder="密码（至少6位）" required minlength="6" />
        <input v-model="form.nickname" type="text" placeholder="昵称（可选）" />
        <button type="submit" class="agree-btn">注册</button>
        <p class="tip">
          已有账号？
          <router-link to="/login">去登录</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/login'

const router = useRouter()
const form = reactive({ phone: '', password: '', nickname: '' })

const onRegister = async () => {
  if (form.password.length < 6) {
    alert('密码至少6位')
    return
  }
  try {
    await register(form)
    alert('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    alert(e?.response?.data?.msg || e?.message || '注册失败')
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f8f8;
}
.register-box {
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
