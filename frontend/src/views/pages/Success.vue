<template>

</template>

<script>
import {api} from "@/constants/api";

export default {
  name: "Success",
  mounted() {
    api.get("/me")
        .then(response => {
          sessionStorage.setItem("isAdmin", response.data.groups.includes("ROLE_ADMIN").toString())
          sessionStorage.setItem("isAuthenticated", "true")
          sessionStorage.setItem("username", response.data.username)
          this.$router.replace('/')
        })
        .catch(err => {
          console.log(err);
        })
  }
}
</script>

<style scoped>

</style>