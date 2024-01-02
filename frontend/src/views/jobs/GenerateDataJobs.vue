<template>
  <CCard>
    <CCardHeader>
      Generate data job
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
        <!--
        <template #username="data">
          <td>
            <strong>{{data.item.username}}</strong>
          </td>
        </template>

        <template #status="data">
          <td>
            <CBadge :color="getBadge(data.item.status)">
              {{data.item.status}}
            </CBadge>
          </td>
        </template>
        -->
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
              Edit
            </CButton>
            <CButton @click="showDeleteWarning(item, index)"
                     color="primary"
                     variant="outline"
                     square
                     size="sm"
                     class="mr-1"
                     :disabled="disabledButtons"
            >
              Delete
            </CButton>
            <CButton v-bind:to="{path:'/jobs/' + item.externalId + '/logs', query: {name: item.name}}"
                     color="primary"
                     variant="outline"
                     square
                     size="sm"
                     class="mr-1"
                     :disabled="disabledButtons"
            >
              View logs
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
        title="Delete warning"
        color="danger"
        :show.sync="dangerModal"
    >
      Your about to delete {{currentItem}}
      <template #footer>
        <CButton @click="dangerModal = false" color="danger">Discard</CButton>
        <CButton @click="deleteItem(currentItem, rowNumber)" color="success">Accept</CButton>
      </template>
    </CModal>
  </CCard>
</template>

<script>
  import {api} from "../../constants/api";

  export default {
    name: 'GenerateDataJobs',
    data: () => {
      return {
        items: [],
        fields: [
          { key: 'name' },
          { key: 'description' },
          { key: 'workspaceName', label: 'Workspace' },
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
        rowNumber: -1
      }
    },
    mounted() {
      this.isLoading = true
      api.get("/generate-data-job")
      .then(response => {
        this.items = response.data.payload
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
      showDeleteWarning(item, index) {
        this.dangerModal = true
        this.currentItem = item
        this.rowNumber = index
      },
      runJob(item, index) {
        api.post(`/job-execution/${item.externalId}/schedule`)
        .then(response => {
          console.log(response)
        })
        .catch(err => {})
      },
      stopJob(item, index) {
        api.delete(`/job-execution/${item.externalId}/schedule`)
        .then(response => {
          console.log(response)
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
        }).catch(err => {
          this.disabledButtons = false
          this.isLoading = false
        })
      },
      getBadge (status) {
        return status === 'Active' ? 'success'
            : status === 'Inactive' ? 'secondary'
                : status === 'Pending' ? 'warning'
                    : status === 'Banned' ? 'danger' : 'primary'
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
