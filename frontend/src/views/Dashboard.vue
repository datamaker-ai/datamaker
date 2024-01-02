<template>
  <div>
    <CRow>
      <CCol sm="6" lg="3">
        <CCard>
          <CCardHeader>{{ $t('dashboard.fields_type_header') }}</CCardHeader>
          <CCardBody>
            <CChartPie
                    :options="{responsive: true, aspectRatio: 1}"
                    :datasets="[       {
                      backgroundColor: dynamicColor(Object.values(statistics.totalFieldsPerType).length),
                      data: Object.values(statistics.totalFieldsPerType)
                    } ]"
                    :labels="Object.keys(statistics.totalFieldsPerType)"
            />
          </CCardBody>
        </CCard>
      </CCol>
      <CCol sm="6" lg="3">
        <CCard>
          <CCardHeader>{{ $t('dashboard.sinks_type_header') }}</CCardHeader>
          <CCardBody>
            <CChartPie
                    :options="{responsive: true, aspectRatio: 1}"
                    :datasets="[       {
                      backgroundColor: dynamicColor(Object.values(statistics.totalSinksPerType).length),
                      data: Object.values(statistics.totalSinksPerType)
                    } ]"
                    :labels="Object.keys(statistics.totalSinksPerType)"
            />
          </CCardBody>
        </CCard>
      </CCol>
      <CCol sm="6" lg="3">
        <CCard>
          <CCardHeader>{{ $t('dashboard.generators_type_header') }}</CCardHeader>
          <CCardBody>
            <CChartPie
                    :options="{responsive: true, aspectRatio: 1}"
                    :datasets="[       {
                      backgroundColor: dynamicColor(Object.values(statistics.totalGeneratorsPerType).length),
                      data: Object.values(statistics.totalGeneratorsPerType)
                    } ]"
                    :labels="Object.keys(statistics.totalGeneratorsPerType)"
            />
          </CCardBody>
        </CCard>
      </CCol>
    </CRow>
    <CRow>
      <CCol sm="6" lg="3">
        <CWidgetDropdown color="primary" :header="statistics.totalJobExecutions.toString()" :text="$t('dashboard.jobs_week_text')">
          <template #footer>
            <CChartLineSimple
                    pointed
                    class="mt-3 mx-3"
                    style="height:70px"
                    :data-points="statistics.jobExecutionsPerWeeks"
                    point-hover-background-color="primary"
                    :label="$t('dashboard.jobs_week_label')"
            />
          </template>
        </CWidgetDropdown>
      </CCol>
      <CCol sm="6" lg="3">
        <CWidgetDropdown color="info" :header="statistics.totalRecordsGenerated.toString()" :text="$t('dashboard.records_week_text')">
          <template #footer>
            <CChartLineSimple
                    pointed
                    class="mt-3 mx-3"
                    style="height:70px"
                    :data-points="statistics.recordsGeneratedPerWeeks"
                    point-hover-background-color="info"
                    :label="$t('dashboard.records_label')"
            />
          </template>
        </CWidgetDropdown>
      </CCol>
      <CCol sm="6" lg="3">
        <CWidgetDropdown
                color="danger"
                :header="statistics.failedJobExecutions.toString()"
                :text="$t('dashboard.jobs_failed_label')"
        >
          <template #footer>
            <CChartLineSimple
                    pointed
                    class="mt-3 mx-3"
                    style="height:70px"
                    :data-points="statistics.failedJobExecutionsPerWeeks"
                    point-hover-background-color="danger"
                    :label="$t('dashboard.jobs_failed_label')"
                    labels="weeks"
            />
          </template>
        </CWidgetDropdown>
      </CCol>
    </CRow>
    <CCard>
      <CCardBody>
        <CRow>
          <CCol sm="5">
            <h4 id="traffic" class="card-title mb-0">{{ $t('dashboard.generate_data_jobs') }}</h4>
          </CCol>
        </CRow>
      </CCardBody>
      <CCardFooter>
        <CRow class="text-center">
          <CCol md sm="12" class="mb-sm-2 mb-0">
            <div class="text-muted">{{ $t('dashboard.job_running') }}</div>
            <strong>{{statistics.activeJobExecutions}}</strong>
            <!--
            <CProgress
              class="progress-xs mt-2"
              :precision="1"
              color="success"
              :value="getRunningPercentage()"
            />
            -->
          </CCol>
          <CCol md sm="12" class="mb-sm-2 mb-0 d-md-down-none">
            <div class="text-muted">{{ $t('dashboard.job_scheduled') }}</div>
            <strong>{{statistics.pendingJobExecutions}}
              ({{ getPendingPercentage() }}%)</strong>
            <CProgress
              class="progress-xs mt-2"
              :precision="1"
              color="info"
              :value="getPendingPercentage()"
            />
          </CCol>
          <CCol md sm="12" class="mb-sm-2 mb-0">
            <div class="text-muted">{{ $t('dashboard.job_run_once') }}</div>
            <strong>{{statistics.totalConfiguredDataJobs - statistics.pendingJobExecutions}}
              ({{ getRunOncePercentage() }}%)</strong>
            <CProgress
              class="progress-xs mt-2"
              :precision="1"
              :value="getRunOncePercentage()"
            />
          </CCol>
          <CCol md sm="12" class="mb-sm-2 mb-0">
            <div class="text-muted">{{ $t('dashboard.total') }}</div>
            <strong>{{statistics.totalConfiguredDataJobs}} {{ $t('dashboard.jobs') }}</strong>
          </CCol>
        </CRow>
      </CCardFooter>
    </CCard>
    <CRow>
      <CCol md="12">
        <CCard>
          <CCardHeader>
            {{ $t('dashboard.statistics_header') }}
          </CCardHeader>
          <CCardBody>
            <CRow>
              <CCol sm="12" lg="6">
                <CRow>
                  <CCol sm="6">
                    <CCallout color="info">
                      <small class="text-muted">{{ $t('dashboard.total_users') }}</small><br>
                      <strong class="h4">{{statistics.totalUsers}}</strong>
                    </CCallout>
                  </CCol>
                  <CCol sm="6">
                    <CCallout color="info">
                      <small class="text-muted">{{ $t('dashboard.total_groups') }}</small><br>
                      <strong class="h4">{{statistics.totalGroups}}</strong>
                    </CCallout>
                  </CCol>
                  <CCol sm="6">
                    <CCallout color="info">
                      <small class="text-muted">{{ $t('dashboard.total_fields') }}</small><br>
                      <strong class="h4">{{statistics.totalFields}}</strong>
                    </CCallout>
                  </CCol>
                  <CCol sm="6">
                    <CCallout color="info">
                      <small class="text-muted">{{ $t('dashboard.total_field_mappings') }}</small><br>
                      <strong class="h4">{{statistics.totalFieldMappings}}</strong>
                    </CCallout>
                  </CCol>
                </CRow>
              </CCol>
              <CCol sm="12" lg="6">
                <CRow>
                  <CCol sm="6">
                    <CCallout color="info">
                      <small class="text-muted">{{ $t('dashboard.total_datasets') }}</small><br>
                      <strong class="h4">{{statistics.totalDatasets}}</strong>
                    </CCallout>
                  </CCol>
                  <CCol sm="6">
                    <CCallout color="info">
                      <small class="text-muted">{{ $t('dashboard.total_sinks') }}</small><br>
                      <strong class="h4">{{statistics.totalSinks}}</strong>
                    </CCallout>
                  </CCol>
                  <CCol sm="6">
                    <CCallout color="info">
                      <small class="text-muted">{{ $t('dashboard.total_workspaces') }}</small><br>
                      <strong class="h4">{{statistics.totalWorkspaces}}</strong>
                    </CCallout>
                  </CCol>
                  <CCol sm="6">
                    <CCallout color="info">
                      <small class="text-muted">{{ $t('dashboard.total_generators') }}</small><br>
                      <strong class="h4">{{statistics.totalGenerators}}</strong>
                    </CCallout>
                  </CCol>
                </CRow>
              </CCol>
            </CRow>
            <br/>
          </CCardBody>
        </CCard>
      </CCol>
    </CRow>
  </div>
