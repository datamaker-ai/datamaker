<template>
  <CCard>
    <CCardHeader>
      {{ $t('jobs.header') }}
      <div class="float-right">
        <CButton to="jobs/create" class="mr-1" color="primary" :disabled="disabledButtons">{{ $t('jobs.header') }}</CButton>
      </div>
    </CCardHeader>
    <CCardBody>
      <CDataTable
          hover
          :items="items"
          :fields="fields"
          :items-per-page="perPage"
          pagination
          column-filter
          :table-filter="{label: $t('table.filter_label'), placeholder: $t('table.filter_placeholder')}"
          index-column
          sorter
          :items-per-page-select="{label: $t('table.items_label')}"
          :clickable-rows=false
          :loading="isLoading"
      >
        <template #lastRunStatus="data">
          <td>
            <CBadge :color="getBadge(data.item.lastRunStatus)">
              {{data.item.lastRunStatus}}
            </CBadge>
          </td>
        </template>
        <template #edit_details="{item, index}">
          <td class="py-2">
            <CButton v-bind:to="{path:'/jobs/edit/' + item.externalId, query: {name: item.name}}"
                     color="primary"
                     variant="outline"
                     square
                     size="sm"
                     class="mr-1"
                     :disabled="disabledButtons"
            >
              {{ $t('common.edit_button') }}
            </CButton>
            <CButton @click="showDeleteWarning(item, index)"
                     color="primary"
                     variant="outline"
                     square
                     size="sm"
                     class="mr-1"
                     :disabled="disabledButtons"
            >
              {{ $t('common.delete_button') }}
            </CButton>
            <CButton v-bind:to="{path:'/jobs/' + item.externalId + '/logs', query: {name: item.name}}"
                     color="primary"
                     variant="outline"
                     square
                     size="sm"
                     class="mr-1"
                     :disabled="disabledButtons"
            >
              {{ $t('jobs.view_logs_button') }}
            </CButton>
            <CButton @click="runJob(item, index)"><CIcon name="cil-media-play"/></CButton>
            <CButton @click="stopJob(item, index)"><CIcon name="cil-media-stop" /></CButton>
          </td>
        </template>
      </CDataTable>
    </CCardBody>
    <!--
    <CCardFooter>
      <CButton to="jobs/create" class="mr-1" color="primary" :disabled="disabledButtons">Generate data job</CButton>
    </CCardFooter>
    -->
    <CModal
        :title="$t('common.delete_warning_title')"
        color="danger"
        :show.sync="dangerModal"
    >
      {{ $t('common.delete_warning_message', { item: currentItem ? currentItem.name : '' }) }}
      <template #footer>
        <CButton @click="dangerModal = false" color="danger">{{ $t('common.discard_action') }}</CButton>
        <CButton @click="deleteItem(currentItem, rowNumber)" color="success">{{ $t('common.accept_action') }}</CButton>
      </template>
    </CModal>
  </CCard>
</template>

<script>
  import {api} from "../../constants/api";
  import {i18n} from "@/plugins/i18n";

  export default {
    name: 'SummaryView',
    data: () => {
      return {
        items: [],
        fields: [
          {
            key: 'name',
            label: i18n.t('common.name')
          },
          {
            key: 'description',
            label: i18n.t('common.description')
          },
          {
            key: 'workspace',
            label: i18n.t('common.workspace')
          },
          {
            key: 'lastRunStatus',
            label: i18n.t('jobs.run_status_label')
          },
          {
            key: 'lastStartTime',
            label: i18n.t('jobs.start_time_label')
          },
          {
            key: 'nextRunSchedule',
            label: i18n.t('jobs.next_run_label')
          },
          {
            key: 'edit_details',
            label: '',
            _style: 'width:20%',
            sorter: false,
            filter: false
          }
        ],
        perPage: 10,
        isLoading: false,
        disabledButtons: false,
        dangerModal: false,
        currentItem: null,
        rowNumber: -1,
        timer: null
      }
    },
    mounted() {
      this.refreshData()
      this.timer = setInterval(this.refreshData, 5000)
    },
    beforeDestroy() {
      console.log("Stopping refresh interval...")
      clearInterval(this.timer)
      delete this.timer
    },
    paginationProps: {
      align: 'center',
      doubleArrows: false,
      previousButtonHtml: 'prev',
      nextButtonHtml: 'next'
    },
    methods: {
      refreshData() {
        //this.isLoading = true
        api.get("/generate-data-job/summary")
        .then(response => {
          this.items = response.data.payload
          this.isLoading = false
        })
        .catch(err => {})
      },
      showDeleteWarning(item, index) {
        this.dangerModal = true
        this.currentItem = item
        this.rowNumber = index
      },
      runJob(item, index) {
        api.post(`/job-execution/${item.externalId}/schedule`)
        .then(response => {
          this.$eventHub.$emit('show-toast', "Job scheduled");
        })
        .catch(err => {})
      },
      stopJob(item, index) {
        api.delete(`/job-execution/${item.externalId}/schedule`)
        .then(response => {
          this.$eventHub.$emit('show-toast', "Job canceled");
        })
        .catch(err => {})
      },
      deleteItem(item, index) {
        this.dangerModal = false
        this.isLoading = true

        api.delete(`/generate-data-job/${item.externalId}`)
        .then(response => {
          this.disabledButtons = false
          this.isLoading = false
          this.items.splice(this.items.indexOf(item), 1)
          this.$eventHub.$emit('show-toast', "Job deleted successfully");
        }).catch(err => {
          this.disabledButtons = false
          this.isLoading = false
        })
      },
      getBadge (status) {
        return status === 'COMPLETED' ? 'success'
            : status === 'CANCELLED' ? 'warning'
                : status === 'FAILED' ? 'danger' : 'primary'
      },
      dataJobLink (id) {
        return `generate-data-job/${id.toString()}`
      },
      rowClicked (item, index) {
        const dataJobLink = this.dataJobLink(item.externalId)
        this.$router.push({path: dataJobLink, params: {name: item.name}})
      }
    }
  }
</script>
