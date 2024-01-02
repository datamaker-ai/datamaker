<template>
  <div>
    <CTextarea
        rows="20"
        v-model="logfile">
    </CTextarea>
    <CButton color="primary" @click="downloadFile"> {{ $t('common.download_button') }} </CButton>
  </div>
</template>

<script>
  import {api, metrics} from "../../constants/api"

  export default {
    name: "Logs",
    created() {
      this.updateData()
      this.timer = setInterval(this.updateData, 5000)
    },
    data() {
      return {
        logfile: '',
        currentRange: 0,
        date: 'Thu, 30 Apr 2020 23:32:41 GMT'
      }
    },
    beforeDestroy() {
      clearInterval(this.timer)
      delete this.timer
    },
    updated() {
      let textarea = this.$el.querySelector('textarea')
      textarea.scrollTop = textarea.scrollHeight;
    },
    methods: {
      downloadFile() {
        metrics.get('/logfile',
          {
            responseType: 'blob',
          }
        ).then((response) => {
          const blob = new Blob([response.data], {type: response.data.type});
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          const contentDisposition = response.headers['content-disposition'];
          let fileName = 'logs';
          if (contentDisposition) {
            const fileNameMatch = contentDisposition.match(/filename="(.+)"/);
            if (fileNameMatch.length === 2)
              fileName = fileNameMatch[1];
          }
          link.setAttribute('download', fileName);
          document.body.appendChild(link);
          link.click();
          link.remove();
          window.URL.revokeObjectURL(url);
        });
      },
      updateData() {
        let config = {
          headers: {
            Range: `bytes=${this.currentRange}-`
          }
        }
        metrics.get('/logfile', config)
        .then(response => {
          let nextRange = parseInt(response.headers['content-length'])
          this.currentRange += nextRange - 1
          if (nextRange !== 1) {
            this.logfile += this.logfile === '' ? response.data : response.data.substring(1)
          }
        })
        .catch((error) => {
          if (error.response && error.response.status === 416) {
            //console.log("no data available")
          } else {
            clearInterval(this.timer)
          }
        })
      }
    }
  }
</script>

<style scoped>

</style>