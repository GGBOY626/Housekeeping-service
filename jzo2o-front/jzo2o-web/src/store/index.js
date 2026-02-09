import { createStore } from 'vuex'

export default createStore({
  state: {
    footStatus: 0,
    city: null
  },
  mutations: {
    setFootStatus(state, val) {
      state.footStatus = val
    },
    setCity(state, val) {
      state.city = val
    }
  }
})
