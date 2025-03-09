import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    user: JSON.parse(localStorage.getItem('user')) || null
  },
  mutations: {
    SET_USER(state, user) {
      state.user = user
      if (user) {
        localStorage.setItem('user', JSON.stringify(user))
      } else {
        localStorage.removeItem('user')
      }
    }
  },
  actions: {
    setUser({ commit }, user) {
      commit('SET_USER', user)
    },
    clearUser({ commit }) {
      commit('SET_USER', null)
    }
  },
  getters: {
    isLoggedIn: state => !!state.user,
    userType: state => state.user ? state.user.userType : null
  }
}) 