</template>

<script>
import WidgetsDropdown from './widgets/WidgetsDropdown'
import WidgetsBrand from './widgets/WidgetsBrand'
import {api, processState} from "../constants/api"
import {colorRangeCool, interpolateColors} from "../components/colors.js"
import {CChartLineSimple, CChartBarSimple} from './charts/index'
import {CChartPie} from '@coreui/vue-chartjs'

export default {
  name: 'Dashboard',
  components: {
    WidgetsDropdown,
    WidgetsBrand,
    CChartPie,
    CChartLineSimple,
    CChartBarSimple
  },
  data () {
    return {
      statistics: {
        "failedJobExecutions": 0,
        "failedJobExecutionsPerWeeks": [0],
        "totalJobExecutions": 0,
        "jobExecutionsPerWeeks": [0],
        "activeJobExecutions": 0,
        "pendingJobExecutions": 0,
        "totalConfiguredDataJobs": 0,
        "totalRecordsGenerated": 0,
        "recordsGeneratedPerWeeks": [0],
        "totalFieldsPerType": {"none":0},
        "totalSinks": 0,
        "totalSinksPerType": {"none": 0},
        "totalGenerators": 0,
        "totalGeneratorsPerType": {"none": 0},
        "totalUsers": 0,
        "totalDatasets": 0,
        "totalGroups": 0,
        "totalFields": 0,
        "totalWorkspaces": 0,
        "totalFieldMappings": 0
      },
      isLoading: {},
      selected: 'Month',
      timer: '',
      tableItems: [
      ],
      tableFields: [
      ]
    }
  },
  created() {
    this.updateData()
    this.timer = setInterval(this.updateData, 60 * 1000)
  },
  beforeDestroy() {
    clearInterval(this.timer)
    delete this.timer
  },
  methods: {
    getColors(values) {
      return interpolateColors(values.length, colorRangeCool)
    },
    getRunOncePercentage() {
      return Math.round(((this.statistics.totalConfiguredDataJobs - this.statistics.pendingJobExecutions) / this.statistics.totalConfiguredDataJobs * 100))
    },
    getRunningPercentage() {
      return Math.round(((this.statistics.activeJobExecutions) / this.statistics.totalConfiguredDataJobs * 100))
    },
    getPendingPercentage() {
      return Math.round(((this.statistics.pendingJobExecutions) / this.statistics.totalConfiguredDataJobs * 100))
    },
    dynamicColor(length) {
      let colors = []
      for (let i=0; i<length; i++) {
        let r = Math.floor(Math.random() * 255)
        let g = Math.floor(Math.random() * 255)
        let b = Math.floor(Math.random() * 255)
        colors.push("rgb(" + r + "," + g + "," + b + ")")
      }
      return colors
    },
    updateData() {
      api.get('dashboard')
          .then(response => {
            this.statistics = response.data.payload
            this.isLoading = false
          })
          .catch(err => {})
    },
    color (value) {
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
