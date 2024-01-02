<template>
  <CCard>
    <CCardHeader>
      {{ $t('workspaces.workspace_header') }}
      <div class="float-right">
        <CButton to="workspaces/create" class="mr-1" color="primary" :disabled="disabledButtons">{{ $t('workspaces.create_workspace_button') }}</CButton>
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
        :clickable-rows=false
        :loading="isLoading"
      >
        <template #edit_details="{item, index}">
          <td class="py-2">
            <CButton v-bind:to="'workspaces/edit/' + item.externalId + '?name=' + item.name"
                     color="primary"
                     variant="outline"
                     square
                     size="sm"
                     class="mr-1"
                     :disabled="disabledButtons"
            >
              {{ $t('common.edit_button') }}
            </CButton>
            <CButton v-bind:to="'/datasets?name=' + item.name"
                     color="primary"
                     variant="outline"
                     square
                     size="sm"
                     class="mr-1"
                     :disabled="disabledButtons"
            >
              {{ $t('common.datasets_button') }}
            </CButton>
            <CButton @click="showDeleteWarning(item, index)"
                     color="primary"
                     variant="outline"
                     square
                     size="sm"
                     :disabled="disabledButtons"
            >
              {{ $t('common.delete_button') }}
            </CButton>
          </td>
        </template>
      </CDataTable>
    </CCardBody>
    <!--
    <CCardFooter>
      <CButton to="workspaces/create" color="primary">Create workspace</CButton>
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
  name: 'Workspaces',
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
          key: 'owner',
          label: i18n.t('users.owner_label')
        },
        {
          key: 'groupName',
          label: i18n.t('users.group_name_label')
        },
        {
          key: 'dateCreated',
          label: i18n.t('common.created')
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
      rowNumber: -1,
      disabledButtons: false,
      dangerModal: false,
      currentItem: null,
      isLoading: false
    }
  },
  mounted() {
    this.isLoading = true
    api.get("workspace")
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
    deleteItem(item, index) {
      this.dangerModal = false
      this.isLoading = true

      api.delete("/workspace/" + item.externalId)
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
    workspaceLink (id) {
      return `workspaces/${id.toString()}`
    },
    rowClicked (item, index) {
      const workspaceLink = this.workspaceLink(item.externalId)
      this.$router.push({path: workspaceLink})
    }
  }
}
</script>
