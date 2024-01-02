<template>
  <CCard>
    <CCardHeader>
      {{ $t('system.manager_header') }}
      <div class="float-right">
        <CButton to="files/create" class="mr-1" color="primary" :disabled="disabledButtons">{{ $t('system.upload_file_button') }}</CButton>
      </div>
    </CCardHeader>
    <CCardBody>
      <CDataTable
          hover
          :fields="fields"
          :items="items"
          :items-per-page="perPage"
          pagination
          column-filter
          index-column
          striped
          sorter
          :items-per-page-select="{label: $t('table.items_label')}"
          :clickable-rows=false
          :loading="isLoading"
      >
        <template #edit_details="{item, index}">
          <td class="py-2">
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
      <CButton to="mappings/create" class="mr-1" color="primary" :disabled="disabledButtons">Create field mapping</CButton>
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
  import { api, getComponents } from "../../constants/api";
  import {i18n} from "@/plugins/i18n";

  export default {
    name: "FileManager",
    data() {
      return {
        fields: [
          {
            key: 'filename',
            label: i18n.t('system.filename_label')
          },
          {
            key: 'fileType',
            label: i18n.t('system.filetype_label')
          },
          {
            key: 'modified',
            label: i18n.t('system.modified_label')
          },
          {
            key: 'absolutePath',
            label: i18n.t('system.path_label')
          },
          {
            key: 'edit_details',
            label: '',
            _style: 'width:20%',
            sorter: false,
            filter: false
          }
        ],
        items: [],
        perPage: 5,
        isLoading: false,
        disabledButtons: false,
        dangerModal: false,
        currentItem: null,
        rowNumber: -1
      }
    },
    mounted() {
      this.isLoading = true
      api.get('/files')
      .then(response => {
        if (response.data && response.data.payload.length > 0) {
          this.items = response.data.payload
        }
        this.isLoading = false
      })
      .catch(err => {})
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
        api.delete("/files?filename=" + item.filename + "&type=" + item.fileType)
        .then(response => {
          this.disabledButtons = false
          this.isLoading = false
          this.items.splice(this.items.indexOf(item), 1)
        }).catch(err => {
          this.disabledButtons = false
          this.isLoading = false
        })
      },
    },
    paginationProps: {
      align: 'center',
      doubleArrows: false,
      previousButtonHtml: 'prev',
      nextButtonHtml: 'next'
    }
  }
</script>

<style scoped>

</style>