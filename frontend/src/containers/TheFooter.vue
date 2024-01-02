<template>
  <CFooter>
    <div class="ml-auto">
      <a href="https://www.datamaker.ai" target="_blank">More info</a>
      <span class="ml-1">&copy; 2022 Datamaker</span>
      <span > - version: {{version}}</span>
    </div>
  </CFooter>
</template>

<script>
export default {
  name: 'TheFooter',
  data () {
    return {
      version: VERSION
    }
  }

}
</script>
