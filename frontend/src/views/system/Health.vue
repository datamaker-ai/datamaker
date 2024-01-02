<template>
  <div>
    <CCardGroup columns class="card-columns cols-2">
      <CCard>
        <CCardHeader>
          {{ $t('system.processor_header') }}
        </CCardHeader>
        <CCardBody>
          <CChartLine
              :datasets="processors"
              :labels="minutes"
              :options="options"
          />
        </CCardBody>
      </CCard>
      <CCard>
        <CCardHeader>
          {{ $t('system.memory_header') }}
        </CCardHeader>
        <CCardBody>
          <div class="clearfix">
            <div class="float-left">
              <small class="text-muted">{{ $t('system.memory_used', {used: jvmUsed, max: jvmMax}) }}</small>
            </div>
            <div class="float-right">
              <strong>{{memory.usage}}%</strong>
            </div>
          </div>
          <CProgress
              class="progress-xs"
              v-model="memory.usage"
              :color="color(memory.usage)"
          />
        </CCardBody>
      </CCard>
    </CCardGroup>
    <CCard>
      <CCardHeader>
        {{ $t('system.metrics_header') }}
      </CCardHeader>
      <CCardBody>
        <CDataTable
                hover
                sorter
                :items="items"
                :fields="fields"
                :loading="isLoading"
        >
        </CDataTable>
      </CCardBody>
    </CCard>
  </div>
</template>

<script>
  import {CChartLine} from '@coreui/vue-chartjs'
  import {api, metrics} from "../../constants/api"
  import {i18n} from "@/plugins/i18n";
  const GIGABYTES = 1024 * 1024 * 1024
  const MEGABYTES = 1024 * 1024

  export default {
    name: "Health",
    components: {CChartLine},
    data() {
      return {
        items: [],
        fields: [
          {
            key: 'name',
            label: i18n.t('common.name')
          },
          {
            key: 'type',
            label: i18n.t('common.type')
          },
          {
            key: 'value',
            label: i18n.t('common.value')
          }
        ],
        perPage: 5,
        disabledButtons: false,
        isLoading: false,
        timer: '',
        memory: {usage: 0},
        minutes: [1, 2, 3, 4, 5, 6, 7],
        jvmMax: 0,
        jvmUsed: 0,
        processors: [{
          label: 'System CPU Usage (%)',
          borderColor: 'rgb(228,102,81,0.9)',
          data: [0]
        },{
          label: 'JVM CPU Usage (%)',
          borderColor: 'rgb(0,216,255,0.9)',
          data: [0]
        }],
        options: {
          scales: {
            yAxes: [{
              ticks: {
                beginAtZero: true,
                suggestedMin: 0,
                suggestedMax: 100
              }
            }]
          }
        }
      }
    },
    created() {
      this.updateData()
      this.timer = setInterval(this.updateData, 5000)
    },
    computed: {
      // processors() {
      //   return [
      //     {
      //       label: 'Usage (%)',
      //       backgroundColor: 'rgb(0,216,255,0.9)',
      //       data: [39, 80, 40, 35, 40, 20, 45]
      //     }
      //   ]
      // }
    },
    beforeDestroy() {
      clearInterval(this.timer)
      delete this.timer
    },
    methods: {
      setItem(array, item, length) {
        if (array.push(item) > length){
          array.shift()
        }
      },
      convertToSize(value) {
        if (value > GIGABYTES) {
          return (value / GIGABYTES).toFixed() + "GB"
        } else if (value > MEGABYTES) {
          return (value / MEGABYTES).toFixed() + "MB"
        }
        return value
      },
      updateData() {
        metrics.get('/monitor')
        .then(response => {
          let processUsage = response.data['process.cpu.usage'][0].value * 100
          let systemUsage = response.data['system.cpu.usage'][0].value * 100

          this.jvmUsed = this.convertToSize(response.data['jvm.memory.used'][0].value)
          this.jvmMax = this.convertToSize(response.data['jvm.memory.max'][0].value)

          const copyData = [...this.processors];

          this.setItem(copyData[0].data, +systemUsage.toFixed(2), 7)
          this.setItem(copyData[1].data, +processUsage.toFixed(2), 7)

          this.processors = copyData

          let memoryUsage = response.data['jvm.memory.used'][0].value
          let memoryMax = response.data['jvm.memory.max'][0].value
          this.memory.usage = +((memoryUsage / memoryMax) * 100).toFixed(2)

          this.items = []
          for (let [key, value] of Object.entries(response.data)) {
            value.forEach(metric => this.items.push({'name': key, 'value': metric['value'], 'type': metric['statistic']}))
          }
        })
        .catch((err) => {
          console.log(err)
          clearInterval(this.timer)
        })
      },
      color(value) {
        let $color
        if (value <= 25) {
          $color = 'info'
        } else if (value > 25 && value <= 50) {
          $color = 'success'
        } else if (value > 50 && value <= 75) {
          $color = 'warning'
        } else if (value > 75 && value <= 100) {
          $color = 'danger'
        }
        return $color
      }
    }
  }
</script>

<style scoped>

</style>