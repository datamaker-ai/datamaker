<template>
  <CCard>
    <CCardHeader>
      {{ $t('jobs.job_executions_log_header') }}
    </CCardHeader>
    <CCardBody>
      <CDataTable
          hover
          :items="items"
          :fields="fields"
          :items-per-page="perPage"
          pagination
          :table-filter="{label: $t('table.filter_label'), placeholder: $t('table.filter_placeholder')}"
          index-column
          sorter
          :clickable-rows=false
          :loading="isLoading"
      >
        <template #state="data">
          <td>
            <CBadge :color="getBadge(data.item.state)">
              {{data.item.state}}
            </CBadge>
          </td>
        </template>
        <template #errors="data">
          <td style="">
            <div style="height: 10rem; width: 50rem; overflow-y: scroll;">{{data.item.errors}}</div>
          </td>
        </template>
        <template #replay="{item, index}">
          <td v-if="item.dataJobReplayable">
            <CButton v-if="!item.replay && item.replayLogFound" @click="replayJob(item, index)"><CIcon name="cil-media-play"/></CButton>
          </td>
        </template>
      </CDataTable>
    </CCardBody>
    <CCardFooter>

    </CCardFooter>
  </CCard>
</template>

<script>
  import {api} from "../../constants/api";
  import {i18n} from "@/plugins/i18n";

  export default {
    name: "JobExecutionLogs",
    data: () => {
      return {
        items: [],
        fields: [
          {
            key: 'state',
            label: i18n.t('jobs.job_logs_state')
          },
          {
            key: 'startTime',
            label: i18n.t('jobs.job_logs_start_time'),
            _style: 'white-space:nowrap; width: 15em;'
          },
          {
            key: 'endTime',
            label: i18n.t('jobs.job_logs_end_time'),
            _style: 'white-space:nowrap; width: 15em;'
          },
          {
            key: 'errors',
            label: i18n.t('jobs.errors'),
            sorter: false,
            filter: false,
            _style: 'white-space:nowrap; width: 150px; max-width: 150px;'
          },
          {
            key: 'results',
            label: i18n.t('jobs.results'),
            sorter: false,
            filter: false,
            _style: 'white-space:nowrap; min-width: 15em;'
          },
          {
            key: 'replay',
            label: 'Replay',
            _style: 'width:5px',
            sorter: false,
            filter: false
          }
        ],
        perPage: 10,
        isLoading: false
      }
    },
    mounted() {
      this.isLoading = true
      api.get(`/job-execution/${this.$route.params.id}/logs?size=9999`)
      .then(response => {
        this.items = response.data.payload.content
        this.isLoading = false
      })
      .catch(err => {})
    },
    paginationProps: {
      align: 'center',
      doubleArrows: false,
      previousButtonHtml: 'prev',
      nextButtonHtml: 'next'
    },
    methods: {
      getBadge (status) {
        return status === 'COMPLETED' ? 'success'
            : status === 'CANCELLED' ? 'warning'
            : status === 'FAILED' ? 'danger' : 'primary'
      },
      workspaceLink (id) {
        return `workspaces/${id.toString()}`
      },
      replayJob(item, index) {
        api.post(`/job-execution/${item.externalId}/replay`)
            .then(response => {
              this.$eventHub.$emit('show-toast', "Job replayed");
            })
            .catch(err => {})
      },
      scheduleJob () {

      },
      cancelJob () {

      },
      rowClicked (item, index) {
        const workspaceLink = this.workspaceLink(item.externalId)
        this.$router.push({path: workspaceLink})
      }
    }
  }
</script>

<style scoped>

</style>